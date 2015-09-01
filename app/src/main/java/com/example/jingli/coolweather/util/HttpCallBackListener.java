package com.example.jingli.coolweather.util;

/**
 * Created by jingli on 9/1/15.
 */
public interface HttpCallBackListener {
    void onFinish(String response);
    void onError(Exception e);
}
