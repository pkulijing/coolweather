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
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jingli.coolweather.R;
import com.example.jingli.coolweather.model.Weather;
import com.example.jingli.coolweather.service.UpdateWeatherService;
import com.example.jingli.coolweather.util.DailyForecastAdapter;
import com.example.jingli.coolweather.util.DataParser;
import com.example.jingli.coolweather.util.HttpCallBackListener;
import com.example.jingli.coolweather.util.HttpUtil;
import com.example.jingli.coolweather.util.MyApplication;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class WeatherActivity extends Activity{

    private static final int EDIT_LIST = 1;
    private TextView currentTimeText;
    private TextView condText;
    private TextView tmpText;
    private TextView aqiText;
    private TextView windText;
    private TextView humText;
    private TextView visText;
    private TextView presText;
    private TextView updateTimeText;

    private RecyclerView dailyForecast;

    private ProgressDialog progressDialog;

    private SwipeRefreshLayout refreshLayout;

    private UpdateWeatherReceiver updateWeatherReceiver;
    private LocalBroadcastManager localBroadcastManager;

    private ListView leftDrawer;
    private List<String> citiesList = new ArrayList<>();

    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private TextView cityNameText;
    private ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);

        currentTimeText = (TextView) findViewById(R.id.current_time);
        condText = (TextView) findViewById(R.id.cond_text);
        tmpText = (TextView) findViewById(R.id.tmp_text);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        windText = (TextView) findViewById(R.id.wind_text);
        humText = (TextView) findViewById(R.id.hum_text);
        visText = (TextView) findViewById(R.id.vis_text);
        presText = (TextView) findViewById(R.id.pres_text);
        updateTimeText = (TextView) findViewById(R.id.update_time);

        dailyForecast = (RecyclerView) findViewById(R.id.daily_forecast);

        dailyForecast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        toolbar = (Toolbar) findViewById(R.id.weather_toolbar);
        cityNameText = (TextView) toolbar.findViewById(R.id.city_name_text);

        // TODO: 9/6/15:
        // 1. When the whole interface becomes larger than the screen, it will have to be
        // put into a scrollview wrapper. Touch event on the ScrollView and the RecyclerView will
        // intercept.
        // 2. Refresh should be enabled only when the top of the ScrollView is visible. Otherwise
        // the refresh event would be called when user tries to go back to the top of the ScrollView,
        // in which case it would be impossible to get back to the top.

        dailyForecast.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                int action = e.getAction();
                switch (action) {
                    case MotionEvent.ACTION_MOVE:
                        dailyForecast.getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                String city = prefs.getString("city", "");

                if(!TextUtils.isEmpty(city)) {
                    String weatherInfoAddress = "http://apis.baidu.com/heweather/weather/free?city="
                            + city;
                    queryFromServer(city, weatherInfoAddress);
                }
            }
        });

        String countyName = getIntent().getStringExtra("county_name");

        if(TextUtils.isEmpty(countyName)) {
            showWeather();
        } else {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
                    WeatherActivity.this).edit();
            editor.putString("city",  countyName);
            editor.apply();

            String weatherAddress = "http://apis.baidu.com/heweather/weather/free?city="
                    + countyName;
            showProgressDialog();
            queryFromServer(countyName, weatherAddress);
        }

        //Is it appropriate to start the service here?
        final Intent intent = new Intent(this, UpdateWeatherService.class);
        startService(intent);

        localBroadcastManager = LocalBroadcastManager.getInstance(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.jingli.coolweather.UPDATE_WEATHER");
        updateWeatherReceiver = new UpdateWeatherReceiver();
        localBroadcastManager.registerReceiver(updateWeatherReceiver, intentFilter);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        leftDrawer = (ListView) findViewById(R.id.left_drawer);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String citiesString = prefs.getString("all_cities", "");
        final String[] cities = citiesString.split(",");

        for(String city : cities) {
            citiesList.add(city);
        }
        citiesList.add(getString(R.string.edit_list));

        adapter = new ArrayAdapter<>(WeatherActivity.this, android.R.layout.simple_list_item_1, citiesList);
        leftDrawer.setAdapter(adapter);
        leftDrawer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("MyLog", "position = " + position + " " + citiesList.get(position));
                if (position == citiesList.size() - 1) {
                    Intent editLocationIntent = new Intent(WeatherActivity.this, EditLocationActivity.class);
                    startActivityForResult(editLocationIntent, EDIT_LIST);
                } else {
                    String cityName = citiesList.get(position);
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
                            WeatherActivity.this).edit();
                    editor.putString("city", cityName);
                    editor.apply();

                    String weatherAddress = "http://apis.baidu.com/heweather/weather/free?city="
                            + cityName;
                    showProgressDialog();
                    queryFromServer(cityName, weatherAddress);
                    drawerLayout.closeDrawer(leftDrawer);
                }

            }
        });
    }

    private void queryFromServer(final String countyName, String address) {
        //Log.d("MyLog", address);
        HttpUtil.sendHttpRequest(address, new HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                try {
                    DataParser.handleWeatherResponse(response, countyName);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(refreshLayout.isRefreshing()) {
                                refreshLayout.setRefreshing(false);
                            }
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
                        if(refreshLayout.isRefreshing()) {
                            refreshLayout.setRefreshing(false);
                        }
                    }
                });
            }
        });
    }

    private void showWeather() {
        closeProgressDialog();
        Weather weather = DataParser.retrieveWeather();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年M月d日 E HH:mm", Locale.CHINA);
        Date now = new Date();

        cityNameText.setText(weather.city);
        currentTimeText.setText(dateFormat.format(now));

        condText.setText(weather.cond_text);
        tmpText.setText(weather.tmp + "°");
        aqiText.setText(weather.aqi + " " + weather.qlty);

        windText.setText(weather.wind_dir + weather.wind_sc + "级");
        humText.setText(getString(R.string.humidity) + " " + weather.hum + "%");
        visText.setText(getString(R.string.visibility) + " " + weather.vis + "km");
        presText.setText(getString(R.string.pressure) + " " + weather.pres + "hPa");

        updateTimeText.setText(this.getString(R.string.updated_at) + weather.update_loc);

        dailyForecast.setAdapter(new DailyForecastAdapter(weather.dailyForecasts));

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
            showWeather();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case EDIT_LIST:
                if(resultCode == RESULT_OK) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    String citiesString = prefs.getString("all_cities", "");
                    String[] cities = citiesString.split(",");
                    citiesList.clear();
                    for (String city : cities) {
                        citiesList.add(city);
                    }
                    citiesList.add(getString(R.string.edit_list));
                    adapter.notifyDataSetChanged();
                }
                break;
            default:
        }

    }
}
