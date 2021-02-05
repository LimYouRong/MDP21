package com.example.mdpandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

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
    private EditText chatboxEditText;


    public static MessageAdapter messageAdapter;
    private RecyclerView chatView;
    private List<Message> messageList;

    private String device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        setContentView(R.layout.activity_main);

        //nav bar
        dl = (DrawerLayout)findViewById(R.id.activity_main);
        dl.addDrawerListener(t);
        t = new ActionBarDrawerToggle(this, dl,R.string.app_name, R.string.app_name);
        t.syncState();
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        //initialize components
        deviceName = (TextView) findViewById(R.id.deviceName);
        deviceName.setText("No connection found");
//        upButton = findViewById(R.id.upButton);
//        downButton = findViewById(R.id.downButton);
//        leftButton = findViewById(R.id.leftButton);
//        rightButton = findViewById(R.id.rightButton);

        //TODO Set onclick listeners for them later


        //retrieve intent values
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey("device")) {
                device = bundle.getString("device");
                if(device == null){
                    Log.d("FindMEEEEEEEEEEEEEEEEE","null");
                }
                else if (!device.equalsIgnoreCase("")) {
                    //enable all bluetooth-related buttons because there is a device connected
                    deviceName.setText("Connected to: " + device);
                }
            }
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(BluetoothStatusReceiver,
                new IntentFilter("getConnectedDevice"));
//        LocalBroadcastManager.getInstance(this).registerReceiver(BluetoothTextReceiver,
//                new IntentFilter("getTextFromDevice"));
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
                String message = chatboxEditText.getText().toString();
                if (message.length() < 1){
                    //user did not type anything
                    Toast.makeText(MainActivity.this, "Text field is empty", Toast.LENGTH_SHORT).show();
                } else {
                    Message msgOut = new Message(0, message); //id = 0 because user is the one who sent this message
                    //append current message to arraylist
                    messageList.add(messageList.size(), msgOut);
                    messageAdapter.notifyDataSetChanged();
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
    }

    //update status whenever connection changes
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
//                device = "";
            } else {
                //device connected, enable all bluetooth-related actions
//                nameTv.setText("Connected to: "+theName);
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
//                device = theName;
            }
        }
    };

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
}
