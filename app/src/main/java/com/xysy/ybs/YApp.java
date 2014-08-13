package com.xysy.ybs;

import android.app.Application;
import android.content.Context;

import com.xysy.ybs.tools.Logger;

public class YApp extends Application{
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        Logger.DEBUG = true;
    }
    public static Context getContext() {
        return mContext;
    }
}
