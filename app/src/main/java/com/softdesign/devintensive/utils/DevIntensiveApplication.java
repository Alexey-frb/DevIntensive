package com.softdesign.devintensive.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class DevIntensiveApplication extends Application {

    private static Context sContext;
    private static SharedPreferences sSharedPreferences;

    public static SharedPreferences getSharedPreferences() {
        return sSharedPreferences;
    }

    public static Context getContext() {
        return sContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sContext = getApplicationContext();
        sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }
}
