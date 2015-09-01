package com.example.jingli.coolweather.db;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
/**
 * Created by jingli on 9/1/15.
 */
public class CoolWeatherOpenHelper extends SQLiteOpenHelper {

    public static final String CREATE_PROVINCE = "create table Province("
            + "id integer primary key autoincrement, "
            + "province_name text, "
            + "province_code text)";

    public static final String CREATE_CITY = "create table City("
            + "id integer primary key autoincrement, "
            + "city_name text, "
            + "city_code text, "
            + "province_id integer)";

    public static final String CREATE_COUNTY = "create table county("
            + "id integer primary key autoincrement, "
            + "county_name text, "
            + "county_code text, "
            + "city_id integer)";

    public CoolWeatherOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PROVINCE);
        db.execSQL(CREATE_CITY);
        db.execSQL(CREATE_COUNTY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}


}
