package com.speed.traquer.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.pushbots.push.Pushbots;


public class MainActivity extends Activity implements OnClickListener{

    private EasyTracker easyTracker = null;
    Button iButton_speed;
    Button iButton_code;
    private String SENDER_ID = "877176008453";
    private String PUSHBOTS_APPLICATION_ID = "5397a75c1d0ab1d0048b45e9";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Pushbots.init(this, SENDER_ID, PUSHBOTS_APPLICATION_ID);
        setContentView(R.layout.activity_main);

        iButton_speed = (Button) findViewById(R.id.speedometer);
        iButton_code = (Button) findViewById(R.id.traqcode);
        iButton_speed.setOnClickListener(this);
        iButton_code.setOnClickListener(this);

        String fontPath = "fonts/segoeuil.ttf";
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        iButton_speed.setTypeface(tf);
        iButton_code.setTypeface(tf);

        easyTracker = EasyTracker.getInstance(MainActivity.this);

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack (true);
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
        getMenuInflater().inflate(R.menu.main, menu);
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
        }else if (id == R.id.action_logout) {
            SaveSharedPreference.clearUserName(MainActivity.this);
            Intent intent = new Intent(getApplicationContext(),Login.class);
            startActivity(intent);
        }else if (id == R.id.action_call_support) {
            String url = "tel:1800887723";
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            startActivity(intent);

        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.speedometer){
            easyTracker.send(MapBuilder.createEvent("Main", "speedometer button pressed", "Main event", null).build());
            startActivity(new Intent(getApplicationContext(),Speedometer.class));
        }
        else if(view.getId() == R.id.traqcode){
            easyTracker.send(MapBuilder.createEvent("Main", "billboard button pressed", "Main event", null).build());
            startActivity(new Intent(getApplicationContext(), FragmentRecord.class));
        }
    }
}
