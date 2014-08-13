package com.xysy.ybs.type;

/**
 * Created by liu37130 on 14-7-27.
 */
public class JobInfo {
    private String jobTitle;
    private String company;
    private String city;
    private String salary;
    private String url;
    private String avatar_url;
    private String date;

    public JobInfo(String jobTitle, String company, String city, String salary, String url,
                   String date, String avatar_url) {
        this.jobTitle = jobTitle;
        this.city = city;
        this.company = company;
        this.salary = salary;
        this.url = url;
        this.date = date;
        this.avatar_url = avatar_url;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getCompany() {
        return company;
    }

    public String getCity() {
        return city;
    }

    public String getSalary() {
        return salary;
    }

    public String getUrl() {
        return url;
    }

    public String getAvatarUrl() {
        return avatar_url;
    }

    public String getDate() {
        return date;
    }
}
