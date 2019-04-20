/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.target.android.bullvest.hotlist;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.target.android.bullvest.utils.Stock;

import java.util.List;

/**
 * Loads a list of {@link Stock}s by using an AsyncTask to perform the
 * network request to the given URL.
 */
public class HotListStockLoader extends AsyncTaskLoader<List<Stock>> {

    /** Tag for log messages */
    private static final String TAG = HotListStockLoader.class.getName();

    /** Query URL */
    private String mUrl;

    /**
     * Constructs a new {@link HotListStockLoader}.
     *
     * @param context of the activity
     * @param url to load data from
     */
    public HotListStockLoader(Context context, String url) {
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
    public List<Stock> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract a list of stocks.
        return HotListQueryUtils.fetchStockData(mUrl);
    }
}
