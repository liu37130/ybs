package com.xysy.ybs.tools;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.widget.ImageView;

import com.android.volley.Cache;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ClearCacheRequest;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import com.xysy.ybs.YApp;
import com.xysy.ybs.data.DataHelper;
import com.xysy.ybs.data.DataHelper.JobsContract;
import com.xysy.ybs.data.DataProvider;
import com.xysy.ybs.data.JobDataSource;
import com.xysy.ybs.data.LGSource;
import com.xysy.ybs.data.NSBSource;
import com.xysy.ybs.data.NTSource;
import com.xysy.ybs.type.JobInfo;
import com.xysy.ybs.type.JobRequest;
import com.xysy.ybs.type.JobRequestCarrier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RequestCenter {

    /*
     * 返回请求的状态:NORMAL 包括正常有数据返回，链接出错两种情况
     *              NO_MORE_DATA 返回状态正常但是没有返回数据
     *              NO_NETWORK 没有网络链接
     */
    public static final int NORMAL = 0;
    public static final int NO_MORE_DATA = 1;
    public static final int NO_NETWORK = 2;

    private static RequestCenter mCenter;
    private Context mContext;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;

    //所有数据源，在registerDataSource()注册数据源
    private ArrayList<JobDataSource> mJobDataSources;

    //统计所有返回的请求，无论是否有数据，是否出错
    private Map<JobRequestCarrier, ArrayList<JobRequest>> mReturnedRequests;

    //统计没有返回数据的请求
    private Map<JobRequestCarrier, ArrayList<JobRequest>> mNoDataRequests;

    //统计出错的请求
    private Map<JobRequestCarrier, ArrayList<JobRequest>> mErrorRequests;

    private RequestCenter(Context context) {
        mContext = context;
        mRequestQueue = Volley.newRequestQueue(mContext);
        mImageLoader = new ImageLoader(mRequestQueue, new LruBitmapCache(mContext));
        mJobDataSources = new ArrayList<JobDataSource>();
        mReturnedRequests = new HashMap<JobRequestCarrier, ArrayList<JobRequest>>();
        mNoDataRequests = new HashMap<JobRequestCarrier, ArrayList<JobRequest>>();
        mErrorRequests = new HashMap<JobRequestCarrier, ArrayList<JobRequest>>();
        registerDataSource();
    }

    public static synchronized RequestCenter getCenter() {
        if (mCenter == null) {
            mCenter = new RequestCenter(YApp.getContext());
        }
        return mCenter;
    }

    private void registerDataSource() {
        mJobDataSources.add(NSBSource.getSource());
        mJobDataSources.add(LGSource.getSource());
        mJobDataSources.add(NTSource.getSource());
    }

    /**
     * 记录所有返回的请求，在JobRequest的deliverResponse()和deliverError()中调用
     * @param request 返回的请求
     * @param response 如果连接出错，response为null
     */
    public synchronized void addReturnedRequest(JobRequest request, ArrayList<JobInfo> response) {
        Logger.i("[RequestCenter] A request return / Source-" + request.getSource().getTag()
                + "; URL-" + request.getUrl());

        JobRequestCarrier carrier = request.getCarrier();
        checkFirstPageReturn(carrier);
        mReturnedRequests.get(carrier).add(request);
        if (response == null) {
            mErrorRequests.get(carrier).add(request);
        } else if (response.size() == 0) {
            mNoDataRequests.get(carrier).add(request);
        } else {
            new DataBaseAddTask().execute(response);
        }
        checkAllRequestsReturn(carrier);
    }

    private void checkFirstPageReturn(JobRequestCarrier carrier) {
        if (carrier.getPage() == 1
                && mReturnedRequests.get(carrier).size() == 0) {
            Logger.i("[RequestCenter] Loading the first page");
            new DataBaseDeleteTask().execute(carrier.getCity());
        }
    }

    private void checkAllRequestsReturn(JobRequestCarrier carrier) {
        if (mReturnedRequests.get(carrier).size() ==
                carrier.getCarrierSize()) {

            Logger.i("[RequestCenter] All request returned / CarrierInfo: City-" + carrier.getCity()
                    + "; Page-" + carrier.getPage() + "; KeyWords-" + carrier.getKeywords());

            Bundle args = new Bundle();
            Message msg = new Message();
            args.putInt("page", carrier.getPage());

            if (mNoDataRequests.get(carrier).size() == carrier.getCarrierSize()) {
                Logger.i("[RequestCenter] No more data for this carrier");
                msg.what = NO_MORE_DATA;
            } else {
                msg.what = NORMAL;
                ArrayList<String> errorList = new ArrayList<String>();
                for (JobRequest request : mErrorRequests.get(carrier)) {
                    errorList.add(request.getSource().getTag());
                }
                args.putStringArrayList("errorlist", errorList);
            }

            msg.setData(args);
            carrier.getHandler().sendMessage(msg);
            mReturnedRequests.remove(carrier);
            mNoDataRequests.remove(carrier);
            mErrorRequests.remove(carrier);
        }
    }

    public synchronized void getJobInfo(JobRequestCarrier carrier, Object tag) {
        if (!NetworkUtils.networkConnected()) {
            carrier.getHandler().sendEmptyMessage(NO_NETWORK);
            return;
        }

        carrier.setCarrierSize(mJobDataSources.size());
        initRequestRecorder(carrier);

        for (JobDataSource source : mJobDataSources) {
            String url = source.getSearchUrl(carrier.getKeywords(), carrier.getCity(),
                    carrier.getPage());
            JobRequest request = new JobRequest(url, this, source, carrier, null);
            request.setTag(tag);
            mRequestQueue.add(request);
        }

    }

    public void cancelAll(Object tag) {
        mRequestQueue.cancelAll(tag);
    }

    public void clearCache() {
        Cache cache = mRequestQueue.getCache();
        mRequestQueue.add(new ClearCacheRequest(cache, null));
    }

    private void initRequestRecorder(JobRequestCarrier carrier) {
        mReturnedRequests.put(carrier, new ArrayList<JobRequest>());
        mNoDataRequests.put(carrier, new ArrayList<JobRequest>());
        mErrorRequests.put(carrier, new ArrayList<JobRequest>());
    }

    public ImageLoader.ImageContainer loadImage(String url, ImageView view,
                                                int defaultImageResId, int errorImageResId) {
        return mImageLoader.get(url,
                ImageLoader.getImageListener(view, defaultImageResId, errorImageResId));
    }

    public class DataBaseAddTask extends AsyncTask<ArrayList<JobInfo>, Void, Void> {
        @Override
        protected Void doInBackground(ArrayList<JobInfo>... params) {
            ArrayList<JobInfo> list = params[0];
            ContentResolver resolver = mContext.getContentResolver();
            for (JobInfo job: list) {
                ContentValues values = new ContentValues();
                values.put(DataHelper.JobsContract.JOB_TITLE, job.getJobTitle());
                values.put(DataHelper.JobsContract.COMPANY, job.getCompany());
                values.put(DataHelper.JobsContract.CITY, job.getCity());
                values.put(DataHelper.JobsContract.SALARY, job.getSalary());
                values.put(DataHelper.JobsContract.URL, job.getUrl());
                values.put(DataHelper.JobsContract.AVATAR_URL, job.getAvatarUrl());
                values.put(DataHelper.JobsContract.DATE, job.getDate());
                values.put(JobsContract.STAR_STATE, JobsContract.NORMAL_JOB);
                resolver.insert(DataProvider.JOBS_URI, values);
            }
            return null;
        }
    }

    public class DataBaseDeleteTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String city = params[0];
            ContentResolver resolver = mContext.getContentResolver();
            resolver.delete(DataProvider.JOBS_URI,
                    JobsContract.CITY + " LIKE ? and " + JobsContract.STAR_STATE + "=?",
                    new String[] {"%" + city + "%", String.valueOf(JobsContract.NORMAL_JOB)});
            return null;
        }
    }
}
