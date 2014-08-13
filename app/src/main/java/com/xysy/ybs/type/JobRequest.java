package com.xysy.ybs.type;

import android.location.GpsStatus;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.xysy.ybs.data.JobDataSource;
import com.xysy.ybs.tools.Logger;
import com.xysy.ybs.tools.RequestCenter;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by liu37130 on 14-7-27.
 */
public class JobRequest extends Request<ArrayList<JobInfo>> {
    private final JobDataSource mSource;
    private final JobRequestCarrier mCarrier;
    private final RequestCenter mCenter;

    public JobRequest(int method, String url, RequestCenter center,
                      JobDataSource source, JobRequestCarrier carrier,
                      ErrorListener errorListener) {
        super(method, url, errorListener);
        mCenter = center;
        mSource = source;
        mCarrier = carrier;

        Logger.i("[JobRequest] A new request Source is " + getSource().getTag());
    }

    public  JobRequest(String url, RequestCenter center,
                       JobDataSource source, JobRequestCarrier carrier,
                       Response.ErrorListener errorListener) {
        this(Method.GET, url, center, source, carrier, errorListener);
    }

    @Override
    public void deliverResponse(ArrayList<JobInfo> response) {
        mCenter.addReturnedRequest(this, response);

        Logger.i("[JobRequest] deliverResponse() Source is " + getSource().getTag());
    }

    @Override
    public void deliverError(VolleyError error) {
        mCenter.addReturnedRequest(this, null);

        Logger.i("[JobRequest] deliverError() Source is " + getSource().getTag());
    }

    @Override
    protected Response<ArrayList<JobInfo>> parseNetworkResponse(NetworkResponse response) {
        ArrayList<JobInfo> parsed = null;

        try {
            String rawData = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            parsed = mSource.parseJobsFromRaw(rawData);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Logger.i("[JobRequest] parseNetworkResponse() Source is " + getSource().getTag());
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }

    public JobRequestCarrier getCarrier() {
        return mCarrier;
    }

    public JobDataSource getSource() {
        return mSource;
    }
}
