package com.example.administrator.myapplication;

import android.app.Application;

import com.iflytek.cloud.SpeechUtility;

/**
 * Created by Administrator on 2017/7/17.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        SpeechUtility.createUtility(this, "appid=" + "596d80eb");
        super.onCreate();
    }
}
