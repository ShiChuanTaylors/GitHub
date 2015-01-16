package com.speed.traquer.app;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.speed.traquer.app.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SignUp extends ActionBarActivity {

    JSONParser jsonParser = new JSONParser();
    private final static int ALERT_DIALOG = 1;
    private static final String TAG_SUCCESS = "success";
    private static String url_insert_formLogin= "http://cyberweb.my/traquer/insert_sign_up.php";

    Button btnSignUp;
    EditText tbUserName;
    EditText tbEmail;
    EditText tbPhoneNo;
    EditText tbPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        this.btnSignUp = (Button)findViewById(R.id.btnSignUp);
        this.tbUserName = (EditText)findViewById(R.id.usr);
        this.tbEmail = (EditText)findViewById(R.id.mail);
        this.tbPhoneNo = (EditText)findViewById(R.id.hpno);
        this.tbPassword = (EditText)findViewById(R.id.pwd);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    if(isNetworkConnected() == true) {
                        //Toast.makeText(SignUp.this, "Complaint sent. Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                        new SignUpForm().execute();
                    }else {
                        Toast.makeText(SignUp.this, "Failed to send. Please check your network connection.", Toast.LENGTH_SHORT).show();
                    }
                }catch (ArrayIndexOutOfBoundsException e){
                    //easyTracker.send(MapBuilder.createException(new StandardExceptionParser(Login.this, null).getDescription(Thread.currentThread().getName(),e),false).build());
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sign_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    String userName;
    String email;
    String phone_no;
    String password;
    String message = null;
    int success = 0;


    //INSERT Form to database
    class SignUpForm extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {

            //Retrieve form info
            userName = tbUserName.getText().toString();
            email = tbEmail.getText().toString().replace(" ", "");
            phone_no = tbPhoneNo.getText().toString().replace(" ","");
            password = tbPassword.getText().toString();

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            if(userName.length() < 5 )
                return "Invalid Username. Required more then 5 characters.";
            if(password.length() < 7)
                return "Invalid password. Required more than 6 characters";
            if(email.length() < 12 )
                return "Invalid Email";
            if(phone_no.length() < 10)
                return "Invalid phone number";


            params.add(new BasicNameValuePair("username", userName));
            params.add(new BasicNameValuePair("email", email));
            params.add(new BasicNameValuePair("phone_no", phone_no));
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

                Toast.makeText(SignUp.this, "Sign up succesfully!", Toast.LENGTH_SHORT).show();
                // successfully created product
                SaveSharedPreference.setUserName(SignUp.this, userName);
                Intent i;
                i = new Intent(getApplicationContext(), Speedometer.class);
                startActivity(i);
                finish();

            } else {
                Toast.makeText(SignUp.this, result, Toast.LENGTH_SHORT).show();

            }
        }
    }
}
