package com.speed.traquer.app;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import java.text.DecimalFormat;


public class BusDetail extends ActionBarActivity {

    private EasyTracker easyTracker = null;
    String passedComp = null;
    String [] arrayExcelPassed = new String[5];
    String [] arrayGoodPassed = new String[5];
    String [] arrayBadPassed = new String[5];

    private TextView txtComp = null;
    private TextView[] arrayTxtTre = new TextView[5];
    private TextView[] arrayTxtTrg = new TextView[5];
    private TextView[] arrayTxtTrb = new TextView[5];

    int rectWidth;

    ImageView[] arrayImgOrg = new ImageView[5];
    ImageView[] arrayImgYellow = new ImageView[5];
    ImageView[] arrayImgRed = new ImageView[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        easyTracker = EasyTracker.getInstance(BusDetail.this);
        Bundle extras = getIntent().getExtras();
        passedComp = extras.getString("compCode");
        arrayExcelPassed[0] = extras.getString("tre1");
        arrayExcelPassed[1] = extras.getString("tre2");
        arrayExcelPassed[2] = extras.getString("tre3");
        arrayExcelPassed[3] = extras.getString("tre4");
        arrayExcelPassed[4] = extras.getString("tre5");
        arrayGoodPassed[0] = extras.getString("trg1");
        arrayGoodPassed[1] = extras.getString("trg2");
        arrayGoodPassed[2] = extras.getString("trg3");
        arrayGoodPassed[3] = extras.getString("trg4");
        arrayGoodPassed[4] = extras.getString("trg5");
        arrayBadPassed[0] = extras.getString("trb1");
        arrayBadPassed[1] = extras.getString("trb2");
        arrayBadPassed[2] = extras.getString("trb3");
        arrayBadPassed[3] = extras.getString("trb4");
        arrayBadPassed[4] = extras.getString("trb5");

        txtComp = (TextView) findViewById(R.id.comp_list);
        txtComp.setText(passedComp);
        arrayTxtTre[0] = (TextView) findViewById(R.id.list_tre1);
        arrayTxtTre[1] = (TextView) findViewById(R.id.list_tre2);
        arrayTxtTre[2] = (TextView) findViewById(R.id.list_tre3);
        arrayTxtTre[3]  = (TextView) findViewById(R.id.list_tre4);
        arrayTxtTre[4]  = (TextView) findViewById(R.id.list_tre5);

        arrayTxtTrg[0] = (TextView) findViewById(R.id.list_trg1);
        arrayTxtTrg[1] = (TextView) findViewById(R.id.list_trg2);
        arrayTxtTrg[2] = (TextView) findViewById(R.id.list_trg3);
        arrayTxtTrg[3]  = (TextView) findViewById(R.id.list_trg4);
        arrayTxtTrg[4]  = (TextView) findViewById(R.id.list_trg5);

        arrayTxtTrb[0] = (TextView) findViewById(R.id.list_trb1);
        arrayTxtTrb[1] = (TextView) findViewById(R.id.list_trb2);
        arrayTxtTrb[2] = (TextView) findViewById(R.id.list_trb3);
        arrayTxtTrb[3]  = (TextView) findViewById(R.id.list_trb4);
        arrayTxtTrb[4]  = (TextView) findViewById(R.id.list_trb5);

        for(int i = 0; i < 5; i++){
            arrayTxtTre[i].setText(arrayExcelPassed[i]);
            arrayTxtTrg[i].setText(arrayGoodPassed[i]);
            arrayTxtTrb[i].setText(arrayBadPassed[i]);
        }

        String fontPath = "fonts/segoeuil.ttf";
        Typeface tf = Typeface.createFromAsset(getAssets(), fontPath);
        txtComp.setTypeface(tf);

        arrayImgOrg[0] = (ImageView) findViewById(R.id.rectOrg1);
        arrayImgYellow[0] = (ImageView) findViewById(R.id.rectYellow1);
        arrayImgRed[0] = (ImageView) findViewById(R.id.rectRed1);

        arrayImgOrg[1] = (ImageView) findViewById(R.id.rectOrg2);
        arrayImgYellow[1] = (ImageView) findViewById(R.id.rectYellow2);
        arrayImgRed[1] = (ImageView) findViewById(R.id.rectRed2);

        arrayImgOrg[2] = (ImageView) findViewById(R.id.rectOrg3);
        arrayImgYellow[2] = (ImageView) findViewById(R.id.rectYellow3);
        arrayImgRed[2] = (ImageView) findViewById(R.id.rectRed3);

        arrayImgOrg[3] = (ImageView) findViewById(R.id.rectOrg4);
        arrayImgYellow[3] = (ImageView) findViewById(R.id.rectYellow4);
        arrayImgRed[3] = (ImageView) findViewById(R.id.rectRed4);

        arrayImgOrg[4] = (ImageView) findViewById(R.id.rectOrg5);
        arrayImgYellow[4] = (ImageView) findViewById(R.id.rectYellow5);
        arrayImgRed[4] = (ImageView) findViewById(R.id.rectRed5);

        final LinearLayout layout = (LinearLayout) findViewById(R.id.linearDetail);
        ViewTreeObserver vto = layout.getViewTreeObserver();
        assert vto != null;
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int parentWidth  = layout.getMeasuredWidth();
                rectWidth = parentWidth;
                Log.d("RectWidth", String.valueOf(rectWidth));

                drawWidth();
            }
        });

    }

    private void drawWidth() {

        for(int i = 0; i < 5; i++) {
            int eResult = Integer.parseInt(arrayExcelPassed[i]), gResult = Integer.parseInt(arrayGoodPassed[i]), bResult = Integer.parseInt(arrayBadPassed[i]);
            int totalResult;
            DecimalFormat df = new DecimalFormat("#.##");
            double barGoodResult, barBadResult, barExcelResult;
            totalResult = gResult + bResult + eResult;

            barBadResult = Double.valueOf(df.format(bResult)) / Double.valueOf(df.format(totalResult));
            barExcelResult = Double.valueOf(df.format(eResult)) / Double.valueOf(df.format(totalResult));
            barGoodResult = Double.valueOf(df.format(gResult)) / Double.valueOf(df.format(totalResult));

            arrayImgYellow[i].requestLayout();
            arrayImgYellow[i].getLayoutParams().width = (int) (rectWidth * barExcelResult);

            arrayImgOrg[i].requestLayout();
            arrayImgOrg[i].getLayoutParams().width = (int) (rectWidth * barGoodResult) + arrayImgYellow[i].getLayoutParams().width;
            Log.d("rectOrg1", String.valueOf(arrayImgOrg[i].getLayoutParams().width));

            arrayImgRed[i].requestLayout();
            arrayImgRed[i].getLayoutParams().width = (int) (rectWidth * barBadResult) + arrayImgOrg[i].getLayoutParams().width + arrayImgYellow[i].getLayoutParams().width;
            Log.d("rectRed1", String.valueOf(arrayImgRed[i].getLayoutParams().width));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bus_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home){
            easyTracker.send(MapBuilder.createEvent("Bus Detail", "back button pressed", "Bus Detail event", null).build());
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showDialog(View view) {
        easyTracker.send(MapBuilder.createEvent("BusDetail", "Comments shown", "BusDetail event", null).build());
        Intent intent = new Intent(getApplicationContext(),ListCmmt.class);
        intent.putExtra("compcode",passedComp);
        startActivity(intent);
        //CommentDialogList commentDL = new CommentDialogList();
        //commentDL.show(getSupportFragmentManager(),"Comment");

    }
}
