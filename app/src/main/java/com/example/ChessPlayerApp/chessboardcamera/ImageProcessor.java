package com.example.ChessPlayerApp.chessboardcamera;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import com.example.ChessPlayerApp.chessboardcamera.KmeanCluster.Centroid;
import com.example.ChessPlayerApp.chessboardcamera.KmeanCluster.Distance;
import com.example.ChessPlayerApp.chessboardcamera.KmeanCluster.HoughDistance;
import com.example.ChessPlayerApp.chessboardcamera.KmeanCluster.Intersection;
import com.example.ChessPlayerApp.chessboardcamera.KmeanCluster.KMeans;
import com.example.ChessPlayerApp.chessboardcamera.KmeanCluster.Line;
import com.example.ChessPlayerApp.chessboardcamera.KmeanCluster.LineWithPoint;
import com.example.ChessPlayerApp.chessboardcamera.KmeanCluster.Match;
import com.example.ChessPlayerApp.chessboardcamera.KmeanCluster.PointDistance;
import com.example.ChessPlayerApp.chessboardcamera.KmeanCluster.ThetaDistance;
import com.example.ChessPlayerApp.robot_arm.Recognition.CameraFragment;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;



public class ImageProcessor {

    public static final String TAG = "OPENCV_DEBUG";

    // key
    public static final String KEY_PREF_CANNY_THRES_1 = "canny_thres_1";
    public static final String KEY_PREF_CANNY_THRES_2 = "canny_thres_2";
    public static final String KEY_PREF_HOUGHLINE_THRES = "houghline_thres";
    public static final String KEY_PREF_HOUGHLINE_MINLINELENGTH = "houghline_minlinelength";
    public static final String KEY_PREF_HOUGHLINE_MAXLINEGAP = "houghline_maxlinegap";
    public static final String KEY_PREF_BINARY_THRES = "binary_thres";
    private static final int PERMISSION_REQUEST_CAMERA=9992;


    // value
    public static int cannyEdgeThres1 = 40;
    public static int cannyEdgeThres2 = 100;
    public static int houghLinesThres = 200;
    public static int houghLinesMinLineLength = 300;
    public static int houghLinesMaxLineGap = 500;
    public static int binaryThres = 120;


    private Bitmap currentBitmap;
    Mat originalMat;

    // perpective transform
    static Mat recentAppliedTransform;
    static Mat chessboardMatchTransform;

    public static final int blockSize = 100;
    public static final int AOI_indent = 10;
    public static final int AOI_height = 80;

    public static final int AOI_height_origin = 600;

    // AOI threshold = min number of canny edge
    public static final int AOI_canny_thres = 120;

    public static final int occpancy_thres = 80;

    static Point[][] detectedChessboardModel;




    private void loadImageToImageView()
    {
        //ImageView imgView = (ImageView) findViewById(R.id.image_view);
        //imgView.setImageBitmap(currentBitmap);
    }



    public String printArray(double[] values){
        String re = "";
        for (int i = 0; i < values.length; i ++){
            re += values[i] + " ";
        }
        return re;
    }

    public static Mat blurAndAT(Mat src){
        // its modified version of canny, it only detect canny edge of darker pixel, so that it can remove unnecessary detail. You can convert back to original one by removing code of binary mat
        Mat grayMat = new Mat();
        Mat cannyEdges = new Mat();
        Mat binaryMat = new Mat();

        //Converting the image to grayscale
        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGR2GRAY);
        //Imgproc.threshold(grayMat, binaryMat, binaryThres, 255, Imgproc.THRESH_BINARY);

        //Log.d("GrayScale", "get pixel value: " + printArray(grayMat.get(500,500)));

        Imgproc.medianBlur(grayMat, grayMat, 23);

        //Imgproc.threshold(grayMat, binaryMat, binaryThres, 255, Imgproc.THRESH_BINARY);
        Imgproc.adaptiveThreshold(grayMat, binaryMat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY, 9, 0);

        Imgproc.medianBlur(binaryMat, binaryMat, 5);


        //Converting Mat back to Bitmap
        //Utils.matToBitmap(binaryMat, currentBitmap);
        //loadImageToImageView();
        return binaryMat;
    }


    public static Mat Canny(Mat src)
    {
        // its modified version of canny, it only detect canny edge of darker pixel, so that it can remove unnecessary detail. You can convert back to original one by removing code of binary mat
        Mat grayMat = new Mat();
        Mat cannyEdges = new Mat();
        Mat binaryMat = new Mat();

        //Converting the image to grayscale
        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGR2GRAY);
        //Imgproc.threshold(grayMat, binaryMat, binaryThres, 255, Imgproc.THRESH_BINARY);

        Imgproc.medianBlur(grayMat, grayMat, 23);

        //Imgproc.threshold(grayMat, binaryMat, binaryThres, 255, Imgproc.THRESH_BINARY);
        Imgproc.adaptiveThreshold(grayMat, binaryMat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY, 9, 0);

        Imgproc.medianBlur(binaryMat, binaryMat, 5);

        //Log.d("THRES", cannyEdgeThres1 + " " + cannyEdgeThres2);
        Imgproc.Canny(binaryMat, cannyEdges, cannyEdgeThres1, cannyEdgeThres2);

       //Mat kernelDilate = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15,15));
        //Imgproc.dilate(binaryMat, binaryMat, kernelDilate);

        //Mat kernelErode = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5));
        //Imgproc.erode(binaryMat, binaryMat, kernelErode);


        return cannyEdges;
        //Converting Mat back to Bitmap
        //Utils.matToBitmap(cannyEdges, currentBitmap);
        //loadImageToImageView();
    }


    public static Mat HoughLines(Mat src) throws IOException {
        Mat grayMat = new Mat();
        Mat binaryMat = new Mat();
        Mat cannyEdges = new Mat();
        Mat hierarchy = new Mat();
        List<MatOfPoint> contourList = new
                ArrayList<MatOfPoint>();

        Mat lines = new Mat();
        //A list to store all the contours
        //Converting the image to grayscale

        //Converting the image to grayscale
        Imgproc.cvtColor(src,grayMat, Imgproc.COLOR_BGR2GRAY);



        Imgproc.Canny(grayMat, cannyEdges,cannyEdgeThres1, cannyEdgeThres2);

        // dilate the binary mat
        Mat kernelDilate = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9,9));
        Imgproc.dilate(cannyEdges, cannyEdges, kernelDilate);

        //finding contours
        Imgproc.findContours(cannyEdges,contourList
                ,hierarchy, Imgproc.RETR_CCOMP,
                Imgproc.CHAIN_APPROX_SIMPLE);

        if (contourList.size() == 0){
            return null;
        }

        int index = 0;
        double maxim = Imgproc.contourArea(contourList.get(0));
        for (int contourIdx = 1; contourIdx < contourList.size();
             contourIdx++) {
            double temp;
            temp= Imgproc.contourArea(contourList.get(contourIdx));
            if(maxim<temp)
            {
                maxim=temp;
                index=contourIdx;
            }
        }

        Mat contour_mask = Mat.zeros(cannyEdges.rows()
                ,cannyEdges.cols(), CvType.CV_8UC1);
        Imgproc.drawContours(contour_mask, contourList, index, new Scalar(255),
                -1);

