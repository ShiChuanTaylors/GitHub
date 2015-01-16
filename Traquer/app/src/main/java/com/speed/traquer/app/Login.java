package com.speed.traquer.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;
import com.pushbots.push.Pushbots;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Login extends Activity{

    private EasyTracker easyTracker = null;
    Button ibSignIn;
    Button ibSignUp;

    JSONParser jsonParser = new JSONParser();
    private final static int ALERT_DIALOG = 1;
    private static final String TAG_SUCCESS = "success";
    private static String url_insert_formLogin= "http://cyberweb.my/traquer/tlogin.php";


    EditText tbUserName;
    EditText tbPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        ibSignIn = (Button) findViewById(R.id.ibLogin);
        ibSignUp = (Button) findViewById(R.id.ibSignUp);
        tbUserName = (EditText)findViewById(R.id.login_user);
        tbPassword = (EditText)findViewById(R.id.login_pwd);

        ibSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                easyTracker.send(MapBuilder.createEvent("Login", "login button pressed", "Login event", null).build());
                if(isNetworkConnected() == true) {
                    //Toast.makeText(SignUp.this, "Complaint sent. Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                    new LoginForm().execute();
                }else {
                    Toast.makeText(Login.this, "Failed to send. Please check your network connection.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ibSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    Intent intent = new Intent(getApplicationContext(),SignUp.class);
                    startActivity(intent);
                }catch (ArrayIndexOutOfBoundsException e){
                    easyTracker.send(MapBuilder.createException(new StandardExceptionParser(Login.this, null).getDescription(Thread.currentThread().getName(),e),false).build());
                }
            }
        });

        easyTracker = EasyTracker.getInstance(Login.this);


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
    public void onBackPressed() {
        moveTaskToBack (true);
    }

    String userName;
    String password;
    String message = null;
    int success = 0;

    //Search username and password from database
    class LoginForm extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {

            //Retrieve form info
            userName = tbUserName.getText().toString();
            password = tbPassword.getText().toString();

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            if(userName.length() < 5 )
                return "Invalid Username entered.";

            params.add(new BasicNameValuePair("username", userName));
            params.add(new BasicNameValuePair("password", password));


            JSONObject json = jsonParser.makeHttpRequest(url_insert_formLogin,
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

                SaveSharedPreference.setUserName(Login.this, userName);
                Toast.makeText(Login.this, "Login succesfully!", Toast.LENGTH_SHORT).show();
                // successfully created product
                SaveSharedPreference.setUserName(Login.this, userName);
                Intent i;
                i = new Intent(getApplicationContext(), Speedometer.class);
                startActivity(i);
                finish();

            } else {
                Toast.makeText(Login.this, result, Toast.LENGTH_SHORT).show();

            }
        }
    }

}
