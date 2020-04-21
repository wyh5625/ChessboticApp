package com.example.ChessPlayerApp.chessboardcamera.KmeanCluster;

import org.opencv.core.Point;

public class PointDistance implements Distance{
    @Override
    public double calculate(Object a, Object b) {
        Point pa = (Point)a;
        Point pb = (Point)b;

        return Math.sqrt(Math.pow(pa.x-pb.x, 2) + Math.pow(pa.y - pb.y, 2));
    }
}
