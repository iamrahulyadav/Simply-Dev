package com.builder.ibalance.services;

import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.util.Log;

import com.builder.ibalance.util.MyApplication;

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

    @Override
    public void onChange(boolean selfChange, Uri uri)
    {
        Log.d(tag, "Change in callLog detected " + uri.toString());
        String sortOrder = String.format("%s limit 1 ", CallLog.Calls.DATE + " DESC");

        cursor = MyApplication.context.getContentResolver().
                query(
                        android.provider.CallLog.Calls.CONTENT_URI,
                        null,
                        android.provider.CallLog.Calls.TYPE+"="+android.provider.CallLog.Calls.OUTGOING_TYPE,
                        null,
                        sortOrder);
        if(cursor.moveToFirst() && cursor!=null)
        {
            int len = cursor.getColumnCount();
            for(int i=0;i<len;i++)
            {
                Log.d(tag,"Col = "+cursor.getColumnName(i));
            }
            Message msg = accessibiltyServiceHandler.obtainMessage();
            msg.what = 1729;
            msg.obj = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
            accessibiltyServiceHandler.sendMessage(msg);
            /*accessibiltyServiceHandler.post(new Runnable()
            {
                *//**
                 * Starts executing the active part of the class' code. This method is4
                 * called when a thread is started that has been created with a class which
                 * implements {@code Runnable}.
                 *//*
                @Override
                public void run()
                {

                    Toast.makeText(MyApplication.context
                            , "Call Log Updated with " + cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER)), Toast.LENGTH_LONG).show();

                }

            });*/
           //new CallDetailsFetch().execute(cursor);
        }
        else
        {
            //Don't do anything
        }

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
