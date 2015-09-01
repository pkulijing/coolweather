package com.example.jingli.coolweather.model;

/**
 * Created by jingli on 9/1/15.
 */
public class City {
    private int id;
    private String name;
    private String code;
    private int provinceId;

    public City(int id, String name, String code, int provinceId) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.provinceId = provinceId;
    }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCode(String code) { this.code = code; }
    public void setProvinceId(int provinceId) { this.provinceId = provinceId; }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public int getProvinceId() { return provinceId; }
}
