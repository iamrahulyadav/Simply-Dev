package com.builder.ibalance.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.builder.ibalance.core.SimModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

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

                Log.d(tag, "Serializing Failed");
                e.printStackTrace();

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
            String dual_sim_details = mSharedPreferences.getString("DUAL_SIM",null);
            Log.d(tag,dual_sim_details);
            try
            {
                sim_list = new Gson().fromJson(dual_sim_details,listType);
                SimModel.two_slots = mSharedPreferences.getBoolean("HAS_TWO_SLOTS", true);
                SimModel.dual_type = mSharedPreferences.getInt("DUAL_SIM_TYPE", 0);
                SimModel.call_log_columns = new Gson().fromJson(mSharedPreferences.getString("CALL_LOG_COLUMNS", "[]"), ArrayList.class);
                Log.d(tag,"Serialized SimList = "+sim_list.toString());
            }

            catch (Exception e)
            {
                Log.d(tag, "De-Serializing Failed");
                e.printStackTrace();
            }
            return sim_list;
        }


    }
}
