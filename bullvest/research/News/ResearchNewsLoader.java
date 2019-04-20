package com.target.android.bullvest.research.News;

import android.content.Context;
import android.content.AsyncTaskLoader;

import com.target.android.bullvest.utils.NewsData;

import java.util.List;

/**
 * Loads a list of articles by using an AsyncTask to perform the
 * network request to the given URL.
 */
public class ResearchNewsLoader extends AsyncTaskLoader<List<NewsData>> {

    /** Tag for log messages */
    private static final String TAG = ResearchNewsLoader.class.getName();

    /** Query URL */
    private String mUrl;

    /**
     * Constructs a new {@link ResearchNewsLoader}.
     *
     * @param context of the activity
     * @param url to load data from
     */
    public ResearchNewsLoader(Context context, String url) {
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
    public List<NewsData> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract a list of articles.
        return ResearchNewsQueryUtils.fetchNewsData(mUrl);
    }
}
