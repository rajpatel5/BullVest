package com.target.android.bullvest.transactions;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.target.android.bullvest.R;
import com.target.android.bullvest.data.StockContract;
import com.target.android.bullvest.trade.TradeActivity;

import java.util.Date;

/**
 * Created by Ashwin on 01/06/2018.
 */

public class PendingFragment extends Fragment implements
        PendingAdapter.PendingAdapterOnClickHandler{

    // Used for logging
    private final String TAG = TransactionsActivity.class.getSimpleName();

    private LoaderManager.LoaderCallbacks<Cursor> pendingLoaderListener =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                @NonNull
                @Override
                public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
                    /* URI for all rows of stock data in our Pending table */
                    Uri pendingQueryUri = StockContract.PendingEntry.CONTENT_URI;

                    return new CursorLoader(getActivity(), pendingQueryUri,
                            MAIN_STOCK_PROJECTION, null, null, null);
                }

                @Override
                public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
                    mPendingAdapter.swapCursor(data);
                    if (data.getCount() != 0) {
                        showTransactionDataView();
                    } else {
                        hideTransactionDataView();
                    }
                }

                @Override
                public void onLoaderReset(@NonNull Loader<Cursor> loader) {}
            };

    /**
     * The columns of data that we are displaying within our
     * TransactionsActivity's list of stock data.
     */
    public static final String[] MAIN_STOCK_PROJECTION = {
            StockContract.PendingEntry._ID,
            StockContract.PendingEntry.COLUMN_SYMBOL,
            StockContract.PendingEntry.COLUMN_ORDER_TYPE,
            StockContract.PendingEntry.COLUMN_SHARES,
            StockContract.PendingEntry.COLUMN_TRADE_PRICE,
            StockContract.PendingEntry.COLUMN_STOP_PRICE,
            StockContract.PendingEntry.COLUMN_LIMIT_PRICE,
            StockContract.PendingEntry.COLUMN_ORDERS,
            StockContract.PendingEntry.COLUMN_TIME_FRAME,
            StockContract.PendingEntry.COLUMN_EXECUTION_PRICE
    };

    /** These ID's will be used to identify the Loader responsible for loading our stock data.*/
    private static final int PENDING_LOADER_ID = 15;
    private PendingAdapter mPendingAdapter;
    private RecyclerView mRecyclerView;
    private TextView empty; // temporary, replace with a nice picture or something

    Uri alarmSound;

    // Empty public constructor
    public PendingFragment() {}

    // Layout of the screen
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.transaction_recyclerview, container, false);

        empty = (TextView) rootView.findViewById(R.id.empty_view);
        alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        /**
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the RecyclerView and toggle the visibility.
         */
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.transaction_recyclerview_stocks);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        /* setLayoutManager associates the LayoutManager we created above with our RecyclerView */
        mRecyclerView.setLayoutManager(layoutManager);

        /**
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(false);

        /** New PendingAdapter */
        mPendingAdapter = new PendingAdapter(getContext(), this);
        mPendingAdapter.setHasStableIds(true);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mPendingAdapter);

        /**
         * Add a touch helper to the RecyclerView to recognize when a user swipes to delete an item.
         * An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
         * and uses callbacks to signal when a user is performing these actions.
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.ACTION_STATE_IDLE,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // Retrieves the id as a String
                int position = viewHolder.getAdapterPosition();
                String _id = ((TextView) mRecyclerView.findViewHolderForAdapterPosition(position)
                        .itemView.findViewById(R.id.order_id)).getText().toString().toUpperCase();
                String order = ((TextView) mRecyclerView.findViewHolderForAdapterPosition(position)
                        .itemView.findViewById(R.id.store_order)).getText().toString().toUpperCase();
                String orderType = ((TextView) mRecyclerView.findViewHolderForAdapterPosition(position)
                        .itemView.findViewById(R.id.transaction_order_type)).getText().toString().toUpperCase();
                String shares = ((TextView) mRecyclerView.findViewHolderForAdapterPosition(position)
                        .itemView.findViewById(R.id.transaction_share_amount)).getText().toString().toUpperCase();
                String stockSymbol = ((TextView) mRecyclerView.findViewHolderForAdapterPosition(position)
                        .itemView.findViewById(R.id.transaction_symbol)).getText().toString().toUpperCase();

                // Build appropriate uri with String row id appended
                Uri uri = StockContract.PendingEntry.CONTENT_URI;
                uri = uri.buildUpon().appendPath(_id).build();

                // Deletes the row via a ContentResolver
                getActivity().getContentResolver().delete(uri, _id, null);
                mPendingAdapter.notifyItemRemoved(position);

                // Creates a notification of Order Cancelled
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(getActivity())
                                .setSmallIcon(R.mipmap.ic_launcher_foreground_white)
                                .setSound(alarmSound)
                                .setContentTitle("Order Cancelled")
                                .setContentText("Your " + orderType.toLowerCase() + " order to " +
                                        order.toLowerCase() + " " + shares.toLowerCase() +
                                        " of " + stockSymbol.toUpperCase() + " was cancelled");

                // Displays the notification
                NotificationManager notificationManager = (NotificationManager)
                        getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                // Creates an unique id for each notification
                int notificationId = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
                notificationManager.notify(notificationId, mBuilder.build());

                // Restart the loader to re-query for all tasks after a deletion
                fetchAgain();
            }
        }).attachToRecyclerView(mRecyclerView);

        // Kick off the loader
        getLoaderManager().initLoader(PENDING_LOADER_ID, null, pendingLoaderListener);

        return rootView;
    }
    public void fetchAgain(){
        getLoaderManager().restartLoader(PENDING_LOADER_ID, null, pendingLoaderListener);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void showTransactionDataView() {
        empty.setVisibility(View.INVISIBLE);
        /* Finally, make sure the transaction data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void hideTransactionDataView() {
        empty.setText("NO PENDING TRANSACTIONS");
        empty.setVisibility(View.VISIBLE);
        /* Finally, make sure the transaction data is invisible */
        mRecyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        // Sends the info to the Post Trade activity
        Intent infoIntent = new Intent(getContext(), TradeActivity.class);
        String symbol = PendingAdapter.mCursor.getString(TransactionsActivity.INDEX_STOCK_SYMBOL);
        String orderType = PendingAdapter.mCursor.getString(TransactionsActivity.INDEX_STOCK_ORDER_TYPE);
        String shares = PendingAdapter.mCursor.getString(TransactionsActivity.INDEX_STOCK_SHARES);
        String[] sharesList = shares.split(" ");
        String shareAmount = sharesList[0];
        String tradePrice = PendingAdapter.mCursor.getString(TransactionsActivity.INDEX_STOCK_TRADE_PRICE);
        String stopPrice = PendingAdapter.mCursor.getString(TransactionsActivity.INDEX_STOCK_STOP_PRICE);
        String limitPrice = PendingAdapter.mCursor.getString(TransactionsActivity.INDEX_STOCK_LIMIT_PRICE);
        String orders = PendingAdapter.mCursor.getString(TransactionsActivity.INDEX_STOCK_ORDERS);
        String timeFrame = PendingAdapter.mCursor.getString(TransactionsActivity.INDEX_STOCK_TIME_FRAME);
        String executionPrice = PendingAdapter.mCursor.getString(TransactionsActivity.INDEX_STOCK_EXECUTION_PRICE);
        infoIntent.putExtra("postTrade", true);
        infoIntent.putExtra("stockSymbol", symbol.toLowerCase());
        infoIntent.putExtra("orderType", orderType);
        infoIntent.putExtra("shares", shareAmount);
        infoIntent.putExtra("tradePrice", tradePrice);
        infoIntent.putExtra("stopPrice", stopPrice);
        infoIntent.putExtra("limitPrice", limitPrice);
        infoIntent.putExtra("orders", orders);
        infoIntent.putExtra("timeFrame", timeFrame);
        infoIntent.putExtra("executionPrice", executionPrice);
        infoIntent.putExtra("orderStatus", "Pending");
        startActivity(infoIntent);
    }
}
