package com.example.mdpandroid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.navigation.NavigationView;

/**
 * class for Settings UI
 * used for saving custom messages on app
 *
 * Shared preference code reference https://www.tutorialspoint.com/android/android_shared_preferences.htm
 */
public class SettingsActivity extends AppCompatActivity {

    private Button buttonFirst;
    private Button buttonSecond;
    private Button saveBtn;
    private Button editBtn;
    private EditText editTextTop;
    private EditText editTextBot;
    private TextView textViewTop;
    private TextView textViewBot;
    private String device;

    //navbar
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //get intent
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){
            if (bundle.containsKey("device")){
                device = bundle.getString("device");
            }
        } else {
            device = "";
        }

        buttonFirst = findViewById(R.id.button1);
        buttonSecond = findViewById(R.id.button2);
        saveBtn = findViewById(R.id.buttonSave);
        editBtn = findViewById(R.id.buttonEdit);
        editTextTop = findViewById(R.id.editText1);
        editTextBot = findViewById(R.id.editText2);
        textViewTop = findViewById(R.id.textView5);
        textViewBot = findViewById(R.id.textView6);

        saveBtn.setVisibility(View.INVISIBLE);
        editTextTop.setVisibility(View.INVISIBLE);
        editTextBot.setVisibility(View.INVISIBLE);
        textViewTop.setVisibility(View.INVISIBLE);
        textViewBot.setVisibility(View.INVISIBLE);

        //nav bar
        dl = findViewById(R.id.activity_settings);
        dl.addDrawerListener(t);
        t = new ActionBarDrawerToggle(this, dl,R.string.app_name, R.string.app_name);
        t.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        nv = findViewById(R.id.nv);
        nv.setItemIconTintList(null);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id)
                {
                    case R.id.home:
                        Intent i = new Intent(SettingsActivity.this, MainActivity.class);
                        i.putExtra("device", device);
                        startActivity(i);
                        return true;
                    case R.id.bluetooth:
                        Intent x = new Intent(SettingsActivity.this, BluetoothActivity.class);
                        x.putExtra("device", device);
                        startActivity(x);
                        return true;
                    case R.id.settings:
                        dl.closeDrawers();
                        return true;
                    default:
                        return true;
                }

            }
        });

        /**
         * Shows the editText and save button to update the default message
         * Used value1 and value2 as key
         */
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBtn.setVisibility(View.VISIBLE);
                editTextTop.setVisibility(View.VISIBLE);
                editTextBot.setVisibility(View.VISIBLE);
                textViewTop.setVisibility(View.VISIBLE);
                textViewBot.setVisibility(View.VISIBLE);
                editBtn.setEnabled(false);
                editBtn.setVisibility(View.INVISIBLE);

                SharedPreferences sharedPref = getSharedPreferences("pref", Context.MODE_PRIVATE);
                if (sharedPref.contains("value1")){
                    editTextTop.setText(sharedPref.getString("value1", ""));
                } else {
                    editTextTop.setText("Default Message 1");
                }
                if (sharedPref.contains("value2")){
                    editTextBot.setText(sharedPref.getString("value2", ""));
                } else {
                    editTextBot.setText("Default Message 2");
                }
            }
        });

        /**
         * Save as SharedPreference. Key are value1 and value2 accordingly
         */
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check if any of the edit texts are empty
                if (editTextTop.getText().toString().trim().equalsIgnoreCase("") ||
                        editTextBot.getText().toString().trim().equalsIgnoreCase("")) {
                    Toast.makeText(SettingsActivity.this, "Custom messages cannot be empty", Toast.LENGTH_SHORT).show();
                }
                else {
                    saveBtn.setVisibility(View.INVISIBLE);
                    editTextTop.setVisibility(View.INVISIBLE);
                    editTextBot.setVisibility(View.INVISIBLE);
                    textViewTop.setVisibility(View.INVISIBLE);
                    textViewBot.setVisibility(View.INVISIBLE);
                    editBtn.setEnabled(true);
                    editBtn.setVisibility(View.VISIBLE);

                    SharedPreferences sharedPref = getSharedPreferences("pref", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("value1", editTextTop.getText().toString());
                    editor.putString("value2", editTextBot.getText().toString());
                    editor.commit();
                    Toast.makeText(SettingsActivity.this, "Custom messages saved", Toast.LENGTH_SHORT).show();
                }

            }
        });

        /**
         * Using share preference to save persistance data. This key value pair allows us to store unique value and acts as a way
         * to store and retrieve simple data like a few strings.
         *
         */
        buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set default message
                String value;
                //retrieve app's SharedPreferences
                SharedPreferences sharedPref = getSharedPreferences("pref", Context.MODE_PRIVATE);
                //if app's SharedPreferences has "value1" as key
                if (sharedPref.contains("value1")){
                    //change default message to value retrieved from SharedPreferences
                    value = sharedPref.getString("value1", "");
                } else {
                    value = "Default Message 1";
                }
                //send ToastMessage to notify user that message is sent
                Toast.makeText(SettingsActivity.this, value, Toast.LENGTH_SHORT).show();
                //use method to send to BluetoothActivity's handler to send out message
                sendToBtAct(value);
            }
        });

        buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //set default message
                String value;
                //retrieve app's SharedPreferences
                SharedPreferences sharedPref = getSharedPreferences("pref", Context.MODE_PRIVATE);
                //if app's SharedPreferences has "value2" as key
                if (sharedPref.contains("value2")){
                    //change default message to value retrieved from SharedPreferences
                    value = sharedPref.getString("value2", "");
                } else {
                    value = "Default Message 2";
                }
                //send ToastMessage to notify user that message is sent
                Toast.makeText(SettingsActivity.this, value, Toast.LENGTH_SHORT).show();
                //use method to send to BluetoothActivity's handler to send out message
                sendToBtAct(value);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                dl.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //to send bluetoothactivity for bluetooth chat
    private void sendToBtAct(String msg) {
        Intent intent = new Intent("BlueToothChatSendIntent");
        // You can also include some extra data.
        intent.putExtra("bluetoothTextSend", msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


}
