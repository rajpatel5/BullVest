package com.target.android.bullvest.utils;

/**
 * Created by Ashwin on 10/05/2018.
 */

/**
 * An {@link ChartPoints} object contains information related to a single point on the chart.
 */
public class ChartPoints {

    private double mAvgPrice, mOpen, mHigh, mLow, mClose, mOhlc;

    /**
     * Constructs a new {@link ChartPoints} object.
     *
     * @param open is the opening price of the stock
     * @param high is the high price of the stock
     * @param low is the low price of the stock
     * @param close is the closing price of the stock
     * @param ohlc is average price of the open, high, low, and close of the stock
     */
    public ChartPoints(double open, double high, double low, double close, double ohlc) {
        mOpen = open;
        mHigh = high;
        mLow = low;
        mClose = close;
        mOhlc = ohlc;
    }

    /**
     * Constructs a new {@link ChartPoints} object.
     * This is used only for the DailyFragment.
     *
     * @param avgPrice is average price of the stock
     */
    public ChartPoints(double avgPrice) {
        mAvgPrice = avgPrice;
    }

    // Gets the Average Price
    public double getAvgPrice() {
        return mAvgPrice;
    }

    // Gets the Opening Price
    public double getOpen() {
        return mOpen;
    }

    // Gets the High Price
    public double getHigh() {
        return mHigh;
    }

    // Gets the Low Price
    public double getLow() {
        return mLow;
    }

    // Gets the Closing Price
    public double getClose() {
        return mClose;
    }

    // Gets the OHLC Price
    public double getOhlc() {
        return mOhlc;
    }
}
