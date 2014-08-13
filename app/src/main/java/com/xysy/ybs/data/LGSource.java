package com.xysy.ybs.data;

import com.xysy.ybs.tools.Logger;
import com.xysy.ybs.type.JobInfo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class LGSource implements JobDataSource {

    private static LGSource source;
    private static final String BASE_URL = "http://www.lagou.com";
    private static final String SEARCH_URL = "/jobs/list_%s?kd=%s&city=%s&pn=%d";

    public static LGSource getSource() {
        if (source == null) {
            source = new LGSource();
        }
        return source;
    }

    private LGSource() {

    }

    @Override
    public String getSearchUrl(String keywords, String city, int page) {
        String url = null;
        try {
            String keywordsEncoded = URLEncoder.encode(keywords, "utf-8").replace("+", "%20");
            String cityEncoded = URLEncoder.encode(city, "utf-8");
            url = String.format(BASE_URL + SEARCH_URL,
                    keywordsEncoded, keywordsEncoded, cityEncoded, page);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }

    @Override
    public ArrayList<JobInfo> parseJobsFromRaw(String html) {
        ArrayList<JobInfo> jobList = new ArrayList<JobInfo>();
        Document doc = Jsoup.parse(html);
        Elements jobs = doc.getElementsByClass("clearfix");
        for (Element job: jobs) {
            Element l = job.getElementsByClass("hot_pos_l").first();
            Element base = l.getElementsByClass("mb10").first();
            String url = base.getElementsByTag("a").first().attr("href");
            String jobTitle = base.getElementsByTag("a").first().text();
            String city = base.getElementsByClass("c9").first().text();
            city = city.substring(1, city.length()-1);
            String salary = l.getElementsByTag("span").get(1).text();
            salary = salary.substring(3, salary.length());
            String date = l.getElementsByTag("span").last().text();
            Element r = job.getElementsByClass("hot_pos_r").first();
            String company = r.getElementsByClass("mb10").first().text();

            jobList.add(new JobInfo(jobTitle, company,city, salary, url, date, null));
        }
        return jobList;

    }

    public String getTag() {
        return "拉勾网";
    }
}
