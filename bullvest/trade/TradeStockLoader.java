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
package com.target.android.bullvest.trade;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.target.android.bullvest.utils.Stock;

/**
 * Loads a quote of a stock by using an AsyncTask to perform the
 * network request to the given URL.
 */
public class TradeStockLoader extends AsyncTaskLoader<Stock> {

    /** Tag for log messages */
    private static final String TAG = TradeStockLoader.class.getName();

    /** Query URL */
    private String mUrl;

    /**
     * Constructs a new {@link TradeStockLoader}.
     *
     * @param context of the activity
     * @param url to load data from
     */
    public TradeStockLoader(Context context, String url) {
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
    public Stock loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract a quote.
        return TradeQueryUtils.fetchStockData(mUrl);
    }
}
