package com.example.ChessPlayerApp.robot_arm;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.TabLayout;


import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.example.ChessPlayerApp.BLE.BLEScanHelper;
import com.example.ChessPlayerApp.BLE.BluetoothLeService;
import com.example.ChessPlayerApp.BLE.ScanBaseActivity;
import com.example.ChessPlayerApp.R;
import com.example.ChessPlayerApp.robot_arm.Chess.ChessFragment;
import com.example.ChessPlayerApp.robot_arm.Controller.ControllerFragment;
import com.example.ChessPlayerApp.robot_arm.Recognition.CameraFragment;

import java.util.ArrayList;


public class RobotArmControllerActivity extends ScanBaseActivity{



    private final static String TAG = RobotArmControllerActivity.class.getSimpleName();

    public static boolean mConnected = false;

    // robot arm device
    public static String mDeviceAddress;

    public static BluetoothLeService mBluetoothLeService;
    public static boolean bleServiceConnected = false;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            bleServiceConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            bleServiceConnected = false;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                Log.e(TAG, "Address: " + mDeviceAddress + " Connected!");
                ControllerFragment.print("Connected!");
                ControllerFragment.mConnectButton.setText("Disconnect");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                ControllerFragment.print("Disconnected!");
                ControllerFragment.mConnectButton.setText("Connect");

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                ControllerFragment.print(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    ArrayList<Fragment> mFragments;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        /*
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

         */

        // save one copy of each fragment
        mFragments = new ArrayList<>();
        Fragment conFragment = ControllerFragment.getInstance();
        Fragment chessFragment = ChessFragment.getInstance();
        Fragment cameraFragment = CameraFragment.getInstance();
        mFragments.add(conFragment);
        mFragments.add(chessFragment);
        mFragments.add(cameraFragment);

        // tab layout
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_label1));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.tab_label2));
        tabLayout.addTab(tabLayout.newTab().setText("Camera"));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // ViewPager
        final ViewPager viewPager = findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // create BLE service for communication
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void init() {
        mBLEScanHelper = BLEScanHelper.getInstance(mBluetooth, ControllerFragment.mSpinnerAdapter);
    }

    @Override
    public void clearDevice() {
        if (ControllerFragment.mSpinnerAdapter != null) {
            ControllerFragment.mSpinnerAdapter.clear();
            ControllerFragment.mSpinnerAdapter.notifyDataSetChanged();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService.disconnect();
        mBluetoothLeService = null;
    }



    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }



}
