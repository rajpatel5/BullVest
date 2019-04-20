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

import android.text.TextUtils;
import android.util.Log;

import com.target.android.bullvest.utils.Constants;
import com.target.android.bullvest.utils.Stock;

import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Helper methods related to requesting and receiving stocks data from IEX.
 */
public final class HotListQueryUtils {

    /** Tag for the log messages */
    private static final String TAG = HotListQueryUtils.class.getSimpleName();

    /**
     * Create a private constructor because no one should ever
     * create a {@link HotListQueryUtils} object.
     */
    private HotListQueryUtils() {}

    /**
     * Query the IEX data set and return a list of {@link Stock} objects.
     */
    public static List<Stock> fetchStockData(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(TAG, "Problem making the HTTP request.", e);
        }

        /* Extract relevant fields from the JSON response and create a list of {@link Stock}s */
        List<Stock> stocks = extractFeatureFromJson(jsonResponse);

        /* Return the list of {@link Stock}s */
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
            urlConnection.setReadTimeout(10000 /* in milliseconds */);
            urlConnection.setConnectTimeout(15000 /* in milliseconds */);
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
            Log.e(TAG, "Problem retrieving the Stock JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                /*
                 * Closing the input stream could throw an IOException, which is why
                 * the makeHttpRequest(URL url) method signature specifies than an IOException
                 * could be thrown.
                 */
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
     * Return a list of {@link Stock} objects that has been built up from
     * parsing the given JSON response.
     */
    private static List<Stock> extractFeatureFromJson(String stockJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(stockJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding stocks to
        List<Stock> stocks = new ArrayList<>();

        /*
         *  Tries to parse the JSON response string.
         */
        try {

            // Create a JSONObject from the JSON response string
            JSONArray stockArray = new JSONArray(stockJSON);

            /* For each stock in the stockArray, create an {@link Stock} object */
            for (int i = 0; i < stockArray.length(); i++) {

                // Get a single stock at position i within the list of stocks
                JSONObject currentStock = stockArray.getJSONObject(i);

                // Extract the value for the key called "symbol"
                String symbol = currentStock.getString(Constants.IEX_SYMBOL);

                // Extract the value for the key called "companyName"
                String companyName = currentStock.getString(Constants.IEX_COMPANY_NAME);

                // Extract the value for the key called "latestPrice"
                double price = currentStock.getDouble(Constants.IEX_PRICE);

                // Extract the value for the key called "change"
                double priceChange = currentStock.getDouble(Constants.IEX_PRICE_CHANGE);

                // Extract the value for the key called "percentChange"
                double percentChange = (currentStock.getDouble(Constants.IEX_PERCENT_CHANGE)) * 100;

                // Create a new {@link Stock} object from the JSON response.
                Stock stock = new Stock(symbol, companyName, price, priceChange, percentChange);

                // Add the new {@link Stock} to the list of stocks.
                stocks.add(stock);
            }

        } catch (JSONException e) {
            /*
             * If an error is thrown when executing any of the above statements in the "try" block,
             * catch the exception here, so the app doesn't crash.
             */
            Log.e(TAG, "Problem parsing the stock JSON results", e);
        }

        // Return the list of stocks
        return stocks;
    }

}
