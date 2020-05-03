package com.example.ChessPlayerApp.robot_arm.Controller;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;


import com.example.ChessPlayerApp.R;
import com.example.ChessPlayerApp.robot_arm.RobotArmControllerActivity;

import static com.example.ChessPlayerApp.robot_arm.RobotArmControllerActivity.bleServiceConnected;
import static com.example.ChessPlayerApp.robot_arm.RobotArmControllerActivity.mBluetoothLeService;
import static com.example.ChessPlayerApp.robot_arm.RobotArmControllerActivity.mDeviceAddress;
import static com.example.ChessPlayerApp.robot_arm.RobotArmControllerActivity.mConnected;



import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class ControllerFragment extends Fragment implements View.OnClickListener{

    private static ControllerFragment instance;

    public static RoboticArm myRoboticArm;

    // Distance from chessboard to original point, block size
    private double dist = 106;
    private double block_size = 31;
    private double piece_y = 0;

    private double gripperOffset = 36;

    // coordinate of a1
    private double a1_x;
    private double a1_y;

    private boolean firstClick;
    private String firstPos;



    // UI
    public static TextView mConsole;
    public static ScrollView mScrollView;

    // Connection
    public static Button mConnectButton;

    // Stepper/Fan
    public static Switch mSwitch_stepper;
    public static Switch mSwitch_fan;
    // Gripper
    public static Button mGripperClose;
    public static Button mGripperOpen;

    // Gripper param
    public static EditText close_num;
    public static EditText open_num;

    // Position
    public static Button mHome;
    public static Button mBottom;
    public static Button mRest;
    public static Button mEndStop;
    // Movement Granularity

    // Movements
    // y
    public static Button y_fine_plus_button;
    public static Button y_coarse_plus_button;
    public static Button y_fine_minus_button;
    public static Button y_coarse_minus_button;
    // x
    public static Button x_fine_plus_button;
    public static Button x_coarse_plus_button;
    public static Button x_fine_minus_button;
    public static Button x_coarse_minus_button;
    // z
    public static Button z_fine_plus_button;
    public static Button z_coarse_plus_button;
    public static Button z_fine_minus_button;
    public static Button z_coarse_minus_button;



    // Device spinner
    private Spinner mSpinner;
    public static ArrayAdapter<String> mSpinnerAdapter;
    private List<String> deviceList;

    // Coarse Movement Spinner
    public static Spinner coarseSpinner;
    // Fine Movement Spinner
    public static Spinner fineSpinner;

    public static ControllerFragment getInstance(){
        if (instance == null)
            instance = new ControllerFragment();

        return instance;
    }



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        View root = inflater.inflate(R.layout.fragment_controller, container, false);


        // calculate coordinate of a1
        a1_x = block_size*7/2;
        a1_y = dist + block_size*15/2;

        firstClick = false;

        initUI(root);


        return root;
    }

    public void initUI(View root){
        // Console
        mConsole = root.findViewById(R.id.console);
        mScrollView = root.findViewById(R.id.status_scroll_view);

        // EditText
        close_num = root.findViewById(R.id.gripper_close_button_number);
        open_num = root.findViewById(R.id.gripper_open_button_number);

        // Buttons
        mConnectButton = root.findViewById(R.id.connect_button);
        mConnectButton.setOnClickListener(this);

        mGripperClose = root.findViewById(R.id.gripper_close_button);
        mGripperOpen = root.findViewById(R.id.gripper_open_button);
        mGripperClose.setOnClickListener(this);
        mGripperOpen.setOnClickListener(this);

        mHome = root.findViewById(R.id.home_button);
        mBottom = root.findViewById(R.id.bottom_button);
        mRest = root.findViewById(R.id.rest_button);
        mEndStop = root.findViewById(R.id.end_stop_button);
        mHome.setOnClickListener(this);
        mBottom.setOnClickListener(this);
        mRest.setOnClickListener(this);
        mEndStop.setOnClickListener(this);

        x_fine_plus_button = root.findViewById(R.id.x_fine_plus_button);
        x_coarse_plus_button = root.findViewById(R.id.x_coarse_plus_button);
        x_fine_minus_button = root.findViewById(R.id.x_fine_minus_button);
        x_coarse_minus_button = root.findViewById(R.id.x_coarse_minus_button);
        x_fine_plus_button.setOnClickListener(this);
        x_coarse_plus_button.setOnClickListener(this);
        x_fine_minus_button.setOnClickListener(this);
        x_coarse_minus_button.setOnClickListener(this);

        y_fine_plus_button = root.findViewById(R.id.y_fine_plus_button);
        y_coarse_plus_button = root.findViewById(R.id.y_coarse_plus_button);
        y_fine_minus_button = root.findViewById(R.id.y_fine_minus_button);
        y_coarse_minus_button = root.findViewById(R.id.y_coarse_minus_button);
        y_fine_plus_button.setOnClickListener(this);
        y_coarse_plus_button.setOnClickListener(this);
        y_fine_minus_button.setOnClickListener(this);
        y_coarse_minus_button.setOnClickListener(this);

        z_fine_plus_button = root.findViewById(R.id.z_fine_plus_button);
        z_coarse_plus_button = root.findViewById(R.id.z_coarse_plus_button);
        z_fine_minus_button = root.findViewById(R.id.z_fine_minus_button);
        z_coarse_minus_button = root.findViewById(R.id.z_coarse_minus_button);
        z_fine_plus_button.setOnClickListener(this);
        z_coarse_plus_button.setOnClickListener(this);
        z_fine_minus_button.setOnClickListener(this);
        z_coarse_minus_button.setOnClickListener(this);

        // chessboard button
        for (int i = 0; i < 8; i ++)
            for (int j = 0; j < 8; j++){
                int id = getResources().getIdentifier("" + (char)('a'+ i) + (char)('1' + j), "id", getContext().getPackageName());
                //Log.d("MYID", "" + (char)('a'+ i) + (char)('1' + j) + "id is "+ id);
                root.findViewById(id).setOnClickListener(this);
            }

        // spinner
        deviceList = new ArrayList<>();
        mSpinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, deviceList);

        mSpinner = root.findViewById(R.id.spinner_device);
        mSpinner.setAdapter(mSpinnerAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                RobotArmControllerActivity.mDeviceAddress = deviceList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                RobotArmControllerActivity.mDeviceAddress = null;
            }
        });

        // coarse/fine spinner
        coarseSpinner = root.findViewById(R.id.spinner_coarse);
        coarseSpinner.setSelection(2);
        fineSpinner = root.findViewById(R.id.spinner_fine);
        fineSpinner.setSelection(4);

        // stepper switch
        mSwitch_stepper = root.findViewById(R.id.switch_stepper);
        mSwitch_stepper.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    myRoboticArm.activate(true);
                }else{
                    myRoboticArm.activate(false);
                }
            }
        });

        // fan switch
        mSwitch_fan = root.findViewById(R.id.switch_fan);
        mSwitch_fan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    myRoboticArm.activateFan(true);
                }else{
                    myRoboticArm.activateFan(false);
                }
            }
        });


    }

    public static void print(final String msg) {
        mConsole.append(msg + "\n");
        mScrollView.scrollTo(0, mConsole.getBottom());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.connect_button){
            if (bleServiceConnected)
                if (mDeviceAddress != null) {
                    if(!mConnected){
                        ControllerFragment.print("Start Connecting " + mDeviceAddress);
                        myRoboticArm = new RoboticArm(mBluetoothLeService, block_size, dist);
                        myRoboticArm.connect(mDeviceAddress);
                    }else{
                        ControllerFragment.print("Disconnect...");
                        myRoboticArm.disconnect();
                    }

                } else
                    ControllerFragment.print("Please choose a device.");
        }else{
            String action = "";
            switch (v.getId()) {
                case R.id.x_fine_plus_button:
                    myRoboticArm.moveX(Double.parseDouble(fineSpinner.getSelectedItem().toString()));
                    break;
                case R.id.x_coarse_plus_button:
                    myRoboticArm.moveX(Double.parseDouble(coarseSpinner.getSelectedItem().toString()));
                    break;
                case R.id.x_fine_minus_button:
                    myRoboticArm.moveX(-Double.parseDouble(fineSpinner.getSelectedItem().toString()));
                    break;
                case R.id.x_coarse_minus_button:
                    myRoboticArm.moveX(-Double.parseDouble(coarseSpinner.getSelectedItem().toString()));
                    break;
                case R.id.y_fine_plus_button:
                    myRoboticArm.moveY(Double.parseDouble(fineSpinner.getSelectedItem().toString()));
                    break;

                case R.id.y_coarse_plus_button:
                    myRoboticArm.moveY(Double.parseDouble(coarseSpinner.getSelectedItem().toString()));
                    break;

                case R.id.y_fine_minus_button:
                    myRoboticArm.moveY(-Double.parseDouble(fineSpinner.getSelectedItem().toString()));
                    break;

                case R.id.y_coarse_minus_button:
                    myRoboticArm.moveY(-Double.parseDouble(coarseSpinner.getSelectedItem().toString()));
                    break;

                case R.id.z_fine_plus_button:
                    myRoboticArm.moveZ(Double.parseDouble(fineSpinner.getSelectedItem().toString()));
                    break;

                case R.id.z_coarse_plus_button:
                    myRoboticArm.moveZ(Double.parseDouble(coarseSpinner.getSelectedItem().toString()));
                    break;

                case R.id.z_fine_minus_button:
                    myRoboticArm.moveZ(-Double.parseDouble(fineSpinner.getSelectedItem().toString()));
                    break;

                case R.id.z_coarse_minus_button:
                    myRoboticArm.moveZ(-Double.parseDouble(coarseSpinner.getSelectedItem().toString()));
                    break;

                // function button

                case R.id.gripper_close_button:
                    myRoboticArm.closeGripper(Double.parseDouble(close_num.getText().toString()));
                    break;

                case R.id.gripper_open_button:
                    myRoboticArm.openGripper(Double.parseDouble(open_num.getText().toString()));
                    break;

                case R.id.home_button:
                    // 38 is offset of gripper
                    myRoboticArm.moveTo(0,180 + gripperOffset,180);
                    break;

                case R.id.bottom_button:
                    myRoboticArm.moveTo(0,40,60);
                    break;

                case R.id.rest_button:
                    myRoboticArm.moveTo(0,40,70);
                    break;

                case R.id.end_stop_button:
                    myRoboticArm.moveTo(0,19.5,134);
                    break;
            }


            // or it's board button, we need to check the pattern
            String template = "[a-h][1-8]";
            Pattern pattern = Pattern.compile(template);
            String position = getResources().getResourceEntryName(v.getId());
            Matcher matcher = pattern.matcher(position);
            boolean matches = matcher.matches();

            if(matches){
                Log.d("Chessboard", "Board button click: " + position);

                if (firstClick){
                    myRoboticArm.movePiece(firstPos, position);
                    firstClick = false;
                }else{
                    firstPos = position;
                    firstClick = true;
                }

                //myRoboticArm.moveTo(position);

            }
        }

    }

}