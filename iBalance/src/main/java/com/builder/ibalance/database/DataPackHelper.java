package com.builder.ibalance.database;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.builder.ibalance.models.USSDModels.PackData;

import java.util.LinkedList;
import java.util.List;

public class DataPackHelper {
	final String TAG = DataPackHelper.class.getSimpleName();
	DatabaseManager mMySQLiteHelper;
	public DataPackHelper() {
		mMySQLiteHelper = DatabaseManager.getInstance();
	}

	
	public void addEntry(PackData entry) {

		// 1. get reference to writable DB
		SQLiteDatabase db = mMySQLiteHelper.getWritableDatabase();
					
		// 2. create ContentValues to add key "column"/value
		ContentValues values = new ContentValues();
		values.put("DATE", entry.date); // get date in milliseconds
		values.put("TYPE", 1); // get callcost
		values.put("BALANCE", entry.main_bal); // get balance
		values.put("DATA_CONSUMED", entry.data_used); // get callduration
		values.put("REMAINING", entry.data_left); // get callduration
		values.put("MESSAGE", entry.original_message);
		// 3. insert
		db.insert("DATA_PACK", // table
				null, // nullColumnHack
				values); // key/value -> keys = column names/ values = column
							// values

		// 4. close
	}
	//"DATA_PACK =   _id INTEGER PRIMARY KEY AUTOINCREMENT , " + "DATE INTEGER  , "
	// + "TYPE INTEGER, "+ "DATA_CONSUMED FLOAT,  "+ "REMAINING INTEGER, " +"VALIDITY TEXT, "+"BALANCE FLOAT , " +"MESSAGE TEXT"+ " )";
	public List<PackData> getAllEntries() {
		List<PackData> entries = new LinkedList<PackData>();

		// 1. build the query
		String query = "SELECT  * FROM " + "DATA_PACK";

		// 2. get reference to writable DB
		SQLiteDatabase myDataBase = mMySQLiteHelper.getReadableDatabase();
		
		Cursor cursor = myDataBase.rawQuery(query, null);

		// 3. go over each row, build entry and add it to list
		PackData entry = null;
		// 1-Date 2-TYPE 3-DATA_CONSUMED 4-REMAINING 5-VALIDITY 6-BALANCE 7-MESSAGE
		if (cursor.moveToFirst()) {
			do {
				entry = new PackData();
				entry.date = (Long.parseLong(cursor.getString(1)));
				//skip type as of now
				entry.data_used = (Float.parseFloat(cursor.getString(3)));
				entry.data_left = (Float.parseFloat(cursor.getString(4)));
				entry.validity = (cursor.getString(5));
				entry.main_bal = Float.parseFloat(cursor.getString(6));
				entry.original_message = cursor.getString(7);
				// Add book to entry
				entries.add(entry);
			} while (cursor.moveToNext());
		}
		cursor.close();

		// //Log.d("getAllUSSD Entries()", entries.toString());
		// return entries
		return entries;
	}

	public Cursor getData() {
		// 1. build the query
		String query = "SELECT  * FROM " + "DATA_PACK" + " ORDER BY date DESC";
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


