package com.example.ChessPlayerApp.chessboardcamera.KmeanCluster;

public class HoughDistance implements Distance{
    @Override
    public double calculate(Object a, Object b) {
        Line line_a = (Line)a;
        Line line_b = (Line)b;
        double lineAngle = Math.abs(line_a.getTheta() - line_b.getTheta());
        if(lineAngle > 90){
            lineAngle = 180 - lineAngle;
        }

        double rtho_diff = Math.abs(line_a.getRtho() - line_b.getRtho());

        return Math.sqrt(Math.pow(lineAngle, 2) + Math.pow(rtho_diff, 2));
        //return lineAngle;
    }
}
