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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AppFeedback extends ActionBarActivity {

    private EasyTracker easyTracker = null;
    JSONParser jsonParser = new JSONParser();
    private static final String TAG_SUCCESS = "success";
    private static String url_insert_feedbackForm= "http://cyberweb.my/traquer/insert_feedback.php";

    Button btnSubmit;
    EditText tbName;
    EditText tbEmail;
    EditText tbComments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_feedback);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.btnSubmit = (Button)findViewById(R.id.feedback_send);
        this.tbName = (EditText)findViewById(R.id.fName);
        this.tbEmail = (EditText)findViewById(R.id.fMail);
        this.tbComments = (EditText)findViewById(R.id.fFeedback);

        String fontPath = "fonts/segoeuil.ttf";
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        btnSubmit.setTypeface(tf);
        tbName.setTypeface(tf);
        tbEmail.setTypeface(tf);
        tbComments.setTypeface(tf);

        TextView txtName = (TextView) findViewById(R.id.txtName);
        txtName.setTypeface(tf);

        TextView txtEmail = (TextView) findViewById(R.id.txtMail);
        txtEmail.setTypeface(tf);

        TextView txtFeedback = (TextView) findViewById(R.id.txtFeedback);
        txtFeedback.setTypeface(tf);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    if(isNetworkConnected()) {
                        //Toast.makeText(SignUp.this, "Complaint sent. Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                        easyTracker.send(MapBuilder.createEvent("Menu Feedback", "Send button pressed", "Menu Feedback event", null).build());
                        new FeedbackForm().execute();
                    }else {
                        easyTracker.send(MapBuilder.createEvent("Menu Feedback", "Send button pressed (no network)", "Menu Feedback event", null).build());
                        Toast.makeText(AppFeedback.this, "Failed to send. Please check your network connection.", Toast.LENGTH_SHORT).show();
                    }
                }catch (ArrayIndexOutOfBoundsException e){
                    //easyTracker.send(MapBuilder.createException(new StandardExceptionParser(Login.this, null).getDescription(Thread.currentThread().getName(),e),false).build());
                }
            }
        });

        easyTracker = EasyTracker.getInstance(AppFeedback.this);
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
        getMenuInflater().inflate(R.menu.activity_app_feedback, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            easyTracker.send(MapBuilder.createEvent("Menu Feedback", "back button pressed", "Menu Feedback event", null).build());
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(tbName.length() != 0){
                final AlertDialog.Builder alertBox = new AlertDialog.Builder(AppFeedback.this);
                alertBox.setIcon(R.drawable.info_icon);
                alertBox.setCancelable(false);
                alertBox.setTitle("Do you want to cancel feedback?");
                alertBox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        // finish used for destroyed activity
                        easyTracker.send(MapBuilder.createEvent("Menu Feedback", "Cancel Feedback (Yes)", "Menu Feedback event", null).build());
                        finish();
                    }
                });

                alertBox.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        easyTracker.send(MapBuilder.createEvent("Menu Feedback", "Cancel Feedback (No)", "Menu Feedback event", null).build());
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

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            return false;
        } else {
            return true;
        }
    }

    String name;
    String email;
    String comment;
    String message = null;
    int success = 0;


    //INSERT Form to database
    class FeedbackForm extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {

            //Retrieve form info
            name = tbName.getText().toString();
            email = tbEmail.getText().toString().replace(" ", "");
            comment = tbComments.getText().toString();
            String user_name = SaveSharedPreference.getUserName(AppFeedback.this);

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            if(name.length() < 5 )
                return "Invalid Username. Required more then 5 characters.";
            if(email.length() < 12 )
                return "Invalid Email";

            params.add(new BasicNameValuePair("fb_name", name));
            params.add(new BasicNameValuePair("fb_email", email));
            params.add(new BasicNameValuePair("fb_comment", comment));
            params.add(new BasicNameValuePair("tusrname", user_name));


            JSONObject json = jsonParser.makeHttpRequest(url_insert_feedbackForm,
                    "POST", params);


            Log.d("Create Response", json.toString());

            try {
                success = 0;
                success = json.getInt(TAG_SUCCESS);
                message = json.getString("message");


                return message;

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            if (success == 1) {

                Toast.makeText(AppFeedback.this, "Feedback sent succesfully!", Toast.LENGTH_SHORT).show();
                // successfully created product
                Intent i;
                i = new Intent(getApplicationContext(), Speedometer.class);
                startActivity(i);
                finish();

            } else {
                Toast.makeText(AppFeedback.this, result, Toast.LENGTH_SHORT).show();

            }
        }
    }
}
