package com.builder.ibalance;


import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appsflyer.AppsFlyerLib;
import com.apptentive.android.sdk.Apptentive;
import com.builder.ibalance.core.DualSim;
import com.builder.ibalance.core.SimModel;
import com.builder.ibalance.datainitializers.DataInitializer;
import com.builder.ibalance.util.Constants;
import com.builder.ibalance.util.ConstantsAndStatics;
import com.builder.ibalance.util.GlobalData;
import com.builder.ibalance.util.Helper;
import com.builder.ibalance.util.MyApplication;
import com.builder.ibalance.util.MyApplication.TrackerName;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.ParseObject;

import java.util.ArrayList;
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
    Helper.SharedPreferenceHelper mSharedPreferenceHelper = new Helper.SharedPreferenceHelper();
    ProgressBar dual_sim_bar;
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
        dual_sim_bar = (ProgressBar) findViewById(R.id.sim_check_progress);
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
		final SharedPreferences mSharedPreferences = getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);

        new SimChecker().execute();


	}
    class SimChecker extends AsyncTask<Void, Void, ArrayList<SimModel>>
    {
        SharedPreferences mSharedPreferences = MyApplication.context.getSharedPreferences("DEVICE_DETAILS", Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();

        @Override
        protected ArrayList<SimModel> doInBackground(Void... voids)
        {

            SimModel.dual_type = mSharedPreferences.getInt("DUAL_SIM_TYPE", 0);
            DualSim mDualSimObject = new DualSim();
            //Pass previously known Type, this function will not return null
            GlobalData.globalSimList = mDualSimObject.getSimList(SimModel.dual_type );
            //If no sim card found there show error screen
            if(GlobalData.globalSimList ==null)
            {
                return null;
            }
            if(SimModel.two_slots ||true)
            {
                Constants.HAS_TWO_SLOTS = true;
                if (SimModel.call_log_columns.isEmpty())
                {
                    SimModel.call_log_columns = mDualSimObject.getDualCallLogColumn();
                    Log.d(TAG,"call_log_columns :"+SimModel.call_log_columns.toString());
                    if (SimModel.call_log_columns.isEmpty())
                    {
                        //TODO Fall back to logical check
                    }
                }
                if(GlobalData.globalSimList.size()>=2)
                {
                    Constants.IS_SINGLE_SIM = false;

                }
                else
                {
                    Constants.IS_SINGLE_SIM = true;
                }

            }
            else
            {
                Constants.HAS_TWO_SLOTS = false;
                Constants.IS_SINGLE_SIM = true;
            }

            mSharedPreferenceHelper.saveDualSimDetails(GlobalData.globalSimList);
            return GlobalData.globalSimList;

        }


        @Override
        protected void onPostExecute(ArrayList<SimModel> sim_list)
        {

            super.onPostExecute(sim_list);
            if(sim_list == null)
            {
                startActivity(new Intent(SplashscreenActivity.this,NoSimActivity.class));
                finish();
            }
            else
            {
                DataInitializer mDataInitializer = new DataInitializer();
                mDataInitializer.execute();
                dual_sim_bar.setVisibility(View.GONE);
                if (sim_list != null)
                {
                    mEditor.putInt("TYPE", SimModel.dual_type).commit();
                    Log.d(TAG + " Sim Info =", sim_list.toString());
                } else
                {
                    Log.d(TAG + " Sim Info =", "Null");
                }
                boolean first_app_launch = mSharedPreferences.getBoolean("FIRST_APP_LAUNCH",true);
                if(first_app_launch)
                {
                   TelephonyManager manager = (TelephonyManager) (MyApplication.context.getSystemService(TELEPHONY_SERVICE));
                    ParseObject pObj = new ParseObject("SIMPLY_USERS");
                    pObj.put("DEVICE_ID",
                            manager.getDeviceId());
                    for(SimModel m:sim_list)
                    {
                        pObj.put("CARRIER_"+m.getSimslot(), m.getCarrier());
                        pObj.put("CIRCLE_"+m.getSimslot(),m.getCircle());
                    }
                    String phNumber = manager.getLine1Number();
                    if(phNumber!=null)
                    {
                        pObj.put("NUMBER",phNumber);
                    }
                    pObj.put("MODEL",android.os.Build.MODEL);
                    pObj.put("MANUFACTURER",android.os.Build.MANUFACTURER);
                    pObj.put("ANDROID_VERSION", Build.VERSION.SDK_INT);
                    pObj.put("TWO_SLOTS",SimModel.isTwo_slots());
                    pObj.put("DUAL_SIM", !Constants.IS_SINGLE_SIM);
                    pObj.put("CALLLOG_COLUMNS",SimModel.call_log_columns.toString());
                    pObj.put("SAMPLE_CALLLOGS", getCallLogs());
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    pObj.put("LOCATION",loc.toString());
                    pObj.put("APP_VERSION", BuildConfig.VERSION_CODE);
                    SharedPreferences.Editor editor =mSharedPreferences.edit();
                    editor.putInt("APP_VERSION", BuildConfig.VERSION_CODE);
                    editor.putBoolean("FIRST_APP_LAUNCH",false);
                    pObj.put("DUAL_SIM_LOGS",SimModel.debugInfo);
                    pObj.saveEventually();
                }
                Boolean isEnabledAccess = isAccessibilityEnabled(accessibiltyID);
                if (!isEnabledAccess)
                {
                    //Log.d(TAG, "Accesibilty  Not Enabled");
                    ConstantsAndStatics.WAITING_FOR_SERVICE = true;
                    startActivity(new Intent(getApplicationContext(), ServiceEnableActivity.class));
                    finish();
//	          				DataInitializer.initializeUSSDData(getApplicationContext());
//	          	 			//mBalanceHelper.addDemoentries();
//	          	 			startActivity(new Intent(getApplicationContext(),MainActivity.class));
//	          	 			finish();
                } else
                {
                    //Log.d(TAG, "Accesibilty  Enabled!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    //mBalanceHelper.addDemoentries();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
            }
        }

    }

    private String getCallLogs()
    {
        StringBuilder details = new StringBuilder();
        Cursor managedCursor = this.getContentResolver().query(CallLog.Calls.CONTENT_URI,null,null,null, CallLog.Calls.DATE + " DESC");
        details.append("CallLogInfo DB\n");
        int c=managedCursor.getColumnCount(),i=0;
        for( i=0;i<2;i++)
        {
            if(managedCursor.moveToNext())
            {
                for( i=0;i<c;i++)
                {
                    details.append(managedCursor.getColumnName(i)+ ":");
                    details.append(managedCursor.getString(i));
                    details.append('\n');
                }
            }

        }
        return details.toString();
    }

    private Boolean isAccessibilityEnabled(String id) {
		AccessibilityManager mAccessibilityManager = (AccessibilityManager) this.getSystemService(Context.ACCESSIBILITY_SERVICE);
		//Log.d(TAG,"Checking for: "+id);
		List<AccessibilityServiceInfo> runningServices = mAccessibilityManager.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
		Log.d(TAG, "size of ruuning services : " + runningServices.size());
		for (AccessibilityServiceInfo service : runningServices) {
			Log.d(TAG, service.getId());
			if (id.equals(service.getId())) {
				return true;
			}
		}

		return false;
	}
	




}
