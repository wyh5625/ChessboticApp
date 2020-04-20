package com.example.ChessPlayerApp.robot_arm;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.ChessPlayerApp.robot_arm.Chess.ChessFragment;
import com.example.ChessPlayerApp.robot_arm.Controller.ControllerFragment;
import com.example.ChessPlayerApp.robot_arm.Recognition.CameraFragment;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0: return new ControllerFragment();
            case 1: return new ChessFragment();
            case 2: return new CameraFragment();
            default: return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
