package com.builder.ibalance.util;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import com.builder.ibalance.core.SimModel;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Shabaz on 7/28/2015.
 */
public  class Helper {
    static SharedPreferences mSharedPreferences;
    public static String getTotalDurationFormatted(int totalSecs) {
        String min, sec, hr;
        Integer hrs, mins, secs;
        secs = totalSecs % 60;
        if (secs < 10)
            sec = "0" + secs;
        else
            sec = "" + secs;
        totalSecs = totalSecs / 60;
        mins = totalSecs % 60;
        if (mins < 10)
            min = "0" + mins;
        else
            min = "" + mins;
        totalSecs = totalSecs / 60;
        hrs = totalSecs;
        if (hrs < 10)
            hr = "0" + hrs;
        else
            hr = "" + hrs;
        return hr + ":" + min + ":" + sec;
    }
    public static void toastHelper(final String Message)
    {
        Handler handler =  new Handler(MyApplication.context.getMainLooper());
        handler.post(new Runnable()
        {
            /**
             * Starts executing the active part of the class' code. This method is
             * called when a thread is started that has been created with a class which
             * implements {@code Runnable}.
             */
            @Override
            public void run()
            {

                Toast.makeText(MyApplication.context,Message,Toast.LENGTH_SHORT).show();
            }

        });

    }
    public static String getDeviceId()
    {
        return ((TelephonyManager)MyApplication.context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
    }
   public static boolean contactExists(String number)
    {
        Uri lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));
        String[] mPhoneNumberProjection = { ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER};
        Cursor cur = MyApplication.context.getContentResolver().query(lookupUri,mPhoneNumberProjection, null, null, null);
        try {
            if (cur.moveToFirst()) {
                return true;
            }
        } finally {
            if (cur != null)
                cur.close();
        }
        return false;
    }
    public static Boolean isAccessibilityEnabled(String id)
    {
        AccessibilityManager mAccessibilityManager = (AccessibilityManager) MyApplication.context.getSystemService(Context.ACCESSIBILITY_SERVICE);
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
    public static boolean isExists(List paramList, int paramInt)
    {
        return (paramList != null) && (paramList.size() > paramInt) && (paramList.get(paramInt) != null);
    }
    public static Intent openWhatsApp( String number,String deviceID) {
/// number is the phone number

        try
        {
            Uri mUri = Uri.parse("smsto:+" + number);
            Intent mIntent = new Intent(Intent.ACTION_SENDTO, mUri);
            PackageInfo info = MyApplication.context.getPackageManager().getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
            mIntent.setPackage("com.whatsapp");
            mIntent.putExtra("sms_body", deviceID);
            mIntent.putExtra("chat", true);
            return  mIntent;

        } catch (Exception e)
        {
            // some code
            Uri uri = Uri.parse("smsto:"+number);
            Intent it = new Intent(Intent.ACTION_SENDTO, uri);
            it.putExtra("sms_body", "--Support_info--\n"+deviceID+"\n-------------");
            return it;
        }

    }
    public static String getFromSharedPrefence(String KEY)
    {
        if(mSharedPreferences==null)
        {
            mSharedPreferences = MyApplication.context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
        }
        return mSharedPreferences.getString(KEY,null);
    }
    public static String normalizeNumber(String number)
    {
        if (number.startsWith("+91"))
        {
            number = number.substring(3);
        }
        if(number.startsWith("0"))
        {
            number = number.substring(1);
        }
        number = number.replaceAll(" ","").replaceAll("-", "");
        return number;
    }
    public static void logUserEngageMent(String from)
    {


            Tracker t = ((MyApplication) MyApplication.context).getTracker(MyApplication.TrackerName.APP_TRACKER);
            Map<String, String> engageParams = new HashMap<String, String>();
            String deviceID = ((TelephonyManager) MyApplication.context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            engageParams.put("DEVICE_ID", deviceID);
            engageParams.put("IST_DATE", (new IndianDate().getTime()) + "");
            engageParams.put("FROM", from);
            FlurryAgent.logEvent("USER_ENGAGEMENT", engageParams);
    }
    public static void logFlurry(String eventName,String param_key_1,String param_1,String param_key_2,String param_2)
    {

            HashMap<String, String> params = new HashMap<>();
            params.put(param_key_1, param_1);
            if (param_key_2 != null) params.put(param_key_2, param_2);
            FlurryAgent.logEvent(eventName, params);

    }
    public static void logFlurry(String eventName,String param_key_1,String param_1)
    {
        logFlurry(eventName,param_key_1,param_1,null,null);
    }
    public static void logGA(String category,String action,String label)
    {
            Tracker t = ((MyApplication) MyApplication.context).getTracker(MyApplication.TrackerName.APP_TRACKER);
            t.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).build());
    }
    public static void logGA(String category,String action)
    {
        logGA( category, action,"");
    }
    public static void logGA(String category)
    {
        logGA( category, "");
    }
    public static String getUSSDType(int i)
    {
        switch (i)
        {
            case ConstantsAndStatics.USSD_TYPES.NORMAL_CALL:
                //V16Log.d(TAG,"Type Normal call");
                return"NORMAL_CALL";
            case ConstantsAndStatics.USSD_TYPES.PACK_CALL:
                //V16Log.d(TAG,"Type Pack call");
                return "PACK_CALL";
            case ConstantsAndStatics.USSD_TYPES.NORMAL_SMS:
                //V16Log.d(TAG,"Type Normal SMS");
                return "NORMAL_SMS";
            case ConstantsAndStatics.USSD_TYPES.PACK_SMS:
                //V16Log.d(TAG,"Type Pack SMS");
                return "PACK_SMS";
            case ConstantsAndStatics.USSD_TYPES.NORMAL_DATA:
                //V16Log.d(TAG,"Type Normal Data");
                return "NORMAL_DATA";
            case ConstantsAndStatics.USSD_TYPES.PACK_DATA:
                //V16Log.d(TAG,"Type Pack Data");
                return "PACK_DATA";
        }
        return "UNKNOWN";
    }

    public static String shift(String ph_number)
    {
        StringBuilder sb = new StringBuilder();
        int len = ph_number.length();
        for (int i=0;i<len;i++)
        {
            sb.append(((ph_number.charAt(i))-'0'+'A'));
        }
        return sb.toString();
    }

    public static class SharedPreferenceHelper
    {
        final String tag = this.getClass().getSimpleName();
        SharedPreferences mSharedPreferences = MyApplication.context.getSharedPreferences("DEVICE_DETAILS", Context.MODE_PRIVATE);

        public  void    saveDualSimDetails(ArrayList<SimModel> sim_list)
        {

            String dual_sim_details = null,call_log_names = null;
            try
            {
                dual_sim_details = new Gson().toJson(sim_list);
                call_log_names = new Gson().toJson(SimModel.call_log_columns);

            }catch (Exception e)
            {

               //V10Log.d(tag, "Serializing Failed");
               //V10e.printStackTrace();

            }
            SharedPreferences.Editor mEditor = mSharedPreferences.edit();
            mEditor.putString("DUAL_SIM", dual_sim_details);
            mEditor.putInt("DUAL_SIM_TYPE", SimModel.getDual_type());
            mEditor.putBoolean("HAS_TWO_SLOTS", SimModel.isTwo_slots());
            mEditor.putString("CALL_LOG_COLUMNS", call_log_names);
            mEditor.commit();
        }

        public ArrayList<SimModel>  getDualSimDetails()
        {
            Type listType = new TypeToken<ArrayList<SimModel>>(){}.getType();
            ArrayList<SimModel> sim_list = null;
            String dual_sim_details = mSharedPreferences.getString("DUAL_SIM","[]");
           //V10Log.d(tag,dual_sim_details);
            try
            {
                sim_list = new Gson().fromJson(dual_sim_details,listType);
                SimModel.two_slots = mSharedPreferences.getBoolean("HAS_TWO_SLOTS", true);
                SimModel.dual_type = mSharedPreferences.getInt("DUAL_SIM_TYPE", 0);
                SimModel.call_log_columns = new Gson().fromJson(mSharedPreferences.getString("CALL_LOG_COLUMNS", "[]"), ArrayList.class);
               //V10Log.d(tag,"Serialized SimList = "+sim_list.toString());
            }

            catch (Exception e)
            {
               //V10Log.d(tag, "De-Serializing Failed");
               //V10e.printStackTrace();
            }
            return sim_list;
        }


    }
    public static boolean whatsappInstalledOrNot()
    {
        PackageManager pm = MyApplication.context.getPackageManager();
        boolean app_installed = false;
        try
        {
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e)
        {
            app_installed = false;
        }
        return app_installed;
    }
}
