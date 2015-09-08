package com.example.jingli.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jingli.coolweather.R;
import com.example.jingli.coolweather.db.CoolWeatherDB;
import com.example.jingli.coolweather.model.City;
import com.example.jingli.coolweather.model.County;
import com.example.jingli.coolweather.model.Province;
import com.example.jingli.coolweather.util.DataParser;
import com.example.jingli.coolweather.util.HttpCallBackListener;
import com.example.jingli.coolweather.util.HttpUtil;

import java.util.ArrayList;
import java.util.List;

public class ChooseAreaActivity extends Activity {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    public static final int QUERY_PROVINCE = 0;
    public static final int QUERY_CITY = 1;
    public static final int QUERY_COUNTY = 2;

    private int currentLevel;

    private TextView titleText;
    private ProgressDialog progressDialog;

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    private CoolWeatherDB coolWeatherDB;

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    private Province selectedProvince;
    private City selectedCity;
    private County selectedCounty;

    private boolean isFromWeatherActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);

        if(!isFromWeatherActivity) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            Log.d("MyLog", "county_selected = " + prefs.getBoolean("county_selected", false));
            if(prefs.getBoolean("county_selected", false)) {
                Intent intent = new Intent(this, WeatherActivity.class);
                startActivity(intent);
                finish();
                return;//Still necessary although finish() has been called! check the log below to verify.
            }
        }

        //Log.d("MyLog", "Still coming here");
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);

        listView = (ListView)findViewById(R.id.list_view);
        titleText = (TextView) findViewById(R.id.title_text);

        coolWeatherDB = CoolWeatherDB.getInstance(this);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (currentLevel) {
                    case LEVEL_PROVINCE:
                        selectedProvince = provinceList.get(position);
                        queryCities();
                        break;
                    case LEVEL_CITY:
                        selectedCity = cityList.get(position);
                        queryCounties();
                        break;
                    case LEVEL_COUNTY:
                        selectedCounty = countyList.get(position);
                        Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
                        intent.putExtra("county_name", selectedCounty.getName());
                        ChooseAreaActivity.this.startActivity(intent);
                        finish();
                        break;
                    default:
                }
            }
        });
        queryProvinces();
    }

    private void queryProvinces() {
        provinceList = coolWeatherDB.loadProvinces();
        if(provinceList.size() > 0) {
            dataList.clear();
            for(Province province : provinceList) {
                dataList.add(province.getName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryFromServer("", QUERY_PROVINCE);
        }
    }

    private void queryCities() {
        cityList = coolWeatherDB.loadCities(selectedProvince.getId());
        if(cityList.size() > 0) {
            dataList.clear();
            for(City city : cityList) {
                dataList.add(city.getName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getName());
            currentLevel = LEVEL_CITY;
        } else {
            queryFromServer(selectedProvince.getCode(), QUERY_CITY);
        }
    }
    private void queryCounties() {
        countyList = coolWeatherDB.loadCounties(selectedCity.getId());
        if(countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getName());
            currentLevel = LEVEL_COUNTY;
        } else {
            queryFromServer(selectedCity.getCode(), QUERY_COUNTY);
        }
    }


    private void queryFromServer(final String code, final int type) {
        String address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
        showProgressDialog();
        HttpUtil.sendHttpRequest(address, new HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                boolean result = false;
                switch (type) {
                    case QUERY_PROVINCE:
                        result = DataParser.parseProvinceResponse(coolWeatherDB, response);
                        break;
                    case QUERY_CITY:
                        result = DataParser.parseCityResponse(coolWeatherDB, response, selectedProvince.getId());
                        break;
                    case QUERY_COUNTY:
                        result = DataParser.parseCountyResponse(coolWeatherDB, response, selectedCity.getId());
                        break;
                    default:
                }
                if(result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            switch (type) {
                                case QUERY_PROVINCE:
                                    queryProvinces();
                                    break;
                                case QUERY_CITY:
                                    queryCities();
                                    break;
                                case QUERY_COUNTY:
                                    queryCounties();
                                    break;
                                default:
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(
                                ChooseAreaActivity.this,
                                ChooseAreaActivity.this.getString(R.string.load_failed),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
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
    public void onBackPressed() {
        switch (currentLevel) {
            case LEVEL_COUNTY:
                queryCities();
                break;
            case LEVEL_CITY:
                queryProvinces();
                break;
            default:
                if(isFromWeatherActivity) {
                    Intent intent = new Intent(this, WeatherActivity.class);
                    startActivity(intent);
                }
                finish();
        }
    }
}
