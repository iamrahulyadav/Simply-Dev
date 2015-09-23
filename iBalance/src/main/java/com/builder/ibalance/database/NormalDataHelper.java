package com.builder.ibalance.database;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.builder.ibalance.database.models.NormalData;
import com.builder.ibalance.util.MyApplication;

import java.util.LinkedList;
import java.util.List;

public class NormalDataHelper {
	final String TAG = NormalDataHelper.class.getSimpleName();
	MySQLiteHelper mMySQLiteHelper;
	public NormalDataHelper() {
		mMySQLiteHelper = MySQLiteHelper.getInstance(MyApplication.context);
	}

	
	public void addEntry(NormalData entry) {

		// 1. get reference to writable DB
		SQLiteDatabase db = mMySQLiteHelper.getWritableDatabase();
					
		// 2. create ContentValues to add key "column"/value
		ContentValues values = new ContentValues();
		values.put("DATE", entry.date); // get date in milliseconds
		values.put("COST", entry.cost); // get callcost
		values.put("BALANCE", entry.bal); // get balance
		values.put("DATA_CONSUMED", entry.data_consumed); // get callduration
		values.put("MESSAGE", entry.message);
		// 3. insert
		db.insert("DATA", // table
				null, // nullColumnHack
				values); // key/value -> keys = column names/ values = column
							// values

		// 4. close
		db.close();
	}

	public List<NormalData> getAllEntries() {
		List<NormalData> entries = new LinkedList<NormalData>();

		// 1. build the query
		String query = "SELECT  * FROM " + "DATA";

		// 2. get reference to writable DB
		SQLiteDatabase myDataBase = mMySQLiteHelper.getReadableDatabase();
		
		Cursor cursor = myDataBase.rawQuery(query, null);

		// 3. go over each row, build entry and add it to list
		NormalData entry = null;
		// 1-Date 2-COST 3-DATA_CONSUMED 4-BALANCE 5-MESSAGE
		if (cursor.moveToFirst()) {
			do {
				entry = new NormalData();
				entry.date = (Long.parseLong(cursor.getString(1)));
				entry.cost = Float.parseFloat(cursor.getString(2));
				
				entry.data_consumed = (Float.parseFloat(cursor.getString(3)));
				entry.bal = Float.parseFloat(cursor.getString(4));
				entry.message = cursor.getString(5);
				// Add book to entry
				entries.add(entry);
			} while (cursor.moveToNext());
		}
		cursor.close();

		// //Log.d("getAllUSSD Entries()", entries.toString());
		myDataBase.close();
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


	public void close() {
		mMySQLiteHelper.close();
		
	}
}

