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
        //Get an Analytics tracker to report app starts and uncaught exceptions etc.
        //GoogleAnalytics.getInstance(this).reportActivityStart(this);
        super.onStart();
        // Your Code Here
    }

    @Override
    protected void onStop() {

        //V10Log.d(tag, "Stopping Main Activity");


        Kahuna.getInstance().stop();
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
    //matched as nomal call
    //"CALL_CHRG:0.00 RS,CALL_DURN:156 SECS,BAL_LEFT= 58.02 RS,UNLIMITED I2I MINUTES PACK_EXP:2015-07-06 \n",

int i = 0;
    //wrong match
    //"CALLCOST0.13MIN BALRS0.06 ALL LOC BAL25.43MINEXP31JUL15 DURATION8SEC GET LOAN BALANCE OF RS 4 DIAL *366# OR SMS LOAN TO 366 (TOLL FREE) \n",

    //Not working nopop up
    /*
    "CHRG: PROMO BAL RS 1.15 DUR: 29 SEC BAL_LEFT: MAIN RS 0.148 PROMOBAL:RS 13.50,EXP:10/07/2015.RS.25-250 MB 3G FOR 7 DAYS. RC 154-1GB 3G FOR 28 DAYS \n",
                              "CHRG: CAPPEDTT RS 0.70 DUR: 37 SEC BAL_LEFT: MAIN RS 34.660 CAPPEDTT RS 77.82 1. TOP JOBS FOR YOU @ RS 10 PRESS 1 FOR DETAILS \n",
                              "CALL_DURN: 19 SEC, CALL_CHRG: 0.00 INR , CHRG FROM LOC  MINS: 1MINS, MINS LEFT: 147MINS .RS5:15 LOC I2I MINS.@1DAY DIAL *369*4# TO ACTIVATE.\n",
                              "DURATION : 00:02:03, CHRG FROM MAIN BAL: 0.00 INR, CHRG FROM LOCAL IDEA NIGHT MINS : 3.00 INR, LOCAL IDEA NIGHT MINS LEFT:971, MAIN BALANCE LEFT: 227.16 INR\n",
                              "CALL_DURN : 00:00:09, CALL_CHRG: 0.00 INR, CHRG FROM LOCAL IDEA MINS : 1.00 INR, LOCAL IDEA MINS LEFT: 1813, BAL_LEFT= 92.46 INR    \n",
                              "CHRG: SECONDARY_COST RS 0.250 DUR: 12SEC  BAL_LEFT: MAIN RS 110.900 SECONDARY RS 50.350 EXP 20/07/2015 23:59HR.DIALER TONE RS18/MONTH-121300 \n",


    * */
/*    String [] ussdMessages = {"DATAUSAGE:0.04 MB DATA BAL:271.97MB VAL:FEB 27 2016 BAL:RS.101.67 NEED BALANCE? REPLY 1 TO TAKE RS 10 ADVANCE TALKTIMENULL",
                              "VOICE COST: LCL&NAT MIN-1 DURATION: 11SEC BALANCE: FREE LCLSTD MIN:34.00 EXP:31/07/2015NULL",
                              "DURATION:00:00:09;LOCAL/STD SEC DEDUCTED:9;LOCAL/STD SEC LEFT:1723;L/STD SEC VALIDITY TILL 06AUG2015;MAIN BALANCE:150.94 INR",
                              "CHRG: LOCAL NIGHT MIN 1 DUR: 12SEC  BAL_LEFT: MAIN RS 14.201 LOCAL NIGHT MIN 85 EXP 11/07/2015 23:59HR",
                              "CALL DURATION:130 SEC. CALL COST :ALL LOCAL MIN 3 .BALANCE :ALL LOCAL MIN 255. STV 64 OFFERS ULTD U2U LOC CALLS FOR 28 DAYS.DIAL *234# FOR SPL OFFERSNULL",
                              "DURATION:00:00:01 REM.LOCAL SECS:9204 VALIDITY:JUL 31 2015 MAINBAL:RS.0.00.",
                              "VOICE COST: A2A MIN-5 _DURATION: 253SEC_BALANCE: RS.0.04 _TALKTIME: 61.00 EXP:08/07/2015NULL\n",
                              "LAST CALL DURATION:00:00:01, DEDUCTION: LOC+STD BAL SEC=60 REMAININGBAL: LOC+STD BAL SEC=13107 45 LAKH TAK KA INAAM JEETO! DIAL *545*1# .KHAS MOUKE KELIYE KHAS CALLERTUNE,DIAL 1234\n",
                              "LASTCALLCHRG ,FREESEC BAL=3660.LASTCALL DURATION=01:00:01.REMAINING FREESEC BAL=2940 .MAIN BAL= 13.3380. CALL *444*108# TO MAKE LOCAL V2V CALL AT 10P/MIN IN RS108 FR28DAY\n",
                              "LAST CALL DUR-00:00:33,REMAING MAIN ACCOUNT BAL-46.77 INR,REMAING LOCAL ONNET MIN-26.00 INR.\n",
                              "YOUR LAST 1MIN HAVE BEEN CHARGED FROM FREE SEC ANYNET,LEFT 3HR 46MIN AND EXPIRES ON 12-02-2016.NULL\n",
                              "DURATION:00:00:10 REM.PACK BAL:15.20 VAL:JUL 08 2015 BAL:RS.0.00 TOP 10 TRENDING APPS TO JAZZ UP YOUR SMARTPHONE! TO KNOW MORE.REPLY WITH 1NULL\n",
                              "CHRG: PROMO BAL RS 1.15 DUR: 29 SEC BAL_LEFT: MAIN RS 0.148 PROMOBAL:RS 13.50,EXP:10/07/2015.RS.25-250 MB 3G FOR 7 DAYS. RC 154-1GB 3G FOR 28 DAYS \n",
                              "CHRG: CAPPEDTT RS 0.70 DUR: 37 SEC BAL_LEFT: MAIN RS 34.660 CAPPEDTT RS 77.82 1. TOP JOBS FOR YOU @ RS 10 PRESS 1 FOR DETAILS \n",
                              "CALL_DURN: 19 SEC, CALL_CHRG: 0.00 INR , CHRG FROM LOC  MINS: 1MINS, MINS LEFT: 147MINS .RS5:15 LOC I2I MINS.@1DAY DIAL *369*4# TO ACTIVATE.\n",
                              "CALL_CHRG:0.00 INR,CALL_DURN:137 SECS,BAL_LEFT=1.07 INR,LOC IDEA MIN3.00 INR,LOC IDEA MIN BAL 174.00 INR\n",
                              "DURATION : 00:02:03, CHRG FROM MAIN BAL: 0.00 INR, CHRG FROM LOCAL IDEA NIGHT MINS : 3.00 INR, LOCAL IDEA NIGHT MINS LEFT:971, MAIN BALANCE LEFT: 227.16 INR\n",
                              "CALL_DURN : 00:00:09, CALL_CHRG: 0.00 INR, CHRG FROM LOCAL IDEA MINS : 1.00 INR, LOCAL IDEA MINS LEFT: 1813, BAL_LEFT= 92.46 INR    \n",
                              "LAST CALL CHRG 0.02  INR.CURRENT BAL 34.20  INR,CALL DURATION 83 SEC.LOCAL V2V NIGHT MINS USED 0.02 .BAL FREE LOCAL V2V NIGHT MINS 0.27 .SPORTS *123*5#.\n",
                              "CALL DUR: IS 00:00:01,CHARGED 60 SECS FROM ALL LOCAL CALLS/MIN.BAL IS 325.000.MAIN ACC BAL IS 3.32 INR,OFFER! RS 147=970MB 3G, 28 DAYS. RECHARGE NOW.\n",
                              "CALL DUR-00:00:46,REMAING MAIN ACCOUNT BAL-0.00 INR,REMAING VOICE ISD MIN-28297.00 INR.LAST CALL USED MIN-46.00 INR\n",
                              "LAST CALL DURATION 0MIN 55SEC.LAST CALL/SMS CHARGE - RS0. MAIN ACCOUNT BALANCE : RS4.57.LOCAL MINS CHARGE  1MIN 0SEC BAL 406MIN 0SEC. CALL 55315 JAANE KAISE POORE HONGE AAPKE RUKE HUNULL\n",
                              "CALL CHARGES IS 1 MIN 0 SEC FROM MIN ACC.BAL IN MIN ACC IS 5 MIN 0 SEC.BAL IN ACC AFTER CALL IS RS 45.94. .DIAL 567899 FREE GOLD. DIAL *111# FREE DISCOUNTS.\n",

                              "LAST CALL OF 10SEC, USED FROM KERSTV135. REMAINING BALANCE IS 4HR 56MIN5SEC. YOUR MAIN ACCOUNT BALANCE IS RS. 0.013\n",
                              "LAST CALL:DURATION 01 MIN. 39 SEC.CHARGE RS0.000.LOC+STD MINS: DEDUCTED(MM:SS) - 02:00,BAL(HH:MM) - 09:27.MAIN ACC BAL RS:1050.546.*444*32# ROAMFREE 32RS\n",
                              "CALL DURATION:  00:00:10 . CALL CHARGED FROM:NIGHT BAL:SEC 10  REMAINING BAL AFTER THE CALL:  NIGHT BAL:SEC 98                 NULL\n",

                              "DATA SESSION CHARGE:-110.00SEC . BAL RS.0.49 YOUR BALANCE IN GPRSSEC IS 1030.00 SEC EXPIRING ON 16/07/2015. TIME USAGE IS 108 . \n",
                              "FREELOCMINS LASTUSAGE:60BAL:25260.DURATION:00:00:26.ENJOY LOCAL VODAFONE CALLS¡15P/MIN FOR 28 DAYS WITH RS119.\n",
                              "VOICE COST: RS.0.15  DURATION: 20SEC BALANCE:   FREE PROMOBAL RS.14.08 EXP:17/07/2015NULL\n",

                              "CHRG:0.00 INR,DURN:24 SEC,MIN/SEC USED:1MIN,AVL MIN/SEC:139MIN,VAL TILL 01-10-2015, BAL-LEFT:0.19 INR NULL\n",
                              "DURATION:00:00:19;PROMO BALANCE COST:0.10 INR;PROMO A/C BALANCE:70.78 INR;PROMO A/C VALIDITY TILL 04JAN2016;MAIN BALANCE:67.32 INR\n",
                              "DUR:00:00:03,SHULK:RS0.0300.PROMOBAL:RS32.6300.MAINBAL:RS6.8720.FORDOUBLEDATA,DOUBLEVALIDITYDIAL*121#.RS15=150SMS(14DIN)DIAL*444*15#\n",
                              "LASTCALLCOST:0.90INR,URACCNTBAL:26.12INRNLOCALATOABAL:0.00INRANDCALLDURATN:00:00:45\n",
                              "CALLCHARGE-SECS=120.CALLDURATION=00:01:28.BALANCE=RS.-5.99SECS=15300\n",

    };*/
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
                //EventBus.getDefault().post(new TestMessage(ussdMessages[i++]));
                //TODO Reset This
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
