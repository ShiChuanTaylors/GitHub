package com.speed.traquer.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

//Shi Chuan's Code
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;


import com.facebook.*;
import com.facebook.android.Facebook;
import com.facebook.model.*;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

public class TraqComplaint extends ActionBarActivity {

    private EasyTracker easyTracker = null;
    private UiLifecycleHelper uiHelper;

    //Facebook Login
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
            @Override
            public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
                Log.e("Activity", String.format("Error: %s", error.toString()));
            }

            @Override
            public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
                Log.i("Activity", "Success!");
            }
        });
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);


        //Run when submit button clicked and permission updated
        if(isSubmitButtonClicked)
            onSessionStateChange(Session.getActiveSession().getState());
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
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
        SaveSharedPreference.setSpamNotification(TraqComplaint.this, true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
        SaveSharedPreference.setSpamNotification(TraqComplaint.this, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    EditText inputBus;

    AutoCompleteTextView actv_comp;
    AutoCompleteTextView actv_from;
    AutoCompleteTextView actv_to;

    ImageButton rateBtnTaxi;
    TextView geoLat;
    TextView geoLong;
    EditText editDate;
    EditText editTime;
    EditText editCurrTime;
    Button complainSend;
    JSONParser jsonParser = new JSONParser();

    final Calendar c = Calendar.getInstance();

    boolean updateTwitter = false;
    boolean updateFacebook = false;
    String[] comp, location;
    private final static int ALERT_DIALOG = 1;
    private static final String TAG_SUCCESS = "success";
    private static String url_insert_form= "http://cyberweb.my/traquer/insert_form.php";
    private static String url_insert_formmedia= "http://cyberweb.my/traquer/insert_formmedia.php";
    private final static String locUrl = "http://traquer.cyberweb.my/getLoclike.php?loc=";
    private final static String busUrl = "http://traquer.cyberweb.my/getBuslike.php?compcode=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traq_complaint);
        easyTracker = EasyTracker.getInstance(TraqComplaint.this);
        //onCreateView(savedInstanceState);
        //InitialSetupUI();

        //Added UIHelper
        uiHelper = new UiLifecycleHelper(this, null);
        uiHelper.onCreate(savedInstanceState);

        inputBus = (EditText) findViewById(R.id.bus_id);
        geoLat = (TextView) findViewById(R.id.geoLat);
        geoLong= (TextView) findViewById(R.id.geoLong);
        editDate = (EditText) findViewById(R.id.editDate);
        editTime = (EditText) findViewById(R.id.editTime);
        editCurrTime = (EditText) findViewById(R.id.editCurrTime);
        complainSend = (Button) findViewById(R.id.complain_send);

        ProgressBar barProgress = (ProgressBar) findViewById(R.id.progressLoading);
        ProgressBar barProgressFrom = (ProgressBar) findViewById(R.id.progressLoadingFrom);
        ProgressBar barProgressTo = (ProgressBar) findViewById(R.id.progressLoadingTo);

        actv_comp = (AutoCompleteTextView) findViewById(R. id.search_comp);
        SuggestionAdapter sa =new SuggestionAdapter(this,actv_comp.getText().toString(), busUrl, "compcode");
        sa.setLoadingIndicator(barProgress);
        actv_comp.setAdapter(sa);

        actv_from = (AutoCompleteTextView) findViewById(R. id.search_from);
        SuggestionAdapter saFrom =new SuggestionAdapter(this,actv_from.getText().toString(), locUrl, "location");
        saFrom.setLoadingIndicator(barProgressFrom);
        actv_from.setAdapter(saFrom);

        actv_to = (AutoCompleteTextView) findViewById(R. id.search_to);
        SuggestionAdapter saTo =new SuggestionAdapter(this,actv_to.getText().toString(), locUrl, "location");
        saTo.setLoadingIndicator(barProgressTo);
        actv_to.setAdapter(saTo);
        /*/Auto Complete Database
        if(isNetworkConnected()) {
            new getBusComp().execute(new ApiConnector());
        }*/

        //Setting Fonts
        String fontPath = "fonts/segoeuil.ttf";
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        complainSend.setTypeface(tf);
        inputBus.setTypeface(tf);
        editDate.setTypeface(tf);
        editTime.setTypeface(tf);
        actv_comp.setTypeface(tf);
        actv_to.setTypeface(tf);
        actv_from.setTypeface(tf);

        TextView txtComp = (TextView) findViewById(R.id.bus_comp);
        txtComp.setTypeface(tf);

        TextView txtNumber = (TextView) findViewById(R.id.bus_number);
        txtNumber.setTypeface(tf);

        TextView txtTo = (TextView) findViewById(R.id.to);
        txtTo.setTypeface(tf);

        TextView txtDate = (TextView) findViewById(R.id.date);
        txtDate.setTypeface(tf);

        TextView txtTime = (TextView) findViewById(R.id.time);
        txtTime.setTypeface(tf);

        gLongitude = this.getIntent().getExtras().getDouble("Longitude");
        gLatitude = this.getIntent().getExtras().getDouble("Latitude");
        speedBusExceed = this.getIntent().getExtras().getDouble("SpeedBusExceed");


        isTwitterSelected = false;
        isFacebookSelected = false;
        isDefaultSelected = false;
        isSmsSelected = false;

        geoLat.setText(Double.toString(gLatitude));
        geoLong.setText(Double.toString(gLongitude));

        rateBtnTaxi = (ImageButton) findViewById(R.id.btn_rate_taxi);

        rateBtnTaxi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                if(actv_comp.length() != 0) {
                    final AlertDialog.Builder alertBox = new AlertDialog.Builder(TraqComplaint.this);
                    alertBox.setIcon(R.drawable.info_icon);
                    alertBox.setCancelable(false);
                    alertBox.setTitle("Do you want to cancel complaint?");
                    alertBox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            // finish used for destroyed activity
                            easyTracker.send(MapBuilder.createEvent("Complaint", "Cancel Complaint (Yes)", "Complaint event", null).build());
                            Intent intent = new Intent(getApplicationContext(),TraqComplaintTaxi.class);
                            intent.putExtra("Latitude",gLatitude);
                            intent.putExtra("Longitude",gLongitude);
                            intent.putExtra("SpeedBusExceed",speedBusExceed);
                            startActivity(intent);
                        }
                    });

                    alertBox.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg1) {
                            easyTracker.send(MapBuilder.createEvent("Complaint", "Cancel Complaint (No)", "Complaint event", null).build());
                            dialog.cancel();
                        }
                    });

                    alertBox.show();
                }else {
                    Intent intent = new Intent(getApplicationContext(),TraqComplaintTaxi.class);
                    intent.putExtra("Latitude",gLatitude);
                    intent.putExtra("Longitude",gLongitude);
                    intent.putExtra("SpeedBusExceed",speedBusExceed);
                    startActivity(intent);
                }
            }
        });


        complainSend.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                String bus_id = inputBus.getText().toString().toUpperCase();
                String bus_comp = actv_comp.getText().toString();

                if(bus_comp.length() == 0){
                    Toast.makeText(TraqComplaint.this, "Bus Company is required!", Toast.LENGTH_SHORT).show();
                }else if(bus_comp.length() < 2){
                    Toast.makeText(TraqComplaint.this, "Invalid Bus Company.", Toast.LENGTH_SHORT).show();
                }else if(bus_id.length() == 0){
                    Toast.makeText(TraqComplaint.this, "Bus Plate Number is required!", Toast.LENGTH_SHORT).show();
                }else if(bus_id.length() < 7){
                    Toast.makeText(TraqComplaint.this, "Invalid Bus Number.", Toast.LENGTH_SHORT).show();
                }else{
                    easyTracker.send(MapBuilder.createEvent("Complaint", "Dialog Prompt", "Complaint event", null).build());
                    PromptCustomDialog();
                }
            }
        });
        setCurrentDateOnView();
        getCurrentTime();

        //Shi Chuan's Code

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        cd = new ConnectionDetector(getApplicationContext());

        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            alert.showAlertDialog(TraqComplaint.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }

        // Check if twitter keys are set
        if(TWITTER_CONSUMER_KEY.trim().length() == 0 || TWITTER_CONSUMER_SECRET.trim().length() == 0){
            // Internet Connection is not present
            alert.showAlertDialog(TraqComplaint.this, "Twitter oAuth tokens", "Please set your twitter oauth tokens first!", false);
            // stop executing code by return
            return;
        }

        // Shared Preferences
        mSharedPreferences =  getApplicationContext().getSharedPreferences(
                "MyPref", 0);

        /** This if conditions is tested once is
         * redirected from twitter page. Parse the uri to get oAuth
         * Verifier
         * */


        if (!isTwitterLoggedInAlready()) {
            Uri uri = getIntent().getData();
            if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)) {
                // oAuth verifier
                String verifier = uri
                        .getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);

                try {
                    // Get the access token
                    AccessToken accessToken = twitter.getOAuthAccessToken(
                            requestToken, verifier);

                    // Shared Preferences
                    SharedPreferences.Editor e = mSharedPreferences.edit();

                    // After getting access token, access token secret
                    // store them in application preferences
                    e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
                    e.putString(PREF_KEY_OAUTH_SECRET,
                            accessToken.getTokenSecret());
                    // Store login status - true
                    e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
                    e.commit(); // save changes

                    Log.e("Twitter OAuth Token", "> " + accessToken.getToken());


                    // Getting user details from twitter
                    // For now i am getting his name only
                    long userID = accessToken.getUserId();
                    User user = twitter.showUser(userID);

                    String username = user.getName();
                    String description = user.getDescription();

                    // Displaying in xml ui
                    //lblUserName.setText(Html.fromHtml("<b>Welcome " + username + "</b>" + description));
                } catch (Exception e) {
                    // Check log for login errors
                    Log.e("Twitter Login Error", "> " + e.getMessage());
                }
            }
        }

        //LocationManager lm = (LocationManager)  this.getSystemService(Context.LOCATION_SERVICE);
        //LocationListener ll = new passengerLocationListener();
        //lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);

    }

    String bus_id;
    String bus_comp;
    String loc_frm;
    String loc_to;
    String dateBus;
    String timeBus;
    String curr_time;
    String user_name;
    double gLongitude;
    double gLatitude;
    double speedBusExceed;
    int latestId;

    //INSERT Form to database
    class InsertForm extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {


//Shi CHuan's Code
            /* Check Twitter Login */
            if(isTwitterSelected)
            {
                checkTwitterID();
            }

//END Shi Chuan Code


            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("bus_id", bus_id));
            params.add(new BasicNameValuePair("bus_comp", bus_comp));
            params.add(new BasicNameValuePair("tusrname", user_name));
            params.add(new BasicNameValuePair("loc_frm", loc_frm));
            params.add(new BasicNameValuePair("loc_to", loc_to));
            params.add(new BasicNameValuePair("date", dateBus));
            params.add(new BasicNameValuePair("time", timeBus));
            params.add(new BasicNameValuePair("curr_time", curr_time));
            params.add(new BasicNameValuePair("spdgps", Double.toString(speedBusExceed)));
            params.add(new BasicNameValuePair("LTT", Double.toString(gLatitude) + "°N"));
            params.add(new BasicNameValuePair("LGT", Double.toString(gLongitude) + "°E"));
            if(Long.toString(twitterID) != null)
                params.add(new BasicNameValuePair("tw_id", Long.toString(twitterID)));
            if(fbID != null)
                params.add(new BasicNameValuePair("fb_id", fbID));
            if(Long.toString(twPostId) != null)
                params.add(new BasicNameValuePair("tw_pid", Long.toString(twPostId)));
            if(fbPostId != null)
                params.add(new BasicNameValuePair("fb_pid", fbPostId));

            JSONObject json = jsonParser.makeHttpRequest(url_insert_form,
                    "POST", params);


            Log.d("Create Response", json.toString());

            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {

//                    JSONObject jsonGet = jsonParser.makeHttpRequest(url_insert_form,
  //                          "GET", params);

                    latestId = Integer.parseInt(json.getString("lastid"));

                    if(isSmsSelected)
                    {
                        Log.i("Clicks", "You clicked sent.");

                        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                        sendIntent.putExtra("address", "15888");
                        sendIntent.putExtra("sms_body", "SPAD Aduan " + bus_id + ", " + bus_comp + " bus is speeding with " + speedBusExceed + "km/h at " + Double.toString(gLatitude) + "°N, "+ Double.toString(gLongitude) + "°E, " + curr_time + " - Traquer");
                        sendIntent.setType("vnd.android-dir/mms-sms");
                        startActivity(sendIntent);

                        //1800-88-7723
                    }
                    else
                    {
                        // successfully created product
                        Intent i;
                        i = new Intent(getApplicationContext(), Speedometer.class);
                        startActivity(i);
                        // closing this screen
                        finish();
                    }
                } else {
                    //Toast.makeText(TraqComplaint.this, "Failed to send. Please check your network connection.", Toast.LENGTH_SHORT).show();
                    // failed to create product
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

/*
    private void runUpdateMedias() {
        //Combine Strings for Twitter Status

        String status = bus_id + ", " + bus_comp + " bus is speeding with " + speedBusExceed + "km/h at " + Double.toString(gLatitude) + "°N, "+ Double.toString(gLongitude) + "°E, " + curr_time + " @MyTraquer #Traquer";

        finalStatus = status;


        if(isFacebokSelected)
        {
            //share to facebook
            ShareToFacebook(status);


            //publishFeedDialog();
        }


        if(isTwitterSelected)
        {

            //Toast.makeText(TraqComplaint.this, Long.toString(twitterID) + userName, Toast.LENGTH_SHORT).show();

            // Check for blank text
            if (status.trim().length() > 0) {
                // update status
                //if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1)
                //{
                    new updateTwitterStatus().execute(status);
                /*}
                else
                {
                    new updateTwitterStatus().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, status);
                }

            } else {
                // EditText is empty
                Toast.makeText(getApplicationContext(),
                        "Please enter status message", Toast.LENGTH_SHORT)
                        .show();
            }

        }

    }*/

 /*   //UPDATE Social IDs to Database
    private class UpdateForm extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {



            List<NameValuePair> params = new ArrayList<NameValuePair>();
            if(latestId != 0)
                params.add(new BasicNameValuePair("last_id", Integer.toString(latestId)));

            //Update Facebook IDs
            if(updateFacebook)
            {
                if(fbID != null)
                    params.add(new BasicNameValuePair("fb_id", fbID));

                if(fbPostId != null)
                    params.add(new BasicNameValuePair("fb_pid", fbPostId));
                isFacebokSelected = false;
            }

            //Update Facebook IDs
            if(updateTwitter)
            {
                 /* Check Twitter Login
                if(isTwitterSelected)
                {
                    checkTwitterID();
                }

                if(Long.toString(twitterID) != null)
                    params.add(new BasicNameValuePair("tw_id", Long.toString(twitterID)));

                if(Long.toString(twPostId) != null)
                    params.add(new BasicNameValuePair("tw_pid", Long.toString(twPostId)));

                isTwitterSelected = false;
            }

            //Toast.makeText(TraqComplaint.this, "Updating Twitter. . .", Toast.LENGTH_SHORT).show();

            JSONObject json = jsonParser.makeHttpRequest(url_insert_formmedia,
                    "POST", params);



            Log.d("Create Response", json.toString());

            try {
                int success = json.getInt(TAG_SUCCESS);

                // successfully created product
                if (success == 1) {



                    //Toast.makeText(TraqComplaint.this, "Complaint sent. Thank you for your feedback!", Toast.LENGTH_SHORT).show();

                } else {
                    //Toast.makeText(TraqComplaint.this, "Failed to send. Please check your network connection.", Toast.LENGTH_SHORT).show();
                    // failed to create product
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
        protected void onPostExecute(String file_url) {
            //If all updated
            /*
            Toast.makeText(TraqComplaint.this, "Update Completed", Toast.LENGTH_SHORT).show();
            new InsertForm().execute();

            if(!isTwitterSelected && !isFacebokSelected)
            {
                Intent i;
                i = new Intent(getApplicationContext(), Speedometer.class);
                startActivity(i);

                // closing this screen
                finish();
            }
        }
    }*/
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch (id) {
            case ALERT_DIALOG:
                builder.setMessage("Are you sure you want to send complaint? Action will be taken!")
                        .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if(isNetworkConnected() == true) {
                                    Toast.makeText(TraqComplaint.this, "Complaint sent. Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                                    new InsertForm().execute();
                                }else {
                                    Toast.makeText(TraqComplaint.this, "Failed to send. Please check your network connection.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                Toast.makeText(TraqComplaint.this, "Failed to send.", Toast.LENGTH_SHORT).show();
                                dialog.cancel();
                            }
                        })
                        .setCancelable(false);
                dialog = builder.create();
                break;
            default:
        }
        return dialog;

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


    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener(){
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth){
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, monthOfYear);
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            setCurrentDateOnView();
        }
    };

    TimePickerDialog.OnTimeSetListener time = new TimePickerDialog.OnTimeSetListener(){

        @Override
        public void onTimeSet(TimePicker view, int hour, int minute) {
            c.set(Calendar.HOUR_OF_DAY, hour);
            c.set(Calendar.MINUTE, minute);
            setCurrentDateOnView();
        }
    };

    public void dateOnClick(View view){
        new DatePickerDialog(TraqComplaint.this, date, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    public void timeOnClick(View view){
        new TimePickerDialog(TraqComplaint.this, time, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE),false).show();
    }

    public void setCurrentDateOnView() {
        String dateFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat( dateFormat, Locale.ENGLISH );
        editDate.setText( sdf.format( c.getTime() ) );

        String timeFormat = "hh:mm a";
        SimpleDateFormat stf = new SimpleDateFormat( timeFormat, Locale.ENGLISH );
        editTime.setText( stf.format( c.getTime() ) );
    }

    public void getCurrentTime() {
        String timeFormat = "hh:mm a";
        SimpleDateFormat stf = new SimpleDateFormat( timeFormat, Locale.ENGLISH );
        editCurrTime.setText( stf.format( c.getTime() ) );

    }



    //Shi Chuan's Code

    /**
     * Tutorial Reference
     * http://www.androidhive.info/2012/09/android-twitter-oauth-connect-tutorial/
     **/

    // Constants
    /**
     * Register your here app https://dev.twitter.com/apps/new and get your
     * consumer key and secret
     * */
    static String TWITTER_CONSUMER_KEY = "NvEj3FcVlO9yMpC4B9iKNpEzn";
    static String TWITTER_CONSUMER_SECRET = "OqxMp9Jx0dJ9ZR2StozFk58MAfyGc5kRY3nqemC6XU4bAJFGPY";

    // Preference Constants
    static String PREFERENCE_NAME = "twitter_oauth";
    static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";

    static final String TWITTER_CALLBACK_URL = "oauth://traquer";

    // Twitter oauth urls
    static final String URL_TWITTER_AUTH = "auth_url";
    static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
    static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";

    //UI
    ImageButton useTwitter;

    // Progress dialog
    ProgressDialog pDialog;

    // Twitter
    private static Twitter twitter;
    private static RequestToken requestToken;

    // Shared Preferences
    private static SharedPreferences mSharedPreferences;

    // Internet Connection detector
    private ConnectionDetector cd;


    // Alert Dialog Manager
    AlertDialogManager alert = new AlertDialogManager();

    /**
     * Function to login twitter
     * */


    private void loginToTwitter() {


        // Check if already logged in
        if (!isTwitterLoggedInAlready()) {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
            builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
            Configuration configuration = builder.build();

            TwitterFactory factory = new TwitterFactory(configuration);
            twitter = factory.getInstance();

            try {
                requestToken = twitter
                        .getOAuthRequestToken(TWITTER_CALLBACK_URL);
                this.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                        .parse(requestToken.getAuthenticationURL())));
            } catch (TwitterException e) {
                e.printStackTrace();
            }
        } else {
            // user already logged into twitter
            Toast.makeText(TraqComplaint.this,
                    "Already Logged into twitter", Toast.LENGTH_SHORT).show();


            /* Get Access Token after login*/
            /*try {
                ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
                builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);

                // Access Token
                String access_token = mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
                // Access Token Secret
                String access_token_secret = mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");

                AccessToken accessToken = new AccessToken(access_token, access_token_secret);
                Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);


                // Getting user details from twitter
                // For now i am getting his name only
                twitterID= accessToken.getUserId();
                User user = twitter.showUser(twitterID);

                userName = user.getName();

                // Displaying in xml ui
                //lblUserName.setText(Html.fromHtml("<b>Welcome " + username + "</b>" + description));


            } catch (TwitterException e) {
                // Error in updating status
                Log.d("Twitter Update Error", e.getMessage());
            }*/

        }
    }

    /**
     * Check user already logged in your application using twitter Login flag is
     * fetched from Shared Preferences
     * */
    private boolean isTwitterLoggedInAlready() {
        // return twitter login status from Shared Preferences
        return mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
    }

    /**
     * Function to update status
     * */

    long twPostId = 0;
    class updateTwitterStatus extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            /*super.onPreExecute();
            pDialog = new ProgressDialog(TraqComplaint.this);
            pDialog.setMessage("Updating to twitter...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();*/
        }

        /**
         * getting Places JSON
         * */
        protected String doInBackground(String... args) {
            Log.d("Tweet Text", "> " + args[0]);
            String status = args[0];
            try {
                ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
                builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);

                // Access Token
                String access_token = mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
                // Access Token Secret
                String access_token_secret = mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");

                AccessToken accessToken = new AccessToken(access_token, access_token_secret);
                Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

                // Update status
                twitter4j.Status response = twitter.updateStatus(status);

                Log.d("Status", "> " + response.getText());
                twPostId = response.getId();
            } catch (TwitterException e) {
                // Error in updating status
                Log.d("Twitter Update Error", e.getMessage());
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog and show
         * the data in UI Always use runOnUiThread(new Runnable()) to update UI
         * from background thread, otherwise you will get error
         * **/
        protected void onPostExecute(String file_url) {

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1)
                new InsertForm().execute();
            else
                new InsertForm().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


           /* // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),
                            "Status tweeted successfully", Toast.LENGTH_SHORT)
                            .show();
                    // Clearing EditText field
                    //txtUpdate.setText("");
                }
            });*/
        }


    }

    //Database fields set to long
    public long twitterID;
    public String userName = null;

    private void checkTwitterID() {

        /* Get Access Token after login*/
        try {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
            builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);

            // Access Token
            String access_token = mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
            // Access Token Secret
            String access_token_secret = mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");

            AccessToken accessToken = new AccessToken(access_token, access_token_secret);
            Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);


            // Getting user details from twitter
            // For now i am getting his name only
            twitterID= accessToken.getUserId();
            User user = twitter.showUser(twitterID);

            userName = user.getName();


            //Toast.makeText(TraqComplaint.this, Long.toString(twitterID) + userName, Toast.LENGTH_SHORT).show();
            // Displaying in xml ui
            //lblUserName.setText(Html.fromHtml("<b>Welcome " + username + "</b>" + description));


        }
        catch (TwitterException e) {
            // Error in updating status
            Log.d("Twitter Update Error", e.getMessage());
        }
        catch (Exception e) {
            // Error in updating status
            Log.d("error!", e.getMessage());
        }
    }
    boolean isTwitterSelected = false;
    boolean isFacebookSelected = false;
    boolean isDefaultSelected = false;
    boolean isSmsSelected = false;
    boolean isSubmitButtonClicked = false;
    String finalStatus = null;
    private void PromptCustomDialog() {

        // Create custom dialog object
        final Dialog dialog = new Dialog(TraqComplaint.this);
        // Include dialog.xml file
        dialog.setContentView(R.layout.activity_submit_social);
        // Set dialog title
        dialog.setTitle("Submit via");

        // set values for custom dialog components - text, image and button
        final TextView twitterText = (TextView) dialog.findViewById(R.id.textTwitterDialog);
        twitterText.setText("Twitter");

        final TextView facebookText = (TextView) dialog.findViewById(R.id.textFacebookDialog);
        facebookText.setText("Facebook");

        final TextView defaultText = (TextView) dialog.findViewById(R.id.textDefaultDialog);
        defaultText.setText("Default");
        defaultText.setTextColor(getResources().getColor(R.color.Orange));

        final TextView smsText = (TextView) dialog.findViewById(R.id.textSMSDialog);
        smsText.setText("SMS");

        final ImageView image = (ImageView) dialog.findViewById(R.id.imageDialog);
        image.setImageResource(R.drawable.icon_twitter);

        final ImageView imageFb = (ImageView) dialog.findViewById(R.id.imageDialogFb);
        imageFb.setImageResource(R.drawable.ic_fb_grey);

        final ImageView imageDefault = (ImageView) dialog.findViewById(R.id.imageDialogDefault);
        imageDefault.setImageResource(R.drawable.icon_traquer_color);
        isDefaultSelected = true;

        final ImageView imageSMS = (ImageView) dialog.findViewById(R.id.imageDialogSMS);
        imageSMS.setImageResource(R.drawable.icon_sms);
        
        dialog.show();

        //Retrieve form info
        bus_id = inputBus.getText().toString().toUpperCase();
        bus_id = bus_id.replace(" ", "");
        bus_comp = actv_comp.getText().toString();
        loc_frm = actv_from.getText().toString();
        loc_to = actv_to.getText().toString();
        dateBus = editDate.getText().toString();
        timeBus = editTime.getText().toString();
        curr_time = editCurrTime.getText().toString();
        user_name = SaveSharedPreference.getUserName(TraqComplaint.this);

        //Twitter Button
        final RelativeLayout twitterLogin = (RelativeLayout)dialog.findViewById(R.id.twitterImageButton);
        twitterLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isTwitterSelected)
                {
                    image.setImageResource(R.drawable.icon_twitter);
                    twitterText.setTextColor(getResources().getColor(R.color.DarkGray));
                    isTwitterSelected = false;
                }
                else
                {
                    loginToTwitter();
                    image.setImageResource(R.drawable.icon_twitter_blue);
                    twitterText.setTextColor(getResources().getColor(R.color.TwitterBlue));
                    isTwitterSelected = true;
                }

            }
        });

        //facebook Button
        final RelativeLayout facebookLogin = (RelativeLayout)dialog.findViewById(R.id.facebookImageButton);
        facebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFacebookSelected)
                {
                    imageFb.setImageResource(R.drawable.ic_fb_grey);
                    if(fbUserName != null)
                        facebookText.setText("Facebook");

                    facebookText.setTextColor(getResources().getColor(R.color.DarkGray));
                    isFacebookSelected = false;
                }
                else
                {

                    //loginToTwitter();
                    // start Facebook Login
                    loginToFacebook();
                    if(fbUserName != null)
                        facebookText.setText(fbUserName);
                    imageFb.setImageResource(R.drawable.ic_fb_blue);
                    facebookText.setTextColor(getResources().getColor(R.color.TwitterBlue));
                    isFacebookSelected = true;
                }

            }
        });

        //SMS Button
        final RelativeLayout SMSLogin = (RelativeLayout)dialog.findViewById(R.id.smsImageButton);
        SMSLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSmsSelected)
                {
                    imageSMS.setImageResource(R.drawable.icon_sms);
                    smsText.setTextColor(getResources().getColor(R.color.DarkGray));
                    isSmsSelected = false;
                }
                else
                {
                    imageSMS.setImageResource(R.drawable.icon_sms_color);
                    smsText.setTextColor(getResources().getColor(R.color.Orange));
                    isSmsSelected = true;
                }

            }
        });

        /*/Default Button
        final RelativeLayout defaultLogin = (RelativeLayout)dialog.findViewById(R.id.defaultImageButton);
        defaultLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isDefaultSelected)
                {
                    imageDefault.setImageResource(R.drawable.icon_traquer_color);
                    defaultText.setTextColor(getResources().getColor(R.color.Orange));
                    isDefaultSelected = false;
                }
                else
                {
                    imageDefault.setImageResource(R.drawable.icon_traquer_color);
                    defaultText.setTextColor(getResources().getColor(R.color.Orange));
                    isDefaultSelected = true;
                }

            }
        });*/



        //Submit Button
        Button declineButton = (Button) dialog.findViewById(R.id.submitButton);
        // if decline button is clicked, close the custom dialog
        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //submit complaint
                isSubmitButtonClicked = true;
                if(isNetworkConnected() == true) {
                    Toast.makeText(TraqComplaint.this, "Complaint sent. Thank you for taking action!", Toast.LENGTH_SHORT).show();


                    //Combine Strings for Twitter Status

                    String status = bus_id + ", " + bus_comp + " bus is speeding with " + speedBusExceed + "km/h at " + Double.toString(gLatitude) + "°N, "+ Double.toString(gLongitude) + "°E, " + curr_time + " @aduanSPAD @MyTraquer #Traquer";

                    finalStatus = status;


                    if(isFacebookSelected)
                    {
                        //share to facebook
                        ShareToFacebook(status);
                        //publishFeedDialog();
                    }


                    if(isTwitterSelected)
                    {

                        //Toast.makeText(TraqComplaint.this, Long.toString(twitterID) + userName, Toast.LENGTH_SHORT).show();

                        // Check for blank text
                        if (status.trim().length() > 0) {
                            // update status
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1)
                            {
                                new updateTwitterStatus().execute(status);
                            }

                            else
                                new updateTwitterStatus().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, status);

                        } else {
                            // EditText is empty
                            Toast.makeText(getApplicationContext(),
                                    "Please enter status message", Toast.LENGTH_SHORT)
                                    .show();
                        }

                    }
                    else
                    {
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1)
                            new InsertForm().execute();
                        else
                            new InsertForm().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                    }
                    
                    // Close dialog
                    dialog.dismiss();

                }else {
                    if(isSmsSelected)
                    {
                        Log.i("Clicks", "You clicked sent.");

                        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
                        sendIntent.putExtra("address", "15888");
                        sendIntent.putExtra("sms_body", "SPAD Aduan " + bus_id + ", " + bus_comp + " bus is speeding with " + speedBusExceed + "km/h at " + Double.toString(gLatitude) + "°N, "+ Double.toString(gLongitude) + "°E, " + curr_time + " - Traquer");
                        sendIntent.setType("vnd.android-dir/mms-sms");
                        startActivity(sendIntent);

                        //1800-88-7723
                    }else {
                        Toast.makeText(TraqComplaint.this, "Failed to send. Please check your network connection.", Toast.LENGTH_SHORT).show();
                    }
                }

            }

        });
        //TraqComplaint.this.showDialog(ALERT_DIALOG);
    }

    public String fbUserName = null;
    public String fbID;
    private static final List<String> ALLPERMISSIONS = Arrays.asList("email","public_profile","user_friends");
    //Facebook Login

    private void loginToFacebook() {
        try {
            // start Facebook Login
            Session.openActiveSession(this, true, ALLPERMISSIONS, new Session.StatusCallback() {

                // callback when session changes state
                @Override
                public void call(final Session session, SessionState state, Exception exception) {
                    if (session.isOpened()) {

                        // make request to the /me API
                        Request.newMeRequest(session, new Request.GraphUserCallback() {

                            // callback after Graph API response with user object
                            @Override
                            public void onCompleted(GraphUser user, Response response) {
                                if (user != null) {

                                    if (!pendingPublishReauthorization) {
                                        List<String> permissions = session.getPermissions();
                                        if (!isSubsetOf(PERMISSIONS, permissions)) {

                                            pendingPublishReauthorization = true;

                                            Session.NewPermissionsRequest newPermissionsRequest = new Session
                                                    .NewPermissionsRequest(TraqComplaint.this, PERMISSIONS);

                                            session.requestNewPublishPermissions(newPermissionsRequest);

                                        } else {
                                            pendingPublishReauthorization = false;
                                        }
                                    } else {
                                        List<String> permissions = session.getPermissions();
                                        if (isSubsetOf(PERMISSIONS, permissions)) {
                                            pendingPublishReauthorization = false;
                                        }
                                    }
                                    fbID = user.getId();
                                    fbUserName = user.getName();

                                }
                            }
                        }).executeAsync();
                    }
                }
            });
        }
        catch(Exception error)
        {
            Toast.makeText(TraqComplaint.this
                            .getApplicationContext(),
                    "JSON error " + error.getMessage(),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    //Publish using Facebook Share Dialog
    private void publishFeedDialog() {
        if (FacebookDialog.canPresentShareDialog(getApplicationContext(),
                FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
            // Publish the post using the Share Dialog
            FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(this)
                    .setLink("https://developers.facebook.com/android")
                    .build();
            uiHelper.trackPendingDialogCall(shareDialog.present());

        } else {

            Bundle params = new Bundle();
            params.putString("name", "Facebook SDK for Android");
            params.putString("caption", "Build great social apps and get more installs.");
            params.putString("description", "The Facebook SDK for Android makes it easier and faster to develop Facebook integrated Android apps.");
            params.putString("link", "https://developers.facebook.com/android");
            params.putString("picture", "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png");

            WebDialog feedDialog = (
                    new WebDialog.FeedDialogBuilder(TraqComplaint.this,
                            Session.getActiveSession(),
                            params))
                    .setOnCompleteListener(new WebDialog.OnCompleteListener() {

                        @Override
                        public void onComplete(Bundle values,
                                               FacebookException error) {
                            if (error == null) {
                                // When the story is posted, echo the success
                                // and the post Id.
                                final String postId = values.getString("post_id");
                                if (postId != null) {
                                    Toast.makeText(TraqComplaint.this,
                                            "Posted story, id: " + postId,
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    // User clicked the Cancel button
                                    Toast.makeText(TraqComplaint.this.getApplicationContext(),
                                            "Publish cancelled",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else if (error instanceof FacebookOperationCanceledException) {
                                // User clicked the "x" button
                                Toast.makeText(TraqComplaint.this.getApplicationContext(),
                                        "Publish cancelled",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // Generic, ex: network error
                                Toast.makeText(TraqComplaint.this.getApplicationContext(),
                                        "Error posting story",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                    })
                    .build();
            feedDialog.show();
        }
    }
/*
    private static Session openActiveSession(Activity activity, boolean allowLoginUI, Session.StatusCallback callback, List<String> permissions) {
        Session.OpenRequest openRequest = new Session.OpenRequest(activity).setPermissions(permissions).setCallback(callback);
        Session session = new Session.Builder(activity).build();
        if (SessionState.CREATED_TOKEN_LOADED.equals(session.getState()) || allowLoginUI) {
            Session.setActiveSession(session);
            session.openForRead(openRequest);
            return session;
        }
        return null;
    }*/


    private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
    private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
    private boolean pendingPublishReauthorization = false;

    Facebook facebookClient;
    SharedPreferences mPrefs;

    String fbPostId = null;
    boolean isPublished = false;
    private void ShareToFacebook(String status) {
        try
        {


            Session session = Session.getActiveSession();

            if (session != null) {

                // Check for publish permissions
                List<String> permissions = session.getPermissions();
                if (!isSubsetOf(PERMISSIONS, permissions)) {

                    pendingPublishReauthorization = true;

                    Session.NewPermissionsRequest newPermissionsRequest = new Session
                            .NewPermissionsRequest(this, PERMISSIONS);

                    session.requestNewPublishPermissions(newPermissionsRequest);
                    return;
                }

                Bundle postParams = new Bundle();
                postParams.putString("message", status);
                postParams.putString("name", "Traquer Complaint");
                postParams.putString("caption", "Awareness is the key.");
                postParams.putString("description", "Traquer Complaint is a platform to share and create awareness on bus safety to public and government agency");
                postParams.putString("link", "https://traquer.my");
                postParams.putString("picture", "http://www.traquer.my/uploads/3/7/4/3/37436477/1409536841.png");

                Request.Callback callback = new Request.Callback() {
                    public void onCompleted(Response response) {
                        JSONObject graphResponse = response
                                .getGraphObject()
                                .getInnerJSONObject();

                        try {
                            fbPostId = graphResponse.getString("id");
                        } catch (JSONException e) {
                            Toast.makeText(TraqComplaint.this
                                            .getApplicationContext(),
                                    "JSON error " + e.getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show();


                        }
                        FacebookRequestError error = response.getError();
                        if (error != null) {
                            Toast.makeText(TraqComplaint.this
                                            .getApplicationContext(),
                                    error.getErrorMessage(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        } else {
                            Toast.makeText(TraqComplaint.this
                                            .getApplicationContext(),
                                    "Posted to facebook" + fbPostId,
                                    Toast.LENGTH_LONG
                            ).show();
                            //updateFacebook = true;
                            /*
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1)
                            {
                                new UpdateForm().execute();
                            }
                            else
                                new UpdateForm().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            */

                        }
                    }
                };


                Request request = new Request(session, "me/feed", postParams,
                        HttpMethod.POST, callback);

                RequestAsyncTask task = new RequestAsyncTask(request);
                task.execute();

                isPublished = true;
            }

        }
        catch(Exception error)
        {
            Toast.makeText(TraqComplaint.this
                            .getApplicationContext(),
                    "JSON error " + error.getMessage(),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
        for (String string : subset) {
            if (!superset.contains(string)) {
                return false;
            }
        }
        return true;
    }


    //@Override
    private void onSessionStateChange(SessionState state) {
        if (state.isOpened()) {
            //shareButton.setVisibility(View.VISIBLE);
            if (pendingPublishReauthorization &&
                    state.equals(SessionState.OPENED_TOKEN_UPDATED)) {
                pendingPublishReauthorization = false;
                ShareToFacebook(finalStatus);
            }
        } else if (state.isClosed()) {
            //shareButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            final AlertDialog.Builder alertBox = new AlertDialog.Builder(TraqComplaint.this);
            alertBox.setIcon(R.drawable.info_icon);
            alertBox.setCancelable(false);
            alertBox.setTitle("Do you want to cancel complaint?");
            alertBox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    // finish used for destroyed activity
                    easyTracker.send(MapBuilder.createEvent("Complaint", "Cancel Complaint (Yes)", "Complaint event", null).build());
                    NavUtils.navigateUpFromSameTask(TraqComplaint.this);
                }
            });

            alertBox.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    easyTracker.send(MapBuilder.createEvent("Complaint", "Cancel Complaint (No)", "Complaint event", null).build());
                    dialog.cancel();
                }
            });

            alertBox.show();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        return false;
    }

}
