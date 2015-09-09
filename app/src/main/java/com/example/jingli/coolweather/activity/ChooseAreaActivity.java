package com.example.jingli.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
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
import java.util.Collections;
import java.util.List;

public class ChooseAreaActivity extends Activity {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    public static final int QUERY_PROVINCE = 0;
    public static final int QUERY_CITY = 1;
    public static final int QUERY_COUNTY = 2;

    private int currentLevel;

    private ProgressDialog progressDialog;

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    private TextView nameText;

    private CoolWeatherDB coolWeatherDB;

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    private Province selectedProvince;
    private City selectedCity;
    private County selectedCounty;

    private boolean isFromEditLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFromEditLocation = getIntent().getBooleanExtra("from_edit_location", false);

        if(!isFromEditLocation) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if(!prefs.getString("city", "").equals("")) {
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
        Toolbar toolbar = (Toolbar) findViewById(R.id.choose_area_toolbar);
        nameText = (TextView) toolbar.findViewById(R.id.name_text);

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
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ChooseAreaActivity.this);
                        String citiesString = prefs.getString("all_cities", "");
                        String[] cities = citiesString.split(",");
                        List<String> citiesList = new ArrayList<>();
                        Collections.addAll(citiesList, cities);


                        String county_name = selectedCounty.getName();
                        if(!citiesList.contains(county_name)) {
                            SharedPreferences.Editor editor = prefs.edit();
                            if(citiesString.equals("")) {
                                editor.putString("all_cities", county_name);
                            } else {
                                editor.putString("all_cities", citiesString + "," + county_name);
                            }
                            editor.apply();
                        }

                        if(isFromEditLocation) {
                            Intent intent = new Intent();
                            intent.putExtra("new_city_name", county_name);
                            setResult(RESULT_OK, intent);
                        } else {
                            Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
                            intent.putExtra("county_name", county_name);
                            ChooseAreaActivity.this.startActivity(intent);
                        }
                        finish();
                        break;
                    default:
                }
            }
        });
        queryProvinces();
    }

    private void queryProvinces() {
        nameText.setText(getString(R.string.china));
        provinceList = coolWeatherDB.loadProvinces();
        if(provinceList.size() > 0) {
            dataList.clear();
            for(Province province : provinceList) {
                dataList.add(province.getName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryFromServer("", QUERY_PROVINCE);
        }
    }

    private void queryCities() {
        nameText.setText(selectedProvince.getName());
        cityList = coolWeatherDB.loadCities(selectedProvince.getId());
        if(cityList.size() > 0) {
            dataList.clear();
            for(City city : cityList) {
                dataList.add(city.getName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            queryFromServer(selectedProvince.getCode(), QUERY_CITY);
        }
    }
    private void queryCounties() {
        nameText.setText(selectedCity.getName());
        countyList = coolWeatherDB.loadCounties(selectedCity.getId());
        if(countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
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
                finish();
        }
    }
}
