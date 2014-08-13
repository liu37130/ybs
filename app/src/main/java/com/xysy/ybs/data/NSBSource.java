package com.xysy.ybs.data;


import com.xysy.ybs.type.JobInfo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NSBSource implements JobDataSource {
    private static NSBSource source;

    private final String BASE_URL = "http://www.nashangban.com";
    private final String SEARCH_URL = "/search?region=%d&keyword=%s&page=%d";

    public static NSBSource getSource() {
        if (source == null) {
            source = new NSBSource();
        }
        return source;
    }
    private NSBSource() {
    }

    @Override
    public String getSearchUrl(String keywords, String city, int page) {
        String url = null;
        try {
            String keywordsEncoded = URLEncoder.encode(keywords, "utf-8").replace("+", "%20");
            url = String.format(getAbsoluteUrl(SEARCH_URL), getCityId(city), keywordsEncoded, page);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }

    private int getCityId(String cityName) {
        if (CITY_IDS.containsKey(cityName)) {
            return CITY_IDS.get(cityName);
        }
        if (cityName == "") {

        }
        return -1;
    }

    public String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    @Override
    public ArrayList<JobInfo> parseJobsFromRaw(String html) {
        ArrayList<JobInfo> jobList = new ArrayList<JobInfo>();
        String cityName = getCityName(html);
        Document doc = Jsoup.parse(html);
        Elements jobs = doc.getElementsByClass("index-job-item");
        for (Element job : jobs) {
            String url = getAbsoluteUrl(job.attr("href"));
            String jobTitle = job.getElementsByClass("job-title").first().text();
            String company = job.getElementsByClass("name").first().text();
            String city = job.getElementsByClass("location").first().getElementsByTag("b").first().text();
            if (cityName != null && !city.equals(cityName)) {
                city = cityName + "-" + city;
            }
            Element salaryElement = job.getElementsByClass("salary-start").first();
            String salary = null;
            if (salaryElement != null) {
                salary = salaryElement.getElementsByTag("b").first().text();
            } else {
                salary = "面议";
            }
            String avatar_url = job.getElementsByClass("avatar").first().
                    getElementsByTag("img").first().attr("src");
            String date = "";
            jobList.add(new JobInfo(jobTitle, company, city, salary, url, date, avatar_url));
        }

        return jobList;
    }

    public String getCityName(String html) {
        String cityName = null;
        Document doc = Jsoup.parse(html);
        Elements blocks = doc.getElementsByClass("filter-line");
        for (Element block : blocks) {
            String type = block.getElementsByTag("span").first().text();
            if (type.equals("地区")) {
                Element active = block.getElementsByClass("active").first();
                if (active != null) {
                    cityName = active.text();
                }
            }
        }
        return cityName;
    }

    public String getTag() {
        return "哪上班";
    }

    private final Map<String, Integer> CITY_IDS = new HashMap<String, Integer>() {
        {
            put("北京", 1);
            put("上海", 115);
            put("广州", 267);
            put("深圳", 269);
            put("杭州", 150);
            put("天津", 20);
            put("石家庄", 40);
            put("太原", 52);
            put("呼和浩特", 64);
            put("包头", 65);
            put("沈阳", 77);
            put("大连", 78);
            put("长春", 92);
            put("哈尔滨", 102);
            put("南京", 136);
            put("苏州", 140);
            put("宁波", 151);
            put("合肥", 162);
            put("厦门", 181);
            put("济南", 202);
            put("青岛", 203);
            put("郑州", 220);
            put("武汉", 238);
            put("长沙", 252);
            put("珠海", 270);
            put("南宁", 289);
            put("海口", 304);
            put("重庆", 307);
            put("成都", 349);
            put("云南", 380);
            put("西安", 408);
            put("兰州", 417);
        }
    };
}
