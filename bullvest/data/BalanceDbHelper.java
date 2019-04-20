package com.target.android.bullvest.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.target.android.bullvest.utils.Balance;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by Ashwin on 03/06/2018.
 */

public class BalanceDbHelper extends SQLiteAssetHelper {

    /* This is the name of our database. */
    private static final String DATABASE_NAME = "balance.db";

    final String tableName = "balance";

    /*
     * If you change the database schema, you must increment the database version or the onUpgrade
     * method will not be called.
     */
    private static final int DATABASE_VERSION = 1;

    public BalanceDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public Balance getBalance() {
        SQLiteDatabase balanceDb = getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        String[] sqlSelect = {"id", "cash", "commissions"};

        queryBuilder.setTables(tableName);
        Cursor cursor = queryBuilder.query(balanceDb, sqlSelect, null,
                null, null, null, null);

        Balance balance = new Balance();

        try {
            if (cursor != null) {
                cursor.moveToFirst();
                balance.setId(cursor.getInt(cursor.getColumnIndex("id")));
                balance.setCash(cursor.getDouble(cursor.getColumnIndex("cash")));
                balance.setCommissions(cursor.getDouble(cursor.getColumnIndex("commissions")));
            }
        } finally {
            cursor.close();
        }

        return balance;
    }

    // Sets the new values in the database
    public void setBalance(int id, double cash, double commissions) {
        SQLiteDatabase balanceDb = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("cash", cash);
        values.put("commissions", commissions);

        balanceDb.update(tableName, values, id + "", null);
    }
}
