package com.example.jingli.coolweather.model;

/**
 * Created by jingli on 9/1/15.
 */
public class County {
    private int id;
    private String name;
    private String code;
    private int cityId;

    public County(int id, String name, String code, int cityId) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.cityId = cityId;
    }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCode(String code) { this.code = code; }
    public void setCityId(int cityId) { this.cityId = cityId; }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public int getCityId() { return cityId; }
}
