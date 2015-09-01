package com.example.jingli.coolweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.example.jingli.coolweather.db.CoolWeatherDB;
import com.example.jingli.coolweather.model.City;
import com.example.jingli.coolweather.model.County;
import com.example.jingli.coolweather.model.Province;
import com.example.jingli.coolweather.model.Weather;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by jingli on 9/1/15.
 */
public class DataParser {
    public synchronized static boolean parseProvinceResponse(CoolWeatherDB coolWeatherDB, String response) {
        if(!TextUtils.isEmpty(response)) {
            String[] provinces = response.split(",");
            if(provinces != null && provinces.length > 0) {
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
            if(cities != null && cities.length > 0) {
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
            if(counties != null && counties.length > 0) {
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

    public static void parseWeatherResponse(Context context, String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONObject data = new JSONObject(response);
                JSONObject weatherInfo = data.getJSONObject("weatherinfo");
                Weather weather = new Weather();
                weather.setCountyName(weatherInfo.getString("city"));
                weather.setWeatherCode(weatherInfo.getString("cityid"));
                weather.setHighTemperature(weatherInfo.getString("temp1"));
                weather.setLowTemperature(weatherInfo.getString("temp2"));
                weather.setType(weatherInfo.getString("weather"));
                weather.setUpdateTime(weatherInfo.getString("ptime"));
                saveWeatherInfo(context, weather);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveWeatherInfo(Context context, Weather weather) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年M月d日 E", Locale.CHINA);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH : mm", Locale.CHINA);
        Date now = new Date();

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("county_selected", true);
        editor.putString("county_name", weather.getCountyName());
        editor.putString("weather_code", weather.getWeatherCode());
        editor.putString("low_temperature", weather.getLowTemperature());
        editor.putString("high_temperature", weather.getHighTemperature());
        editor.putString("weather_type", weather.getType());
        editor.putString("update_time", weather.getUpdateTime());
        editor.putString("date", dateFormat.format(now));
        editor.putString("time", timeFormat.format(now));
        editor.commit();
    }

    public static Weather retrieveWeatherInfo(Context context) {
        Weather weather = new Weather();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        weather.setCountyName(prefs.getString("county_name", ""));
        weather.setWeatherCode(prefs.getString("weather_code", ""));
        weather.setLowTemperature(prefs.getString("low_temperature", ""));
        weather.setHighTemperature(prefs.getString("high_temperature", ""));
        weather.setType(prefs.getString("weather_type", ""));
        weather.setUpdateTime(prefs.getString("update_time", ""));
        weather.setDate(prefs.getString("date", ""));
        weather.setTime(prefs.getString("time", ""));
        return weather;
    }

}
