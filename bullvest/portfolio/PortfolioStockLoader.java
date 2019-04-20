package com.target.android.bullvest.portfolio;

import android.content.ContentValues;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Created by Ashwin on 19/05/2018.
 */

public class PortfolioStockLoader extends AsyncTaskLoader<ContentValues[]> {

    /** Tag for log messages */
    private static final String TAG = PortfolioStockLoader.class.getName();

    /** Query URL */
    private String mUrl;

    /**
     * Constructs a new {@link PortfolioStockLoader}.
     *
     * @param context of the activity
     * @param url to load data from
     */
    public PortfolioStockLoader(Context context, String url) {
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
        return PortfolioQueryUtils.fetchStockData(mUrl, getContext());
    }
}
