package com.target.android.bullvest.transactions;

import android.text.TextUtils;
import android.util.Log;

import com.target.android.bullvest.utils.Constants;
import com.target.android.bullvest.utils.Stock;

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

/**
 * Helper methods related to requesting and receiving stock data from IEX.
 */
public final class PendingQueryUtils {

    /** Tag for the log messages */
    private static final String TAG = "PendingQueryUtils";

    /**
     * Create a private constructor because no one
     * should ever create a {@link PendingQueryUtils} object.
     */
    private PendingQueryUtils() {
    }

    /**
     * Query the IEX data set and return a list of {@link Stock} objects.
     */
    public static Stock fetchStockData(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a list of {@link Stock}s
        return extractFeatureFromJson(jsonResponse);
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
     * Return a list of {@link Stock} objects that has been built up from
     * parsing the given JSON response.
     */
    private static Stock extractFeatureFromJson(String stockJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(stockJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding stocks to
        Stock stock = new Stock();

        // Try to parse the JSON response string.
        try {

            // Create a JSONObject from the JSON response string
            JSONObject currentStock = new JSONObject(stockJSON);

            // Extract the value for the key called "symbol"
            String symbol = currentStock.getString(Constants.IEX_SYMBOL);

            // Extract the value for the key called "companyName"
            String companyName = currentStock.getString(Constants.IEX_COMPANY_NAME);

            // Extract the value for the key called "primaryExchange"
            String exchange = currentStock.getString(Constants.IEX_EXCHANGE);

            // Extract the value for the key called "sector"
            String sector = currentStock.getString(Constants.IEX_SECTOR);

            // Extract the value for the key called "latestPrice"
            double price = currentStock.getDouble(Constants.IEX_PRICE);

            // Extract the value for the key called "change"
            double priceChange = currentStock.getDouble(Constants.IEX_PRICE_CHANGE);

            // Extract the value for the key called "percentChange"
            double percentChange = (currentStock.getDouble(Constants.IEX_PERCENT_CHANGE)) * 100;

            // Extract the value for the key called "open"
            double open = currentStock.getDouble(Constants.IEX_OPEN);

            // Extract the value for the key called "previousClose"
            double previousClose = currentStock.getDouble(Constants.IEX_PREVIOUS_CLOSE);

            // Extract the value for the key called "high"
            String high = currentStock.getString(Constants.IEX_HIGH);

            // Extract the value for the key called "low"
            String low = currentStock.getString(Constants.IEX_LOW);

            // Extract the value for the key called "peRatio"
            String peRatio = currentStock.getString(Constants.IEX_PE_RATIO);

            // Extract the value for the key called "week52High"
            double week52High = currentStock.getDouble(Constants.IEX_WEEK_52_HIGH);

            // Extract the value for the key called "week52Low"
            double week52Low = currentStock.getDouble(Constants.IEX_WEEK_52_LOW);

            // Extract the value for the key called "marketCap"
            double marketCap = currentStock.getDouble(Constants.IEX_MARKET_CAP);

            /* Create a new {@link Stock} object from the JSON response. */
            stock = new Stock(symbol, companyName, exchange,
                    sector, price, priceChange, percentChange, open, previousClose,
                    high, low, marketCap, peRatio, week52High, week52Low);

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash.
            Log.e(TAG, "Problem parsing the stock JSON results", e);
        }

        // Return the stock
        return stock;
    }

}
