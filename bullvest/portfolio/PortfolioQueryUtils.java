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
package com.target.android.bullvest.portfolio;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.target.android.bullvest.utils.Constants;
import com.target.android.bullvest.utils.NetworkUtils;
import com.target.android.bullvest.data.StockContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DecimalFormat;

/**
 * Helper methods related to requesting and receiving stock data from IEX.
 */
public final class PortfolioQueryUtils {

    /** Tag for the log messages */
    private static final String TAG = "PortfolioQueryUtils";

    /**
     * Create a private constructor because no one
     * should ever create a {@link PortfolioQueryUtils} object.
     */
    private PortfolioQueryUtils() {
    }

    /**
     * Query the IEX data set and return an array of ContentValues.
     */
    public static ContentValues[] fetchStockData(String requestUrl, Context context) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(TAG, "Problem making the HTTP request.", e);
        }

        /** Extract relevant fields from the JSON response and create a ContentValues array */
        ContentValues[] stocks = extractFeatureFromJson(jsonResponse, context);

        /** Return the ContentValues array */
        return stocks;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(TAG, "Problem retrieving the stock JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return an array of ContentValues that has been built up from
     * parsing the given JSON response.
     */
    private static ContentValues[] extractFeatureFromJson(String stockBatchJSONStr, Context context){
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(stockBatchJSONStr)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding stocks to
        ContentValues[] stockContentValues = null;

        // Try to parse the JSON response string.
        try {
            // Creates a new JSONObject
            JSONObject stockBatchJson = new JSONObject(stockBatchJSONStr);

            // Creates a new ContentValues array to insert stock data into database
            stockContentValues = new ContentValues[NetworkUtils.allPositionSymbols.size()];

            /* Get a handle on the ContentResolver to delete and insert data */
            final ContentResolver myInvestmentsContentResolver = context.getContentResolver();

                /* Opens the time frame for the transactions from database */
            Cursor cursor = myInvestmentsContentResolver.query(
                    StockContract.MyInvestmentsEntry.CONTENT_URI, null,
                    null,null,null);

            try{
                if (cursor != null) {
                    cursor.moveToFirst();
                    for (int i = 0; i < NetworkUtils.allPositionSymbols.size(); i++) {
                        JSONObject stockJson = stockBatchJson.getJSONObject(NetworkUtils
                                .allPositionSymbols.get(i).toUpperCase());
                        JSONObject quoteJson = stockJson.getJSONObject(Constants.IEX_QUOTE);

                        // Extract the value for the key called "symbol"
                        String symbol = quoteJson.getString(Constants.IEX_SYMBOL);
                        // Extract the value for the key called "latestPrice"
                        double price = Double.parseDouble(formatDecimal(
                                quoteJson.getDouble(Constants.IEX_PRICE)));
                        // Extract the value for the key called "priceChange"
                        double priceChange = Double.parseDouble(formatDecimal(
                                quoteJson.getDouble(Constants.IEX_PRICE_CHANGE)));
                        // Extract the value for the key called "percentChange"
                        double percentChange = Double.parseDouble(formatDecimal(
                                quoteJson.getDouble(Constants.IEX_PERCENT_CHANGE) * 100));

                        // Retrieves the components of the trade
                        String stockSymbol = cursor.getString(cursor
                                .getColumnIndexOrThrow(StockContract.MyInvestmentsEntry.COLUMN_SYMBOL));

                        // Stores the data in a ContentValues object
                        ContentValues stockValues = new ContentValues();

                        if (stockSymbol.toLowerCase().equals(symbol.toLowerCase())) {
                            String shares = cursor.getString(cursor
                                    .getColumnIndexOrThrow(StockContract.MyInvestmentsEntry.COLUMN_SHARES));

                            stockValues.put(StockContract.MyInvestmentsEntry.COLUMN_SYMBOL, symbol);
                            stockValues.put(StockContract.MyInvestmentsEntry.COLUMN_SHARES, shares);
                            stockValues.put(StockContract.MyInvestmentsEntry.COLUMN_PRICE, price);
                            stockValues.put(StockContract.MyInvestmentsEntry.COLUMN_PRICE_CHANGE, priceChange);
                            stockValues.put(StockContract.MyInvestmentsEntry.COLUMN_PERCENT_CHANGE, percentChange);

                            stockContentValues[i] = stockValues;
                        }
                        cursor.moveToNext();
                    }
                }
            } finally {
                cursor.close();
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash.
            Log.e(TAG, "Problem parsing the stock JSON results", e);
        }

        // Return the array of ContentValues
        return stockContentValues;
    }

    /**
     * Return the formatted value string
     * showing 2 decimal places (i.e. "3.25") from a decimal value.
     */
    private static String formatDecimal(double item) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return decimalFormat.format(item);
    }

}
