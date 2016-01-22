package com.builder.ibalance.database.helpers;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.builder.ibalance.database.DatabaseManager;
import com.builder.ibalance.models.USSDModels.NormalData;

import java.util.LinkedList;
import java.util.List;

public class NormalDataHelper {
	final String TAG = NormalDataHelper.class.getSimpleName();
	DatabaseManager mMySQLiteHelper;
	public NormalDataHelper() {
		mMySQLiteHelper = DatabaseManager.getInstance();
	}

	
	public void addEntry(NormalData entry) {

		// 1. get reference to writable DB
		SQLiteDatabase db = mMySQLiteHelper.getWritableDatabase();
					
		// 2. create ContentValues to add key "column"/value
		ContentValues values = new ContentValues();
		values.put(IbalanceContract.DataEntry.COLUMN_NAME_DATE, entry.date); // get date in milliseconds
		values.put(IbalanceContract.DataEntry.COLUMN_NAME_COST, entry.cost); // get callcost
		values.put(IbalanceContract.DataEntry.COLUMN_NAME_SLOT, entry.sim_slot); // get Sim_slot
		values.put(IbalanceContract.DataEntry.COLUMN_NAME_BALANCE, entry.main_bal); // get balance
		values.put(IbalanceContract.DataEntry.COLUMN_NAME_DATA_CONSUMED, entry.data_used); // get callduration
		values.put(IbalanceContract.DataEntry.COLUMN_NAME_MESSAGE, entry.original_message);
		Log.d(TAG,"Db Details "+ values.toString());
		// 3. insert
		db.insert(IbalanceContract.DataEntry.TABLE_NAME, // table
				null, // nullColumnHack
				values); // key/value -> keys = column names/ values = column
							// values

	}

	public List<NormalData> getAllEntries() {
		List<NormalData> entries = new LinkedList<NormalData>();
		//TODO Refactor with IbalanceContract and new Changes
		// 1. build the query
		String query = "SELECT  * FROM " + "DATA";

		// 2. get reference to writable DB
		SQLiteDatabase myDataBase = mMySQLiteHelper.getReadableDatabase();
		
		Cursor cursor = myDataBase.rawQuery(query, null);

		// 3. go over each row, build entry and add it to list
		NormalData entry = null;
		// 1-Date 2-COST 3-DATA_CONSUMED 4-BALANCE 5-MESSAGE
		try
		{
		if (cursor.moveToFirst()) {
			do {
				entry = new NormalData();
				entry.date = (Long.parseLong(cursor.getString(1)));
				entry.cost = Float.parseFloat(cursor.getString(2));
				
				entry.data_used = (Float.parseFloat(cursor.getString(3)));
				entry.main_bal = Float.parseFloat(cursor.getString(4));
				entry.original_message = cursor.getString(5);
				// Add book to entry
				entries.add(entry);
			} while (cursor.moveToNext());
		}
	}
	catch (Exception e)
	{
		//V10e.printStackTrace();
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
		String query = "SELECT  * FROM " + "DATA" + " ORDER BY date DESC";
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



}

