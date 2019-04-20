package com.target.android.bullvest.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.target.android.bullvest.utils.PortfolioChartBalance;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by Ashwin on 03/06/2018.
 */

public class PortfolioChartDbHelper extends SQLiteAssetHelper {

    /* This is the name of our database. */
    private static final String DATABASE_NAME = "portfolioChart.db";

    final String tableName = "portfolioChart";

    /*
     * If you change the database schema, you must increment the database version or the onUpgrade
     * method will not be called.
     */
    private static final int DATABASE_VERSION = 1;

    public PortfolioChartDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public PortfolioChartBalance getStartingBalance() {
        SQLiteDatabase portfolioChartDb = getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        String[] sqlSelect = {"id", "balance"};

        queryBuilder.setTables(tableName);
        Cursor cursor = queryBuilder.query(portfolioChartDb, sqlSelect, null,
                null, null, null, null);

        PortfolioChartBalance portfolioChartBalance = new PortfolioChartBalance();

        try {
            if (cursor != null) {
                cursor.moveToFirst();
                portfolioChartBalance.setId(cursor.getInt(cursor.getColumnIndex("id")));
                portfolioChartBalance.setBalance(cursor.getDouble(cursor.getColumnIndex("balance")));
            }
        } finally {
            cursor.close();
        }

        return portfolioChartBalance;
    }

    public double getBalance(int position) {
        SQLiteDatabase portfolioChartDb = getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        String[] sqlSelect = {"id", "balance"};

        queryBuilder.setTables(tableName);
        Cursor cursor = queryBuilder.query(portfolioChartDb, sqlSelect, null,
                null, null, null, null);

        PortfolioChartBalance portfolioChartBalance = new PortfolioChartBalance();

        try {
            if (cursor != null) {
                cursor.moveToPosition(position);
                portfolioChartBalance.setId(cursor.getInt(cursor.getColumnIndex("id")));
                portfolioChartBalance.setBalance(cursor.getDouble(cursor.getColumnIndex("balance")));
            }
        } catch (SQLiteCantOpenDatabaseException e){
            Log.e("PortfolioChartDbHelper", "Could not open the database" );
        }
        finally {
            cursor.close();
        }

        return portfolioChartBalance.getBalance();
    }

    public double getFinalBalance() {
        SQLiteDatabase portfolioChartDb = getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        String[] sqlSelect = {"balance"};

        queryBuilder.setTables(tableName);
        Cursor cursor = queryBuilder.query(portfolioChartDb, sqlSelect, null,
                null, null, null, null);

        PortfolioChartBalance portfolioChartBalance = new PortfolioChartBalance();

        try {
            if (cursor != null) {
                cursor.moveToLast();
                portfolioChartBalance.setBalance(cursor.getDouble(cursor.getColumnIndex("balance")));
            }
        } finally {
            cursor.close();
        }

        return portfolioChartBalance.getBalance();
    }

    // Sets the new values in the database
    public void addBalance(double balance) {
        SQLiteDatabase portfolioChartDb = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("balance", balance);

        portfolioChartDb.insert(tableName, null, values);
    }

    // Deletes all the values in the database
    public void deleteAllBalances() {
        SQLiteDatabase portfolioChartDb = getWritableDatabase();
        portfolioChartDb.delete(tableName, null, null);
    }

    public int getSize(){
        SQLiteDatabase portfolioChartDb = getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        String[] sqlSelect = {"id", "balance"};

        queryBuilder.setTables(tableName);
        Cursor cursor = queryBuilder.query(portfolioChartDb, sqlSelect, null,
                null, null, null, null);

        return cursor.getCount();
    }
}
