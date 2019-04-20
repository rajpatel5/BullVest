package com.target.android.bullvest.research.Charts;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.target.android.bullvest.R;

/**
 * {@link ResearchChartCategoryAdapter} is a {@link FragmentStatePagerAdapter}
 * that can provide the layout for each list item
 */
public class ResearchChartCategoryAdapter extends FragmentStatePagerAdapter {

    /** Context of the app */
    private Context mContext;

    // Initializes fragments
    public static DailyFragment DAILY = new DailyFragment();
    public static MonthlyFragment MONTHLY = new MonthlyFragment();
    public static SixMonthsFragment SIX_MONTHS = new SixMonthsFragment();
    public static YearlyFragment YEARLY = new YearlyFragment();
    public static FiveYearsFragment FIVE_YEARS = new FiveYearsFragment();

    /**
     * Create a new {@link ResearchChartCategoryAdapter} object.
     *
     * @param context is the context of the app
     * @param fm      is the fragment manager that will keep each fragment's state in the adapter
     *                across swipes.
     */
    public ResearchChartCategoryAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    /**
     * Return the {@link Fragment} that should be displayed for the given page number.
     */
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return DAILY;
        } else if (position == 1) {
            return MONTHLY;
        } else if (position == 2) {
            return SIX_MONTHS;
        } else if (position == 3) {
            return YEARLY;
        } else {
            return FIVE_YEARS;
        }
    }

    /**
     * Return the total number of pages.
     */
    @Override
    public int getCount() {
        return 5;
    }

    /**
     * Return the page titles.
     */
    @Override
    public CharSequence getPageTitle ( int position){
        if (position == 0) {
            return mContext.getString(R.string.daily);
        } else if (position == 1) {
            return mContext.getString(R.string.monthly);
        } else if (position == 2) {
            return mContext.getString(R.string.six_months);
        }else if (position == 3) {
            return mContext.getString(R.string.yearly);
        }else {
            return mContext.getString(R.string.five_years);
        }
    }
}
