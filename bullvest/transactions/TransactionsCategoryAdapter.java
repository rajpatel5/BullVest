package com.target.android.bullvest.transactions;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.target.android.bullvest.R;

/**
 * {@link TransactionsCategoryAdapter} is a {@link FragmentPagerAdapter} that can
 * provide the layout for each transaction list
 */

public class TransactionsCategoryAdapter extends FragmentPagerAdapter{

    /**
     * Context of the app
     */
    private Context mContext;

    /**
     * Create a new {@link TransactionsCategoryAdapter} object.
     *
     * @param context is the context of the app
     * @param fm      is the fragment manager that will keep each fragment's state in the adapter
     *                across swipes.
     */
    public TransactionsCategoryAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    /**
     * Return the {@link Fragment} that should be displayed for the given page number.
     */
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new PendingFragment();
        } else {
            return new FilledFragment();
        }
    }

    /**
     * Return the total number of pages.
     */
    @Override
    public int getCount() {
        return 2;
    }

    /**
     * Return the page title.
     */
    @Override
    public CharSequence getPageTitle (int position){
        if (position == 0) {
            return mContext.getString(R.string.pending);
        } else {
            return mContext.getString(R.string.filled);
        }
    }
}
