package com.builder.ibalance.database;


import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.builder.ibalance.database.models.NormalSMS;
import com.builder.ibalance.util.MyApplication;

public class NormalSMSHelper {
	final String TAG = BalanceHelper.class.getSimpleName();
	MySQLiteHelper mMySQLiteHelper;
	public NormalSMSHelper() {
		mMySQLiteHelper = MySQLiteHelper.getInstance(MyApplication.context);
	}


	/* "CREATE TABLE SMS  ( "
	+ "_id INTEGER PRIMARY KEY AUTOINCREMENT , " + "DATE INTEGER  , "
	+ "COST FLOAT, "  + "NUMBER TEXT, "+ "BALANCE FLOAT, " +"MESSAGE TEXT "+" )";*/
	public void addEntry(NormalSMS entry) {

		// 1. get reference to writable DB
		SQLiteDatabase db = mMySQLiteHelper.getWritableDatabase();

		// 2. create ContentValues to add key "column"/value
		ContentValues values = new ContentValues();
		values.put("DATE", entry.date); // get date in milliseconds
		values.put("COST", entry.cost); // get callcost
		values.put("BALANCE", entry.bal); // get balance
		values.put("NUMBER", entry.lastNumber); // last dialled number
		values.put("MESSAGE", entry.message);
		// 3. insert
		db.insert("SMS", // table
				null, // nullColumnHack
				values); // key/value -> keys = column names/ values = column
							// values

		// 4. close
		db.close();
	}

	public List<NormalSMS> getAllEntries() {
		List<NormalSMS> entries = new LinkedList<NormalSMS>();

		// 1. build the query
		String query = "SELECT  * FROM " + "SMS";

		// 2. get reference to writable DB
		SQLiteDatabase myDataBase = mMySQLiteHelper.getReadableDatabase();
		
		Cursor cursor = myDataBase.rawQuery(query, null);

		// 3. go over each row, build entry and add it to list
		NormalSMS entry = null;
		
		// 1-DATE 2-COST 3-NUMBER 4-BALANCE 5-TEST
		if (cursor.moveToFirst()) {
			do {
				entry = new NormalSMS();
				entry.date = (Long.parseLong(cursor.getString(1)));
				entry.cost = Float.parseFloat(cursor.getString(2));
				entry.lastNumber = cursor.getString(3);
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
		String query = "SELECT  * FROM " + "SMS" + " ORDER BY date DESC";
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

