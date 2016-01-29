package com.builder.ibalance.util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.appsflyer.AppsFlyerLib;
import com.builder.ibalance.BuildConfig;
import com.builder.ibalance.R;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.digits.sdk.android.Digits;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.kahuna.sdk.EmptyCredentialsException;
import com.kahuna.sdk.IKahunaUserCredentials;
import com.kahuna.sdk.Kahuna;
import com.kahuna.sdk.KahunaUserCredentials;
import com.parse.Parse;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import io.fabric.sdk.android.Fabric;

public class MyApplication extends Application
{

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "dkuXLOEQguuxaRAXxrIbs0eg1";
    private static final String TWITTER_SECRET = "3ikMAf7ig4faJ9ZNAoBiv5BUHtBJUgI7sY2Zeg6Av7JXg1kI7c";

	public static Context context;
	final String tag = MyApplication.class.getSimpleName();
	private static final String PROPERTY_ID = "UA-62225498-2";


    @Override
    public void onCreate() {

        super.onCreate();
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();
		TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
		Fabric.with(this, crashlyticsKit, new TwitterCore(authConfig), new Digits(), new Crashlytics());
        context = this;
        TelephonyManager mtelTelephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
        //Log.d(tag, "Initializing Parse");
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "UDKaBRgXs4lSZoTyUbUFtEDcCoFjnVHYkt8M7xvW", "7Sf6euqZUq30EqN9YDjOgwZTD4uSYm9En7pqH7Ax");
        AppsFlyerLib.setAppsFlyerKey("M3yd5JZJrEjPSSvFggiwME");
        //for US
        if(ConstantsAndStatics.DEMO)
        {
            SharedPreferences mSharedPreferences = getSharedPreferences("US", Context.MODE_PRIVATE);
            if (mSharedPreferences.getBoolean("YET_TO_COPY", true))
            {
                copyAssets();
                mSharedPreferences.edit().putBoolean("YET_TO_COPY", false).commit();
            }
        }
        String deviceId =mtelTelephonyManager.getDeviceId(); 
        //String number =mtelTelephonyManager.getLine1Number() ;

        Crashlytics.getInstance().core.setUserIdentifier(deviceId);
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
        FlurryAgent.setCaptureUncaughtExceptions(false);
        FlurryAgent.setLogEnabled(false);
        if(!BuildConfig.DEBUG)
        {
            //Release Key
            FlurryAgent.init(this, "7R65ZKFNW9CPSNGS4XNK");
        }
        else
        {
            //DEBUG KEY
            FlurryAgent.init(this, "HRZ63D96THTDTT4G9TVM");
        }
    }
	private void copyAssets() {
		AssetManager assetManager = getAssets();
		String[] files = null;
		try {
			files = assetManager.list("");
		} catch (IOException e) {
			Log.e("tag", "Failed to get asset file list.", e);
		}
		if (files != null) for (String filename : files) {
            if(filename.equals("DEVICE_DETAILS.xml") || filename.equals("GOOGLE_PREFS.xml") || filename.equals("USER_DATA.xml"))
            {
              //V17Log.d(tag,"Writing  = "+filename);
                InputStream in = null;
                OutputStream out = null;
                try
                {

                    in = assetManager.open(filename);
                    File sharedPrefFolder = new File(getFilesDir(), "../shared_prefs");
                    String shared_pref_path = sharedPrefFolder.getPath();
                   //V17Log.d(tag,"shared_pref_path = "+shared_pref_path);
                        File outFile = new File( shared_pref_path, filename);
                    out = new FileOutputStream(outFile);
                    copyFile(in, out);
                } catch (IOException e)
                {
                    Log.e("tag", "Failed to copy asset file: " + filename, e);
                    e.printStackTrace();
                } finally
                {
                    if (in != null)
                    {
                        try
                        {
                            in.close();
                        } catch (IOException e)
                        {
                            // NOOP
                        }
                    }
                    if (out != null)
                    {
                        try
                        {
                            out.close();
                        } catch (IOException e)
                        {
                            // NOOP
                        }
                    }
                }
            }
		}
	}
	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}
	}
	/*@Override
	public void attachBaseContext(Context base) {
		MultiDex.install(base);
		super.attachBaseContext(base);
	}*/
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
}

