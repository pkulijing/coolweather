package com.example.jingli.coolweather.util;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {

    private static Context context;
    private static boolean weatherInForeground;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }

    public static boolean isWeatherInForeground() {
        return weatherInForeground;
    }

    public static void setWeatherInForeground(boolean weatherInForeground) {
        MyApplication.weatherInForeground = weatherInForeground;
    }

}