/*
        //Drawing contours on a new image
        Mat contours = new Mat();
        contours.create(cannyEdges.rows()
                ,cannyEdges.cols(),CvType.CV_8UC3);
        Random r = new Random();
        for(int i = 0; i < contourList.size(); i++)
        {
            Imgproc.drawContours(contours
                    ,contourList,i,new Scalar(r.nextInt(255)
                            ,r.nextInt(255),r.nextInt(255)), -1);
        }
        */
        Mat masked_mat = new Mat();
        //Log.d("Size", "Size 1: " + contour_mask.size() + " Size 2: " + originalMat.size());
        Core.bitwise_and(contour_mask, grayMat, masked_mat);

        Imgproc.medianBlur(masked_mat, masked_mat, 23);

        //Imgproc.threshold(grayMat, binaryMat, binaryThres, 255, Imgproc.THRESH_BINARY);
        Imgproc.adaptiveThreshold(masked_mat, binaryMat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY, 9, 0);

        Imgproc.medianBlur(binaryMat, binaryMat, 5);

        Imgproc.Canny(binaryMat, cannyEdges, cannyEdgeThres1, cannyEdgeThres2);



        Imgproc.HoughLinesP(cannyEdges, lines, 1, Math.PI / 180, houghLinesThres, houghLinesMinLineLength, houghLinesMaxLineGap);

        //Imgproc.HoughLines(cannyEdges, lines, 1, Math.PI / 180, houghLinesThres, 0, 0, houghLinesMinLineLength, houghLinesMaxLineGap);

        Mat houghLines = src.clone();
        //houghLines.create(cannyEdges.rows(),
        //        cannyEdges.cols(), CvType.CV_8UC1);

        List<Line> crossLine = new ArrayList<Line>();

        //Drawing lines on the image
        Log.d("LENGTH", " " + lines.rows());

        // at least 2 lines for cluster
        if (lines.rows() < 2)
            return null;


        for (int i = 0; i < lines.rows(); i++) {
            double[] points = lines.get(i,0);
            Line l = new Line(points, src.width(), src.height());
            crossLine.add(l);

            Log.d("Line", "theta = " + l.getTheta() + " | Rtho = " + l.getRtho());
        }

        Map<Centroid, List<Line>> clusters = KMeans.fit(crossLine, 2, new ThetaDistance(), 100);

        //Random random = new Random();

        // Write line data to file for python analyzing
        //OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.openFileOutput("line_data.txt", Context.MODE_PRIVATE));
        // i indicates different clusters
        Scalar colors[] = new Scalar[]{new Scalar(255,0,0), new Scalar(0, 255, 0)};
        int i = 0;
        for(Centroid centroid: clusters.keySet()){
            //Scalar scalar = new Scalar(random.nextDouble()*255, random.nextDouble()*255, random.nextDouble()*255);
            for(Line line: clusters.get(centroid)){
                Imgproc.line(houghLines, line.getP1(), line.getP2(), colors[i], 1);

                //outputStreamWriter.write(i + " " + line.getTheta() + " " + line.getRtho() + "\n");
            }
            i++;
        }

        //outputStreamWriter.close();

        return houghLines;
        //Converting Mat back to Bitmap
        //Utils.matToBitmap(houghLines, currentBitmap);
        //loadImageToImageView();
    }


    // find maximum contour
    public static Mat Contours(Mat src)
    {
        Mat grayMat = new Mat();
        Mat binaryMat = new Mat();
        Mat cannyEdges = new Mat();
        Mat hierarchy = new Mat();
        List<MatOfPoint> contourList = new
                ArrayList<MatOfPoint>();

        Mat lines = new Mat();
        //A list to store all the contours
        //Converting the image to grayscale

        //Converting the image to grayscale
        Imgproc.cvtColor(src,grayMat, Imgproc.COLOR_BGR2GRAY);



        Imgproc.Canny(grayMat, cannyEdges,cannyEdgeThres1, cannyEdgeThres2);

        // dilate the binary mat
        Mat kernelDilate = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9,9));
        Imgproc.dilate(cannyEdges, cannyEdges, kernelDilate);

        //finding contours
        Imgproc.findContours(cannyEdges,contourList
                ,hierarchy, Imgproc.RETR_CCOMP,
                Imgproc.CHAIN_APPROX_SIMPLE);


        if (contourList.isEmpty()){
            return null;
        }
        int index = 0;
        double maxim = Imgproc.contourArea(contourList.get(0));
        for (int contourIdx = 1; contourIdx < contourList.size();
             contourIdx++) {
            double temp;
            temp= Imgproc.contourArea(contourList.get(contourIdx));
            if(maxim<temp)
            {
                maxim=temp;
                index=contourIdx;
            }
        }

        Mat contour_mask = Mat.zeros(cannyEdges.rows()
                ,cannyEdges.cols(), CvType.CV_8UC1);
        Imgproc.drawContours(contour_mask, contourList, index, new Scalar(255),
                -1);

