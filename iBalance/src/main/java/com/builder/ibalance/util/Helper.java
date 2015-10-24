package com.builder.ibalance.util;

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
import android.widget.Toast;

import com.builder.ibalance.core.SimModel;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Shabaz on 7/28/2015.
 */
public  class Helper {
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
    public static void logUserEngageMent(String from)
    {

        Tracker t = ((MyApplication)MyApplication.context).getTracker(MyApplication.TrackerName.APP_TRACKER);
        Map<String, String> engageParams = new HashMap<String, String>();
        String deviceID = ((TelephonyManager)MyApplication.context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        engageParams.put("DEVICE_ID", deviceID);
        engageParams.put("IST_DATE", (new IndianDate().getTime()) + "");
        engageParams.put("FROM", from);
        FlurryAgent.logEvent("USER_ENGAGEMENT", engageParams);
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("DEVICE_ID",deviceID);
            jsonObject.put("IST_DATE",(new IndianDate()).getTime());
        } catch (JSONException e)
        {
           //V10e.printStackTrace();
        }
        t.send(new HitBuilders.EventBuilder().
                setCategory("USER_ENGAGEMENT")
                .setAction(from)
                .setLabel(jsonObject.toString())
                .build());
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
            mEditor.putString("DUAL_SIM", dual_sim_details).commit();
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
}
