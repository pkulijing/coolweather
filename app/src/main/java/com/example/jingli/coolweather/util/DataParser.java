package com.example.jingli.coolweather.util;

import android.text.TextUtils;

import com.example.jingli.coolweather.db.CoolWeatherDB;
import com.example.jingli.coolweather.model.City;
import com.example.jingli.coolweather.model.County;
import com.example.jingli.coolweather.model.Province;

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

}
