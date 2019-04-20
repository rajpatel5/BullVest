package com.target.android.bullvest.portfolio;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.target.android.bullvest.R;
import com.target.android.bullvest.data.BalanceDbHelper;
import com.target.android.bullvest.data.PortfolioChartDbHelper;
import com.target.android.bullvest.data.StockContract;
import com.target.android.bullvest.trade.TradeActivity;
import com.target.android.bullvest.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.NotificationManager.IMPORTANCE_HIGH;

/**
 * Created by Ashwin on 06/06/2018.
 */

public class PortfolioChartService extends Service{

    /** Tag for the log messages */
    private static final String TAG = "PortfolioChartService";

    int marketClosed = 0; // Used to determine if markets are closed
    public static boolean serviceOn = false;

    /* Timer used to refresh data */
    Timer timer;
    TimerTask timerTask;

    // We are going to use a handler to be able to run in our TimerTask
    final Handler handler = new Handler();

    public PortfolioChartService() {
        // Empty
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        String CHANNEL_ONE_ID = "com.target.android.bullvest";
        String CHANNEL_ONE_NAME = "Channel One";
        NotificationChannel notificationChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.YELLOW);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
        }

        Intent notificationIntent = new Intent(this, PortfolioActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setChannelId(CHANNEL_ONE_ID)
                .setSmallIcon(R.mipmap.ic_launcher_foreground_white)
                .setContentTitle("BullVest")
                .setPriority(Notification.PRIORITY_MIN)
                .setContentIntent(pendingIntent).build();

        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startTimer();
        serviceOn = true;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Empty
    }

    /** Updates the chart after the markets have closed */
    public void updateChart(){
        double investmentValue, cash, portfolioBalance;

        // Gets the day
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EE");
        String currentDay = dayFormat.format(calendar.getTime());

        if (!currentDay.equals(R.string.saturday) && !currentDay.equals(R.string.sunday) &&
                Constants.getTime() >= TradeActivity.marketOpenTime &&
                Constants.getTime() <= TradeActivity.marketCloseTime){
            marketClosed = 0;
        } else if (!currentDay.equals(R.string.saturday) && !currentDay.equals(R.string.sunday) &&
                Constants.getTime() >= TradeActivity.marketCloseTime) {
            marketClosed += 1;
        }

        if (marketClosed == 1){
            Cursor cursor = getContentResolver().query(StockContract.MyInvestmentsEntry.CONTENT_URI,
                    null,null, null,null);

            BalanceDbHelper balanceDatabase =
                    new BalanceDbHelper(PortfolioChartService.this);

            // Adds each starting balance point into an ArrayList
            PortfolioChartDbHelper portfolioChartBalanceDatabase;
            portfolioChartBalanceDatabase = new PortfolioChartDbHelper(PortfolioChartService.this);

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
                    portfolioChartBalanceDatabase.addBalance(portfolioBalance);
                }
            } finally {
                cursor.close();
            }
        }
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
                        updateChart();
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
