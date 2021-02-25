package com.example.mdpandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //instantiate class to generate map
    public MazeView mazeView;

    //navbar
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    //components
    private TextView deviceName;
    private Button upButton;
    private Button downButton;
    private Button leftButton;
    private Button rightButton;
    private Button chatboxBtn;
    private Button load_mapBtn;
    private Button send_mapBtn;
    private Button explore_button;
    private Button fastest_button;
    private Button calibrate_button;
    private Button refresh_button;
    private Button set_wp_button;
    private Button set_rob_button;
    private Button reset_button;
    private Switch switchAuto;
    private boolean SettingWaypoint = false;
    private boolean SettingRobot;
    private Button set_obs_button;

    private TextView robcoord;
    private TextView coord;

    private EditText chatboxEditText;
    private TextView robot_staText;


    public static MessageAdapter messageAdapter;
    private RecyclerView chatView;
    private List<Message> messageList;

    private SensorManager sensorManager;
    private Switch tiltSwitch;
    private boolean _tilt=false;


    private String device;
    boolean autoUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //nav bar
        dl = (DrawerLayout)findViewById(R.id.activity_main);
        dl.addDrawerListener(t);
        t = new ActionBarDrawerToggle(this, dl,R.string.app_name, R.string.app_name);
        t.syncState();
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        nv = (NavigationView)findViewById(R.id.nv);
        nv.setItemIconTintList(null);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id)
                {
                    case R.id.home:
                        dl.closeDrawers();
                        return true;
                    case R.id.bluetooth:
                        Intent i = new Intent(MainActivity.this, BluetoothActivity.class);
                        //send device name to next activity to allow app to know whether there is still bluetooth connection
                        i.putExtra("device", device);
                        startActivity(i);
                        return true;
                    case R.id.settings:
                        Intent x = new Intent(MainActivity.this, SettingsActivity.class);
                        //send device name to next activity to allow app to know whether there is still bluetooth connection
                        x.putExtra("device", device);
                        startActivity(x);
                        return true;
                    default:
                        return true;
                }
            }
        });


        mazeView = findViewById(R.id.mazeView);
        switchAuto = (Switch) findViewById(R.id.auto_switch) ;

        switchAuto.setChecked(true);
        autoUpdate = true;
        switchAuto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    autoUpdate = true;
                    refresh_button.setEnabled(false);
                    mazeView.invalidate();
                }else {
                    autoUpdate = false;
                }
            }
        });

        load_mapBtn = findViewById(R.id.load_map);
        send_mapBtn = findViewById(R.id.send_map);


        explore_button = findViewById(R.id.explore_button);
        explore_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAGGGGGGGGGGGG","Explore button clicked");
                sendToBlueToothChat("PC,AN,startexp");
                setStatusMessage("Exploration in progress");

            }
        });
        fastest_button = findViewById(R.id.fastest_button);
        fastest_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("TAGGGGGGGGGGGG","fastest button clicked"+mazeView.getWaypoint()[0]+","+ mazeView.getWaypoint()[1]);
                sendToBlueToothChat("PC,AN,startfp");
                setStatusMessage("Fastest Path in progress");
            }
        });


        calibrate_button = findViewById(R.id.calibrate_button);
        refresh_button = findViewById(R.id.refresh_button);
        set_wp_button = findViewById(R.id.set_wp_button);
        set_rob_button = findViewById(R.id.set_rob_button);
        reset_button = findViewById(R.id.reset_button);
        set_obs_button = findViewById(R.id.set_obs_button);

        //initialize components
        deviceName = (TextView) findViewById(R.id.deviceName);
        deviceName.setText("No connection found");
        upButton = findViewById(R.id.front_button);
        downButton = findViewById(R.id.back_button);
        leftButton = findViewById(R.id.left_button);
        rightButton = findViewById(R.id.right_button);

        robcoord = (TextView) findViewById(R.id.robcoord);
        robcoord.setText("X:-- Y:--");
        final TextView wayPtText = findViewById(R.id.waypt);

        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAGGGGGGGGGGGG","Left button clicked");
                sendToBlueToothChat("AR,AN,L,");
                mazeView.turn(0);
                setStatusMessage("Rotating Left");
                robcoord.setText("X:"+mazeView.getCurrentPosition()[0]+" Y:"+mazeView.getCurrentPosition()[1]);
            }
        });
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAGGGGGGGGGGGG","right button clicked");
                sendToBlueToothChat("AR,AN,R,");
                mazeView.turn(1);
                setStatusMessage("Rotating Right");
                robcoord.setText("X:"+mazeView.getCurrentPosition()[0]+" Y:"+mazeView.getCurrentPosition()[1]);
            }
        });
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAGGGGGGGGGGGG","up button clicked");
                sendToBlueToothChat("AR,AN,1,");
                mazeView.move(0);
                setStatusMessage("Moving Forward");
                robcoord.setText("X:"+mazeView.getCurrentPosition()[0]+" Y:"+mazeView.getCurrentPosition()[1]);
            }
        });
        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mazeView.realTimeObstacleCheck("2 2 2 4 1 0");
                Log.d("TAGGGGGGGGGGGG","down button clicked");
