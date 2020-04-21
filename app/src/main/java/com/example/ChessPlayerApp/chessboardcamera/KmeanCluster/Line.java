package com.example.ChessPlayerApp.chessboardcamera.KmeanCluster;
import org.opencv.core.Point;

public class Line {
    // points has size of 4, which is coordinates of two points
    //            x1 = points[0];
    //            y1 = points[1];
    //            x2 = points[2];
    //            y2 = points[3];
    private double points[];
    // scaled rtho
    private double rtho;
    private double theta;
    //private String a;
    //private double rtho_norm;


    public Line(double points[]){
        this.points = points;
    }

    public Line(double points[], int width, int height){
        this.points = points;
        theta = computeTheta();
        rtho = computeRtho(width, height);
    }

    public Line(double rtho, double theta){
        this.rtho = rtho;
        this.theta = theta;
    }

    public Point getP1(){
        return new Point(points[0], points[1]);
    }

    public Point getP2(){
        return new Point(points[2], points[3]);
    }

    // angle between line and x axis
    public double getAngle(){
        if (points[1] == points[3])
            return 90;
        else
            return Math.atan((points[1]-points[3])/(points[0]-points[2]))*180/Math.PI;
    }

    public double getRtho() {
        return rtho;
    }

    public double getTheta() {
        return theta;
    }

    // angle between the distance line and x axis
    private double computeTheta(){
        double theta = -Math.atan2((points[0]-points[2]), (points[1]-points[3]))*180/Math.PI;
        if (theta > 90){
            theta = theta - 180;
        }else if (theta < -90){
            theta = theta + 180;
        }
        return theta;
    }

    private double computeRtho(int width, int height){
        double dist = Math.abs(points[2]*points[1] - points[3]*points[0])/Math.sqrt(Math.pow(points[3] - points[1], 2) + Math.pow(points[2] - points[0], 2));
        double interceptY = -points[0]*(points[3] - points[1])/(points[2] - points[0]) + points[1];
        double theta = computeTheta();
        if((theta > 0 && interceptY < 0) || (theta < 0 && interceptY > 0))
            dist = -dist;

        double d = Math.sqrt(Math.pow(height, 2) + Math.pow(width, 2));


        return dist*90/d;
    }

}
