package com.target.android.bullvest.portfolio;

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
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.ads.MobileAds;
import com.target.android.bullvest.R;
import com.target.android.bullvest.account.AccountActivity;
import com.target.android.bullvest.data.BalanceDbHelper;
import com.target.android.bullvest.data.PortfolioChartDbHelper;
import com.target.android.bullvest.data.StockContract;
import com.target.android.bullvest.hotlist.HotListActivity;
import com.target.android.bullvest.learningcenter.LearningCenterActivity;
import com.target.android.bullvest.research.ResearchActivity;
import com.target.android.bullvest.trade.TradeActivity;
import com.target.android.bullvest.transactions.PendingService;
import com.target.android.bullvest.transactions.TransactionsActivity;
import com.target.android.bullvest.utils.NetworkUtils;
import com.target.android.bullvest.watchlist.WatchlistActivity;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class PortfolioActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        PortfolioAdapter.PortfolioAdapterOnClickHandler{

    // Used for logging
    private final String TAG = PortfolioActivity.class.getSimpleName();

    private LoaderManager.LoaderCallbacks<Cursor> cursorLoaderListener =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                @NonNull
                @Override
                public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
                    /* URI for all rows of stock data in my investments table */
                    Uri myInvestmentsQueryUri = StockContract.MyInvestmentsEntry.CONTENT_URI;

                    return new CursorLoader(PortfolioActivity.this, myInvestmentsQueryUri,
                            MAIN_STOCK_PROJECTION, null, null, null);
                }

                @Override
                public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
                    mPortfolioAdapter.swapCursor(data);
                    if (data.getCount() != 0) {
                        showPortfolioDataView();
                    } else {
                        hidePortfolioDataView();
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
                    URL portfolioRequestUrl = NetworkUtils.getPortfolioUrl(PortfolioActivity.this);

                    return new PortfolioStockLoader(PortfolioActivity.this,
                            portfolioRequestUrl.toString());
                }

                @Override
                public void onLoadFinished(@NonNull Loader<ContentValues[]> loader, ContentValues[] data) {
                    if (data != null && data.length != 0) {
                        /* Get a handle on the ContentResolver to delete and insert data */
                        ContentResolver portfolioContentResolver = getContentResolver();

                        /* Delete old weather data because we don't need to keep multiple days' data */
                        portfolioContentResolver.delete(StockContract.MyInvestmentsEntry.CONTENT_URI,
                                null, null);

                        /* Insert our new stock data into BullVest's ContentProvider */
                        portfolioContentResolver.bulkInsert(StockContract.MyInvestmentsEntry.CONTENT_URI, data);
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
            StockContract.MyInvestmentsEntry._ID,
            StockContract.MyInvestmentsEntry.COLUMN_SYMBOL,
            StockContract.MyInvestmentsEntry.COLUMN_SHARES,
            StockContract.MyInvestmentsEntry.COLUMN_PRICE,
            StockContract.MyInvestmentsEntry.COLUMN_PRICE_CHANGE,
            StockContract.MyInvestmentsEntry.COLUMN_PERCENT_CHANGE,
    };

    /**
     * Indices of the values in the array of Strings above to more quickly be able to
     * access the data from our query. If the order of the Strings above changes, these indices
     * must be adjusted to match the order of the Strings.
     */
    public static final int INDEX_SYMBOL = 1;
    public static final int INDEX_SHARES = 2;
    public static final int INDEX_PRICE = 3;
    public static final int INDEX_PRICE_CHANGE = 4;
    public static final int INDEX_PERCENT_CHANGE = 5;

    /** These ID's will be used to identify the Loader responsible for loading our stock data */
    private static final int PORTFOLIO_LOADER_ID = 18;
    private static final int DATA_LOADER_ID = 19;

    private PortfolioAdapter mPortfolioAdapter;
    private RecyclerView mRecyclerView;
    private TextView empty, portfolioBalanceView, portfolioBalanceChangeView,
            portfolioBalancePercentChangeView;
    public String stockSymbol;
    public int shares;

    public static double cash, investmentValue, portfolioBalance, portfolioBalanceChange,
            portfolioBalancePercentChange;

    BalanceDbHelper balanceDatabase;

    // Pie Chart
    PieChart portfolioPieChart;
    // Line Chart
    public LineChart portfolioBalanceChart;
    
    // Used for the Pie Chart
    ArrayList<PieEntry> yValues;

    // Used for the Portfolio Chart
    ArrayList<Entry> balance;
    ArrayList<Entry> startingBalance;
    LineDataSet balanceData;
    LineDataSet startingBalanceData;
    ArrayList<ILineDataSet> dataSets;
    LineData portfolioChartData;

    /** Timer used to refresh data */
    Timer timer;
    TimerTask timerTask;

    // We are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();

    DrawerLayout drawer;
    NavigationView navigationView;
    Toolbar toolbar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portfolio);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setTitle(R.string.Portfolio);
        setSupportActionBar(toolbar);

