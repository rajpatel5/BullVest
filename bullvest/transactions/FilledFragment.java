package com.target.android.bullvest.transactions;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
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

/**
 * Created by Ashwin on 01/06/2018.
 */

public class FilledFragment extends Fragment implements FilledAdapter.FilledAdapterOnClickHandler{

    private LoaderManager.LoaderCallbacks<Cursor> filledLoaderListener =
            new LoaderManager.LoaderCallbacks<Cursor>() {
                @NonNull
                @Override
                public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
                    /* URI for all rows of stock data in our filled table */
                    Uri filledQueryUri = StockContract.FilledEntry.CONTENT_URI;

                    return new CursorLoader(getActivity(), filledQueryUri,
                            MAIN_STOCK_PROJECTION, null, null, null);
                }

                @Override
                public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
                    mFilledAdapter.swapCursor(data);
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
     * The columns of data that we are displaying within our TransactionsActivity's list of
     * stock data.
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
    private static final int FILLED_LOADER_ID = 14;

    private FilledAdapter mFilledAdapter;
    private RecyclerView mRecyclerView;
    private TextView empty; // temporary, replace with a nice picture or something

    // Empty public constructor
    public FilledFragment() {}

    // Layout of the screen
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.transaction_recyclerview, container, false);

        empty = (TextView) rootView.findViewById(R.id.empty_view);
        /*
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the RecyclerView and toggle the visibility.
         */
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.transaction_recyclerview_stocks);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        /* setLayoutManager associates the LayoutManager we created above with our RecyclerView */
        mRecyclerView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(false);

        /* New FilledAdapter */
        mFilledAdapter = new FilledAdapter(getContext(), this);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mFilledAdapter);

        /*
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

                // Build appropriate uri with String row id appended
                Uri uri = StockContract.FilledEntry.CONTENT_URI;
                uri = uri.buildUpon().appendPath(_id).build();

                // Deletes the row via a ContentResolver
                getActivity().getContentResolver().delete(uri, _id, null);
                mFilledAdapter.notifyItemRemoved(position);

                // Restart the loader to re-query for all tasks after a deletion
                fetchAgain();
            }
        }).attachToRecyclerView(mRecyclerView);

        // Kick off the loader
        getLoaderManager().initLoader(FILLED_LOADER_ID, null, filledLoaderListener);

        return rootView;
    }

    public void fetchAgain(){
        getLoaderManager().restartLoader(FILLED_LOADER_ID, null, filledLoaderListener);
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
        empty.setText("NO FILLED TRANSACTIONS");
        empty.setVisibility(View.VISIBLE);
        /* Finally, make sure the transaction data is invisible */
        mRecyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        // Sends the info to the Post Trade activity
        Intent infoIntent = new Intent(getContext(), TradeActivity.class);
        String symbol = FilledAdapter.mCursor.getString(TransactionsActivity.INDEX_STOCK_SYMBOL);
        String orderType = FilledAdapter.mCursor.getString(TransactionsActivity.INDEX_STOCK_ORDER_TYPE);
        String shares = FilledAdapter.mCursor.getString(TransactionsActivity.INDEX_STOCK_SHARES);
        String[] sharesList = shares.split(" ");
        String shareAmount = sharesList[0];
        String tradePrice = FilledAdapter.mCursor.getString(TransactionsActivity.INDEX_STOCK_TRADE_PRICE);
        String stopPrice = FilledAdapter.mCursor.getString(TransactionsActivity.INDEX_STOCK_STOP_PRICE);
        String limitPrice = FilledAdapter.mCursor.getString(TransactionsActivity.INDEX_STOCK_LIMIT_PRICE);
        String orders = FilledAdapter.mCursor.getString(TransactionsActivity.INDEX_STOCK_ORDERS);
        String timeFrame = FilledAdapter.mCursor.getString(TransactionsActivity.INDEX_STOCK_TIME_FRAME);
        String executionPrice = FilledAdapter.mCursor.getString(TransactionsActivity.INDEX_STOCK_EXECUTION_PRICE);
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
        infoIntent.putExtra("orderStatus", "Filled");
        startActivity(infoIntent);
    }
}
