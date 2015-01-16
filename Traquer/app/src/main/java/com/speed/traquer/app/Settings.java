package com.speed.traquer.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.speed.traquer.app.R;

public class Settings extends ActionBarActivity {

    private EasyTracker easyTracker = null;
    ListView listSetting;
    ImageView imgOnOff;
    int onOff;
    String[] values = new String[] { "Notification Service","Account"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        easyTracker = EasyTracker.getInstance(Settings.this);
        imgOnOff = (ImageView) findViewById(R.id.imgOnOff);

        onOff = SaveSharedPreference.getNotification(Settings.this);

        if(onOff == 0){
            imgOnOff.setBackgroundResource(R.drawable.on);
        }else{
            imgOnOff.setBackgroundResource(R.drawable.off);
        }
        listSetting = (ListView) findViewById(R.id.listSetting);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
        listSetting.setAdapter(adapter);

        listSetting.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, final long id) {

                if(position == 0 && id == 0){
                    if (onOff == 0) {
                        final AlertDialog.Builder alertBox = new AlertDialog.Builder(Settings.this);
                        alertBox.setCancelable(true);
                        alertBox.setTitle("You are about to turn off alert notification system");
                        alertBox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                // finish used for destroyed activity
                                easyTracker.send(MapBuilder.createEvent("Settings", "Cancel Settings (Yes)", "Settings event", null).build());
                                imgOnOff.setBackgroundResource(R.drawable.off);
                                SaveSharedPreference.setNotification(Settings.this, 1);
                                onOff = 1;
                            }
                        });

                        alertBox.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int arg1) {
                                easyTracker.send(MapBuilder.createEvent("Settings", "Cancel Settings (No)", "Settings event", null).build());
                                dialog.cancel();
                            }
                        });

                        alertBox.show();
                    } else {
                        onOff = 0;
                        imgOnOff.setBackgroundResource(R.drawable.on);
                        SaveSharedPreference.setNotification(Settings.this, 0);
                    }
                }else if(position == 1 && id == 1){
                    Intent intent = new Intent(getApplicationContext(),Account.class);
                    startActivity(intent);
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home){
            easyTracker.send(MapBuilder.createEvent("Settings", "back button pressed", "Settings event", null).build());
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart(){
        super.onStart();

        EasyTracker.getInstance(this).activityStart(this);

    }

    @Override
    protected void onStop(){
        super.onStop();

        EasyTracker.getInstance(this).activityStop(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            easyTracker.send(MapBuilder.createEvent("Settings", "back button pressed", "Settings event", null).build());
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
