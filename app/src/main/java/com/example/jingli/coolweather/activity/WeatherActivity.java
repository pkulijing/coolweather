package com.example.jingli.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jingli.coolweather.R;
import com.example.jingli.coolweather.model.Weather;
import com.example.jingli.coolweather.util.DataParser;
import com.example.jingli.coolweather.util.HttpCallBackListener;
import com.example.jingli.coolweather.util.HttpUtil;


/**
 * Created by jingli on 9/1/15.
 */
public class WeatherActivity extends Activity {

    private String countyCode;

    private TextView countyNameText;
    private TextView currentDateText;
    private TextView currentTimeText;
    private TextView temperatureText;
    private TextView weatherTypeText;
    private TextView updateTimeText;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);

        countyNameText = (TextView) findViewById(R.id.county_name);
        currentDateText = (TextView) findViewById(R.id.current_date);
        currentTimeText = (TextView) findViewById(R.id.current_time);
        temperatureText = (TextView) findViewById(R.id.temperature);
        weatherTypeText = (TextView) findViewById(R.id.weather_type);
        updateTimeText = (TextView) findViewById(R.id.update_time);

        countyCode = getIntent().getStringExtra("county_code");

        if(TextUtils.isEmpty(countyCode)) {
            showWeather();
        } else {
            String weatherCodeAddress = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
            showProgressDialog();
            queryFromServer(weatherCodeAddress, "weathercode");
        }
    }

    private void queryFromServer(String address, final String type) {
        HttpUtil.sendHttpRequest(address, new HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                if(type.equals("weatherinfo")) {
                    try {
                        DataParser.parseWeatherResponse(WeatherActivity.this, response);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showWeather();
                            }
                        });
                    } catch (Exception e) {
                        onError(e);
                    }
                } else if(type.equals("weathercode")) {
                    String[] parts = response.split("\\|");
                    String weatherCode = parts[1];
                    String weatherInfoAddress = "http://www.weather.com.cn/data/cityinfo/"
                            + weatherCode + ".html";
                    queryFromServer(weatherInfoAddress, "weatherinfo");
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
        countyNameText.setText(weather.getCountyName());
        currentDateText.setText(weather.getDate());
        currentTimeText.setText(weather.getTime());
        temperatureText.setText(weather.getLowTemperature() + " ~ "
                + weather.getHighTemperature());
        weatherTypeText.setText(weather.getType());
        updateTimeText.setText(this.getString(R.string.updated_at) + weather.getUpdateTime());
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

}
