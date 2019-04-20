package com.target.android.bullvest.hotlist;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.target.android.bullvest.R;

/**
 * {@link HotListCategoryAdapter} is a {@link FragmentPagerAdapter} that can provide the layout for
 * each list item
 */
public class HotListCategoryAdapter extends FragmentPagerAdapter {

    /**
     * Context of the app
     */
    private Context mContext;

    /**
     * Create a new {@link HotListCategoryAdapter} object.
     *
     * @param context is the context of the app
     * @param fm      is the fragment manager that will keep each fragment's state in the adapter
     *                across swipes.
     */
    public HotListCategoryAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    /**
     * Return the {@link Fragment} that should be displayed for the given page number.
     */
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new GainersFragment();
        } else if (position == 1) {
            return new LosersFragment();
        } else {
            return new MostActiveFragment();
        }
    }

    /**
     * Return the total number of pages.
     */
    @Override
    public int getCount() {
        return 3;
    }

    /**
     * Return the page title.
     */
    @Override
    public CharSequence getPageTitle ( int position){
        if (position == 0) {
            return mContext.getString(R.string.category_gainers);
        } else if (position == 1) {
            return mContext.getString(R.string.category_losers);
        } else {
            return mContext.getString(R.string.category_most_active);
        }
    }
}
