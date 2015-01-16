package com.speed.traquer.app;

import android.content.Context;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.speed.traquer.app.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChangePassword extends ActionBarActivity {

    JSONParser jsonParser = new JSONParser();
    private static final String TAG_SUCCESS = "success";
    EditText curr_pwd, new_pwd, pwd;
    private EasyTracker easyTracker = null;
    private static String url_update_password= "http://cyberweb.my/traquer/pwdUpdate.php";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        curr_pwd = (EditText) findViewById(R.id.curr_pwd);
        new_pwd = (EditText) findViewById(R.id.new_pwd);
        pwd = (EditText) findViewById(R.id.pwd);


        easyTracker = EasyTracker.getInstance(ChangePassword.this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.change_password, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_send) {
            if(isNetworkConnected()) {
                if(curr_pwd.length() == 0 || new_pwd.length() == 0 || pwd.length() == 0){
                    Toast.makeText(ChangePassword.this, "Please fill in all required field.", Toast.LENGTH_SHORT).show();
                }else if(new_pwd.length() < 7){
                    Toast.makeText(ChangePassword.this, "Invalid password. Required more than 6 characters.", Toast.LENGTH_SHORT).show();
                }else if(!new_pwd.getText().toString().equals(pwd.getText().toString())){
                    Toast.makeText(ChangePassword.this, "Password do not match", Toast.LENGTH_SHORT).show();
                }else {
                    easyTracker.send(MapBuilder.createEvent("Change Password", "Send button pressed", "Change Password event", null).build());
                    //new InsertComment().execute();
                    new ChangePwdForm().execute();
                }
            }else {
                Toast.makeText(ChangePassword.this, "Failed to complete your request. Please check your network connection.", Toast.LENGTH_SHORT).show();
            }
        }else if (id == android.R.id.home){
            easyTracker.send(MapBuilder.createEvent("Change Password", "back button pressed", "Change Password event", null).build());
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
            easyTracker.send(MapBuilder.createEvent("Change Password", "back button pressed", "Change Password event", null).build());
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            return false;
        } else {
            return true;
        }
    }

    String userName;
    String currPwd;
    String newPwd;
    String message = null;
    int success = 0;


    //INSERT Form to database
    class ChangePwdForm extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {

            //Retrieve form info
            userName = SaveSharedPreference.getUserName(ChangePassword.this);
            currPwd = curr_pwd.getText().toString();
            newPwd = new_pwd.getText().toString();

            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("username", userName));
            params.add(new BasicNameValuePair("pwd_old", currPwd));
            params.add(new BasicNameValuePair("pwd", newPwd));

            JSONObject json = jsonParser.makeHttpRequest(url_update_password,
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

                Toast.makeText(ChangePassword.this, "Password changed successfully!", Toast.LENGTH_SHORT).show();
                // successfully created product
                Intent i;
                i = new Intent(getApplicationContext(), Account.class);
                startActivity(i);
                finish();

            } else {
                Toast.makeText(ChangePassword.this, result, Toast.LENGTH_SHORT).show();

            }
        }
    }
}
