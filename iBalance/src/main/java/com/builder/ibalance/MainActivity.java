package com.builder.ibalance;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.appsflyer.AppsFlyerLib;
import com.apptentive.android.sdk.Apptentive;
import com.builder.ibalance.adapters.MainActivityAdapter;
import com.builder.ibalance.datainitializers.DataInitializer;
import com.builder.ibalance.datainitializers.FilteredDataInitializer;
import com.builder.ibalance.util.DataLoader;
import com.builder.ibalance.util.MyApplication;
import com.builder.ibalance.util.MyApplication.TrackerName;
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

public class MainActivity extends Activity implements ActionBar.TabListener,
		DataLoader,DialogInterface.OnClickListener,PreferenceChangeListener {
	final String tag = MainActivity.class.getSimpleName();
	SharedPreferences mSharedPreferences;
	EditText input ;
	//MoPubInterstitial mInterstitial;
	Tracker t = ((MyApplication)MyApplication.context).getTracker(
		    TrackerName.APP_TRACKER);

	public static int appOpenCount = 0;
	public static int adFrequency = -1;

	 MainActivityAdapter mMainActivityAdapter;
	public static MenuItem dateSelector = null;
	ViewPager mViewPager;
	float current_balance,minimum_balance;
	DataInitializer mDataInitializer;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		
		//SubscriptionManager mSubscriptionManager = SubscriptionManager.from(this);
		////Log.d("DUAL_SIM","Sunscription Count = " + mSubscriptionManager.getActiveSubscriptionInfoCountMax());
		mDataInitializer = new DataInitializer();
		mDataInitializer.execute(this);
		mSharedPreferences = getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
		current_balance = mSharedPreferences.getFloat("CURRENT_BALANCE", (float)-1.0);
		minimum_balance = mSharedPreferences.getFloat("MINIMUM_BALANCE", (float)10.0);
//		RechargeHelper mRechargeHelper = new RechargeHelper(MyApplication.context);
//		mRechargeHelper.adDummyData();
		/*mInterstitial = new MoPubInterstitial(MainActivity.this, "37f5fbea1a5847d894ad27f15729d20e");
        mInterstitial.setInterstitialAdListener(MainActivity.this);
        mInterstitial.load();*/
        appOpenCount = mSharedPreferences.getInt("APP_OPEN_COUNT", 0);
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
        	/*mInterstitial = new MoPubInterstitial(MainActivity.this, "37f5fbea1a5847d894ad27f15729d20e");
            mInterstitial.setInterstitialAdListener(MainActivity.this);
            mInterstitial.load();*/
        }
        else
        {
        	/*if(mInterstitial!=null)
        		{
        		mInterstitial.destroy();
        		}*/
        }
		
		if(current_balance>0.0)
		{
		if(current_balance<minimum_balance)
		{
			createReminderDialog(this);
		}
		}
	
		
        
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);/*
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setCustomView(R.layout.custom_actionbar_title); */
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mMainActivityAdapter = new MainActivityAdapter(getFragmentManager(),
				this);

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



		Intent popup_intent = popup_intent = new Intent(getApplicationContext(),
				UssdPopup.class);
		popup_intent.putExtra("TYPE", 1);
		popup_intent.putExtra("BALANCE",
				"121.23");
		popup_intent.putExtra("CALL_COST",
				String.format("%.2f", 2.15));
		popup_intent.putExtra("CALL_DURATION",
				"121" + "");
		popup_intent.putExtra("NUMBER",
				"121" + "");
		popup_intent
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		popup_intent
				.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		popup_intent
				.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		startActivity(popup_intent);
		
	}

	private void createReminderDialog(Context context) {
		AlertDialog.Builder alertbox = new AlertDialog.Builder(context);
		TextView myMsg = new TextView(this);
		myMsg.setText("\nLow Balance Alert\n\n Your Current Balance is Rs." + current_balance
				+ "\n   Please Recharge to stay connected!\n");
		myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
		alertbox.setView(myMsg);
		Tracker t = ((MyApplication) getApplication()).getTracker(
			    TrackerName.APP_TRACKER);
		t.send(new HitBuilders.EventBuilder().setCategory("LOW_BALANCE")
				.setAction(current_balance+"").setLabel("").build());

		Apptentive.engage(this, "LOW_BALANCE");
		FlurryAgent.logEvent("LOW_BALANCE");
		AppsFlyerLib.sendTrackingWithEvent(MyApplication.context,
				"LOW_BALANCE", "current_balance");
		// Set the message to display

		// Set a positive/yes button and create a listener
		alertbox.setNeutralButton("Okay", null);
/*		alertbox.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {

					// Click listener

					public void onClick(DialogInterface arg0, int arg1) {

						 Toast.makeText(getApplicationContext(),
						 "Recharge Feature is Coming Soon", Toast.LENGTH_LONG).show();
						Intent i = new Intent(getApplicationContext(),
								Recharge.class);
						startActivity(i);
					}

				});

		// Set a negative/no button and create a listener

		alertbox.setNegativeButton("No", new DialogInterface.OnClickListener() {

			// Click listener

			public void onClick(DialogInterface arg0, int arg1) {

				// Toast.makeText(getApplicationContext(),
				// "'No' button clicked", Toast.LENGTH_SHORT).show();

			}

		});*/

		// display box

		alertbox.show();
		
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
		 	FilteredDataInitializer.mainActivityRunning = true;
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
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Minimum Balance");
		alert.setMessage("Enter the value");

		// Set an EditText view to get user input
		input = new EditText(this);
		Float previousSetBal = mSharedPreferences.getFloat("MINIMUM_BALANCE", (float) 10.0);
		input.append(previousSetBal+"");
		input.setFocusable(true);
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
		input.setMinimumWidth(5);
		
		input.setSelection(input.getText().length(),input.getText().length());
		input.setGravity(Gravity.CENTER_HORIZONTAL);
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		alert.setView(input);
		alert.setPositiveButton("Ok", this); 
		alert.show();
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

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

	@Override
	public void dataLoaded() {
		//Log.d(tag, "Data loaded!!!!!!!!!!!!!!!!!!!");
		//exportData();
		if(mMainActivityAdapter.getmBalanceFragment()!=null)
		{
			((BalanceFragment)mMainActivityAdapter.getmBalanceFragment()).setPredictedDays();
			mMainActivityAdapter.notifyDataSetChanged();
		}
		if(mMainActivityAdapter.getmCallPatternFragment()!=null)
		{
			
			((CallPatternFragment) mMainActivityAdapter
					.getmCallPatternFragment()).loadDataAsync(mMainActivityAdapter);
		}
		if(mMainActivityAdapter.getmContFragment()!=null)
		{
			
			((ContactsFragment) mMainActivityAdapter
					.getmContFragment()).loadDataAsync(mMainActivityAdapter);
		}
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
		DataInitializer.mainActivityRunning = false;
		FilteredDataInitializer.mainActivityRunning = false;
		/*if(mInterstitial!=null)
		mInterstitial.destroy();*/
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		//Log.d("CHART", "Clicked okay");
		Float setBal;
		try{
			
		setBal = Float.parseFloat(input.getText().toString());
		}
		catch(Exception e)
		{
			setBal = mSharedPreferences.getFloat("MINIMUM_BALANCE", (float) 10.0);
		}
		//Log.d("CHART", setBal + " ");
		Editor editor = mSharedPreferences.edit();
		editor.putFloat("MINIMUM_BALANCE", (float) setBal);
		editor.commit();
		if(mMainActivityAdapter.getmBalanceFragment()!=null && ((BalanceFragment)mMainActivityAdapter.getmBalanceFragment()).mLineChart!=null)
		{
			//Log.d("CHART", "Balance fragment not null" + " ");
			((BalanceFragment)mMainActivityAdapter.getmBalanceFragment()).setLimitLine();
			((BalanceFragment)mMainActivityAdapter.getmBalanceFragment()).mLineChart.notifyDataSetChanged();
			((BalanceFragment)mMainActivityAdapter.getmBalanceFragment()).mLineChart.invalidate();
			//mMainActivityAdapter.notifyDataSetChanged();
		}
		
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

/*	@Override
	public void onInterstitialClicked(MoPubInterstitial arg0) {

		  t.send(new HitBuilders.EventBuilder().setCategory("AD")
  				.setAction("Clicked").setLabel("").build());

  		Apptentive.engage(this, "AD_Clicked");
  		FlurryAgent.logEvent("AD_Clicked");
  		AppsFlyerLib.sendTrackingWithEvent(MyApplication.context, "AD_Clicked",
  				"");
	}

	@Override
	public void onInterstitialDismissed(MoPubInterstitial arg0) {
		 t.send(new HitBuilders.EventBuilder().setCategory("AD")
	  				.setAction("Dismissed").setLabel("").build());

	  		Apptentive.engage(this, "AD_Dismissed");
	  		FlurryAgent.logEvent("AD_Dismissed");
	  		AppsFlyerLib.sendTrackingWithEvent(MyApplication.context, "AD_Dismissed",
	  				"");
	}

	@Override
	public void onInterstitialFailed(MoPubInterstitial arg0, MoPubErrorCode arg1) {
		t.send(new HitBuilders.EventBuilder().setCategory("AD")
				.setAction("Failed").setLabel("").build());

		Apptentive.engage(this, "AD_Failed");
		FlurryAgent.logEvent("AD_Failed");
		AppsFlyerLib.sendTrackingWithEvent(MyApplication.context, "AD_Failed",
				"");
	}

	@Override
	public void onInterstitialLoaded(MoPubInterstitial arg0) {
		 if (arg0.isReady()) {
			 //Log.d(tag, "ad Listener call back");
			 if(adFrequency>-1 && appOpenCount>10 && appOpenCount%adFrequency==0 )
	            mInterstitial.show();
	          
	        } else {
	            // Other code
	        }
		
	}

	@Override
	public void onInterstitialShown(MoPubInterstitial arg0) {
		Tracker t = ((MyApplication) MyApplication.context).getTracker(
			    TrackerName.APP_TRACKER);
		t.send(new HitBuilders.EventBuilder()
	    .setCategory("ADS")
	    .setAction("SHOWN")
	    .setLabel("Interstitial")
	    .build());
		Apptentive.engage(this, "AD_SHOWN");
		FlurryAgent.logEvent("AD_SHOWN");
		AppsFlyerLib.sendTrackingWithEvent(MyApplication.context, "AD_SHOWN",
				"");
		
	}*/



}
