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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

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
import java.util.ArrayList;
import java.util.List;

public class Account extends ActionBarActivity {

    JSONParser jsonParser = new JSONParser();
    private static final String TAG_SUCCESS = "success";
    private static String url_update_user= "http://cyberweb.my/traquer/updateuseremail.php";
    private EasyTracker easyTracker = null;
    private EditText mail, hpno;
    private TextView usr;
    private String username;
    private String email_old;
    ListView listAccSetting;
    String[] values_pwd = new String[] { "Change Password"};
    String[] values_hp = new String[] { "Phone Number"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        usr = (TextView) findViewById(R.id.usr);
        mail = (EditText) findViewById(R.id.mail);
        hpno = (EditText) findViewById(R.id.hpno);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        username = SaveSharedPreference.getUserName(Account.this);
        usr.setText(username);

        listAccSetting = (ListView) findViewById(R.id.listAccSetting);

        if(isNetworkConnected()) {
            progressBar.setVisibility(View.VISIBLE);
            new getUserInfo().execute(new ApiConnector());
        }else {
            Toast.makeText(getApplicationContext(), "Sorry we couldn't complete your request. Please check your network connection.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
        }

        easyTracker = EasyTracker.getInstance(Account.this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.account, menu);
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
                if(mail.getText().toString().length() < 12 ){
                    Toast.makeText(Account.this, "Invalid Email.", Toast.LENGTH_SHORT).show();
                }else{
                    easyTracker.send(MapBuilder.createEvent("Account", "Send button pressed", "Account event", null).build());
                    new ChangeUserForm().execute();
                }
            }else {
                Toast.makeText(Account.this, "Failed to complete your request. Please check your network connection.", Toast.LENGTH_SHORT).show();
            }
        }else if (id == android.R.id.home){
            easyTracker.send(MapBuilder.createEvent("Account", "back button pressed", "Account event", null).build());
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class getUserInfo extends AsyncTask<ApiConnector,Long,JSONArray> {
        @Override
        protected JSONArray doInBackground(ApiConnector... params){
            //it is executed on Background thread
            return params[0].GetUserInfo();
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray){
            setTextToTextView(jsonArray);
        }

    }

    //private ProgressBar view = new ProgressBar(Feedback_rate.this);

    private void setTextToTextView(JSONArray jsonArray) {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        TextView usr = (TextView) findViewById(R.id.usr);
        EditText mail = (EditText) findViewById(R.id.mail);
        EditText hpno = (EditText) findViewById(R.id.hpno);
        final ListView listAccSetting = (ListView) findViewById(R.id.listAccSetting);
        final ListView listAccPwd = (ListView) findViewById(R.id.listAccPwd);
        try{
            String sMail = "", sHpno = "";

            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject json = null;
                try{
                    json = jsonArray.getJSONObject(i);
                    sMail = json.getString("temail");
                    sHpno = json.getString("thpno");
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
            this.mail.setText(sMail);
            this.hpno.setText(sHpno);
            email_old = mail.getText().toString();
        }
        catch(Exception error) {
            error.printStackTrace();
            new getUserInfo().execute(new ApiConnector());
        }
        try{
            progressBar.setVisibility(View.GONE);
            usr.setVisibility(View.VISIBLE);
            mail.setVisibility(View.VISIBLE);
            values_hp[0] = hpno.getText().toString();
            ArrayAdapter<String> adapterSet = new ArrayAdapter<String>(this, R.layout.list_acc_hp, values_hp);
            listAccSetting.setAdapter(adapterSet);
            listAccSetting.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, final long id) {

                    if(position == 0 && id == 0){
                       // Intent intent = new Intent(getApplicationContext(),Account.class);
                        //startActivity(intent);
                    }
                }
            });

            ArrayAdapter<String> adapterSetPwd = new ArrayAdapter<String>(this, R.layout.list_acc_pwd, values_pwd);
            listAccPwd.setAdapter(adapterSetPwd);

            listAccPwd.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, final long id) {

                    if(position == 0 && id == 0){
                        Intent intent = new Intent(getApplicationContext(),ChangePassword.class);
                        startActivity(intent);
                    }
                }
            });
        }catch(Exception error) {
            error.printStackTrace();
        }

    }

    public class ApiConnector {


        public JSONArray GetUserInfo(){
            String url = "http://cyberweb.my/traquer/getUserInfo.php?username=" + username.replace(" ", "%20");

            HttpEntity httpEntity = null;

            try {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);

                HttpResponse httpResponse = httpClient.execute(httpGet);

                httpEntity = httpResponse.getEntity();
            } catch (ClientProtocolException e){
                e.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }

            //Convert HttpEntity into JSON Array
            JSONArray jsonArray = null;

            if(httpEntity != null){
                try {
                    String entityResponse = EntityUtils.toString(httpEntity);

                    Log.e("Entity Response : ", entityResponse);

                    jsonArray = new JSONArray(entityResponse);
                } catch (JSONException e){
                    e.printStackTrace();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
            return jsonArray;
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            easyTracker.send(MapBuilder.createEvent("Account", "back button pressed", "Account event", null).build());
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
    String userName_old;
    String email;
    String message = null;
    int success = 0;


    //INSERT Form to database
    class ChangeUserForm extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {

            //Retrieve form info
            email = mail.getText().toString().replace(" ", "");
            userName_old = SaveSharedPreference.getUserName(Account.this);
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            params.add(new BasicNameValuePair("email", email));
            params.add(new BasicNameValuePair("email_old", email_old));
            params.add(new BasicNameValuePair("username_old", userName_old));

            JSONObject json = jsonParser.makeHttpRequest(url_update_user,
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

                Toast.makeText(Account.this, "Account changed successfully!", Toast.LENGTH_SHORT).show();
                // successfully created product
                Intent i;
                i = new Intent(getApplicationContext(), Settings.class);
                startActivity(i);
                finish();

            } else {
                Toast.makeText(Account.this, result, Toast.LENGTH_SHORT).show();

            }
        }
    }
}
