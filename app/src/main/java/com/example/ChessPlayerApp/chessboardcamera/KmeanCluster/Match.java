package com.example.ChessPlayerApp.chessboardcamera.KmeanCluster;

import org.opencv.core.Mat;
import org.opencv.core.Point;

public class Match {

    public Point[][] points;
    public Mat tranf;

    public Match(Point[][] points, Mat tranf){
        this.points = points;
        this.tranf = tranf;
    }
}
