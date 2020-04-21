package com.example.ChessPlayerApp.robot_arm;

import com.example.ChessPlayerApp.robot_arm.Chess.ChessFragment;
import com.example.ChessPlayerApp.robot_arm.Controller.ControllerFragment;
import com.example.ChessPlayerApp.robot_arm.Recognition.CameraFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

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
