package com.example.ChessPlayerApp.robot_arm.Recognition;

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

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import static com.example.ChessPlayerApp.robot_arm.Chess.ChessFragment.getNextMove;
import static com.example.ChessPlayerApp.robot_arm.Chess.TheEngine.terminal;
import static com.example.ChessPlayerApp.robot_arm.Chess.TheEngine.whiteTurn;
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

    Mode ImgProMethod = Mode.HoughLine;

    Button caliBtn;
    Button moveBtn;
    Button gameModeButton;
    Button matchModeButton;

    boolean calibrated = false;


    public static int[][] curr_Intensity_raw = new int[8][8];
    public static int[][] curr_Edges_raw = new int[8][8];
    public static int[][] curr_PieceIntensity_raw = new int[8][8];
    public static int[][] curr_IntensityDev_raw = new int[8][8];

    public static int[][] last_PieceIntensity_raw = new int[8][8];

    public static int[][] last_Intensity = new int[8][8];
    public static int[][] last_Edges = new int[8][8];

    public static Mat ref_AOI = null;
    public static Mat AOI = null;

    int[][] currIntensities = null;
    int[][] currEdges = null;


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

        matchModeButton = root.findViewById(R.id.btn_2);
        matchModeButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                ImgProMethod = Mode.MatchChessBoard;
            }
        });


        // deactivate when game start
        caliBtn = root.findViewById(R.id.btn_3);
        caliBtn.setOnClickListener(new View.OnClickListener() {
            // Need Click two times, first time: open this mode, second time capture to send to ChessFragment, then return to normal mode
            // This mode can only be used before game start, otherwise after the board occupied, the calibration gives wrong reference data of empty board.
            @Override
            public void onClick(View v) {
                if(currEdges != null && currIntensities != null){
                    copyIntArr(currEdges, last_Edges);
                    copyIntArr(currIntensities, last_Intensity);
                    copyIntArr(curr_PieceIntensity_raw, last_PieceIntensity_raw);
                    ref_AOI = AOI;

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
                            bottomRow += curr_Intensity_raw[6][j] + curr_Intensity_raw[7][j];
                            topRow += curr_Intensity_raw[0][j] + curr_Intensity_raw[1][j];
                        }
                        for (int i = 0; i < 8; i ++){
                            leftColumn += curr_Intensity_raw[i][0] + curr_Intensity_raw[i][1];
                            rightColumn += curr_Intensity_raw[i][6] + curr_Intensity_raw[i][7];
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
                    Log.d("CaliAngle", "the rotate angle is: " + rotate_angle);

                }
                lastPcl = new char[8][8];
                updatePcl(lastPcl, TheEngine.theBoard);
                //if (TheEngine.gameStarted)
                printPcl(lastPcl);


                /*
                if(!firstClicked_cali){
                    firstClicked_cali = true;
                    ImgProMethod = Mode.CalibrateChessBoard;
                }else{
                    // calibrate mode
                    // Do calibration, only after you see the matchBoard, edges no and intensity are well shown can you click for the second time.
                    // Boolean Calibrate(Mat currImg)
                    if (calibrated) {
                        ImgProMethod = Mode.Normal;
                        firstClicked_cali = false;
                    }
                }

                 */
            }
        });

        moveBtn = root.findViewById(R.id.btn_4);
        moveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                        copyIntArr(currEdges, last_Edges);
                        copyIntArr(currIntensities, last_Intensity);
                        copyIntArr(curr_PieceIntensity_raw, last_PieceIntensity_raw);

                        AIWork();
                    }else{
                        Log.d("MyMove", "Not a valid move, please try again.");
                    }
                }
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



    public void AIWork(){
        drawBoardPieces();
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

    public Mat GameMonitor(Mat src){

        Mat grayMat = new Mat();
        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGR2GRAY);

        // get transformMat and chessboard points
        List<List<Point>> boardPoints = ImageProcessor.ClusterLinesAndIntersection(grayMat);
        Match pointsAndTranf = ImageProcessor.MatchChessboard(boardPoints);
        // get intensifies and edges
        if (pointsAndTranf.points != null){
            AOI = ImageProcessor.tranformInterestArea(grayMat, pointsAndTranf.tranf);
            Mat AOI_color = ImageProcessor.tranformInterestArea(src, pointsAndTranf.tranf);
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
            intenDevFilter.update(ImageProcessor.getIntensityDev(AOI));




            curr_Intensity_raw = intensityFilter.getAvg();
            curr_Edges_raw = edgeFilter.getAvg();
            curr_PieceIntensity_raw = pieceIntenFilter.getAvg();
            curr_IntensityDev_raw = intenDevFilter.getAvg();




            currIntensities = curr_Intensity_raw;
                    //rotateArray(curr_Intensity_raw, rotate_angle);
            currEdges = curr_Edges_raw;
            //rotateArray(curr_Edges_raw, rotate_angle);

            //printPcl(lastPcl);
            printIntArr(currEdges);
            printIntArr(currIntensities);

            src = ImageProcessor.drawPoint(src, pointsAndTranf.points);
            //AOI = ImageProcessor.Canny2(AOI);
            //AOI.copyTo(src);
            //last_Intensity = intensities;
            //last_Edges = edges;
            if (calibrated){
                currPcl = ImageProcessor.getPieceColor(currEdges, last_Edges, curr_PieceIntensity_raw, last_PieceIntensity_raw, curr_IntensityDev_raw, lastPcl);
                printPcl(currPcl);
                // Show currPcl on mat
                for(int i = 0; i < 8; i ++)
                    for(int j = 0; j < 8; j ++){
                        Imgproc.putText(src, " "+ currPcl[i][j], pointsAndTranf.points[i+1][j], Core.FONT_HERSHEY_COMPLEX, 0.6, new Scalar(255,255, 255,255), 1);
                        //Imgproc.putText(src, ""+ currIntensities[i][j], pointsAndTranf.points[i][j+1], Core.FONT_HERSHEY_COMPLEX, 0.6, new Scalar(255,255, 255,255), 1);
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
                        //printPcl(lastPcl);
                        //printPcl(currPcl);
                        Log.d("MyMove", "move: " + myMove);
                        if (myMove != "" && validMove(myMove)){
                            String query = terminal("myMove,"+myMove);

                            copyCharArr(currPcl, lastPcl);

                            copyIntArr(curr_PieceIntensity_raw, last_PieceIntensity_raw);
                            copyIntArr(currEdges, last_Edges);
                            copyIntArr(currIntensities, last_Intensity);


                            AIWork();

                        }
                    }
                    //currPcl = ImageProcessor.getPieceColor(currEdges, last_Edges, currIntensities, last_Intensity);
                    // calculate its move
                }else if(!ChessFragment.wTurn){
                    if (matchPclwithTheBoard(currPcl, TheEngine.theBoard)){
                        copyIntArr(curr_PieceIntensity_raw, last_PieceIntensity_raw);
                        copyIntArr(currEdges, last_Edges);
                        copyIntArr(currIntensities, last_Intensity);
                        copyCharArr(currPcl, lastPcl);

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
        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGR2GRAY);
        currImg = src;

        switch (ImgProMethod){
            case MonitorGame:
                src = GameMonitor(src);
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
                Mat lines = ImageProcessor.HoughLines(grayMat);
                src = ImageProcessor.drawLines(src, lines);
        }

        //Log.d("CameraTest", "It capture frame.");


        return src;
    }



}
