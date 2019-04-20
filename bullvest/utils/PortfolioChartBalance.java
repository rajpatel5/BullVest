package com.target.android.bullvest.utils;

/**
 * Created by Ashwin on 05/06/2018.
 */

public class PortfolioChartBalance {

    int mId;
    double mBalance;

    public PortfolioChartBalance(){
        // Empty Constructor
    }

    public PortfolioChartBalance(int id, double balance){
        mId = id;
        mBalance = balance;
    }

    public PortfolioChartBalance(double balance){
        mBalance = balance;
    }

    /* Setter Methods */
    public void setId(int id) {
        mId = id;
    }

    public void setBalance(double balance) {
        mBalance = balance;
    }

    /* Getter Methods */
    public int getId() {
        return mId;
    }

    public double getBalance() {
        return mBalance;
    }

}
