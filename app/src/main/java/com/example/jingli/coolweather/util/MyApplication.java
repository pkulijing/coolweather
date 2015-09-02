package com.example.jingli.coolweather.util;

import android.app.Application;
import android.content.Context;

/**
 * Created by jingli on 9/2/15.
 */
public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }

}
