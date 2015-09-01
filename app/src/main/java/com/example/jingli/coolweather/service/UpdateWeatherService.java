package com.example.jingli.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.jingli.coolweather.receiver.AlarmReceiver;
import com.example.jingli.coolweather.util.DataParser;
import com.example.jingli.coolweather.util.HttpCallBackListener;
import com.example.jingli.coolweather.util.HttpUtil;

/**
 * Created by jingli on 9/2/15.
 */
public class UpdateWeatherService extends Service {

    public static final int HALF_HOUR = 30 * 60 * 1000;

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
        Log.d("MyLog", "weather updated");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherCode = prefs.getString("weather_code", "");
        if(!TextUtils.isEmpty(weatherCode)) {
            String weatherInfoAddress = "http://www.weather.com.cn/data/cityinfo/"
                    + weatherCode + ".html";
            HttpUtil.sendHttpRequest(weatherInfoAddress, new HttpCallBackListener() {
                @Override
                public void onFinish(String response) {
                    try {
                        DataParser.parseWeatherResponse(UpdateWeatherService.this, response);
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
