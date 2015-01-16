package com.speed.traquer.app;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.speed.traquer.app.R;

public class StartActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        if(SaveSharedPreference.getUserName(StartActivity.this).length() == 0)
        {
            Intent intent = new Intent(getApplicationContext(),Login.class);
            startActivity(intent);
        }
        else
        {
            Intent intent = new Intent(getApplicationContext(),Speedometer.class);
            startActivity(intent);
        }
    }
}
