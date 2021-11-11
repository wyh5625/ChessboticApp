package com.example.ChessPlayerApp.BLE;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.example.ChessPlayerApp.BLE.ScanRecycler.DeviceListAdapter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.UUID;

/**
 *  BLE helper  integrates all necessary methods for scanning devices
 *
 *  To reuse this class, just modify constructor eg. change DeviceListAdapter signature, and scanCallback function
 */

public class BLEScanHelper {

    public static BLEScanHelper instance;

    private static final long SCAN_PERIOD = 10000;

    private BluetoothAdapter mBluetooth;
    private LinkedList<BluetoothDevice> deviceList = new LinkedList<>();

    // for recycler view
    private DeviceListAdapter mDeviceListAdapter = null;
    // for spin list
    private ArrayAdapter<String> mSpinAdaptor = null;

    //private List<UUID> mServices = new ArrayList<>();

    public static final UUID charUUID_v1 = UUID.nameUUIDFromBytes(TransmitionGattAttributes.charUUID_vendor1.getBytes());
    public static final UUID charUUID_v2 = UUID.nameUUIDFromBytes(TransmitionGattAttributes.charUUID_vendor2.getBytes());

    private Handler mHandler;
    private boolean mScanning = false;

    private static ArrayList<ScanFilter> filters;



    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d("BLE_DEBUG", "Scan_Result: " + result.getDevice().getAddress() + " " + result.getDevice().getName());
            if (mDeviceListAdapter != null) {
                if (result.getDevice().getType() == BluetoothDevice.DEVICE_TYPE_LE && !mDeviceListAdapter.containDevice(result.getDevice())) {
                    mDeviceListAdapter.addDevice(result.getDevice());
                }
            } else if (mSpinAdaptor != null){
                for (int i = 0; i < mSpinAdaptor.getCount(); i++){
                    if (mSpinAdaptor.getItem(i).equals(result.getDevice().getAddress()))
                        return;
                }
                Log.d("BLE_DEBUG", "Addr ---- " + result.getDevice().getAddress());
                mSpinAdaptor.add(result.getDevice().getAddress());
                mSpinAdaptor.notifyDataSetChanged();
            }

        }
    };

    static{
        filters = new ArrayList<>();
        ParcelUuid pu_v1 = new ParcelUuid(UUID.fromString(TransmitionGattAttributes.serviceUUID_vendor1));
        ParcelUuid pu_v2 = new ParcelUuid(UUID.fromString(TransmitionGattAttributes.serviceUUID_vendor2));
        ScanFilter filter1 = new ScanFilter.Builder().setServiceUuid(pu_v1).build();
        ScanFilter filter2 = new ScanFilter.Builder().setServiceUuid(pu_v2).build();
        filters.add(filter1);
        filters.add(filter2);
    }

    public static BLEScanHelper getInstance(BluetoothAdapter bluetooth, DeviceListAdapter listAdapter){
        if(instance == null){
            instance = new BLEScanHelper(bluetooth, listAdapter);
        }
        return instance;
    }

    public static BLEScanHelper getInstance(BluetoothAdapter bluetooth, ArrayAdapter<String> adapter){
        if(instance == null){
            instance = new BLEScanHelper(bluetooth, adapter);
        }
        return instance;
    }



    // for recycler view
    private BLEScanHelper(BluetoothAdapter bluetooth, DeviceListAdapter listAdapter){
        mBluetooth = bluetooth;
        mDeviceListAdapter = listAdapter;

        // initialize handler
        mHandler = new Handler();
    }

    private BLEScanHelper(BluetoothAdapter bluetooth, ArrayAdapter<String> adapter){
        mBluetooth = bluetooth;

        mSpinAdaptor = adapter;

        // initialize handler
        mHandler = new Handler();
    }

    public void leScan(final boolean enable) {
        if (enable) {
            if(mDeviceListAdapter != null)
                mDeviceListAdapter.clearDevice();
            else if(mSpinAdaptor != null)
                mSpinAdaptor.clear();

            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetooth.getBluetoothLeScanner().stopScan(scanCallback);
                    //invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            // without using filter
            mBluetooth.getBluetoothLeScanner().startScan(scanCallback);
        } else {
            mScanning = false;
            mBluetooth.getBluetoothLeScanner().stopScan(scanCallback);
        }
        //invalidateOptionsMenu();
    }











}
