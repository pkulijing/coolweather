package com.example.jingli.coolweather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.jingli.coolweather.service.UpdateWeatherService;

/**
 * Created by jingli on 9/2/15.
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, UpdateWeatherService.class);
        context.startService(i);

    }
}
