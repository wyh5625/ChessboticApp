package com.example.ChessPlayerApp.robot_arm.Recognition;

import android.content.Context;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.ChessPlayerApp.R;
import com.example.ChessPlayerApp.chessboardcamera.Filter.AverageFIlter;
import com.example.ChessPlayerApp.chessboardcamera.ImageProcessor;
import com.example.ChessPlayerApp.chessboardcamera.KmeanCluster.Match;
import com.example.ChessPlayerApp.chessboardcamera.ZoomCameraView;
import com.example.ChessPlayerApp.robot_arm.Chess.ChessFragment;
import com.example.ChessPlayerApp.robot_arm.Chess.MoveCalculator;
import com.example.ChessPlayerApp.robot_arm.Chess.TheEngine;


import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import static com.example.ChessPlayerApp.robot_arm.Chess.ChessFragment.getNextMove;
import static com.example.ChessPlayerApp.robot_arm.Chess.TheEngine.terminal;
import static com.example.ChessPlayerApp.robot_arm.Chess.TheUserInterface.drawBoardPieces;


public class CameraFragment extends Fragment implements  CameraBridgeViewBase.CvCameraViewListener2{


    enum Mode {
        Normal,
        CalibrateChessBoard,
        MonitorGame,
        DetectMove,
        Contours,
        MatchChessBoard,
        HoughLine
    }

    private static CameraFragment instance;

    // camera
    // Loads camera view of OpenCV for us to use. This lets us see using OpenCV
    public static ZoomCameraView mZoomCameraView;

    public final static char W = 'w';
    public final static char B = 'b';
    public final static char E = '*';

    // Used in Camera selection from menu (when implemented)
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;

    private Mat currImg = null;
    // only update once
    private Match pt;

    Mode ImgProMethod = Mode.HoughLine;

    Button caliBtn;
    Button showBtn_PI;
    Button gameModeButton;
    Button showBtn_Edge;

    boolean showPI = false;
    boolean showEdge = false;
    boolean showPCL = false;

    boolean calibrated = false;

    int updateCount = 0;
    int period = 40;
    Mat lines;

    String gridPointsFileName = "chessboard_model.txt";
    String gridTransformFileName = "chessboard_tf.txt";


    public static int[][] curr_Intensity = new int[8][8];

    public static int[][] curr_Edges = new int[8][8];
    public static int[][] curr_PI = new int[8][8];
    public static int[][] curr_IntensityDev_raw = new int[8][8];

    public static int[][] last_PI = new int[8][8];

    public static int[][] last_Intensity = new int[8][8];
    public static int[][] last_Edges = new int[8][8];

    public static Mat ref_AOI = null;
    public static Mat AOI = null;

    public static Match mPointsAndTranf = null;

    //int[][] currIntensities = null;



    // localtion of white piece and black piece
    public static char[][] PCL = new char[][]{
            {B,B,B,B,B,B,B,B},
            {B,B,B,B,B,B,B,B},
            {E,E,E,E,E,E,E,E},
            {E,E,E,E,E,E,E,E},
            {E,E,E,E,E,E,E,E},
            {E,E,E,E,E,E,E,E},
            {W,W,W,W,W,W,W,W},
            {W,W,W,W,W,W,W,W}
    };
    public static char[][] lastPcl;
    public char[][] currPcl;


    // Filter
    AverageFIlter edgeFilter = new AverageFIlter(5);
    AverageFIlter intensityFilter = new AverageFIlter(5);
    AverageFIlter pieceIntenFilter = new AverageFIlter(5);
    AverageFIlter intenDevFilter = new AverageFIlter(5);


    public static int rotate_angle; // 0 = 0, 1 = 90, 2 = 180, 3 = -90


    public static CameraFragment getInstance(){
        if (instance == null)
            instance = new CameraFragment();

        return instance;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);


        View root = inflater.inflate(R.layout.fragment_camera, container, false);

