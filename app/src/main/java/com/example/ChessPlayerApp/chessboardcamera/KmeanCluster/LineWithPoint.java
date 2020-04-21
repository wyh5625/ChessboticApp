package com.example.ChessPlayerApp.chessboardcamera.KmeanCluster;

import org.opencv.core.Point;

public class LineWithPoint {
    public Line line;
    public Point point;

    public LineWithPoint(Line line, Point point) {
        this.line = line;
        this.point = point;
    }
}
