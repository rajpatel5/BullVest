package com.target.android.bullvest.research.Charts;

import android.text.TextUtils;
import android.util.Log;

import com.target.android.bullvest.utils.ChartPoints;
import com.target.android.bullvest.utils.Constants;

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
 * Helper methods related to requesting and receiving stocks chart data from IEX.
 */
public final class ResearchChartQueryUtils {

    /** Tag for the log messages */
    private static final String TAG = ResearchChartQueryUtils.class.getSimpleName();

    /**
     * Create a private constructor because no one should ever
     * create a {@link ResearchChartQueryUtils} object.
     */
    private ResearchChartQueryUtils() {}

    /**
     * Query the IEX data set and return a list of {@link ChartPoints} objects.
     */
    public static List<ChartPoints> fetchChartData(String requestUrl, String chartTag) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(TAG, "Problem making the HTTP request.", e);
        }

        /*
         * Extract relevant fields from the JSON
         *  response and create a list of {@link ChartPoints}
         */
        List<ChartPoints> points = extractFeatureFromJson(jsonResponse, chartTag);

        /* Return the list of {@link ChartPoints} */
        return points;
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
     * Return a list of {@link ChartPoints} objects that has been built up from
     * parsing the given JSON response.
     */
    private static List<ChartPoints> extractFeatureFromJson(String chartJSON, String chartTag) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(chartJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding stocks to
        List<ChartPoints> points = new ArrayList<>();

        // Try to parse the JSON response string.
        if (chartTag.equals("DailyFragment")){
            // Chart data for 1D
            try {
                // Create a JSONObject from the JSON response string
                JSONArray chartArray = new JSONArray(chartJSON);

                /* For each point in the chartArray, create an {@link ChartPoints} object */
                for (int i = 0; i < chartArray.length(); i++) {

                    // Get a single point at position i within the list of the chart
                    JSONObject currentPoint = chartArray.getJSONObject(i);

                    // Extract the value for the key called "marketAverage"
                    double avgPrice = currentPoint.getDouble(Constants.IEX_MARKET_AVERAGE);

                    /* Create a new {@link ChartPoints} object from the JSON response. */
                    ChartPoints point = new ChartPoints(avgPrice);

                    /* Add the new {@link ChartPoints} to the list of points. */
                    points.add(point);
                }

            } catch (JSONException e) {
                // If an error is thrown when executing any of the above statements in the "try" block,
                // catch the exception here, so the app doesn't crash.
                Log.e(TAG, "Problem parsing the Daily chart JSON results", e);
            }
        } else {
            // Chart data for 1M, 6M, 1Y, 5Y
            try {
                // Create a JSONObject from the JSON response string
                JSONArray chartArray = new JSONArray(chartJSON);

                /* For each point in the chartArray, create an {@link ChartPoints} object */
                for (int i = 0; i < chartArray.length(); i++) {

                    // Get a single point at position i within the list of the chart
                    JSONObject currentPoint = chartArray.getJSONObject(i);

                    // Extract the value for the key called "open"
                    double open = currentPoint.getDouble(Constants.IEX_OPEN);

                    // Extract the value for the key called "high"
                    double high = currentPoint.getDouble(Constants.IEX_HIGH);

                    // Extract the value for the key called "low"
                    double low = currentPoint.getDouble(Constants.IEX_LOW);

                    // Extract the value for the key called "close"
                    double close = currentPoint.getDouble(Constants.IEX_CLOSE);

                    // Stores the value of OHCL by calculating it
                    double ohlc = (open + high + low + close) / 4.0;

                    /* Create a new {@link ChartPoints} object from the JSON response. */
                    ChartPoints point = new ChartPoints(open, high, low, close, ohlc);

                    /* Add the new {@link ChartPoints} to the list of points. */
                    points.add(point);
                }

            } catch (JSONException e) {
                // If an error is thrown when executing any of the above statements in the "try" block,
                // catch the exception here, so the app doesn't crash.
                Log.e(TAG, "Problem parsing the chart JSON results", e);
            }
        }

        // Return the list of stocks
        return points;
    }

}
