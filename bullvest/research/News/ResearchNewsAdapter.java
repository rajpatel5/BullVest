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
package com.target.android.bullvest.research.News;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.target.android.bullvest.utils.NewsData;
import com.target.android.bullvest.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * An {@link ResearchNewsAdapter} knows how to create a list item layout for each stock
 * in the data source (a list of {@link NewsData} objects).
 *
 * These list item layouts will be provided to an adapter view like ListView
 * to be displayed to the user.
 */
public class ResearchNewsAdapter extends ArrayAdapter<NewsData> {

    // Used for logging
    private final String TAG = ResearchNewsAdapter.class.getSimpleName();

    /* The context we use to utility methods, app resources and layout inflaters */
    private final Context mContext;

    /**
     * Constructs a new {@link ResearchNewsAdapter}.
     *
     * @param context of the app
     * @param news is the list of articles, which is the data source of the adapter
     */
    public ResearchNewsAdapter(Context context, List<NewsData> news) {
        super(context, 0, news);
        mContext = context;
    }

    /**
     * Returns a list item view that displays information about the article at the given position
     * in the list of articles.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if there is an existing list item view (called convertView) that we can reuse,
        // otherwise, if convertView is null, then inflate a new list item layout.
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.news_list_item, parent, false);
        }

        // Find the article at the given position in the list of articles
        NewsData currentArticle = getItem(position);

        // Find the TextView with view ID headline
        TextView headlineView = (TextView) listItemView.findViewById(R.id.headline);
        // Retrieve the headline
        String headline = currentArticle.getHeadline();
        // Display the headline of the current article in that TextView
        headlineView.setText(headline);

        // Find the TextView with view ID source
        TextView sourceView = (TextView) listItemView.findViewById(R.id.source);
        // Retrieve the source
        String source = currentArticle.getSource();
        // Display the source of the current article in that TextView
        sourceView.setText(source);

        // Find the TextView with view ID datetime
        TextView datetimeView = (TextView) listItemView.findViewById(R.id.datetime);

        // Retrieves the datetime and stores the hour and minute value
        String datetime = currentArticle.getDatetime();
        String array[]= datetime.split("T");
        int articleDate = Integer.parseInt(array[0].substring(8,10));
        int articleHour = Integer.parseInt(array[1].substring(0,2));
        int articleMinute = Integer.parseInt(array[1].substring(3,5));

        // Gets the time and formats the Date, Hour and Minute
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd");
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
        SimpleDateFormat minuteFormat = new SimpleDateFormat("mm");
        int currentDate = Integer.parseInt(dateFormat.format(calendar.getTime()));
        int currentHour = Integer.parseInt(hourFormat.format(calendar.getTime()));
        int currentMinute = Integer.parseInt(minuteFormat.format(calendar.getTime()));

        if (currentDate - articleDate > 0){
            // Display the datetime of the current article in that TextView
            datetimeView.setText((currentDate - articleDate) + "d ago");
        } else if (currentHour - articleHour > 0) {
            // Display the datetime of the current article in that TextView
            datetimeView.setText((currentHour - articleHour) + "h ago");
        } else if (currentHour - articleHour == 0){
            // Display the datetime of the current article in that TextView
            datetimeView.setText((currentMinute - articleMinute) + "m ago");
        }

        // Return the list item view that is now showing the appropriate data
        return listItemView;
    }
}
