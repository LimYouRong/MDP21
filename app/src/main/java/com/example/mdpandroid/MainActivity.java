package com.example.mdpandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //instantiate class to generate map
    public MazeView mazeView;

    //navbar
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    //components
    private TextView deviceName;
    private ImageButton upButton;
    private ImageButton downButton;
    private ImageButton leftButton;
    private ImageButton rightButton;
    private Button chatboxBtn;
    private Button explore_button;
    private Button fastest_button;
    private Button image_recog_button;
    private Button refresh_button;
    private Button set_wp_button;
    private Button set_rob_button;
    private Button reset_button;
    private Switch switchAuto;
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
        dl = findViewById(R.id.activity_main);
        dl.addDrawerListener(t);
        t = new ActionBarDrawerToggle(this, dl,R.string.app_name, R.string.app_name);
        t.syncState();
        nv = findViewById(R.id.nv);
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
        switchAuto = findViewById(R.id.auto_switch);

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


        explore_button = findViewById(R.id.explore_button);
        explore_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAGGGGGGGGGGGG","Explore button clicked");
                Toast.makeText(MainActivity.this, "EXPLORE BUTTON PRESSED", Toast.LENGTH_SHORT).show();
                sendToBlueToothChat("PC,AN,startexp");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendToBlueToothChat("AR,AN,s");
                        Log.d("INTENTIONAL DELAY","SENT");
                    }
                }, 1000);

                setStatusMessage("Exploration in progress");

            }
        });
        fastest_button = findViewById(R.id.fastest_button);
        fastest_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("TAGGGGGGGGGGGG","fastest button clicked"+mazeView.getWaypoint()[0]+","+ mazeView.getWaypoint()[1]);
                Toast.makeText(MainActivity.this, "FASTEST BUTTON PRESSED", Toast.LENGTH_SHORT).show();
                sendToBlueToothChat("PC,AN,startfp");
                setStatusMessage("Fastest Path in progress");
            }
        });
        image_recog_button = findViewById(R.id.image_recog_button);
        image_recog_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAGGGGGGGGG", "IMAGE RECOG BUTTON PRESSED");
                sendToBlueToothChat("PC,AN,startIMGexp");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendToBlueToothChat("AR,AN,s");
                        Log.d("INTENTIONAL DELAY","SENT");
                    }
                }, 1000);


                setStatusMessage("Image recog in progress");

            }
        });


        image_recog_button = findViewById(R.id.image_recog_button);
        refresh_button = findViewById(R.id.refresh_button);
        set_wp_button = findViewById(R.id.set_wp_button);
        set_rob_button = findViewById(R.id.set_rob_button);
        reset_button = findViewById(R.id.reset_button);
        set_obs_button = findViewById(R.id.set_obs_button);

        //initialize components
        deviceName = findViewById(R.id.deviceName);
        deviceName.setText("No connection found");
        upButton = findViewById(R.id.front_button);
        downButton = findViewById(R.id.back_button);
        leftButton = findViewById(R.id.left_button);
        rightButton = findViewById(R.id.right_button);

        robcoord = findViewById(R.id.robcoord);
        robcoord.setText("X:-- Y:--");
        final TextView wayPtText = findViewById(R.id.waypt);

        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("LEFT BTN TAG","Left button clicked");
                sendToBlueToothChat("AR,AN,L,");
                mazeView.turn(0);
                setStatusMessage("Rotating Left");
                robcoord.setText("X:"+mazeView.getCurrentPosition()[0]+" Y:"+mazeView.getCurrentPosition()[1]);
            }
        });
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("RIGHT BTN TAG","right button clicked");
                sendToBlueToothChat("AR,AN,R,");
                mazeView.turn(1);
                setStatusMessage("Rotating Right");
                robcoord.setText("X:"+mazeView.getCurrentPosition()[0]+" Y:"+mazeView.getCurrentPosition()[1]);
            }
        });
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("UP BTN TAG","up button clicked");
                sendToBlueToothChat("AR,AN,1,");
                mazeView.move(0);
                setStatusMessage("Moving Forward");
                robcoord.setText("X:"+mazeView.getCurrentPosition()[0]+" Y:"+mazeView.getCurrentPosition()[1]);
            }
        });
        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mazeView.realTimeObstacleCheck("2 2 2 4 1 0");
                Log.d("DOWN BTN TAG","down button clicked");
            }
        });



        set_wp_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d("WAYPOINT BTN TAG","setwaypoint button");
                    mazeView.setWaypoint(mazeView.getTouchPos());
                    wayPtText.setText("X:"+(mazeView.getWaypoint()[0]+1)+" , Y:"+(mazeView.getWaypoint()[1]+1));
                    sendToBlueToothChat("PC,AN,"+(mazeView.getWaypoint()[0]+1)+","+(mazeView.getWaypoint()[1]+1)+",");
                    mazeView.invalidate();
            }
        });


        set_rob_button.setOnClickListener(new View.OnClickListener() {//to test
            @Override
            public void onClick(View v) {
                Log.d("SET ROB BTN TAG","setrobbutton button");
                mazeView.setCurrentPosition(mazeView.getTouchPos()[0],mazeView.getTouchPos()[1]);
                robcoord.setText("X:"+(mazeView.getCurrentPosition()[0]+1)+" Y:"+(mazeView.getCurrentPosition()[1]+1));
                mazeView.invalidate();
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
                mazeView.setObstacle(mazeView.getTouchPos()[1],mazeView.getTouchPos()[0]);
            }
        });

        //https://developer.android.com/reference/android/hardware/SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 50000000);
        tiltSwitch = findViewById(R.id.tilt_switch);
        tiltSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                _tilt = isChecked;
            }
        });

        //retrieve intent values
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey("device")) {
                device = bundle.getString("device");
                if(device == null){
                    Log.d("NO DEVICE TAG","NO DEVICE FOUND");
                    deviceName.setText("Device not Connected ");
                }
                else if (!device.equalsIgnoreCase("")) {
                    Log.d("DEVICE NAME TAG",device);
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
        chatboxBtn = findViewById(R.id.chatboxBtn);
        chatboxEditText = findViewById(R.id.chatboxEt);
        chatView = findViewById(R.id.reyclerview_message_list);
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
        Message msgOut = new Message(id,msg);//id = 0 for user who sent this message, 1 for message received
        messageList.add(messageList.size(), msgOut);
        messageAdapter.notifyDataSetChanged();

    }
    //update status whenever connection changes
    private BroadcastReceiver BluetoothMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String theMessage = intent.getStringExtra("bluetoothMessage");
            Log.d("MESSAGE Received ***************************************",theMessage);

            if(theMessage.equals("Finish")){
                setStatusMessage("WAITING");
            }
            else if(theMessage.regionMatches(0,"OBS|",0,4)){
                Log.d("OBS| TAG",theMessage);
                String []split = theMessage.split("\\|");
                mazeView.realTimeObstacleCheck(split[1]);
            }
            if(theMessage.regionMatches(0,"MDFshort|",0,4)){
                findStr1Str2(theMessage);
            }
            if(theMessage.regionMatches(0,"MDFlong|",0,4)){
                mdfLong(theMessage);
            }

            else if(theMessage.regionMatches(0,"NUM|",0,4)){
                addNumberGrid(theMessage);
            }
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
                device = "";
            } else {
                //device connected, enable all bluetooth-related actions
                deviceName.setText("Connected to: "+theName);
                device = theName;
            }
        }
    };