//        // Hide both the status bar.
//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//        decorView.setSystemUiVisibility(uiOptions);

        empty = (TextView) findViewById(R.id.empty_view);
        portfolioBalanceView = (TextView) findViewById(R.id.portfolio_balance);
        portfolioBalanceChangeView = (TextView) findViewById(R.id.balance_change);
        portfolioBalancePercentChangeView = (TextView) findViewById(R.id.balance_percent_change);
        portfolioPieChart = (PieChart) findViewById(R.id.portfolio_pie_chart);
        portfolioBalanceChart = (LineChart)findViewById(R.id.portfolio_chart);

        // Initializes the Test Banner Ad Id
        MobileAds.initialize(this, "ca-app-pub-3940256099942544/6300978111");

//        // Displays ads
//        AdView adView = (AdView)findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().tagForChildDirectedTreatment(true).build();
//        adView.loadAd(adRequest);

        // Sets up the charts
        setupPieChart();
        setupPortfolioChart();
        showChartBalances();
        showStartingBalance();

        // Retrieves the cash and commissions values from the database
        balanceDatabase = new BalanceDbHelper(this);
        cash = balanceDatabase.getBalance().getCash();

        /*
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the RecyclerView and toggle the visibility.
         */
        mRecyclerView = (RecyclerView)findViewById(R.id.positions);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        /* setLayoutManager associates the LayoutManager we created above with our RecyclerView */
        mRecyclerView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(false);

        /* New PortfolioAdapter */
        mPortfolioAdapter = new PortfolioAdapter(this, this);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mPortfolioAdapter);
        startTimer();  // Starts the timer

        hidePortfolioDataView();

        // Kick off the loader
        getSupportLoaderManager().initLoader(PORTFOLIO_LOADER_ID, null, cursorLoaderListener);
        getSupportLoaderManager().initLoader(DATA_LOADER_ID, null, contentValuesLoaderListener);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Starts a service for the overall portfolio chart
        if (!PortfolioChartService.serviceOn){
            startService(new Intent(PortfolioActivity.this, PortfolioChartService.class));
        }
        // Starts a service for all the pending transactions
        startService(new Intent(PortfolioActivity.this, PendingService.class));
    }

    public void fetchAgain(){
        getSupportLoaderManager().restartLoader(PORTFOLIO_LOADER_ID, null, cursorLoaderListener);
        getSupportLoaderManager().restartLoader(DATA_LOADER_ID, null, contentValuesLoaderListener);
    }

    public void updatePortfolioBalance(){
        Cursor cursor = getContentResolver().query(StockContract.MyInvestmentsEntry.CONTENT_URI,
                null,null, null,null);

        // Adds each starting balance point into an ArrayList
        PortfolioChartDbHelper portfolioChartBalanceDatabase;
        portfolioChartBalanceDatabase = new PortfolioChartDbHelper(this);

        try {
            if (cursor != null) {
                investmentValue = 0;
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++) {
                    // Retrieves the components of the my investments list
                    final double price = cursor.getDouble(cursor
                            .getColumnIndexOrThrow(StockContract.MyInvestmentsEntry.COLUMN_PRICE));
                    final int shares = cursor.getInt(cursor
                            .getColumnIndexOrThrow(StockContract.MyInvestmentsEntry.COLUMN_SHARES));

                    investmentValue += (price * shares);
                    cursor.moveToNext();  // Moves to next stock Symbol in the database
                }

                cash = balanceDatabase.getBalance().getCash();
                portfolioBalance = investmentValue + cash;
                portfolioBalanceChange = portfolioBalance -
                        portfolioChartBalanceDatabase.getStartingBalance().getBalance();
                portfolioBalancePercentChange = (portfolioBalanceChange /
                        portfolioChartBalanceDatabase.getStartingBalance().getBalance()) * 100;

                portfolioBalanceChangeView.setText(formatDoubleDecimal(portfolioBalanceChange));
                portfolioBalanceView.setText("$" + formatDoubleDecimal(portfolioBalance));
                portfolioBalancePercentChangeView.setText(" (" +
                        formatDoubleDecimal(portfolioBalancePercentChange) + "%)");

                if (portfolioBalance > portfolioChartBalanceDatabase.getStartingBalance().getBalance()){
                    portfolioBalanceChangeView.setTextColor(ContextCompat.getColor(
                            PortfolioActivity.this, R.color.priceGreen));
                    portfolioBalancePercentChangeView.setTextColor(ContextCompat.getColor(
                            PortfolioActivity.this, R.color.priceGreen));
                } else if (portfolioBalance < portfolioChartBalanceDatabase.getStartingBalance().getBalance()) {
                    portfolioBalanceChangeView.setTextColor(ContextCompat.getColor(
                            PortfolioActivity.this, R.color.priceRed));
                    portfolioBalancePercentChangeView.setTextColor(ContextCompat.getColor(
                            PortfolioActivity.this, R.color.priceRed));
                }

                int colors[] = {ContextCompat.getColor(PortfolioActivity.this, R.color.colorPrimary),
                        ContextCompat.getColor(PortfolioActivity.this, R.color.colorPrimaryDark)};

                yValues = new ArrayList<>();
                if (cash != 0){
                    yValues.add(new PieEntry((float)cash, "Cash"));
                }
                if (investmentValue != 0) {
                    yValues.add(new PieEntry((float) investmentValue, "Investments"));
                }

                PieDataSet portfolioPieDataSet = new PieDataSet(yValues, "");
                portfolioPieDataSet.setSliceSpace(3f);
                portfolioPieDataSet.setSelectionShift(0f);
                portfolioPieDataSet.setColors(colors);

                PieData portfolioPieData = new PieData(portfolioPieDataSet);
                portfolioPieData.setValueTextSize(12f);
                portfolioPieData.setValueTextColor(ContextCompat.getColor(
                        PortfolioActivity.this, R.color.pieTextColor));
                portfolioPieData.setValueFormatter(new PercentFormatter());

                portfolioPieChart.setData(portfolioPieData);
            }
        } finally {
            cursor.close();
        }
    }

    /**
     * Return the formatted value string
     * showing 2 decimal places (i.e. "3.25") from a decimal value.
     */
    private static String formatDoubleDecimal(double item) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return decimalFormat.format(item);
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
        getMenuInflater().inflate(R.menu.portfolio, menu);
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
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        // Decides which Activity to open based on user click
        switch (id){
            case R.id.nav_trade:
                Intent tradeIntent = new Intent(this, TradeActivity.class);
                startActivity(tradeIntent);
                break;
            case R.id.nav_watchlist:
                Intent watchlistIntent = new Intent(this, WatchlistActivity.class);
                startActivity(watchlistIntent);
                break;
            case R.id.nav_hot_list:
                Intent hotListIntent = new Intent(this, HotListActivity.class);
                startActivity(hotListIntent);
                break;
            case R.id.nav_research:
                Intent researchIntent = new Intent(this, ResearchActivity.class);
                startActivity(researchIntent);
                break;
            case R.id.nav_transactions:
                Intent transactionsIntent = new Intent(this, TransactionsActivity.class);
                startActivity(transactionsIntent);
                break;
            case R.id.nav_learning_center:
                Intent learningCenterIntent = new Intent(this, LearningCenterActivity.class);
                startActivity(learningCenterIntent);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showPortfolioDataView() {
        empty.setVisibility(View.INVISIBLE);
        /* Finally, make sure the portfolio data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void hidePortfolioDataView() {
        empty.setText("NO INVESTMENTS FOUND");
        empty.setVisibility(View.VISIBLE);
        /* Finally, make sure the portfolio data is invisible */
        mRecyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        stopTimerTask();  // Stops the timer task

        // Sends the symbol to the Research Activity
        Intent researchIntent = new Intent(getApplicationContext(), ResearchActivity.class);
        stockSymbol = PortfolioAdapter.mCursor.getString(PortfolioActivity.INDEX_SYMBOL);
        shares = PortfolioAdapter.mCursor.getInt(PortfolioActivity.INDEX_SHARES);
        researchIntent.putExtra("stockSymbol", stockSymbol.toLowerCase());
        researchIntent.putExtra("fromPortfolio", true);
        researchIntent.putExtra("shares", shares);
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
                        updatePortfolioBalance();
                        showChartBalances();
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

    public void setupPieChart(){
        // Sets up the Pie chart displaying the cash and investment values
        portfolioPieChart.getDescription().setEnabled(false);
        portfolioPieChart.setExtraOffsets(0, 0, 0, 0);
        portfolioPieChart.setDragDecelerationFrictionCoef(0.95f);  // Smooth dragging
        portfolioPieChart.setDrawHoleEnabled(true); // Adds a hole in the chart
        portfolioPieChart.setHoleColor(ContextCompat.getColor(
                PortfolioActivity.this, R.color.holeColor));
        portfolioPieChart.setTransparentCircleRadius(58f); // Creates a 3d effect
        portfolioPieChart.animateY(1500, Easing.EasingOption.EaseInOutCubic);
        portfolioPieChart.setUsePercentValues(true); // displays the percentage

        // Customize Pie chart legend
        Legend pieChartLegend = portfolioPieChart.getLegend();
        pieChartLegend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        pieChartLegend.setTextSize(13f);
    }

    public void setupPortfolioChart(){
        // Finds chart and sets the rules
        portfolioBalanceChart.setScaleEnabled(false);
        portfolioBalanceChart.setDragEnabled(true);
        portfolioBalanceChart.setClickable(true);

        // Does not display the Axes labels
        portfolioBalanceChart.getAxisLeft().setDrawLabels(false);
        portfolioBalanceChart.getAxisRight().setDrawLabels(false);
        portfolioBalanceChart.getXAxis().setDrawLabels(false);
        portfolioBalanceChart.getAxisLeft().setDrawGridLines(false);
        portfolioBalanceChart.getXAxis().setDrawGridLines(false);
        portfolioBalanceChart.getAxisRight().setDrawGridLines(false);

        // Does not display the Axes
        XAxis xAxis = portfolioBalanceChart.getXAxis();
        xAxis.setEnabled(false);
        YAxis yAxis = portfolioBalanceChart.getAxisLeft();
        yAxis.setEnabled(false);
        YAxis yAxis2 = portfolioBalanceChart.getAxisRight();
        yAxis2.setEnabled(false);

        // Does not display the borders or the Grid
        portfolioBalanceChart.setDrawBorders(false);
        portfolioBalanceChart.setDrawGridBackground(false);

        // Does not display the Legend or description
        portfolioBalanceChart.getLegend().setEnabled(false);
        // No description text
        portfolioBalanceChart.getDescription().setEnabled(false);
        portfolioBalanceChart.animateX(2000);
    }

    public void showStartingBalance(){
        // Creates an ArrayList of Entry Points and adds new Entries
        startingBalance = new ArrayList<>();

        // Adds each starting balance point into an ArrayList
        PortfolioChartDbHelper portfolioChartBalanceDatabase;
        portfolioChartBalanceDatabase = new PortfolioChartDbHelper(this);
        for (int i = 0; i < portfolioChartBalanceDatabase.getSize(); i++) {
            startingBalance.add(new Entry((float)(i), startingBalancePoint()));
        }
        // Creates a new Line data set named 'Starting Balance'
        startingBalanceData = new LineDataSet(startingBalance, "Starting Balance");

        // Changes the size of the points and doesn't display the horizontal indicator
        // Changes the radius of the points and makes the line invisible using transparent color
        startingBalanceData.setDrawCircles(true);
        startingBalanceData.setDrawValues(false);
        startingBalanceData.setCircleRadius(1f);
        startingBalanceData.setCircleColor(ContextCompat.getColor(this, R.color.previousCloseColor));
        startingBalanceData.setCircleColorHole(ContextCompat.getColor(this, R.color.previousCloseColor));
        startingBalanceData.setColor(android.R.color.transparent);
        startingBalanceData.setDrawHorizontalHighlightIndicator(false);
        startingBalanceData.setDrawVerticalHighlightIndicator(false);
        startingBalanceData.setHighLightColor(ContextCompat.getColor(this, R.color.highlightColor));

        dataSets.add(startingBalanceData);
        portfolioChartData.addDataSet(startingBalanceData);

        portfolioBalanceChart.setData(portfolioChartData);
    }

    public void showChartBalances(){
        // Creates an ArrayList of Entry Points and adds new Entries
        balance = new ArrayList<>();

        // Adds each previous close price point into an ArrayList
        PortfolioChartDbHelper portfolioChartBalanceDatabase;
        portfolioChartBalanceDatabase = new PortfolioChartDbHelper(this);
        for (int i = 0; i < portfolioChartBalanceDatabase.getSize(); i++) {
            balance.add(new Entry((float)i, (float)portfolioChartBalanceDatabase.getBalance(i)));
        }

        // Creates a new Line data set named 'Stock Prices'
        balanceData = new LineDataSet(balance, "Balances");

        // Changes the size of the points and doesn't display the horizontal indicator
        balanceData.setDrawCircles(false);
        balanceData.setLineWidth(1.3f);
        balanceData.setDrawHorizontalHighlightIndicator(false);
        balanceData.setHighLightColor(ContextCompat.getColor(this, R.color.highlightColor));
        balanceData.setDrawFilled(true);

        double finalBalance = portfolioChartBalanceDatabase.getFinalBalance();

        // Sets the color of the line according to the price and the previous close
        if (finalBalance > (double)startingBalancePoint()) {
            balanceData.setColor(ContextCompat.getColor(this, R.color.graphGreen));
            balanceData.setFillDrawable(ContextCompat.
                    getDrawable(this, R.drawable.chart_gradient_green));
        } else if (finalBalance < (double)startingBalancePoint()){
            balanceData.setColor(ContextCompat.getColor(this, R.color.graphRed));
            balanceData.setFillDrawable(ContextCompat.
                    getDrawable(this, R.drawable.chart_gradient_red));
        } else if (finalBalance == (double)startingBalancePoint()){
            balanceData.setColor(ContextCompat.getColor(this, R.color.graphNeutral));
            balanceData.setFillDrawable(ContextCompat.
                    getDrawable(this, R.drawable.chart_gradient_neutral));
        }

        // Creates a new ArrayList of data sets and creates new Line Data
        dataSets = new ArrayList<>();
        dataSets.add(balanceData);

        portfolioChartData = new LineData();
        portfolioChartData.addDataSet(balanceData);

        // Does not display the price values
        balanceData.setDrawValues(false);

        // Sets the Data
        portfolioBalanceChart.setData(portfolioChartData);
    }

    /** Returns the Starting Balance Point */
    public float startingBalancePoint(){
        PortfolioChartDbHelper portfolioChartBalanceDatabase;
        portfolioChartBalanceDatabase = new PortfolioChartDbHelper(this);

        return (float) portfolioChartBalanceDatabase.getStartingBalance().getBalance();
    }
}
