package com.target.android.bullvest.trade;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.target.android.bullvest.R;
import com.target.android.bullvest.account.AccountActivity;
import com.target.android.bullvest.data.BalanceDbHelper;
import com.target.android.bullvest.data.StockContract;
import com.target.android.bullvest.hotlist.HotListActivity;
import com.target.android.bullvest.learningcenter.LearningCenterActivity;
import com.target.android.bullvest.portfolio.PortfolioActivity;
import com.target.android.bullvest.research.ResearchActivity;
import com.target.android.bullvest.transactions.TransactionsActivity;
import com.target.android.bullvest.utils.Constants;
import com.target.android.bullvest.utils.Stock;
import com.target.android.bullvest.watchlist.WatchlistActivity;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TradeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Used for logging
    private final String TAG = TradeActivity.class.getSimpleName();

    // Loads the Current Price
    private LoaderManager.LoaderCallbacks<Stock> stockLoaderListener =
            new LoaderManager.LoaderCallbacks<Stock>() {
                @NonNull
                @Override
                public Loader<Stock> onCreateLoader(int id, @Nullable Bundle args) {
                    Uri baseUri = Uri.parse(stockQuoteUrl);
                    Uri.Builder uriBuilder = baseUri.buildUpon();

                    return new TradeStockLoader(TradeActivity.this, uriBuilder.toString());
                }

                @Override
                public void onLoadFinished(@NonNull Loader<Stock> loader, Stock data) {
                    // Checks to see if shares entry box is empty or null
                    if (sharesEntry.getText().toString().equals("") || sharesEntry.getText() == null){
                        shares = 0;
                    } else {
                        shares = Integer.parseInt(sharesEntry.getText().toString());
                    }

                    // Checks to see if data is null or not
                    if (!(data == null)){
                        // Retrieves the price and displays it rounded to two decimal places
                        currentPrice = (float)data.getPrice();
                        currentPriceView.setText("$" + formatFloatDecimal(currentPrice));
                        if (buySelected && !sellSelected) {
                            // Sets the estimated Value
                            estimateValueView.setText("$" +
                                    formatFloatDecimal((currentPrice * shares) + commissions));
                        } else if (!buySelected && sellSelected){
                            // Sets the estimated Value
                            estimateValueView.setText("$" +
                                    formatFloatDecimal((currentPrice * shares) - commissions));
                        } else {
                            // Sets the estimated Value
                            estimateValueView.setText("$" +
                                    formatFloatDecimal(currentPrice * shares));
                        }
                    } else {
                        // Sets the current Price to $0.00
                        currentPrice = 0;
                        currentPriceView.setText("$" + currentPrice);
                        if (buySelected && !sellSelected) {
                            // Sets the estimated Value
                            estimateValueView.setText("$" +
                                    formatFloatDecimal((currentPrice * shares) + commissions));
                        } else if (!buySelected && sellSelected){
                            // Sets the estimated Value
                            estimateValueView.setText("$" +
                                    formatFloatDecimal((currentPrice * shares) - commissions));
                        } else {
                            // Sets the estimated Value
                            estimateValueView.setText("$" +
                                    formatFloatDecimal(currentPrice * shares));
                        }
                    }
                }

                @Override
                public void onLoaderReset(@NonNull Loader<Stock> loader) {}
            };

    /** Constant value for the stock loader ID. */
    private static final int STOCK_LOADER_ID = 13;

    double cash;
    float currentPrice, commissions;
    int shares = 1;
    public String stockSymbol, purchase, currentDay, orderType;
    String stockQuoteUrl = Constants.IEX_REQUEST_URL + stockSymbol + Constants.IEX_INDIVIDUAL_PATH_END;

    boolean buy, buySelected, sellSelected, postTrade;
    boolean marketChecked, stopIsVisible, limitIsVisible, timeFrameIsVisible  = false;

    public static long marketOpenTime = (9 * Constants.hourToMilli) + (30 * Constants.minuteToMilli);
    public static long marketCloseTime = 16 * Constants.hourToMilli;

    // All the views required to change from trade to post-trade
    TextView cashView, currentPriceView, estimateValueView, commissionsView, shareAmountView,
            executionPriceView, totalView, orderTypeView, postStopPriceView,
            postLimitPriceView, postCommissionsView, tradeStatementView, orderStatusView, goodTillView;
    Button sellTrade, buyTrade;
    CheckBox market, limit, stopLoss, stopLimit, goodForTheDay, goodTillCancelled;
    EditText symbolEntry, sharesEntry, stopEntry, limitEntry;
    RelativeLayout executed;
    ScrollView trading;
    View stopPriceLayoutDivider, limitPriceLayoutDivider, timeFrameLayoutDivider;
    LinearLayout stopOrder, limitOrder, goodTill, stopPriceLayout, limitPriceLayout, timeFrameLayout;

    Uri alarmSound;

    // Used to connect to the internet and load data
    NetworkInfo networkInfo;
    LoaderManager loaderManager;

    BalanceDbHelper balanceDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade);
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

        balanceDatabase = new BalanceDbHelper(this);
        cash = balanceDatabase.getBalance().getCash();
        commissions = (float)balanceDatabase.getBalance().getCommissions();
        alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Gets the day
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EE");
        currentDay = dayFormat.format(calendar.getTime());

        // Initializes all the views
        cashView = (TextView) findViewById(R.id.cash);
        currentPriceView = (TextView) findViewById(R.id.current_price);
        estimateValueView = (TextView) findViewById(R.id.estimate_value);
        commissionsView = (TextView) findViewById(R.id.commissions);
        symbolEntry = (EditText) findViewById(R.id.symbol_field);
        sharesEntry = (EditText) findViewById(R.id.shares_field);
        buyTrade = (Button) findViewById(R.id.buy_trade);
        sellTrade = (Button) findViewById(R.id.sell_trade);
        market = (CheckBox) findViewById(R.id.market);
        limit = (CheckBox) findViewById(R.id.limit);
        stopLoss = (CheckBox) findViewById(R.id.stop_loss);
        stopLimit = (CheckBox) findViewById(R.id.stop_limit);
        goodForTheDay = (CheckBox) findViewById(R.id.for_day);
        goodTillCancelled = (CheckBox) findViewById(R.id.cancelled);
        stopOrder = (LinearLayout) findViewById(R.id.stop_order);
        limitOrder = (LinearLayout) findViewById(R.id.limit_order);
        goodTill = (LinearLayout) findViewById(R.id.good_till);
        stopEntry = (EditText) findViewById(R.id.stop_field);
        limitEntry = (EditText) findViewById(R.id.limit_field);
        trading = (ScrollView) findViewById(R.id.trading);
        executed = (RelativeLayout) findViewById(R.id.executed);
        shareAmountView = (TextView) findViewById(R.id.share_amount);
        executionPriceView = (TextView) findViewById(R.id.execution_price);
        totalView = (TextView) findViewById(R.id.total);
        orderTypeView = (TextView) findViewById(R.id.order_type);
        postStopPriceView = (TextView) findViewById(R.id.post_stop_price);
        postLimitPriceView = (TextView) findViewById(R.id.post_limit_price);
        postCommissionsView = (TextView) findViewById(R.id.post_commissions);
        tradeStatementView = (TextView) findViewById(R.id.trade_statement);
        stopPriceLayoutDivider = (View) findViewById(R.id.stop_price_layout_divider);
        stopPriceLayout = (LinearLayout) findViewById(R.id.stop_price_layout);
        limitPriceLayoutDivider = (View) findViewById(R.id.limit_price_layout_divider);
        limitPriceLayout = (LinearLayout) findViewById(R.id.limit_price_layout);
        timeFrameLayoutDivider = (View) findViewById(R.id.time_frame_Layout_divider);
        timeFrameLayout = (LinearLayout) findViewById(R.id.time_frame_layout);
        orderStatusView = (TextView) findViewById(R.id.order_status);
        goodTillView = (TextView) findViewById(R.id.time_frame);

        // Retrieves the cash and commissions values from the database
        balanceDatabase = new BalanceDbHelper(this);
        cash = balanceDatabase.getBalance().getCash();
        commissions = (float)balanceDatabase.getBalance().getCommissions();

        // Sets it to the correct layout
        trading.setVisibility(View.VISIBLE);
        executed.setVisibility(View.GONE);

        // Sets the visibility of post Stop Price and Limit Price layouts and Time Frame to GONE
        stopPriceLayout.setVisibility(View.GONE);
        stopPriceLayoutDivider.setVisibility(View.GONE);
        limitPriceLayout.setVisibility(View.GONE);
        limitPriceLayoutDivider.setVisibility(View.GONE);
        timeFrameLayout.setVisibility(View.GONE);
        timeFrameLayoutDivider.setVisibility(View.GONE);

        // Presents some of the values
        sharesEntry.setText("1");
        cashView.setText("$" + formatDoubleDecimal(cash));
        currentPriceView.setText("$0.00");
        commissionsView.setText("$" + formatFloatDecimal(commissions));

        // Doesn't display these until user needs them
        stopOrder.setVisibility(View.GONE);
        limitOrder.setVisibility(View.GONE);
        goodTill.setVisibility(View.GONE);

        /* Receives stock Symbol, order Type, and current Price from the Research Activity
         * and sets the shares to 1 and symbol to the stock symbol given */
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            postTrade = extras.getBoolean("postTrade");
            // Checks if this is post trade
            if (postTrade){
                trading.setVisibility(View.GONE);
                executed.setVisibility(View.VISIBLE);

                // Retrieves the order and symbol
                String orders = extras.getString("orders");
                String symbol = extras.getString("stockSymbol");

                // Checks the type of order (Buy or Sell)
                if (orders.equals("Buy")){
                    tradeStatementView.setText(orders + " " + symbol);
                } else if (orders.equals("Sell")){
                    tradeStatementView.setText(orders + " " + symbol);
                }

                // Retrieves the share amount, execution price, and order type
                String shares = extras.getString("shares");
                String executionPrice = extras.getString("executionPrice");
                String orderType = extras.getString("orderType");

                // Sets the values of shares, execution price, and order type
                shareAmountView.setText(shares);
                if (executionPrice.equals("-N/A-")){
                    executionPriceView.setText("-N/A-");
                } else {
                    executionPriceView.setText("$" + formatDoubleDecimal(Double.parseDouble(executionPrice)));
                }
                orderTypeView.setText(orderType);

                String stopPrice = extras.getString("stopPrice");
                String limitPrice = extras.getString("limitPrice");
                String timeFrame = extras.getString("timeFrame");

                // Check to see if stop Price is empty or not
                if (stopPrice.equals("") || stopPrice == null){
                    stopPriceLayout.setVisibility(View.GONE);
                    stopPriceLayoutDivider.setVisibility(View.GONE);
                } else {
                    stopPriceLayout.setVisibility(View.VISIBLE);
                    stopPriceLayoutDivider.setVisibility(View.VISIBLE);
                    postStopPriceView.setText("$" + stopPrice);
                }

                // Check to see if limit Price is empty or not
                if (limitPrice.equals("") || limitPrice == null){
                    limitPriceLayout.setVisibility(View.GONE);
                    limitPriceLayoutDivider.setVisibility(View.GONE);
                } else {
                    limitPriceLayout.setVisibility(View.VISIBLE);
                    limitPriceLayoutDivider.setVisibility(View.VISIBLE);
                    postLimitPriceView.setText("$" + limitPrice);
                }

                // Check to see if time Frame is empty or not
                if (timeFrame == null || timeFrame.equals("")){
                    timeFrameLayout.setVisibility(View.GONE);
                    timeFrameLayoutDivider.setVisibility(View.GONE);
                } else {
                    timeFrameLayout.setVisibility(View.VISIBLE);
                    timeFrameLayoutDivider.setVisibility(View.VISIBLE);
                    goodTillView.setText(timeFrame);
                }

                postCommissionsView.setText("$" + commissions);
                String orderStatus = extras.getString("orderStatus");
                orderStatusView.setText(orderStatus);

                // Sets the total value depending on the order status
                if (orderStatus.equals("Pending")){
                    orderStatusView.setTextColor(ContextCompat.
                            getColor(TradeActivity.this, R.color.pending));
                    totalView.setText("-N/A-");
                } else if (orderStatus.equals("Filled")){
                    orderStatusView.setTextColor(ContextCompat.
                            getColor(TradeActivity.this, R.color.filled));
                    if (orders.equals("Buy")) {
                        totalView.setText("$" +
                                formatDoubleDecimal((Double.parseDouble(executionPrice) *
                                        Integer.parseInt(shares)) + commissions));
                    } else {
                        totalView.setText("$" +
                                formatDoubleDecimal((Double.parseDouble(executionPrice) *
                                        Integer.parseInt(shares)) - commissions));
                    }
                }
            } else {
                // Retrieves the necessary values
                stockSymbol = extras.getString("stockSymbol");
                currentPrice = Float.parseFloat(formatFloatDecimal(extras.getFloat("currentPrice")));
                buy = extras.getBoolean("buy?");
                symbolEntry.setText(stockSymbol.toUpperCase());  // Sets the symbol
                currentPriceView.setText("$" + currentPrice);  // Displays the current Price
                stockQuoteUrl = Constants.IEX_REQUEST_URL
                        + stockSymbol + Constants.IEX_INDIVIDUAL_PATH_END;

                // Checks if it was a buy or sell order
                if (buy) {
                    buyTrade.setBackgroundResource(R.drawable.selected_trade_order);
                    buySelected = true;
                    sellSelected = false;
                    // Sets the estimated value
                    estimateValueView.setText("$" + formatFloatDecimal((currentPrice * shares) + commissions));
                } else {
                    sellTrade.setBackgroundResource(R.drawable.selected_trade_order);
                    sellSelected = true;
                    buySelected = false;
                    // Sets the estimated value
                    estimateValueView.setText("$" + formatFloatDecimal((currentPrice * shares) - commissions));
                }
            }
        } else {
            // Sets the estimated value
            estimateValueView.setText("$" + formatFloatDecimal(currentPrice * shares));
        }

        shares = Integer.parseInt(sharesEntry.getText().toString());

        // All the Edit texts and checkboxes
        entryBoxes();
        orderTypeCheckBoxes();
        timeFrameCheckBoxes();
        orderTypeButtons();

        // Sets the code for the FAB
        final FloatingActionButton submit = (FloatingActionButton) findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard(TradeActivity.this);  // Hides the keyboard
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                // User clicked "Cancel" button, navigate to parent activity.
                                trading.setVisibility(View.VISIBLE);
                                executed.setVisibility(View.GONE);
                                submit.setVisibility(View.VISIBLE);
                            }
                        };
                fetchAgain();
                if (shouldProceed()){
                    // Displays a dialog box before submitting order
                    showBeforeSubmitDialog(discardButtonClickListener);
                    submit.setVisibility(View.GONE);
                }
            }
        });

        // If this is post trade then the first layout is set to GONE
        if (postTrade){
            submit.setVisibility(View.GONE);
        }

        // Used to switch to different menus
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        networkInfo = connMgr.getActiveNetworkInfo();
    }

    /**
     * Restarts the loader
     */
    public void fetchAgain(){
        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            loaderManager = getLoaderManager();

            // Restarts the loader.
            loaderManager.restartLoader(STOCK_LOADER_ID, null, stockLoaderListener);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
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
        getMenuInflater().inflate(R.menu.trade, menu);
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
            case R.id.nav_portfolio:
                Intent portfolioIntent = new Intent(this, PortfolioActivity.class);
                startActivity(portfolioIntent);
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

    /**
     * Return the formatted value string
     * showing 2 decimal places (i.e. "3.25") from a decimal value.
     */
    private static String formatFloatDecimal(float item) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return decimalFormat.format(item);
    }

    /**
     * Return the formatted value string
     * showing 2 decimal places (i.e. "3.25") from a decimal value.
     */
    private static String formatDoubleDecimal(double item) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return decimalFormat.format(item);
    }

    /**
     * Determines which button was clicked (Buy or Sell)
     */
    private void orderTypeButtons(){
        buyTrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.buy_trade) {
                    buyTrade.setBackgroundResource(R.drawable.selected_trade_order);
                    buySelected = true;
                    sellSelected = false;
                    sellTrade.setBackgroundResource(R.drawable.trade_buttons);
                    hideKeyboard(TradeActivity.this);
                    fetchAgain();  // Used to refresh Est.Value
                }
            }
        });

        sellTrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.sell_trade) {
                    sellTrade.setBackgroundResource(R.drawable.selected_trade_order);
                    buyTrade.setBackgroundResource(R.drawable.trade_buttons);
                    sellSelected = true;
                    buySelected = false;
                    hideKeyboard(TradeActivity.this);
                    fetchAgain();  // Used to refresh Est.Value
                }
            }
        });
    }

    /**
     * Determines which Order type was checked (Market, Limit, Stop Loss or Stop Limit)
     * Depending on which is selected the views are visible to the user
     */
    private void orderTypeCheckBoxes(){
        // Checks if the checkbox is checked
        market.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    orderType = "market";
                    marketChecked = true;
                    limit.setChecked(false);
                    stopLoss.setChecked(false);
                    stopLimit.setChecked(false);
                    stopOrder.setVisibility(View.GONE);
                    limitOrder.setVisibility(View.GONE);
                    goodTill.setVisibility(View.GONE);
                    orderTypeView.setText(R.string.market);
                    limitIsVisible = false;
                    stopIsVisible = false;
                    timeFrameIsVisible = false;
                    timeFrameLayout.setVisibility(View.GONE);
                    timeFrameLayoutDivider.setVisibility(View.GONE);
                    goodTillCancelled.setChecked(false);
                    goodForTheDay.setChecked(false);
                    hideKeyboard(TradeActivity.this);
                }
            }
        });

        // Checks if the checkbox is checked
        limit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    orderType = "limit";
                    marketChecked = false;
                    market.setChecked(false);
                    stopLoss.setChecked(false);
                    stopLimit.setChecked(false);
                    stopOrder.setVisibility(View.GONE);
                    limitOrder.setVisibility(View.VISIBLE);
                    goodTill.setVisibility(View.VISIBLE);
                    orderTypeView.setText(R.string.limit);
                    limitIsVisible = true;
                    stopIsVisible = false;
                    timeFrameIsVisible = true;
                    hideKeyboard(TradeActivity.this);
                }
            }
        });

        // Checks if the checkbox is checked
        stopLoss.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    orderType = "stop loss";
                    marketChecked = false;
                    limit.setChecked(false);
                    market.setChecked(false);
                    stopLimit.setChecked(false);
                    stopOrder.setVisibility(View.VISIBLE);
                    limitOrder.setVisibility(View.GONE);
                    goodTill.setVisibility(View.VISIBLE);
                    orderTypeView.setText(R.string.stop_loss);
                    stopIsVisible = true;
                    limitIsVisible = false;
                    timeFrameIsVisible = true;
                    hideKeyboard(TradeActivity.this);
                }
            }
        });

        // Checks if the checkbox is checked
        stopLimit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    orderType = "stop limit";
                    marketChecked = false;
                    limit.setChecked(false);
                    stopLoss.setChecked(false);
                    market.setChecked(false);
                    stopOrder.setVisibility(View.VISIBLE);
                    limitOrder.setVisibility(View.VISIBLE);
                    goodTill.setVisibility(View.VISIBLE);
                    orderTypeView.setText(R.string.stop_limit);
                    stopIsVisible = true;
                    limitIsVisible = true;
                    timeFrameIsVisible = true;
                    hideKeyboard(TradeActivity.this);
                }
            }
        });
    }

    /**
     * Determines which Time Frame was checked (Good For the Day or Good Till Cancelled)
     * Depending on which is selected the views are visible to the user
     */
    private void timeFrameCheckBoxes(){
        goodForTheDay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    timeFrameLayout.setVisibility(View.VISIBLE);
                    timeFrameLayoutDivider.setVisibility(View.VISIBLE);
                    goodTillCancelled.setChecked(false);
                    goodTillView.setText("Good For the Day");
                    hideKeyboard(TradeActivity.this);
                }
            }
        });

        goodTillCancelled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    timeFrameLayout.setVisibility(View.VISIBLE);
                    timeFrameLayoutDivider.setVisibility(View.VISIBLE);
                    goodForTheDay.setChecked(false);
                    goodTillView.setText("Good Till Cancelled");
                    hideKeyboard(TradeActivity.this);
                }
            }
        });
    }

    /**
     * Determines whether or not the entry boxes are empty
     * If not then it displays the estimated cost
     */
    private void entryBoxes(){
        symbolEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Empty
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    currentPrice = 0;  // Sets current Price to 0
                    // Checks to see if shares entry box is empty or null
                    if (sharesEntry.getText().toString().equals("") || sharesEntry.getText() == null){
                        shares = 0;
                    } else {
                        shares = Integer.parseInt(sharesEntry.getText().toString());
                    }
                    if (buySelected && !sellSelected) {
                        // Sets the estimated Value
                        estimateValueView.setText("$" +
                                formatFloatDecimal((currentPrice * shares) + commissions));
                    } else if (!buySelected && sellSelected){
                        // Sets the estimated Value
                        estimateValueView.setText("$" +
                                formatFloatDecimal((currentPrice * shares) - commissions));
                    } else {
                        // Sets the estimated Value
                        estimateValueView.setText("$" +
                                formatFloatDecimal(currentPrice * shares));
                    }
                }
                stockQuoteUrl = Constants.IEX_REQUEST_URL
                        + (s.toString().toLowerCase()) + Constants.IEX_INDIVIDUAL_PATH_END;
                fetchAgain();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Empty
            }
        });
        symbolEntry.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    symbolEntry.setSelection(symbolEntry.length());  // Sets the cursor all the way to the end
                }
            }
        });

        // Checks if the entry text changed
        sharesEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Empty
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    // Sets the estimated cost to $0.00
                    estimateValueView.setText("$0.00");
                } else {
                    shares = Integer.parseInt(sharesEntry.getText().toString());
                    if (buySelected && !sellSelected) {
                        // Sets the estimated Value
                        estimateValueView.setText("$" +
                                formatFloatDecimal((currentPrice * shares) + commissions));
                    } else if (!buySelected && sellSelected){
                        // Sets the estimated Value
                        estimateValueView.setText("$" +
                                formatFloatDecimal((currentPrice * shares) - commissions));
                    } else {
                        // Sets the estimated Value
                        estimateValueView.setText("$" +
                                formatFloatDecimal(currentPrice * shares));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Empty
            }
        });
        sharesEntry.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    sharesEntry.setSelection(sharesEntry.length());  // Sets the cursor all the way to the end
                }
            }
        });
    }

    /**
     * Hides the keyboard
     */
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Checks to see if everything is clicked and not empty
     * Otherwise a Toast error message pops up
     */
    private boolean shouldProceed(){
        // Checks if a Symbol has been entered or not
        if (symbolEntry.getText().toString().equals("") ||
                (!symbolEntry.getText().toString().equals("") && currentPrice == 0)){
            Toast.makeText(getApplicationContext(), "Please Enter a Valid Stock Symbol",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        // Checks if the shares entry box is empty or zero
        if (sharesEntry.getText().toString().equals("") || sharesEntry.getText().toString().equals("0")){
            Toast.makeText(getApplicationContext(), "Please Enter A Valid Amount Of Shares",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        // Checks if the buy or sell is selected or not
        if (!buySelected && !sellSelected){
            Toast.makeText(getApplicationContext(), "Please Select Buy Or Sell",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        // Checks if the order type is selected or not
        if (!market.isChecked() && !limit.isChecked() &&
                !stopLoss.isChecked() && !stopLimit.isChecked()){
            Toast.makeText(getApplicationContext(), "Please Select An Order Type",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (limitIsVisible && limitEntry.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), "Please Enter A Limit Price",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (stopIsVisible && stopEntry.getText().toString().equals("")){
            Toast.makeText(getApplicationContext(), "Please Enter A Stop Price",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        // Checks if the time frame is selected or not
        if (timeFrameIsVisible && (!goodTillCancelled.isChecked() && !goodForTheDay.isChecked())){
            Toast.makeText(getApplicationContext(), "Please Select A Time Frame",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        // Checks if the user has enough cash to buy or sell
        if (buySelected) {
            if (((currentPrice * shares) + commissions) > cash) {
                Toast.makeText(getApplicationContext(), "Not Enough Cash",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (sellSelected) {
        /* Get a handle on the ContentResolver to delete and insert data */
            ContentResolver myInvestmentsContentResolver = getContentResolver();

                                /* Opens the time frame for the transactions from database */
            Cursor cursor = myInvestmentsContentResolver.query(
                    StockContract.MyInvestmentsEntry.CONTENT_URI, null,
                    null, null, null);

            try {
                if (cursor != null) {
                    boolean inInvestmentDb = false;
                    cursor.moveToFirst();
                    for (int i = 0; i < cursor.getCount(); i++) {
                        // Retrieves the components of the trade
                        String stockSymbol = cursor.getString(cursor
                                .getColumnIndexOrThrow(StockContract.MyInvestmentsEntry.COLUMN_SYMBOL));
                        String shares = cursor.getString(cursor
                                .getColumnIndexOrThrow(StockContract.MyInvestmentsEntry.COLUMN_SHARES));
                        int shareInt = Integer.parseInt(shares.split(" ")[0]);

                        if (symbolEntry.getText().toString().toLowerCase().equals(stockSymbol.toLowerCase())) {
                            inInvestmentDb = true;
                            if (Integer.parseInt(sharesEntry.getText().toString().toLowerCase()) > shareInt) {
                                Toast.makeText(getApplicationContext(), "You Do Not Own These Many Shares",
                                        Toast.LENGTH_SHORT).show();
                                return false;
                            }
                        }
                        cursor.moveToNext();  // Moves to next stock Symbol in the database
                    }
                    if (!inInvestmentDb){
                        Toast.makeText(getApplicationContext(), "Investment Not In Your Portfolio",
                                Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
            } finally {
                cursor.close();
            }
        }

        return true;
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showBeforeSubmitDialog(DialogInterface.OnClickListener discardButtonClickListener){
        /*
         * Create an AlertDialog.Builder and set the message, and click listeners
         * for the positive and negative buttons on the dialog.
         */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (buySelected && !sellSelected) {
            builder.setMessage("Are you sure you would like to" + " purchase "
                    + sharesEntry.getText().toString() + " shares of "
                    + symbolEntry.getText().toString() + "?");
        } else if (sellSelected && !buySelected){
            builder.setMessage("Are you sure you would like to" + " sell "
                    + sharesEntry.getText().toString() + " shares of "
                    + symbolEntry.getText().toString() + "?");
        }
        builder.setPositiveButton("Cancel", discardButtonClickListener);
        builder.setNegativeButton("Submit Order", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Submit" button, so dismiss the dialog
                // and continue with the transaction.
                if (dialog != null) {
                    if (buySelected && !sellSelected) {
                        tradeStatementView.setText("Buy " + symbolEntry.getText().toString());
                        // Subtracts the investment value from cash
                        cash -= ((currentPrice * shares) + commissions);
                    } else if (sellSelected && !buySelected){
                        tradeStatementView.setText("Sell " + symbolEntry.getText().toString());
                        // Adds the investment value to cash
                        cash += ((currentPrice * shares) - commissions);
                    }
                    shareAmountView.setText(sharesEntry.getText().toString());
                    postCommissionsView.setText(commissionsView.getText().toString());

                    if (!stopEntry.getText().toString().equals("")){
                        stopPriceLayout.setVisibility(View.VISIBLE);
                        stopPriceLayoutDivider.setVisibility(View.VISIBLE);
                        postStopPriceView.setText("$" + stopEntry.getText().toString());
                    }
                    if (!limitEntry.getText().toString().equals("")){
                        limitPriceLayout.setVisibility(View.VISIBLE);
                        limitPriceLayoutDivider.setVisibility(View.VISIBLE);
                        postLimitPriceView.setText("$" + limitEntry.getText().toString());
                    }

                    if (marketChecked){
                        // Determines if markets are open or not
                        if (!currentDay.equals(R.string.saturday) && !currentDay.equals(R.string.sunday) &&
                                (Constants.getTime() > marketOpenTime &&
                                        Constants.getTime() < marketCloseTime)) {
                            orderStatusView.setText(R.string.filled_status);
                            orderStatusView.setTextColor(ContextCompat.
                                    getColor(TradeActivity.this, R.color.filled));
                            executionPriceView.setText("$" + currentPrice);

                            // Sets the new Balance values
                            balanceDatabase.setBalance(Constants.balanceId, cash, commissions);

                            // Add the trade to Filled database
                            ContentValues filledInfo = new ContentValues();
                            filledInfo.put(StockContract.FilledEntry.COLUMN_SYMBOL,
                                    symbolEntry.getText().toString());
                            filledInfo.put(StockContract.FilledEntry.COLUMN_ORDER_TYPE,
                                    orderTypeView.getText().toString());
                            filledInfo.put(StockContract.FilledEntry.COLUMN_SHARES,
                                    sharesEntry.getText().toString() + " Shares");
                            filledInfo.put(StockContract.FilledEntry.COLUMN_TRADE_PRICE,
                                    currentPriceView.getText().toString());
                            filledInfo.put(StockContract.FilledEntry.COLUMN_STOP_PRICE,
                                    stopEntry.getText().toString());
                            filledInfo.put(StockContract.FilledEntry.COLUMN_LIMIT_PRICE,
                                    limitEntry.getText().toString());

                            // Determines whether it is a buy or sell order
                            if (buySelected && !sellSelected) {
                                totalView.setText("$" + ((currentPrice * shares) + commissions));
                                purchase = "buy";
                                filledInfo.put(StockContract.FilledEntry.COLUMN_ORDERS, "Buy");
                            } else if (sellSelected && !buySelected) {
                                totalView.setText("$" + ((currentPrice * shares) - commissions));
                                purchase = "sell";
                                filledInfo.put(StockContract.FilledEntry.COLUMN_ORDERS, "Sell");
                            }

                            filledInfo.put(StockContract.FilledEntry.COLUMN_TIME_FRAME,
                                    goodTillView.getText().toString());
                            filledInfo.put(StockContract.FilledEntry.COLUMN_EXECUTION_PRICE, currentPrice);

                            if (buySelected) {
                                ContentValues positionInfo = new ContentValues();

                                positionInfo.put(StockContract.MyInvestmentsEntry.COLUMN_SYMBOL,
                                        symbolEntry.getText().toString());
                                positionInfo.put(StockContract.MyInvestmentsEntry.COLUMN_SHARES,
                                        sharesEntry.getText().toString() + " Shares");
                                positionInfo.put(StockContract.MyInvestmentsEntry.COLUMN_PRICE,
                                        currentPriceView.getText().toString());

                                getContentResolver().insert(StockContract.MyInvestmentsEntry.CONTENT_URI, positionInfo);
                            } else if (sellSelected){
                                /* Get a handle on the ContentResolver to delete and insert data */
                                ContentResolver myInvestmentsContentResolver = getContentResolver();

                                /* Opens the time frame for the transactions from database */
                                Cursor cursor = myInvestmentsContentResolver.query(
                                        StockContract.MyInvestmentsEntry.CONTENT_URI, null,
                                        null,null,null);

                                try {
                                    if (cursor != null) {
                                        cursor.moveToFirst();
                                        for (int i = 0; i < cursor.getCount(); i++) {
                                            // Retrieves the components of the trade
                                            String _id = cursor.getString(cursor
                                                    .getColumnIndexOrThrow(StockContract.MyInvestmentsEntry._ID));
                                            String stockSymbol = cursor.getString(cursor
                                                    .getColumnIndexOrThrow(StockContract.MyInvestmentsEntry.COLUMN_SYMBOL));
                                            String shares = cursor.getString(cursor
                                                    .getColumnIndexOrThrow(StockContract.MyInvestmentsEntry.COLUMN_SHARES));
                                            int shareInt = Integer.parseInt(shares.split(" ")[0]);

                                            if (stockSymbol.toLowerCase().equals(symbolEntry.getText().toString().toLowerCase())) {
                                                if (Integer.parseInt(sharesEntry.getText().toString()) == shareInt) {
                                                    // Build appropriate uri with String row id appended
                                                    Uri uri = StockContract.MyInvestmentsEntry.CONTENT_URI;
                                                    uri = uri.buildUpon().appendPath(_id).build();

                                                    // Deletes the corresponding investment
                                                    getContentResolver().delete(uri, _id, null);
                                                    break;
                                                } else if (Integer.parseInt(sharesEntry.getText().toString()) < shareInt){
                                                    shareInt -= Integer.parseInt(sharesEntry.getText().toString());
                                                    // Update the investment in the database
                                                    ContentValues positionInfo = new ContentValues();
                                                    positionInfo.put(StockContract.MyInvestmentsEntry.COLUMN_SHARES,
                                                            shareInt + " Shares");

                                                    // Build appropriate uri with String row id appended
                                                    Uri uri = StockContract.MyInvestmentsEntry.CONTENT_URI;
                                                    uri = uri.buildUpon().appendPath(_id).build();

                                                    getContentResolver().update(uri, positionInfo, _id, null);
                                                }
                                            }

                                            cursor.moveToNext();  // Moves to next stock Symbol in the database
                                        }
                                    }
                                } finally {
                                    cursor.close();
                                }
                            }

                            // Insert a new row for the stock into the provider using the ContentResolver.
                            getContentResolver().insert(StockContract.FilledEntry.CONTENT_URI, filledInfo);

                            // Creates a notification of Order Filled
                            NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(TradeActivity.this)
                                            .setSmallIcon(R.mipmap.ic_launcher_foreground_white)
                                            .setSound(alarmSound)
                                            .setContentTitle("Order Filled")
                                            .setContentText("Your " + orderType + " order to " + purchase + " " +
                                                    sharesEntry.getText().toString() + " shares of " +
                                                    symbolEntry.getText().toString().toUpperCase()
                                                    + " was filled at " + executionPriceView.getText().toString());

                            // Displays the notification
                            NotificationManager notificationManager = (NotificationManager)
                                    getSystemService(Context.NOTIFICATION_SERVICE);
                            // Creates an unique id for each notification
                            int notificationId = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
                            notificationManager.notify(notificationId, mBuilder.build());
                        } else {
                            // Sets the Text
                            orderStatusView.setText(R.string.pending_status);
                            orderStatusView.setTextColor(ContextCompat.
                                    getColor(TradeActivity.this, R.color.pending));
                            executionPriceView.setText("-N/A-");
                            totalView.setText("-N/A-");

                            // Add the trade to Pending database
                            ContentValues info = new ContentValues();
                            info.put(StockContract.PendingEntry.COLUMN_SYMBOL,
                                    symbolEntry.getText().toString());
                            info.put(StockContract.PendingEntry.COLUMN_ORDER_TYPE,
                                    orderTypeView.getText().toString());
                            info.put(StockContract.PendingEntry.COLUMN_SHARES,
                                    sharesEntry.getText().toString() + " Shares");
                            info.put(StockContract.PendingEntry.COLUMN_TRADE_PRICE,
                                    currentPriceView.getText().toString());
                            info.put(StockContract.PendingEntry.COLUMN_STOP_PRICE,
                                    stopEntry.getText().toString());
                            info.put(StockContract.PendingEntry.COLUMN_LIMIT_PRICE,
                                    limitEntry.getText().toString());

                            // Determines whether it is a buy or sell order
                            if (buySelected && !sellSelected) {
                                info.put(StockContract.PendingEntry.COLUMN_ORDERS, "Buy");
                                purchase = "buy";
                            } else if (sellSelected && !buySelected){
                                info.put(StockContract.PendingEntry.COLUMN_ORDERS, "Sell");
                                purchase = "sell";
                            }

                            info.put(StockContract.PendingEntry.COLUMN_TIME_FRAME,
                                    goodTillView.getText().toString());
                            info.put(StockContract.PendingEntry.COLUMN_EXECUTION_PRICE,
                                    executionPriceView.getText().toString());

                            // Insert a new row for the stock into the provider using the ContentResolver.
                            getContentResolver().insert(StockContract.PendingEntry.CONTENT_URI, info);

                            // Sends a notification if order is placed
                            orderPlaced();
                        }

                    } else {
                        // Sets the Text
                        orderStatusView.setText(R.string.pending_status);
                        orderStatusView.setTextColor(ContextCompat.
                                getColor(TradeActivity.this, R.color.pending));
                        executionPriceView.setText("-N/A-");
                        totalView.setText("-N/A-");

                        // Add the trade to Pending database
                        ContentValues info = new ContentValues();
                        info.put(StockContract.PendingEntry.COLUMN_SYMBOL,
                                symbolEntry.getText().toString());
                        info.put(StockContract.PendingEntry.COLUMN_ORDER_TYPE,
                                orderTypeView.getText().toString());
                        info.put(StockContract.PendingEntry.COLUMN_SHARES,
                                sharesEntry.getText().toString() + " Shares");
                        info.put(StockContract.PendingEntry.COLUMN_TRADE_PRICE,
                                currentPriceView.getText().toString());
                        info.put(StockContract.PendingEntry.COLUMN_STOP_PRICE,
                                stopEntry.getText().toString());
                        info.put(StockContract.PendingEntry.COLUMN_LIMIT_PRICE,
                                limitEntry.getText().toString());

                        // Determines whether it is a buy or sell order
                        if (buySelected && !sellSelected) {
                            info.put(StockContract.PendingEntry.COLUMN_ORDERS, "Buy");
                            purchase = "buy";
                        } else if (sellSelected && !buySelected){
                            info.put(StockContract.PendingEntry.COLUMN_ORDERS, "Sell");
                            purchase = "sell";
                        }

                        info.put(StockContract.PendingEntry.COLUMN_TIME_FRAME,
                                goodTillView.getText().toString());
                        info.put(StockContract.PendingEntry.COLUMN_EXECUTION_PRICE,
                                executionPriceView.getText().toString());

                        // Insert a new row for the stock into the provider using the ContentResolver.
                        getContentResolver().insert(StockContract.PendingEntry.CONTENT_URI, info);

                        // Sends a notification
                        orderPlaced();
                    }

                    // Switches to post trading
                    trading.setVisibility(View.GONE);
                    executed.setVisibility(View.VISIBLE);
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void orderPlaced(){
        // Creates a notification of Order Placed
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(TradeActivity.this)
                        .setSmallIcon(R.mipmap.ic_launcher_foreground_white)
                        .setSound(alarmSound)
                        .setContentTitle("Order Placed")
                        .setContentText("Your " + orderType + " order to " + purchase + " " +
                                sharesEntry.getText().toString() + " shares of " +
                                symbolEntry.getText().toString().toUpperCase()
                                + " was placed");

        // Displays the notification
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        // Creates an unique id for each notification
        int notificationId = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        notificationManager.notify(notificationId, mBuilder.build());
    }

}
