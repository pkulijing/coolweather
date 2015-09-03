package com.example.jingli.coolweather.util;

import android.content.Context;
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

    public static void parseWeatherResponse(Context context, String response, String countyName) {
        if (!TextUtils.isEmpty(response)) {
            try {
                Log.d("MyLog", response);
                JSONObject data = new JSONObject(response);
                JSONArray weatherInfos = data.getJSONArray("HeWeather data service 3.0");
                Log.d("MyLog", weatherInfos.length() + " data returned.");
                Weather weather = new Weather();
                for(int i = 0; i < weatherInfos.length(); i++) {
                    JSONObject info = weatherInfos.getJSONObject(i);

                    JSONObject basic = info.getJSONObject("basic");
                    String city = basic.getString("city");
                    if(!city.equals(countyName)) {
                        continue;
                    }

                    weather.city = city;
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
                    break;//The city has been found
                }
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

        editor.putString("date", dateFormat.format(now));
        editor.putString("time", timeFormat.format(now));

        editor.putString("city", weather.city);
        editor.putString("cnty", weather.cnty);
        editor.putString("cnty",weather.id);
        editor.putString("lat", weather.lat);
        editor.putString("lon", weather.lon);
        editor.putString("updat_loc", weather.update_loc);
        editor.putString("update_utc", weather.update_utc);

        editor.putString("cond_code", weather.cond_code);
        editor.putString("cond_text", weather.cond_text);
        editor.putString("fl", weather.fl);
        editor.putString("hum", weather.hum);
        editor.putString("pcpn", weather.pcpn);
        editor.putString("pres", weather.pres);
        editor.putString("tmp", weather.tmp);
        editor.putString("vis", weather.vis);
        editor.putString("wind_deg", weather.wind_deg);
        editor.putString("wind_dir", weather.wind_dir);
        editor.putString("wind_sc", weather.wind_sc);
        editor.putString("wind_spd", weather.wind_spd);

        editor.putString("aqi", weather.aqi);
        editor.putString("qlty", weather.qlty);

        editor.commit();
    }

    public static Weather retrieveWeatherInfo(Context context) {
        Weather weather = new Weather();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        weather.date = prefs.getString("date", "");
        weather.time = prefs.getString("time", "");

        weather.city = prefs.getString("city", "");
        weather.cnty = prefs.getString("cnty", "");
        weather.id = prefs.getString("id", "");
        weather.lat = prefs.getString("lat", "");
        weather.lon = prefs.getString("lon", "");
        weather.update_loc = prefs.getString("updat_loc", "");
        weather.update_utc = prefs.getString("update_utc", "");

        weather.cond_code = prefs.getString("cond_code", "");
        weather.cond_text = prefs.getString("cond_text", "");
        weather.fl = prefs.getString("fl", "");
        weather.hum = prefs.getString("hum", "");
        weather.pcpn = prefs.getString("pcpn", "");
        weather.pres = prefs.getString("pres", "");
        weather.tmp = prefs.getString("tmp", "");
        weather.vis = prefs.getString("vis", "");
        weather.wind_deg = prefs.getString("wind_deg", "");
        weather.wind_dir = prefs.getString("wind_dir", "");
        weather.wind_sc = prefs.getString("wind_sc", "");
        weather.wind_spd = prefs.getString("wind_spd", "");

        weather.aqi = prefs.getString("aqi", "");
        weather.qlty = prefs.getString("qlty", "");

        return weather;
    }

}
