package com.xysy.ybs.tools;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.xysy.ybs.YApp;

public class NetworkUtils {
    public static boolean networkConnected() {
        ConnectivityManager connMgr = (ConnectivityManager)
                YApp.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isWifiConn = networkInfo.isConnected();
        networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isMobileConn = false;
        if(networkInfo != null)
        	isMobileConn = networkInfo.isConnected();

        return isWifiConn || isMobileConn;
    }
}
