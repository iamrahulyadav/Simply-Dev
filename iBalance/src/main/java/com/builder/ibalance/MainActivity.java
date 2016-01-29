package com.builder.ibalance;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.builder.ibalance.adapters.MainActivityAdapter;
import com.builder.ibalance.messages.MinimumBalanceMessage;
import com.builder.ibalance.util.ConstantsAndStatics;
import com.builder.ibalance.util.Helper;
import com.builder.ibalance.util.MyApplication;
import com.digits.sdk.android.Digits;
import com.facebook.appevents.AppEventsLogger;
import com.flurry.android.FlurryAgent;
import com.kahuna.sdk.Kahuna;
import com.parse.ConfigCallback;
import com.parse.ParseConfig;
import com.parse.ParseException;

import de.greenrobot.event.EventBus;

public class MainActivity extends AppCompatActivity {
    final String tag = MainActivity.class.getSimpleName();
    SharedPreferences userDataPrefs;
    protected RecyclerView mRecyclerView;
    public static boolean sim1BalanceReminderShown = false, sim2BalanceReminderShown = false;
    EditText input;
    //MoPubInterstitial mInterstitial;

    //public static int appOpenCount = 0;
    //public static int adFrequency = -1;

    // MainActivityAdapter mainActivityAdapter;
    MainActivityAdapter mainActivityAdapter;
    ViewPager mViewPager;
    boolean showingUpdateDialog = false;
    public ActionBar ab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        userDataPrefs = getSharedPreferences(ConstantsAndStatics.USER_PREF_KEY, Context.MODE_PRIVATE);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        ImageView view = (ImageView) findViewById(android.R.id.home);

