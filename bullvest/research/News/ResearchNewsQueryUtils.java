package com.target.android.bullvest.research.News;

import android.text.TextUtils;
import android.util.Log;

import com.target.android.bullvest.utils.Constants;
import com.target.android.bullvest.utils.NewsData;

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
public final class ResearchNewsQueryUtils {

    /** Tag for the log messages */
    private static final String TAG = ResearchNewsQueryUtils.class.getSimpleName();

    /**
     * Create a private constructor because no one should ever
     * create a {@link ResearchNewsQueryUtils} object.
     */
    private ResearchNewsQueryUtils() {}

    /**
     * Query the IEX data set and return a list of {@link NewsData} objects.
     */
    public static List<NewsData> fetchNewsData(String requestUrl) {
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
         * Extract relevant fields from the JSON response
         * and create a list of {@link NewsData}
         * Return the list of {@link NewsData}
         */
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
     * Return a list of {@link NewsData} objects that has been built up from
     * parsing the given JSON response.
     */
    private static List<NewsData> extractFeatureFromJson(String newsJSON) {
        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(newsJSON)) {
            return null;
        }

        // Create an empty ArrayList that we can start adding stocks to
        List<NewsData> articles = new ArrayList<>();

        // Try to parse the JSON response string.
        try {

            // Create a JSONObject from the JSON response string
            JSONArray newsArray = new JSONArray(newsJSON);

            /* For each article in the newsArray, create an {@link NewsData} object */
            for (int i = 0; i < newsArray.length(); i++) {

                // Get a single article at position i within the list of the chart
                JSONObject currentArticle = newsArray.getJSONObject(i);

                // Extract the value for the key called "headline"
                String headline = currentArticle.getString(Constants.IEX_HEADLINE);

                // Extract the value for the key called "source"
                String source = currentArticle.getString(Constants.IEX_SOURCE);

                // Extract the value for the key called "url"
                String url = currentArticle.getString(Constants.IEX_URL);

                // Extract the value for the key called "datetime"
                String datetime = currentArticle.getString(Constants.IEX_DATETIME);

                /* Create a new {@link NewsData} object from the JSON response.*/
                NewsData article = new NewsData(headline, source, url, datetime);

                /* Add the new {@link NewsData} to the list of articles. */
                articles.add(article);
            }

        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash.
            Log.e(TAG, "Problem parsing the Daily chart JSON results", e);
        }

        // Return the list of articles
        return articles;
    }

}
