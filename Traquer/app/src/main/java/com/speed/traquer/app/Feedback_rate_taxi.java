package com.speed.traquer.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.speed.traquer.app.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Feedback_rate_taxi extends ActionBarActivity {

    private EasyTracker easyTracker = null;
    AutoCompleteTextView actv_comp_taxi;
    RadioGroup rgSafety;
    RadioGroup rgDriver;
    RadioGroup rgClean;
    RadioGroup rgComfort;
    RadioGroup rgPunctual;
    RadioGroup rgIntegrity;
    EditText feedback_remarks;
    ImageButton btnImg;
    EditText editCurrDate;
    EditText editCurrTime;
    EditText inputTaxi;
    EditText taxiDriver;
    EditText taxiLic;
    ImageButton rateBtnBus;
    int re1=0,re2=0,re3=0,re4=0,re5=0,re6=0,rg1=0,rg2=0,rg3=0,rg4=0,rg5=0,rg6=0,rb1=0,rb2=0,rb3=0,rb4=0,rb5=0,rb6=0;
    int rGroup1=0,rGroup2=0,rGroup3=0,rGroup4=0,rGroup5=0,rGroup6=0;
    private static final String TAG_SUCCESS = "success";
    JSONParser jsonParser = new JSONParser();
    final Calendar c = Calendar.getInstance();
    private static String url_insert_comment= "http://cyberweb.my/traquer/inserttx_comment.php";
    private final static String taxiUrl = "http://traquer.cyberweb.my/getTaxilike.php?compcode=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_rate_taxi);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        inputTaxi = (EditText) findViewById(R.id.taxi_id);
        taxiDriver = (EditText) findViewById(R.id.taxi_driver);
        taxiLic = (EditText) findViewById(R.id.taxi_license);
        feedback_remarks = (EditText) findViewById(R.id.feedback_remarks);
        rgSafety = (RadioGroup) findViewById(R.id.radioSafety);
        rgDriver = (RadioGroup) findViewById(R.id.radioDriver);
        rgClean = (RadioGroup) findViewById(R.id.radioClean);
        rgComfort = (RadioGroup) findViewById(R.id.radioComfort);
        rgPunctual = (RadioGroup) findViewById(R.id.radioPunctual);
        rgIntegrity = (RadioGroup) findViewById(R.id.radioIntegrity);
        rateBtnBus = (ImageButton) findViewById(R.id.btn_rate_bus);
        editCurrDate = (EditText) findViewById(R.id.editCurrDate);
        editCurrTime= (EditText) findViewById(R.id.editCurrTime);
        ProgressBar barProgress = (ProgressBar) findViewById(R.id.progressLoading);

        rateBtnBus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(),Feedback_rate.class);
                startActivity(intent);
            }
        });

        //Auto Complete Database
        if(isNetworkConnected()) {
            actv_comp_taxi = (AutoCompleteTextView) findViewById(R. id.search_taxi_comp);
            SuggestionAdapter sa =new SuggestionAdapter(this,actv_comp_taxi.getText().toString(), taxiUrl, "compcode");
            sa.setLoadingIndicator(barProgress);
            actv_comp_taxi.setAdapter(sa);
        }else {
            Toast.makeText(getApplicationContext(), "Looks like there's a problem with your network connection.", Toast.LENGTH_SHORT).show();
        }

        actv_comp_taxi = (AutoCompleteTextView) findViewById(R. id.search_taxi_comp);
        SuggestionAdapter sa =new SuggestionAdapter(this,actv_comp_taxi.getText().toString(), taxiUrl, "compcode");
        sa.setLoadingIndicator(barProgress);
        actv_comp_taxi.setAdapter(sa);

        getCurrentDate();
        getCurrentTime();

        String fontPath = "fonts/segoeuil.ttf";
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        actv_comp_taxi.setTypeface(tf);
        actv_comp_taxi.setTypeface(tf);
        feedback_remarks.setTypeface(tf);
        inputTaxi.setTypeface(tf);
        taxiDriver.setTypeface(tf);
        taxiLic.setTypeface(tf);

        TextView txtTaxiDriver = (TextView) findViewById(R.id.txt_taxi_driver);
        txtTaxiDriver.setTypeface(tf);

        TextView txtTaxiLic = (TextView) findViewById(R.id.txt_taxi_license);
        txtTaxiLic.setTypeface(tf);

        TextView txtComp = (TextView) findViewById(R.id.taxi_comp);
        txtComp.setTypeface(tf);

        TextView txtNumber = (TextView) findViewById(R.id.taxi_number);
        txtNumber.setTypeface(tf);

        TextView txtSafety = (TextView) findViewById(R.id.txtSafety);
        txtSafety.setTypeface(tf);

        TextView txtDriver = (TextView) findViewById(R.id.txtDriver);
        txtDriver.setTypeface(tf);

        TextView txtClean = (TextView) findViewById(R.id.txtClean);
        txtClean.setTypeface(tf);

        TextView txtComfort = (TextView) findViewById(R.id.txtComfort);
        txtComfort.setTypeface(tf);

        TextView txtPunctual = (TextView) findViewById(R.id.txtPunctual);
        txtPunctual.setTypeface(tf);

        TextView txtIntegrity = (TextView) findViewById(R.id.txtIntegrity);
        txtIntegrity.setTypeface(tf);

        rgSafety.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton ibGreatSafety = (RadioButton) findViewById(R.id.ibGreatSafety);
                RadioButton ibGoodSafety = (RadioButton) findViewById(R.id.ibGoodSafety);
                RadioButton ibBadSafety = (RadioButton) findViewById(R.id.ibBadSafety);
                if(ibGreatSafety.isChecked()){
                    re1 = 1;
                    rGroup1 = 1;
                    //Toast.makeText(Feedback_rate.this, "Awesome Safety",Toast.LENGTH_SHORT).show();
                } else if (ibGoodSafety.isChecked()){
                    rg1 = 1;
                    rGroup1 = 1;
                    //Toast.makeText(Feedback_rate.this, "Good Safety",Toast.LENGTH_SHORT).show();
                } else if (ibBadSafety.isChecked()){
                    rb1 = 1;
                    rGroup1 = 1;
                    //Toast.makeText(Feedback_rate.this, "Bad Safety",Toast.LENGTH_SHORT).show();
                }
            }
        });
        rgDriver.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton ibGreatDriver = (RadioButton) findViewById(R.id.ibGreatDriver);
                RadioButton ibGoodDriver = (RadioButton) findViewById(R.id.ibGoodDriver);
                RadioButton ibBadDriver = (RadioButton) findViewById(R.id.ibBadDriver);
                if(ibGreatDriver.isChecked()){
                    re2 = 1;
                    rGroup2 = 1;
                    //Toast.makeText(Feedback_rate.this, "Awesome Driver",Toast.LENGTH_SHORT).show();
                } else if (ibGoodDriver.isChecked()){
                    rg2 = 1;
                    rGroup2 = 1;
                    //Toast.makeText(Feedback_rate.this, "Good Driver",Toast.LENGTH_SHORT).show();
                } else if (ibBadDriver.isChecked()){
                    rb2 = 1;
                    rGroup2 = 1;
                    //Toast.makeText(Feedback_rate.this, "Bad Driver",Toast.LENGTH_SHORT).show();
                }
            }
        });
        rgClean.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton ibGreatClean = (RadioButton) findViewById(R.id.ibGreatClean);
                RadioButton ibGoodClean = (RadioButton) findViewById(R.id.ibGoodClean);
                RadioButton ibBadClean = (RadioButton) findViewById(R.id.ibBadClean);
                if(ibGreatClean.isChecked()){
                    re3 = 1;
                    rGroup3 = 1;
                    //Toast.makeText(Feedback_rate.this, "Awesome Cleanliness",Toast.LENGTH_SHORT).show();
                } else if (ibGoodClean.isChecked()){
                    rg3 = 1;
                    rGroup3 = 1;
                    //Toast.makeText(Feedback_rate.this, "Good Cleanliness",Toast.LENGTH_SHORT).show();
                } else if (ibBadClean.isChecked()){
                    rb3 = 1;
                    rGroup3 = 1;
                    //Toast.makeText(Feedback_rate.this, "Bad Cleanliness",Toast.LENGTH_SHORT).show();
                }
            }
        });
        rgComfort.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton ibGreatComfort = (RadioButton) findViewById(R.id.ibGreatComfort);
                RadioButton ibGoodComfort = (RadioButton) findViewById(R.id.ibGoodComfort);
                RadioButton ibBadComfort = (RadioButton) findViewById(R.id.ibBadComfort);
                if(ibGreatComfort.isChecked()){
                    re4 = 1;
                    rGroup4 = 1;
                    //Toast.makeText(Feedback_rate.this, "Super Comfort",Toast.LENGTH_SHORT).show();
                } else if (ibGoodComfort.isChecked()){
                    rg4 = 1;
                    rGroup4 = 1;
                    //Toast.makeText(Feedback_rate.this, "Comfort",Toast.LENGTH_SHORT).show();
                } else if (ibBadComfort.isChecked()){
                    rb4 = 1;
                    rGroup4 = 1;
                    //Toast.makeText(Feedback_rate.this, "Not Comfort at all",Toast.LENGTH_SHORT).show();
                }
            }
        });
        rgPunctual.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton ibGreatPunctual = (RadioButton) findViewById(R.id.ibGreatPunctual);
                RadioButton ibGoodPunctual = (RadioButton) findViewById(R.id.ibGoodPunctual);
                RadioButton ibBadPunctual = (RadioButton) findViewById(R.id.ibBadPunctual);
                if(ibGreatPunctual.isChecked()){
                    re5 = 1;
                    rGroup5 = 1;
                    //Toast.makeText(Feedback_rate.this, "Very Punctual",Toast.LENGTH_SHORT).show();
                } else if (ibGoodPunctual.isChecked()){
                    rg5 = 1;
                    rGroup5 = 1;
                    //Toast.makeText(Feedback_rate.this, "Delay Abit",Toast.LENGTH_SHORT).show();
                } else if (ibBadPunctual.isChecked()){
                    rb5 = 1;
                    rGroup5 = 1;
                    //Toast.makeText(Feedback_rate.this, "Not Punctual at all",Toast.LENGTH_SHORT).show();
                }
            }
        });

        rgIntegrity.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton ibGreatDriver = (RadioButton) findViewById(R.id.ibGreatIntegrity);
                RadioButton ibGoodDriver = (RadioButton) findViewById(R.id.ibGoodIntegrity);
                RadioButton ibBadDriver = (RadioButton) findViewById(R.id.ibBadIntegrity);
                if(ibGreatDriver.isChecked()){
                    re6 = 1;
                    rGroup6 = 1;
                    //Toast.makeText(Feedback_rate.this, "Awesome Driver",Toast.LENGTH_SHORT).show();
                } else if (ibGoodDriver.isChecked()){
                    rg6 = 1;
                    rGroup6 = 1;
                    //Toast.makeText(Feedback_rate.this, "Good Driver",Toast.LENGTH_SHORT).show();
                } else if (ibBadDriver.isChecked()){
                    rb6 = 1;
                    rGroup6 = 1;
                    //Toast.makeText(Feedback_rate.this, "Bad Driver",Toast.LENGTH_SHORT).show();
                }
            }
        });


        easyTracker = EasyTracker.getInstance(Feedback_rate_taxi.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.feedback_rate_taxi, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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

    class InsertComment extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            String taxi_id = inputTaxi.getText().toString().toUpperCase();
            taxi_id = taxi_id.replace(" ", "");
            String tcmmt = feedback_remarks.getText().toString();
            String taxi_comp = actv_comp_taxi.getText().toString();
            String taxi_lic = taxiLic.getText().toString();
            String taxi_driver = taxiDriver.getText().toString();
            String user_name = SaveSharedPreference.getUserName(Feedback_rate_taxi.this);
            String rate_re1 = Integer.toString(re1);
            String rate_re2 = Integer.toString(re2);
            String rate_re3 = Integer.toString(re3);
            String rate_re4 = Integer.toString(re4);
            String rate_re5 = Integer.toString(re5);
            String rate_re6 = Integer.toString(re6);
            String rate_rg1 = Integer.toString(rg1);
            String rate_rg2 = Integer.toString(rg2);
            String rate_rg3 = Integer.toString(rg3);
            String rate_rg4 = Integer.toString(rg4);
            String rate_rg5 = Integer.toString(rg5);
            String rate_rg6 = Integer.toString(rg6);
            String rate_rb1 = Integer.toString(rb1);
            String rate_rb2 = Integer.toString(rb2);
            String rate_rb3 = Integer.toString(rb3);
            String rate_rb4 = Integer.toString(rb4);
            String rate_rb5 = Integer.toString(rb5);
            String rate_rb6 = Integer.toString(rb6);
            String rdate = editCurrDate.getText().toString();
            String rtime = editCurrTime.getText().toString();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("taxi_id", taxi_id));
            params.add(new BasicNameValuePair("tcmmt", tcmmt));
            params.add(new BasicNameValuePair("taxi_comp", taxi_comp));
            params.add(new BasicNameValuePair("tdriver", taxi_driver));
            params.add(new BasicNameValuePair("tlicense", taxi_lic));
            params.add(new BasicNameValuePair("username", user_name));
            params.add(new BasicNameValuePair("re1", rate_re1));
            params.add(new BasicNameValuePair("re2", rate_re2));
            params.add(new BasicNameValuePair("re3", rate_re3));
            params.add(new BasicNameValuePair("re4", rate_re4));
            params.add(new BasicNameValuePair("re5", rate_re5));
            params.add(new BasicNameValuePair("re6", rate_re6));
            params.add(new BasicNameValuePair("rg1", rate_rg1));
            params.add(new BasicNameValuePair("rg2", rate_rg2));
            params.add(new BasicNameValuePair("rg3", rate_rg3));
            params.add(new BasicNameValuePair("rg4", rate_rg4));
            params.add(new BasicNameValuePair("rg5", rate_rg5));
            params.add(new BasicNameValuePair("rg6", rate_rg6));
            params.add(new BasicNameValuePair("rb1", rate_rb1));
            params.add(new BasicNameValuePair("rb2", rate_rb2));
            params.add(new BasicNameValuePair("rb3", rate_rb3));
            params.add(new BasicNameValuePair("rb4", rate_rb4));
            params.add(new BasicNameValuePair("rb5", rate_rb5));
            params.add(new BasicNameValuePair("rb6", rate_rb6));
            params.add(new BasicNameValuePair("rdate", rdate));
            params.add(new BasicNameValuePair("rtime", rtime));
            JSONObject json = jsonParser.makeHttpRequest(url_insert_comment,
                    "POST", params);
            Log.d("Create Response", json.toString());
            try {
                int success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    // successfully created product
                    Intent i = new Intent(getApplicationContext(), Speedometer.class);
                    startActivity(i);
                    //Toast.makeText(TraqComplaint.this, "Complaint sent. Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                    // closing this screen
                    finish();
                } else {
                    Toast.makeText(Feedback_rate_taxi.this, "Failed to send. Please check your network connection.", Toast.LENGTH_SHORT).show();
                    // failed to create product
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_send) {
            if(isNetworkConnected()) {
                if(inputTaxi.length() == 0){
                    Toast.makeText(Feedback_rate_taxi.this, "Vehicle Number is required!", Toast.LENGTH_SHORT).show();
                }else if(inputTaxi.length() < 3){
                    Toast.makeText(Feedback_rate_taxi.this, "Invalid Vehicle Number.", Toast.LENGTH_SHORT).show();
                }else if(rGroup1 == 0 || rGroup2 == 0 || rGroup3 == 0 || rGroup4 == 0 || rGroup5 == 0 || rGroup6 == 0){
                    Toast.makeText(Feedback_rate_taxi.this, "Please Rate for the Bus Company.", Toast.LENGTH_SHORT).show();
                }else {
                    easyTracker.send(MapBuilder.createEvent("Feedback", "Send button pressed", "Feedback event", null).build());
                    Toast.makeText(Feedback_rate_taxi.this, "Rate Successful. Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                    new InsertComment().execute();
                }
            }else {
                Toast.makeText(Feedback_rate_taxi.this, "Failed to send. Please check your network connection.", Toast.LENGTH_SHORT).show();
            }
        }else if (id == android.R.id.home){
            easyTracker.send(MapBuilder.createEvent("Feedback_Taxi", "back button pressed", "Feedback event", null).build());
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getCurrentDate() {
        String dateFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat( dateFormat, Locale.ENGLISH );
        editCurrDate.setText( sdf.format( c.getTime() ) );
    }
    public void getCurrentTime() {
        String timeFormat = "hh:mm a";
        SimpleDateFormat stf = new SimpleDateFormat( timeFormat, Locale.ENGLISH );
        editCurrTime.setText( stf.format( c.getTime() ) );
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(actv_comp_taxi.length() != 0 || inputTaxi.length() != 0){
                final AlertDialog.Builder alertBox = new AlertDialog.Builder(Feedback_rate_taxi.this);
                alertBox.setIcon(R.drawable.info_icon);
                alertBox.setCancelable(false);
                alertBox.setTitle("Do you want to cancel feedback?");
                alertBox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        // finish used for destroyed activity
                        easyTracker.send(MapBuilder.createEvent("Feedback taxi", "Cancel Feedback taxi (Yes)", "Feedback taxi event", null).build());
                        finish();
                        Intent intent = new Intent(Feedback_rate_taxi.this, Speedometer.class);
                        Feedback_rate_taxi.this.startActivity(intent);
                    }
                });

                alertBox.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        easyTracker.send(MapBuilder.createEvent("Feedback taxi", "Cancel Feedback taxi (No)", "Feedback taxi event", null).build());
                        dialog.cancel();
                    }
                });

                alertBox.show();
            }else{
                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
