package com.speed.traquer.app;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Feedback_rate extends ActionBarActivity {

    private EasyTracker easyTracker = null;
    RadioGroup rgSafety;
    RadioGroup rgDriver;
    RadioGroup rgClean;
    RadioGroup rgComfort;
    RadioGroup rgPunctual;
    EditText feedback_remarks;
    AutoCompleteTextView actv_comp;
    ImageButton btnImg;
    EditText editCurrDate;
    EditText editCurrTime;
    EditText inputBus;
    ImageButton rateBtnTaxi;
    int re1=0,re2=0,re3=0,re4=0,re5=0,rg1=0,rg2=0,rg3=0,rg4=0,rg5=0,rb1=0,rb2=0,rb3=0,rb4=0,rb5=0;
    int rGroup1=0,rGroup2=0,rGroup3=0,rGroup4=0,rGroup5=0;
    private static final String TAG_SUCCESS = "success";
    JSONParser jsonParser = new JSONParser();
    final Calendar c = Calendar.getInstance();
    private static String url_insert_comment= "http://cyberweb.my/traquer/insert_comment.php";
    private final static String busUrl = "http://traquer.cyberweb.my/getBuslike.php?compcode=";
    private static final int ACTION_TAKE_PHOTO= 1;
    private static final String BITMAP_STORAGE_KEY = "viewbitmap";
    private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
    private Bitmap mImageBitmap;
    private String mCurrentPhotoPath;
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private AlbumStorage mAlbumStorage = null;
    private String getAlbumName() {
        return getString(R.string.app_name);
    }
    private File getAlbumDir() {
        File storageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = mAlbumStorage.getAlbumStorage(getAlbumName());
            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        Log.d("CameraSample", "failed to create directory");
                        return null;
                    }
                }
            }
        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }
        return storageDir;
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        return imageF;
    }
    private File setUpPhotoFile() throws IOException {
        File f = createImageFile();
        mCurrentPhotoPath = f.getAbsolutePath();
        return f;
    }
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
    private void dispatchTakePictureIntent(int actionCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        switch(actionCode) {
            case ACTION_TAKE_PHOTO:
                File f = null;
                try {
                    f = setUpPhotoFile();
                    mCurrentPhotoPath = f.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                } catch (IOException e) {
                    e.printStackTrace();
                    f = null;
                    mCurrentPhotoPath = null;
                }
                break;
            default:
                break;
        }
        startActivityForResult(takePictureIntent, actionCode);
    }
    private void handleBigCameraPhoto() {
        if (mCurrentPhotoPath != null) {
            galleryAddPic();
            mCurrentPhotoPath = null;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traq_feeback_rate);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        rgSafety = (RadioGroup) findViewById(R.id.radioSafety);
        rgDriver = (RadioGroup) findViewById(R.id.radioDriver);
        rgClean = (RadioGroup) findViewById(R.id.radioClean);
        rgComfort = (RadioGroup) findViewById(R.id.radioComfort);
        rgPunctual = (RadioGroup) findViewById(R.id.radioPunctual);
        feedback_remarks = (EditText) findViewById(R.id.feedback_remarks);
        inputBus = (EditText) findViewById(R.id.bus_id);
        editCurrDate = (EditText) findViewById(R.id.editCurrDate);
        editCurrTime= (EditText) findViewById(R.id.editCurrTime);

        ProgressBar barProgress = (ProgressBar) findViewById(R.id.progressLoading);

        rateBtnTaxi = (ImageButton) findViewById(R.id.btn_rate_taxi);

        rateBtnTaxi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(),Feedback_rate_taxi.class);
                startActivity(intent);
            }
        });

        //Auto Complete Database
        if(isNetworkConnected()) {
            actv_comp = (AutoCompleteTextView) findViewById(R. id.search_comp);
            SuggestionAdapter sa =new SuggestionAdapter(this,actv_comp.getText().toString(), busUrl, "compcode");
            sa.setLoadingIndicator(barProgress);
            actv_comp.setAdapter(sa);
        }else {
            Toast.makeText(getApplicationContext(), "Looks like there's a problem with your network connection.", Toast.LENGTH_SHORT).show();
        }

        actv_comp = (AutoCompleteTextView) findViewById(R. id.search_comp);
        SuggestionAdapter sa =new SuggestionAdapter(this,actv_comp.getText().toString(), busUrl, "compcode");
        sa.setLoadingIndicator(barProgress);
        actv_comp.setAdapter(sa);

        String fontPath = "fonts/segoeuil.ttf";
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        inputBus.setTypeface(tf);
        actv_comp.setTypeface(tf);
        feedback_remarks.setTypeface(tf);

        TextView txtComp = (TextView) findViewById(R.id.bus_comp);
        txtComp.setTypeface(tf);

        TextView txtNumber = (TextView) findViewById(R.id.bus_number);
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

        getCurrentDate();
        getCurrentTime();
        btnImg = (ImageButton) findViewById(R.id.take_gallery);
        ImageButton btnCamera = (ImageButton) findViewById(R.id.take_camera);
        //btnImg.setOnClickListener(btnPressedToGallery);
        //btnCamera.setOnClickListener(btnPressedToCamera);
        /**Button.OnClickListener mTakePicOnClickListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
        dispatchTakePictureIntent(ACTION_TAKE_PHOTO);
        }
        }; **/
        //setBtnListenerOrDisable(btnCamera, btnPressedToCamera,MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorage = new FroyoAlbum();
        } else {
            mAlbumStorage = new BaseAlbum();
        }
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

        easyTracker = EasyTracker.getInstance(Feedback_rate.this);
    }
    public View.OnClickListener btnPressedToGallery = new View.OnClickListener() {
        @Override
        public void onClick(View v){
            Intent i = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            final int ACTIVITY_SELECT_IMAGE = 1234;
            startActivityForResult(i, ACTIVITY_SELECT_IMAGE);

        }
    };

    /*/Start ACTV
    private class getBusComp extends AsyncTask<ApiConnector,Long,JSONArray> {
        @Override
        protected JSONArray doInBackground(ApiConnector... params){
            //it is executed on Background thread
            return params[0].GetBusComp();
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray){
            setTextToTextView(jsonArray);
        }

    }

    //private ProgressBar view = new ProgressBar(Feedback_rate.this);

    private void setTextToTextView(JSONArray jsonArray) {
        try{
            String s ="";
            List<String> responseList = new ArrayList<String>();
            final ProgressBar barProgress = (ProgressBar) findViewById(R.id.progressLoading);
            //Progress bar
            //AutoCompleteLoading loading = new AutoCompleteLoading(Feedback_rate.this);
            //loading.setLoadingIndicator(view);
            barProgress.setVisibility(View.VISIBLE);
            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject json;
                try{
                    json = jsonArray.getJSONObject(i);
                    s = json.getString("compcode");
                    responseList.add(s);
                    Log.e("json array: ", String.valueOf(json));
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
            barProgress.setVisibility(View.INVISIBLE);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.listcomp, responseList);
            actv_comp = (AutoCompleteTextView) findViewById(R.id.search_comp);
            actv_comp.setAdapter(adapter);
        }
        catch(Exception error) {
            error.printStackTrace();
            new getBusComp().execute(new ApiConnector());
        }
    }

    public class ApiConnector {

        public JSONArray GetBusComp(){
            String url = "http://cyberweb.my/traquer/getBus.php";

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
    *///End ACTV
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case 1234:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getApplicationContext().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();
                    Bitmap yourSelectedImage = BitmapFactory.decodeFile(filePath);

				/* Now you have choosen image in Bitmap format in object "yourSelectedImage". You can use it in way you want! */
                }
            case ACTION_TAKE_PHOTO: {
                if (resultCode == RESULT_OK) {
                    handleBigCameraPhoto();
                }
                break;
            }
        }
    }
    public Button.OnClickListener btnPressedToCamera = new Button.OnClickListener(){
        @Override
        public void onClick(View v){
            dispatchTakePictureIntent(ACTION_TAKE_PHOTO);
        }
    };

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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.feedback_rate, menu);
        return true;
    }
    class InsertComment extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            String bus_id = inputBus.getText().toString().toUpperCase();
            bus_id = bus_id.replace(" ", "");
            String tcmmt = feedback_remarks.getText().toString();
            String bus_comp = actv_comp.getText().toString();
            String user_name = SaveSharedPreference.getUserName(Feedback_rate.this);
            String rate_re1 = Integer.toString(re1);
            String rate_re2 = Integer.toString(re2);
            String rate_re3 = Integer.toString(re3);
            String rate_re4 = Integer.toString(re4);
            String rate_re5 = Integer.toString(re5);
            String rate_rg1 = Integer.toString(rg1);
            String rate_rg2 = Integer.toString(rg2);
            String rate_rg3 = Integer.toString(rg3);
            String rate_rg4 = Integer.toString(rg4);
            String rate_rg5 = Integer.toString(rg5);
            String rate_rb1 = Integer.toString(rb1);
            String rate_rb2 = Integer.toString(rb2);
            String rate_rb3 = Integer.toString(rb3);
            String rate_rb4 = Integer.toString(rb4);
            String rate_rb5 = Integer.toString(rb5);
            String rdate = editCurrDate.getText().toString();
            String rtime = editCurrTime.getText().toString();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("bus_id", bus_id));
            params.add(new BasicNameValuePair("tcmmt", tcmmt));
            params.add(new BasicNameValuePair("bus_comp", bus_comp));
            params.add(new BasicNameValuePair("username", user_name));
            params.add(new BasicNameValuePair("re1", rate_re1));
            params.add(new BasicNameValuePair("re2", rate_re2));
            params.add(new BasicNameValuePair("re3", rate_re3));
            params.add(new BasicNameValuePair("re4", rate_re4));
            params.add(new BasicNameValuePair("re5", rate_re5));
            params.add(new BasicNameValuePair("rg1", rate_rg1));
            params.add(new BasicNameValuePair("rg2", rate_rg2));
            params.add(new BasicNameValuePair("rg3", rate_rg3));
            params.add(new BasicNameValuePair("rg4", rate_rg4));
            params.add(new BasicNameValuePair("rg5", rate_rg5));
            params.add(new BasicNameValuePair("rb1", rate_rb1));
            params.add(new BasicNameValuePair("rb2", rate_rb2));
            params.add(new BasicNameValuePair("rb3", rate_rb3));
            params.add(new BasicNameValuePair("rb4", rate_rb4));
            params.add(new BasicNameValuePair("rb5", rate_rb5));
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
                    Toast.makeText(Feedback_rate.this, "Failed to send. Please check your network connection.", Toast.LENGTH_SHORT).show();
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
                if(actv_comp.length() == 0){
                    Toast.makeText(Feedback_rate.this, "Bus Company is required!", Toast.LENGTH_SHORT).show();
                }else if(actv_comp.length() < 3) {
                    Toast.makeText(Feedback_rate.this, "Invalid Bus Company.", Toast.LENGTH_SHORT).show();
                }else if(rGroup1 == 0 || rGroup2 == 0 || rGroup3 == 0 || rGroup4 == 0 || rGroup5 == 0){
                        Toast.makeText(Feedback_rate.this, "Please Rate for the Bus Company.", Toast.LENGTH_SHORT).show();
                }else {
                    easyTracker.send(MapBuilder.createEvent("Feedback", "Send button pressed", "Feedback event", null).build());
                    Toast.makeText(Feedback_rate.this, "Rate Successful. Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                    new InsertComment().execute();
                }
            }else {
                Toast.makeText(Feedback_rate.this, "Failed to send. Please check your network connection.", Toast.LENGTH_SHORT).show();
            }
        }else if (id == android.R.id.home){
            easyTracker.send(MapBuilder.createEvent("Feedback", "back button pressed", "Feedback event", null).build());
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
            if(actv_comp.length() != 0){
                final AlertDialog.Builder alertBox = new AlertDialog.Builder(Feedback_rate.this);
                alertBox.setIcon(R.drawable.info_icon);
                alertBox.setCancelable(false);
                alertBox.setTitle("Do you want to cancel feedback?");
                alertBox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        // finish used for destroyed activity
                        easyTracker.send(MapBuilder.createEvent("Feedback", "Cancel Feedback (Yes)", "Feedback event", null).build());
                        finish();
                    }
                });

                alertBox.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        easyTracker.send(MapBuilder.createEvent("Feedback", "Cancel Feedback (No)", "Feedback event", null).build());
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
    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTION_TAKE_PHOTO: {
                if (resultCode == RESULT_OK) {
                    handleBigCameraPhoto();
                }
                break;
            }
        }
    }*/
    // Some lifecycle callbacks so that the image can survive orientation change
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(BITMAP_STORAGE_KEY, mImageBitmap);
        outState.putBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY, (mImageBitmap != null) );
        super.onSaveInstanceState(outState);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mImageBitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
    }
    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
    /** private void setBtnListenerOrDisable(
     ImageButton btn,
     Button.OnClickListener onClickListener,
     String intentName
     ) {
     if (isIntentAvailable(this, intentName)) {
     btn.setOnClickListener(onClickListener);
     } else {
     btn.setTextAlignment(getText(R.string.cannot).toString()+ "" + btn.getTextAlignment());
     btn.setClickable(false);
     /**btn.setText(
     getText(R.string.cannot).toString() + " " + btn.getText());
     btn.setClickable(false);
     }
     }  **/
}