/*
        //Drawing contours on a new image
        Mat contours = new Mat();
        contours.create(cannyEdges.rows()
                ,cannyEdges.cols(),CvType.CV_8UC3);
        Random r = new Random();
        for(int i = 0; i < contourList.size(); i++)
        {
            Imgproc.drawContours(contours
                    ,contourList,i,new Scalar(r.nextInt(255)
                            ,r.nextInt(255),r.nextInt(255)), -1);
        }
        */
        Mat masked_mat = new Mat();
        //Log.d("Size", "Size 1: " + contour_mask.size() + " Size 2: " + originalMat.size());
        Core.bitwise_and(contour_mask, grayMat, masked_mat);

        return masked_mat;
        //Converting Mat back to Bitmap
        //Utils.matToBitmap(masked_mat, currentBitmap);
        //loadImageToImageView();
    }

    // find maximum contour
    public static Mat SubContours(Mat src)
    {
        Mat grayMat = new Mat();
        Mat binaryMat = new Mat();
        Mat cannyEdges = new Mat();
        Mat hierarchy = new Mat();
        List<MatOfPoint> contourList = new
                ArrayList<MatOfPoint>();

        Mat lines = new Mat();
        //A list to store all the contours
        //Converting the image to grayscale

        /*
        Mat currentMat = new Mat(currentBitmap.getHeight(), currentBitmap.getWidth(), CvType.CV_8U);
        Utils.bitmapToMat(currentBitmap, currentMat);

         */

        //Converting the image to grayscale
        Imgproc.cvtColor(src,grayMat, Imgproc.COLOR_BGR2GRAY);





        Imgproc.Canny(grayMat, cannyEdges,cannyEdgeThres1, cannyEdgeThres2);

        // dilate the binary mat
        Mat kernelDilate = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9,9));
        Imgproc.dilate(cannyEdges, cannyEdges, kernelDilate);

        //finding contours
        Imgproc.findContours(cannyEdges,contourList
                ,hierarchy, Imgproc.RETR_CCOMP,
                Imgproc.CHAIN_APPROX_SIMPLE);

        /*
        int index = 0;
        double maxim = Imgproc.contourArea(contourList.get(0));
        for (int contourIdx = 1; contourIdx < contourList.size();
             contourIdx++) {
            double temp;
            temp=Imgproc.contourArea(contourList.get(contourIdx));
            if(maxim<temp)
            {
                maxim=temp;
                index=contourIdx;
            }
        }

        Mat contour_mask = Mat.zeros(cannyEdges.rows()
                ,cannyEdges.cols(), CvType.CV_8UC1);
        Imgproc.drawContours(contour_mask, contourList, index, new Scalar(255),
                -1);

         */


        //Drawing contours on a new image
        Mat contours = new Mat();
        contours.create(cannyEdges.rows()
                ,cannyEdges.cols(), CvType.CV_8UC3);
        Random r = new Random();
        for(int i = 0; i < contourList.size(); i++)
        {
            Imgproc.drawContours(contours
                    ,contourList,i,new Scalar(r.nextInt(255)
                            ,r.nextInt(255),r.nextInt(255)), -1);
        }

        /*
        Mat masked_mat = new Mat();
        //Log.d("Size", "Size 1: " + contour_mask.size() + " Size 2: " + originalMat.size());
        Core.bitwise_and(contour_mask, grayMat, masked_mat);

         */



        return contours;
        //Converting Mat back to Bitmap
        //Utils.matToBitmap(contours, currentBitmap);
        //loadImageToImageView();
    }


    public static Mat ClusterLinesAndIntersection(Mat src){
        /**mask part**/
        Mat grayMat = new Mat();
        Mat binaryMat = new Mat();
        Mat cannyEdges = new Mat();
        Mat hierarchy = new Mat();
        List<MatOfPoint> contourList = new
                ArrayList<MatOfPoint>();
        //A list to store all the contours
        //Converting the image to grayscale

        /* Canny Edge */
        //Converting the image to grayscale
        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGR2GRAY);


        Imgproc.Canny(grayMat, cannyEdges,cannyEdgeThres1, cannyEdgeThres2);

        // dilate the binary mat
        Mat kernelDilate = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15,15));
        Imgproc.dilate(cannyEdges, cannyEdges, kernelDilate);

        /* Contour */
        //finding contours
        Imgproc.findContours(cannyEdges,contourList
                ,hierarchy, Imgproc.RETR_CCOMP,
                Imgproc.CHAIN_APPROX_SIMPLE);

        if (contourList.size() == 0)
            return src;

        // find largest contour
        int index = 0;
        double maxim = Imgproc.contourArea(contourList.get(0));
        for (int contourIdx = 1; contourIdx < contourList.size();
             contourIdx++) {
            double temp;
            temp= Imgproc.contourArea(contourList.get(contourIdx));
            if(maxim<temp)
            {
                maxim=temp;
                index=contourIdx;
            }
        }

        // get mask with in shape of largest contour
        Mat contour_mask = Mat.zeros(cannyEdges.rows()
                ,cannyEdges.cols(), CvType.CV_8UC1);
        Imgproc.drawContours(contour_mask, contourList, index, new Scalar(255),
                -1);

        Log.d("Contour", "size of contour is: " + contourList.get(index).size());

        /*
        // find outer corners of chessboard
        double epsilon = 0.1*Imgproc.arcLength(new MatOfPoint2f(contourList.get(index).toArray()),true);
        MatOfPoint2f approx = new MatOfPoint2f();
        Imgproc.approxPolyDP(new MatOfPoint2f(contourList.get(index).toArray()),approx,epsilon,true);
        Log.d("Contour corner", "Corner: " + approx.rows());

         */

        /*
        //Drawing all contours on a new image
        Mat contours = new Mat();
        contours.create(cannyEdges.rows()
                ,cannyEdges.cols(),CvType.CV_8UC3);
        Random r = new Random();
        for(int i = 0; i < contourList.size(); i++)
        {
            Imgproc.drawContours(contours
                    ,contourList,i,new Scalar(r.nextInt(255)
                            ,r.nextInt(255),r.nextInt(255)), -1);
        }
        */

        // apply mask on grayMat
        Mat masked_mat = new Mat();
        //Log.d("Size", "Size 1: " + contour_mask.size() + " Size 2: " + originalMat.size());
        Core.bitwise_and(contour_mask, grayMat, masked_mat);


        /**Hough Line part**/

        Mat lines = new Mat();
        /* find hough line */

        Imgproc.medianBlur(masked_mat, masked_mat, 23);

        //Imgproc.threshold(grayMat, binaryMat, binaryThres, 255, Imgproc.THRESH_BINARY);
        Imgproc.adaptiveThreshold(masked_mat, binaryMat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY, 9, 0);

        Imgproc.medianBlur(binaryMat, binaryMat, 5);

        Imgproc.Canny(binaryMat, cannyEdges, cannyEdgeThres1, cannyEdgeThres2);

        Imgproc.HoughLinesP(cannyEdges, lines, 1, Math.PI / 180, houghLinesThres, houghLinesMinLineLength, houghLinesMaxLineGap);

        //Imgproc.HoughLines(cannyEdges, lines, 1, Math.PI / 180, houghLinesThres, 0, 0, houghLinesMinLineLength, houghLinesMaxLineGap);

        Mat houghLines = src.clone();


        if (lines.rows() < 2)
            return src;

        List<Line> crossLine = new ArrayList<Line>();

        Log.d("LENGTH", " " + lines.rows());

        // store line in list
        for (int i = 0; i < lines.rows(); i++) {
            double[] points = lines.get(i,0);
            Line l = new Line(points, src.width(), src.height());
            crossLine.add(l);
        }

        /* cluster lines in two groups -- horizontal group and vertical group */


        Map<Centroid, List<Line>> clusters = KMeans.fit(crossLine, 2, new HoughDistance(), 500);

        //Map<Centroid, List<Line>> clusters_a = KMeans.fit2((List<Line>)clusters.values().toArray()[0], 2, new HoughDistance(), 500);

        /*  cluster lines in each group by crossPointCluster -- expected 9,9  */
        Centroid verticalCent;
        Centroid horizontalCent;

        Centroid firstCent = (Centroid)clusters.keySet().toArray()[0];
        Centroid secondCent = (Centroid)clusters.keySet().toArray()[1];
        Log.d("Orientation", "first cent: " + Math.abs(firstCent.line.getTheta()));
        Log.d("Orientation", "second cent: " + Math.abs(secondCent.line.getTheta()));
        if(Math.abs(firstCent.line.getTheta()) > Math.abs(secondCent.line.getTheta())){
            horizontalCent = firstCent;
            verticalCent = secondCent;
        }else{
            horizontalCent = secondCent;
            verticalCent = firstCent;
        }

        List<LineWithPoint> lineWithPoints_v = new ArrayList<>();
        List<LineWithPoint> lineWithPoints_h = new ArrayList<>();

        // calculate intersection points between all vercital line and a horizoncel line

        // get one line of another group
        Line hzLine = clusters.get(horizontalCent).get(0);
        Line vtLine = clusters.get(verticalCent).get(0);

        for(Line line: clusters.get(verticalCent)){
            Point ins = Intersection.calculate(line.getP1(), line.getP2(), hzLine.getP1(), hzLine.getP2());
            lineWithPoints_v.add(new LineWithPoint(line, ins));
        }

        for(Line line: clusters.get(horizontalCent)){
            Point ins = Intersection.calculate(line.getP1(), line.getP2(), vtLine.getP1(), vtLine.getP2());
            lineWithPoints_h.add(new LineWithPoint(line, ins));
        }

        // cluster cross points by coordinates
        Map<Centroid, List<LineWithPoint>> verticalIntersectionClusters = KMeans.fit2_point(lineWithPoints_v, 60, new PointDistance(), 500);
        Map<Centroid, List<LineWithPoint>> horizontalIntersectionClusters = KMeans.fit2_point(lineWithPoints_h, 60, new PointDistance(), 500);

        // sort verticalIntersectionClusters's keys in order according to their point
        Map<Centroid, List<LineWithPoint>> sortedVerticalClusters = new TreeMap<>(new CentroidComparator());
        Map<Centroid, List<LineWithPoint>> sortedHorizontalClusters = new TreeMap<>(new CentroidComparator());

        /*
        for (Centroid ce : verticalIntersectionClusters.keySet()){
            if (ce == null){
                Log.d("CentroidNl", "Is Centroid Null: " + ce);
                Log.d("CentroidNl", "Size of values: " + verticalIntersectionClusters.get(ce).size());
            }
        }

         */
        sortedVerticalClusters.putAll(verticalIntersectionClusters);
        sortedHorizontalClusters.putAll(horizontalIntersectionClusters);

        // convert to standard cluster type for looping
        ArrayList<List<Line>> sortedVerticalClusters_SD = new ArrayList<>();
        ArrayList<List<Line>> sortedHorizontalClusters_SD = new ArrayList<>();

        for(Map.Entry<Centroid, List<LineWithPoint>> entry: sortedVerticalClusters.entrySet()){
            // Convert List<LineWithPoint> to List<Line>
            Log.d("Point", "GP ---------");
            List<Line> gp = new ArrayList<>();
            for(LineWithPoint lp: entry.getValue()){
                gp.add(lp.line);
                Log.d("Point", " x = " + lp.point.x + " y = " + lp.point.y);
            }
            sortedVerticalClusters_SD.add(gp);
        }

        for(Map.Entry<Centroid, List<LineWithPoint>> entry: sortedHorizontalClusters.entrySet()){
            // Convert List<LineWithPoint> to List<Line>
            Log.d("Point", "GP ---------");
            List<Line> gp = new ArrayList<>();
            for(LineWithPoint lp: entry.getValue()){
                gp.add(lp.line);
                Log.d("Point", " x = " + lp.point.x + " y = " + lp.point.y);
            }
            sortedHorizontalClusters_SD.add(gp);
        }



        Random random = new Random();

        // Write line data to file for python analyzing
        // i indicates different clusters

        int colors[] = new int[]{
                Color.BLUE,
                Color.YELLOW,
                Color.CYAN,
                Color.MAGENTA,
                Color.BLACK,
                Color.DKGRAY,
                Color.GRAY,
                Color.LTGRAY,
                Color.WHITE,
                Color.RED,
                Color.GREEN,
                0x663366,
                0xFF6666,
                0xCCCC00,
                0x734C00,
                0x3CCC00
        };


        /* Calculate mean line of each group and removing duplicated line */
        List<Line> verticalCrossLine = new ArrayList<>();
        List<Line> horizontalCrossLine = new ArrayList<>();
        //int i = 0;
        for(List<Line> line_gp: sortedVerticalClusters_SD){
            Scalar scalar = new Scalar(random.nextDouble()*255, random.nextDouble()*255, random.nextDouble()*255, 255);
            // Log.d("Line", "Color: " + scalar);
            Line meanLine = calculateMeanLine(line_gp);
            verticalCrossLine.add(meanLine);
            Imgproc.line(houghLines, meanLine.getP1(), meanLine.getP2(), scalar, 1);
            //i++;
        }

        //i = 0;
        for(List<Line> line_gp: sortedHorizontalClusters_SD){
            Scalar scalar = new Scalar(random.nextDouble()*255, random.nextDouble()*255, random.nextDouble()*255, 255);
            //Log.d("Line", "Color: " + scalar);
            Line meanLine = calculateMeanLine(line_gp);
            horizontalCrossLine.add(meanLine);
            Imgproc.line(houghLines, meanLine.getP1(), meanLine.getP2(), scalar, 1);
            //i++;
        }


        /*for(int i = 0; i < approx.rows(); i++){
            Point p = new Point(approx.get(i, 0));
            Imgproc.circle(masked_mat, p,
                    10, new Scalar(255,0,0), 2);
        }*/



        /*  calculate intersection points between two refined groups */
        List<List<Point>> intersectPoints = new ArrayList<>();

        for(Line line_a: horizontalCrossLine){
            List<Point> rowPoints = new ArrayList<>();
            for(Line line_b: verticalCrossLine){
                rowPoints.add(Intersection.calculate(line_a.getP1(), line_a.getP2(), line_b.getP1(), line_b.getP2()));
            }
            intersectPoints.add(rowPoints);
        }


        for(List<Point> row : intersectPoints){
            for(Point p : row)
                Imgproc.circle(houghLines, p,
                        5, new Scalar(0,255,0, 255), 1);
            //outputStreamWriter.write(i + " " + p.x + " " + p.y + "\n");
        }
        /*
        //Converting Mat back to Bitmap
        Utils.matToBitmap(houghLines, currentBitmap);
        loadImageToImageView();

         */
        return houghLines;


    }



    public static Mat MatchChessboard(Mat src){

        /**mask part**/
        Mat grayMat = new Mat();
        Mat binaryMat = new Mat();
        Mat cannyEdges = new Mat();
        Mat hierarchy = new Mat();
        List<MatOfPoint> contourList = new
                ArrayList<MatOfPoint>();
        //A list to store all the contours
        //Converting the image to grayscale

        /* Canny Edge */
        //Converting the image to grayscale
        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGR2GRAY);

        Imgproc.Canny(grayMat, cannyEdges,cannyEdgeThres1, cannyEdgeThres2);

        // dilate the binary mat
        Mat kernelDilate = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15,15));
        Imgproc.dilate(cannyEdges, cannyEdges, kernelDilate);

        /* Contour */
        //finding contours
        Imgproc.findContours(cannyEdges,contourList
                ,hierarchy, Imgproc.RETR_CCOMP,
                Imgproc.CHAIN_APPROX_SIMPLE);

        if(contourList.size() == 0)
            return null;


        // find largest contour
        int index = 0;
        double maxim = Imgproc.contourArea(contourList.get(0));
        for (int contourIdx = 1; contourIdx < contourList.size();
             contourIdx++) {
            double temp;
            temp= Imgproc.contourArea(contourList.get(contourIdx));
            if(maxim<temp)
            {
                maxim=temp;
                index=contourIdx;
            }
        }

        // get mask with in shape of largest contour
        Mat contour_mask = Mat.zeros(cannyEdges.rows()
                ,cannyEdges.cols(), CvType.CV_8UC1);
        Imgproc.drawContours(contour_mask, contourList, index, new Scalar(255),
                -1);

        Log.d("Contour", "size of contour is: " + contourList.get(index).size());

        /*
        // find outer corners of chessboard
        double epsilon = 0.1*Imgproc.arcLength(new MatOfPoint2f(contourList.get(index).toArray()),true);
        MatOfPoint2f approx = new MatOfPoint2f();
        Imgproc.approxPolyDP(new MatOfPoint2f(contourList.get(index).toArray()),approx,epsilon,true);
        Log.d("Contour corner", "Corner: " + approx.rows());

         */

        /*
        //Drawing all contours on a new image
        Mat contours = new Mat();
        contours.create(cannyEdges.rows()
                ,cannyEdges.cols(),CvType.CV_8UC3);
        Random r = new Random();
        for(int i = 0; i < contourList.size(); i++)
        {
            Imgproc.drawContours(contours
                    ,contourList,i,new Scalar(r.nextInt(255)
                            ,r.nextInt(255),r.nextInt(255)), -1);
        }
        */

        // apply mask on grayMat
        Mat masked_mat = new Mat();
        //Log.d("Size", "Size 1: " + contour_mask.size() + " Size 2: " + originalMat.size());
        Core.bitwise_and(contour_mask, grayMat, masked_mat);


        /**Hough Line part**/

        Mat lines = new Mat();
        /* find hough line */

        Imgproc.medianBlur(masked_mat, masked_mat, 23);

        //Imgproc.threshold(grayMat, binaryMat, binaryThres, 255, Imgproc.THRESH_BINARY);
        Imgproc.adaptiveThreshold(masked_mat, binaryMat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY, 9, 0);

        Imgproc.medianBlur(binaryMat, binaryMat, 5);

        Imgproc.Canny(binaryMat, cannyEdges, cannyEdgeThres1, cannyEdgeThres2);

        Imgproc.HoughLinesP(cannyEdges, lines, 1, Math.PI / 180, houghLinesThres, houghLinesMinLineLength, houghLinesMaxLineGap);

        //Imgproc.HoughLines(cannyEdges, lines, 1, Math.PI / 180, houghLinesThres, 0, 0, houghLinesMinLineLength, houghLinesMaxLineGap);

        Mat houghLines = src.clone();



        List<Line> crossLine = new ArrayList<Line>();

        Log.d("LENGTH", " " + lines.rows());

        if (lines.rows() < 2)
            return null;

        // store line in list
        for (int i = 0; i < lines.rows(); i++) {
            double[] points = lines.get(i,0);
            Line l = new Line(points, src.width(), src.height());
            crossLine.add(l);
        }

        /* cluster lines in two groups -- horizontal group and vertical group */


        Map<Centroid, List<Line>> clusters = KMeans.fit(crossLine, 2, new HoughDistance(), 500);

        //Map<Centroid, List<Line>> clusters_a = KMeans.fit2((List<Line>)clusters.values().toArray()[0], 2, new HoughDistance(), 500);

        /*  cluster lines in each group by crossPointCluster -- expected 9,9  */
        Centroid verticalCent;
        Centroid horizontalCent;

        Centroid firstCent = (Centroid)clusters.keySet().toArray()[0];
        Centroid secondCent = (Centroid)clusters.keySet().toArray()[1];
        //Log.d("Orientation", "first cent: " + Math.abs(firstCent.line.getTheta()));
        //Log.d("Orientation", "second cent: " + Math.abs(secondCent.line.getTheta()));
        if(Math.abs(firstCent.line.getTheta()) > Math.abs(secondCent.line.getTheta())){
            horizontalCent = secondCent;
            verticalCent = firstCent;
        }else{
            horizontalCent = firstCent;
            verticalCent = secondCent;
        }

        List<LineWithPoint> lineWithPoints_v = new ArrayList<>();
        List<LineWithPoint> lineWithPoints_h = new ArrayList<>();

        // calculate intersection points between all vercital line and a horizoncel line

        // get one line of another group
        Line hzLine = clusters.get(horizontalCent).get(0);
        Line vtLine = clusters.get(verticalCent).get(0);

        for(Line line: clusters.get(verticalCent)){
            Point ins = Intersection.calculate(line.getP1(), line.getP2(), hzLine.getP1(), hzLine.getP2());
            lineWithPoints_v.add(new LineWithPoint(line, ins));
        }

        for(Line line: clusters.get(horizontalCent)){
            Point ins = Intersection.calculate(line.getP1(), line.getP2(), vtLine.getP1(), vtLine.getP2());
            lineWithPoints_h.add(new LineWithPoint(line, ins));
        }

        // cluster cross points by coordinates
        Map<Centroid, List<LineWithPoint>> verticalIntersectionClusters = KMeans.fit2_point(lineWithPoints_v, 60, new PointDistance(), 500);
        Map<Centroid, List<LineWithPoint>> horizontalIntersectionClusters = KMeans.fit2_point(lineWithPoints_h, 60, new PointDistance(), 500);

        // sort verticalIntersectionClusters's keys in order according to their point
        Map<Centroid, List<LineWithPoint>> sortedVerticalClusters = new TreeMap<>(new CentroidComparator());
        Map<Centroid, List<LineWithPoint>> sortedHorizontalClusters = new TreeMap<>(new CentroidComparator());
        sortedVerticalClusters.putAll(verticalIntersectionClusters);
        sortedHorizontalClusters.putAll(horizontalIntersectionClusters);

        // convert to standard cluster type for looping
        ArrayList<List<Line>> sortedVerticalClusters_SD = new ArrayList<>();
        ArrayList<List<Line>> sortedHorizontalClusters_SD = new ArrayList<>();

        for(Map.Entry<Centroid, List<LineWithPoint>> entry: sortedVerticalClusters.entrySet()){
            // Convert List<LineWithPoint> to List<Line>
            Log.d("Point", "GP ---------");
            List<Line> gp = new ArrayList<>();
            for(LineWithPoint lp: entry.getValue()){
                gp.add(lp.line);
                Log.d("Point", " x = " + lp.point.x + " y = " + lp.point.y);
            }
            sortedVerticalClusters_SD.add(gp);
        }

        for(Map.Entry<Centroid, List<LineWithPoint>> entry: sortedHorizontalClusters.entrySet()){
            // Convert List<LineWithPoint> to List<Line>
            Log.d("Point", "GP ---------");
            List<Line> gp = new ArrayList<>();
            for(LineWithPoint lp: entry.getValue()){
                gp.add(lp.line);
                Log.d("Point", " x = " + lp.point.x + " y = " + lp.point.y);
            }
            sortedHorizontalClusters_SD.add(gp);
        }



        Random random = new Random();

        // Write line data to file for python analyzing
        // OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.openFileOutput("line_data.txt", Context.MODE_PRIVATE));
        // i indicates different clusters

        int colors[] = new int[]{
                Color.BLUE,
                Color.YELLOW,
                Color.CYAN,
                Color.MAGENTA,
                Color.BLACK,
                Color.DKGRAY,
                Color.GRAY,
                Color.LTGRAY,
                Color.WHITE,
                Color.RED,
                Color.GREEN,
                0x663366,
                0xFF6666,
                0xCCCC00,
                0x734C00,
                0x3CCC00
        };


        /* Calculate mean line of each group and removing duplicated line */
        List<Line> verticalCrossLine = new ArrayList<>();
        List<Line> horizontalCrossLine = new ArrayList<>();
        //int i = 0;
        for(List<Line> line_gp: sortedVerticalClusters_SD){
            Scalar scalar = new Scalar(random.nextDouble()*255, random.nextDouble()*255, random.nextDouble()*255, 255);
            //Log.d("Line", "Color: " + scalar);
            Line meanLine = calculateMeanLine(line_gp);
            verticalCrossLine.add(meanLine);
            Imgproc.line(houghLines, meanLine.getP1(), meanLine.getP2(), scalar, 1);
            //i++;
        }

        //i = 0;
        for(List<Line> line_gp: sortedHorizontalClusters_SD){
            Scalar scalar = new Scalar(random.nextDouble()*255, random.nextDouble()*255, random.nextDouble()*255, 255);
            //Log.d("Line", "Color: " + scalar);
            Line meanLine = calculateMeanLine(line_gp);
            horizontalCrossLine.add(meanLine);
            Imgproc.line(houghLines, meanLine.getP1(), meanLine.getP2(), scalar, 1);
            //i++;
        }



        //outputStreamWriter.write(i + " " + line.getTheta() + " " + line.getRtho() + "\n");

        //outputStreamWriter.close();



        /*for(int i = 0; i < approx.rows(); i++){
            Point p = new Point(approx.get(i, 0));
            Imgproc.circle(masked_mat, p,
                    10, new Scalar(255,0,0), 2);
        }*/



        /*  calculate intersection points between two refined groups */
        List<List<Point>> intersectPoints = new ArrayList<>();

        Collections.reverse(verticalCrossLine);

        for(Line line_a: verticalCrossLine){
            List<Point> rowPoints = new ArrayList<>();
            for(Line line_b: horizontalCrossLine){
                rowPoints.add(Intersection.calculate(line_a.getP1(), line_a.getP2(), line_b.getP1(), line_b.getP2()));
            }
            intersectPoints.add(rowPoints);
        }

        /*
        for(List<Point> row : intersectPoints){
            for(Point p : row)
                Imgproc.circle(houghLines, p,
                    30, new Scalar(0,255,0, 255), 10);
            //outputStreamWriter.write(i + " " + p.x + " " + p.y + "\n");
        }
        */



        /**  match chessboard part **/
        if(intersectPoints.size() > 8 && intersectPoints.get(0).size() > 8) {
            Point[][] chessboardReferenceModel = new Point[9][9];
            // initialize the chessboard
            for (int i = 0; i < 9; i++)
                for (int j = 0; j < 9; j++) {
                    chessboardReferenceModel[i][j] = new Point(blockSize * j, blockSize * i);
                }

            // chessboard kernel mat
            List<List<Point>> kernelMat = new ArrayList<>();
            double minDistance = Double.MAX_VALUE;

            // top left corner
            int idx_x = 0;
            int idx_y = 0;
            // outer Dot Mat loop, locate top left corner of the mat, size of chessboard is 9x9
            // boundary is 8 not 9
            for (int row = 0; row < intersectPoints.size() - 8; row++)
                for (int col = 0; col < intersectPoints.get(row).size() - 8; col++) {
                    // inner Chessboard kernel loop

                    // crop 9x9 points array
                    Point[][] chessboardModel = new Point[9][9];
                    for (int i = 0; i < 9; i++) {
                        for (int j = 0; j < 9; j++) {
                            chessboardModel[i][j] = intersectPoints.get(row + i).get(col + j);
                        }
                    }
                    // calculate distance from this model to chessboard reference model
                    double bD = distanceToChessboardModel(chessboardModel, chessboardReferenceModel);
                    //Log.d("Distance", "Distance to model reference: " + bD);

                    if (bD < minDistance) {
                        chessboardMatchTransform = recentAppliedTransform;
                        detectedChessboardModel = chessboardModel;
                        minDistance = bD;
                        idx_x = row;
                        idx_y = col;
                    }
                }

            for (int i = 0; i < 9; i++)
                for (int j = 0; j < 9; j++) {
                    Point p = intersectPoints.get(idx_x + i).get(idx_y + j);
                    Imgproc.circle(houghLines, p,
                            5, new Scalar(0, 255, 0, 255), 1);
                }
        }


        /*
        //Converting Mat back to Bitmap
        Utils.matToBitmap(houghLines, currentBitmap);
        loadImageToImageView();

         */
        return houghLines;

    }

    public static Mat tranformInterestArea(Mat src, Mat transf){
        // its modified version of canny, it only detect canny edge of darker pixel, so that it can remove unnecessary detail. You can convert back to original one by removing code of binary mat
        Mat output = null;

        if(transf != null) {
            output = new Mat(8 * blockSize + 1, 8 * blockSize + 1, CvType.CV_8UC4);
            Imgproc.warpPerspective(src, output, transf, new Size(8 * blockSize + 1, 8 * blockSize + 1));
            /*
            currentBitmap = Bitmap.createBitmap(8 * blockSize + 1, 8 * blockSize + 1, Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(outputMat, currentBitmap);
            loadImageToImageView();
             */
        }
        return output;
    }

    public static int[][] getOccpancy(int[][] currE, int[][] reE){
        int[][] Occ = new int[8][8];


        for(int i = 0; i < 8; i ++)
            for(int j = 0; j < 8; j ++){
                if (Math.abs(currE[i][j] - reE[i][j]) > occpancy_thres)
                    Occ[i][j] = 1;
                else
                    Occ[i][j] = 0;
            }

        return Occ;
    }

    public static char[][] defaultFill(char[][] arr, char val){
        if (arr.length == 0 || arr[0].length == 0)
            return null;

        for(int i = 0; i < arr.length; i ++)
            for(int j = 0; j < arr[i].length; j ++){
                arr[i][j] = val;
            }

        return arr;
    }

    public static char[][] getPieceColor(int[][] currE, int[][] reE, int[][] currI, int[][] refI){
        char[][] pieces = new char[8][8];

        int[][] Occ = getOccpancy(currE, reE);
        for(int i = 0; i < 8; i ++)
            for(int j = 0; j < 8; j ++){
                if (Occ[i][j] == 1){
                    pieces[i][j] = getColor(currI[i][j], refI[i][j]);
                }else
                    pieces[i][j] = CameraFragment.E;
            }

        return pieces;
    }

    public static char getColor(int currI, int refI){
        if(currI - refI > 0)
            return CameraFragment.W;
        else
            return CameraFragment.B;
    }

    public void showAOI_Origin(){
        int[][] occupancy = new int[8][8];
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++) {
                Log.d("Occ", "occupancy[i][j]");
            }
        Mat chessboardMat = new Mat(currentBitmap.getHeight(), currentBitmap.getWidth(), CvType.CV_8U);
        Utils.bitmapToMat(currentBitmap, chessboardMat);

        // find possible occupancy on chessboard
        for(int i = 0; i < 8; i ++)
            for(int j = 0; j < 8; j ++){
                Point start = new Point(blockSize*i + AOI_indent, blockSize * j + AOI_indent);
                Point end = new Point(blockSize*(i+1) - AOI_indent, blockSize * j + AOI_indent + AOI_height);
                //Imgproc.rectangle(chessboardMat, start, end, new Scalar(0, 255, 0, 255), 1);
                Log.d("CannyEdgeInAOI", "col: " + i + ", row: " + j + " Edge: " + countCannyEdgeInAOI(chessboardMat, start, end, binaryThres));
                if (countCannyEdgeInAOI(chessboardMat, start, end, binaryThres) > AOI_canny_thres) {
                    occupancy[j][i] = 1;
                    Imgproc.rectangle(chessboardMat, start, end, new Scalar(0, 255, 0, 255), 1);
                }
            }
        Mat AOI_mat = originalMat.clone();

        for (int i = 0; i < 8; i++) {
            Log.d("Chessboard Intersection", " row ----");
            for (int j = 0; j < 8; j++) {
                Log.d("Chessboard Intersection", "x: " + detectedChessboardModel[i][j].x + ", y: " + detectedChessboardModel[i][j].y);
            }
        }

        // localize piece on original image
        if (detectedChessboardModel != null) {
            for (int i = 0; i < 8; i++)
                for (int j = 0; j < 8; j++) {
                    if(occupancy[i][j] != 0){
                        // four corner points are; detectedChessboardModel[i][j], detectedChessboardModel[i + 1][j], detectedChessboardModel[i][j + 1], and detectedChessboardModel[i + 1][j + 1],
                        Point[] rect = getAOI(detectedChessboardModel[i][j], detectedChessboardModel[i + 1][j], detectedChessboardModel[i][j + 1], detectedChessboardModel[i + 1][j + 1]);
                        Imgproc.rectangle(AOI_mat, rect[0], rect[1], new Scalar(0, 255, 0, 255), 5);
                    }
                }
        }

        currentBitmap = Bitmap.createBitmap(originalMat.width(), originalMat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(AOI_mat, currentBitmap);
        loadImageToImageView();
    }

    public Point[] getAOI(Point p1, Point p2, Point p3, Point p4){
        Point[] rectangle = new Point[2];
        double left = Math.min(p1.x, Math.min(p2.x, Math.min(p3.x, p4.x)));
        double right = Math.max(p1.x, Math.max(p2.x, Math.max(p3.x, p4.x)));
        double bottom = Math.max(p1.y, Math.max(p2.y, Math.max(p3.y, p4.y)));
        rectangle[0] = new Point(left, bottom);
        rectangle[1] = new Point(right, bottom - AOI_height_origin);
        return rectangle;
    }

    public static int countCannyEdgeInAOI(Mat src, Point start, Point end, int threshold){
        int tot = 0;
        for(int i = (int)start.y; i <= end.y; i++)
            for(int j = (int)start.x; j <= end.x; j++){
                if(src.get(i,j)[0] > threshold)
                    tot ++;
            }

        return tot;
    }


    // calculate transform matrix and calculate distance after map other inner points to reference model
    public static double distanceToChessboardModel(Point[][] src, Point[][] model){

        MatOfPoint2f src4corner = new MatOfPoint2f(src[0][0], src[0][src[0].length-1], src[src.length-1][src[0].length-1], src[src.length-1][0]);
        MatOfPoint2f model4corner = new MatOfPoint2f(model[0][0], model[0][model[0].length-1], model[model.length-1][model[0].length-1], model[model.length-1][0]);

        Mat transform = Imgproc.getPerspectiveTransform(src4corner, model4corner);
        recentAppliedTransform = transform;

        // convert src format
        MatOfPoint2f srcMat = new MatOfPoint2f();
        for(Point[] row: src){
            MatOfPoint2f rowMat = new MatOfPoint2f(row);
            srcMat.push_back(rowMat);
        }

        // convert model format
        MatOfPoint2f modelMat = new MatOfPoint2f();
        for(Point[] row: model){
            MatOfPoint2f rowMat = new MatOfPoint2f(row);
            modelMat.push_back(rowMat);
        }

        // calculate projected points on reference model
        MatOfPoint2f dstMat = new MatOfPoint2f();
        Core.perspectiveTransform(srcMat, dstMat, transform);

        return chessboardDistance(dstMat, modelMat);

    }

    // compute distance of each pair points
    // boardA boardB should have the same size
    public static double chessboardDistance(MatOfPoint2f boardA, MatOfPoint2f boardB){
        Distance dist = new PointDistance();
        double tot_dist = 0;
        for(int i = 0; i < boardA.rows(); i ++){
            tot_dist += dist.calculate(new Point(boardA.get(i, 0)), new Point(boardB.get(i, 0)));
        }
        return tot_dist/boardA.rows();
    }

    public static Line calculateMeanLine(List<Line> lines){
        // find mean point of p1 and p2 set
        double p1_x_tot = 0;
        double p1_y_tot = 0;
        double p2_x_tot = 0;
        double p2_y_tot = 0;

        for(Line line: lines){
            //Log.d("Line", i + " theta = " + line.getTheta() + " | Rtho = " + line.getRtho());
            //outputStreamWriter.write(i + " " + line.getTheta() + " " + line.getRtho() + "\n");
            p1_x_tot += line.getP1().x;
            p1_y_tot += line.getP1().y;
            p2_x_tot += line.getP2().x;
            p2_y_tot += line.getP2().y;
        }

        return new Line(new double[]{p1_x_tot/lines.size(), p1_y_tot/lines.size(), p2_x_tot/lines.size(), p2_y_tot/lines.size()});
    }

    public Scalar toRGB(int hex) {
        float red   = (hex >> 16) & 0xFF;
        float green = (hex >> 8)  & 0xFF;
        float blue  = (hex)       & 0xFF;

        return new Scalar(red, green, blue);
        //System.out.println("red="+red+"--green="+green+"--blue="+blue);
    }

    private static class CentroidComparator implements Comparator<Centroid>
    {
        @Override
        public int compare (Centroid o1, Centroid o2){
            Point p1 = o1.point;
            Point p2 = o2.point;
            if (p1.x != p2.x) {
                // compare x
                if (p1.x > p2.x)
                    return 1;
                else
                    return -1;
            } else {
                // compare y
                if (p1.y == p2.y)
                    return 0;
                else if (p1.y > p2.y)
                    return 1;
                else
                    return -1;
            }
        }
    }
    // get average intensity of the grid
    public void getIntensity(){
        // 8 x 8 grids
        Mat chessboardMat = new Mat(currentBitmap.getHeight(), currentBitmap.getWidth(), CvType.CV_8U);
        Utils.bitmapToMat(currentBitmap, chessboardMat);
        for(int i = 0; i < 8; i ++)
            for(int j = 0; j < 8; j ++) {
                Point start = new Point(blockSize * i + AOI_indent, blockSize * j + AOI_indent);
                Point end = new Point(blockSize * (i + 1) - AOI_indent, blockSize * j + AOI_indent + AOI_height);
                Mat block = chessboardMat.submat((int) start.x, (int) end.x, (int) start.y, (int) end.y);
                double mu = Core.mean(block).val[0];
                Imgproc.putText(chessboardMat, (int)mu + "", new Point(start.x,start.y), Core.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(150,255, 23));
            }



                //Imgproc.rectangle(chessboardMat, start, end, new Scalar(0, 255, 0, 255), 1);
                // Log.d("CannyEdgeInAOI", "col: " + i + ", row: " + j + " Edge: " + countCannyEdgeInAOI(chessboardMat, start, end, binaryThres));
        //Log.d("mMean", "mean of the block is : " + mu);
        //Imgproc.putText(block, mu + "", new Point(40,40), Core.FONT_HERSHEY_SIMPLEX, 10.0, new Scalar(150,255, 23));
        Utils.matToBitmap(chessboardMat, currentBitmap);
        loadImageToImageView();

    }

    // a grid points: row x column
    public static List<List<Point>> getIntersetPoints(Mat src){
        List<List<Point>> intersectPoints = new ArrayList<>();

        /**mask part**/
        Mat grayMat = new Mat();
        Mat binaryMat = new Mat();
        Mat cannyEdges = new Mat();
        Mat hierarchy = new Mat();
        List<MatOfPoint> contourList = new
                ArrayList<MatOfPoint>();
        //A list to store all the contours
        //Converting the image to grayscale

        /* Canny Edge */
        //Converting the image to grayscale
        Imgproc.cvtColor(src, grayMat, Imgproc.COLOR_BGR2GRAY);

        Imgproc.Canny(grayMat, cannyEdges,cannyEdgeThres1, cannyEdgeThres2);

        // dilate the binary mat
        Mat kernelDilate = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(15,15));
        Imgproc.dilate(cannyEdges, cannyEdges, kernelDilate);

        /* Contour */
        //finding contours
        Imgproc.findContours(cannyEdges,contourList
                ,hierarchy,Imgproc.RETR_CCOMP,
                Imgproc.CHAIN_APPROX_SIMPLE);

        if(contourList.size() == 0)
            return intersectPoints;


        // find largest contour
        int index = 0;
        double maxim = Imgproc.contourArea(contourList.get(0));
        for (int contourIdx = 1; contourIdx < contourList.size();
             contourIdx++) {
            double temp;
            temp=Imgproc.contourArea(contourList.get(contourIdx));
            if(maxim<temp)
            {
                maxim=temp;
                index=contourIdx;
            }
        }

        // get mask with in shape of largest contour
        Mat contour_mask = Mat.zeros(cannyEdges.rows()
                ,cannyEdges.cols(), CvType.CV_8UC1);
        Imgproc.drawContours(contour_mask, contourList, index, new Scalar(255),
                -1);

        Log.d("Contour", "size of contour is: " + contourList.get(index).size());

        /*
        // find outer corners of chessboard
        double epsilon = 0.1*Imgproc.arcLength(new MatOfPoint2f(contourList.get(index).toArray()),true);
        MatOfPoint2f approx = new MatOfPoint2f();
        Imgproc.approxPolyDP(new MatOfPoint2f(contourList.get(index).toArray()),approx,epsilon,true);
        Log.d("Contour corner", "Corner: " + approx.rows());

         */

        /*
        //Drawing all contours on a new image
        Mat contours = new Mat();
        contours.create(cannyEdges.rows()
                ,cannyEdges.cols(),CvType.CV_8UC3);
        Random r = new Random();
        for(int i = 0; i < contourList.size(); i++)
        {
            Imgproc.drawContours(contours
                    ,contourList,i,new Scalar(r.nextInt(255)
                            ,r.nextInt(255),r.nextInt(255)), -1);
        }
        */

        // apply mask on grayMat
        Mat masked_mat = new Mat();
        //Log.d("Size", "Size 1: " + contour_mask.size() + " Size 2: " + originalMat.size());
        Core.bitwise_and(contour_mask, grayMat, masked_mat);


        /**Hough Line part**/

        Mat lines = new Mat();
        /* find hough line */

        Imgproc.medianBlur(masked_mat, masked_mat, 23);

        //Imgproc.threshold(grayMat, binaryMat, binaryThres, 255, Imgproc.THRESH_BINARY);
        Imgproc.adaptiveThreshold(masked_mat, binaryMat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY, 9, 0);

        Imgproc.medianBlur(binaryMat, binaryMat, 5);

        Imgproc.Canny(binaryMat, cannyEdges, cannyEdgeThres1, cannyEdgeThres2);

        Imgproc.HoughLinesP(cannyEdges, lines, 1, Math.PI / 180, houghLinesThres, houghLinesMinLineLength, houghLinesMaxLineGap);

        //Imgproc.HoughLines(cannyEdges, lines, 1, Math.PI / 180, houghLinesThres, 0, 0, houghLinesMinLineLength, houghLinesMaxLineGap);

        Mat houghLines = src.clone();



        List<Line> crossLine = new ArrayList<Line>();

        Log.d("LENGTH", " " + lines.rows());

        if (lines.rows() < 2)
            return intersectPoints;

        // store line in list
        for (int i = 0; i < lines.rows(); i++) {
            double[] points = lines.get(i,0);
            Line l = new Line(points, src.height(), src.width());
            crossLine.add(l);
        }

        /* cluster lines in two groups -- horizontal group and vertical group */


        Map<Centroid, List<Line>> clusters = KMeans.fit(crossLine, 2, new HoughDistance(), 500);

        //Map<Centroid, List<Line>> clusters_a = KMeans.fit2((List<Line>)clusters.values().toArray()[0], 2, new HoughDistance(), 500);

        /*  cluster lines in each group by crossPointCluster -- expected 9,9  */
        Centroid verticalCent;
        Centroid horizontalCent;

        Centroid firstCent = (Centroid)clusters.keySet().toArray()[0];
        Centroid secondCent = (Centroid)clusters.keySet().toArray()[1];
        //Log.d("Orientation", "first cent: " + Math.abs(firstCent.line.getTheta()));
        //Log.d("Orientation", "second cent: " + Math.abs(secondCent.line.getTheta()));
        if(Math.abs(firstCent.line.getTheta()) > Math.abs(secondCent.line.getTheta())){
            horizontalCent = secondCent;
            verticalCent = firstCent;
        }else{
            horizontalCent = firstCent;
            verticalCent = secondCent;
        }

        List<LineWithPoint> lineWithPoints_v = new ArrayList<>();
        List<LineWithPoint> lineWithPoints_h = new ArrayList<>();

        // calculate intersection points between all vercital line and a horizoncel line

        // get one line of another group
        Line hzLine = clusters.get(horizontalCent).get(0);
        Line vtLine = clusters.get(verticalCent).get(0);

        for(Line line: clusters.get(verticalCent)){
            Point ins = Intersection.calculate(line.getP1(), line.getP2(), hzLine.getP1(), hzLine.getP2());
            lineWithPoints_v.add(new LineWithPoint(line, ins));
        }

        for(Line line: clusters.get(horizontalCent)){
            Point ins = Intersection.calculate(line.getP1(), line.getP2(), vtLine.getP1(), vtLine.getP2());
            lineWithPoints_h.add(new LineWithPoint(line, ins));
        }

        // cluster cross points by coordinates
        Map<Centroid, List<LineWithPoint>> verticalIntersectionClusters = KMeans.fit2_point(lineWithPoints_v, 60, new PointDistance(), 500);
        Map<Centroid, List<LineWithPoint>> horizontalIntersectionClusters = KMeans.fit2_point(lineWithPoints_h, 60, new PointDistance(), 500);

        // sort verticalIntersectionClusters's keys in order according to their point
        Map<Centroid, List<LineWithPoint>> sortedVerticalClusters = new TreeMap<>(new CentroidComparator());
        Map<Centroid, List<LineWithPoint>> sortedHorizontalClusters = new TreeMap<>(new CentroidComparator());
        sortedVerticalClusters.putAll(verticalIntersectionClusters);
        sortedHorizontalClusters.putAll(horizontalIntersectionClusters);

        // convert to standard cluster type for looping
        ArrayList<List<Line>> sortedVerticalClusters_SD = new ArrayList<>();
        ArrayList<List<Line>> sortedHorizontalClusters_SD = new ArrayList<>();

        for(Map.Entry<Centroid, List<LineWithPoint>> entry: sortedVerticalClusters.entrySet()){
            // Convert List<LineWithPoint> to List<Line>
            Log.d("Point", "GP ---------");
            List<Line> gp = new ArrayList<>();
            for(LineWithPoint lp: entry.getValue()){
                gp.add(lp.line);
                Log.d("Point", " x = " + lp.point.x + " y = " + lp.point.y);
            }
            sortedVerticalClusters_SD.add(gp);
        }

        for(Map.Entry<Centroid, List<LineWithPoint>> entry: sortedHorizontalClusters.entrySet()){
            // Convert List<LineWithPoint> to List<Line>
            Log.d("Point", "GP ---------");
            List<Line> gp = new ArrayList<>();
            for(LineWithPoint lp: entry.getValue()){
                gp.add(lp.line);
                Log.d("Point", " x = " + lp.point.x + " y = " + lp.point.y);
            }
            sortedHorizontalClusters_SD.add(gp);
        }



        Random random = new Random();

        // Write line data to file for python analyzing
        //OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.openFileOutput("line_data.txt", Context.MODE_PRIVATE));
        // i indicates different clusters

        int colors[] = new int[]{
                Color.BLUE,
                Color.YELLOW,
                Color.CYAN,
                Color.MAGENTA,
                Color.BLACK,
                Color.DKGRAY,
                Color.GRAY,
                Color.LTGRAY,
                Color.WHITE,
                Color.RED,
                Color.GREEN,
                0x663366,
                0xFF6666,
                0xCCCC00,
                0x734C00,
                0x3CCC00
        };


        /* Calculate mean line of each group and removing duplicated line */
        List<Line> verticalCrossLine = new ArrayList<>();
        List<Line> horizontalCrossLine = new ArrayList<>();
        //int i = 0;
        for(List<Line> line_gp: sortedVerticalClusters_SD){
            Scalar scalar = new Scalar(random.nextDouble()*255, random.nextDouble()*255, random.nextDouble()*255, 255);
            //Log.d("Line", "Color: " + scalar);
            Line meanLine = calculateMeanLine(line_gp);
            verticalCrossLine.add(meanLine);
            Imgproc.line(houghLines, meanLine.getP1(), meanLine.getP2(), scalar, 1);
            //i++;
        }

        //i = 0;
        for(List<Line> line_gp: sortedHorizontalClusters_SD){
            Scalar scalar = new Scalar(random.nextDouble()*255, random.nextDouble()*255, random.nextDouble()*255, 255);
            //Log.d("Line", "Color: " + scalar);
            Line meanLine = calculateMeanLine(line_gp);
            horizontalCrossLine.add(meanLine);
            Imgproc.line(houghLines, meanLine.getP1(), meanLine.getP2(), scalar, 1);
            //i++;
        }



        //outputStreamWriter.write(i + " " + line.getTheta() + " " + line.getRtho() + "\n");

        //outputStreamWriter.close();



        /*for(int i = 0; i < approx.rows(); i++){
            Point p = new Point(approx.get(i, 0));
            Imgproc.circle(masked_mat, p,
                    10, new Scalar(255,0,0), 2);
        }*/



        /*  calculate intersection points between two refined groups */


        Collections.reverse(verticalCrossLine);

        for(Line line_a: verticalCrossLine){
            List<Point> rowPoints = new ArrayList<>();
            for(Line line_b: horizontalCrossLine){
                rowPoints.add(Intersection.calculate(line_a.getP1(), line_a.getP2(), line_b.getP1(), line_b.getP2()));
            }
            intersectPoints.add(rowPoints);
        }

        return intersectPoints;
    }

    public static Match getChessBoardPointsAndMatrix(List<List<Point>> intersectPoints){
        /**  match chessboard part **/
        if(intersectPoints.size() > 8 && intersectPoints.get(0).size() > 8) {
            Point[][] chessboardReferenceModel = new Point[9][9];
            // initialize the chessboard
            for (int i = 0; i < 9; i++)
                for (int j = 0; j < 9; j++) {
                    chessboardReferenceModel[i][j] = new Point(blockSize * j, blockSize * i);
                }

            // chessboard kernel mat
            List<List<Point>> kernelMat = new ArrayList<>();
            double minDistance = Double.MAX_VALUE;

            // top left corner
            int idx_x = 0;
            int idx_y = 0;
            // outer Dot Mat loop, locate top left corner of the mat, size of chessboard is 9x9
            // boundary is 8 not 9
            for (int row = 0; row < intersectPoints.size() - 8; row++)
                for (int col = 0; col < intersectPoints.get(row).size() - 8; col++) {
                    // inner Chessboard kernel loop

                    // crop 9x9 points array
                    Point[][] chessboardModel = new Point[9][9];
                    for (int i = 0; i < 9; i++) {
                        for (int j = 0; j < 9; j++) {
                            chessboardModel[i][j] = intersectPoints.get(row + i).get(col + j);
                        }
                    }
                    // calculate distance from this model to chessboard reference model
                    double bD = distanceToChessboardModel(chessboardModel, chessboardReferenceModel);
                    //Log.d("Distance", "Distance to model reference: " + bD);

                    if (bD < minDistance) {
                        chessboardMatchTransform = recentAppliedTransform;
                        detectedChessboardModel = chessboardModel;
                        minDistance = bD;
                        idx_x = row;
                        idx_y = col;
                    }
                }
            return new Match(detectedChessboardModel, chessboardMatchTransform);
        }else
            return null;
    }

    // get average intensity of the grid
    public static int[][] getIntensity(Mat grid){
        // 8 x 8 grids

        int[][] intensities = new int[8][8];

        //Mat chessboardMat = new Mat(currentBitmap.getHeight(), currentBitmap.getWidth(), CvType.CV_8U);
        //Utils.bitmapToMat(currentBitmap, chessboardMat);
        Mat grayMat = new Mat();
        Imgproc.cvtColor(grid, grayMat, Imgproc.COLOR_BGR2GRAY);

        for(int i = 0; i < 8; i ++)
            for(int j = 0; j < 8; j ++) {
                Point start = new Point(blockSize * i + AOI_indent, blockSize * j + AOI_indent);
                Point end = new Point(blockSize * (i + 1) - AOI_indent, blockSize * j + AOI_indent + AOI_height);
                Mat block = grayMat.submat((int) start.x, (int) end.x, (int) start.y, (int) end.y);
                double mu = Core.mean(block).val[0];
                //Imgproc.putText(chessboardMat, (int)mu + "", new Point(start.x,start.y), Core.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(150,255, 23));
                intensities[i][j] = (int)mu;
            }



        //Imgproc.rectangle(chessboardMat, start, end, new Scalar(0, 255, 0, 255), 1);
        // Log.d("CannyEdgeInAOI", "col: " + i + ", row: " + j + " Edge: " + countCannyEdgeInAOI(chessboardMat, start, end, binaryThres));
        //Log.d("mMean", "mean of the block is : " + mu);
        //Imgproc.putText(block, mu + "", new Point(40,40), Core.FONT_HERSHEY_SIMPLEX, 10.0, new Scalar(150,255, 23));
        return intensities;

    }

    // get number of edges on each block
    public static int[][] getEdges(Mat src){
        int[][] edges = new int[8][8];
        Mat cannyEdges = Canny(src);
        for(int i = 0; i < 8; i ++)
            for(int j = 0; j < 8; j ++) {
                Point start = new Point(blockSize * i + AOI_indent, blockSize * j + AOI_indent);
                Point end = new Point(blockSize * (i + 1) - AOI_indent, blockSize * j + AOI_indent + AOI_height);
                edges[i][j] = countCannyEdgeInAOI(cannyEdges, start, end, 150);
            }
        return edges;
    }

    public static Mat drawPoint(Mat src, Point[][] points){
        if(points.length == 0 || points[0].length == 0)
            return src;
        Mat dest = src.clone();
        for (int i = 0; i < points.length; i++)
            for (int j = 0; j < points[0].length; j++) {
                Imgproc.circle(dest, points[i][j],
                        15, new Scalar(0, 255, 0, 255), 2);
            }
        return dest;
    }


    public static Mat calibrate(Mat src){
        // get transformMat and chessboard points
        List<List<Point>> boardPoints = getIntersetPoints(src);
        Match pointsAndTranf = getChessBoardPointsAndMatrix(boardPoints);
        // get intensifies and edges
        if (pointsAndTranf != null){
            Mat AOI = tranformInterestArea(src, pointsAndTranf.tranf);
            int[][] intensities = getIntensity(AOI);
            int[][] edges = getEdges(AOI);
            src = drawPoint(src, pointsAndTranf.points);
            for(int i = 0; i < 8; i ++)
                for(int j = 0; j < 8; j ++){
                    //Imgproc.putText(src, " E: "+ edges[i][j] + "\n I: " + (int)intensities[i][j], pointsAndTranf.points[i][j], Core.FONT_HERSHEY_COMPLEX, 3, new Scalar(255,0, 0,255), 4);
                    Log.d("Info", " E: "+ edges[i][j] + ", I: " + intensities[i][j]);
                }
            // put data in original mat
            //Imgproc.putText(chessboardMat, (int)mu + "", new Point(start.x,start.y), Core.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(150,255, 23));

            //currentBitmap = Bitmap.createBitmap(originalMat.width(), originalMat.height(), Bitmap.Config.ARGB_8888);
        }

        return src;
    }
}
