package com.example.jingli.coolweather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.jingli.coolweather.service.UpdateWeatherService;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, UpdateWeatherService.class);
        context.startService(i);

    }
}
