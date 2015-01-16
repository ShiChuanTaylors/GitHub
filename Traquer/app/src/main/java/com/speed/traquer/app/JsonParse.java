package com.speed.traquer.app;

/**
 * Created by ting on 12/8/14.
 */
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonParse {
    double current_latitude,current_longitude;
    private String url;
    private String keyWord;

    public JsonParse(String url, String keyWord)
    {
        this.url = url;
        this.keyWord = keyWord;
    }

    public String getUrl() {
        return this.url;
    }

    public JsonParse(double current_latitude,double current_longitude){
        this.current_latitude=current_latitude;
        this.current_longitude=current_longitude;
    }
    public List<SuggestGetSet> getParseJsonWCF(String sName)
    {
        List<SuggestGetSet> ListData = new ArrayList<SuggestGetSet>();
        try {
            String temp=sName.replace(" ", "%20");
            URL js = new URL(this.getUrl() + temp);
            URLConnection jc = js.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(jc.getInputStream()));
            String line = reader.readLine();
            JSONArray jsonArray = new JSONArray(line);
            //JSONObject jsonResponse = new JSONObject(line);
            //JSONArray jsonArray = jsonResponse.getJSONArray("location");

            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject r = jsonArray.getJSONObject(i);
                ListData.add(new SuggestGetSet(r.getString(this.keyWord)));
                Log.e("json array: ", String.valueOf(r));
            }
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return ListData;

    }

}