        ab = getSupportActionBar();
        ab.setHomeAsUpIndicator(null);
        ab.setLogo(R.mipmap.app_logo);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mainActivityAdapter = new MainActivityAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mainActivityAdapter);
        mViewPager.setOffscreenPageLimit(1);


        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
    /*	mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);

					}
				});*/

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mainActivityAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.s

            tabLayout.addTab(tabLayout.newTab().setText(mainActivityAdapter.getPageTitle(i)));
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
			/*actionBar.addTab(actionBar.newTab()
					.setText(mainActivityAdapter.getPageTitle(i))
					.setTabListener(this));*/
        }
        tabLayout.setupWithViewPager(mViewPager);
        Boolean isEnabledAccess = Helper.isAccessibilityEnabled(ConstantsAndStatics.accessibiltyID);
        boolean hasRefreshedBAl = userDataPrefs.getBoolean("REFRESHED_BAL",false);

        float currentBal = userDataPrefs.getFloat("CURRENT_BALANCE_0",userDataPrefs.getFloat("CURRENT_BALANCE_1",-20.0f));
        if(currentBal>-20.0f)
            hasRefreshedBAl = true;
        if (!isEnabledAccess || hasRefreshedBAl == false )
        {
            //Log.d(TAG, "Accesibilty  Not Enabled");
            ConstantsAndStatics.WAITING_FOR_SERVICE = true;
            startActivity(new Intent(getApplicationContext(), OnBoardingActivity.class));

        }
        else
        {
            //Won't happen but a precaution
            //If Recharge was called
            if (this.getIntent().getBooleanExtra("RECHARGE", false))
            {
                goToRechargepage();
            }
            else
            {

                final int APP_VERSION = BuildConfig.VERSION_CODE;
                ParseConfig.getInBackground(new ConfigCallback()
                {
                    @Override
                    public void done(ParseConfig config, ParseException e)
                    {
                        if (e == null)
                        {
                            //V16Log.d(tag, "Yay! Config was fetched from the server.");
                        } else
                        {
                            Log.e(tag, "Failed to fetch. Using Cached Config.");
                            config = ParseConfig.getCurrentConfig();
                        }
                        if ((config != null))
                        {
                            int NEW_APP_VERSION = config.getInt("APP_VERSION");
                            //V16Log.d(tag,"NEW_PARSER_VERSION = "+NEW_PARSER_VERSION);
                            if (APP_VERSION <NEW_APP_VERSION)
                            {
                                showingUpdateDialog = true;
                                createUpdateDialog();
                            }
                        }
                        //Log.d(tag, String.format("The ad frequency is %d!", adFrequency));
                    }
                });

            }
        }


    }

    private void createUpdateDialog()
    {
        Helper.logGA("UPDATE","SHOWN");
        Helper.logFlurry("UPDATE","ACTION","SHOWN");
        AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View mView = inflater.inflate(R.layout.recharge_reminder, null);
        alertbox.setView(mView);
        ((TextView)mView.findViewById(R.id.alert_heading)).setText("Simply Update");
        mView.findViewById(R.id.alert_static_text).setVisibility(View.GONE);
        TextView infoText = (TextView) mView.findViewById(R.id.low_bal_text_id);
        Button rechargeNow = (Button) mView.findViewById(R.id.recharge_now_button);
        rechargeNow.setText("Update");
        infoText.setText("New Version of the App is Available,\n Update is Highly Recommended");

        final AlertDialog alert = alertbox.create();

        rechargeNow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
               Helper.logGA("UPDATE","CLICKED");
               Helper.logFlurry("UPDATE","ACTION","CLICKED");
                Uri uri = Uri.parse("market://details?id=" + MainActivity.this.getPackageName());
                //Log.d(tag,"URI = "+ uri);
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" +  MainActivity.this.getPackageName())));
                }
                alert.dismiss();
            }
        });
        alert.show();
    }

    public void goToRechargepage() {
        mViewPager.setCurrentItem(3);
    }

    @Override
    protected void onResume() {
        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
        super.onResume();
    }


    @Override
    protected void onPause() {

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
        super.onPause();
    }

    @Override
    protected void onStart() {
        //V10Log.d(tag, "onStart Main Activity");
        Kahuna.getInstance().start();
        FlurryAgent.logEvent("MainScreen", true);
        //Get an Analytics tracker to report app starts and uncaught exceptions etc.
        //GoogleAnalytics.getInstance(this).reportActivityStart(this);
        super.onStart();
        // Your Code Here
    }

    @Override
    protected void onStop() {

        //V10Log.d(tag, "Stopping Main Activity");


        Kahuna.getInstance().stop();
        FlurryAgent.endTimedEvent("MainScreen");
        //Stop the analytics tracking
       // GoogleAnalytics.getInstance(this).reportActivityStop(this);
        super.onStop();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.main, menu);
        //Show n Hide Date menu itme

        View view = (View) findViewById(R.id.spinner_nav);
        view.getVisibility();
        if ((view.getVisibility() == view.VISIBLE))
            view.setVisibility(View.GONE);
        return true;

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
       /* SharedPreferences mSharedPreferences = getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
        if (mSharedPreferences.getBoolean("USER_VERIFIED", false)) {
            //menu.findItem().setTitle("Login");
            menu.removeItem(R.id.log);
        }*/


        return true;
    }

