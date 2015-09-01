package com.example.jingli.coolweather.model;

/**
 * Created by jingli on 9/1/15.
 */
public class Weather {

    private String countyName;
    private String weatherCode;
    private String type;
    private String lowTemperature;
    private String highTemperature;
    private String updateTime;
    private String date;
    private String time;

    public String getCountyName() { return countyName; }
    public String getWeatherCode() { return weatherCode; }
    public String getType() { return type; }
    public String getLowTemperature() { return lowTemperature; }
    public String getHighTemperature() { return highTemperature; }
    public String getUpdateTime() { return updateTime; }
    public String getDate() { return date; }
    public String getTime() { return time; }

    public void setCountyName(String countyName) { this.countyName = countyName; }
    public void setWeatherCode(String weatherCode) {this.weatherCode = weatherCode; }
    public void setType(String type) { this.type = type; }
    public void setLowTemperature(String lowTemperature) { this.lowTemperature = lowTemperature; }
    public void setHighTemperature(String highTemperature) { this.highTemperature = highTemperature; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
    public void setDate(String date) { this.date = date; }
    public void setTime(String time) { this.time = time; }

}
