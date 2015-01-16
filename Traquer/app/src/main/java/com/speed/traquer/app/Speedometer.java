package com.speed.traquer.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import java.util.List;

public class Speedometer extends Activity implements LocationListener{

    private EasyTracker easyTracker = null;
    Button iButton_feedback;
    Button iButton_rank;
    ImageButton iButton_complain;
    Button iButton_speed;
    View speedPage;
    int ibSpeed_indicate = 0;
    private static final int NOTIFICATION_ID = 0;
    private NotificationManager nNM;
    static boolean check_running_mode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speedometer);

        //give reference to location manager and register our class file
        LocationManager lm = (LocationManager)  this.getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        //initialize, get everything to zero
        this.onLocationChanged(null);

        if ( !lm.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();

        }

        /*/Notification Share Preference
        final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        //test
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                // fix our issues for static variables
                isActive = false;

                // fix our issues for sharedpreferences
                SharedPreferences sp = getSharedPreferences("OURINFO", MODE_PRIVATE);
                SharedPreferences.Editor ed = sp.edit();
                ed.putBoolean("active", false);
                ed.commit();

                // Handle everthing else
                defaultHandler.uncaughtException(thread, throwable);
            }
        });*/

        speedPage = findViewById(R.id.speedPage);
        String fontPath = "fonts/segoeuil.ttf";
        TextView txt = (TextView) findViewById(R.id.text_speed);
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        txt.setTypeface(tf);

        TextView txt2 = (TextView) findViewById(R.id.unit);
        txt2.setTypeface(tf);

        TextView txtBillBoard = (TextView) findViewById(R.id.txtBillBoard);
        txtBillBoard.setTypeface(tf);

        TextView txtRate = (TextView) findViewById(R.id.txtRate);
        txtRate.setTypeface(tf);

        TextView txtAlert= (TextView) findViewById(R.id.txtAlert);
        txtAlert.setTypeface(tf);

        iButton_rank = (Button) findViewById(R.id.ibBillBoard);
        iButton_speed = (Button) findViewById(R.id.ibutton_speed);
        //button code starts here
        addButtonClickListener();

        easyTracker = EasyTracker.getInstance(Speedometer.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SaveSharedPreference.setSpamNotification(Speedometer.this, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SaveSharedPreference.setSpamNotification(Speedometer.this, false);
    }

    private void addButtonClickListener() {
        iButton_feedback = (Button) findViewById(R.id.ibFeedback);
        iButton_complain = (ImageButton) findViewById(R.id.complain);

        iButton_feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                easyTracker.send(MapBuilder.createEvent("Speedometer", "Feedback button pressed", "Speedometer event", null).build());
                startActivity(new Intent(getApplicationContext(),Feedback_rate.class));
            }
        });

        iButton_rank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                easyTracker.send(MapBuilder.createEvent("Speedometer", "billboard button pressed", "Speedometer event", null).build());
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1)
                {
                    Toast.makeText(Speedometer.this, "Ops, your phone version is not compatible to this feature.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    startActivity(new Intent(getApplicationContext(), FragmentRecord.class));
                }
            }
        });

        iButton_complain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),TraqComplaint.class);
                intent.putExtra("Latitude",_latitude);
                intent.putExtra("Longitude",_longitude);
                intent.putExtra("SpeedBusExceed",_highestSpeed);
                startActivity(intent);
            }
        });

        iButton_speed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ibSpeed_indicate == 1){
                    easyTracker.send(MapBuilder.createEvent("Speedometer", "Complaint button pressed (speed exceeded)", "Speedometer event", null).build());
                    Intent intent = new Intent(getApplicationContext(),TraqComplaint.class);
                    intent.putExtra("Latitude",_latitude);
                    intent.putExtra("Longitude",_longitude);
                    intent.putExtra("SpeedBusExceed",_highestSpeed);
                    startActivity(intent);
                }else if(ibSpeed_indicate == 0){
                    easyTracker.send(MapBuilder.createEvent("Speedometer", "Complaint button pressed (speed not exceeded)", "Speedometer event", null).build());
                    Toast.makeText(Speedometer.this, "The bus doesn't exceed speed limit yet.", Toast.LENGTH_SHORT).show();
                }else if(ibSpeed_indicate == -1){
                    easyTracker.send(MapBuilder.createEvent("Speedometer", "Complaint button pressed (No GPS)", "Speedometer event", null).build());
                    Toast.makeText(Speedometer.this, "No GPS Network", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(Speedometer.this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
               .setCancelable(false)
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                       easyTracker.send(MapBuilder.createEvent("Speedometer", "GPS button pressed (Yes)", "Speedometer event", null).build());
                       startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                   }
               })
               .setNegativeButton("No", new DialogInterface.OnClickListener() {
                   public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                       easyTracker.send(MapBuilder.createEvent("Speedometer", "GPS button pressed (No)", "Speedometer event", null).build());
                       dialog.cancel();
                   }
               });
        final AlertDialog alert = builder.create();
        alert.show();
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
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.speedometer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_about_us) {
            Intent intent = new Intent(getApplicationContext(),AboutUs.class);
            startActivity(intent);
        }else if (id == R.id.action_feedback_traq) {
            Intent intent = new Intent(getApplicationContext(),AppFeedback.class);
            startActivity(intent);
        }else if (id == R.id.action_logout) {
            SaveSharedPreference.clearUserName(Speedometer.this);
            Intent intent = new Intent(getApplicationContext(),Login.class);
            startActivity(intent);
        }else if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(),Settings.class);
            startActivity(intent);
        }else if (id == R.id.action_call_support) {
            String url = "tel:1800887723";
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }
    private double _highestSpeed = 0.0;
    private double _longitude = 0.0;
    private double _latitude = 0.0;
    @Override
    public void onLocationChanged(Location location) {
        TextView speed = (TextView) findViewById(R.id.text_speed);
        TextView txtAlert = (TextView) findViewById(R.id.txtAlert);


        if(location == null){
            speed.setText("--.--");
            ibSpeed_indicate = -1;
        }
        else {
            float nCurrentSpeed = (int) ((location.getSpeed()*3600)/1000);

            speed.setText(nCurrentSpeed + "");
            if(nCurrentSpeed > 110){

                iButton_speed.setBackgroundResource(R.drawable.btn_speed_red);
                speedPage.setBackgroundColor(getResources().getColor(R.color.Black));
                txtAlert.setVisibility(View.VISIBLE);
                ibSpeed_indicate = 1;
                if(nCurrentSpeed > _highestSpeed)
                {
                    if(SaveSharedPreference.getNotification(Speedometer.this) == 0) {
                        //receive notification
                        if(!SaveSharedPreference.getSpamNotification(Speedometer.this)) {
                            showNotification();
                        }
                    }

                    _highestSpeed = nCurrentSpeed;
                    if(location != null){
                        _longitude = location.getLongitude();
                        _latitude = location.getLatitude();
                    }
                }
                //send message
                /*String alertMsg = "The bus has exceed speed limit";
                String num = "0129832169";
                sendMsg(alertMsg, num);*/
            }
            else if(nCurrentSpeed >= 0 && nCurrentSpeed < 80){
                iButton_speed.setBackgroundResource(R.drawable.btn_speed_green);
                speedPage.setBackgroundResource(R.drawable.bgroad);
                ibSpeed_indicate = 0;
                txtAlert.setVisibility(View.GONE);

                if(nCurrentSpeed > _highestSpeed)
                {
                    _highestSpeed = nCurrentSpeed;
                    if(location != null){
                        _longitude = location.getLongitude();
                        _latitude = location.getLatitude();
                    }
                }

            }
            else if(nCurrentSpeed >= 80 && nCurrentSpeed <= 110){
                iButton_speed.setBackgroundResource(R.drawable.btn_speed_orange);
                speedPage.setBackgroundResource(R.drawable.bgroad);
                ibSpeed_indicate = 0;
                txtAlert.setVisibility(View.GONE);
            }
        }
    }

    /*protected void sendMsg(String num, String alertMsg) {
        final String SENT = "Alert message sent";

        PendingIntent sentPI = PendingIntent.getBroadcast(Speedometer.this, 0, new Intent(SENT), 0);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()){
                    case  Activity.RESULT_OK:
                        Toast.makeText(Speedometer.this, SENT, Toast.LENGTH_LONG).show();;
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic Failure", Toast.LENGTH_LONG).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        },new IntentFilter(SENT));


        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(num, null, alertMsg, sentPI, null);
    }*/

    private void showNotification() {
        nNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification n = new Notification();
        n.icon = R.drawable.ic_launcher;
        n.tickerText = "The bus has exceed the speed limit!";
        n.when = System.currentTimeMillis();

        CharSequence contentText = "Click on the speed to complaint! Action will be taken immediately";
        CharSequence contentTitle = "Speed exceeded up to " + _highestSpeed + "km/h!";

        Intent notificationIntent = new Intent(this, TraqComplaint.class);
        notificationIntent.putExtra("Latitude",_latitude);
        notificationIntent.putExtra("Longitude",_longitude);
        notificationIntent.putExtra("SpeedBusExceed",_highestSpeed);
        startActivity(notificationIntent);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this).setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(contentTitle)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentTitle))
                .setAutoCancel(true).setContentText(contentText);
        mBuilder.setContentIntent(contentIntent);

        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
        mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);

        mBuilder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;
        nNM.notify(NOTIFICATION_ID, mBuilder.build());


        /*n.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
        n.defaults|= Notification.DEFAULT_SOUND;
        n.defaults|= Notification.DEFAULT_LIGHTS;
        n.defaults|= Notification.DEFAULT_VIBRATE;

        nNM.notify(NOTIFICATION_ID, n);*/


    }

    @Override
    public void onBackPressed() {
        moveTaskToBack (true);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}