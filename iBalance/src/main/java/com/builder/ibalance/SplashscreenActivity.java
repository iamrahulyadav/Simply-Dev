package com.builder.ibalance;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.builder.ibalance.core.DualSim;
import com.builder.ibalance.core.SimModel;
import com.builder.ibalance.datainitializers.DataInitializer;
import com.builder.ibalance.util.DualSimConstants;
import com.builder.ibalance.util.GlobalData;
import com.builder.ibalance.util.Helper;
import com.builder.ibalance.util.MyApplication;
import com.builder.ibalance.util.MyApplication.TrackerName;
import com.builder.ibalance.util.RegexUpdater;
import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashscreenActivity extends AppCompatActivity implements View.OnClickListener {

    final String TAG = SplashscreenActivity.class.getSimpleName();
    final int SPLASH_TIME_OUT = 1000;

    Helper.SharedPreferenceHelper mSharedPreferenceHelper = new Helper.SharedPreferenceHelper();
    ProgressBar dual_sim_bar;
    boolean recharge = false;
    private TextView btnSkipLogin;
    boolean isVerified = false;
    int numberOfSkips = 0;
    private boolean isSkipped = false;



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
       /* this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        setContentView(R.layout.activity_splashscreen);

        Tracker t = ((MyApplication) this.getApplication()).getTracker(
                TrackerName.APP_TRACKER);
        HitBuilders.EventBuilder mEvent = new HitBuilders.EventBuilder().setCategory("APP_OPEN");
        HashMap<String,String> flurryParams = new HashMap<>();
        mEvent.setCustomDimension(1, Calendar.getInstance(TimeZone.getDefault()).getTime().toString());
        dual_sim_bar = (ProgressBar) findViewById(R.id.sim_check_progress);
        String app_open_from = this.getIntent().getStringExtra("FROM");
        try
        {
            if(app_open_from==null)
            {
                mEvent.setAction("DIRECT");
                mEvent.setLabel("DIRECT");
                flurryParams.put("SOURCE","DIRECT_APP_OPEN");
                Helper.logUserEngageMent("NORMAL");
            }
            else
            {
                if (app_open_from.equals("WIDGET"))
                {
                    Helper.logUserEngageMent("WIDGET");
                    mEvent.setAction("WIDGET");
                    mEvent.setLabel("WIDGET");
                    flurryParams.put("SOURCE","WIDGET_APP_OPEN");
                    //V10AppsFlyerLib.sendTrackingWithEvent(getApplicationContext(), "WIDGET_APP_OPEN", "");
                }
                else if (app_open_from.equals("POPUP"))
                {
                    if(this.getIntent().getBooleanExtra("RECHARGE",false))
                    {
                        recharge = true;
                        mEvent.setAction("POPUP")
                                .setLabel("RECHARGE");
                        flurryParams.put("SOURCE","POPUP_APP_OPEN_RECHARGE");
                    }
                    else
                    {

                        mEvent.setAction("POPUP")
                                .setLabel("POPUP");
                        Helper.logUserEngageMent("POPUP_APP_OPEN");
                        flurryParams.put("SOURCE","POPUP_APP_OPEN");
                    }
                }

            }
        }
        catch (Exception e)
        {
            Crashlytics.logException(e);
            //Log.d(TAG, "Failed to  get the intent");
            //V10e.printStackTrace();
        }
        t.send(mEvent.build());
        FlurryAgent.logEvent("APP_OPEN",flurryParams);
        //Log.d(TAG, "Splash Screen in");

       //V10AppsFlyerLib.sendTracking(getApplicationContext());
        Typeface tf = Typeface.createFromAsset(getResources().getAssets(), "Roboto-Regular.ttf");
        ((TextView) findViewById(R.id.fullscreen_content)).setTypeface(tf);
        final SharedPreferences userPreferences = getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
        //loginphone = (RelativeLayout)(findViewById(R.id.loginphone));
        /*findViewById(R.id.loginphone).setOnClickListener(this);
        findViewById(R.id.btnSkipLogin).setOnClickListener(this);
        findViewById(R.id.termsOfAgreement).setOnClickListener(this);
        ((TextView)findViewById(R.id.termsOfAgreement)).setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        ((TextView)findViewById(R.id.btnSkipLogin)).setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);*/

        int prevVersion = userPreferences.getInt("APP_VERSION", -1);
        if (prevVersion < BuildConfig.VERSION_CODE)
        {
            //For the pilot version us
            // ers
            if (userPreferences.getBoolean("WIZARD", false))
            {
                prevVersion = 9;
            }
            ParseObject parseObject = new ParseObject("APP_UPDATES");
            TelephonyManager manager = (TelephonyManager) (MyApplication.context.getSystemService(TELEPHONY_SERVICE));
            parseObject.put("DEVICE_ID", manager.getDeviceId()==null?"EMPTY ID":manager.getDeviceId());
            parseObject.put("FROM_VERSION", prevVersion);
            parseObject.put("TO_VERSION", BuildConfig.VERSION_CODE);
            parseObject.saveEventually();
        }
        if (userPreferences.getBoolean("OLD_PREFS", true))
        {
            userPreferences.edit().clear().commit();
            userPreferences.edit().putBoolean("OLD_PREFS",false).commit();
        }
        new SimChecker().execute();
        //LOGIN Code
        /*isVerified = userDataPrefs.getBoolean("USER_VERIFIED",false);
        numberOfSkips = userDataPrefs.getInt("SKIP_COUNTER", 1);
        if(getIntent().getExtras()!=null && !TextUtils.isEmpty(getIntent().getExtras().getString("isSkipped"))){
            isSkipped = false;
        }
        findViewById(R.id.sim_check_progress).setVisibility(View.VISIBLE);
        findViewById(R.id.sim_check_progress).setVisibility(View.INVISIBLE);
        if(isVerified || numberOfSkips>3){
            findViewById(R.id.loginphone).setVisibility(View.INVISIBLE);
            findViewById(R.id.btnSkipLogin).setVisibility(View.INVISIBLE);
            findViewById(R.id.check_box_and_text).setVisibility(View.INVISIBLE);
            findViewById(R.id.sim_check_progress).setVisibility(View.VISIBLE);
            isSkipped = true;
            new SimChecker().execute();

        }*/

    }

    private String getCallLogs()
    {
        StringBuilder details = new StringBuilder();
        Cursor managedCursor = this.getContentResolver()
                .query(CallLog.Calls.CONTENT_URI,
                        null,
                        CallLog.Calls._ID + ">?",
                        new String[]{String.valueOf(Long.MAX_VALUE)},
                        null);
        details.append("Just DB Column Names No DATA ;-)\n");
        int c = managedCursor.getColumnCount();
        for (int i = 0; i < c; i++)
        {
            details.append(managedCursor.getColumnName(i) );
            details.append('\n');
        }
        details.append("-----------------------------------------------------------------------------------\n");
        return details.toString();
    }



    @Override
    public void onClick(View v) {

        /*if(R.id.loginphone == v.getId()){
            //Log.e("Login","phone");
            //isSkipped = true;
            if(!((CheckBox) findViewById(R.id.chkTerms)).isChecked()){
                Toast.makeText(this,
                        "You need to agree Terms of Agreement.", Toast.LENGTH_LONG).show();
            }else{
                new SimChecker().execute();
            }

        }else if(R.id.btnSkipLogin ==v.getId()){
            SharedPreferences userDataPrefs = MyApplication.context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
            numberOfSkips = userDataPrefs.getInt("SKIP_COUNTER" ,0);
            numberOfSkips = numberOfSkips+1;
            userDataPrefs.edit().putInt("SKIP_COUNTER",numberOfSkips).commit();
            isSkipped = true;
            new SimChecker().execute();

        }else if(R.id.termsOfAgreement ==v.getId())
        {
            String url = "http://ibalanceapp.com/privacy-policy/";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);


        }*/

    }

    class SimChecker extends AsyncTask<Void, Void, ArrayList<SimModel>>
    {
        SharedPreferences deviceDetailsPreferences = MyApplication.context.getSharedPreferences("DEVICE_DETAILS", Context.MODE_PRIVATE);
        SharedPreferences.Editor mEditor = deviceDetailsPreferences.edit();
        boolean isNetworkConnected() {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

            return cm.getActiveNetworkInfo() != null;
        }
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
        protected void onPostExecute(final ArrayList<SimModel> sim_list)
        {

            super.onPostExecute(sim_list);
            final SharedPreferences userDataPref = MyApplication.context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
            //chnage it to == for Sim
            (new RegexUpdater()).check();
            if (sim_list == null)
            {
                FlurryAgent.logEvent("NO_SIM");
                Helper.logGA("NO_SIM");
                startActivity(new Intent(SplashscreenActivity.this, NoSimActivity.class));
                finish();
            }
            else
            {

                /*if((!isVerified && !isSkipped)){
                    //Add Phpne number for default prefix
                    Digits.authenticate(((DigitLoginActivity)getApplication()).getAuthCallback(),R.style.CustomDigitsTheme);
                    finish();

                }*/

                boolean first_app_launch = userDataPref.getBoolean("FIRST_APP_LAUNCH", true);
                if (first_app_launch)
                {
                   logNewUser(sim_list);
                }
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
                startActivity(new Intent(getApplicationContext(), MainActivity.class).putExtra("RECHARGE",recharge));
                finish();
            }
        }

        private void logNewUser(ArrayList<SimModel> sim_list)
        {
            ParseObject pObj = null;
            if(isNetworkConnected())
            {
                pObj = new ParseObject("SIMPLY_USERS");
                pObj.put("TYPE", "NEW");
                pObj.put("MODEL", Build.MODEL);
                pObj.put("MANUFACTURER", Build.MANUFACTURER);

                TelephonyManager manager = (TelephonyManager) (MyApplication.context.getSystemService(TELEPHONY_SERVICE));
                pObj.put("DEVICE_ID", manager.getDeviceId());
                for (SimModel m : GlobalData.globalSimList)
                {
                    pObj.put("CARRIER_" + m.getSimslot(), m.getCarrier());
                    pObj.put("CIRCLE_" + m.getSimslot(), m.getCircle());
                }
                String phNumber = manager.getLine1Number();
                if (phNumber != null)
                {
                    pObj.put("NUMBER", phNumber);
                }
                pObj.put("ANDROID_VERSION", Build.VERSION.SDK_INT);
                pObj.put("TWO_SLOTS", SimModel.isTwo_slots());
                pObj.put("DUAL_SIM", !DualSimConstants.IS_SINGLE_SIM);
                pObj.put("COLUMN_NAMES", SimModel.call_log_columns.toString());
                                /*pObj.put("SAMPLE_CALLLOGS", getCallLogs());*/
                try
                {
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (loc != null)
                    {
                        pObj.put("LOCATION", loc.toString());
                    }
                } catch (Exception e)
                {
                    pObj.put("LOCATION", "EXCEPTION");
                }
                pObj.put("APP_VERSION", BuildConfig.VERSION_CODE);

                pObj.put("DUAL_SIM_LOGS", SimModel.debugInfo);
               //V20 Log.d(TAG,"SAVING INFO");
                pObj.saveEventually();


                SharedPreferences.Editor editor = MyApplication.context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE).edit();
                editor.putInt("APP_VERSION", BuildConfig.VERSION_CODE);
                editor.putBoolean("FIRST_APP_LAUNCH", false);
                editor.commit();
            }
            else
            {
               //V20 Log.d(TAG,"No NET");
            }

        }

    }


}
