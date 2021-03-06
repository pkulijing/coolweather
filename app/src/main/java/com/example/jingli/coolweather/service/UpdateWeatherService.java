package com.example.jingli.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.jingli.coolweather.receiver.AlarmReceiver;
import com.example.jingli.coolweather.util.DataParser;
import com.example.jingli.coolweather.util.HttpCallBackListener;
import com.example.jingli.coolweather.util.HttpUtil;
import com.example.jingli.coolweather.util.MyApplication;

public class UpdateWeatherService extends Service {

    public static final int HALF_HOUR = 30 * 60 * 1000;
    //public static final int HALF_MINUTE = 30 * 1000; //For debug use.

    private LocalBroadcastManager localBroadcastManager;

    @Override
    public void onCreate() {
        super.onCreate();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWeather();
            }
        }).start();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Long triggerAtTime = SystemClock.elapsedRealtime() + HALF_HOUR;
        Intent i = new Intent(this, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent,flags,startId);
    }

    private void updateWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String cityName = prefs.getString("city", "");
        if(!TextUtils.isEmpty(cityName)) {
            String weatherInfoAddress = "http://apis.baidu.com/heweather/weather/free?city="
                    + cityName;
            HttpUtil.sendHttpRequest(weatherInfoAddress, new HttpCallBackListener() {
                @Override
                public void onFinish(String response) {
                    try {
                        DataParser.handleWeatherResponse(response, cityName);
                        if(MyApplication.isWeatherInForeground()) {
                            Intent intent = new Intent("com.example.jingli.coolweather.UPDATE_WEATHER");
                            localBroadcastManager.sendBroadcast(intent);
                        }
                        //check if it is foreground. If so, update UI.
                    } catch (Exception e) {
                        onError(e);
                    }
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
