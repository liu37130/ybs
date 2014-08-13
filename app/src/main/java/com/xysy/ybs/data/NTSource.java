package com.xysy.ybs.data;

import com.xysy.ybs.tools.Logger;
import com.xysy.ybs.type.JobInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class NTSource implements JobDataSource {

    private static NTSource source;
    private static final String BASE_URL = "http://www.neitui.me";
    private static final String SEARCH_URL = "/?dev=android&name=devapi&json=1&handle=jobs&city=%s&keyword=%s&page=%d";
    private static final String DETAIL_URL = "/j/";

    public static NTSource getSource() {
        if (source == null) {
            source = new NTSource();
        }
        return source;
    }
    private NTSource() {
    }

    @Override
    public String getSearchUrl(String keywords, String city, int page) {
        String url = null;
        try {
            String keywordsEncoded = URLEncoder.encode(keywords, "utf-8").replace("+", "%20");
            String cityEncoded = URLEncoder.encode(city, "utf-8");
            url = String.format(BASE_URL + SEARCH_URL, cityEncoded, keywordsEncoded, page);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return url;
    }

    @Override
    public ArrayList<JobInfo> parseJobsFromRaw(String json) {
        ArrayList<JobInfo> jobList = new ArrayList<JobInfo>();
        try {
            JSONObject data = new JSONObject(json);
            JSONArray jobs = data.getJSONArray("jobs");
            for (int i = 0; i < jobs.length(); i++) {
                JSONObject job = jobs.getJSONObject(i);
                String url = getJobDetailUrl(job.getString("id"));
                String avatar_url = job.getString("avatar");
                String city = job.getString("city");
                String company = job.getString("department");
                String jobTitle = job.getString("position");
                String salary = job.getString("beginsalary") + "K~" + job.getString("endsalary") + "k";
                String date = job.getString("createdate");
                jobList.add(new JobInfo(jobTitle, company, city, salary, url, date, avatar_url));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jobList;
    }

    @Override
    public String getTag() {
        return "内推网";
    }

    private String getJobDetailUrl(String id) {
        return BASE_URL + DETAIL_URL + id;
    }

}