//MDFlong|1|4|3|fffffffffffffffffffffffffffffffffffffffffff8ffffffffffffffffffffffffffffffff|0000000000000400080010c000f0000000000000007007e00000000c00000800100020004000

    private void mdfLong(String theMessage) {
        String[] split = theMessage.split("\\|");
        int mdf1=Integer.parseInt(split[1]);//x
        int mdf2=Integer.parseInt(split[2]);//y
        int mdf3=Integer.parseInt(split[3]);//direction

        mazeView.setCurrentPosition(mdf2,mdf1);
        mazeView.setCurrentAngle(mdf3*90);

        Log.d("MDF12",split[1]+"   "+split[2]);
        Log.d("MDFLONGSTR4&5",split[4]+"sth\n"+split[5]+"sth");
        Log.d("mdf4 running hex to binary ","sth: "+split[4].length());
        String mdf4=hexToBinary(split[4]);//mdf1
        Log.d("mdf4 ended hex to binary ","sth: "+mdf4.length());
        Log.d("mdf5 running hex to binary ","sth: "+split[5].length());
        String mdf5=hexToBinary(split[5]);//mdf2
        Log.d("mdf5 ended hex to binary ","sth: "+split[5].length());
        try{
            setMDFShort(mdf4,mdf5);
        }catch (NumberFormatException e){
            Log.d("OOOOOOOOOOOOOOOOOOOOO","Number format exception");
        }

    }
    private void setMDFShort(String mdf1,String mdf2){
        Log.d("#####################3",mdf1+" "+mdf1.length());
        Log.d("#####################3",mdf2+" "+mdf2.length());
        int row=0;
        int column=0;
        int obsPointer=0;
        for(int i=2;i<mdf1.length()-2;i++){
            //try {
            if(mdf1.charAt(i)=='1'){
                mazeView.setExplored(row,column);
                if(mdf2.charAt(obsPointer)=='1'){
                    mazeView.setObstacle(row,column);
                }
                if(obsPointer<mdf2.length())obsPointer++;
            }
            else{
                mazeView.setUnexplored(row,column);
            }
            if(column==14){
                column=0;
                row++;
            }
            else column++;
        }
    }
    private void findStr1Str2(String theMessage){
        String[] split = theMessage.split("\\|");
        String mdf1=hexToBinary(split[1]);
        String mdf2=hexToBinary(split[2]);
        try{
            setMDFShort(mdf1,mdf2);
        }catch (NumberFormatException e){
            Log.d("OOOOOOOOOOOOOOOOOOOOO","Number format exception");
        }

    }

    private String hexToBinary(String str){
        String ret = "";
        str=str.trim();
        Log.d("Length of str after trim","ssth:"+str.length());
        for(int i = 0; i < str.length(); i++){
            if(str.charAt(i)==' ')continue;
            String tem = new BigInteger(String.valueOf(str.charAt(i)), 16).toString(2);
            for(int j = 0; j < 4-tem.length(); j++) ret += "0";
            ret += tem;
        }

        return ret;
    }

    private void addNumberGrid(String str){
        Pattern pattern = Pattern.compile("\\(([^)]+)\\)");
        Matcher m = pattern.matcher(str);
        while(m.find()) {
            String[] splitItem = m.group(1).split(", ");
            Log.d("Stringitemmmmmmmmmmmmmmmmmmmmmm",m.group(1));
            mazeView.setNumberGrid(splitItem[0],Integer.parseInt(splitItem[1]),Integer.parseInt(splitItem[2]));
        }
        mazeView.invalidate();

    }

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
    public void updateCoord(){
        coord = findViewById(R.id.coord);
        coord.setText("X:"+(mazeView.getTouchPos()[0]+1)+" Y:"+(mazeView.getTouchPos()[1]+1));
    }
}