        mZoomCameraView = root.findViewById(R.id.ZoomCameraView);
        mZoomCameraView.setVisibility(SurfaceView.VISIBLE);

        //mZoomCameraView.setCameraIndex(0);
        //mOpenCvCameraView.setScaleX(0.5f);
        //mOpenCvCameraView.setScaleY(0.5f);
        /*
        mZoomCameraView.setZoomControl(findViewById(R.id.CameraZoomControls));
        mZoomCameraView.setCvCameraViewListener(this);
         */
        mZoomCameraView.setCvCameraViewListener(this);
        mZoomCameraView.enableFpsMeter();
        mZoomCameraView.enableView();

        gameModeButton = root.findViewById(R.id.btn_1);
        gameModeButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                ImgProMethod = Mode.MonitorGame;
            }
        });

        showBtn_Edge = root.findViewById(R.id.btn_2);
        showBtn_Edge.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //ImgProMethod = Mode.MatchChessBoard;
                showPI = false;
                showEdge = true;
                showPCL = false;
            }
        });


        // deactivate when game start
        caliBtn = root.findViewById(R.id.btn_3);
        caliBtn.setOnClickListener(new View.OnClickListener() {
            // Need Click two times, first time: open this mode, second time capture to send to ChessFragment, then return to normal mode
            // This mode can only be used before game start, otherwise after the board occupied, the calibration gives wrong reference data of empty board.
            @Override
            public void onClick(View v) {
                ImgProMethod = Mode.CalibrateChessBoard;
                /*
                if(curr_Edges != null){
                    //copyIntArr(currEdges, last_Edges);
                    //copyIntArr(currIntensities, last_Intensity);
                    //copyIntArr(curr_PI, last_PI);
                    //ref_AOI = AOI;

                    // edited
                    // find the rotate angle, only check when the game is not started yet

                    if (!calibrated){
                        calibrated = true;
                        int bottomRow = 0;
                        int topRow = 0;
                        int leftColumn = 0;
                        int rightColumn = 0;
                        // bottom and top
                        for (int j = 0; j < 8; j ++){
                            bottomRow += curr_Intensity[6][j] + curr_Intensity[7][j];
                            topRow += curr_Intensity[0][j] + curr_Intensity[1][j];
                        }
                        for (int i = 0; i < 8; i ++){
                            leftColumn += curr_Intensity[i][0] + curr_Intensity[i][1];
                            rightColumn += curr_Intensity[i][6] + curr_Intensity[i][7];
                        }

                        int dark = Math.min(Math.min(Math.min(bottomRow, topRow), leftColumn), rightColumn);
                        if (bottomRow == dark){
                            rotate_angle = 2;
                        }else if (topRow == dark){
                            rotate_angle = 0;
                        }else if (dark == leftColumn){
                            rotate_angle = 1;
                        }else if (dark == rightColumn){
                            rotate_angle = 3;
                        }

                        //rotateArray(last_Intensity, rotate_angle);
                        //rotateArray(last_Edges, rotate_angle);
                    }

                    //Log.d("CaliAngle", "the rotate angle is: " + rotate_angle);
                }
                */

                lastPcl = new char[8][8];
                updatePcl(lastPcl, TheEngine.theBoard);
                //if (TheEngine.gameStarted)
                printPcl(lastPcl);

            }
        });

        caliBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // TODO Auto-generated method stub
                String cornerData = readInternalFile(getContext(), gridPointsFileName);
                String tfData = readInternalFile(getContext(), gridTransformFileName);


                if(cornerData != null && cornerData != null){
                    String[] cornerDataSplited = cornerData.split("\\s+");
                    String[] tfDataSplited = tfData.split("\\s+");

                    Point[][] ps = new Point[9][9];
                    for(int i = 0; i < 9; i ++){
                        for(int j = 0; j < 9; j++){
                            Point p = new Point(Double.parseDouble(cornerDataSplited[18*i+2*j+0]),Double.parseDouble(cornerDataSplited[18*i+2*j+1]));
                            ps[i][j] = p;
                        }
                    }
                    Mat tfMat = new Mat(3,3,org.opencv.core.CvType.CV_64FC1);
                    double[][] tf_data = new double[3][3];
                    for(int i = 0; i < 3; i ++) {
                        for (int j = 0; j < 3; j++) {
                            tf_data[i][j] = Double.parseDouble(tfDataSplited[3*i + j]);
                        }
                        tfMat.put(i,0,tf_data[i]);
                    }
                    pt = new Match(ps, tfMat);
                    mPointsAndTranf = pt;
                }
                return true;
            }
        });

        showBtn_PI = root.findViewById(R.id.btn_4);
        showBtn_PI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPI = true;
                showEdge = false;
                showPCL = false;
                /*
                // if game start, white turn,
                if (TheEngine.whiteTurn && TheEngine.gameStarted){
                    // check pcl
                    //copyCharArr(lastPcl, currPcl);
                    //currPcl[6][7] = '*';
                    //currPcl[5][7] = 'w';
                    String myMove = MoveCalculator.getMove(lastPcl, currPcl);
                    printPcl(lastPcl);
                    printPcl(currPcl);
                    Log.d("MyMove", "move: " + myMove);
                    if (myMove != "" && validMove(myMove)){
                        String query = terminal("myMove,"+myMove);

                        copyCharArr(currPcl, lastPcl);
                        copyIntArr(curr_Edges, last_Edges);
                        //copyIntArr(currIntensities, last_Intensity);
                        copyIntArr(curr_PI, last_PI);

                        AIWork();
                    }else{
                        Log.d("MyMove", "Not a valid move, please try again.");
                    }
                }

                 */
            }
        });


        return root;
    }


    public int[][] rotateArray(int[][] arr, int type){
        int[][] arr_copy = new int[8][8];
        copyIntArr(arr, arr_copy);

        switch (type){
            case 1: // clock wise 90
                for(int i = 0; i < 8; i ++)
                    for(int j = 0; j < 8; j ++){
                        arr_copy[j][7 - i] = arr[i][j];
                    }
                break;
            case 2:
                for(int i = 0; i < 8; i ++)
                    for(int j = 0; j < 8; j ++){
                        arr_copy[7 - i][7 - j] = arr[i][j];
                    }
                break;
            case 3:
                for(int i = 0; i < 8; i ++)
                    for(int j = 0; j < 8; j ++){
                        arr_copy[7 - i][i] = arr[i][j];
                    }
                break;
        }

        return arr_copy;
    }


    public void copyCharArr(char[][] from, char[][] to){
        for(int i = 0; i < 8; i ++)
            for(int j = 0; j < 8; j ++){
                to[i][j] = from[i][j];
            }
    }

    public void copyIntArr(int[][] from, int[][] to){
        for(int i = 0; i < 8; i ++)
            for(int j = 0; j < 8; j ++){
                to[i][j] = from[i][j];
            }
    }

    private void updateChessboardUI() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                drawBoardPieces();
            }
        });
    }

    public void AIWork(){

        //drawBoardPieces();
        updateChessboardUI();

        ChessFragment.wTurn = !ChessFragment.wTurn;


        // Since we moved, if it is not pass and play, make the computer movePiece.
        String moveOptions="";
        if (!ChessFragment.wTurn){
            moveOptions= terminal("suggestMove,black");
        } else {
            moveOptions= terminal("suggestMove,white");
        }
        if (moveOptions.isEmpty()) {
            //ChessFragment.staleOrCheckMate();
            // define camera friendly game end effect
            TheEngine.gameStarted = false;
        } else {
            getNextMove();

        }
        /*wait for the update on the chess board*/

        // update PCL from camera or theEngine?
        //updatePcl(lastPcl, TheEngine.theBoard);
        //printPcl(lastPcl);
    }

    void updatePcl(char[][] thePcl, char[] theBoard){
        for(int k = 0; k < 64; k ++){
            int i = 7 - k/8;
            int j = k%8;
            if(theBoard[k] == '*')
                thePcl[i][j] = E;
            else if(Character.isLowerCase(theBoard[k]))
                thePcl[i][j] = B;
            else
                thePcl[i][j] = W;
        }
    }

    boolean validMove(String myMove){
        String moveOptions= terminal("availMoves,"+String.valueOf(TheEngine.whiteTurn));
        String[] separated = moveOptions.split(",");
        if (Arrays.asList(separated).contains(myMove)) {
            return true;
        }else{
            return false;
        }
    }

    public void writeFileOnInternalStorage(Context mcoContext, String sFileName, String sBody){
        File dir = new File(mcoContext.getFilesDir(), "chessboard_data");
        if(!dir.exists()){
            dir.mkdir();
        }
        try {
            File gpxfile = new File(dir, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private String readInternalFile(Context mContext, String sFileName){

        String folder = mContext.getFilesDir().getAbsolutePath() + File.separator + "chessboard_data";
        File file = new File(folder, sFileName);

        if (!file.exists()) {
            return null;
        }

        FileInputStream fis = null;
        String textContent = "";

        try {
            fis = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            textContent = br.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return textContent;
    }

    public Mat CalibrateMode(Mat src){
        Mat grayMat = new Mat();

        updateCount ++;
        if (updateCount > period && pt == null){
            updateCount = 0;

            Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGR2GRAY);
            List<List<Point>> boardPoints = ImageProcessor.ClusterLinesAndIntersection(grayMat);
            pt = ImageProcessor.MatchChessboard(boardPoints);
            if (pt != null){
                mPointsAndTranf = pt;
                // write pt to internal storage
                String pointsString = "";
                for(int i = 0; i < 9; i++){
                    for(int j = 0; j < 9; j++){
                        pointsString += pt.points[i][j].x + " " + pt.points[i][j].y + " ";
                    }
                }
                String transformString = "";
                for(int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        transformString += pt.tranf.get(i,j)[0] + " ";
                    }
                }
                Log.e("Match data", pointsString);
                Log.e("Match data", transformString);
                Log.e("Match data", "" + pt.tranf.type());
                Log.e("Match data", pt.tranf.toString());
                //Log.e("Match data", pt.tranf.toString());

                writeFileOnInternalStorage(getContext(), gridPointsFileName, pointsString);
                writeFileOnInternalStorage(getContext(), gridTransformFileName, transformString);
            }
        }

        if (mPointsAndTranf != null){
            src = ImageProcessor.drawPoint(src, mPointsAndTranf.points);

            updateCurrEdgeAndIntensity(src);
            // store reference edges and intensities
            copyIntArr(curr_Edges, last_Edges);
            //copyIntArr(currIntensities, last_Intensity);
            copyIntArr(curr_PI, last_PI);
        }


        if(!calibrated)
            calibrated = true;

        return src;
    }

    public void updateCurrEdgeAndIntensity(Mat src){
        Mat grayMat = new Mat();
        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGR2GRAY);
        AOI = ImageProcessor.tranformInterestArea(grayMat, mPointsAndTranf.tranf);
        Mat AOI_color = ImageProcessor.tranformInterestArea(src, mPointsAndTranf.tranf);
        //Mat diff_AOI = AOI;
            /*
            if (ref_AOI != null) {
                Core.absdiff(AOI, ref_AOI, diff_AOI);
                diff_AOI = normalize(diff_AOI);
            }
            */
        edgeFilter.update(ImageProcessor.getEdges(AOI));
        intensityFilter.update(ImageProcessor.getIntensity(AOI));
        pieceIntenFilter.update(ImageProcessor.getPieceIntensity(AOI_color));
        //intenDevFilter.update(ImageProcessor.getIntensityDev(AOI));

        curr_Intensity = intensityFilter.getAvg();
        curr_Edges = edgeFilter.getAvg();
        curr_PI = pieceIntenFilter.getAvg();
    }


    public Mat GameMonitor(Mat src){

        showPCL = true;

        // Using the Tranf from calibration
        if (mPointsAndTranf != null){
            // get intensifies and edges
            updateCurrEdgeAndIntensity(src);

            //currIntensities = curr_Intensity;
            //rotateArray(curr_Intensity, rotate_angle);


            //rotateArray(curr_Edges, rotate_angle);

            //printPcl(lastPcl);
            printIntArr(curr_Edges);
            //printIntArr(currIntensities);

            src = ImageProcessor.drawPoint(src, mPointsAndTranf.points);
            //AOI = ImageProcessor.Canny2(AOI);
            //AOI.copyTo(src);
            //last_Intensity = intensities;
            //last_Edges = edges;
            if (calibrated){
                currPcl = ImageProcessor.getPieceColor(curr_Edges, last_Edges, curr_PI, last_PI, curr_IntensityDev_raw, lastPcl, curr_Intensity);
                printPcl(currPcl);
                // Show currPcl on mat
                for(int i = 0; i < 8; i ++)
                    for(int j = 0; j < 8; j ++){
                        if(showPCL)
                            Imgproc.putText(src, " "+ currPcl[i][j], mPointsAndTranf.points[i+1][j], Core.FONT_HERSHEY_COMPLEX, 0.6, new Scalar(255,255, 255,255), 1);
                        else if(showPI)
                            Imgproc.putText(src, " "+ curr_PI[i][j], mPointsAndTranf.points[i+1][j], Core.FONT_HERSHEY_COMPLEX, 0.6, new Scalar(255,255, 255,255), 1);
                        else if(showEdge)
                            Imgproc.putText(src, " "+ curr_Edges[i][j], mPointsAndTranf.points[i+1][j], Core.FONT_HERSHEY_COMPLEX, 0.6, new Scalar(255,255, 255,255), 1);
                        //Imgproc.putText(src, ""+ currIntensities[i][j], mPointsAndTranf.points[i][j+1], Core.FONT_HERSHEY_COMPLEX, 0.6, new Scalar(255,255, 255,255), 1);
                        //Log.d("Info", " E: "+ currEdges[i][j] + ", I: " + currIntensities[i][j]);
                    }
                if (ChessFragment.wTurn){
                    // only calibrated can you do:
                    if (TheEngine.gameStarted){
                        // check pcl
                        //copyCharArr(lastPcl, currPcl);
                        //currPcl[6][7] = '*';
                        //currPcl[5][7] = 'w';
                        String myMove = MoveCalculator.getMove(lastPcl, currPcl);
                        printPcl(lastPcl);
                        printPcl(currPcl);
                        Log.d("MyMove", "move: " + myMove);
                        if (myMove != "" && validMove(myMove)){
                            Log.d("MyMove","White turn: It's a valid move.");
                            String query = terminal("myMove,"+myMove);

                            copyCharArr(currPcl, lastPcl);

                            copyIntArr(curr_PI, last_PI);
                            copyIntArr(curr_Edges, last_Edges);
                            //copyIntArr(currIntensities, last_Intensity);


                            AIWork();

                        }
                    }
                    //currPcl = ImageProcessor.getPieceColor(currEdges, last_Edges, currIntensities, last_Intensity);
                    // calculate its move
                }else if(!ChessFragment.wTurn){
                    if (matchPclwithTheBoard(currPcl, TheEngine.theBoard)){
                        copyIntArr(curr_PI, last_PI);
                        copyIntArr(curr_Edges, last_Edges);
                        //copyIntArr(currIntensities, last_Intensity);
                        updatePcl(lastPcl, TheEngine.theBoard);
                        //copyCharArr(currPcl, lastPcl);

                        ChessFragment.wTurn = !ChessFragment.wTurn;

                        Log.d("MatchBoard", "AI Act the board updated");
                    }
                    // check whether currPcl match theBoard,

                }

            }


            // put data in original mat
            //Imgproc.putText(chessboardMat, (int)mu + "", new Point(start.x,start.y), Core.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(150,255, 23));

        }


        return src;
    }

    public boolean matchPclwithTheBoard(char[][] thePcl, char[] theBoard){
        //boolean matched = false;
        for(int k = 0; k < 64; k ++) {
            int i = 7 - k / 8;
            int j = k % 8;

            if(theBoard[k] == '*')
                if(thePcl[i][j] != E)
                    return false;
                else if(Character.isLowerCase(theBoard[k]))
                    if (thePcl[i][j] != B)
                        return false;
                    else
                    if (thePcl[i][j] != W)
                        return false;
        }
        return true;

    }

    public Mat normalize(Mat diffMat){
        for(int i = 0; i < 8; i ++)
            for(int j = 0; j < 8; j ++){
                Core.MinMaxLocResult re = Core.minMaxLoc(diffMat);
                int newVal = (int)(((diffMat.get(i,j)[0]-re.minVal)/(re.maxVal-re.minVal))*254);
                diffMat.put(i,j, new double[]{newVal});
            }

        return diffMat;
    }




    public void printPcl(char[][] pcl){
        String re = "The PCL table: \n";
        for(int i = 0; i < 8; i ++) {
            for (int j = 0; j < 8; j++) {
                re += pcl[i][j];
            }
            re += "\n";
        }
        Log.d("PCL", re);

    }

    public void printDoubleArr(double[][] arr){
        String re = "The double table: \n";
        for(int i = 0; i < 8; i ++) {
            for (int j = 0; j < 8; j++) {
                re += arr[i][j] + " ";
            }
            re += "\n";
        }
        Log.d("PCL", re);

    }

    public void printIntArr(int[][] arr){
        String re = "The Int table: \n";
        for(int i = 0; i < 8; i ++) {
            for (int j = 0; j < 8; j++) {
                re += arr[i][j] + " ";
            }
            re += "\n";
        }
        Log.d("PCL", re);

    }




    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d("CameraEvent", "Camera start");
    }

    @Override
    public void onCameraViewStopped() {
        Log.d("CameraEvent", "Camera stop");
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        /*
        Mat mRgba = inputFrame.rgba();
        Mat mRgbaT = mRgba.t();
        Core.flip(mRgba.t(), mRgbaT, 1);
        Imgproc.resize(mRgbaT, mRgbaT, mRgba.size());
         */

        Mat src = inputFrame.rgba();
        //Mat processed = null;
        Mat grayMat = new Mat();
        Mat hsvMat = new Mat();
        List<Mat> channels = new ArrayList<Mat>(3);
        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGR2GRAY);
        //Core.split(hsvMat, channels);
        currImg = src;
        //grayMat = channels.get(0);

        switch (ImgProMethod){
            case MonitorGame:
                src = GameMonitor(src);
                break;

            case CalibrateChessBoard:

                src = CalibrateMode(src);
                break;

            case MatchChessBoard:
                Point[][] chessPoints  = ImageProcessor.MatchChessboard(ImageProcessor.ClusterLinesAndIntersection(grayMat)).points;
                src = ImageProcessor.drawPoint(src, chessPoints);
                break;

            case Contours:
                src  = ImageProcessor.Contours(grayMat);
                break;

            case HoughLine:
                //Imgproc.cvtColor(mRgba, grayMat, Imgproc.COLOR_BGR2GRAY);
//                updateCount ++;
//                if(updateCount > period){
//                    updateCount = 0;
//                    lines = ImageProcessor.HoughLines(grayMat);
//                    //ImageProcessor.drawLines(src, lines);
//                }
//                if(lines != null)
//                    ImageProcessor.drawLines(src, lines);
                src = ImageProcessor.Canny(grayMat);

                break;
        }

        //Log.d("CameraTest", "It capture frame.");


        return src;
    }



}
