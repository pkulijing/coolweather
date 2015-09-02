package com.example.jingli.coolweather.model;

/**
 * Created by jingli on 9/1/15.
 */

public class Weather {

    public String date;
    public String time;

    //basic 基本信息
    public String city;//城市
    public String cnty;//国家
    public String id;//id
    public String lat;//纬度
    public String lon;//经度
    public String update_loc;//更新时间，当地
    public String update_utc;//更新时间，utc

    //now 实况天气
    public String cond_code;//天气状况代码
    public String cond_text;//天气状况描述
    public String fl;//体感温度
    public String hum;//相对湿度（%）
    public String pcpn;//降水量（mm）
    public String pres;//气压
    public String tmp;//温度
    public String vis;//能见度（km）
    public String wind_deg;//风向（360度）
    public String wind_dir;//风向
    public String wind_sc;//风力
    public String wind_spd;//风速（kmph）

}
