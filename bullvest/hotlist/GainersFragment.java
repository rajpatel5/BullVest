package com.target.android.bullvest.hotlist;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.target.android.bullvest.utils.Constants;
import com.target.android.bullvest.R;
import com.target.android.bullvest.utils.Stock;
import com.target.android.bullvest.research.ResearchActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class GainersFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<List<Stock>> {

    // Layout of the screen
    private View rootView;

    // Used for logging
    private final String TAG = GainersFragment.class.getSimpleName();

    /** Constant value for the stock loader ID */
    private static final int STOCK_LOADER_ID = 1;

    /** Adapter for the list of stocks */
    private HotListStockAdapter mAdapter;

    /** TextView that is displayed when the list is empty */
    private TextView mEmptyStateTextView;

    /** Help connect and receive data from the web */
    NetworkInfo networkInfo;
    LoaderManager loaderManager;

    /** Timer used to refresh data */
    Timer timer;
    TimerTask timerTask;

    //we are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();

    // Empty public constructor
    public GainersFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.stocks_list_view, container, false);

        // Find a reference to the {@link ListView} in the layout
        final ListView stockListView = (ListView) rootView.findViewById(R.id.list);

        mEmptyStateTextView = (TextView) rootView.findViewById(R.id.empty_view);
        stockListView.setEmptyView(mEmptyStateTextView);

        // Create a new adapter that takes an empty list of stocks as input
        mAdapter = new HotListStockAdapter(getActivity(), new ArrayList<Stock>());

        /**
         * Set the adapter on the {@link ListView}
         * so the list can be populated in the user interface
         */
        stockListView.setAdapter(mAdapter);

        /**
         * Set an item click listener on the ListView, which sends an intent to
         * the ResearchActivity to open more info about the stock.
         */
        stockListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String stockSymbol = mAdapter.getItem(position).getSymbol().toLowerCase();
                Intent researchIntent = new Intent(getContext(), ResearchActivity.class);
                researchIntent.putExtra("stockSymbol", stockSymbol);
                researchIntent.putExtra("fromPortfolio", false);
                startActivity(researchIntent);
                stopTimerTask();
            }
        });

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            loaderManager = getLoaderManager();

            // Initialize the loader.
            loaderManager.initLoader(STOCK_LOADER_ID, null, this);
        } else {
            // Hides loading indicator so error message will be visible
            View loadingIndicator = rootView.findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }

        return rootView;
    }

    public void fetchAgain(){
        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {

            // Restarts the loader.
            loaderManager.restartLoader(STOCK_LOADER_ID, null, this);
        } else {
            // Hides loading indicator so error message will be visible
            View loadingIndicator = rootView.findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
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

    /** Required methods for the loader, on create; the URI is made and then passed to
     * return the data, then on finish; the Data is set on the adapter*/
    @NonNull
    @Override
    public Loader<List<Stock>> onCreateLoader(int id, @Nullable Bundle args) {
        // Builds the URI
        Uri baseUri = Uri.parse(Constants.IEX_REQUEST_URL + Constants.IEX_GAINERS_PATH);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        return new HotListStockLoader(getActivity(), uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Stock>> loader, List<Stock> stocks) {
        // Hide loading indicator because the data has been loaded
        View loadingIndicator = rootView.findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        // Set empty state text to display "No stocks found."
        mEmptyStateTextView.setText(R.string.no_stocks);

        /**
         * If there is a valid list of {@link Stock}s, then add them to the adapter's
         * data set. This will trigger the ListView to update.
         */
        if (stocks != null && !stocks.isEmpty()) {
            // Keeps the scroller on current position when data refreshes
            mAdapter.setNotifyOnChange(false);
            mAdapter.clear();
            mAdapter.addAll(stocks);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Stock>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
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

        // Schedule the timer, TimerTask will run every 1 second
        timer.schedule(timerTask, 0, 1000); //
    }

    /* Stops the timer */
    public void stopTimerTask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}