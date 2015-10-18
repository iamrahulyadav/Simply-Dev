package com.builder.ibalance.util;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.apptentive.android.sdk.Apptentive;
import com.builder.ibalance.R;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.kahuna.sdk.KahunaAnalytics;
import com.kahuna.sdk.KahunaUserCredentialKeys;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.SaveCallback;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.util.HashMap;

public class MyApplication extends MultiDexApplication
{
	public static Context context;
	final String tag = MyApplication.class.getSimpleName();
	private static final String PROPERTY_ID = "UA-62225498-2";

    static RefWatcher refWatcher;
	public static RefWatcher getRefWatcher() {
		return  refWatcher;
	}

    @Override
    public void onCreate() {
    	
        super.onCreate();
		//Fabric.with(this, new Crashlytics());
		context = this;
		refWatcher = LeakCanary.install(this);
        TelephonyManager mtelTelephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
        //Log.d(tag, "Initializing Parse");
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "UDKaBRgXs4lSZoTyUbUFtEDcCoFjnVHYkt8M7xvW", "7Sf6euqZUq30EqN9YDjOgwZTD4uSYm9En7pqH7Ax");
        //AppsFlyerLib.setAppsFlyerKey("M3yd5JZJrEjPSSvFggiwME");


        String deviceId =mtelTelephonyManager.getDeviceId(); 
        //String number =mtelTelephonyManager.getLine1Number() ; 
        KahunaAnalytics.onAppCreate(this, "3e512234b35542f4b42f7cc05f4c047a", "88008162879"); 
        KahunaAnalytics.setUserCredential(KahunaUserCredentialKeys.USER_ID_KEY, 
        		deviceId);
        //Log.d(tag, "Parse Initialization DONE!!!");
        //Configure GA
     // configure Flurry
        FlurryAgent.setLogEnabled(false);
        FlurryAgent.init(this, "7R65ZKFNW9CPSNGS4XNK");
       //AppsFlyerLib.setCurrencyCode("INR");
        //AppsFlyerLib.setUseHTTPFalback(true);
        //Log.d(tag, "Flurry Configured");
        ParsePush.subscribeInBackground("", new SaveCallback() {
        	  @Override
        	  public void done(ParseException e) {
        	    if (e == null) {
        	          String deviceToken = (String) ParseInstallation.getCurrentInstallation().get("deviceToken");
        	            Apptentive.addParsePushIntegration(getApplicationContext(), deviceToken);
        	      Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
        	    } else {
        	      Log.e("com.parse.push", "failed to subscribe for push", e);
        	    }
        	  }
        	});
    }
	@Override
	public void attachBaseContext(Context base) {
		MultiDex.install(base);
		super.attachBaseContext(base);
	}
    public enum TrackerName {
    	  APP_TRACKER, // Tracker used only in this app.
    	  GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
    	  ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    	}

    	HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();
    	public synchronized Tracker getTracker(TrackerName trackerId) {
    		if (!mTrackers.containsKey(trackerId)) {
    		 
    		GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
    		Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(R.xml.app_tracker)
    		: (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(PROPERTY_ID)
    		: analytics.newTracker(R.xml.ecommerce_tracker);
    		mTrackers.put(trackerId, t);
    		 
    		}
    		return mTrackers.get(trackerId);
    		}
/*    public synchronized Tracker getTracker(TrackerName trackerId) {
    	  if (!mTrackers.containsKey(trackerId)) {

    	    GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
    	    Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID)
    	        : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
    	            : analytics.newTracker(R.xml.ecommerce_tracker);
    	    mTrackers.put(trackerId, t);

    	  }
    	  return mTrackers.get(trackerId);
    	}*/
}

