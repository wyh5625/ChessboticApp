package com.example.ChessPlayerApp.console;

public class JNI {

    public JNI(){
        initController();
    }

    public void init(){

    }

    // Interface print
    public native String printLogo();
    public native String printMenu();

    // Controller
    public native void initController();
    public native void newGame();
    public native void saveGame(String fileName);
    public native void loadGame(String fileName);

    public native void undoMove();
    public native void movePiece(String from, String to);

    public native String printSituation();
    public native String printBoard();

    static{
        System.loadLibrary("chess-jni");
    }
}
