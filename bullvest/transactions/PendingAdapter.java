package com.target.android.bullvest.transactions;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.target.android.bullvest.R;

/**
 * Created by Ashwin on 19/05/2018.
 */

/**
 * {@link PendingAdapter} exposes a list of stocks
 * from a {@link android.database.Cursor} to a {@link android.support.v7.widget.RecyclerView}.
 */
public class PendingAdapter extends RecyclerView.Adapter<PendingAdapter.PendingAdapterViewHolder> {

    // Used for logging
    private final String TAG = TransactionsActivity.class.getSimpleName();

    /* The context we use to utility methods, app resources and layout inflaters */
    private final Context mContext;

    /** Interface to handle clicks on items within this Adapter.*/
    final private PendingAdapterOnClickHandler mClickHandler;

    /** The interface that receives onClick messages. */
    public interface PendingAdapterOnClickHandler {
        void onListItemClick(int clickedItemIndex);
    }

    public static Cursor mCursor;

    /**
     * Creates a PendingAdapter.
     *
     * @param context      Used to talk to the UI and app resources
     */
    public PendingAdapter(@NonNull Context context, PendingAdapterOnClickHandler clickHandler) {
        mContext = context;
        mClickHandler = clickHandler;
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new PendingAdapterViewHolder that holds the View for each list item
     */
    @Override
    public PendingAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.transaction_list_item,
                viewGroup, false);
        view.setFocusable(true);
        return new PendingAdapterViewHolder(view);
    }

    /**
     * Display the data at the specified position. In this method, we update the contents
     * of the ViewHolder to display the stock data for this particular position.
     *
     * @param PendingAdapterViewHolder The ViewHolder which should be updated to represent the
     *                                  contents of the item at the given position in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(PendingAdapterViewHolder PendingAdapterViewHolder, int position) {
        if (!mCursor.isClosed()) {
            try {
                mCursor.moveToPosition(position);

                /* Read id from the cursor */
                int _id = mCursor.getInt(TransactionsActivity.INDEX_STOCK_ID);
                /* Set the id */
                PendingAdapterViewHolder.idView.setText(_id + "");

                /* Read order from the cursor */
                String order = mCursor.getString(TransactionsActivity.INDEX_STOCK_ORDERS);
                /* Set the text */
                PendingAdapterViewHolder.orderView.setText(order);

                /* Read order type from the cursor */
                String orderType = mCursor.getString(TransactionsActivity.INDEX_STOCK_ORDER_TYPE);
                /* Display order type */
                PendingAdapterViewHolder.orderTypeView.setText(orderType);

                /* Read symbol from the cursor */
                String symbol = mCursor.getString(TransactionsActivity.INDEX_STOCK_SYMBOL);
                /* Display stock symbol */
                PendingAdapterViewHolder.symbolView.setText(order.toUpperCase() + " " + symbol.toUpperCase());

                /* Read share amount from the cursor */
                String shareAmount = mCursor.getString(TransactionsActivity.INDEX_STOCK_SHARES);
                /* Set the text */
                PendingAdapterViewHolder.sharesView.setText(shareAmount);

                /* Read trading price from the cursor */
                String tradePrice = mCursor.getString(TransactionsActivity.INDEX_STOCK_TRADE_PRICE);

                /* Read stop price from the cursor */
                String stopPrice = mCursor.getString(TransactionsActivity.INDEX_STOCK_STOP_PRICE);
                /* Set the text */
                PendingAdapterViewHolder.stopPriceView.setText(stopPrice);

                /* Read limit price from the cursor */
                String limitPrice = mCursor.getString(TransactionsActivity.INDEX_STOCK_LIMIT_PRICE);
                /* Set the text */
                PendingAdapterViewHolder.limitPriceView.setText(limitPrice);

                if (orderType.equals("Limit")) {
                    /* Set the text */
                    PendingAdapterViewHolder.tradePriceChangeView.setText("$" + limitPrice);
                } else if (orderType.equals("Stop") || orderType.equals("Stop Limit")) {
                    /* Set the text */
                    PendingAdapterViewHolder.tradePriceChangeView.setText("$" + stopPrice);
                } else {
                    /* Set the text */
                    PendingAdapterViewHolder.tradePriceChangeView.setText(tradePrice);
                }

                /* Read time Frame from the cursor */
                String timeFrame = mCursor.getString(TransactionsActivity.INDEX_STOCK_TIME_FRAME);
                /* Set the text */
                PendingAdapterViewHolder.timeFrameView.setText(timeFrame);

                /* Read execution price from the cursor */
                String executionPrice = mCursor.getString(TransactionsActivity.INDEX_STOCK_EXECUTION_PRICE);
                /* Set the text */
                PendingAdapterViewHolder.executionPriceView.setText(executionPrice);

            } catch (IllegalStateException e) {
                Log.e(TAG, "Tried to open an already-closed object");
            }
        }
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our database
     */
    @Override
    public int getItemCount() {
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    /**
     * Swaps the cursor used by the WatchlistAdapter for its stock data.
     * notifyDataSetChanged is called to tell the RecyclerView to update.
     *
     * @param newCursor the new cursor to use as PendingAdapter's data source
     */
    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    /**
     * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
     * a cache of the child views for a transaction item. It's also a convenient place to set an
     * OnClickListener, since it has access to the adapter and the views.
     */
    public class PendingAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView idView;
        final TextView symbolView;
        final TextView orderTypeView;
        final TextView stopPriceView;
        final TextView limitPriceView;
        final TextView timeFrameView;
        final TextView sharesView;
        final TextView orderView;
        final TextView tradePriceChangeView;
        final TextView executionPriceView;

        PendingAdapterViewHolder(View view) {
            super(view);
            idView = (TextView) view.findViewById(R.id.order_id);
            orderView = (TextView) view.findViewById(R.id.store_order);
            symbolView = (TextView) view.findViewById(R.id.transaction_symbol);
            orderTypeView = (TextView) view.findViewById(R.id.transaction_order_type);
            sharesView = (TextView) view.findViewById(R.id.transaction_share_amount);
            tradePriceChangeView = (TextView) view.findViewById(R.id.set_price);
            stopPriceView = (TextView) view.findViewById(R.id.store_stop_price);
            limitPriceView = (TextView) view.findViewById(R.id.store_limit_price);
            timeFrameView = (TextView) view.findViewById(R.id.store_time_frame);
            executionPriceView = (TextView) view.findViewById(R.id.store_execution_price);

            view.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click. We fetch the date that has been
         * selected, and then call the onClick handler registered with this adapter, passing that
         * date.
         *
         * @param view the View that was clicked
         */
        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            mClickHandler.onListItemClick(adapterPosition);
        }
    }
}
