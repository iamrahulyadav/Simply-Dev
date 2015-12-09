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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.builder.ibalance.core.DualSim;
import com.builder.ibalance.core.SimModel;
import com.builder.ibalance.datainitializers.DataInitializer;
import com.builder.ibalance.util.DualSimConstants;
import com.builder.ibalance.util.ConstantsAndStatics;
import com.builder.ibalance.util.GlobalData;
import com.builder.ibalance.util.Helper;
import com.builder.ibalance.util.MyApplication;
import com.builder.ibalance.util.MyApplication.TrackerName;
import com.builder.ibalance.util.RegexUpdater;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.ConfigCallback;
import com.parse.ParseConfig;
import com.parse.ParseException;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashscreenActivity extends Activity
{

    final String TAG = SplashscreenActivity.class.getSimpleName();
    final int SPLASH_TIME_OUT = 1000;
    String accessibiltyID = "com.builder.ibalance/.services.RecorderUpdaterService";//to check if service is on
    Helper.SharedPreferenceHelper mSharedPreferenceHelper = new Helper.SharedPreferenceHelper();
    ProgressBar dual_sim_bar;
    boolean recharge = false;

    @Override
    protected void onStart()
    {
        //KahunaAnalytics.start();
        super.onStart();
        // Your Code Here
    }


    @Override
    protected void onStop()
    {

        //KahunaAnalytics.stop();
        super.onStop();
        // Your Code Here
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splashscreen);
        dual_sim_bar = (ProgressBar) findViewById(R.id.sim_check_progress);
        String app_open_from = this.getIntent().getStringExtra("FROM");
        try
        {
            if ((app_open_from != null) && app_open_from.equals("WIDGET"))
            {
                Helper.logUserEngageMent("WIDGET");
                Tracker t = ((MyApplication) this.getApplication()).getTracker(
                        TrackerName.APP_TRACKER);
                t.send(new HitBuilders.EventBuilder()
                        .setCategory("APP_OPEN")
                        .setAction("WIDGET")
                        .setLabel("WIDGET")
                        .build());
                FlurryAgent.logEvent("WIDGET_APP_OPEN");
               //V10AppsFlyerLib.sendTrackingWithEvent(getApplicationContext(), "WIDGET_APP_OPEN", "");
            }
            if ((app_open_from != null) && app_open_from.equals("POPUP"))
            {
                if(this.getIntent().getBooleanExtra("RECHARGE",false))
                {
                    recharge = true;
                    Tracker t = ((MyApplication) this.getApplication()).getTracker(
                            TrackerName.APP_TRACKER);
                    t.send(new HitBuilders.EventBuilder()
                            .setCategory("APP_OPEN")
                            .setAction("POPUP")
                            .setLabel("RECHARGE")
                            .build());
                    FlurryAgent.logEvent("POPUP_APP_OPEN_RECHARGE");
                }
                else
                {
                    Tracker t = ((MyApplication) this.getApplication()).getTracker(
                            TrackerName.APP_TRACKER);
                    t.send(new HitBuilders.EventBuilder()
                            .setCategory("APP_OPEN")
                            .setAction("POPUP")
                            .setLabel("POPUP")
                            .build());
                    FlurryAgent.logEvent("POPUP_APP_OPEN");
                }
               //V10AppsFlyerLib.sendTrackingWithEvent(getApplicationContext(), "POPUP_APP_OPEN", "");
            }
            else
            {
                Helper.logUserEngageMent("NORMAL");
            }
        } catch (Exception e)
        {
            //Log.d(TAG, "Failed to  get the intent");
           //V10e.printStackTrace();
        }
        //Log.d(TAG, "Splash Screen in");

       //V10AppsFlyerLib.sendTracking(getApplicationContext());
        Typeface tf = Typeface.createFromAsset(getResources().getAssets(), "Roboto-Regular.ttf");
        TextView tv = (TextView) findViewById(R.id.fullscreen_content);
        tv.setTypeface(tf);
        final SharedPreferences mSharedPreferences = getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);

        int prevVersion = mSharedPreferences.getInt("APP_VERSION", -1);
        if (prevVersion < BuildConfig.VERSION_CODE)
        {
            //For the pilot version users
            if (mSharedPreferences.getBoolean("WIZARD", false))
            {
                prevVersion = 9;
            }
            ParseObject parseObject = new ParseObject("APP_UPDATES");
            TelephonyManager manager = (TelephonyManager) (MyApplication.context.getSystemService(TELEPHONY_SERVICE));
            parseObject.put("DEVICE_ID", manager.getDeviceId());
            parseObject.put("FROM_VERSION", prevVersion);
            parseObject.put("TO_VERSION", BuildConfig.VERSION_CODE);
            parseObject.saveEventually();
        }
        if (mSharedPreferences.getBoolean("OLD_PREFS", true))
        {
            mSharedPreferences.edit().clear().commit();
            mSharedPreferences.edit().putBoolean("OLD_PREFS",false).commit();
        }
        new SimChecker().execute();


    }

    private String getCallLogs()
    {
        StringBuilder details = new StringBuilder();
        Cursor managedCursor = this.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC");
        details.append("CallLogInfo DB\n");
        int c = managedCursor.getColumnCount(), i = 0;
        for (int j = 0; j < 2; j++)
        {
            if (managedCursor.moveToNext())
            {
                for (i = 0; i < c; i++)
                {
                    details.append(managedCursor.getColumnName(i) + ":");
                    details.append(managedCursor.getString(i));
                    details.append('\n');
                }
            }
            details.append("-----------------------------------------------------------------------------------\n");
        }
        return details.toString();
    }

    private Boolean isAccessibilityEnabled(String id)
    {
        AccessibilityManager mAccessibilityManager = (AccessibilityManager) this.getSystemService(Context.ACCESSIBILITY_SERVICE);
        //Log.d(TAG,"Checking for: "+id);
        List<AccessibilityServiceInfo> runningServices = mAccessibilityManager.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
       //V10Log.d(TAG, "size of ruuning services : " + runningServices.size());
        for (AccessibilityServiceInfo service : runningServices)
        {
           //V10Log.d(TAG, service.getId());
            if (id.equals(service.getId()))
            {
                return true;
            }
        }

        return false;
    }

    class SimChecker extends AsyncTask<Void, Void, ArrayList<SimModel>>
    {
        SharedPreferences deviceDetailsPreferences = MyApplication.context.getSharedPreferences("DEVICE_DETAILS", Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = deviceDetailsPreferences.edit();
        int PARSER_VERSION = 1;
        int NEW_PARSER_VERSION = 1;
        @Override
        protected ArrayList<SimModel> doInBackground(Void... voids)
        {

            SimModel.dual_type = deviceDetailsPreferences.getInt("DUAL_SIM_TYPE", 0);
            DualSim mDualSimObject = new DualSim();
            //Pass previously known Type, this function will not return null
            GlobalData.globalSimList = mDualSimObject.getSimList(SimModel.dual_type);
            //If no sim card found there show error screen
            if (GlobalData.globalSimList == null)
            {
                return null;
            }
            if (SimModel.isTwo_slots() )
            {
                DualSimConstants.HAS_TWO_SLOTS = true;
                if (SimModel.call_log_columns.isEmpty())
                {
                    SimModel.call_log_columns = mDualSimObject.getDualCallLogColumn();
                   //V10Log.d(TAG, "call_log_columns :" + SimModel.call_log_columns.toString());
                    if (SimModel.call_log_columns.isEmpty())
                    {
                        //TODO Fall back to logical check
                        ParseObject pObj = new ParseObject("ISSUES");
                        pObj.put("DEVICE_ID", Helper.getDeviceId());
                        pObj.put("TYPE", "DUAL_SIM_NO_CallLog_Columns");
                        pObj.saveEventually();
                    }
                }
                if (GlobalData.globalSimList.size() >= 2)
                {
                    DualSimConstants.IS_SINGLE_SIM = false;

                } else
                {
                    DualSimConstants.IS_SINGLE_SIM = true;
                }

            } else
            {
                DualSimConstants.HAS_TWO_SLOTS = false;
                DualSimConstants.IS_SINGLE_SIM = true;
            }
            //Log.d(TAG,GlobalData.globalSimList.toString());
            mSharedPreferenceHelper.saveDualSimDetails(GlobalData.globalSimList);


            /*
            {
                @Override
                public void done(, ParseException e)
                {
                    if (e == null)
                    {
                        //Log.d(tag, "Yay! Config was fetched from the server.");
                    } else
                    {

                    }

                    //Log.d(tag, String.format("The ad frequency is %d!", adFrequency));
                }
            });*/
            return GlobalData.globalSimList;

        }


        @Override
        protected void onPostExecute(ArrayList<SimModel> sim_list)
        {

            super.onPostExecute(sim_list);
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
                       //V12Log.e(TAG, "Failed to fetch. Using Cached Config.");
                        config = ParseConfig.getCurrentConfig();
                    }
                    if ((config != null))
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
            if (sim_list == null)
            {
                startActivity(new Intent(SplashscreenActivity.this, NoSimActivity.class));
                finish();
            } else
            {
                DataInitializer mDataInitializer = new DataInitializer();
                mDataInitializer.execute();
                dual_sim_bar.setVisibility(View.GONE);
                if (sim_list != null)
                {
                    mEditor.putInt("TYPE", SimModel.dual_type).commit();
                   //V10Log.d(TAG + " Sim Info =", sim_list.toString());
                } else
                {
                   //V10Log.d(TAG + " Sim Info =", "Null");
                }
               //V10Log.d(TAG, "Debug info :" + SimModel.debugInfo);
                SharedPreferences mSharedPreferences = MyApplication.context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
                boolean first_app_launch = mSharedPreferences.getBoolean("FIRST_APP_LAUNCH", true);
                if (first_app_launch)
                {
                    TelephonyManager manager = (TelephonyManager) (MyApplication.context.getSystemService(TELEPHONY_SERVICE));
                    ParseObject pObj = new ParseObject("SIMPLY_USERS");
                    pObj.put("DEVICE_ID", manager.getDeviceId());
                    for (SimModel m : sim_list)
                    {
                        pObj.put("CARRIER_" + m.getSimslot(), m.getCarrier());
                        pObj.put("CIRCLE_" + m.getSimslot(), m.getCircle());
                    }
                    String phNumber = manager.getLine1Number();
                    if (phNumber != null)
                    {
                        pObj.put("NUMBER", phNumber);
                    }
                    pObj.put("MODEL", android.os.Build.MODEL);
                    pObj.put("MANUFACTURER", android.os.Build.MANUFACTURER);
                    pObj.put("ANDROID_VERSION", Build.VERSION.SDK_INT);
                    pObj.put("TWO_SLOTS", SimModel.isTwo_slots());
                    pObj.put("DUAL_SIM", !DualSimConstants.IS_SINGLE_SIM);
                    pObj.put("CALLLOG_COLUMNS", SimModel.call_log_columns.toString());
                    /*pObj.put("SAMPLE_CALLLOGS", getCallLogs());*/
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if(loc!=null)
                    {
                        pObj.put("LOCATION", loc.toString());
                    }
                    pObj.put("APP_VERSION", BuildConfig.VERSION_CODE);
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putInt("APP_VERSION", BuildConfig.VERSION_CODE);
                    editor.putBoolean("FIRST_APP_LAUNCH", false);
                    editor.commit();
                    pObj.put("DUAL_SIM_LOGS", SimModel.debugInfo);
                    pObj.saveEventually();
                }
                //Send SMS Data to the Server But don't slow down Data initializer so start it in onPostExecute of DataInitializer

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
                    startActivity(new Intent(getApplicationContext(), MainActivity.class).putExtra("RECHARGE",recharge));
                    finish();
                }
            }
        }

    }


}
