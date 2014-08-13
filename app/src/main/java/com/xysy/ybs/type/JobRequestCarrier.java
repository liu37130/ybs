package com.xysy.ybs.type;

import android.os.Handler;

/**
 * Created by liu37130 on 14-7-27.
 */
public class JobRequestCarrier {
    private String city;
    private int page;
    private String keywords;
    private Handler handler;
    private int carrierSize;

    public JobRequestCarrier(String keywords, String city, int page, Handler handler) {
        this.page = page;
        this.city = city;
        this.keywords = keywords;
        this.handler = handler;
    }

    public void setCarrierSize(int size) {
        this.carrierSize = size;
    }

    public int getCarrierSize() {
        return carrierSize;
    }

    public int getPage() {
        return page;
    }

    public String getKeywords() {
        return keywords;
    }

    public String getCity() {
        return city;
    }

    public Handler getHandler() {
        return handler;
    }

}
