package com.speed.traquer.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


public class TraqRecord extends Activity{

    Button search;
    EditText code;
    String getId = "";
    TextView resultView;
    TextView txtDate;
    final Calendar c = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traq_record);
        search = (Button) findViewById(R.id.search);
        code = (EditText) findViewById(R.id.insert_code);
        resultView = (TextView) findViewById(R.id.result);
        txtDate = (TextView) findViewById(R.id.txtDate);

        String fontPathMonth = "fonts/segoeuib.ttf";
        TextView txt = txtDate;
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPathMonth);
        txt.setTypeface(tf);

        setCurrentDateOnView();

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //hide keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(code.getWindowToken(), 0);

                //get value of ticket id
                getId = code.getText().toString().toUpperCase();
                getId = getId.replace(" ", "");
                //prevent empty code
                if (getId.length() == 0) {
                    Toast.makeText(TraqRecord.this, "Please enter the code!", Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(TraqCode.this, getId, Toast.LENGTH_SHORT).show();
                    new getIdResult().execute(new ApiConnector());
                }
            }
        });
    }

    private void setCurrentDateOnView() {
        String dateFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat( dateFormat, Locale.ENGLISH );
        txtDate.setText( sdf.format( c.getTime() ) );
    }

    private class getIdResult extends AsyncTask<ApiConnector,Long,JSONArray> {
        @Override
        protected JSONArray doInBackground(ApiConnector... params){
            //it is executed on Background thread
            return params[0].GetResult();
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray){
            setTextToTextView(jsonArray);
        }

    }

    private void setTextToTextView(JSONArray jsonArray) {
        String s ="";
        for (int i = 0; i < jsonArray.length(); i++){
            JSONObject json;
            try{
                json = jsonArray.getJSONObject(i);
                s = s +
                        "Bus Number : " + json.getString("busid") + "\n" +
                        "Company : " + json.getString("bus_comp") + "\n" +
                        "Number of complaint: " + json.getString("COUNT(*)") + "times" + "\n\n";
            } catch (JSONException e){
                e.printStackTrace();
            }
        }

        this.resultView.setText(s);

    }

    public class ApiConnector {

        public JSONArray GetResult(){
            String url = "http://cyberweb.my/traquer/getIdResult.php?busid=" + getId;

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



}
