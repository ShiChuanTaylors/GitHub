package com.speed.traquer.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.speed.traquer.app.R;

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
import java.text.DecimalFormat;
import java.util.ArrayList;

public class ListCmmt extends ActionBarActivity{

    private EasyTracker easyTracker = null;
    String passedCmmtComp = null;
    ListView listComment;
    FancyAdapter fa = null;
    ArrayList<rateComment> rateCommentArrayList = new ArrayList<rateComment>();
    class rateComment{
        public String cmmt_user;
        public String tcmmt;
        public String cmmt_dateTime;
        public String no_comment;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        easyTracker = EasyTracker.getInstance(ListCmmt.this);
        Intent i = getIntent();
        passedCmmtComp = i.getStringExtra("compcode");
        setContentView(R.layout.activity_list_cmmt);
        listComment = (ListView) findViewById(R.id.listComment);
        listComment.setClickable(false);
        new getComment().execute(new ApiConnector());

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

    public class ApiConnector {

        public JSONArray GetComment(){
            String url = "http://cyberweb.my/traquer/getComment.php?compcode=" + passedCmmtComp;
            url = url.replace(" ", "%20");
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

    private class getComment extends AsyncTask<ApiConnector,Long,JSONArray> {
        @Override
        protected JSONArray doInBackground(ApiConnector... params){
            //it is executed on Background thread
            return params[0].GetComment();
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray){
            setTextToTextView(jsonArray);
            fa = new FancyAdapter();
            listComment.setAdapter(fa);
        }

    }

    private void setTextToTextView(JSONArray jsonArray) {
       ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        try{
            if(jsonArray != null) {
               for (int i = 0; i < jsonArray.length(); i++) {
                   JSONObject json = null;
                   try {
                       json = jsonArray.getJSONObject(i);
                       rateComment resultRow = new rateComment();
                       resultRow.cmmt_user = json.getString("tusrname");
                       resultRow.tcmmt = json.getString("trptcomm");
                       resultRow.cmmt_dateTime = json.getString("rdate") + " at " + json.getString("rtime");
                       rateCommentArrayList.add(resultRow);
                   } catch (JSONException e) {
                       e.printStackTrace();
                   }
               }
           }else{
               rateComment resultRow = new rateComment();
               resultRow.no_comment = "No Comment";
               rateCommentArrayList.add(resultRow);
               System.out.print("No Comment");
           }
        }catch(Exception error) {
            error.printStackTrace();
        }

        try{
            progressBar.setVisibility(View.GONE);
        }catch(Exception error) {
            error.printStackTrace();
        }
    }

    public ViewHolder holder;
    public int getViewPosition;
    class FancyAdapter extends ArrayAdapter<rateComment> {
        FancyAdapter(){
            super(getApplicationContext(), R.layout.list_comment, rateCommentArrayList);
        }
        public View getView(int position, View convertView, ViewGroup parent){
            getViewPosition = position;
            View row = convertView;
            if(row == null){
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.list_comment,parent,false);
                holder = new ViewHolder(row);
                row.setTag(holder);
                Log.d("Traq", "Creating new row");
            }else{
                holder = (ViewHolder) row.getTag();
            }
            holder.populateFrom(rateCommentArrayList.get(position));
            return row;
        }
    }

    class ViewHolder {
        public TextView comment_user;
        public TextView tcomment;
        public TextView comment_dateTime;
        public TextView no_comment;
        ViewHolder(View list_result){
            comment_user = (TextView) list_result.findViewById(R.id.cmmt_user);
            tcomment = (TextView) list_result.findViewById(R.id.tcmmt);
            comment_dateTime = (TextView) list_result.findViewById(R.id.cmmtDateTime);
            no_comment = (TextView) list_result.findViewById(R.id.no_comment);

            String fontPath = "fonts/segoeuil.ttf";
            Typeface tf = Typeface.createFromAsset(getApplicationContext().getAssets(), fontPath);
            comment_user.setTypeface(tf);
            tcomment.setTypeface(tf);
            comment_dateTime.setTypeface(tf);
            no_comment.setTypeface(tf);
        }
        void populateFrom(rateComment r){
            comment_user.setText(r.cmmt_user);
            tcomment.setText(r.tcmmt);
            comment_dateTime.setText(r.cmmt_dateTime);
            no_comment.setText(r.no_comment);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_cmmt, menu);
        return true;
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home){
            easyTracker.send(MapBuilder.createEvent("List Comments", "back button pressed", "List Comment event", null).build());
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            easyTracker.send(MapBuilder.createEvent("Comments", "Back button pressed", "Comments event", null).build());
        }
        return super.onKeyDown(keyCode, event);
    }
}
