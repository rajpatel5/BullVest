package com.target.android.bullvest.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Ashwin on 22/04/2018.
 */

public class StockProvider extends ContentProvider {

    // Used for logging
    private final String TAG = StockProvider.class.getSimpleName();

    /** These constants will be used to match URIs with the data we are looking for. */
    public static final int WATCHLIST_CODE_STOCK = 100;
    public static final int WATCHLIST_CODE_STOCK_WITH_SYMBOL = 101;
    public static final int FILLED_CODE_STOCK = 200;
    public static final int FILLED_CODE_STOCK_WITH_SYMBOL = 201;
    public static final int PENDING_CODE_STOCK = 300;
    public static final int PENDING_CODE_STOCK_WITH_SYMBOL = 301;
    public static final int MY_INVESTMENTS_CODE_STOCK = 400;
    public static final int MY_INVESTMENTS_CODE_STOCK_WITH_SYMBOL = 401;

    /* The URI Matcher used by this Content Provider. */
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    /* Opens the Watchlist Helper class, used to query, delete, insert, or update. */
    public static WatchlistDbHelper mOpenWatchlistHelper;

    /* Opens the Filled Helper class, used to query, delete, insert, or update. */
    public static FilledDbHelper mOpenFilledHelper;

    /* Opens the Pending Helper class, used to query, delete, insert, or update. */
    public static PendingDbHelper mOpenPendingHelper;

    /* Opens the Pending Helper class, used to query, delete, insert, or update. */
    public static MyInvestmentsDbHelper mOpenMyInvestmentsHelper;

