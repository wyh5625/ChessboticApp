package com.example.ChessPlayerApp.BLE.ScanRecycler;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.ChessPlayerApp.BLE.BLEScanHelper;
import com.example.ChessPlayerApp.BLE.ScanBaseActivity;
import com.example.ChessPlayerApp.R;

public class ScanRecyclerActivity extends ScanBaseActivity {

    // Recycler view for showing devices
    private RecyclerView mRecyclerView;
    private DeviceListAdapter mDeviceListAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_recycler);

        // Recycler view init
        mRecyclerView = findViewById(R.id.recyclerview);
        mDeviceListAdapter = new DeviceListAdapter(this, deviceList);
        mRecyclerView.setAdapter(mDeviceListAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    public void init() {
        // Use BLEScanHelper for scanning devices
        mBLEScanHelper = BLEScanHelper.getInstance(mBluetooth, mDeviceListAdapter);
    }

    @Override
    public void clearDevice() {
        if (mDeviceListAdapter != null)
            mDeviceListAdapter.clearDevice();
    }


}
