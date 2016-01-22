package com.builder.ibalance.database.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.builder.ibalance.database.DatabaseManager;
import com.builder.ibalance.models.USSDModels.NormalCall;
import com.builder.ibalance.util.Helper;
import com.builder.ibalance.util.MyApplication;
import com.crashlytics.android.Crashlytics;

import java.util.LinkedList;
import java.util.List;

public class BalanceHelper {
	final String TAG = BalanceHelper.class.getSimpleName();
	DatabaseManager mMySQLiteHelper;
	public BalanceHelper() {
		mMySQLiteHelper = DatabaseManager.getInstance();
	}
	SQLiteDatabase readableDb = null;
	public void addEntry(NormalCall entry) {

		// 1. get reference to writable DB
		SQLiteDatabase db = mMySQLiteHelper.getWritableDatabase();
        String phNumber = entry.ph_number;
        phNumber = Helper.normalizeNumber(phNumber);
		// 2. create ContentValues to add key "column"/value
		ContentValues values = new ContentValues();
		values.put(IbalanceContract.CallEntry.COLUMN_NAME_ID, entry.id); // Id which matches the call log id
		values.put(IbalanceContract.CallEntry.COLUMN_NAME_DATE, entry.date); // get date in milliseconds
		values.put(IbalanceContract.CallEntry.COLUMN_NAME_SLOT,entry.sim_slot); //Sim slot
		values.put(IbalanceContract.CallEntry.COLUMN_NAME_COST, entry.call_cost); // get callcost
		values.put(IbalanceContract.CallEntry.COLUMN_NAME_BALANCE, entry.main_bal); // get balance
		values.put(IbalanceContract.CallEntry.COLUMN_NAME_DURATION, entry.call_duration); // get callduration
		values.put(IbalanceContract.CallEntry.COLUMN_NAME_NUMBER, phNumber); // last dialled number
		values.put(IbalanceContract.CallEntry.COLUMN_NAME_MESSAGE, entry.original_message);
		Log.d(TAG,"Db Details "+ values.toString());
		// 3. insert
		db.insert(IbalanceContract.CallEntry.TABLE_NAME, // table
                null, // nullColumnHack
                values); // key/value -> keys = column names/ values = column
							// values

	}
 public Float getCostForId(long id)
 {
     if(readableDb==null)
         readableDb = mMySQLiteHelper.getReadableDatabase();
     Cursor c = readableDb.query(IbalanceContract.CallEntry.TABLE_NAME,
             new String[]{IbalanceContract.CallEntry.COLUMN_NAME_COST},
             IbalanceContract.CallEntry.COLUMN_NAME_ID + " = ?",
             new String[]{id+""},null,null,null);
    try{
		if(c.moveToFirst())
		{
			//DB is 0 indexed
			Float cost = c.getFloat(0);
			c.close();
			return cost;
		}
	}
	 catch (Exception e)
	 {
		 Crashlytics.logException(e);
	 }
	 finally
	 {
		 c.close();
	 }
	 return null;
 }
	public List<NormalCall> getAllEntries() {
		List<NormalCall> entries = new LinkedList<NormalCall>();

		// 1. build the query
		String query = "SELECT  * FROM " + "CALL";

		// 2. get reference to writable DB
		SQLiteDatabase myDataBase = mMySQLiteHelper.getReadableDatabase();
		
		Cursor cursor = myDataBase.rawQuery(query, null);

		// 3. go over each row, build entry and add it to list
		NormalCall entry = null;

        int date_idx = cursor.getColumnIndex(IbalanceContract.CallEntry.COLUMN_NAME_DATE),
                slot_idx = cursor.getColumnIndex(IbalanceContract.CallEntry.COLUMN_NAME_SLOT),
                cost_idx = cursor.getColumnIndex(IbalanceContract.CallEntry.COLUMN_NAME_COST),
                dur_idx = cursor.getColumnIndex(IbalanceContract.CallEntry.COLUMN_NAME_DURATION),
                num_idx = cursor.getColumnIndex(IbalanceContract.CallEntry.COLUMN_NAME_NUMBER),
                bal_idx = cursor.getColumnIndex(IbalanceContract.CallEntry.COLUMN_NAME_BALANCE),
                msg_idx =cursor.getColumnIndex(IbalanceContract.CallEntry.COLUMN_NAME_MESSAGE);
    try
    {


        while (cursor.moveToNext())
        {
            entry = new NormalCall();
            entry.date = cursor.getLong(date_idx);
            entry.sim_slot = cursor.getInt(slot_idx);
            entry.call_cost = cursor.getFloat(cost_idx);

            entry.call_duration = cursor.getInt(dur_idx);
            entry.ph_number = cursor.getString(num_idx);
            entry.main_bal = cursor.getFloat(bal_idx);
            entry.original_message = cursor.getString(msg_idx);
            // Add book to entry
            entries.add(entry);
        }
    }
        catch (Exception e)
        {
            Crashlytics.logException(e);
        }
        finally
        {
            cursor.close();
        }

		// //Log.d("getAllUSSD Entries()", entries.toString());
		// return entries
		return entries;
	}

