package com.example.ChessPlayerApp.robot_arm.Recognition;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.example.ChessPlayerApp.R;


import org.opencv.core.Mat;


public class CameraFragment extends Fragment {

    private static CameraFragment instance;

    // camera
    // Loads camera view of OpenCV for us to use. This lets us see using OpenCV
    //private ZoomCameraView mZoomCameraView;

    // Used in Camera selection from menu (when implemented)
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;

    // These variables are used (at the moment) to fix camera orientation from 270degree to 0degree
    Mat mRgba;
    Mat mRgbaF;
    Mat mRgbaT;

    public static CameraFragment getInstance(){
        if (instance == null)
            instance = new CameraFragment();

        return instance;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        View root = inflater.inflate(R.layout.fragment_camera, container, false);


        return root;
    }
}