int i = 0;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.share:
                boolean isWhatsappInstalled = Helper.whatsappInstalledOrNot();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "To Track your prepaid Balance and know how much you spend on your contacts.\nTry out \"Simply\": http://bit.ly/SimplyApp ");
                sendIntent.setType("text/plain");
                if (isWhatsappInstalled) {

                    // Build and send an Event.
                    Helper.logGA("SHARE","WhatsApp");
                    Helper.logFlurry("SHARE","SOURCE","APP");
                    //V10AppsFlyerLib.sendTrackingWithEvent(MyApplication.context,"WhatsApp_Share","");
                    sendIntent.setPackage("com.whatsapp");
                    startActivity(sendIntent);
                } else {
                    // Build and send an Event.
                    Helper.logGA("SHARE","OTHER_SHARE");
                    //V10AppsFlyerLib.sendTrackingWithEvent(MyApplication.context,"OTHER_SHARE","");
                    Helper.logFlurry("SHARE","SOURCE","APP");
                    startActivity(sendIntent);
                }
                break;
            case R.id.set_bal:
                //Log.d(tag, "Set Bal");
                return setbal();
            case R.id.privacy:
                /*
                Intent popup_intent = new Intent(this,
                        UssdPopup.class);
                popup_intent.putExtra("TYPE", ConstantsAndStatics.USSD_TYPES.NORMAL_CALL);

                popup_intent.putExtra("DATA", new NormalCallPopup());
                popup_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                popup_intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                popup_intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(popup_intent);*/
                //Log.d(tag, "Prefrences selected");
			/*USSDParser mParser = new USSDParser();
                if(i==0)
			mParser.parseMessage("INTERNETUSAGE:0.00MB MAINBAL:RS.119.68._124=124 TT 15DREPLY WITH 1");
                if(i==1)
            mParser.parseMessage("CALLCOST:RS.0.31 DURATION:00:00:17 BAL:RS.219.01_RS186=1GB 3G/4G,28D,DIAL *121*1# FOR");
                i++;
			Log.d(tag,mParser.getDetails().getType()+"");
			Log.d(tag,mParser.getDetails().toString());*/
                String url = "http://ibalanceapp.com/privacy-policy/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            case R.id.contact_us:
                //Log.d(tag, "contact_us selected");
                startActivity(new Intent(this, ContactUs.class));
                break;
            /*case R.id.log:
                //Log.d(tag, "contact_us selected");
                //verifyUser();
                break;*/
            case R.id.rate:

                SharedPreferences userPrefs = MyApplication.context.getSharedPreferences(ConstantsAndStatics.USER_PREF_KEY,Context.MODE_PRIVATE);
                userPrefs.edit().putBoolean("RATED",true).apply();
                Helper.logGA("RATE","APP");
                Helper.logGA("RATE","SOURCE","APP");
                Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
                //Log.d(tag,"URI = "+ uri);
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + this.getPackageName())));
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void verifyUser() {
        SharedPreferences mSharedPreferences = getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
        Digits.authenticate(((DigitLoginActivity) getApplication()).getAuthCallback(), R.style.CustomDigitsTheme);
        finish();

    }

    private Boolean setbal() {


        // create a dialog box to enter the minimum balance
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View mView = inflater.inflate(R.layout.set_balance_reminder, null);
        alertBuilder.setView(mView);

        // Set an EditText view to get user input
        input = (EditText) mView.findViewById(R.id.balance_value);
        Float previousSetBal = userDataPrefs.getFloat("MINIMUM_BALANCE", (float) 10.0);
        input.setText(previousSetBal + "");
        input.setFocusable(true);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
        input.setMinimumWidth(5);

        input.setSelection(input.getText().length(), input.getText().length());
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        Button done = (Button) mView.findViewById(R.id.balance_reminder_done);
        Button cancel = (Button) mView.findViewById(R.id.balance_reminder_cancel);
        final AlertDialog alert = alertBuilder.create();
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Float setBal;
                try {
                    if(TextUtils.isEmpty(input.getText().toString()))
                    {
                        Toast.makeText(MainActivity.this, "Plese Enter the Minimum Balance", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    setBal = Float.parseFloat(input.getText().toString());
                } catch (Exception e) {
                    setBal = userDataPrefs.getFloat("MINIMUM_BALANCE", (float) 10.0);
                }
                //Log.d("CHART", setBal + " ");
                userDataPrefs.edit().putFloat("MINIMUM_BALANCE", setBal).commit();
                EventBus.getDefault().post(new MinimumBalanceMessage(setBal));
                alert.dismiss();

            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });
        alert.show();
        //Log.d("CHART", "Dialog Creation done");
        return true;

    }

    /*@Override
    public void onTabSelected(ActionBar.Tab tab,
            FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab,
            FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab,
            FragmentTransaction fragmentTransaction) {
    }

*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        userDataPrefs.edit().putInt("FILTER", 0).commit();
        sim1BalanceReminderShown = false;
        sim2BalanceReminderShown = false;
		/*if(mInterstitial!=null)
		mInterstitial.destroy();*/
    }

}
