package com.target.android.bullvest.account;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.target.android.bullvest.R;
import com.target.android.bullvest.data.BalanceDbHelper;
import com.target.android.bullvest.data.PortfolioChartDbHelper;
import com.target.android.bullvest.data.StockContract;
import com.target.android.bullvest.hotlist.HotListActivity;
import com.target.android.bullvest.learningcenter.LearningCenterActivity;
import com.target.android.bullvest.portfolio.PortfolioActivity;
import com.target.android.bullvest.research.ResearchActivity;
import com.target.android.bullvest.trade.TradeActivity;
import com.target.android.bullvest.transactions.TransactionsActivity;
import com.target.android.bullvest.utils.Constants;
import com.target.android.bullvest.watchlist.WatchlistActivity;

public class AccountActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Used for logging
    private final String TAG = AccountActivity.class.getSimpleName();

    public static double cashBalance;
    public static double commissions;

    BalanceDbHelper balanceDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Hide both the status bar.
//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//        decorView.setSystemUiVisibility(uiOptions);

        // Displays ads
//        AdView adView = (AdView)findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().tagForChildDirectedTreatment(true).build();
//        adView.loadAd(adRequest);

        // Adds each starting balance point into an ArrayList
        PortfolioChartDbHelper portfolioChartBalanceDatabase;
        portfolioChartBalanceDatabase = new PortfolioChartDbHelper(this);

        balanceDatabase = new BalanceDbHelper(this);
        cashBalance = balanceDatabase.getBalance().getCash();
        commissions = balanceDatabase.getBalance().getCommissions();

        // Initializes the necessary components
        Button reset = (Button) findViewById(R.id.reset);
        Button save_changes = (Button) findViewById(R.id.save_changes);
        final EditText cashEntry = (EditText) findViewById(R.id.cash_field);
        final EditText commissionsEntry = (EditText) findViewById(R.id.commissions_field);

        final SeekBar cashBar = (SeekBar) findViewById(R.id.cash_bar); // Initiate the cash bar
        cashBar.setMax(250000); // 250, 000 maximum value for the Cash bar
        cashEntry.setText((int)portfolioChartBalanceDatabase.getStartingBalance().getBalance() + "");
        cashBar.setProgress(Integer.parseInt(cashEntry.getText().toString()));

        // Initiate the commissions bar
        final SeekBar commissionsBar = (SeekBar) findViewById(R.id.commissions_bar);
        commissionsBar.setMax(25); // 25 maximum value for the Commissions bar
        commissionsEntry.setText((int)commissions + "");
        commissionsBar.setProgress(Integer.parseInt(commissionsEntry.getText().toString()));

        // Sets the progress
        cashBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                cashEntry.setText(progress + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Empty
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Empty
            }
        });
        commissionsBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                commissionsEntry.setText(progress + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Empty
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Empty
            }
        });

        // Sets the progress based on the text entered
        cashEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Empty
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals("")) {
                    if (Integer.parseInt(s.toString()) > 250000){
                        cashEntry.setText("250000");
                    }
                    cashBar.setProgress(Integer.parseInt(s.toString()));
                } else {
                    cashEntry.setText("0");
                }
                cashEntry.getSelectionEnd();
                cashEntry.setSelection(cashEntry.length());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Empty
            }
        });
        cashEntry.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    // Sets the cursor all the way to the end
                    cashEntry.setSelection(cashEntry.length());
                }
            }
        });
        commissionsEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Empty
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals("")) {
                    if (Integer.parseInt(s.toString()) > 25) {
                        commissionsEntry.setText("25");
                    }
                    commissionsBar.setProgress(Integer.parseInt(s.toString()));
                } else {
                    commissionsEntry.setText("0");
                }
                commissionsEntry.getSelectionEnd();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Empty
            }
        });
        commissionsEntry.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    // Sets the cursor all the way to the end
                    commissionsEntry.setSelection(commissionsEntry.length());
                }
            }
        });

        // Listens for any clicks on the RESET button
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if (v.getId() == R.id.reset) {
                hideKeyboard(AccountActivity.this);
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                // Empty
                            }
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this);
                builder.setMessage("Are you sure you would like to Reset your Account?");
                builder.setPositiveButton("Cancel", discardButtonClickListener);
                builder.setNegativeButton("Reset", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked the "Submit" button, so dismiss the dialog
                        // and continue with the transaction.
                        if (dialog != null) {
                            // Delete the entire table
                            getContentResolver().delete(StockContract.FilledEntry.CONTENT_URI,
                                    null, null);
                            getContentResolver().delete(StockContract.PendingEntry.CONTENT_URI,
                                    null, null);
                            getContentResolver().delete(StockContract.MyInvestmentsEntry.CONTENT_URI,
                                    null, null);

                            PortfolioChartDbHelper portfolioChartBalanceDatabase;
                            portfolioChartBalanceDatabase = new PortfolioChartDbHelper(AccountActivity.this);

                            portfolioChartBalanceDatabase.deleteAllBalances();
                            portfolioChartBalanceDatabase.addBalance(Double.parseDouble(
                                    cashEntry.getText().toString()));

                            // Sets the new values
                            balanceDatabase.setBalance(Constants.balanceId,
                                    Double.parseDouble(cashEntry.getText().toString()),
                                    Double.parseDouble(commissionsEntry.getText().toString()));

                            // Displays a message to user
                            Toast.makeText(getApplicationContext(), "Account has been Reset By " +
                                            "Setting New Balance and Commissions Value",
                                    Toast.LENGTH_LONG).show();

                            dialog.dismiss();
                        }
                    }
                });

                // Create and show the AlertDialog
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                }
            }
        });

        // Listens for any clicks on the Save Changes button
        save_changes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PortfolioChartDbHelper portfolioChartBalanceDatabase;
                portfolioChartBalanceDatabase = new PortfolioChartDbHelper(AccountActivity.this);

                double startingBalance = portfolioChartBalanceDatabase.getStartingBalance().getBalance();

                if (v.getId() == R.id.save_changes) {
                    hideKeyboard(AccountActivity.this);

                    if (!(startingBalance == Double.parseDouble(cashEntry.getText().toString()))){
                        Toast.makeText(getApplicationContext(), "Cannot Change Account Balance" +
                                        " You Must RESET The Account",
                                Toast.LENGTH_LONG).show();
                    } else {

                        // sets the new values
                        balanceDatabase.setBalance(Constants.balanceId, cashBalance,
                                Double.parseDouble(commissionsEntry.getText().toString()));

                        Toast.makeText(getApplicationContext(), "Set New Commissions Value",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        // Used for the navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        hideKeyboard(AccountActivity.this);
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
            case R.id.nav_hot_list:
                Intent hotListIntent = new Intent(this, HotListActivity.class);
                startActivity(hotListIntent);
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Hides the keyboard
     */
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
