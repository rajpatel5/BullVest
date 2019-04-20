/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.target.android.bullvest.hotlist;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.target.android.bullvest.R;
import com.target.android.bullvest.utils.Stock;

import java.text.DecimalFormat;
import java.util.List;

/**
 * An {@link HotListStockAdapter} knows how to create a list item layout for each stock
 * in the data source (a list of {@link Stock} objects).
 *
 * These list item layouts will be provided to an adapter view like ListView
 * to be displayed to the user.
 */
public class HotListStockAdapter extends ArrayAdapter<Stock> {

    /* The context we use to utility methods, app resources and layout inflaters */
    private final Context mContext;

    /**
     * Constructs a new {@link HotListStockAdapter}.
     *
     * @param context of the app
     * @param stocks is the list of stocks, which is the data source of the adapter
     */
    public HotListStockAdapter(Context context, List<Stock> stocks) {
        super(context, 0, stocks);
        mContext = context;
    }

    /**
     * Returns a list item view that displays information about the stock at the given position
     * in the list of stocks.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if there is an existing list item view (called convertView) that we can reuse,
        // otherwise, if convertView is null, then inflate a new list item layout.
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.quote_list_item, parent, false);
        }

        // Find the stock at the given position in the list of stocks
        Stock currentStock = getItem(position);

        // Find the TextView with view ID symbol
        TextView symbolView = (TextView) listItemView.findViewById(R.id.symbol);
        // Retrieve the symbol
        String symbol = currentStock.getSymbol();
        // Display the symbol of the current stock in that TextView
        symbolView.setText(symbol);

        // Find the TextView with view ID company_name
        TextView companyNameView = (TextView) listItemView.findViewById(R.id.company_name);
        // Retrieve the company name
        String companyName = currentStock.getCompanyName();
        // Display the company name of the current stock in that TextView
        companyNameView.setText(companyName);

        // Find the TextView with view ID price
        TextView priceView = (TextView) listItemView.findViewById(R.id.price);
        // Retrieve the price
        String formattedPrice = formatDecimal(currentStock.getPrice());
        // Display the price of the current stock in that TextView
        priceView.setText("$" + formattedPrice);

        // Find the TextView with view ID price_change
        TextView priceChangeView = (TextView) listItemView.findViewById(R.id.price_change);
        // Retrieve the price change
        String formattedPriceChange = formatDecimal(currentStock.getPriceChange());
        // Display the price change of the current stock in that TextView
        priceChangeView.setText(formattedPriceChange);

        // Find the TextView with view ID percent_change
        TextView percentChangeView = (TextView) listItemView.findViewById(R.id.percent_change);
        // Retrieve the percent change
        String formattedPercentChange = formatDecimal(currentStock.getPercentChange());
        // Display the percent change of the current stock in that TextView
        percentChangeView.setText(" (" + formattedPercentChange + "%)");

        /* Sets the colour depending on the value of percent change */
        if (currentStock.getPercentChange() > 0){
            priceView.setTextColor(ContextCompat.getColor(mContext, R.color.priceGreen));
            priceChangeView.setTextColor(ContextCompat.getColor(mContext, R.color.percentChangeGreen));
            percentChangeView.setTextColor(ContextCompat.getColor(mContext, R.color.percentChangeGreen));
        }
        else if (currentStock.getPercentChange() < 0){
            priceView.setTextColor(ContextCompat.getColor(mContext, R.color.priceRed));
            priceChangeView.setTextColor(ContextCompat.getColor(mContext, R.color.percentChangeRed));
            percentChangeView.setTextColor(ContextCompat.getColor(mContext, R.color.percentChangeRed));
        }

        // Return the list item view that is now showing the appropriate data
        return listItemView;
    }

    /**
     * Return the formatted value string
     * showing 2 decimal places (i.e. "3.25") from a decimal value.
     */
    private String formatDecimal(double item) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return decimalFormat.format(item);
    }

}
