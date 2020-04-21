package com.example.ChessPlayerApp.BLE.ScanRecycler;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ChessPlayerApp.R;

import java.util.LinkedList;


/**
 *  This class functions as ListAdapter as well as  implement connection of ble
 */
public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder>{

    public static final String DEBUG_TAG = "BlueDebug";
    private Context parentContext;

    // Device List for showing
    private final LinkedList<BluetoothDevice> mDeviceList;
    private LayoutInflater mInflater;



    // Gatt for handle BLEScanHelper communication
    BluetoothGatt mBluetoothGatt;

    boolean bleConnected = false;



    public DeviceListAdapter(Context context, LinkedList<BluetoothDevice> deviceList){
        mInflater = LayoutInflater.from(context);
        parentContext = context;
        this.mDeviceList = deviceList;
    }

    public void clearDevice(){
        mDeviceList.clear();
        notifyDataSetChanged();
    }


    public void addDevice(BluetoothDevice device){
        mDeviceList.add(device);
        notifyDataSetChanged();
    }

    public boolean containDevice(BluetoothDevice device){
        return mDeviceList.contains(device);
    }

    @NonNull
    @Override
    public DeviceListAdapter.DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View mItemView = mInflater.inflate(R.layout.devicelist_item, viewGroup, false);
        return new DeviceViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceListAdapter.DeviceViewHolder deviceViewHolder, int i) {
        deviceViewHolder.deviceName.setText(mDeviceList.get(i).getName());
        deviceViewHolder.deviceAddr.setText(mDeviceList.get(i).getAddress());
    }

    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }

    // connect to bluetooth device
    private void connectToGattServer(BluetoothDevice device){
        if (bleConnected)
            mBluetoothGatt.close();

        mBluetoothGatt = device.connectGatt(parentContext, false, mGattCallback);
    }


    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            final String address = gatt.getDevice().getAddress();
            if (newState == BluetoothProfile.STATE_CONNECTED){
                Log.d(DEBUG_TAG, "BLEScanHelper connected successfully!");
                bleConnected = true;

                ((Activity) parentContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(parentContext, "BLEScanHelper Connected: " + address, Toast.LENGTH_SHORT).show();
                    }
                });

                // Update state of BLEScanHelper list item

                mBluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                Log.d(DEBUG_TAG, "Disconnected from GATT server.");
                bleConnected = false;
                // Update state of BLEScanHelper list item

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            // only connect to specific service and char.
            for (BluetoothGattService service: gatt.getServices()){
                Log.d(DEBUG_TAG, "Services: " + service.getUuid());
                for (BluetoothGattCharacteristic characteristic: service.getCharacteristics()){
                    Log.d(DEBUG_TAG, "Characteristic UUID: " + characteristic.getUuid());
                    Log.d(DEBUG_TAG, "Characteristic Value: " + characteristic.getValue());
                    for (BluetoothGattDescriptor descriptor: characteristic.getDescriptors()){
                        Log.d(DEBUG_TAG, "Descriptor: " + descriptor.getValue());
                    }
                }
            }
        }
    };

    class DeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public final TextView deviceName;
        public final TextView deviceAddr;
        //public final ImageView deviceStatus;
        final DeviceListAdapter mAdapter;


        public DeviceViewHolder(View itemView, DeviceListAdapter adapter) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.device_name);
            deviceAddr = itemView.findViewById(R.id.device_address);
            // deviceStatus = itemView.findViewById(R.id.device_status);
            this.mAdapter = adapter;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.d(DEBUG_TAG, "Click___________________________");
            BluetoothDevice currentBD = mDeviceList.get(getAdapterPosition());
            connectToGattServer(currentBD);

        }
    }
}
