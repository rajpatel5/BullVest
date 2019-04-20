package com.target.android.bullvest.hotlist;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.target.android.bullvest.R;
import com.target.android.bullvest.account.AccountActivity;
import com.target.android.bullvest.learningcenter.LearningCenterActivity;
import com.target.android.bullvest.portfolio.PortfolioActivity;
import com.target.android.bullvest.research.ResearchActivity;
import com.target.android.bullvest.trade.TradeActivity;
import com.target.android.bullvest.transactions.TransactionsActivity;
import com.target.android.bullvest.watchlist.WatchlistActivity;

public class HotListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_list);

        // Hide both the status bar.
//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//        decorView.setSystemUiVisibility(uiOptions);

        // Find the view pager that will allow the user to swipe between fragments
        ViewPager viewPager = (ViewPager) findViewById(R.id.hotlist_viewpager);

        // Displays ads
//        AdView adView = (AdView)findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().tagForChildDirectedTreatment(true).build();
//        adView.loadAd(adRequest);

        // Create an adapter that knows which fragment should be shown on each page
        HotListCategoryAdapter adapter = new HotListCategoryAdapter(this, getSupportFragmentManager());

        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);

        // Find the tab layout that shows the tabs
        TabLayout tabLayout = (TabLayout) findViewById(R.id.hotlist_tabs);

        // Connect the tab layout with the view pager.
        tabLayout.setupWithViewPager(viewPager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    /** Closes the drawer if the back button is clicked, or goes back to its previous activity */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hot_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.account_settings) {
            Intent accountIntent = new Intent(this, AccountActivity.class);
            startActivity(accountIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        // Decides which Activity to open based on user click
        switch (id){
            case R.id.nav_portfolio:
                Intent portfolioIntent = new Intent(this, PortfolioActivity.class);
                startActivity(portfolioIntent);
                break;
            case R.id.nav_trade:
                Intent tradeIntent = new Intent(this, TradeActivity.class);
                startActivity(tradeIntent);
                break;
            case R.id.nav_watchlist:
                Intent watchlistIntent = new Intent(this, WatchlistActivity.class);
                startActivity(watchlistIntent);
                break;
            case R.id.nav_research:
                Intent researchIntent = new Intent(this, ResearchActivity.class);
                startActivity(researchIntent);
                break;
            case R.id.nav_transactions:
                Intent transactionsIntent = new Intent(this, TransactionsActivity.class);
                startActivity(transactionsIntent);
                break;
            case R.id.nav_learning_center:
                Intent learningCenterIntent = new Intent(this, LearningCenterActivity.class);
                startActivity(learningCenterIntent);
                break;
        }

        // Closes the drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
