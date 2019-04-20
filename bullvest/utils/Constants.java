package com.target.android.bullvest.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Ashwin on 27/04/2018.
 */

public class Constants {
    /* Default Stock (aapl) */
    public static final String IEX_DEFAULT_SYMBOL = "aapl";

    /* Stock, Chart and News information */
    public static final String IEX_SYMBOL = "symbol";
    public static final String IEX_COMPANY_NAME = "companyName";
    public static final String IEX_EXCHANGE = "primaryExchange";
    public static final String IEX_SECTOR = "sector";
    public static final String IEX_PRICE = "latestPrice";
    public static final String IEX_PRICE_CHANGE = "change";
    public static final String IEX_PERCENT_CHANGE = "changePercent";
    public static final String IEX_OPEN = "open";
    public static final String IEX_PREVIOUS_CLOSE = "previousClose";
    public static final String IEX_HIGH = "high";
    public static final String IEX_LOW = "low";
    public static final String IEX_CLOSE = "close";
    public static final String IEX_PE_RATIO = "peRatio";
    public static final String IEX_WEEK_52_HIGH = "week52High";
    public static final String IEX_WEEK_52_LOW = "week52Low";
    public static final String IEX_MARKET_CAP = "marketCap";
    public static final String IEX_MARKET_AVERAGE = "marketAverage";
    public static final String IEX_HEADLINE = "headline";
    public static final String IEX_SOURCE = "source";
    public static final String IEX_URL = "url";
    public static final String IEX_DATETIME = "datetime";

    /* URL for stock data from the IEX dataset */
    public static String IEX_REQUEST_URL =
            "https://api.iextrading.com/1.0/stock/";

    /* Paths of the Gainers, Loser, and Most Active Http URL */
    public static String IEX_GAINERS_PATH = "market/list/gainers";
    public static String IEX_LOSERS_PATH = "market/list/losers";
    public static String IEX_MOST_ACTIVE_PATH = "market/list/mostactive";

    /* Path of Individual Stocks Http URL */
    public static String IEX_INDIVIDUAL_PATH_END = "/quote";

    /* Path of the Chart Points Http URL */
    public static String IEX_DAILY_PATH_END = "/chart/1d";
    public static String IEX_MONTHLY_PATH_END = "/chart/1m";
    public static String IEX_SIX_MONTHS_PATH_END = "/chart/6m";
    public static String IEX_YEARLY_PATH_END = "/chart/1y";
    public static String IEX_FIVE_YEARS_PATH_END = "/chart/5y";

    /* Path of News Data Http URL */
    public static String IEX_NEWS_PATH = "/news/last/7";

    /* Path of Batch Requests */
    public static String IEX_BATCH_PATH = "market/batch?symbols=";
    public static String IEX_BATCH_PATH_END = "&types=quote";
    public static String IEX_QUOTE = "quote";

    /* Constants of time conversion */
    public static long hourToMilli = 60 * 60 * 1000;
    public static long minuteToMilli = 60 * 1000;
    public static long secondToMilli = 1000;

    /* Id of the balance database */
    public static int balanceId = 1;

    // Gets the time
    public static long getTime(){
        // Gets the time and formats it
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
        SimpleDateFormat minuteFormat = new SimpleDateFormat("mm");
        SimpleDateFormat secondFormat = new SimpleDateFormat("ss");

        // Formats it
        long currentHour = Long.parseLong(hourFormat.format(calendar.getTime())) * Constants.hourToMilli;
        long currentMinute = Long.parseLong(minuteFormat.format(calendar.getTime())) * Constants.minuteToMilli;
        long currentSecond = Long.parseLong(secondFormat.format(calendar.getTime())) * Constants.secondToMilli;

        return currentHour + currentMinute  + currentSecond;
    }
}
