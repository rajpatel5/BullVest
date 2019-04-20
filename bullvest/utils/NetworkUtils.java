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
package com.target.android.bullvest.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.target.android.bullvest.data.StockContract;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * These utilities will be used to communicate with the IEX servers.
 */
public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();
    public static ArrayList<String> allWatchlistSymbols;
    public static ArrayList<String> allPositionSymbols;

    /**
     * @param context used to access other Utility methods
     * @return URL to query stock service
     */
    public static URL getUrl(Context context) {
        String batchUrl = Constants.IEX_REQUEST_URL + Constants.IEX_BATCH_PATH;

        /* Get a handle on the ContentResolver to delete and insert data */
        ContentResolver watchlistContentResolver = context.getContentResolver();
        String[] projection = new String[] {StockContract.WatchlistEntry.COLUMN_SYMBOL};

        /* Opens the stock Symbols from database */
        Cursor cursor = watchlistContentResolver.query(
                StockContract.WatchlistEntry.CONTENT_URI,
                projection, null,null,null);
        allWatchlistSymbols = new ArrayList<>();

        // Adds each stock Symbol to the url
        if (cursor != null){
            cursor.moveToFirst();
            String dbSymbol;
            for (int i = 0; i < cursor.getCount(); i++){
                // Retrieves the symbol
                dbSymbol = cursor.getString(cursor
                        .getColumnIndexOrThrow(StockContract.WatchlistEntry.COLUMN_SYMBOL));
                allWatchlistSymbols.add(dbSymbol.toUpperCase());
                batchUrl += "," + dbSymbol.toLowerCase();
                cursor.moveToNext();  // Moves to next stock Symbol in the database
            }
        }

        batchUrl += Constants.IEX_BATCH_PATH_END;
        Uri batchQueryUri = Uri.parse(batchUrl).buildUpon().build();

        try {
            return new URL(batchQueryUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param context used to access other Utility methods
     * @return URL to query stock service
     */
    public static URL getPortfolioUrl(Context context) {
        String batchUrl = Constants.IEX_REQUEST_URL + Constants.IEX_BATCH_PATH;

        /* Get a handle on the ContentResolver to delete and insert data */
        ContentResolver myInvestmentsContentResolver = context.getContentResolver();
        String[] projection = new String[] {StockContract.MyInvestmentsEntry.COLUMN_SYMBOL};

        /* Opens the stock Symbols from database */
        Cursor cursor = myInvestmentsContentResolver.query(
                StockContract.MyInvestmentsEntry.CONTENT_URI, projection, null,
                null,null);
        allPositionSymbols = new ArrayList<>();

        // Adds each stock Symbol to the url
        if (cursor != null){
            cursor.moveToFirst();
            String dbSymbol;
            for (int i = 0; i < cursor.getCount(); i++){
                // Retrieves the symbol
                dbSymbol = cursor.getString(cursor
                        .getColumnIndexOrThrow(StockContract.MyInvestmentsEntry.COLUMN_SYMBOL));
                allPositionSymbols.add(dbSymbol.toUpperCase());
                batchUrl += "," + dbSymbol.toLowerCase();
                cursor.moveToNext();  // Moves to next stock Symbol in the database
            }
        }

        batchUrl += Constants.IEX_BATCH_PATH_END;
        Uri batchQueryUri = Uri.parse(batchUrl).buildUpon().build();

        try {
            return new URL(batchQueryUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }
}