//                sendToBlueToothChat("AR,AN,B,");
//                mazeView.move(1);
//                setStatusMessage("Rotating");
//                robcoord.setText("X:"+mazeView.getCurrentPosition()[0]+" Y:"+mazeView.getCurrentPosition()[1]);
            }
        });



        set_wp_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d("TAGGGGGGGGGGGG","setwaypoint button");
                //TODO Change line below to retrieve current pressed map box
//                mazeView.setWaypoint(new int[]{2, 2});
//                Log.d("PPPPPPPPPPPPPPPPPPPPPPPPPPPPPP",mazeView.getWaypoint()[0]+" : "+ mazeView.getWaypoint()[1] );
//
//                wayPtText.setText("X:"+(mazeView.getWaypoint()[0]+1)+" , Y:"+(mazeView.getWaypoint()[1]+1));
//                sendToBlueToothChat("PC,AN,"+(mazeView.getWaypoint()[0]+1)+","+(mazeView.getWaypoint()[1]+1));
//                if(!SettingWaypoint){
//                    SettingWaypoint=true;
//                    set_wp_button.setText("Save Setting");
//                    set_rob_button.setEnabled(false);
//                    Log.d("CHangewpppppppppppppppppppp","Changing");
//
//                    wayPtText.setText("X:"+(mazeView.getWaypoint()[0]+1)+" , Y:"+(mazeView.getWaypoint()[1]+1));
//
//                }
//                else{
//                    SettingWaypoint=false;
                    mazeView.setWaypoint(mazeView.getTouchPos());
                    wayPtText.setText("X:"+(mazeView.getWaypoint()[0]+1)+" , Y:"+(mazeView.getWaypoint()[1]+1));
                    sendToBlueToothChat("PC,AN,"+(mazeView.getWaypoint()[0]+1)+","+(mazeView.getWaypoint()[1]+1)+",");

                    //PC,AN,10,20
//                    set_wp_button.setText("SET WAYPOINT");
//                    set_rob_button.setEnabled(true);
//                }
            }
        });


        set_rob_button.setOnClickListener(new View.OnClickListener() {//to test
            @Override
            public void onClick(View v) {
                Log.d("TAGGGGGGGGGGGG","setrobbutton button");
//                TODO Change line below to retrieve current pressed map box
//                mazeView.setCurrentPosition(new int[]{5, 5});
//                robcoord.setText("X:"+(mazeView.getCurrentPosition()[0]+1)+" Y:"+(mazeView.getCurrentPosition()[1]+1));
//                sendToBlueToothChat("PC,AN,"+ (mazeView.getCurrentPosition()[0]+1)+","+(mazeView.getCurrentPosition()[1]+1));
//                if(!SettingRobot){
//                    SettingRobot=true;
//                    set_rob_button.setText("DONE SETTING");
//                    set_wp_button.setEnabled(false);
//                    Log.d("CHangerob","Can change now");
//                }
//                else{
//                    SettingRobot=false;
//                    robcoord.setText("X:"+(mazeView.getCurrentPosition()[0]+1)+" Y:"+(mazeView.getCurrentPosition()[1]+1));
//                    set_rob_button.setText("SET POSITION");
//                    set_wp_button.setEnabled(true);
//                }
                mazeView.setCurrentPosition(mazeView.getTouchPos());
                robcoord.setText("X:"+(mazeView.getCurrentPosition()[0]+1)+" Y:"+(mazeView.getCurrentPosition()[1]+1));

            }
        });

        refresh_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mazeView.invalidate();
            }
        });

        reset_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mazeView.resetMap();
                robcoord.setText("X:-- Y:--");
                wayPtText.setText("X:-- Y:--");
            }
        });
        set_obs_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
