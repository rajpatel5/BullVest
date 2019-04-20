package com.target.android.bullvest.transactions;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.target.android.bullvest.utils.Constants;
import com.target.android.bullvest.R;
import com.target.android.bullvest.utils.Stock;
import com.target.android.bullvest.data.BalanceDbHelper;
import com.target.android.bullvest.data.StockContract;
import com.target.android.bullvest.trade.TradeActivity;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Ashwin on 06/06/2018.
 */

public class PendingService extends Service{

    /** Tag for the log messages */
    private static final String TAG = "PendingService";

    /* Timer used to refresh data */
    Timer timer;
    TimerTask timerTask;

    // We are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();

    public PendingService() {
        // Empty
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        // Empty
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        // Creates a notification of Order Filled
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(PendingService.this)
                        .setSmallIcon(R.mipmap.ic_launcher_foreground)
                        .setContentTitle("PENDING ORDERS ERROR")
                        .setSound(alarmSound)
                        .setContentText("Your pending orders may not execute, please" +
                                "open the app again");

        // Displays the notification
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        // Creates an unique id for each notification
        int notificationId = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        notificationManager.notify(notificationId, mBuilder.build());
    }

    /** Checks if the order can be filled or not and sends out a notification if it is filled */
    public void executeOrder(){

        /* Get a handle on the ContentResolver to delete and insert data */
        final ContentResolver pendingContentResolver = getContentResolver();

        final ArrayList<Thread> arrThreads = new ArrayList<Thread>();

        /* Opens the time frame for the transactions from database */
        Cursor cursor = pendingContentResolver.query(
                StockContract.PendingEntry.CONTENT_URI, null,
                null,null,null);

        // Gets the day
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EE");
        String currentDay = dayFormat.format(calendar.getTime());

        try {
            if (cursor != null) {
                if (!currentDay.equals(R.string.saturday) && !currentDay.equals(R.string.sunday) &&
                        Constants.getTime() >= TradeActivity.marketOpenTime &&
                        Constants.getTime() <= TradeActivity.marketCloseTime) {
                    cursor.moveToFirst(); // Moves to the first object in the cursor

                    // Runs for every object in the cursor
                    for (int i = 0; i < cursor.getCount(); i++) {
                        // Retrieves the components of the trade
                        final String _id = cursor.getString(cursor
                                .getColumnIndexOrThrow(StockContract.PendingEntry._ID));
                        final String orders = cursor.getString(cursor
                                .getColumnIndexOrThrow(StockContract.PendingEntry.COLUMN_ORDERS));
                        final String orderType = cursor.getString(cursor
                                .getColumnIndexOrThrow(StockContract.PendingEntry.COLUMN_ORDER_TYPE));
                        final String stockSymbol = cursor.getString(cursor
                                .getColumnIndexOrThrow(StockContract.PendingEntry.COLUMN_SYMBOL));
                        final int shares = cursor.getInt(cursor
                                .getColumnIndexOrThrow(StockContract.PendingEntry.COLUMN_SHARES));
                        final double stopPrice = cursor.getDouble(cursor
                                .getColumnIndexOrThrow(StockContract.PendingEntry.COLUMN_STOP_PRICE));
                        final double limitPrice = cursor.getDouble(cursor
                                .getColumnIndexOrThrow(StockContract.PendingEntry.COLUMN_LIMIT_PRICE));
                        final String timeFrame = cursor.getString(cursor
                                .getColumnIndexOrThrow(StockContract.PendingEntry.COLUMN_TIME_FRAME));

                        switch (orderType) {
                            case "Market": {
                                // Creates a url to retrieve the stock price
                                Uri baseUri = Uri.parse(Constants.IEX_REQUEST_URL
                                        + stockSymbol.toLowerCase() + Constants.IEX_INDIVIDUAL_PATH_END);
                                final Uri.Builder uriBuilder = baseUri.buildUpon();

                                // Retrieves the stock price and executes the transactions
                                Thread market = new Thread(new Runnable() {
                                    public void run() {
                                        synchronized (this) {
                                            double cash = 0;
                                            BalanceDbHelper balanceDatabase =
                                                    new BalanceDbHelper(PendingService.this);

                                            // Perform the network request, parse the response, and extract a quote.
                                            Stock stock = PendingQueryUtils.fetchStockData(uriBuilder.toString());
                                            if (balanceDatabase.getBalance().getCash() < (shares * stock.getPrice())) {
                                                if (orders.equals("Buy")) {
                                                    cash = balanceDatabase.getBalance().getCash() -
                                                            ((shares * stock.getPrice()) +
                                                                    balanceDatabase.getBalance().getCommissions());

                                                    // Add to the my investment database
                                                    addInvestment(stockSymbol, shares, stock.getPrice());
                                                } else if (orders.equals("Sell")) {
                                                    cash = balanceDatabase.getBalance().getCash() +
                                                            ((shares * stock.getPrice()) -
                                                                    balanceDatabase.getBalance().getCommissions());

                                                    // Deletes or updates the investment database
                                                    deleteOrUpdateInvestment(stockSymbol, shares);
                                                }
                                                balanceDatabase.setBalance(Constants.balanceId, cash,
                                                        balanceDatabase.getBalance().getCommissions());

                                                // Add the trade to Filled database
                                                addFilledTransaction(stockSymbol, orderType, shares,
                                                        stock.getPrice(), "", "", orders, timeFrame);

                                                // Build appropriate uri with String row id appended
                                                Uri uri = StockContract.PendingEntry.CONTENT_URI;
                                                uri = uri.buildUpon().appendPath(_id).build();

                                                // Creates a notification of Order Filled
                                                sendNotification("Order Filled", "Your market order to "
                                                        + orders.toLowerCase() + " " + shares + " shares of " +
                                                        stockSymbol.toUpperCase() + " was filled at $" + stock.getPrice());

                                                // Deletes the row via a ContentResolver
                                                pendingContentResolver.delete(uri, _id, null);
                                                notify();
                                            }
                                        }
                                    }
                                });
                                arrThreads.add(market);
                                break;
                            }
                            case "Limit": {
                                // Creates a url to retrieve the stock price
                                Uri baseUri = Uri.parse(Constants.IEX_REQUEST_URL
                                        + stockSymbol.toLowerCase() + Constants.IEX_INDIVIDUAL_PATH_END);
                                final Uri.Builder uriBuilder = baseUri.buildUpon();

                                // Retrieves the stock price and executes the transactions
                                Thread limit = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        synchronized (this){
                                            double cash = 0;
                                            boolean trade = false;
                                            BalanceDbHelper balanceDatabase =
                                                    new BalanceDbHelper(PendingService.this);

                                            // Perform the network request, parse the response, and extract a quote.
                                            Stock stock = PendingQueryUtils.fetchStockData(uriBuilder.toString());
                                            if (balanceDatabase.getBalance().getCash() < (shares * stock.getPrice())) {
                                                if (orders.equals("Buy") && (stock.getPrice() <= limitPrice)) {
                                                    trade = true;
                                                    cash = balanceDatabase.getBalance().getCash() -
                                                            ((shares * stock.getPrice()) +
                                                                    balanceDatabase.getBalance().getCommissions());

                                                    // Add to the my investment database
                                                    addInvestment(stockSymbol, shares, stock.getPrice());
                                                } else if (orders.equals("Sell") && (stock.getPrice() >= limitPrice)) {
                                                    trade = true;
                                                    cash = balanceDatabase.getBalance().getCash() +
                                                            ((shares * stock.getPrice()) -
                                                                    balanceDatabase.getBalance().getCommissions());
                                                    deleteOrUpdateInvestment(stockSymbol, shares);
                                                }
                                                if (trade) {
                                                    balanceDatabase.setBalance(Constants.balanceId, cash,
                                                            balanceDatabase.getBalance().getCommissions());
                                                    // Build appropriate uri with String row id appended
                                                    Uri uri = StockContract.PendingEntry.CONTENT_URI;
                                                    uri = uri.buildUpon().appendPath(_id).build();

                                                    // Add the trade to Filled database
                                                    addFilledTransaction(stockSymbol, orderType, shares,
                                                            stock.getPrice(), "", formatDoubleDecimal(limitPrice)
                                                            , orders, timeFrame);

                                                    // Creates a notification of Order Filled
                                                    sendNotification("Order Filled", "Your limit order to "
                                                            + orders.toLowerCase() + " " + shares + " shares of " +
                                                            stockSymbol.toUpperCase() + " was filled at $" + stock.getPrice());

                                                    // Deletes the row via a ContentResolver
                                                    pendingContentResolver.delete(uri, _id, null);
                                                    notify();
                                                }
                                            }
                                        }
                                    }
                                });
                                arrThreads.add(limit);
                                break;
                            }
                            case "Stop": {
                                // Creates a url to retrieve the stock price
                                Uri baseUri = Uri.parse(Constants.IEX_REQUEST_URL
                                        + stockSymbol.toLowerCase() + Constants.IEX_INDIVIDUAL_PATH_END);
                                final Uri.Builder uriBuilder = baseUri.buildUpon();

                                // Retrieves the stock price and executes the transactions
                                Thread stop = new Thread(new Runnable() {
                                    public void run() {
                                        synchronized (this){
                                            double cash = 0;
                                            boolean trade = false;
                                            BalanceDbHelper balanceDatabase =
                                                    new BalanceDbHelper(PendingService.this);

                                            // Perform the network request, parse the response, and extract a quote.
                                            Stock stock = PendingQueryUtils.fetchStockData(uriBuilder.toString());
                                            if (balanceDatabase.getBalance().getCash() < (shares * stock.getPrice())) {
                                                if (orders.equals("Buy") && (stock.getPrice() >= stopPrice)) {
                                                    trade = true;
                                                    cash = balanceDatabase.getBalance().getCash() -
                                                            ((shares * stock.getPrice()) +
                                                                    balanceDatabase.getBalance().getCommissions());
                                                    // Add to the my investment database
                                                    addInvestment(stockSymbol, shares, stock.getPrice());
                                                } else if (orders.equals("Sell") && (stock.getPrice() <= stopPrice)) {
                                                    trade = true;
                                                    cash = balanceDatabase.getBalance().getCash() +
                                                            ((shares * stock.getPrice()) -
                                                                    balanceDatabase.getBalance().getCommissions());
                                                    deleteOrUpdateInvestment(stockSymbol, shares);
                                                }
                                                if (trade) {
                                                    balanceDatabase.setBalance(Constants.balanceId, cash,
                                                            balanceDatabase.getBalance().getCommissions());

                                                    // Add the trade to Filled database
                                                    addFilledTransaction(stockSymbol, orderType, shares,
                                                            stock.getPrice(), formatDoubleDecimal(stopPrice),
                                                            "", orders, timeFrame);

                                                    // Build appropriate uri with String row id appended
                                                    Uri uri = StockContract.PendingEntry.CONTENT_URI;
                                                    uri = uri.buildUpon().appendPath(_id).build();

                                                    // Creates a notification of Order Filled
                                                    sendNotification("Order Filled", "Your stop order to "
                                                            + orders.toLowerCase() + " " + shares + " shares of " +
                                                            stockSymbol.toUpperCase() + " was filled at $" + stock.getPrice());

                                                    // Deletes the row via a ContentResolver
                                                    pendingContentResolver.delete(uri, _id, null);
                                                    notify();
                                                }
                                            }
                                        }
                                    }
                                });
                                arrThreads.add(stop);
                                break;
                            }
                            case "Stop Limit": {
                                // Creates a url to retrieve the stock price
                                Uri baseUri = Uri.parse(Constants.IEX_REQUEST_URL
                                        + stockSymbol.toLowerCase() + Constants.IEX_INDIVIDUAL_PATH_END);
                                final Uri.Builder uriBuilder = baseUri.buildUpon();

                                // Retrieves the stock price and executes the transactions
                                Thread stopLimit = new Thread(new Runnable() {
                                    public void run() {
                                        synchronized (this) {
                                            // Perform the network request, parse the response, and extract a quote.
                                            Stock stock = PendingQueryUtils.fetchStockData(uriBuilder.toString());
                                            if ((orders.equals("Buy") && (stock.getPrice() >= stopPrice)) ||
                                                    (orders.equals("Sell") && (stock.getPrice() <= stopPrice))) {

                                                // Add the trade to Pending database as a Limit Order
                                                ContentValues info = new ContentValues();
                                                info.put(StockContract.PendingEntry.COLUMN_SYMBOL, stockSymbol);
                                                info.put(StockContract.PendingEntry.COLUMN_ORDER_TYPE, "Limit");
                                                info.put(StockContract.PendingEntry.COLUMN_SHARES, shares + " Shares");
                                                info.put(StockContract.PendingEntry.COLUMN_TRADE_PRICE, "$" + stock.getPrice());
                                                info.put(StockContract.PendingEntry.COLUMN_STOP_PRICE, "");
                                                info.put(StockContract.PendingEntry.COLUMN_LIMIT_PRICE, limitPrice);
                                                info.put(StockContract.PendingEntry.COLUMN_ORDERS, orders);
                                                info.put(StockContract.PendingEntry.COLUMN_TIME_FRAME, timeFrame);
                                                info.put(StockContract.PendingEntry.COLUMN_EXECUTION_PRICE, stock.getPrice());

                                                // Insert a new row for the stock into the provider using the ContentResolver.
                                                getContentResolver().insert(StockContract.PendingEntry.CONTENT_URI, info);

                                                // Build appropriate uri with String row id appended
                                                Uri uri = StockContract.PendingEntry.CONTENT_URI;
                                                uri = uri.buildUpon().appendPath(_id).build();

                                                // Creates a notification of Order Switch
                                                sendNotification("Order Switch", "Your stop limit order to "
                                                        + orders.toLowerCase() + " " + shares + " shares of " +
                                                        stockSymbol.toUpperCase() + " was switched to a limit order");

                                                // Deletes the row via a ContentResolver
                                                pendingContentResolver.delete(uri, _id, null);
                                            }
                                        }
                                    }
                                });
                                arrThreads.add(stopLimit);
                                break;
                            }
                        }
                        cursor.moveToNext();  // Moves to next stock Symbol in the database
                    }

                    // Runs the threads one by one to avoid double transactions
                    if (arrThreads.size() != 0) {
                        for (int i = 0; i < arrThreads.size(); i++) {
                            try {
                                arrThreads.get(i).start();
                                arrThreads.get(i).join();
                            } catch (InterruptedException e) {
                                Log.e(TAG, "Thread Interrupted");
                            }
                        }
                    }
                } else {
                    cursor.moveToFirst();
                    // Deletes all the orders that were 'Good For the Day' if the markets are closed
                    for (int i = 0; i < cursor.getCount(); i++) {
                        // Retrieves the components of the trade
                        final String timeFrame = cursor.getString(cursor
                                .getColumnIndexOrThrow(StockContract.PendingEntry.COLUMN_TIME_FRAME));

                        if (timeFrame.equals("Good For the Day")) {
                            final String _id = cursor.getString(cursor
                                    .getColumnIndexOrThrow(StockContract.PendingEntry._ID));
                            final String orders = cursor.getString(cursor
                                    .getColumnIndexOrThrow(StockContract.PendingEntry.COLUMN_ORDERS));
                            final String stockSymbol = cursor.getString(cursor
                                    .getColumnIndexOrThrow(StockContract.PendingEntry.COLUMN_SYMBOL));
                            final int shares = cursor.getInt(cursor
                                    .getColumnIndexOrThrow(StockContract.PendingEntry.COLUMN_SHARES));

                            // Build appropriate uri with String row id appended
                            Uri uri = StockContract.PendingEntry.CONTENT_URI;
                            uri = uri.buildUpon().appendPath(_id).build();

                            // Deletes the row via a ContentResolver
                            pendingContentResolver.delete(uri, _id, null);

                            // Creates a notification of Order Cancelled
                            sendNotification("Order Cancelled", "Your order to "
                                    + orders.toLowerCase() + " " + shares + " shares of " +
                                    stockSymbol.toUpperCase() + " was cancelled");
                        }
                        cursor.moveToNext();
                    }
                }

            }
        }
        finally {
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

    /* Starts the Timer */
    public void startTimer() {
        // Set a new Timer
        timer = new Timer();

        // Initialize the TimerTask's job
        timerTask = new TimerTask() {
            public void run() {

                // Use a handler to run the executeOrder method
                handler.post(new Runnable() {
                    public void run() {
                        executeOrder();
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

    public void addInvestment(String stockSymbol, int shares, double stockPrice){
        // Add to the investment database
        ContentValues positionInfo = new ContentValues();
        positionInfo.put(StockContract.MyInvestmentsEntry.COLUMN_SYMBOL, stockSymbol);
        positionInfo.put(StockContract.MyInvestmentsEntry.COLUMN_SHARES, shares + " Shares");
        positionInfo.put(StockContract.MyInvestmentsEntry.COLUMN_PRICE, "$" + stockPrice);
        getContentResolver().insert(StockContract.MyInvestmentsEntry.CONTENT_URI, positionInfo);
    }

    public void deleteOrUpdateInvestment(String pendingSymbol, int pendingShares){
        /* Get a handle on the ContentResolver to delete and insert data */
        final ContentResolver myInvestmentsContentResolver = getContentResolver();

                                /* Opens the time frame for the transactions from database */
        Cursor cursor = myInvestmentsContentResolver.query(
                StockContract.MyInvestmentsEntry.CONTENT_URI, null,
                null,null,null);

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
                String shareInt = shares.split(" ")[0];

                if (pendingSymbol.toLowerCase().equals(stockSymbol.toLowerCase())){
                    if (pendingShares == Integer.parseInt(shareInt)){
                        // Build appropriate uri with String row id appended
                        Uri uri = StockContract.MyInvestmentsEntry.CONTENT_URI;
                        uri = uri.buildUpon().appendPath(_id).build();

                        myInvestmentsContentResolver.delete(uri, _id, null);
                        break;

                    } else if (pendingShares < Integer.parseInt(shareInt)){
                        int shareAmount = Integer.parseInt(shareInt) - pendingShares;
                        updateInvestment(shareAmount, _id);
                        break;
                    }
                }
                cursor.moveToNext();  // Moves to next stock Symbol in the database
            }
        }
    }

    public void updateInvestment(int shares, String _id){
        // Update the investment in the database
        ContentValues positionInfo = new ContentValues();
        positionInfo.put(StockContract.MyInvestmentsEntry.COLUMN_SHARES, shares + " Shares");

        // Build appropriate uri with String row id appended
        Uri uri = StockContract.MyInvestmentsEntry.CONTENT_URI;
        uri = uri.buildUpon().appendPath(_id).build();

        getContentResolver().update(uri, positionInfo, _id, null);
    }

    public void addFilledTransaction(String stockSymbol, String orderType, int shares, double stockPrice,
                                      String stopPrice, String limitPrice, String orders, String timeFrame){
        ContentValues info = new ContentValues();
        info.put(StockContract.FilledEntry.COLUMN_SYMBOL, stockSymbol);
        info.put(StockContract.FilledEntry.COLUMN_ORDER_TYPE, orderType);
        info.put(StockContract.FilledEntry.COLUMN_SHARES, shares + " Shares");
        info.put(StockContract.FilledEntry.COLUMN_TRADE_PRICE, "$" + stockPrice);
        info.put(StockContract.FilledEntry.COLUMN_STOP_PRICE, stopPrice);
        info.put(StockContract.FilledEntry.COLUMN_LIMIT_PRICE, limitPrice);
        info.put(StockContract.FilledEntry.COLUMN_ORDERS, orders);
        info.put(StockContract.FilledEntry.COLUMN_TIME_FRAME, timeFrame);
        info.put(StockContract.FilledEntry.COLUMN_EXECUTION_PRICE, stockPrice);

        // Insert a new row for the stock into the provider using the ContentResolver.
        getContentResolver().insert(StockContract.FilledEntry.CONTENT_URI, info);
    }

    public void sendNotification (String title, String message){
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        // Creates a notification of Order transaction
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(PendingService.this)
                        .setSmallIcon(R.mipmap.ic_launcher_foreground)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setSound(alarmSound);

        // Displays the notification
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        // Creates an unique id for each notification
        int notificationId = (int) (new Date().getTime() % Integer.MAX_VALUE);
        notificationManager.notify(notificationId, mBuilder.build());
    }
}
