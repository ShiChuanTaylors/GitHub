package com.speed.traquer.app;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.Handler;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 *
 */
public class BusFragment extends Fragment {

    private EasyTracker easyTracker = null;
    private ProgressDialog dialog;
    ArrayList<rateResult> rateResultArrayList = new ArrayList<rateResult>();
    class rateResult{
        public String compcode;
        public String tratept;
        public String tratebd;
        public String  tre1, tre2, tre3, tre4, tre5;
        public String trg1, trg2, trg3, trg4, trg5;
        public String trb1, trb2, trb3, trb4, trb5;

        public int jsonCount;
    }

    FancyAdapter fa = null;
    //TextView goodResult;
    //TextView badResult;
    //TextView busCompany;
    TextView txtDate;
    ListView listResult;
    double rectWidth;
    final Calendar c = Calendar.getInstance();
    public BusFragment() {
        // Required empty public constructor
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
            fa = new FancyAdapter();
            listResult.setAdapter(fa);
            listResult.setOnItemClickListener(onListClick);
        }
    }
    private void setTextToTextView(JSONArray jsonArray) {
        //String goodPoint ="";
        //String badPoint ="";

        for (int i = 0; i < jsonArray.length(); i++){
            JSONObject json;
            try{
                json = jsonArray.getJSONObject(i);
                rateResult resultRow = new rateResult();
                resultRow.compcode = json.getString("compcode");
                resultRow.tratept = json.getString("tratept");
                resultRow.tratebd = json.getString("tratebd");
                resultRow.tre1 = json.getString("tre1");
                resultRow.tre2 = json.getString("tre2");
                resultRow.tre3 = json.getString("tre3");
                resultRow.tre4 = json.getString("tre4");
                resultRow.tre5 = json.getString("tre5");
                resultRow.trg1 = json.getString("trg1");
                resultRow.trg2 = json.getString("trg2");
                resultRow.trg3 = json.getString("trg3");
                resultRow.trg4 = json.getString("trg4");
                resultRow.trg5 = json.getString("trg5");
                resultRow.trb1 = json.getString("trb1");
                resultRow.trb2 = json.getString("trb2");
                resultRow.trb3 = json.getString("trb3");
                resultRow.trb4 = json.getString("trb4");
                resultRow.trb5 = json.getString("trb5");
                resultRow.jsonCount = i;
                rateResultArrayList.add(resultRow);

               /* goodPoint = goodPoint +
                        //"Total Complaints " + json.getString("COUNT(*)") + "\n" +
                        json.getString("tratept");
                badPoint = badPoint +
                        json.getString("tratebd");*/
            } catch (JSONException e){
                e.printStackTrace();
            }
        }
        /*this.goodResult.setText(goodPoint);
        this.badResult.setText(badPoint);
        this.busCompany.setText(busComp);*/
        dialog.dismiss();
    }
    public ViewHolder holder;
    public int getViewPosition;
    class FancyAdapter extends ArrayAdapter<rateResult>{
        FancyAdapter(){
            super(getActivity(), R.layout.list_result, rateResultArrayList);
        }
        public View getView(int position, View convertView, ViewGroup parent){
            getViewPosition = position;
            View row = convertView;
            if(row == null){
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.list_result,parent,false);
                holder = new ViewHolder(row);
                row.setTag(holder);
                Log.d("Traq","Creating new row");
            }else{
                holder = (ViewHolder) row.getTag();
            }
            holder.populateFrom(rateResultArrayList.get(position));
            return row;
        }
    }

    class ViewHolder {
        public TextView bus_company;
        public TextView gdResult;
        public TextView bdResult;
        public ImageView rectGood;
        public ImageView rectBad;
        ViewHolder(View list_result){
            bus_company = (TextView) list_result.findViewById(R.id.busCompany);
            gdResult = (TextView) list_result.findViewById(R.id.goodResult);
            bdResult = (TextView) list_result.findViewById(R.id.badResult);
            rectGood = (ImageView) list_result.findViewById(R.id.rectGood);
            rectBad = (ImageView) list_result.findViewById(R.id.rectBad);

            String fontPath = "fonts/segoeuil.ttf";
            Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), fontPath);
            bus_company.setTypeface(tf);
            gdResult.setTypeface(tf);
            bdResult.setTypeface(tf);
        }
        void populateFrom(rateResult r){
            bus_company.setText(r.compcode);
            gdResult.setText(r.tratept);
            bdResult.setText(r.tratebd);
            int gResult = Integer.parseInt(r.tratept), bResult = Integer.parseInt(r.tratebd);
            int totalResult;
            DecimalFormat df = new DecimalFormat("#.##");
            double barResult;
            totalResult =  gResult + bResult;
            barResult = Double.valueOf(df.format(gResult)) / Double.valueOf(df.format(totalResult));
            rectGood.requestLayout();
            rectGood.getLayoutParams().width = (int) (rectWidth * barResult);
            Log.d("RectParamWidth2", String.valueOf(rectGood.getLayoutParams().width));

            int count = r.jsonCount % 5;
            if(count == 1){
                rectGood.setBackgroundColor(getResources().getColor(R.color.LightSkyBlue));
            }else if(count == 2){
                rectGood.setBackgroundColor(getResources().getColor(R.color.Orange));
            }else if(count == 3){
                rectGood.setBackgroundColor(getResources().getColor(R.color.Red));
            }else if(count == 4){
                rectGood.setBackgroundColor(getResources().getColor(R.color.Purple));
            }else{
                rectGood.setBackgroundColor(getResources().getColor(R.color.LimeGreen));
            }
            /*ViewGroup.LayoutParams params =  rectGood.getLayoutParams();
            params.width = rectGood.getWidth();
            rectGood.setLayoutParams(params);*/

        }
    }

    private AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener(){
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            easyTracker.send(MapBuilder.createEvent("BillBoard", "Bus List button pressed", "Billboard event", null).build());
            String comp = rateResultArrayList.get(position).compcode;
            String extra_tre1 = rateResultArrayList.get(position).tre1;
            String extra_tre2 = rateResultArrayList.get(position).tre2;
            String extra_tre3 = rateResultArrayList.get(position).tre3;
            String extra_tre4 = rateResultArrayList.get(position).tre4;
            String extra_tre5 = rateResultArrayList.get(position).tre5;
            String extra_trg1 = rateResultArrayList.get(position).trg1;
            String extra_trg2 = rateResultArrayList.get(position).trg2;
            String extra_trg3 = rateResultArrayList.get(position).trg3;
            String extra_trg4 = rateResultArrayList.get(position).trg4;
            String extra_trg5 = rateResultArrayList.get(position).trg5;
            String extra_trb1 = rateResultArrayList.get(position).trb1;
            String extra_trb2 = rateResultArrayList.get(position).trb2;
            String extra_trb3 = rateResultArrayList.get(position).trb3;
            String extra_trb4 = rateResultArrayList.get(position).trb4;
            String extra_trb5 = rateResultArrayList.get(position).trb5;

            Intent intent = new Intent(getActivity(),BusDetail.class);
            Bundle extras = new Bundle();
            extras.putString("compCode", comp);
            extras.putString("tre1", extra_tre1);
            extras.putString("tre2", extra_tre2);
            extras.putString("tre3", extra_tre3);
            extras.putString("tre4", extra_tre4);
            extras.putString("tre5", extra_tre5);
            extras.putString("trg1", extra_trg1);
            extras.putString("trg2", extra_trg2);
            extras.putString("trg3", extra_trg3);
            extras.putString("trg4", extra_trg4);
            extras.putString("trg5", extra_trg5);
            extras.putString("trb1", extra_trb1);
            extras.putString("trb2", extra_trb2);
            extras.putString("trb3", extra_trb3);
            extras.putString("trb4", extra_trb4);
            extras.putString("trb5", extra_trb5);
            intent.putExtras(extras);
            startActivity(intent);
        }
    };

    public class ApiConnector {
        public JSONArray GetResult(){
            String url = "http://cyberweb.my/traquer/getResult.php";
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        easyTracker = EasyTracker.getInstance(getActivity());
        dialog = ProgressDialog.show(getActivity(),"Loading","Retrieving Data, Please Wait");
        View view = inflater.inflate(R.layout.fragment_bus, container, false);

        txtDate = (TextView) view.findViewById(R.id.txtDate);
        String fontPath = "fonts/Square.ttf";
        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), fontPath);
        txtDate.setTypeface(tf);

        listResult = (ListView) view.findViewById(R.id.listResult);
        listResult.setClickable(true);
        final LinearLayout layout = (LinearLayout) view.findViewById(R.id.busFragLinear);
        ViewTreeObserver vto = layout.getViewTreeObserver();
        assert vto != null;
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int parentWidth  = layout.getMeasuredWidth();
                rectWidth = parentWidth;
                Log.d("RectWidth", String.valueOf(rectWidth));
            }
        });

        setCurrentDateOnView();
        if(isNetworkConnected()) {
            new getIdResult().execute(new ApiConnector());
        }else {
            Toast.makeText(getActivity(), "Failed to retrieve data. Please check your network connection.", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }
        return view;
    }

    @Override
    public void onStart(){
        super.onStart();

        EasyTracker.getInstance(getActivity()).activityStart(getActivity());

    }

    @Override
    public void onStop(){
        super.onStop();

        EasyTracker.getInstance(getActivity()).activityStop(getActivity());
    }

    private void setCurrentDateOnView() {
        String dateFormat = "yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat( dateFormat, Locale.US);
        txtDate.setText( sdf.format( c.getTime() ).toUpperCase() );
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            return false;
        } else {
            return true;
        }
    }


}