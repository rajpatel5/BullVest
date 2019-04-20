/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.target.android.bullvest.utils;

/**
 * An {@link Stock} object contains information related to a single stock.
 */
public class Stock {

    /** All String values of the Stock */
    private String mSymbol, mCompanyName, mExchange, mSector, mHigh, mLow, mPERatio;

    /** Price, Change in price, Change in percent, Opening price,
     * Previous closing price, 52 week high,
     * and 52 week low of the Stock */
    private double mPrice, mPriceChange, mPercentChange, mOpen,
            mPreviousClose, mMarketCap, mWeek52High, mWeek52Low;

    /**
     * Constructs a new {@link Stock} object.
     *
     * @param symbol is the symbol of the Stock
     * @param companyName is the company's name
     * @param exchange is the primary exchange of the Stock
     * @param sector is the sector of industry
     * @param price is price of the stock
     * @param priceChange is change in price from previous close
     * @param percentChange is the percent change
     * @param open is opening price of the stock
     * @param previousClose is the price of the previous close
     * @param high is the highest price of the stock for that day
     * @param low is the lowest price of the stock for that day
     * @param marketCap is market cap of the stock
     * @param peRatio is the PE Ratio
     * @param week52High is the 52 week high of the stock
     * @param week52Low is the 52 wekk low of the stock
     */
    public Stock(String symbol, String companyName, String exchange, String sector,
                 double price, double priceChange, double percentChange,
                 double open, double previousClose, String high, String low,
                 double marketCap, String peRatio, double week52High, double week52Low) {
        mSymbol = symbol;
        mCompanyName = companyName;
        mExchange = exchange;
        mSector = sector;
        mPrice = price;
        mPriceChange = priceChange;
        mPercentChange = percentChange;
        mOpen = open;
        mPreviousClose = previousClose;
        mHigh = high;
        mLow = low;
        mMarketCap = marketCap;
        mPERatio = peRatio;
        mWeek52High = week52High;
        mWeek52Low = week52Low;
    }

    /**
     * Constructs a new {@link Stock} object.
     *
     * @param symbol is the symbol of the Stock
     * @param companyName is the company's name
     * @param price is price of the stock
     * @param priceChange is change in price from previous close
     * @param percentChange is the percent change
     */
    public Stock(String symbol, String companyName,
                 double price, double priceChange, double percentChange) {
        mSymbol = symbol;
        mCompanyName = companyName;
        mPrice = price;
        mPriceChange = priceChange;
        mPercentChange = percentChange;
    }

    /**
     * Constructs a new empty {@link Stock} object.
     */
    public Stock() {}

    /* Getter Methods */
    public String getSymbol() {
        return mSymbol;
    }

    public String getCompanyName() {
        return mCompanyName;
    }

    public String getExchange() {
        return mExchange;
    }

    public String getSector() {
        return mSector;
    }

    public double getPrice() {
        return mPrice;
    }

    public double getPriceChange() {
        return mPriceChange;
    }

    public double getPercentChange() {
        return mPercentChange;
    }

    public double getOpen() {
        return mOpen;
    }

    public double getPreviousClose() {
        return mPreviousClose;
    }

    public String getHigh() {
        return mHigh;
    }

    public String getLow() {
        return mLow;
    }

    public double getMarketCap() {
        return mMarketCap;
    }

    public String getPERatio() {
        return mPERatio;
    }

    public double getWeek52High() {
        return mWeek52High;
    }

    public double getWeek52Low() {
        return mWeek52Low;
    }
}
