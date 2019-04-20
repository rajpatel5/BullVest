package com.target.android.bullvest.utils;

/**
 * Created by Ashwin on 05/06/2018.
 */

public class Balance {

    int mId;
    double mCash, mCommissions;

    public Balance(){
        // Empty Constructor
    }

    public Balance(int id, double cash, double commissions){
        mId = id;
        mCash = cash;
        mCommissions = commissions;
    }

    /* Setter Methods */
    public void setId(int id) {
        mId = id;
    }

    public void setCash(double cash) {
        mCash = cash;
    }

    public void setCommissions(double commissions) {
        mCommissions = commissions;
    }

    /* Getter Methods */
    public int getId() {
        return mId;
    }

    public double getCash() {
        return mCash;
    }

    public double getCommissions() {
        return mCommissions;
    }

}
