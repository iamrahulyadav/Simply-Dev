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
import com.apptentive.android.sdk.Apptentive;
import com.builder.ibalance.adapters.MainActivityAdapter;
import com.builder.ibalance.datainitializers.DataInitializer;
import com.builder.ibalance.messages.DataLoadingDone;
import com.builder.ibalance.messages.MinimumBalanceMessage;
import com.builder.ibalance.util.MyApplication;
import com.builder.ibalance.util.MyApplication.TrackerName;
import com.builder.ibalance.util.RegexUpdater;
import com.facebook.appevents.AppEventsLogger;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.kahuna.sdk.KahunaAnalytics;
import com.parse.ConfigCallback;
import com.parse.ParseConfig;
import com.parse.ParseException;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import de.greenrobot.event.EventBus;

public class MainActivity extends Activity implements ActionBar.TabListener,PreferenceChangeListener {
	final String tag = MainActivity.class.getSimpleName();
	SharedPreferences mSharedPreferences;
	EditText input ;
	EventBus dataIntializerCompleteEvent;
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

		dataIntializerCompleteEvent = EventBus.getDefault();
		dataIntializerCompleteEvent.register(this);
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
       /* appOpenCount = mSharedPreferences.getInt("APP_OPEN_COUNT", 0);
        appOpenCount++;
        Editor mEditor = mSharedPreferences.edit();
        mEditor.putInt("APP_OPEN_COUNT", appOpenCount);
        mEditor.commit();
        if(appOpenCount%10 == 0)
        {
		ParseConfig.getInBackground(new ConfigCallback() {
			  @Override
			  public void done(ParseConfig config, ParseException e) {
				  if (e == null) {
				      //Log.d(tag, "Yay! Config was fetched from the server.");
				    } else {
				      Log.e(tag, "Failed to fetch. Using Cached Config.");
				      config = ParseConfig.getCurrentConfig();
				    }
				  adFrequency = config.getInt("adFrequency");
			    //Log.d(tag, String.format("The ad frequency is %d!", adFrequency));
			  }
			});
        }
        else
        {
        	adFrequency = ParseConfig.getCurrentConfig().getInt("adFrequency");
        }
        //Log.d(tag, "App Open Count = "+ appOpenCount);
        if(adFrequency>0)
        if((appOpenCount%adFrequency)==0)
        {
        	//Log.d(tag, "Showing Add");
        	*//*mInterstitial = new MoPubInterstitial(MainActivity.this, "37f5fbea1a5847d894ad27f15729d20e");
            mInterstitial.setInterstitialAdListener(MainActivity.this);
            mInterstitial.load();*//*
        }
        else
        {
        	*//*if(mInterstitial!=null)
        		{
        		mInterstitial.destroy();
        		}*//*
        }*/
		

	
		
        
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
	        Log.d(tag, "onStart Main Activity");
	        KahunaAnalytics.start(); 
	        Apptentive.onStart(this);
	        FlurryAgent.logEvent("MainScreen", true);
	      //Get an Analytics tracker to report app starts and uncaught exceptions etc.
	        GoogleAnalytics.getInstance(this).reportActivityStart(this);
	        super.onStart();
	        // Your Code Here
	    }

	    @Override
	    protected void onStop() {
	       
	        Log.d(tag, "Stopping Main Activity");
			EventBus.getDefault().unregister(this);
	        KahunaAnalytics.stop();

	        Apptentive.onStop(this);
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

			Apptentive.engage(this, "Rate");
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


public void onEvent(DataLoadingDone mDataLoadingDone)
{

    if(mMainActivityAdapter.getmRechargeFragment()!=null)
    {
        ((RechargeFragment) mMainActivityAdapter
                .getmRechargeFragment()).loadDataAsync(mMainActivityAdapter);
    }

}

	/*private boolean exportData() {
		// DateDuration Data
		CSVWriter writer = new CSVWriter();
		List<String[]> data = new ArrayList<String[]>();
		// InCount-0 Indur-1 OutCount-2 OutDur-3
		ArrayList<Integer> value = null;
		data.add(new String[] { "Date", "Incoming Count", "Incoming Duration",
				"Outgoing Count", "Outgoing Duration" });
		for (Entry<Date, ArrayList<Integer>> entry : DataInitializer.dateDurationMap
				.entrySet()) {
			value = (ArrayList<Integer>) entry.getValue();
			String[] starr = new String[value.size()+1];
			starr[0] = entry.getKey().toString();
			for(int i=0;i<value.size();i++)
			{
				try{
				starr[i+1] = value.get(i).toString();
				}
				catch(Exception e)
				{
					starr[i+1]="null";
				}
			}
			data.add(starr);
			data.add(new String[] { entry.getKey().toString(),
					value.get(0).toString(), value.get(1).toString(),
					value.get(2).toString(), value.get(3).toString() });
		}
		
		String datedur = writer.writeAll(data);
		byte[] dateDuration = datedur.getBytes();
		//Log.d("AllData", datedur);

		// Main Data
		// name,InCount,InDur,OutCount,OutDur,MissCount,Provider,State
		data.clear();
		writer = new CSVWriter();
		data.add(new String[] { "Phone Number", "Name", "Incoming Count",
				"Incoming Duration", "Outgoing Count", "Outgoing Duration",
				"Missed Count", "Provider", "State","image_uri" });
		Object[] values = null;
		for (Entry<String, Object[]> entry : DataInitializer.mainmap.entrySet()) {
			values = (Object[]) entry.getValue();
			if (values[0] == null)
				values[0] = entry.getKey().toString();
			String[] st = new String[values.length+1];
			st[0] = (entry.getKey().toString());
			for(int i=0;i<values.length;i++)
			{
				try{
				st[i+1]=(values[i].toString());
				}
				catch(Exception e)
				{
					st[i+1]="null";
				}
			}
			data.add(st);
			data.add(new String[] { ,
					(String) values[0], values[1].toString(),
					values[2].toString(), values[3].toString(),
					values[4].toString(), values[5].toString(),
					values[6].toString(), values[7].toString() });
		}
		byte[] mainData = writer.writeAll(data).getBytes();

		// Export USSD Data
		// 1-Date 2-Call Cost 3-Balance 4-CallDuration 5-LastNumber
		data.clear();
		writer = new CSVWriter();
		data.add(new String[] { "Date", "CallCost", "Balance", "CallDuration",
				"LastNumber" });
		for (NormalCall entry : DataInitializer.ussdDataList) {
			data.add(new String[] { entry.date.toString(),
					entry.callCost.toString(), entry.bal.toString(),
					entry.callDuration + " ", entry.lastNumber });
		}
		byte[] ussdData = writer.writeAll(data).getBytes();
		DataInitializer.SmsLogs smsData;
		data.clear();
		writer = new CSVWriter();//Date date,String provider,String state,int count
		data.add(new String[] { "Number","Date", "Provider", "State", "Count" });
		try{
		for (Entry<String, DataInitializer.SmsLogs> entry : DataInitializer.smsMap.entrySet()) {

			String key = entry.getKey();
			smsData = DataInitializer.smsMap.get(key);
			data.add(new String[]{key,smsData.date.toString(),smsData.provider,smsData.state,smsData.count+""});
			
			
		}
		}
		catch(Exception e)
		{
			Log.e(tag, "ERROR in Writing SMS data");
			e.printStackTrace();
		}
		byte[] smsDataBytes = writer.writeAll(data).getBytes();
		sendToParse(dateDuration, "dateDuration.csv",
					mainData, "MainData.csv",
					ussdData, "USSD_Data.csv",
					smsDataBytes,"SMS_Data.csv");
		//Log.d(tag, "Done writing");
		
		
		// say thank you
		// Create the dialog box

		
		// List<String> list = new ArrayList<String>();
		// list.add(base+"dateDuration.csv");
		// list.add(base+"MainData.csv");
		// list.add(base+"USSD_Data.csv");
		// email(this,"ahmedshabaz1@gmail.com","","PowerPack Data","PFA",list);

		return true;
	}
	private void sendToParse(byte[] dateDuration, 
			 String fileName1,
			 byte[] mainData,
			 String fileName2, 
			 byte[] ussdData,
			 String fileName3,
			 byte[] smsData,
			 String fileName4) 
{
		ParseFile pfile1, pfile2, pfile3, pfile4;

		pfile1 = new ParseFile(fileName1, dateDuration);
		pfile1.saveInBackground();

		pfile2 = new ParseFile(fileName2, mainData);
		pfile2.saveInBackground();

		pfile3 = new ParseFile(fileName3, ussdData);
		pfile3.saveInBackground();

		pfile4 = new ParseFile(fileName4, smsData);
		pfile4.saveInBackground();

		ParseObject usageData = new ParseObject("UsageData");

		usageData.put("DeviceID",
				mSharedPreferences.getString("DEVICE_ID", "123456"));


		usageData.put("Provider",
				mSharedPreferences.getString("CARRIER", "Unknown"));

		usageData.put("CIRCLE", mSharedPreferences.getString("State", "Unknown"));

		usageData.put("DateDuration", pfile1);
		usageData.put("MainData", pfile2);
		usageData.put("USSD_Data", pfile3);
		usageData.put("SMS_Data", pfile4);

		usageData.saveEventually();

	}
	*/
	@Override
	protected void onDestroy() {
		super.onDestroy();
        EventBus.getDefault().unregister(this);
		DataInitializer.mainActivityRunning = false;
		/*if(mInterstitial!=null)
		mInterstitial.destroy();*/
	}


	

	@Override
	public void preferenceChange(PreferenceChangeEvent pce) {
		//Log.d(tag, "Preference Change Detected");
		if(mMainActivityAdapter.getmRechargeFragment()!=null)
		{
			((RechargeFragment) mMainActivityAdapter
					.getmRechargeFragment()).loadDataAsync(mMainActivityAdapter);
		}
	}




}
