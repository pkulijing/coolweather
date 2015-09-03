package com.example.jingli.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jingli.coolweather.R;
import com.example.jingli.coolweather.model.Weather;
import com.example.jingli.coolweather.service.UpdateWeatherService;
import com.example.jingli.coolweather.util.DataParser;
import com.example.jingli.coolweather.util.HttpCallBackListener;
import com.example.jingli.coolweather.util.HttpUtil;
import com.example.jingli.coolweather.util.MyApplication;


/**
 * Created by jingli on 9/1/15.
 */
public class WeatherActivity extends Activity implements View.OnClickListener{

    private Button switchCounty;
    private Button updateWeather;

    private TextView countyNameText;
    private TextView currentTimeText;
    private TextView condText;
    private TextView tmpText;
    private TextView aqiText;
    private TextView windText;
    private TextView humText;
    private TextView visText;
    private TextView presText;
    private TextView updateTimeText;

    private ProgressDialog progressDialog;

    private UpdateWeatherReceiver updateWeatherReceiver;
    private LocalBroadcastManager localBroadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);

        switchCounty = (Button) findViewById(R.id.switch_county);
        updateWeather = (Button) findViewById(R.id.update_weather);

        countyNameText = (TextView) findViewById(R.id.county_name);
        currentTimeText = (TextView) findViewById(R.id.current_time);
        condText = (TextView) findViewById(R.id.cond_text);
        tmpText = (TextView) findViewById(R.id.tmp_text);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        windText = (TextView) findViewById(R.id.wind_text);
        humText = (TextView) findViewById(R.id.hum_text);
        visText = (TextView) findViewById(R.id.vis_text);
        presText = (TextView) findViewById(R.id.pres_text);
        updateTimeText = (TextView) findViewById(R.id.update_time);

        switchCounty.setOnClickListener(this);
        updateWeather.setOnClickListener(this);

        String countyName = getIntent().getStringExtra("county_name");

        if(TextUtils.isEmpty(countyName)) {
            showWeather();
        } else {
            String weatherAddress = "http://apis.baidu.com/heweather/weather/free?city="
                    + countyName;
            showProgressDialog();
            queryFromServer(countyName, weatherAddress);
        }

        //Is it appropriate to start the service here?
        Intent intent = new Intent(this, UpdateWeatherService.class);
        startService(intent);

        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.jingli.coolweather.UPDATE_WEATHER");
        updateWeatherReceiver = new UpdateWeatherReceiver();
        localBroadcastManager.registerReceiver(updateWeatherReceiver, intentFilter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.switch_county:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();
                break;
            case R.id.update_weather:
                Log.d("MyLog", "manual update executed.");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String city = prefs.getString("city", "");

                if(!TextUtils.isEmpty(city)) {
                    String weatherInfoAddress = "http://apis.baidu.com/heweather/weather/free?city="
                            + city;
                    showProgressDialog();
                    queryFromServer(city, weatherInfoAddress);
                }
                break;
            default:
        }
    }

    private void queryFromServer(final String countyName, String address) {
        Log.d("MyLog", address);
        HttpUtil.sendHttpRequest(address, new HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                try {
                    DataParser.parseWeatherResponse(WeatherActivity.this, response, countyName);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                } catch (Exception e) {
                    onError(e);
                }
            }

            @Override
            public void onError(Exception e) {
                closeProgressDialog();
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(
                                WeatherActivity.this,
                                WeatherActivity.this.getString(R.string.load_failed),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showWeather() {
        closeProgressDialog();
        Weather weather = DataParser.retrieveWeatherInfo(this);

        countyNameText.setText(weather.city);

        currentTimeText.setText(weather.date + " " + weather.time);

        condText.setText(weather.cond_text);
        tmpText.setText(weather.tmp + "∘");
        aqiText.setText(weather.aqi + " " + weather.qlty);

        windText.setText(weather.wind_dir + weather.wind_sc + "级");
        humText.setText(getString(R.string.humidity) + " " + weather.hum + "%");
        visText.setText(getString(R.string.visibility) + " " + weather.vis + "km");
        presText.setText(getString(R.string.pressure) + " " + weather.pres + "hPa");

        updateTimeText.setText(this.getString(R.string.updated_at) + weather.update_loc);
    }

    private void showProgressDialog() {
        if(progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(this.getString(R.string.loading));
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if(progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.setWeatherInForeground(true);
        showWeather();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApplication.setWeatherInForeground(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(updateWeatherReceiver);
    }

    public class UpdateWeatherReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MyLog", "receivd broadcast!");
            showWeather();
        }
    }

}
