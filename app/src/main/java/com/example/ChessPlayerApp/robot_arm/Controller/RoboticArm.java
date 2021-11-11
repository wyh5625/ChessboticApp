package com.example.ChessPlayerApp.robot_arm.Controller;

import android.util.Log;

import com.example.ChessPlayerApp.BLE.BluetoothLeService;

public class RoboticArm implements Gripper {
    private BluetoothLeService myBluetooth;
    private double endPointX,endPointY,endPointZ;

    // coordinate of a1
    private double a1_x;
    private double a1_y;

    private double block_size;
    private double distanecToBoard;

    // gripperOffset
    private double gripperOffset = 40;

    // Bin position (beyond chessboard)
    private String bin = "k8";

    // static value
    public static double gripperFloatHeight = 60;
    public static double gripperGrabHeight = -5;
    public static double gripperGrabHeight2 = 0;
    public static double gripperFloatOffset = 15;

    // gripper value
    public static double openAmount = 0;
    public static double closeAmount = 50;

    public RoboticArm(BluetoothLeService bls, double block_size, double distanceToBoard){
        this.myBluetooth = bls;
        endPointX = 0;
        endPointY = 180 + gripperOffset;
        endPointZ = 180;

        // calculate coordinate of a1
        this.block_size = block_size;
        this.distanecToBoard = distanceToBoard;

        a1_x = block_size*7/2;
        a1_y = distanceToBoard + block_size*15/2;
    }


    public void activate(Boolean activated){
        if (activated)
            myBluetooth.write("M17\r");
        else
            myBluetooth.write("M18\r");

    }

    public void activateFan(Boolean activated){
        if (activated)
            myBluetooth.write("M106\r");
        else
            myBluetooth.write("M107\r");
    }

    public void connect(String addr){
        myBluetooth.connect(addr);
    }

    public void disconnect(){
        myBluetooth.disconnect();
    }

    // location of gripper
    public void moveTo(double x, double y, double z){
        endPointX = x;
        endPointY = y;
        endPointZ = z;
        double theta = Math.atan2(y, x);
        double x_pivot = x - gripperOffset*Math.cos(theta);
        double y_pivot = y - gripperOffset*Math.sin(theta);

        String action = "G1 " + "X" + x_pivot + " Y" + y_pivot + " Z" + z + "\r";

        Log.d("MoveTo", action);
        myBluetooth.write(action);
    }

    public void moveTo(String pos){
        double to_x = a1_x + ('a' - pos.charAt(0))*block_size;
        double to_y = a1_y + ('1' - pos.charAt(1))*block_size;
        moveTo(to_x, to_y, gripperGrabHeight);
    }


    public void moveX(double x){
        endPointX += x;
        moveTo(endPointX, endPointY, endPointZ);
    }

    public void moveY(double y){
        endPointY += y;
        moveTo(endPointX, endPointY, endPointZ);
    }

    public void moveZ(double z){
        endPointZ += z;
        moveTo(endPointX, endPointY, endPointZ);
    }


    @Override
    public void openGripper(double amount) {
        String action = "M5" + " " + "T" + amount + "\r";
        myBluetooth.write(action);
    }

    @Override
    public void closeGripper(double amount) {
        String action = "M3" + " " + "T" + amount + "\r";
        myBluetooth.write(action);
    }

    public void movePiece(int from, int to){
        String s_from = convertPosition(from);
        String s_to = convertPosition(to);
        movePiece(s_from, s_to);
    }

    @Override
    public void movePiece(String from, String to) {
        // move to Home
        // move to from's top
        // open gripper
        // move down
        // close gripper
        // move up to to of piece

        // move to to's top
        // move down
        // open gripper
        // move up to to's top
        // move to Home

        double from_x = a1_x + ('a' - from.charAt(0))*block_size;
        double from_y = a1_y + ('1' - from.charAt(1))*block_size;
        double to_x = a1_x + ('a' - to.charAt(0))*block_size;
        double to_y = a1_y + ('1' - to.charAt(1))*block_size;

        //moveTo(0,180+gripperOffset,180);
        moveTo(from_x, from_y, gripperFloatHeight);
        openGripper(openAmount);

        //moveTo(from_x, from_y + gripperFloatOffset, gripperGrabHeight);
        moveTo(from_x, from_y, gripperGrabHeight);
        closeGripper(closeAmount);

        moveTo(from_x, from_y, gripperFloatHeight);

        moveTo(to_x, to_y, gripperFloatHeight);
        moveTo(to_x, to_y, gripperGrabHeight);
        openGripper(openAmount);

        moveTo(to_x, to_y, gripperFloatHeight);
        moveTo(0,100+gripperOffset,180);

    }
    // convert position index to letter format eg. 21 -> c6
    // map: (actually, seeing up-side-down)
    // {'R','N','B','Q','K','B','N','R',
    //  'P','P','P','P','P','P','P','P',
    //  '*','*','*','*','*','*','*','*',
    //  '*','*','*','*','*','*','*','*',
    //  '*','*','*','*','*','*','*','*',
    //  '*','*','*','*','*','*','*','*',
    //  'p','p','p','p','p','p','p','p',
    //  'r','n','b','q','k','b','n','r'}
    public String convertPosition(int pos){
        char alp = (char)('a' + (pos % 8));
        char num = (char)('1' + (pos / 8));

        return "" + alp + num;
    }

    public void remove(int pos){
        movePiece(convertPosition(pos), bin);
    }


}
