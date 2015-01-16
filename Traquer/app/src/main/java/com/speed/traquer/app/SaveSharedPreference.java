package com.speed.traquer.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by ting on 11/29/14.
 */
public class  SaveSharedPreference {
    static final String PREF_USER_NAME= "username";
    static final String PREF_INT= "key";
    static final String OUR_INFO = "active";


    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setUserName(Context ctx, String userName)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_NAME, userName);
        editor.commit();
    }

    public static String getUserName(Context ctx)
    {
        return getSharedPreferences(ctx).getString(PREF_USER_NAME, "");
    }

    public static void clearUserName(Context ctx)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.clear(); //clear all stored data
        editor.commit();
    }

    public static void setNotification(Context ctx, int i)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putInt(PREF_INT, i);
        editor.commit();
    }

    public static int getNotification(Context ctx)
    {
        return getSharedPreferences(ctx).getInt(PREF_INT, 0);
    }

    public static void setSpamNotification(Context ctx, boolean i)
    {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putBoolean(OUR_INFO, i);
        editor.commit();
    }

    public static Boolean getSpamNotification(Context ctx)
    {
        return getSharedPreferences(ctx).getBoolean(OUR_INFO,false);
    }
}
