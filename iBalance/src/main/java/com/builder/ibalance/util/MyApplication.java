package com.builder.ibalance.util;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.telephony.TelephonyManager;

import com.appsflyer.AppsFlyerLib;
import com.builder.ibalance.R;
import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.kahuna.sdk.EmptyCredentialsException;
import com.kahuna.sdk.IKahunaUserCredentials;
import com.kahuna.sdk.KahunaUserCredentials;
import com.kahuna.sdk.Kahuna;
import com.parse.Parse;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.util.HashMap;

import io.fabric.sdk.android.Fabric;

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
		Fabric.with(this, new Crashlytics());
		context = this;
		refWatcher = LeakCanary.install(this);
        TelephonyManager mtelTelephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
        //Log.d(tag, "Initializing Parse");
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "UDKaBRgXs4lSZoTyUbUFtEDcCoFjnVHYkt8M7xvW", "7Sf6euqZUq30EqN9YDjOgwZTD4uSYm9En7pqH7Ax");
        AppsFlyerLib.setAppsFlyerKey("M3yd5JZJrEjPSSvFggiwME");


        String deviceId =mtelTelephonyManager.getDeviceId(); 
        //String number =mtelTelephonyManager.getLine1Number() ;

        Kahuna.getInstance().onAppCreate(this, "3e512234b35542f4b42f7cc05f4c047a", null);
        IKahunaUserCredentials newCreds = Kahuna.getInstance().createUserCredentials();
        newCreds.add(KahunaUserCredentials.USERNAME_KEY, deviceId);
        try
        {
            Kahuna.getInstance().login(newCreds);
        } catch (EmptyCredentialsException e)
        {
            //e.printStackTrace();
        }
        //Log.d(tag, "Parse Initialization DONE!!!");
        //Configure GA
     // configure Flurry
        FlurryAgent.setLogEnabled(false);
        FlurryAgent.init(this, "7R65ZKFNW9CPSNGS4XNK");
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

