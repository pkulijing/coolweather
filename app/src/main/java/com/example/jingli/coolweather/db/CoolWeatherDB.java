package com.example.jingli.coolweather.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.jingli.coolweather.model.City;
import com.example.jingli.coolweather.model.County;
import com.example.jingli.coolweather.model.Province;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jingli on 9/1/15.
 */
public class CoolWeatherDB {
    public static final String DB_NAME = "cool_weather";
    public static final int VERSION = 1;
    //static instance of this class. ensure only one instance exists.
    private static CoolWeatherDB coolWeatherDB;
    private SQLiteDatabase db;

    private CoolWeatherDB(Context context) {
        CoolWeatherOpenHelper dbHelper = new CoolWeatherOpenHelper(context, DB_NAME, null, VERSION);
        db = dbHelper.getWritableDatabase();
    }

    //synchronized prevents concurrent access. static synchronized method acquires intrinsic
    // lock of CoolWeatherDB.class. Ref: Core JAVA14.5.3-14.5.5
    public synchronized static CoolWeatherDB getInstance(Context context) {
        if(coolWeatherDB == null) {
            coolWeatherDB = new CoolWeatherDB(context);
        }
        return coolWeatherDB;
    }

    /*******************************************************************************/
    public void saveProvince(Province province) {
        if(province != null) {
            ContentValues values = new ContentValues();
            values.put("province_name", province.getName());
            values.put("province_code", province.getCode());
            db.insert("Province", null, values);
        }
    }

    public void saveCity(City city) {
        if(city != null) {
            ContentValues values = new ContentValues();
            values.put("city_name", city.getName());
            values.put("city_code", city.getCode());
            values.put("province_id", city.getProvinceId());
            db.insert("City", null, values);
        }
    }

    public void saveCounty(County county) {
        if(county != null) {
            ContentValues values = new ContentValues();
            values.put("county_name", county.getName());
            values.put("county_code", county.getCode());
            values.put("city_id", county.getCityId());
            db.insert("County", null, values);
        }
    }
    /*******************************************************************************/
    public List<Province> loadProvinces() {
        List<Province> provinces = new ArrayList<Province>();
        Cursor cursor = db.query("Province", null, null, null, null, null, null);
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                do {
                    Province province = new Province(
                            cursor.getInt(cursor.getColumnIndex("id")),
                            cursor.getString(cursor.getColumnIndex("province_name")),
                            cursor.getString(cursor.getColumnIndex("province_code")));
                    provinces.add(province);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return provinces;
    }

    public List<City> loadCities(int provinceId) {
        List<City> cities = new ArrayList<City>();
        Cursor cursor = db.query("City", null,
                "province_id = ?",
                new String[] { String.valueOf(provinceId) },
                null, null, null);
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                do {
                    City city = new City(
                            cursor.getInt(cursor.getColumnIndex("id")),
                            cursor.getString(cursor.getColumnIndex("city_name")),
                            cursor.getString(cursor.getColumnIndex("city_code")),
                            provinceId);
                    cities.add(city);
                } while(cursor.moveToNext());
            }
            cursor.close();
        }
        return cities;
    }

    public List<County> loadCounties(int cityId) {
        List<County> counties = new ArrayList<County>();
        Cursor cursor = db.query("County", null,
                "city_id = ?",
                new String[] { String.valueOf(cityId)},
                null, null, null);
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                do {
                    County county = new County(
                            cursor.getInt(cursor.getColumnIndex("id")),
                            cursor.getString(cursor.getColumnIndex("county_name")),
                            cursor.getString(cursor.getColumnIndex("county_code")),
                            cityId);
                    counties.add(county);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return counties;
    }

}
