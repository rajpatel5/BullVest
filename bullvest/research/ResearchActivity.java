package com.target.android.bullvest.research;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mancj.materialsearchbar.MaterialSearchBar;
import com.target.android.bullvest.R;
import com.target.android.bullvest.account.AccountActivity;
import com.target.android.bullvest.data.StockContract;
import com.target.android.bullvest.hotlist.HotListActivity;
import com.target.android.bullvest.learningcenter.LearningCenterActivity;
import com.target.android.bullvest.portfolio.PortfolioActivity;
import com.target.android.bullvest.research.Charts.ResearchChartCategoryAdapter;
import com.target.android.bullvest.research.News.ResearchNewsAdapter;
import com.target.android.bullvest.research.News.ResearchNewsLoader;
import com.target.android.bullvest.trade.TradeActivity;
import com.target.android.bullvest.transactions.TransactionsActivity;
import com.target.android.bullvest.utils.Constants;
import com.target.android.bullvest.utils.NewsData;
import com.target.android.bullvest.utils.Stock;
import com.target.android.bullvest.watchlist.WatchlistActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ResearchActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Used for logging
    private final String TAG = ResearchActivity.class.getSimpleName();

    // The two loaders
    private LoaderManager.LoaderCallbacks<Stock> stockLoaderListener =
            new LoaderManager.LoaderCallbacks<Stock>() {
                @NonNull
                @Override
                public Loader<Stock> onCreateLoader(int id, @Nullable Bundle args) {
                    Uri baseUri = Uri.parse(stockQuoteUrl);
                    Uri.Builder uriBuilder = baseUri.buildUpon();

                    return new ResearchStockLoader(ResearchActivity.this, uriBuilder.toString());
                }

                @Override
                public void onLoadFinished(@NonNull Loader<Stock> loader, Stock data) {
                    if (!(data == null)){
                        values = new ResearchSetViewValues(data);
                        // Display the symbol in that TextView
                        symbolView.setText(values.getStockSymbol());
                        // Display the company Name in that TextView
                        companyNameView.setText(values.getStockCompanyName());
                        // Display the exchange in that TextView
                        exchangeView.setText(values.getStockExchange());
                        // Display the sector in that TextView
                        sectorView.setText(values.getStockSector());
                        // Display the price and equity value in the TextViews
                        price = Float.parseFloat(values.getStockPrice());
                        priceView.setText("$" + values.getStockPrice());
                        equityValueView.setText("$" + (shares * price));
                        // Display the price change in that TextView
                        priceChangeView.setText(values.getStockPriceChange());
                        // Display the percent change in that TextView
                        percentChangeView.setText("(" + values.getStockPercentChange() + "%)");
                        if (values.isPositiveChange()) {
                            priceView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.priceGreen));
                            priceChangeView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.percentChangeGreen));
                            percentChangeView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.percentChangeGreen));
                        } else {
                            priceView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.priceRed));
                            priceChangeView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.percentChangeRed));
                            percentChangeView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.percentChangeRed));
                        }

                        // Display the opening price in that TextView
                        open = Float.parseFloat(values.getStockOpen());
                        openView.setText(values.getStockOpen());
                        previousClose = Float.parseFloat(values.getStockPreviousClose());
                        // Display the previous close in that TextView
                        previousCloseView.setText(values.getStockPreviousClose());
                        // Display the high price in that TextView
                        highView.setText(values.getStockHigh());
                        // Display the low price in that TextView
                        lowView.setText(values.getStockLow());
                        // Display the market cap in that TextView
                        marketCapView.setText(values.getStockMarketCap());
                        // Display the p/e ratio in that TextView
                        peRatioView.setText(values.getStockPERatio());
                        // Display the 52 week high in that TextView
                        week52HighView.setText(values.getStockWeek52High());
                        // Display the 52 week low in that TextView
                        week52LowView.setText(values.getStockWeek52Low());
                        sendStockData();
                    } else {
                        /** Creates a Toast displaying an error message and switches the stockQuoteUrl to
                         * the previous stock symbol to prevent repeating displaying toast*/
                        Toast.makeText(getApplicationContext(),
                                stockSymbol.toUpperCase() + " Does not Exist",
                                Toast.LENGTH_SHORT).show();

                        stockQuoteUrl = Constants.IEX_REQUEST_URL
                                + symbolView.getText().toString() + Constants.IEX_INDIVIDUAL_PATH_END;
                    }
                    count++;
                }

                @Override
                public void onLoaderReset(@NonNull Loader<Stock> loader) {}
            };

    private LoaderManager.LoaderCallbacks<List<NewsData>> newsLoaderListener =
            new LoaderManager.LoaderCallbacks<List<NewsData>>() {
                @NonNull
                @Override
                public Loader<List<NewsData>> onCreateLoader(int id, @Nullable Bundle args) {
                    Uri baseUri = Uri.parse(Constants.IEX_REQUEST_URL +
                            stockSymbol + Constants.IEX_NEWS_PATH);
                    Uri.Builder uriBuilder = baseUri.buildUpon();

                    return new ResearchNewsLoader(ResearchActivity.this, uriBuilder.toString());
                }

                @Override
                public void onLoadFinished(@NonNull Loader<List<NewsData>> loader, List<NewsData> data) {
                    // Hide loading indicator because the data has been loaded
                    View loadingIndicator = findViewById(R.id.loading_indicator);
                    loadingIndicator.setVisibility(View.GONE);

                    // Set empty state text to display "No stocks found."
                    mEmptyStateTextView.setText(R.string.no_stocks);

                    // If there is a valid list of {@link Stock}s, then add them to the adapter's
                    // data set. This will trigger the ListView to update.
                    if (data != null && !data.isEmpty()) {
                        // Keeps the scroller on current position when data refreshes
                        mNewsAdapter.setNotifyOnChange(false);
                        mNewsAdapter.clear();
                        mNewsAdapter.addAll(data);
                        mNewsAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onLoaderReset(@NonNull Loader<List<NewsData>> loader) {}
            };

    // Search bar
    MaterialSearchBar searchStocks;
    List<String> suggestStockList = new ArrayList<>();

    /* All the text views on the research layout */
    TextView symbolView, companyNameView, exchangeView, sectorView, priceView, priceChangeView,
            percentChangeView, marketHoursView, openView, previousCloseView, highView, lowView,
            marketCapView, peRatioView, week52HighView, week52LowView, positionAmountView,
            equityValueView;

    View afterWatchlistDivider, afterPortfolioPositionInfoDivider;

    LinearLayout portfolioPositionInfo;

    /* Button views */
    Button sellButton, buyButton, watchlistButton;

    /** Constant value for the stock loader ID. */
    private static final int STOCK_LOADER_ID = 9;
    private static final int NEWS_LOADER_ID = 10;
    public static int count = 0;
    static boolean running = true;

    /** Preset Stock symbol, previous close, open price, and URL */
    String stockSymbol = Constants.IEX_DEFAULT_SYMBOL;
    float previousClose;
    float open;
    float price;
    int shares;
    String stockQuoteUrl = Constants.IEX_REQUEST_URL
            + stockSymbol + Constants.IEX_INDIVIDUAL_PATH_END;

    ResearchSetViewValues values;
    /** Adapter for the list of stocks */
    private ResearchNewsAdapter mNewsAdapter;

    /** TextView that is displayed when the list is empty */
    private TextView mEmptyStateTextView;

    // Used to connect to the internet and load data
    NetworkInfo networkInfo;
    LoaderManager loaderManager;

    /** Timer used to refresh data */
    Timer timer;
    TimerTask timerTask;

    //we are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();

    // Chart adapter and fragment manager
    ResearchChartCategoryAdapter adapter;
    private static FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_research);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Hide both the status bar.
//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//        decorView.setSystemUiVisibility(uiOptions);

        fragmentManager = getSupportFragmentManager(); // Get Fragment Manager

        // Displays ads
//        AdView adView = (AdView)findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().tagForChildDirectedTreatment(true).build();
//        adView.loadAd(adRequest);

        // Runs the loader again
        running = true;
        fetchAgain();

        /* Stock information TextViews and their ID's */
        symbolView = (TextView) findViewById(R.id.research_symbol);
        companyNameView = (TextView) findViewById(R.id.research_company_name);
        exchangeView = (TextView) findViewById(R.id.research_exchange);
        sectorView = (TextView) findViewById(R.id.research_sector);
        priceView = (TextView) findViewById(R.id.research_price);
        priceChangeView = (TextView) findViewById(R.id.research_price_change);
        percentChangeView = (TextView) findViewById(R.id.research_percent_change);
        marketHoursView = (TextView) findViewById(R.id.research_market_hours);
        openView = (TextView) findViewById(R.id.open);
        previousCloseView = (TextView) findViewById(R.id.previous_close);
        highView = (TextView) findViewById(R.id.high);
        lowView = (TextView) findViewById(R.id.low);
        marketCapView = (TextView) findViewById(R.id.market_cap);
        peRatioView = (TextView) findViewById(R.id.pe_ratio);
        week52HighView = (TextView) findViewById(R.id.week_52_high);
        week52LowView = (TextView) findViewById(R.id.week_52_low);
        positionAmountView = (TextView) findViewById(R.id.position_amount);
        equityValueView  = (TextView) findViewById(R.id.equity_value);
        afterWatchlistDivider = (View) findViewById(R.id.after_watchlist_divider);
        afterPortfolioPositionInfoDivider = (View) findViewById(R.id.after_position_info_divider);
        portfolioPositionInfo = (LinearLayout) findViewById(R.id.portfolio_position_info);

        portfolioPositionInfo.setVisibility(View.GONE);
        afterPortfolioPositionInfoDivider.setVisibility(View.GONE);
        afterWatchlistDivider.setVisibility(View.GONE);

        /* Receives stock Symbol from the Gainers, Losers, and MostActive fragments
         * and Watchlist, or Portfolio Position list
         * and sets the URL using that symbol to open up the quote */
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getBoolean("fromPortfolio")){
                portfolioPositionInfo.setVisibility(View.VISIBLE);
                afterPortfolioPositionInfoDivider.setVisibility(View.VISIBLE);
                afterWatchlistDivider.setVisibility(View.VISIBLE);

                shares = extras.getInt("shares");
                positionAmountView.setText(shares + "");
            }
            stockSymbol = extras.getString("stockSymbol");
            stockQuoteUrl = Constants.IEX_REQUEST_URL
                    + stockSymbol + Constants.IEX_INDIVIDUAL_PATH_END;
            if (count >= 1) {
                running = true;
                fetchAgain();
            }
        }

        // Find the view pager that will allow the user to swipe between fragments
        final ViewPager viewPager = (ViewPager) findViewById(R.id.research_viewpager);

        // Create an adapter that knows which fragment should be shown on each page
        adapter = new ResearchChartCategoryAdapter(this, fragmentManager);

        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(4);

        // Find the tab layout that shows the tabs
        TabLayout tabLayout = (TabLayout) findViewById(R.id.graph_tabs);

        // Connect the tab layout with the view pager.
        tabLayout.setupWithViewPager(viewPager);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Find a reference to the {@link ListView} in the layout
        final ListView stockListView = (ListView) findViewById(R.id.list);

        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        stockListView.setEmptyView(mEmptyStateTextView);

        // Create a new adapter that takes an empty list of NewsData as input
        mNewsAdapter = new ResearchNewsAdapter(ResearchActivity.this, new ArrayList<NewsData>());

        /* Set the adapter on the {@link ListView}
         * so the list can be populated in the user interface
         */
        stockListView.setAdapter(mNewsAdapter);

        /* Set an item click listener on the ListView, which sends an intent to
         *  open the article link in a browser.
         */
        stockListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String articleUrl = mNewsAdapter.getItem(position).getUrl();
                Intent readArticle = new Intent(Intent.ACTION_VIEW);
                readArticle.setData(Uri.parse(articleUrl));
                startActivity(readArticle);
                stopTimerTask();
            }
        });

        // Displays the market Hours
        displayMarketHours();

        /* Buttons */
        sellButton = (Button) findViewById(R.id.sell);
        buyButton = (Button) findViewById(R.id.buy);
        watchlistButton = (Button) findViewById(R.id.add_watchlist);

        // Sets up search bar and database
        searchStocks = (MaterialSearchBar) findViewById(R.id.search_bar);

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tradeIntent = new Intent(getApplicationContext(), TradeActivity.class);
                tradeIntent.putExtra("postTrade", false);
                tradeIntent.putExtra("stockSymbol", stockSymbol);
                tradeIntent.putExtra("currentPrice", price);
                tradeIntent.putExtra("buy?", false);
                startActivity(tradeIntent);
            }
        });

        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tradeIntent = new Intent(getApplicationContext(), TradeActivity.class);
                tradeIntent.putExtra("postTrade", false);
                tradeIntent.putExtra("stockSymbol", stockSymbol);
                tradeIntent.putExtra("currentPrice", price);
                tradeIntent.putExtra("buy?", true);
                startActivity(tradeIntent);
            }
        });

        watchlistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Used to determine if stock was already inserted
                boolean symbolInserted = false;

                /* Get a handle on the ContentResolver to delete and insert data */
                ContentResolver watchlistContentResolver = getContentResolver();
                String[] projection = new String[] {StockContract.WatchlistEntry.COLUMN_SYMBOL};

                /* Opens the stock Symbols from database */
                Cursor cursor = watchlistContentResolver.query(
                        StockContract.WatchlistEntry.CONTENT_URI,
                        projection, null,null,null);

                // Checks each stock Symbol to see if it matches with the one we are trying to add
                if (cursor != null){
                    cursor.moveToFirst();
                    String dbSymbol;
                    for (int i = 0; i < cursor.getCount(); i++){
                        // Retrieves the symbol
                        dbSymbol = cursor.getString(cursor
                                .getColumnIndexOrThrow(StockContract.WatchlistEntry.COLUMN_SYMBOL));
                        if (dbSymbol.equals(values.getStockSymbol())){
                            // Already inserted
                            symbolInserted = true;

                            // Displays a message to let user know it has already been inserted
                            Toast.makeText(getApplicationContext(), values.getStockSymbol().toUpperCase()
                                    + " was already added to Watchlist", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        else{
                            symbolInserted = false;
                        }
                        cursor.moveToNext();  // Moves to next stock Symbol in the database
                    }
                }

                // If the stock is not already inserted it adds it to the watchlist
                if (!symbolInserted){
                    ContentValues info = new ContentValues();
                    info.put(StockContract.WatchlistEntry.COLUMN_SYMBOL,
                            values.getStockSymbol());
                    info.put(StockContract.WatchlistEntry.COLUMN_COMPANY_NAME,
                            values.getStockCompanyName());
                    info.put(StockContract.WatchlistEntry.COLUMN_PRICE,
                            Double.parseDouble(values.getStockPrice()));
                    info.put(StockContract.WatchlistEntry.COLUMN_PRICE_CHANGE,
                            Double.parseDouble(values.getStockPriceChange()));
                    info.put(StockContract.WatchlistEntry.COLUMN_PERCENT_CHANGE,
                            Double.parseDouble(values.getStockPercentChange()));

                    // Insert a new row for the stock into the provider using the ContentResolver.
                    getContentResolver().insert(StockContract.WatchlistEntry.CONTENT_URI, info);

                    // Displays a message to let user know it has been inserted
                    Toast.makeText(getApplicationContext(), values.getStockSymbol().toUpperCase()
                            + " has been added to Watchlist", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            loaderManager = getLoaderManager();

            // Initialize the loader.
            loaderManager.initLoader(STOCK_LOADER_ID, null, stockLoaderListener);
            loaderManager.initLoader(NEWS_LOADER_ID, null, newsLoaderListener);
        }

        searchStocks.setHint("Stock Symbol"); // Sets a hint

        // Listens for any change in text
        searchStocks.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Adds the user input as last suggestion
                List<String> suggest = new ArrayList<>();
                for (String search: suggestStockList){
                    if (search.toLowerCase().contains(searchStocks.getText().toLowerCase())){
                        suggest.add(search);
                    }
                    searchStocks.setLastSuggestions(suggest);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        searchStocks.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                if (!text.toString().equals("")) {
                    // Remove the position info layout
                    portfolioPositionInfo.setVisibility(View.GONE);
                    afterPortfolioPositionInfoDivider.setVisibility(View.GONE);
                    afterWatchlistDivider.setVisibility(View.GONE);

                    /* Retrieves the Symbol and sets the Url */
                    stockSymbol = searchStocks.getText().toLowerCase();
                    stockQuoteUrl = Constants.IEX_REQUEST_URL
                            + stockSymbol + Constants.IEX_INDIVIDUAL_PATH_END;

                    // Sends the stockSymbol to the fragments
                    running = true;
                    fetchAgain();
                }
                // Disables the Search
                searchStocks.disableSearch();
            }

            @Override
            public void onButtonClicked(int buttonCode) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.research, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.searchable.
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
        // Decides which Activity to open based on user click
        int id = item.getItemId();

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
            case R.id.nav_watchlist:
                Intent watchlistIntent = new Intent(this, WatchlistActivity.class);
                startActivity(watchlistIntent);
                finish();
                break;
            case R.id.nav_hot_list:
                Intent hotListIntent = new Intent(this, HotListActivity.class);
                startActivity(hotListIntent);
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void fetchAgain(){
        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Restarts the loader.
            loaderManager.restartLoader(STOCK_LOADER_ID, null, stockLoaderListener);
            loaderManager.restartLoader(NEWS_LOADER_ID, null, newsLoaderListener);
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
                        displayMarketHours();
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

    public void sendStockData(){
        if (running){
            // Sends the stockSymbol to the chart fragments
            Bundle bundle = new Bundle();
            bundle.putString("stockSymbol", stockSymbol);
            bundle.putFloat("previousClose", previousClose);
            bundle.putFloat("open", open);
            bundle.putFloat("price", price);
            ResearchChartCategoryAdapter.DAILY.setArguments(bundle);
            ResearchChartCategoryAdapter.MONTHLY.setArguments(bundle);
            ResearchChartCategoryAdapter.SIX_MONTHS.setArguments(bundle);
            ResearchChartCategoryAdapter.YEARLY.setArguments(bundle);
            ResearchChartCategoryAdapter.FIVE_YEARS.setArguments(bundle);

            // Creates a fragment Transaction for all fragments
            FragmentTransaction dailyTransaction = fragmentManager.beginTransaction();
            FragmentTransaction monthlyTransaction = fragmentManager.beginTransaction();
            FragmentTransaction sixMonthsTransaction = fragmentManager.beginTransaction();
            FragmentTransaction yearlyTransaction = fragmentManager.beginTransaction();
            FragmentTransaction fiveYearsTransaction = fragmentManager.beginTransaction();

            // Updates the Fragments
            dailyTransaction.detach(ResearchChartCategoryAdapter.DAILY)
                    .attach(ResearchChartCategoryAdapter.DAILY).commit();
            monthlyTransaction.detach(ResearchChartCategoryAdapter.MONTHLY)
                    .attach(ResearchChartCategoryAdapter.MONTHLY).commit();
            sixMonthsTransaction.detach(ResearchChartCategoryAdapter.SIX_MONTHS)
                    .attach(ResearchChartCategoryAdapter.SIX_MONTHS).commit();
            yearlyTransaction.detach(ResearchChartCategoryAdapter.YEARLY)
                    .attach(ResearchChartCategoryAdapter.YEARLY).commit();
            fiveYearsTransaction.detach(ResearchChartCategoryAdapter.FIVE_YEARS)
                    .attach(ResearchChartCategoryAdapter.FIVE_YEARS).commit();
            running = false;
        }
    }

    // Sets the market_hours TextView according to the current time
    public void displayMarketHours(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EE");
        String currentDay = dayFormat.format(calendar.getTime());
        long currentTime = Constants.getTime(); // Gets the current Time

        // Tells the user if markets are open or not based on current time
        if (currentDay.equals(R.string.sunday) || currentDay.equals(R.string.saturday)) {
            marketHoursView.setText(R.string.closed);
        } else {
            if (currentTime >= (4 * Constants.hourToMilli) &&
                    currentTime < (9.5 * Constants.hourToMilli)) {
                marketHoursView.setText(R.string.pre_market);
            } else if (currentTime >= (9.5 * Constants.hourToMilli) &&
                    currentTime < (16 * Constants.hourToMilli)) {
                marketHoursView.setText(R.string.regular);
            } else if (currentTime >= (16 * Constants.hourToMilli) &&
                    currentTime < (20 * Constants.hourToMilli)) {
                marketHoursView.setText(R.string.after_hours);
            } else {
                marketHoursView.setText(R.string.closed);
            }
        }
    }
}