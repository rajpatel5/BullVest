package com.target.android.bullvest.watchlist;

import android.support.v4.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Context;

/**
 * Created by Ashwin on 19/05/2018.
 */

public class WatchlistStockLoader extends AsyncTaskLoader<ContentValues[]> {

    /** Tag for log messages */
    private static final String TAG = WatchlistStockLoader.class.getName();

    /** Query URL */
    private String mUrl;

    /**
     * Constructs a new {@link WatchlistStockLoader}.
     *
     * @param context of the activity
     * @param url to load data from
     */
    public WatchlistStockLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public ContentValues[] loadInBackground(){
        if (mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract a quote.
        ContentValues[] stock = WatchlistQueryUtils.fetchStockData(mUrl);
        return stock;
    }
}
