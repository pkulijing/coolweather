package com.example.jingli.coolweather.util;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.jingli.coolweather.db.CoolWeatherDB;
import com.example.jingli.coolweather.model.City;
import com.example.jingli.coolweather.model.County;
import com.example.jingli.coolweather.model.Province;
import com.example.jingli.coolweather.model.Weather;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class DataParser {
    public synchronized static boolean parseProvinceResponse(CoolWeatherDB coolWeatherDB, String response) {
        if(!TextUtils.isEmpty(response)) {
            String[] provinces = response.split(",");
            if(provinces.length > 0) {
                for(String province : provinces) {
                    String[] parts = province.split("\\|");
                    Province p = new Province();
                    p.setCode(parts[0]);
                    p.setName(parts[1]);
                    coolWeatherDB.saveProvince(p);
                }
                return true;
            }
        }
        return false;
    }

    public synchronized static boolean parseCityResponse(CoolWeatherDB coolWeatherDB, String response, int provinceId) {
        if(!TextUtils.isEmpty(response)) {
            String[] cities = response.split(",");
            if(cities.length > 0) {
                for(String city : cities) {
                    City c = new City();
                    String[] parts = city.split("\\|");
                    c.setCode(parts[0]);
                    c.setName(parts[1]);
                    c.setProvinceId(provinceId);
                    coolWeatherDB.saveCity(c);
                }
                return true;
            }
        }
        return false;
    }

    public synchronized static boolean parseCountyResponse(CoolWeatherDB coolWeatherDB, String response, int cityId) {
        if(!TextUtils.isEmpty(response)) {
            String[] counties = response.split(",");
            if(counties.length > 0) {
                for(String county : counties) {
                    String[] parts = county.split("\\|");
                    County c = new County();
                    c.setCode(parts[0]);
                    c.setName(parts[1]);
                    c.setCityId(cityId);
                    coolWeatherDB.saveCounty(c);
                }
                return true;
            }
        }
        return false;
    }

    private static Weather parseWeatherJSONObject(JSONObject info) {
        Weather weather = new Weather();
        try {
            JSONObject basic = info.getJSONObject("basic");
            weather.city = basic.getString("city");
            weather.cnty = basic.getString("cnty");
            weather.id = basic.getString("id");
            weather.lat = basic.getString("lat");
            weather.lon = basic.getString("lon");

            JSONObject update = basic.getJSONObject("update");
            weather.update_loc = update.getString("loc");
            weather.update_utc = update.getString("utc");

            JSONObject now = info.getJSONObject("now");
            JSONObject cond = now.getJSONObject("cond");

            weather.cond_text = cond.getString("txt");
            weather.cond_code = cond.getString("code");
            weather.fl = now.getString("fl");
            weather.hum = now.getString("hum");
            weather.pcpn = now.getString("pcpn");
            weather.pres = now.getString("pres");
            weather.tmp = now.getString("tmp");
            weather.vis = now.getString("vis");
            JSONObject wind = now.getJSONObject("wind");
            weather.wind_deg = wind.getString("deg");
            weather.wind_dir = wind.getString("dir");
            weather.wind_sc = wind.getString("sc");
            weather.wind_spd = wind.getString("spd");

            JSONObject aqi_city = info.getJSONObject("aqi").getJSONObject("city");
            weather.aqi = aqi_city.getString("aqi");
            weather.qlty = aqi_city.getString("qlty");

            weather.dailyForecasts = new ArrayList<>();

            JSONArray dailyForecasts = info.getJSONArray("daily_forecast");
            for(int j = 0; j < dailyForecasts.length(); j++) {
                JSONObject dailyForecastJSON = dailyForecasts.getJSONObject(j);
                Weather.DailyForecast dailyForecast = new Weather.DailyForecast();
                dailyForecast.date = dailyForecastJSON.getString("date");
                JSONObject forecast_cond = dailyForecastJSON.getJSONObject("cond");
                dailyForecast.code_d = forecast_cond.getString("code_d");
                dailyForecast.code_n = forecast_cond.getString("code_n");
                dailyForecast.condition_d = forecast_cond.getString("txt_d");
                dailyForecast.condition_n = forecast_cond.getString("txt_n");
                JSONObject forecast_tmp = dailyForecastJSON.getJSONObject("tmp");
                dailyForecast.minTemp = forecast_tmp.getString("min");
                dailyForecast.maxTemp = forecast_tmp.getString("max");
                dailyForecast.pop = dailyForecastJSON.getString("pop");
                weather.dailyForecasts.add(dailyForecast);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return weather;
    }

    public static void handleWeatherResponse(String response, String countyName) {
        if (!TextUtils.isEmpty(response)) {
            try {
                //Log.d("MyLog", response);
                JSONObject data = new JSONObject(response);
                JSONArray weatherInfos = data.getJSONArray("HeWeather data service 3.0");
                //Log.d("MyLog", weatherInfos.length() + " data returned.");
                for(int i = 0; i < weatherInfos.length(); i++) {
                    JSONObject info = weatherInfos.getJSONObject(i);

                    JSONObject basic = info.getJSONObject("basic");
                    String city = basic.getString("city");
                    if(!city.equals(countyName)) {
                        continue;
                    }

                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit();
                    editor.putBoolean("county_selected", true);
                    editor.putString("city", city);
                    editor.putString("weatherJSONObject", info.toString());
                    editor.commit();
                    break;//The city has been found
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Weather retrieveWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        Weather weather = new Weather();
        try {
            JSONObject weatherJSONObject = new JSONObject(prefs.getString("weatherJSONObject", ""));
            weather = parseWeatherJSONObject(weatherJSONObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return weather;

    }

}