	public Cursor getData() {
		// 1. build the query
		String query = "SELECT  * FROM " + "CALL" + " ORDER BY date DESC";
		/*// 2. get reference to writable DB
		String myPath = myContext.getDatabasePath(this.DB_NAME).toString();// DB_PATH
																			// +
																			// DB_NAME;
		myDataBase = SQLiteDatabase.openDatabase(myPath, null,
				SQLiteDatabase.OPEN_READONLY);*/
		SQLiteDatabase myDataBase = mMySQLiteHelper.getReadableDatabase();
		// SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = myDataBase.rawQuery(query, null);
		return cursor;
	}
/*	CREATE TABLE CALL ( "
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT , " + "DATE INTEGER  , "
			+ "COST FLOAT, "  + "DURATION INTEGER, "
			+ "NUMBER TEXT, "+ "BALANCE FLOAT, " +"MESSAGE TEXT"+" )";*/
	public List<NormalCall> getEntriesFromDate(long time,int sim_slot) {
		List<NormalCall> entries = new LinkedList<NormalCall>();
		float total_call_cost = (float) 0.0;
		int total_call_duration = 0;
		// 1. build the query
		String query = "SELECT  * FROM " + "CALL" + " WHERE date >= " + time+ " AND "
                +IbalanceContract.CallEntry.COLUMN_NAME_SLOT + " = "+sim_slot;
       //V10Log.d(TAG,query);
        // 2. get reference to writable DB
//		String myPath = myContext.getDatabasePath(this.DB_NAME).toString();// DB_PATH
//																			// +
//																			// DB_NAME;
//		myDataBase = SQLiteDatabase.openDatabase(myPath, null,
//				SQLiteDatabase.OPEN_READONLY);
		SQLiteDatabase myDataBase = mMySQLiteHelper.getReadableDatabase();
		// SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = myDataBase.rawQuery(query, null);
		SharedPreferences mSharedPreferences = MyApplication.context.getSharedPreferences(
                "USER_DATA", Context.MODE_PRIVATE);
		Editor editor = mSharedPreferences.edit();
		// 3. go over each row, build entry and add it to list
		NormalCall entry = null;
		// 1-DATE 2-slot 2-COST 3-DURATION 4-NUMBER 5-BALANCE 6 - TEXT
        int date_idx = cursor.getColumnIndex(IbalanceContract.CallEntry.COLUMN_NAME_DATE),
                slot_idx = cursor.getColumnIndex(IbalanceContract.CallEntry.COLUMN_NAME_SLOT),
                cost_idx = cursor.getColumnIndex(IbalanceContract.CallEntry.COLUMN_NAME_COST),
                dur_idx = cursor.getColumnIndex(IbalanceContract.CallEntry.COLUMN_NAME_DURATION),
                num_idx = cursor.getColumnIndex(IbalanceContract.CallEntry.COLUMN_NAME_NUMBER),
                bal_idx = cursor.getColumnIndex(IbalanceContract.CallEntry.COLUMN_NAME_BALANCE),
                msg_idx =cursor.getColumnIndex(IbalanceContract.CallEntry.COLUMN_NAME_MESSAGE);
        try
        {
            if (cursor.moveToFirst())
            {
                do
                {
                    entry = new NormalCall();
                    entry.date = (Long.parseLong(cursor.getString(date_idx)));
                    entry.call_cost = Float.parseFloat(cursor.getString(cost_idx));
                    total_call_cost += entry.call_cost;

                    entry.call_duration = (Integer.parseInt(cursor.getString(dur_idx)));
                    entry.ph_number = cursor.getString(num_idx);
                    entry.main_bal = Float.parseFloat(cursor.getString(bal_idx));
                    total_call_duration += entry.call_duration;


                    // Add book to entry
                    entries.add(entry);
                } while (cursor.moveToNext());

                try
                {
                    //Log.d(TAG, "Total total_call_cost = " + total_call_cost);
                    //Log.d(TAG, "Total Call Duration = " + total_call_duration);
                    editor.putFloat("CALL_RATE", (float) total_call_cost * 100 / total_call_duration);
                    //Log.d(TAG, "Normally CALL_RATE =" + (float) total_call_cost* 100 / total_call_duration);
                    editor.commit();
                } catch (Exception e)
                {
                    editor.putFloat("CALL_RATE", (float) 1.7);
                    //Log.d(TAG, "Exception CALL_RATE = 1.7");
                    editor.commit();
                }
            } else
            {
                editor.putFloat("CALL_RATE", (float) 1.7);
                //Log.d(TAG, "No data callRate = 1.7");
                editor.commit();
            }
        }
        catch (Exception e)
        {
            Crashlytics.logException(e);
        }
        finally
        {
            cursor.close();
        }

		//Log.d("getEntriesFromDate()", entries.toString());
		// return entries
		return entries;
	}
	public float getTotalCost(String phNumber, int total_duration,float call_rate)
	{
       String query = "select sum(COST), sum(DURATION) from CALL where NUMBER = \'+91"
                + phNumber + "\'" + " OR NUMBER =\'" + phNumber + "\'";
        if(readableDb==null)
            readableDb = mMySQLiteHelper.getReadableDatabase();
        Cursor c = readableDb.rawQuery(query,null);
        float callCost = (float) 0.0;
        int duration = 0;
        try{

        if(c.moveToFirst())
        {
            try
            {
                callCost = c.getFloat(0);
                duration = c.getInt(1);
            } catch (Exception e)
            {
               //V10e.printStackTrace();
            }
        }
    }
    catch (Exception e)
    {
        Crashlytics.logException(e);
    }
    finally
    {
        c.close();
    }
        float total_cost = callCost + ((total_duration-duration) * call_rate)/100;
        return  total_cost;
	}

}
