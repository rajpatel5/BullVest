package com.target.android.bullvest.research.Charts;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.target.android.bullvest.utils.ChartPoints;
import com.target.android.bullvest.utils.Constants;
import com.target.android.bullvest.R;
import com.target.android.bullvest.research.ResearchActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Ashwin on 09/05/2018.
 */

public class DailyFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<List<ChartPoints>>{

    // Layout of the screen
    private View rootView;

    // Used for logging
    private final String TAG = DailyFragment.class.getSimpleName();

    /** Constant value for the stock loader ID */
    private static final int STOCK_LOADER_ID = 4;

    // Used to connect to the internet and load data
    NetworkInfo networkInfo;
    LoaderManager loaderManager;

    /** Timer used to refresh data */
    Timer timer;
    TimerTask timerTask;

    //we are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();

    // Line Chart
    private LineChart mChart;

    // Stock symbol, Opening price, Previous close price and Url
    String stockSymbol;
    String previousStockSymbol;
    float previousClose;
    float open;
    float price;
    String stockChartUrl = Constants.IEX_REQUEST_URL + stockSymbol +
            Constants.IEX_DAILY_PATH_END;

    // Used for the chart
    ArrayList<Entry> prices;
    ArrayList<Entry> previousClosePrice;
    LineDataSet priceData;
    LineDataSet previousCloseData;
    ArrayList<ILineDataSet> dataSets;
    LineData chartData;

    // Set to False
    boolean filled = false;

    int count = 0;

