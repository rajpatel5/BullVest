package com.target.android.bullvest.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Ashwin on 01/06/2018.
 */

public class FilledDbHelper extends SQLiteOpenHelper{

    /* This is the name of our database. */
    private static final String DATABASE_NAME = "filled.db";

    /*
     * If you change the database schema, you must increment the database version or the onUpgrade
     * method will not be called.
     */
    private static final int DATABASE_VERSION = 1;

    public FilledDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time. This is where the creation of
     * tables and the initial population of the tables should happen.
     *
     * @param sqLiteDatabase The database.
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    /*
     * This String will contain a simple SQL statement that will create a table that will
     * cache the stock data.
     */
        final String SQL_CREATE_STOCK_TABLE = "CREATE TABLE " + StockContract.FilledEntry.TABLE_NAME + " (" +
                StockContract.FilledEntry._ID                       + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                StockContract.FilledEntry.COLUMN_SYMBOL             + " TEXT NOT NULL, " +
                StockContract.FilledEntry.COLUMN_ORDER_TYPE         + " TEXT NOT NULL, " +
                StockContract.FilledEntry.COLUMN_SHARES             + " TEXT NOT NULL, " +
                StockContract.FilledEntry.COLUMN_TRADE_PRICE        + " TEXT NOT NULL, " +
                StockContract.FilledEntry.COLUMN_STOP_PRICE         + " TEXT NOT NULL, " +
                StockContract.FilledEntry.COLUMN_LIMIT_PRICE        + " TEXT NOT NULL, " +
                StockContract.FilledEntry.COLUMN_ORDERS             + " TEXT NOT NULL, " +
                StockContract.FilledEntry.COLUMN_TIME_FRAME         + " TEXT NOT NULL, " +
                StockContract.FilledEntry.COLUMN_EXECUTION_PRICE    + " TEXT NOT NULL);";

    /* Execute that SQL with the execSQL method of our SQLite database object. */
        sqLiteDatabase.execSQL(SQL_CREATE_STOCK_TABLE);
    }

    /**
     * This database is only a cache for online data, so its upgrade policy is simply to discard
     * the data and call through to onCreate to recreate the table. Note that this only fires if
     * you change the version number for your database (in our case, DATABASE_VERSION). It does NOT
     * depend on the version number for your application found in your app/build.gradle file. If
     * you want to update the schema without wiping data, commenting out the current body of this
     * method should be your top priority before modifying this method.
     *
     * @param sqLiteDatabase Database that is being upgraded
     * @param oldVersion     The old database version
     * @param newVersion     The new database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + StockContract.FilledEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
