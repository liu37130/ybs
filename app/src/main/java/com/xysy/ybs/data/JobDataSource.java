package com.xysy.ybs.data;

import com.xysy.ybs.type.JobInfo;

import java.util.ArrayList;

public interface JobDataSource {

    public String getSearchUrl(String keywords, String city, int page);

    public ArrayList<JobInfo> parseJobsFromRaw(String raw);

    //用于识别链接属于哪一个网站
    public String getTag();

}
