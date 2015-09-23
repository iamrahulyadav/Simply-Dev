package com.builder.ibalance;


import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;

import com.appsflyer.AppsFlyerLib;
import com.apptentive.android.sdk.Apptentive;
import com.builder.ibalance.datainitializers.DataInitializer;
import com.builder.ibalance.util.ConstantsAndStatics;
import com.builder.ibalance.util.MyApplication;
import com.builder.ibalance.util.MyApplication.TrackerName;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 *
 */
public class SplashscreenActivity extends Activity {
	
	final String TAG = SplashscreenActivity.class.getSimpleName();
	final int SPLASH_TIME_OUT = 1000;
	String accessibiltyID = "com.builder.ibalance/.services.RecorderUpdaterService";//to check if service is on
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
	  super.onWindowFocusChanged(hasFocus);
	  if (hasFocus) {
	    // Returns true if the push notification was for Apptentive, and we handled it.
	    boolean ranApptentive = Apptentive.handleOpenedPushNotification(this);
	  }
	}

	@Override
	protected void onStart() {

		//KahunaAnalytics.start();
		super.onStart();
		// Your Code Here
	}



		@Override
	    protected void onStop() {

	        //KahunaAnalytics.stop();
	        super.onStop();
	        // Your Code Here
	    } 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
	            WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_splashscreen);
		String app_open_from = this.getIntent().getStringExtra("FROM");
		try{
			if((app_open_from!=null) && app_open_from.equals("WIDGET"))
			{
				Tracker t = ((MyApplication) this.getApplication()).getTracker(
					    TrackerName.APP_TRACKER);
				t.send(new HitBuilders.EventBuilder()
			    .setCategory("APP_OPEN")
			    .setAction("WIDGET")
			    .setLabel("WIDGET")
			    .build());
				Apptentive.engage(this, "WIDGET_APP_OPEN");
				FlurryAgent.logEvent("WIDGET_APP_OPEN");
				AppsFlyerLib.sendTrackingWithEvent(getApplicationContext(),"WIDGET_APP_OPEN","");
			}
			}
		catch (Exception e) {
			//Log.d(TAG, "Failed to  get the intent");
			e.printStackTrace();
		}
		//Log.d(TAG, "Splash Screen in");

        AppsFlyerLib.sendTracking(getApplicationContext());
		Typeface tf =Typeface.createFromAsset(getResources().getAssets(),"Roboto-Regular.ttf"); 
		TextView tv = (TextView)findViewById(R.id.fullscreen_content);
		tv.setTypeface(tf);
		final SharedPreferences mSharedPreferences = getSharedPreferences("USER_DATA",Context.MODE_PRIVATE);

		DataInitializer.initializeUSSDData(getApplicationContext());
			
		
		 new Handler().postDelayed(new Runnable() {
			 
	            /*
	             * Showing splash screen with a timer. This will be useful when you
	             * want to show case your app logo / company
	             */
	 
	            @Override
	            public void run() {
	                // This method will be executed once the timer is over
	                // Start your app main activity
	            	////Log.d("SPalsh",""+mSharedPreferences.getBoolean("WIZARD", false));
				if (!mSharedPreferences.getBoolean("TUTORIAL", false)) {
					startActivityForResult(new Intent(getApplicationContext(),
							Tutorial.class), 0);
					finish();
				} else if (!mSharedPreferences.getBoolean("WIZARD", false)) {
					startActivityForResult(new Intent(getApplicationContext(),
							Wizard.class), 0);
					finish();
				}
	          		else
	          		{

	          			
	          			Boolean isEnabledAccess = isAccessibilityEnabled( accessibiltyID);
	          			if (!isEnabledAccess) {
	          				//Log.d(TAG, "Accesibilty  Not Enabled");
	          				ConstantsAndStatics.WAITING_FOR_SERVICE = true;
	          				startActivity(new Intent(getApplicationContext(),ServiceEnableActivity.class));
	          				finish();
//	          				DataInitializer.initializeUSSDData(getApplicationContext());
//	          	 			//mBalanceHelper.addDemoentries();
//	          	 			startActivity(new Intent(getApplicationContext(),MainActivity.class));
//	          	 			finish();
	          			}
	          			
	          			else{
	          				//Log.d(TAG, "Accesibilty  Enabled!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	         			//mBalanceHelper.addDemoentries();
	         			startActivity(new Intent(getApplicationContext(),MainActivity.class));
	         			finish();
	          			}
	          			
	          		}
	            }
	        }, SPLASH_TIME_OUT);
		 
		
         // close this activity
       //  finish();
		 
		

	}
	private Boolean isAccessibilityEnabled(String id) {
		AccessibilityManager mAccessibilityManager = (AccessibilityManager) this.getSystemService(Context.ACCESSIBILITY_SERVICE);
		//Log.d(TAG,"Checking for: "+id);
		List<AccessibilityServiceInfo> runningServices = mAccessibilityManager.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
		//Log.d(TAG,"size of ruuning services : "+runningServices.size());
		for (AccessibilityServiceInfo service : runningServices) {
			//Log.d(TAG, service.getId());
			if (id.equals(service.getId())) {
				return true;
			}
		}

		return false;
	}
	




}