//                mazeView
                mazeView.setObstacle(mazeView.getTouchPos()[0],mazeView.getTouchPos()[1]);
            }
        });

        //https://developer.android.com/reference/android/hardware/SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 50000000);
        tiltSwitch = (Switch) findViewById(R.id.tilt_switch);
        tiltSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    _tilt = true;
                }else {
                    _tilt =false;
                }
            }
        });


        //retrieve intent values
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey("device")) {
                device = bundle.getString("device");
                if(device == null){
                    Log.d("FindMEEEEEEEEEEEEEEEEE","null");
                }
                else if (!device.equalsIgnoreCase("")) {
                    Log.d("hehehehehehhehehhhhheh",device);
                    //enable all bluetooth-related buttons because there is a device connected
                    deviceName.setText("Connected to: " + device);
                }
            }
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(BluetoothStatusReceiver,
                new IntentFilter("getConnectedDevice"));
        LocalBroadcastManager.getInstance(this).registerReceiver(BluetoothMessageReceiver,
                new IntentFilter("getBluetoothMessage"));
        //Bluetooth chat
        chatboxBtn = (Button) findViewById(R.id.chatboxBtn);
        chatboxEditText = (EditText) findViewById(R.id.chatboxEt);
        chatView = (RecyclerView) findViewById(R.id.reyclerview_message_list);
        messageList = new ArrayList<Message>();
        messageAdapter = new MessageAdapter(messageList);
        chatView.setAdapter(messageAdapter);
        chatView.setLayoutManager(new LinearLayoutManager(this));

        //button to send out messages that user has typed
        chatboxBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String message = chatboxEditText.getText().toString().trim();
                if (message.length() < 1){
                    //user did not type anything
                    Toast.makeText(MainActivity.this, "Text field is empty", Toast.LENGTH_SHORT).show();
                } else {
//                    updateBluetoothChat(0,message);
                    chatboxEditText.setText("");
                    sendToBlueToothChat(message);
                }
            }
        });
    }

    private void sendToBlueToothChat(String msg) {
        Intent intent = new Intent("BlueToothChatSendIntent");
        intent.putExtra("bluetoothTextSend", msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        updateBluetoothChat(0,msg);
    }

    private void updateBluetoothChat(int id,String msg){
        Message msgOut = new Message(id,msg);
//        Message msgOut = new Message(0, message); //id = 0 because user is the one who sent this message
        messageList.add(messageList.size(), msgOut);
        messageAdapter.notifyDataSetChanged();

    }
    //update status whenever connection changes
    private BroadcastReceiver BluetoothMessageReceiver = new BroadcastReceiver() {


        @Override
        public void onReceive(Context context, Intent intent) {
            String theMessage = intent.getStringExtra("bluetoothMessage");
            Log.d("MESSAGE FROM MAIN ***************************************",theMessage);
            if(theMessage.equals("Finish")){
                setStatusMessage("WAITING");
            }
            //TODO YOURONG
            else if(theMessage.equals("TEST")){
//                mazeView.realTimeObstacleCheck(theMessage);
            }
            //else if message starts with
            //update map
            //TODO JIAWEN
//            else if message starts with MDF|hexadecimal stuff
            //update map
            updateBluetoothChat(1,theMessage);

        }
    };
    private BroadcastReceiver BluetoothStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String theName = intent.getStringExtra("bluetoothTextReceive");
            if (theName == ""){

                //no device connected, disable bluetooth-related actions
//                nameTv.setText(noDeviceMsg);
//                cbBtn.setEnabled(false);
//                //joystickRight.setEnabled(false);
//                upBtn.setEnabled(false);
//                downBtn.setEnabled(false);
//                leftBtn.setEnabled(false);
//                rightBtn.setEnabled(false);
//                readyBtn.setEnabled(false);
//                readyBtn.setBackgroundResource(R.drawable.disabledbutton);
//                //--dcBtn.setEnabled(false);
//                messageList.clear();
//                chatAdapter.notifyDataSetChanged();
//                statusTv.setText("Offline");
                device = "";
            } else {
                //device connected, enable all bluetooth-related actions
                deviceName.setText("Connected to: "+theName);
//                cbBtn.setEnabled(true);
//                //joystickRight.setEnabled(true);
//                upBtn.setEnabled(true);
//                downBtn.setEnabled(true);
//                leftBtn.setEnabled(true);
//                rightBtn.setEnabled(true);
//                readyBtn.setEnabled(true);
//                readyBtn.setBackgroundResource(R.drawable.commonbutton);
//                //--dcBtn.setEnabled(true);
//                statusTv.setText("Waiting for instructions");
                device = theName;
            }
        }
    };
    private void setStatusMessage(String message){
        robot_staText = findViewById(R.id.robot_sta);
        robot_staText.setText(message);


    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(BluetoothStatusReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (_tilt) {
            float x = event.values[0];
            float y = event.values[1];
            //move robot only if tilt has been enabled
            if (y < -5) {
                //device has been tilted forward
                sendToBlueToothChat("AR,AN,1,");
                mazeView.move(0);
                setStatusMessage("Moving Forward");
                robcoord.setText("X:"+mazeView.getCurrentPosition()[0]+" Y:"+mazeView.getCurrentPosition()[1]);
            } else if (x < -5) {
                //device has been tilted to the right
                sendToBlueToothChat("AR,AN,R,");
                mazeView.turn(1);
                setStatusMessage("Rotating Right");
                robcoord.setText("X:"+mazeView.getCurrentPosition()[0]+" Y:"+mazeView.getCurrentPosition()[1]);
            } else if (x > 5) {
                //device has been tilted to the left
                sendToBlueToothChat("AR,AN,L,");
                mazeView.turn(0);
                setStatusMessage("Rotating Left");
                robcoord.setText("X:"+mazeView.getCurrentPosition()[0]+" Y:"+mazeView.getCurrentPosition()[1]);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
//    public boolean getSettingWaypoint(){
//        return SettingWaypoint;
//    }
//    public boolean getSettingRobot(){
//        return SettingRobot;
//    }
    public void updateCoord(){
        coord = (TextView)findViewById(R.id.coord);
        coord.setText("X:"+(mazeView.getTouchPos()[0]+1)+" Y:"+(mazeView.getTouchPos()[1]+1));
    }
}
