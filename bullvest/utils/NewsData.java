package com.target.android.bullvest.utils;

/**
 * Created by Ashwin on 24/05/2018.
 */

/**
 * An {@link NewsData} object contains information related to a single article.
 */
public class NewsData {
    private String mHeadline, mSource, mUrl, mDatetime;

    /**
     * Constructs a new {@link NewsData} object.
     *
     * @param headline is the symbol of the Stock
     * @param source is the company's name
     * @param url is price of the stock
     * @param datetime is change in price from previous close
     */
    public NewsData(String headline, String source, String url, String datetime) {
        mHeadline = headline;
        mSource = source;
        mUrl = url;
        mDatetime = datetime;
    }

    /**
     * Constructs a new empty {@link NewsData} object.
     */
    public NewsData() {}

    /* Getter Methods */
    public String getHeadline() {
        return mHeadline;
    }

    public String getSource() {
        return mSource;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getDatetime() {
        return mDatetime;
    }

}
