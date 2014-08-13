package com.xysy.ybs.tools;

import android.util.Log;

public class Logger {

    public static boolean DEBUG = false;
    public static String TAG = "YBSDebug";

    public static void i(String tag, String msg) {
        if (DEBUG)
            Log.i(tag, msg);
    }

    public static void i(String msg) {
        if (DEBUG)
            Log.i(TAG,msg);
    }

    public  static void e(String tag, String msg) {
        if (DEBUG)
            Log.e(tag,msg);
    }

    public static void e(String msg)  {
        if (DEBUG)
            Log.e(TAG,msg);
    }
}
