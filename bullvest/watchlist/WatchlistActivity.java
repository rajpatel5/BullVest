package com.target.android.bullvest.watchlist;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.target.android.bullvest.R;
import com.target.android.bullvest.account.AccountActivity;
import com.target.android.bullvest.data.StockContract;
import com.target.android.bullvest.hotlist.HotListActivity;
import com.target.android.bullvest.learningcenter.LearningCenterActivity;
import com.target.android.bullvest.portfolio.PortfolioActivity;
import com.target.android.bullvest.research.ResearchActivity;
import com.target.android.bullvest.trade.TradeActivity;
import com.target.android.bullvest.transactions.TransactionsActivity;
import com.target.android.bullvest.utils.NetworkUtils;

import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class WatchlistActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        WatchlistAdapter.WatchlistAdapterOnClickHandler{

    private final String TAG = WatchlistActivity.class.getSimpleName();

    private LoaderManager.LoaderCallbacks<Cursor> cursorLoaderListener =
            new LoaderManager.LoaderCallbacks<Cursor>() {
        @NonNull
        @Override
        public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
            /* URI for all rows of stock data in our watchlist table */
            Uri watchlistQueryUri = StockContract.WatchlistEntry.CONTENT_URI;

            /* Sort order: Ascending by symbol */
            String sortOrder = StockContract.WatchlistEntry.COLUMN_SYMBOL + " ASC";

            return new CursorLoader(WatchlistActivity.this, watchlistQueryUri,
                    MAIN_STOCK_PROJECTION, null, null, sortOrder);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
            mWatchlistAdapter.swapCursor(data);
            if (data.getCount() != 0) {
                showWatchlistDataView();
            } else {
                hideWatchlistDataView();
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Cursor> loader) {}
    };

    private LoaderManager.LoaderCallbacks<ContentValues[]> contentValuesLoaderListener =
            new LoaderManager.LoaderCallbacks<ContentValues[]>() {
                @NonNull
                @Override
                public Loader<ContentValues[]> onCreateLoader(int id, @Nullable Bundle args) {
                    URL watchlistRequestUrl = NetworkUtils.getUrl(WatchlistActivity.this);

                    return new WatchlistStockLoader(WatchlistActivity.this,
                            watchlistRequestUrl.toString());
                }

                @Override
                public void onLoadFinished(@NonNull Loader<ContentValues[]> loader, ContentValues[] data) {
                    if (data != null && data.length != 0) {
                        /* Get a handle on the ContentResolver to delete and insert data */
                        ContentResolver watchlistContentResolver = getContentResolver();

                        /* Delete old weather data because we don't need to keep multiple days' data */
                        watchlistContentResolver.delete(StockContract.WatchlistEntry.CONTENT_URI,
                                null, null);

                        /* Insert our new stock data into bullvest's ContentProvider */
                        watchlistContentResolver.bulkInsert(StockContract.WatchlistEntry.CONTENT_URI, data);
                    }
                }

                @Override
                public void onLoaderReset(@NonNull Loader<ContentValues[]> loader) {
                    // Empty
                }
            };

    /**
     * The columns of data that we are displaying within our WatchlistActivity's list of
     * stock data.
     */
    public static final String[] MAIN_STOCK_PROJECTION = {
            StockContract.WatchlistEntry._ID,
            StockContract.WatchlistEntry.COLUMN_SYMBOL,
            StockContract.WatchlistEntry.COLUMN_COMPANY_NAME,
            StockContract.WatchlistEntry.COLUMN_PRICE,
            StockContract.WatchlistEntry.COLUMN_PRICE_CHANGE,
            StockContract.WatchlistEntry.COLUMN_PERCENT_CHANGE,
    };

    /**
     * Indices of the values in the array of Strings above to more quickly be able to
     * access the data from our query. If the order of the Strings above changes, these indices
     * must be adjusted to match the order of the Strings.
     */
    public static final int INDEX_STOCK_SYMBOL = 1;
    public static final int INDEX_STOCK_COMPANY_NAME = 2;
    public static final int INDEX_STOCK_PRICE = 3;
    public static final int INDEX_STOCK_PRICE_CHANGE = 4;
    public static final int INDEX_STOCK_PERCENT_CHANGE = 5;

    /** These ID's will be used to identify the Loader responsible for loading our stock data.*/
    private static final int WATCHLIST_LOADER_ID = 11;
    private static final int DATA_LOADER_ID = 12;

    private WatchlistAdapter mWatchlistAdapter;
    private RecyclerView mRecyclerView;
    private TextView empty;  // temporary, replace with a nice picture or something
    public String stockSymbol;

    /** Timer used to refresh data */
    Timer timer;
    TimerTask timerTask;

    // We are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchlist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Hide both the status bar.
//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//        decorView.setSystemUiVisibility(uiOptions);

        // Displays ads
//        AdView adView = (AdView)findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().tagForChildDirectedTreatment(true).build();
//        adView.loadAd(adRequest);

        empty = (TextView) findViewById(R.id.empty_view);

        /* Receives stock Symbol */
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            stockSymbol = extras.getString("stockSymbol");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /*
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the RecyclerView and toggle the visibility.
         */
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerview_stocks);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        /* setLayoutManager associates the LayoutManager we created above with our RecyclerView */
        mRecyclerView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(false);

        /* New WatchlistAdapter */
        mWatchlistAdapter = new WatchlistAdapter(this, this);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mWatchlistAdapter);
        startTimer();  // Starts the timer

        /*
         * Add a touch helper to the RecyclerView to recognize when a user swipes to delete an item.
         * An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
         * and uses callbacks to signal when a user is performing these actions.
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.ACTION_STATE_IDLE,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // retrieves the symbol as a String
                int position = viewHolder.getAdapterPosition();
                String symbol = ((TextView) mRecyclerView.findViewHolderForAdapterPosition(position)
                        .itemView.findViewById(R.id.symbol)).getText().toString().toUpperCase();

                // Build appropriate uri with String row symbol appended
                Uri uri = StockContract.WatchlistEntry.CONTENT_URI;
                uri = uri.buildUpon().appendPath(symbol).build();

                // Deletes the row via a ContentResolver
                getContentResolver().delete(uri, symbol, null);
                mWatchlistAdapter.notifyItemRemoved(position);

                // Restart the loader to re-query for all tasks after a deletion
                fetchAgain();
                startTimer();
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                // Checks if the User is currently swiping or not
                switch (actionState){
                    case ItemTouchHelper.ACTION_STATE_SWIPE:
                        stopTimerTask();
                        break;
                }
                super.onSelectedChanged(viewHolder, actionState);
            }
        }).attachToRecyclerView(mRecyclerView);
        hideWatchlistDataView();

        // Kick off the loader
        getSupportLoaderManager().initLoader(WATCHLIST_LOADER_ID, null, cursorLoaderListener);
        getSupportLoaderManager().initLoader(DATA_LOADER_ID, null, contentValuesLoaderListener);
    }

    public void fetchAgain(){
        getSupportLoaderManager().restartLoader(WATCHLIST_LOADER_ID, null, cursorLoaderListener);
        getSupportLoaderManager().restartLoader(DATA_LOADER_ID, null, contentValuesLoaderListener);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.watchlist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.account_settings) {
            Intent accountIntent = new Intent(this, AccountActivity.class);
            startActivity(accountIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        // Decides which Activity to open based on user click and finishes the current Activity
        switch (id){
            case R.id.nav_portfolio:
                Intent portfolioIntent = new Intent(this, PortfolioActivity.class);
                startActivity(portfolioIntent);
                finish();
                break;
            case R.id.nav_trade:
                Intent tradeIntent = new Intent(this, TradeActivity.class);
                startActivity(tradeIntent);
                finish();
                break;
            case R.id.nav_hot_list:
                Intent hotListIntent = new Intent(this, HotListActivity.class);
                startActivity(hotListIntent);
                finish();
                break;
            case R.id.nav_research:
                Intent researchIntent = new Intent(this, ResearchActivity.class);
                startActivity(researchIntent);
                finish();
                break;
            case R.id.nav_transactions:
                Intent transactionsIntent = new Intent(this, TransactionsActivity.class);
                startActivity(transactionsIntent);
                finish();
                break;
            case R.id.nav_learning_center:
                Intent learningCenterIntent = new Intent(this, LearningCenterActivity.class);
                startActivity(learningCenterIntent);
                finish();
                break;
        }

        // Closes the drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showWatchlistDataView() {
        empty.setVisibility(View.INVISIBLE);
        /* Finally, make sure the watchlist data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void hideWatchlistDataView() {
        empty.setText("NOT WATCHING");
        empty.setVisibility(View.VISIBLE);
        /* Finally, make sure the watchlist data is invisible */
        mRecyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        stopTimerTask();  // Stops the timer task

        // Sends the symbol to the Research Activity
        Intent researchIntent = new Intent(getApplicationContext(), ResearchActivity.class);
        stockSymbol = WatchlistAdapter.mCursor.getString(WatchlistActivity.INDEX_STOCK_SYMBOL);
        researchIntent.putExtra("stockSymbol", stockSymbol.toLowerCase());
        researchIntent.putExtra("fromPortfolio", false);
        startActivity(researchIntent);
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

        // Schedule the timer, the TimerTask will run every second
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
