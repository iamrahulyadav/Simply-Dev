package com.builder.ibalance.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;

import com.builder.ibalance.core.SimModel;
import com.builder.ibalance.messages.OutgoingCallMessage;
import com.builder.ibalance.util.DualSimConstants;
import com.builder.ibalance.util.GlobalData;
import com.builder.ibalance.util.MyApplication;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;

/**
 * Created by Shabaz on 25-Sep-15.
 */

public class CallLogObserver extends ContentObserver
{
    final String tag = this.getClass().getSimpleName();
    Handler accessibiltyServiceHandler;
    Cursor cursor;
    //EventBus eventBus;
    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public CallLogObserver(Handler handler)
    {
        super(handler);
        accessibiltyServiceHandler = handler;
        //eventBus = EventBus.getDefault();
    }

    @Override
    public void onChange(boolean selfChange)
    {
        this.onChange(selfChange, null);
    }

    //Sends a IPC message to Accessibility service with arg1 as SimSlot and Obj as Mobile Number
    @Override
    public void onChange(boolean selfChange, Uri uri)
    {
        //Log.d(tag, "Change in callLog detected " + uri.toString());
        SharedPreferences mSharedPreferences = MyApplication.context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
        long previous_id = mSharedPreferences.getLong("PREV_ID", -1l);
        if(previous_id == -1l)
        {
            previous_id = mSharedPreferences.getLong("INDEXED_ID", -1l);
        }
       //V10Log.d(tag,"NEW ID = "+previous_id);

        cursor = MyApplication.context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                null,
                CallLog.Calls._ID + ">?",
                new String[]{String.valueOf(previous_id)}, CallLog.Calls._ID + " ASC");
        try
        {
            if (cursor != null)
            {
                int id_idx = cursor.getColumnIndex(CallLog.Calls._ID);
                int type_index = cursor.getColumnIndex(CallLog.Calls.TYPE);
                int number_index = cursor.getColumnIndex(CallLog.Calls.NUMBER);
                int duration_index = cursor.getColumnIndex(CallLog.Calls.DURATION);
                //V10Log.d(tag,"No.of Rows = "+cursor.getCount());
                while (cursor.moveToNext())
                {
                    int slot_id = 0;
                    int call_type = cursor.getInt(type_index);
                    if (call_type == CallLog.Calls.OUTGOING_TYPE)
                    {

                        int duration = cursor.getInt(duration_index);
                        if (duration > 0)
                        {

                            int len = cursor.getColumnCount();
                            for (int i = 0; i < len; i++)
                            {
                                //V10Log.d(tag,"Col = "+cursor.getColumnName(i));
                            }
                            ArrayList<String> column_names = SimModel.call_log_columns;
                            if (SimModel.isTwo_slots())
                            {
                                for (String column_name : column_names)
                                {
                                    if (cursor.getColumnIndex(column_name) != -1)
                                    {
                                        boolean found = false;
                                        if (GlobalData.globalSimList.get(0).subid != -1)
                                        {
                                            if (column_name.toLowerCase().equals("iccid") || column_name.toLowerCase().equals("icc_id"))
                                            {
                                                String iccId = cursor.getString(cursor.getColumnIndex(column_name));
                                                if (iccId != null || iccId.equals(""))
                                                {
                                                    slot_id = getSlotIdforLG(iccId);
                                                    found = true;
                                                }
                                            }
                                            if (!found)
                                            {
                                                String subid = cursor.getString(cursor.getColumnIndex(column_name));
                                                slot_id = getSlotIdforSub(subid);
                                            }

                                        } else if (SimModel.dual_type == DualSimConstants.TYPE_ASUS)
                                        {
                                            String sim_index = cursor.getString(cursor.getColumnIndex(column_name));
                                            slot_id = getSlotIdforAsus(sim_index);
                                        }
                                        //ZTE
                                        else if (column_name.toLowerCase().equals("mode_id") || column_name.toLowerCase().equals("modeid"))
                                        {
                                            String simid = cursor.getString(cursor.getColumnIndex(column_name));
                                            try
                                            {
                                                slot_id = Integer.parseInt(simid) - 1;
                                            } catch (Exception e)
                                            {
                                                slot_id = 0;
                                            }
                                        } else
                                        {
                                            String temp = cursor.getString(cursor.getColumnIndex(column_name));
                                            if (temp != null)
                                            {
                                                try
                                                {
                                                    slot_id = Integer.parseInt(temp);
                                                } catch (Exception e)
                                                {
                                                    slot_id = 0;
                                                }
                                            }
                                        }

                                    }
                                }
                            }
                            //V10Log.d(tag, "slot_id = " + slot_id);
                            String number = cursor.getString(number_index);
                            Message msg = accessibiltyServiceHandler.obtainMessage();
                            msg.arg1 = slot_id;
                            msg.what = 1729;
                            long call_id = cursor.getLong(id_idx);
                            msg.obj = new OutgoingCallMessage(number, duration, call_id);
                            accessibiltyServiceHandler.sendMessage(msg);
                        }
                    }
                    previous_id = cursor.getLong(id_idx);
                }
                cursor.close();
                mSharedPreferences.edit().putLong("PREV_ID", previous_id).commit();
            }
        }
        catch (NullPointerException e)
        {
            Crashlytics.logException(e);
        }

    }
    private int getSlotIdforLG(String iccId)
    {
        for (SimModel model: GlobalData.globalSimList)
        {
            if(model.serial.contains(iccId))
                return model.getSimslot();
        }
        return 0;
    }

    private int getSlotIdforAsus(String sim_index)
    {
        for (SimModel model: GlobalData.globalSimList)
        {
            if(model.subscriber_id.contains(sim_index))
                return model.getSimslot();
        }
        return 0;
    }

    private int getSlotIdforSub(String subid)
    {

        for (SimModel model: GlobalData.globalSimList)
        {
            if(String.valueOf(model.subid).equals(subid) || model.serial.equals(String.valueOf(subid)))
                return model.getSimslot();
        }
        return 0;
    }

    /*class CallDetailsFetch extends AsyncTask<Cursor,Void,CallDetailsEvent>
    {
        ;
        *//**
         * Runs on the UI thread before {@link #doInBackground}.
         *
         * @see #onPostExecute
         * @see #doInBackground
         *//*
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        *//**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p/>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         *//*
        @Override
        protected CallDetailsEvent doInBackground(Cursor... params)
        {
            String name ="Unknown";
            String number = "XXXXXXXXXX";
            String photo=null;
            String carrier = "Unknown";
            String circle = "Unkown";
            String total_spent = "0.0";
            int slot_id = 0;
            Cursor cursor = params[0];
            ArrayList<String> column_names = SimModel.call_log_columns;
            for (String column_name : column_names)
            {
                if(cursor.getColumnIndex(column_name)!=-1)
                {
                    if (SimModel.uses_subscription)
                    {
                        long subid = cursor.getLong(cursor.getColumnIndex(column_name));
                        slot_id = getSlotIdforSub(subid);
                    } else
                    {
                        slot_id = cursor.getInt(cursor.getColumnIndex(column_name));
                    }

                }
            }
            number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
            cursor.close();
            return new CallDetailsEvent(slot_id,name,number,photo,carrier,circle,total_spent);
        }

        *//**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         * <p/>
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param callDetailsEvent The result of the operation computed by {@link #doInBackground}.
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         *//*
        @Override
        protected void onPostExecute(CallDetailsEvent callDetailsEvent)
        {
            super.onPostExecute(callDetailsEvent);
            eventBus.post(callDetailsEvent);
        }
    }
    private int getSlotIdforSub(long subid)
    {

        for (SimModel model: GlobalData.globalSimList)
        {
            if(model.subid == subid)
                return model.getSimslot();
        }
        return 0;
    }*/
}
