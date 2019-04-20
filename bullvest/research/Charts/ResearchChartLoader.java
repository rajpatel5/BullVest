package com.target.android.bullvest.research.Charts;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.target.android.bullvest.utils.ChartPoints;

import java.util.List;

/**
 * Loads a list of chart points by using an AsyncTask to perform the
 * network request to the given URL.
 */
public class ResearchChartLoader extends AsyncTaskLoader<List<ChartPoints>> {

    /** Tag for log messages */
    private static final String TAG = ResearchChartLoader.class.getName();

    /** Query URL */
    private String mUrl, mChartTag;

    /**
     * Constructs a new {@link ResearchChartLoader}.
     *
     * @param context of the activity
     * @param url to load data from
     * @param chartTag signifies which chart to return
     */
    public ResearchChartLoader(Context context, String url, String chartTag) {
        super(context);
        mUrl = url;
        mChartTag = chartTag;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public List<ChartPoints> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract a list of price points.
        return ResearchChartQueryUtils.fetchChartData(mUrl, mChartTag);
    }
}
