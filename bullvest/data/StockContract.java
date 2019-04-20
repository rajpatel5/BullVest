package com.target.android.bullvest.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Ashwin on 22/04/2018.
 */

public class StockContract {

    /**
     * Name for the entire content provider
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.test";

    /**
     * The base of all URI's which apps will use to contact
     * the content provider for BullVest.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible paths that can be appended to BASE_CONTENT_URI to form valid URI's that BullVest
     * can handle.
     */
    public static final String PATH_WATCHLIST = "watchlist";
    public static final String PATH_FILLED = "filled";
    public static final String PATH_PENDING = "pending";
    public static final String PATH_MY_INVESTMENTS = "myInvestments";

    /* Inner class that defines the table contents of the watchlist table */
    public static final class WatchlistEntry implements BaseColumns {

        /* The base CONTENT_URI used to query the watchlist table from the Content Provider */
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_WATCHLIST).build();

        /* Used internally as the name of our watchlist table. */
        public static final String TABLE_NAME = "watchlist";

        /* Stock ID, used to identify the stock */
        public static final String _ID = BaseColumns._ID;

        /* Symbol as returned by the API, used to identify the stock ticker symbol */
        public static final String COLUMN_SYMBOL = "symbol";

        /* Company Name as returned by the API, used to identify the stock's company name */
        public static final String COLUMN_COMPANY_NAME = "companyName";

        /* Price as returned by the API, used to identify the stock's price*/
        public static final String COLUMN_PRICE = "price";

        /* Price change as returned by the API, used to identify the change in price */
        public static final String COLUMN_PRICE_CHANGE = "priceChange";

        /* Percent Change as returned by the API, used to identify the change in percent*/
        public static final String COLUMN_PERCENT_CHANGE = "percentChange";
    }

    /* Inner class that defines the table contents of the filled table */
    public static final class FilledEntry implements BaseColumns {

        /* The base CONTENT_URI used to query the filled table from the Content Provider */
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_FILLED).build();

        /* Used internally as the name of our filled table. */
        public static final String TABLE_NAME = "filled";

        /* Stock ID, used to identify the stock */
        public static final String _ID = BaseColumns._ID;

        /* Used to identify the stock ticker symbol */
        public static final String COLUMN_SYMBOL = "symbol";

        /* Used to identify the amount of shares bought or sold */
        public static final String COLUMN_SHARES = "shares";

        /* Used to identify the type of order*/
        public static final String COLUMN_ORDER_TYPE = "orderType";

        /* Used to identify the price brought or sold */
        public static final String COLUMN_TRADE_PRICE = "tradePrice";

        /* Used to identify the stop price */
        public static final String COLUMN_STOP_PRICE = "stopPrice";

        /* Used to identify the limit price */
        public static final String COLUMN_LIMIT_PRICE = "limitPrice";

        /* Used to identify the orders (buy/sell) */
        public static final String COLUMN_ORDERS = "orders";

        /* Used to identify the time frame */
        public static final String COLUMN_TIME_FRAME = "timeFrame";

        /* Used to identify the execution price */
        public static final String COLUMN_EXECUTION_PRICE = "executionPrice";
    }

    /* Inner class that defines the table contents of the pending table */
    public static final class PendingEntry implements BaseColumns {

        /* The base CONTENT_URI used to query the pending table from the Content Provider */
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_PENDING).build();

        /* Used internally as the name of our pending table. */
        public static final String TABLE_NAME = "pending";

        /* Stock ID, used to identify the stock */
        public static final String _ID = BaseColumns._ID;

        /* Used to identify the stock ticker symbol */
        public static final String COLUMN_SYMBOL = "symbol";

        /* Used to identify the amount of shares bought or sold */
        public static final String COLUMN_SHARES = "shares";

        /* Used to identify the type of order*/
        public static final String COLUMN_ORDER_TYPE = "orderType";

        /* Used to identify the price brought or sold */
        public static final String COLUMN_TRADE_PRICE = "tradePrice";

        /* Used to identify the stop price */
        public static final String COLUMN_STOP_PRICE = "stopPrice";

        /* Used to identify the limit price */
        public static final String COLUMN_LIMIT_PRICE = "limitPrice";

        /* Used to identify the orders (buy/sell) */
        public static final String COLUMN_ORDERS = "orders";

        /* Used to identify the time frame */
        public static final String COLUMN_TIME_FRAME = "timeFrame";

        /* Used to identify the execution price */
        public static final String COLUMN_EXECUTION_PRICE = "executionPrice";
    }

    /* Inner class that defines the table contents of the my investments table */
    public static final class MyInvestmentsEntry implements BaseColumns {

        /* The base CONTENT_URI used to query the my investments table from the Content Provider */
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MY_INVESTMENTS).build();

        /* Used internally as the name of my investments table. */
        public static final String TABLE_NAME = "myInvestments";

        /* Stock ID, used to identify the stock */
        public static final String _ID = BaseColumns._ID;

        /* Used to identify the stock ticker symbol */
        public static final String COLUMN_SYMBOL = "symbol";

        /* Used to identify the amount of shares bought or sold */
        public static final String COLUMN_SHARES = "shares";

        /* Price as returned by the API, used to identify the stock's price */
        public static final String COLUMN_PRICE = "price";

        /* Price change as returned by the API, used to identify the change in price */
        public static final String COLUMN_PRICE_CHANGE = "priceChange";

        /* Percent Change as returned by the API, used to identify the change in percent */
        public static final String COLUMN_PERCENT_CHANGE = "percentChange";
    }
}
