package com.example.ChessPlayerApp.BLE;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.ChessPlayerApp.R;

import java.util.LinkedList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public abstract class ScanBaseActivity extends AppCompatActivity {

    private static final int ENABLE_BLUETOOTH = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 2;
    private boolean scanPermission = true;

    protected BLEScanHelper mBLEScanHelper;
    // Scaning handler

    protected BluetoothAdapter mBluetooth;
    protected LinkedList<BluetoothDevice> deviceList = new LinkedList<>();





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get BLE mSpinnerAdapter
        mBluetooth = BluetoothAdapter.getDefaultAdapter();


        // Enable bluetooth
        if (!mBluetooth.isEnabled()) {
            startActivityForResult(
                    new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), ENABLE_BLUETOOTH);
        }

        // Request permission for ble scan
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                scanPermission = false;
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);

            }else
                scanPermission = true;
        }

    }
    // override in subclass: must at least init layout
    public abstract void init();

    // override in subclass
    public abstract void clearDevice();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_scan:
                init();
                if(scanPermission && mBLEScanHelper != null) {
                    clearDevice();
                    mBLEScanHelper.leScan(true);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    scanPermission = true;
                }else{
                    scanPermission = false;
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == ENABLE_BLUETOOTH)
            if (resultCode == RESULT_OK){
                //
            }
    }









}
