package com.builder.ibalance;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.appsflyer.AppsFlyerLib;
import com.builder.ibalance.adapters.MainActivityAdapter;
import com.builder.ibalance.datainitializers.DataInitializer;
import com.builder.ibalance.messages.MinimumBalanceMessage;
import com.builder.ibalance.util.MyApplication;
import com.builder.ibalance.util.MyApplication.TrackerName;
import com.builder.ibalance.util.RegexUpdater;
import com.facebook.appevents.AppEventsLogger;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.kahuna.sdk.Kahuna;
import com.parse.ConfigCallback;
import com.parse.ParseConfig;
import com.parse.ParseException;

import de.greenrobot.event.EventBus;

public class MainActivity extends Activity implements ActionBar.TabListener {
	final String tag = MainActivity.class.getSimpleName();
	SharedPreferences mSharedPreferences;
	EditText input ;
	//MoPubInterstitial mInterstitial;
	Tracker t = ((MyApplication)MyApplication.context).getTracker(
		    TrackerName.APP_TRACKER);

	//public static int appOpenCount = 0;
	//public static int adFrequency = -1;

	 MainActivityAdapter mMainActivityAdapter;
	ViewPager mViewPager;
	int PARSER_VERSION = 1;
	int NEW_PARSER_VERSION = 1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mSharedPreferences = getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
		PARSER_VERSION =  getSharedPreferences("GOOGLE_PREFS", Context.MODE_PRIVATE).getInt("PARSER_VERSION",1);
		ParseConfig.getInBackground(new ConfigCallback()
		{
			@Override
			public void done(ParseConfig config, ParseException e)
			{
				if (e == null)
				{
					//Log.d(tag, "Yay! Config was fetched from the server.");
				} else
				{
					Log.e(tag, "Failed to fetch. Using Cached Config.");
					config = ParseConfig.getCurrentConfig();
				}
                if((config!=null))
                {
                    NEW_PARSER_VERSION = config.getInt("PARSER_VERSION");
                    if (NEW_PARSER_VERSION > PARSER_VERSION)
                    {
                        new RegexUpdater().update(NEW_PARSER_VERSION);
                    }
                }
				//Log.d(tag, String.format("The ad frequency is %d!", adFrequency));
			}
		});

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);/*
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.custom_actionbar_title); */
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mMainActivityAdapter = new MainActivityAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mMainActivityAdapter);
		mViewPager.setOffscreenPageLimit(1);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);

					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mMainActivityAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.s
			
	        
			actionBar.addTab(actionBar.newTab()
					.setText(mMainActivityAdapter.getPageTitle(i))
					.setTabListener(this));
		}

		
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
	        GoogleAnalytics.getInstance(this).reportActivityStart(this);
	        super.onStart();
	        // Your Code Here
	    }

	    @Override
	    protected void onStop() {

	       //V10Log.d(tag, "Stopping Main Activity");
	        Kahuna.getInstance().stop();
	        FlurryAgent.endTimedEvent("MainScreen");
	      //Stop the analytics tracking
	        GoogleAnalytics.getInstance(this).reportActivityStop(this);
	        super.onStop();
	    } 


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}



	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {

		case R.id.set_bal:
			//Log.d(tag, "Set Bal");
			return setbal();
		/*case R.id.help:
			//Log.d(tag, "help chat");
			startActivity(new Intent(this, ChatActivity.class));
			break;*/
		case R.id.preferences:
			//Log.d(tag, "Prefrences selected");
			startActivity(new Intent(this, Preferences.class));
			break;
		case R.id.contact_us:
			//Log.d(tag, "contact_us selected");
			startActivity(new Intent(this, ContactUs.class));
			break;
		case R.id.rate:
			t.send(new HitBuilders.EventBuilder().setCategory("RATE")
					.setAction("Rate").setLabel("").build());
			FlurryAgent.logEvent("Rate");
			AppsFlyerLib.sendTrackingWithEvent(MyApplication.context,
					"Rate", "");
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
	private Boolean setbal() {
	
		
		// create a dialog box to enter the minimum balance
		final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
		LayoutInflater inflater = getLayoutInflater();
        View mView = inflater.inflate(R.layout.balance_reminder, null);
		alertBuilder.setView(mView);

		// Set an EditText view to get user input
		input = (EditText) mView.findViewById(R.id.balance_value);
		Float previousSetBal = mSharedPreferences.getFloat("MINIMUM_BALANCE", (float) 10.0);
		input.setText(previousSetBal+"");
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
		done.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Float setBal;
                try
                {

                    setBal = Float.parseFloat(input.getText().toString());
                } catch (Exception e)
                {
                    setBal = mSharedPreferences.getFloat("MINIMUM_BALANCE", (float) 10.0);
                }
                //Log.d("CHART", setBal + " ");
                mSharedPreferences.edit().putFloat("MINIMUM_BALANCE",  setBal).commit();
                EventBus.getDefault().post(new MinimumBalanceMessage(setBal));
                alert.dismiss();
            }
        });
		cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                alert.dismiss();
            }
        });
        alert.show();;
		//Log.d("CHART", "Dialog Creation done");
		return true;

	}
	@Override
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


	@Override
	protected void onDestroy() {
		super.onDestroy();
		DataInitializer.mainActivityRunning = false;
		mSharedPreferences.edit().putInt("FILTER",0).commit();
		/*if(mInterstitial!=null)
		mInterstitial.destroy();*/
	}

}
