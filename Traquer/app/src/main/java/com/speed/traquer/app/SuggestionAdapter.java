package com.speed.traquer.app;

/**
 * Created by ting on 12/8/14.
 */

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.opengl.Visibility;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ProgressBar;

public class SuggestionAdapter extends ArrayAdapter<String> {

    protected static final String TAG = "SuggestionAdapter";
    private String url, keyWord;
    private List<String> suggestions;
    private Activity context;
    public SuggestionAdapter(Activity context, String nameFilter, String url, String keyWord) {
        super(context, R.layout.listcomp);
        this.context = context;
        suggestions = new ArrayList<String>();
        this.url = url;
        this.keyWord = keyWord;
    }

    private ProgressBar mLoadingIndicator;
    //private Context context;

    public void setLoadingIndicator(ProgressBar view) {
        mLoadingIndicator = view;
        //this.context = context;
    }

    @Override
    public int getCount() {
        return suggestions.size();
    }

    @Override
    public String getItem(int index) {
        return suggestions.get(index);
    }

    @Override
    public Filter getFilter() {
        Filter myFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    //stuff that updates ui
                        mLoadingIndicator.setVisibility(View.INVISIBLE);
                        mLoadingIndicator.setVisibility(View.VISIBLE);
                    }
                });
                JsonParse jp=new JsonParse(url, keyWord);
                if (constraint != null) {
                    // A class that queries a web API, parses the data and
                    // returns an ArrayList<GoEuroGetSet>
                    List<SuggestGetSet> new_suggestions =jp.getParseJsonWCF(constraint.toString());
                    suggestions.clear();
                    for (int i=0;i<new_suggestions.size();i++) {
                        suggestions.add(new_suggestions.get(i).getLocation());
                    }

                    // Now assign the values and count to the FilterResults
                    // object
                    filterResults.values = suggestions;
                    filterResults.count = suggestions.size();
                }
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //stuff that updates ui
                        mLoadingIndicator.setVisibility(View.INVISIBLE);
                    }
                });

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence contraint,
                                          FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return myFilter;
    }

}
