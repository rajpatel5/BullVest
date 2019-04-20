package com.target.android.bullvest.research;

import com.target.android.bullvest.utils.Stock;

import java.text.DecimalFormat;

/**
 * Created by Ashwin on 06/05/2018.
 */

public class ResearchSetViewValues {
    static Stock mStock;
    static boolean positiveChange = false;

    // Takes in a stock object
    ResearchSetViewValues(Stock stock){
        mStock = stock;
    }

    // Getter methods
    public static String getStockSymbol(){
        // return the symbol
        return mStock.getSymbol();
    }

    public static String getStockCompanyName(){
        // return the company name
        return mStock.getCompanyName();
    }

    public static String getStockExchange(){
        // return the primary exchange
        String exchange = mStock.getExchange();
        if (exchange.equals("New York Stock Exchange")){
            return "NYSE";
        }
        String arr[] = exchange.split(" ", 2);
        return arr[0];
    }

    public static String getStockSector(){
        // return the sector
        String sector = mStock.getSector();
        String arr[] = sector.split(" ", 2);
        return arr[0];
    }

    public static String getStockPrice(){
        // retrieve the price
        return formatDecimal(mStock.getPrice());
    }

    public static String getStockPriceChange(){
        // retrieve the price change
        return formatDecimal(mStock.getPriceChange());
    }

    public static String getStockPercentChange(){
        // retrieve the percent change
        double percentChange = mStock.getPercentChange();
        if (percentChange >= 0){
            positiveChange = true;
        }
        else {
            positiveChange = false;
        }
        return formatDecimal(percentChange);
    }

    public static String getStockOpen(){
        // retrieve the opening price
        return formatDecimal(mStock.getOpen());
    }

    public static String getStockPreviousClose(){
        // retrieve the previous close
        return formatDecimal(mStock.getPreviousClose());
    }

    public static String getStockHigh(){
        // retrieve the high price
        String formattedHigh = mStock.getHigh();
        if (formattedHigh.equals("null")){
            return "-N/A-";
        }
        return formattedHigh;
    }

    public static String getStockLow(){
        // retrieve the low price
        String formattedLow = mStock.getLow();
        if (formattedLow.equals("null")){
            return "-N/A-";
        }
        return formattedLow;
    }

    public static String getStockMarketCap(){
        // retrieve the market cap
        double marketCap = mStock.getMarketCap();
        if (marketCap >= 1000000000){
            return formatDecimal(marketCap / 1000000000) + "B";
        }
        else if (marketCap >= 1000000){
            return formatDecimal(marketCap / 1000000) + "M";
        }
        else if (marketCap >= 1000){
            return formatDecimal(marketCap / 1000) + "K";
        }
        return marketCap + "";
    }

    public static String getStockPERatio(){
        // retrieve the P/E Ratio
        String formattedPERatio = mStock.getPERatio();
        if (formattedPERatio.equals("null")){
            return "-N/A-";
        }
        return formattedPERatio;
    }

    public static String getStockWeek52High(){
        // retrieve the 52 week high price
        return formatDecimal(mStock.getWeek52High());
    }

    public static String getStockWeek52Low(){
        // retrieve the 52 week low price
        return formatDecimal(mStock.getWeek52Low());
    }

    public boolean isPositiveChange(){
        return positiveChange;
    }

    /**
     * Return the formatted value string
     * showing 2 decimal places (i.e. "3.25") from a decimal value.
     */
    private static String formatDecimal(double item) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return decimalFormat.format(item);
    }
}
