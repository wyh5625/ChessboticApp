package com.example.ChessPlayerApp.chessboardcamera;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.AttributeSet;


import org.opencv.android.JavaCameraView;

import java.io.IOException;
import java.lang.reflect.Method;


public class ZoomCameraView extends JavaCameraView {
    int progressvalue = 0;

    public ZoomCameraView(Context context, int cameraId) {
        super(context, cameraId);
    }

    public ZoomCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public void setPreviewFPS(double min, double max) {
        /*
        List<int[]> ftp = mCamera.getParameters().getSupportedPreviewFpsRange();

        for (int[] f : ftp) {
            String re = "";
            for (int i : f) {
                re += i + " ";
            }
            Log.d("FPS", "FPS range is: " + re);
        }

         */

        Parameters params = mCamera.getParameters();
        params.setPreviewFpsRange((int) (min * 1000), (int) (max * 1000));
        mCamera.setParameters(params);


    }
/*
    public void setDisplayOrientation(int angle){
        Parameters params = mCamera.getParameters();
        params.setRotation(90);
        mCamera.setParameters(params);
    }

 */

    //protected SeekBar seekBar;

    /*
    public void setZoomControl(SeekBar _seekBar) {
        seekBar = _seekBar;
    }

    protected void enableZoomControls(Parameters params) {

        final int maxZoom = params.getMaxZoom();
        seekBar.setMax(maxZoom);
        seekBar.setOnSeekBarChangeListener(this);

    }

     */

    /*
    protected boolean initializeCamera(int width, int height) {

        boolean ret = super.initializeCamera(width, height);

        return ret;
    }

     */




    /*
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        // TODO Auto-generated method stub
        progressvalue = progress;
        Parameters params = mCamera.getParameters();
        params.setZoom(progress);
        //Log.d("Zoom", "Zoom = " + progress);
        mCamera.setParameters(params);


    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

     */
}