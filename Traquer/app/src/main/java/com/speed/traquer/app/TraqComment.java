package com.speed.traquer.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class TraqComment extends ActionBarActivity implements RatingBar.OnRatingBarChangeListener {
    RatingBar ratingBar;
    EditText inputBus;
    EditText inputComment;
    TextView ratingValue;
    ImageButton commentSend;
    JSONParser jsonParser = new JSONParser();

    private final static int ALERT_DIALOG = 1;
    private static final String TAG_SUCCESS = "success";
    private static String url_insert_comment= "http://cyberweb.my/traquer/insert_comment.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traq_comment);
        inputBus = (EditText) findViewById(R.id.bus_id);
        inputComment = (EditText) findViewById(R.id.traq_comment);
        ratingValue = (TextView) findViewById(R.id.ratingValue);
        ratingBar = (RatingBar)findViewById(R.id.ratingBar);
        ratingBar.setOnRatingBarChangeListener(this);
        commentSend = (ImageButton) findViewById(R.id.comment_send);
        commentSend.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                String bus_id = inputBus.getText().toString().toUpperCase();
                if(bus_id.length() == 0){
                    Toast.makeText(TraqComment.this, "Bus Plate Number is required!", Toast.LENGTH_SHORT).show();
                }else {
                    TraqComment.this.showDialog(ALERT_DIALOG);

                }
            }
        });
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean b) {
        ratingValue.setText(String.valueOf(rating));
    }

    class InsertComment extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            String bus_id = inputBus.getText().toString().toUpperCase();
            bus_id = bus_id.replace(" ", "");
            String bus_cmmt= inputComment.getText().toString();
            String bus_rate = (String) ratingValue.getText();

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("bus_id", bus_id));
            params.add(new BasicNameValuePair("bus_cmmt", bus_cmmt));
            params.add(new BasicNameValuePair("bus_rate", bus_rate));

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
                    //Toast.makeText(TraqComplaint.this, "Failed to send. Please check your network connection.", Toast.LENGTH_SHORT).show();
                    // failed to create product
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch (id) {
            case ALERT_DIALOG:
                builder.setMessage("Are you sure you want to send? Every comment make a difference!")
                        .setPositiveButton("Comment", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                if(isNetworkConnected()) {
                                    Toast.makeText(TraqComment.this, "Comment Successful. Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                                    new InsertComment().execute();
                                }else {
                                    Toast.makeText(TraqComment.this, "Failed to comment. Please check your network connection.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                Toast.makeText(TraqComment.this, "Comment unsuccessful.", Toast.LENGTH_SHORT).show();
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
}