    /**
     * Creates the UriMatcher that will match each URI to the constants.
     *
     * @return A UriMatcher that correctly matches the constants
     */
    public static UriMatcher buildUriMatcher() {

        /* Root URI */
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = StockContract.CONTENT_AUTHORITY;

        /* This URI is content://com.example.android.bullvest/watchlist/ */
        matcher.addURI(authority, StockContract.PATH_WATCHLIST, WATCHLIST_CODE_STOCK);

        /* This URI is content://com.example.android.bullvest/watchlist/aapl */
        matcher.addURI(authority, StockContract.PATH_WATCHLIST + "/*", WATCHLIST_CODE_STOCK_WITH_SYMBOL);

        /* This URI is content://com.example.android.bullvest/filled/ */
        matcher.addURI(authority, StockContract.PATH_FILLED, FILLED_CODE_STOCK);

        /* This URI is content://com.example.android.bullvest/filled/5 */
        matcher.addURI(authority, StockContract.PATH_FILLED + "/#",FILLED_CODE_STOCK_WITH_SYMBOL);

        /* This URI is content://com.example.android.bullvest/pending/ */
        matcher.addURI(authority, StockContract.PATH_PENDING, PENDING_CODE_STOCK);

        /* This URI is content://com.example.android.bullvest/pending/5 */
        matcher.addURI(authority, StockContract.PATH_PENDING + "/#", PENDING_CODE_STOCK_WITH_SYMBOL);

        /* This URI is content://com.example.android.bullvest/myInvestments/ */
        matcher.addURI(authority, StockContract.PATH_MY_INVESTMENTS, MY_INVESTMENTS_CODE_STOCK);

        /* This URI is content://com.example.android.bullvest/myInvestments/5 */
        matcher.addURI(authority, StockContract.PATH_MY_INVESTMENTS + "/#", MY_INVESTMENTS_CODE_STOCK_WITH_SYMBOL);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        /* Initialization of the helper class instances. */
        mOpenWatchlistHelper = new WatchlistDbHelper(getContext());
        mOpenFilledHelper = new FilledDbHelper(getContext());
        mOpenPendingHelper = new PendingDbHelper(getContext());
        mOpenMyInvestmentsHelper = new MyInvestmentsDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        Cursor cursor;

        /*
         * Here's the switch statement that, given a URI, will determine what kind of request is
         * being made and query the database accordingly.
         */
        switch (sUriMatcher.match(uri)) {

            /* Return a cursor that contains one row of stock data fora particular symbol. */
            case WATCHLIST_CODE_STOCK_WITH_SYMBOL:
                String stockSymbolString = uri.getLastPathSegment();

                /*
                 * Creates a string array that only contains one element
                 * because this method signature accepts a string array.
                 */
                String[] selectionArguments = new String[]{stockSymbolString};

                cursor = mOpenWatchlistHelper.getReadableDatabase().query(
                        StockContract.WatchlistEntry.TABLE_NAME, projection,
                        StockContract.WatchlistEntry.COLUMN_SYMBOL + " = ? ",
                        selectionArguments, null, null, sortOrder);
                break;

            case FILLED_CODE_STOCK_WITH_SYMBOL:

                stockSymbolString = uri.getLastPathSegment();
                selectionArguments = new String[]{stockSymbolString};

                cursor = mOpenFilledHelper.getReadableDatabase().query(
                        StockContract.FilledEntry.TABLE_NAME, projection,
                        StockContract.FilledEntry._ID + " = ? ",
                        selectionArguments, null, null, sortOrder);
                break;
            case PENDING_CODE_STOCK_WITH_SYMBOL:

                stockSymbolString = uri.getLastPathSegment();
                selectionArguments = new String[]{stockSymbolString};

                cursor = mOpenPendingHelper.getReadableDatabase().query(
                        StockContract.PendingEntry.TABLE_NAME, projection,
                        StockContract.PendingEntry._ID + " = ? ",
                        selectionArguments, null, null, sortOrder);
                break;
            case MY_INVESTMENTS_CODE_STOCK_WITH_SYMBOL:

                stockSymbolString = uri.getLastPathSegment();
                selectionArguments = new String[]{stockSymbolString};

                cursor = mOpenMyInvestmentsHelper.getReadableDatabase().query(
                        StockContract.MyInvestmentsEntry.TABLE_NAME, projection,
                        StockContract.MyInvestmentsEntry._ID + " = ? ",
                        selectionArguments, null, null, sortOrder);
                break;

            /* Return a cursor that contains every row of stock data */
            case WATCHLIST_CODE_STOCK:
                cursor = mOpenWatchlistHelper.getReadableDatabase().query(
                        StockContract.WatchlistEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);

                break;
            case FILLED_CODE_STOCK:
                cursor = mOpenFilledHelper.getReadableDatabase().query(
                        StockContract.FilledEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);

                break;
            case PENDING_CODE_STOCK:
                cursor = mOpenPendingHelper.getReadableDatabase().query(
                        StockContract.PendingEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);

                break;
            case MY_INVESTMENTS_CODE_STOCK:
                cursor = mOpenMyInvestmentsHelper.getReadableDatabase().query(
                        StockContract.MyInvestmentsEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);

                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new RuntimeException("Not implementing getType in BullVest.");
    }

    /**
     * Handles requests to insert a set of new rows.
     *
     * @param uri    The content:// URI of the insertion request.
     * @param values An array of sets of column_name/value pairs to add to the database.
     *               This must not be {@code null}.
     *
     * @return The number of values that were inserted.
     */
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase watchlistDb = mOpenWatchlistHelper.getWritableDatabase();
        final SQLiteDatabase myInvestmentsDb = mOpenMyInvestmentsHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
            /* In this case, we want to insert to the entire watchlist table.*/
            case WATCHLIST_CODE_STOCK:
                watchlistDb.beginTransaction();
                int watchlistRowsInserted = 0;

                /* Tries to insert each value form the array of values. */
                try {
                    for (ContentValues value : values) {
                        // Inserts the stock
                        long _id = watchlistDb.insert(StockContract.WatchlistEntry.TABLE_NAME,
                                null, value);
                        if (_id != -1) {
                            watchlistRowsInserted++;
                        }
                    }
                    watchlistDb.setTransactionSuccessful();
                } finally {
                    watchlistDb.endTransaction();
                }

                // Notifies if the data table changed
                if (watchlistRowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return watchlistRowsInserted;

            case MY_INVESTMENTS_CODE_STOCK:
                myInvestmentsDb.beginTransaction();
                int myInvestmentsRowsInserted = 0;

                /* Tries to insert each value form the array of values. */
                try {
                    for (ContentValues value : values) {
                        // Inserts the stock
                        long _id = myInvestmentsDb.insert(StockContract.MyInvestmentsEntry.TABLE_NAME,
                                null, value);
                        if (_id != -1) {
                            myInvestmentsRowsInserted++;
                        }
                    }
                    myInvestmentsDb.setTransactionSuccessful();
                } finally {
                    myInvestmentsDb.endTransaction();
                }

                // Notifies if the data table changed
                if (myInvestmentsRowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return myInvestmentsRowsInserted;

            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues value) {
        SQLiteDatabase watchlistDb = mOpenWatchlistHelper.getWritableDatabase();
        SQLiteDatabase filledDb = mOpenFilledHelper.getWritableDatabase();
        SQLiteDatabase pendingDb = mOpenPendingHelper.getWritableDatabase();
        SQLiteDatabase myInvestmentsDb = mOpenMyInvestmentsHelper.getWritableDatabase();
        long _id = -1;  // Preset value of _id

        /*
         * Here's the switch statement that, given a URI, will determine what kind of request is
         * being made and query the database accordingly.
         */
        switch (sUriMatcher.match(uri)) {

            /* Insert the data depending on the uri */
            case WATCHLIST_CODE_STOCK:
                // Inserts a value (stock object)
                _id = watchlistDb.insert(StockContract.WatchlistEntry.TABLE_NAME,
                        null, value);
                break;

            case FILLED_CODE_STOCK:
                // Inserts a value (stock object)
                _id = filledDb.insert(StockContract.FilledEntry.TABLE_NAME,
                        null, value);
                break;
            case PENDING_CODE_STOCK:
                // Inserts a value (stock object)
                _id = pendingDb.insert(StockContract.PendingEntry.TABLE_NAME,
                        null, value);
                break;
            case MY_INVESTMENTS_CODE_STOCK:
                // Inserts a value (stock object)
                _id = myInvestmentsDb.insert(StockContract.MyInvestmentsEntry.TABLE_NAME,
                        null, value);
                break;
        }

        if (_id != -1) {
            // Notify all listeners that the data has changed for the watchlist content URI
            getContext().getContentResolver().notifyChange(uri, null);
        } else {
            return null;
        }

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, _id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase watchlistDb = mOpenWatchlistHelper.getWritableDatabase();
        SQLiteDatabase filledDb = mOpenFilledHelper.getWritableDatabase();
        SQLiteDatabase pendingDb = mOpenPendingHelper.getWritableDatabase();
        SQLiteDatabase myInvestmentsDb = mOpenMyInvestmentsHelper.getWritableDatabase();

        /* Users of the delete method will expect the number of rows deleted to be returned. */
        int numRowsDeleted = 0;

        /*
         * Passing "1" for the selection will delete all rows and return the number of rows
         * deleted, which is what the caller of this method expects.
         */
        if (null == selection) {
            selection = "1";
        }
        switch (sUriMatcher.match(uri)) {
            /* In this case, deletes the entire table. */
            case WATCHLIST_CODE_STOCK:
                numRowsDeleted = watchlistDb.delete(
                        StockContract.WatchlistEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case FILLED_CODE_STOCK:
                numRowsDeleted = filledDb.delete(
                        StockContract.FilledEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PENDING_CODE_STOCK:
                numRowsDeleted = pendingDb.delete(
                        StockContract.PendingEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MY_INVESTMENTS_CODE_STOCK:
                numRowsDeleted = myInvestmentsDb.delete(
                        StockContract.MyInvestmentsEntry.TABLE_NAME, selection, selectionArgs);
                break;

            // Stocks with symbols and id
            case WATCHLIST_CODE_STOCK_WITH_SYMBOL:
                /* Deletes a single row given by the symbol in the URI */
                numRowsDeleted = watchlistDb.delete(
                        StockContract.WatchlistEntry.TABLE_NAME,
                        StockContract.WatchlistEntry.COLUMN_SYMBOL + " = '" + selection + "';",
                        selectionArgs);
                break;
            case FILLED_CODE_STOCK_WITH_SYMBOL:
                /* Deletes a single row given by the symbol in the URI */
                numRowsDeleted = filledDb.delete(
                        StockContract.FilledEntry.TABLE_NAME,
                        StockContract.FilledEntry._ID + " = '" + selection + "';",
                        selectionArgs);
                break;
            case PENDING_CODE_STOCK_WITH_SYMBOL:
                /* Deletes a single row given by the symbol in the URI */
                numRowsDeleted = pendingDb.delete(
                        StockContract.PendingEntry.TABLE_NAME,
                        StockContract.PendingEntry._ID + " = '" + selection + "';",
                        selectionArgs);
                break;
            case MY_INVESTMENTS_CODE_STOCK_WITH_SYMBOL:
                /* Deletes a single row given by the symbol in the URI */
                numRowsDeleted = myInvestmentsDb.delete(
                        StockContract.MyInvestmentsEntry.TABLE_NAME,
                        StockContract.MyInvestmentsEntry._ID + " = '" + selection + "';",
                        selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        /* If we actually deleted any rows, notify that a change has occurred to this URI */
        if (numRowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase myInvestmentsDb = mOpenMyInvestmentsHelper.getWritableDatabase();

        /* Users of the delete method will expect the number of rows deleted to be returned. */
        int numRowsUpdated = 0;

        switch (sUriMatcher.match(uri)) {
            case MY_INVESTMENTS_CODE_STOCK_WITH_SYMBOL:
                /* Updates a single row given by the id in the URI */
                numRowsUpdated = myInvestmentsDb.update(
                        StockContract.MyInvestmentsEntry.TABLE_NAME, values,
                        StockContract.MyInvestmentsEntry._ID  +
                                " = '" + selection + "';", selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        /* If we actually deleted any rows, notify that a change has occurred to this URI */
        if (numRowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsUpdated;
    }
}
