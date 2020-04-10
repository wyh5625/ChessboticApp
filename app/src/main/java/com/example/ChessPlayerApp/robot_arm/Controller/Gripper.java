package com.example.ChessPlayerApp.robot_arm.Controller;

public interface Gripper {
    public abstract void openGripper(double amount);
    public abstract void closeGripper(double amount);
    public abstract void movePiece(String from, String to);
}