    // Empty public constructor
    public DailyFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Retrieves the stock symbol, opening price, and previous close price
        if (ResearchActivity.count >= 1){
            try {
                Bundle bundle = getArguments();
                if (bundle != null) {
                    previousStockSymbol = stockSymbol;  // Sets the previous stock symbol

                    // Gets all the values
                    stockSymbol = bundle.getString("stockSymbol");
                    previousClose = bundle.getFloat("previousClose");
                    price = bundle.getFloat("price");
                    open = bundle.getFloat("open");
                    filled = false;  // Sets 'filled' to false

                    stockChartUrl = Constants.IEX_REQUEST_URL + stockSymbol +
                            Constants.IEX_DAILY_PATH_END;
                    fetchAgain();  // Retrieves the data
                }
            }catch (NullPointerException e){
                Log.e(TAG, "Problem with retrieving stock data");
            }
        }

        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.stock_charts, container, false);

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (ResearchActivity.count >= 1) {
            if (networkInfo != null && networkInfo.isConnected()) {
                // Get a reference to the LoaderManager, in order to interact with loaders.
                loaderManager = getLoaderManager();

                // Initialize the loader.
                loaderManager.initLoader(STOCK_LOADER_ID, null, this);
            }

            // Finds chart and sets the rules
            mChart = (LineChart)rootView.findViewById(R.id.stock_charts);
            mChart.setScaleEnabled(false);
            mChart.setDragEnabled(true);
            mChart.setClickable(true);

            // Does not display the Axes labels
            mChart.getAxisLeft().setDrawLabels(false);
            mChart.getAxisRight().setDrawLabels(false);
            mChart.getXAxis().setDrawLabels(false);
            mChart.getAxisLeft().setDrawGridLines(false);
            mChart.getXAxis().setDrawGridLines(false);
            mChart.getAxisRight().setDrawGridLines(false);

            // Does not display the Axes
            XAxis xAxis = mChart.getXAxis();
            xAxis.setEnabled(false);
            YAxis yAxis = mChart.getAxisLeft();
            yAxis.setEnabled(false);
            YAxis yAxis2 = mChart.getAxisRight();
            yAxis2.setEnabled(false);

            // Does not display the borders or the Grid
            mChart.setDrawBorders(false);
            mChart.setDrawGridBackground(false);

            // Does not display the Legend or description
            mChart.getLegend().setEnabled(false);
            // no description text
            mChart.getDescription().setEnabled(false);
            mChart.animateX(2000);

            // Creates an ArrayList of Entry Points and adds new Entries
            previousClosePrice = new ArrayList<>();

            // Adds each previous close price point into an ArrayList
            for (int i = 0; i < 39; i++) {
                previousClosePrice.add(new Entry((float)(i * 10), previousClose));
            }
            // Creates a new Line data set named 'Previous Close Price'
            previousCloseData = new LineDataSet(previousClosePrice, "Previous Close Price");

            // Changes the size of the points and doesn't display the horizontal indicator
            // Changes the radius of the points and makes the line invisible using transparent color
            previousCloseData.setDrawCircles(true);
            previousCloseData.setDrawValues(false);
            previousCloseData.setCircleRadius(1.75f);
            previousCloseData.setCircleColor(ContextCompat.
                    getColor(getContext(), R.color.previousCloseColor));
            previousCloseData.setCircleColorHole(ContextCompat.
                    getColor(getContext(), R.color.previousCloseColor));
            previousCloseData.setColor(android.R.color.transparent);
            previousCloseData.setDrawHorizontalHighlightIndicator(false);
            previousCloseData.setDrawVerticalHighlightIndicator(false);
            previousCloseData.setHighLightColor(ContextCompat.getColor(getContext(), R.color.highlightColor));

            dataSets = new ArrayList<>();
            dataSets.add(previousCloseData);

            chartData = new LineData();
            chartData.addDataSet(previousCloseData);

            mChart.setData(chartData);
        }

        return rootView;
    }

    public void fetchAgain(){
        // If there is a network connection, fetch data
        if (ResearchActivity.count >= 1) {
            if (networkInfo != null && networkInfo.isConnected()) {
                // Get a reference to the LoaderManager, in order to interact with loaders.
                loaderManager = getLoaderManager();

                // Restart the loader.
                loaderManager.restartLoader(STOCK_LOADER_ID, null, this);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startTimer();  // Starts the timer on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTimerTask();  // Stops the timer on pause
    }

    @NonNull
    @Override
    public Loader<List<ChartPoints>> onCreateLoader(int id, @Nullable Bundle args) {
        // Builds the URI
        Uri baseUri = Uri.parse(stockChartUrl);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        return new ResearchChartLoader(getActivity(), uriBuilder.toString(), TAG);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<ChartPoints>> loader, List<ChartPoints> data) {
        // Checks if data is valid or not
        if (data != null && data.size() != 0) {
            mChart.clear();

            // Creates an ArrayList of Entry Points and adds new Entries
            prices = new ArrayList<>();

            // The first price value is the opening price if the API returns '-1'
            if (data.get(0).getAvgPrice() == -1){
                prices.add(new Entry(0f, open));
            }

            // Adds each price point into an ArrayList
            for (int i = 0; i < data.size(); i++){
                if (data.get(i).getAvgPrice() != -1){
                    // Adds the price into the Prices Array List
                    prices.add(new Entry((float) i, (float) data.get(i).getAvgPrice()));
                }
            }
            // Creates a new Line data set named 'Stock Prices'
            priceData = new LineDataSet(prices, "Stock Prices");

            // Changes the size of the points and doesn't display the horizontal indicator
            priceData.setDrawCircles(false);
            priceData.setLineWidth(1.3f);
            priceData.setDrawHorizontalHighlightIndicator(false);
            priceData.setHighLightColor(ContextCompat.getColor(getContext(), R.color.highlightColor));

            // Checks if the graph is filled or not
            if (!filled){
                priceData.setDrawFilled(true);
                filled = true;
            } else {
                priceData.setDrawFilled(false);
            }

            // Sets the color of the line according to the price and the previous close
            if (price > (double)previousClose) {
                priceData.setColor(ContextCompat.getColor(getContext(), R.color.graphGreen));
                priceData.setFillDrawable(ContextCompat.
                        getDrawable(getContext(), R.drawable.chart_gradient_green));
            } else if (price < (double)previousClose){
                priceData.setColor(ContextCompat.getColor(getContext(), R.color.graphRed));
                priceData.setFillDrawable(ContextCompat.
                        getDrawable(getContext(), R.drawable.chart_gradient_red));
            } else {
                priceData.setColor(ContextCompat.getColor(getContext(), R.color.graphNeutral));
                priceData.setFillDrawable(ContextCompat.
                        getDrawable(getContext(), R.drawable.chart_gradient_neutral));
            }

            // Creates a new ArrayList of data sets and creates new Line Data
            dataSets.add(priceData);
            chartData.addDataSet(priceData);

            // Does not display the price values
            priceData.setDrawValues(false);

            // Sets the Data
            mChart.setData(chartData);
        } else {
            // Resets the stock symbol and the URL and retrieves the data
            stockSymbol = previousStockSymbol;
            stockChartUrl = Constants.IEX_REQUEST_URL + stockSymbol +
                    Constants.IEX_DAILY_PATH_END;
            fetchAgain();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<ChartPoints>> loader) {
        // Empty
    }

    /* Starts the Timer */
    public void startTimer() {
        // Set a new Timer
        timer = new Timer();

        // Initialize the TimerTask's job
        timerTask = new TimerTask() {
            public void run() {

                // Use a handler to run the fetchAgain method
                handler.post(new Runnable() {
                    public void run() {
                        fetchAgain();
                    }
                });
            }
        };

        // Schedule the timer, TimerTask will run every second
        timer.schedule(timerTask, 0, 1000); //
    }

    /* Stops the timer */
    public void stopTimerTask() {
        // Stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
