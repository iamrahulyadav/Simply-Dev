package com.builder.ibalance.database.helpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.provider.CallLog;

import com.builder.ibalance.core.SimModel;
import com.builder.ibalance.database.DatabaseManager;
import com.builder.ibalance.database.helpers.IbalanceContract.CallLogEntry;
import com.builder.ibalance.database.models.ContactDetailModel;
import com.builder.ibalance.util.Constants;
import com.builder.ibalance.util.GlobalData;
import com.builder.ibalance.util.MyApplication;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Shabaz on 04-Sep-15.
 */
//Helper Class to Copy CallLogs to local Database + this will queried for analytics
public class CallLogsHelper
{
    final String tag = this.getClass().getSimpleName();
    SQLiteDatabase mSqlDB;
    public CallLogsHelper()
    {
        mSqlDB  = DatabaseManager.getInstance().getWritableDatabase();
        mSqlDB.execSQL(IbalanceContract.CREATE_CALL_LOG_TABLE);
    }


    public SQLiteDatabase getDatabase()
    {
        return mSqlDB;
    }
    public void executeQuery(String query) throws SQLException
    {
        try
        {
            mSqlDB.execSQL(query);
        }
        catch (SQLiteException e)
        {

        }
    }
    public long updateLocalDatabase(long start_id)
    {
        ArrayList<String> projection = new ArrayList<>();
        projection.add(CallLog.Calls._ID);
        projection.add(CallLog.Calls.DATE);
        projection.add(CallLog.Calls.NUMBER);
        projection.add(CallLog.Calls.TYPE);
        projection.add(CallLog.Calls.DURATION);
        for(String t: SimModel.call_log_columns)
        {
            projection.add(t);
        }
        Cursor managedCursor = MyApplication.context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                projection.toArray(new String[projection.size()]),
                CallLog.Calls._ID + ">?",
                new String[] { String.valueOf(start_id)}, CallLog.Calls._ID + " ASC");
        int id_index = managedCursor.getColumnIndex(CallLog.Calls._ID);
        int date_index =managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration_index =managedCursor.getColumnIndex(CallLog.Calls.DURATION);
        int type_index =managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int number_index =managedCursor.getColumnIndex( CallLog.Calls.NUMBER);
        long date,duration,id ;
        String number,query,query_format = "INSERT INTO " +
                                            CallLogEntry.TABLE_NAME +
                                            "("+
                                            CallLogEntry.COLUMN_NAME_ID+","+
                                            CallLogEntry.COLUMN_NAME_SLOT+","+
                                            CallLogEntry.COLUMN_NAME_DATE+","+
                                            CallLogEntry.COLUMN_NAME_DURATION+","+
                                            CallLogEntry.COLUMN_NAME_TYPE+","+
                                            CallLogEntry.COLUMN_NAME_NUMBER+
                                            ") " +
                                            "VALUES ";
        int type,slot;
        long return_id = start_id;
        while(managedCursor.moveToNext())
        {
            type = managedCursor.getInt(type_index);
            duration = managedCursor.getLong(duration_index);
            if(type>4)
            {
                //If Call Log type is other than incoming,Outgoing,Missed and Voice Mail then skip
                continue;
            }
            if(type == CallLog.Calls.OUTGOING_TYPE)
            {
                if(duration<=0)
                {
                    //If outgoing is 0s then its call didn't connect so skip it
                    continue;
                }
            }
            id = managedCursor.getLong(id_index);
            date = managedCursor.getLong(date_index);
            number = managedCursor.getString(number_index);
            slot = getSlot(managedCursor);
            number = number.replace(" ","");
            query = query_format + "("+id+","+slot+","+date+","+duration+","+type+", '"+number+"')";
            //Log.d(tag,"Query = "+query);
            mSqlDB.execSQL(query);
            return_id = id;
        }
        managedCursor.close();
        return return_id;
    }

    public int getSlot(Cursor cursor)
    {
        int slot_id = 0;
        if (SimModel.isTwo_slots())
        {
            for (String column_name : SimModel.call_log_columns)
            {
                if (cursor.getColumnIndex(column_name) != -1)
                {
                    if (GlobalData.globalSimList.get(0).subid != -1)
                    {
                        if (column_name.toLowerCase().equals("iccid") || column_name.toLowerCase().equals("icc_id"))
                        {
                            String iccId = cursor.getString(cursor.getColumnIndex(column_name));
                            slot_id = getSlotIdforLG(iccId);
                        } else
                        {
                            long subid = cursor.getLong(cursor.getColumnIndex(column_name));
                            slot_id = getSlotIdforSub(subid);
                        }

                    } else if (SimModel.dual_type == Constants.TYPE_ASUS)
                    {
                        String sim_index = cursor.getString(cursor.getColumnIndex(column_name));
                        slot_id = getSlotIdforAsus(sim_index);
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
        return slot_id;
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

    private int getSlotIdforSub(long subid)
    {

        for (SimModel model: GlobalData.globalSimList)
        {
            if(model.subid == subid)
                return model.getSimslot();
        }
        return 0;
    }

    public Cursor getAllCallLogs()
    {

        ArrayList<String> projection = new ArrayList<>();
        projection.add(CallLog.Calls._ID);
        projection.add(CallLog.Calls.DATE);
        projection.add(CallLog.Calls.NUMBER);
        projection.add(CallLog.Calls.TYPE);
        projection.add(CallLog.Calls.DURATION);
        for(String t: SimModel.call_log_columns)
        {
            projection.add(t);
        }
        Cursor managedCursor = MyApplication.context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                 projection.toArray(new String[projection.size()]) ,
                        null,
                        null,
                        CallLog.Calls._ID + " ASC");
        return managedCursor;
    }

    public void insert(ContactDetailModel entry) throws SQLException
    {/*
        public String number,name,carrier,circle,image_uri;
        public int in_count;
        public int in_duration;
        public int out_count;

        public int out_duration;
        public int miss_count;*/
        ContentValues values = new ContentValues();
        values.put(IbalanceContract.ContactDetailEntry.COLUMN_NAME_NUMBER, entry.number);
        values.put(IbalanceContract.ContactDetailEntry.COLUMN_NAME_NAME,entry.name);
        values.put(IbalanceContract.ContactDetailEntry.COLUMN_NAME_CARRIER, entry.carrier);
        values.put(IbalanceContract.ContactDetailEntry.COLUMN_NAME_CIRCLE, entry.circle);
        values.put(IbalanceContract.ContactDetailEntry.COLUMN_NAME_IN_COUNT, entry.in_count);
        values.put(IbalanceContract.ContactDetailEntry.COLUMN_NAME_IN_DURATION, entry.in_duration);
        values.put(IbalanceContract.ContactDetailEntry.COLUMN_NAME_OUT_COUNT, entry.out_count);
        values.put(IbalanceContract.ContactDetailEntry.COLUMN_NAME_OUT_DURATION, entry.out_duration);
        values.put(IbalanceContract.ContactDetailEntry.COLUMN_NAME_MISS_COUNT, entry.miss_count);

        mSqlDB.insert(IbalanceContract.ContactDetailEntry.TABLE_NAME, // table
                null, // nullColumnHack
                values); // key/value -> keys = column names/ values = column
        // values
    }
}
