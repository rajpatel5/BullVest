package com.target.android.bullvest.portfolio;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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
 * {@link PortfolioAdapter} exposes a list of stocks
 * from a {@link android.database.Cursor} to a {@link android.support.v7.widget.RecyclerView}.
 */
public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.PortfolioAdapterViewHolder> {

    // Used for logging
    private final String TAG = PortfolioActivity.class.getSimpleName();

    /* The context we use to utility methods, app resources and layout inflaters */
    private final Context mContext;

    /** Interface to handle clicks on items within this Adapter.*/
    final private PortfolioAdapterOnClickHandler mClickHandler;

    /** The interface that receives onClick messages. */
    public interface PortfolioAdapterOnClickHandler {
        void onListItemClick(int clickedItemIndex);
    }

    public static Cursor mCursor;

    /**
     * Creates a WatchlistAdapter.
     *
     * @param context      Used to talk to the UI and app resources
     */
    public PortfolioAdapter(@NonNull Context context, PortfolioAdapterOnClickHandler clickHandler) {
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
     * @return A new WatchlistAdapterViewHolder that holds the View for each list item
     */
    @Override
    public PortfolioAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.portfolio_list_item, viewGroup, false);
        view.setFocusable(true);
        return new PortfolioAdapterViewHolder(view);
    }

    /**
     * Display the data at the specified position. In this method, we update the contents
     * of the ViewHolder to display the stock data for this particular position.
     *
     * @param portfolioAdapterViewHolder The ViewHolder which should be updated to represent the
     *                                  contents of the item at the given position in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(PortfolioAdapterViewHolder portfolioAdapterViewHolder, int position) {
        if (!mCursor.isClosed()) {
            try {
                mCursor.moveToPosition(position);

                /* Read symbol from the cursor */
                String symbol = mCursor.getString(PortfolioActivity.INDEX_SYMBOL);
                /* Display stock symbol */
                portfolioAdapterViewHolder.symbolView.setText(symbol.toUpperCase());

                /* Read shares from the cursor */
                String shares = mCursor.getString(PortfolioActivity.INDEX_SHARES);
                /* Display Shares */
                portfolioAdapterViewHolder.sharesView.setText(shares);

                /* Read price from the cursor */
                double price = mCursor.getDouble(PortfolioActivity.INDEX_PRICE);
                /* Set the text */
                portfolioAdapterViewHolder.priceView.setText("$" + price);

                /* Read price change from the cursor */
                double priceChange = mCursor.getDouble(PortfolioActivity.INDEX_PRICE_CHANGE);
                /* Set the text */
                portfolioAdapterViewHolder.priceChangeView.setText(priceChange + " ");

                /* Read price change from the cursor */
                double percentChange = mCursor.getDouble(PortfolioActivity.INDEX_PERCENT_CHANGE);
                /* Set the text */
                portfolioAdapterViewHolder.percentChangeView.setText("(" + percentChange + "%)");

                // Sets the colour depending on the percent Change
                if (percentChange > 0) {
                    portfolioAdapterViewHolder.priceView.setTextColor(ContextCompat.
                            getColor(mContext, R.color.priceGreen));
                    portfolioAdapterViewHolder.priceChangeView.setTextColor(ContextCompat.
                            getColor(mContext, R.color.percentChangeGreen));
                    portfolioAdapterViewHolder.percentChangeView.setTextColor(ContextCompat.
                            getColor(mContext, R.color.percentChangeGreen));
                } else if (percentChange < 0){
                    portfolioAdapterViewHolder.priceView.setTextColor(ContextCompat.
                            getColor(mContext, R.color.priceRed));
                    portfolioAdapterViewHolder.priceChangeView.setTextColor(ContextCompat.
                            getColor(mContext, R.color.percentChangeRed));
                    portfolioAdapterViewHolder.percentChangeView.setTextColor(ContextCompat.
                            getColor(mContext, R.color.percentChangeRed));
                }
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
     * @param newCursor the new cursor to use as WatchlistAdapter's data source
     */
    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

    /**
     * A ViewHolder is a required part of the pattern for RecyclerViews. It mostly behaves as
     * a cache of the child views for a forecast item. It's also a convenient place to set an
     * OnClickListener, since it has access to the adapter and the views.
     */
    public class PortfolioAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView symbolView;
        final TextView sharesView;
        final TextView priceView;
        final TextView priceChangeView;
        final TextView percentChangeView;

        PortfolioAdapterViewHolder(View view) {
            super(view);
            symbolView = (TextView) view.findViewById(R.id.portfolio_symbol);
            sharesView = (TextView) view.findViewById(R.id.portfolio_shares);
            priceView = (TextView) view.findViewById(R.id.portfolio_price);
            priceChangeView = (TextView) view.findViewById(R.id.portfolio_price_change);
            percentChangeView = (TextView) view.findViewById(R.id.portfolio_percent_change);

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
