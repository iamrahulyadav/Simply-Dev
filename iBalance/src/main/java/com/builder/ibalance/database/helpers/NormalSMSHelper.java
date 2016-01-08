package com.builder.ibalance.database.helpers;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.builder.ibalance.database.DatabaseManager;
import com.builder.ibalance.models.USSDModels.NormalSMS;

import java.util.LinkedList;
import java.util.List;

public class NormalSMSHelper {
	final String TAG = NormalSMSHelper.class.getSimpleName();
	DatabaseManager mMySQLiteHelper;
	public NormalSMSHelper() {
		mMySQLiteHelper = DatabaseManager.getInstance();
	}


	/* "CREATE TABLE SMS  ( "
	+ "_id INTEGER PRIMARY KEY  , " + "DATE INTEGER  , "
	+ "COST FLOAT, "  + "NUMBER TEXT, "+ "BALANCE FLOAT, " +"MESSAGE TEXT "+" )";*/
	public void addEntry(NormalSMS entry) {

		// 1. get reference to writable DB
		SQLiteDatabase db = mMySQLiteHelper.getWritableDatabase();

		// 2. create ContentValues to add key "column"/value
		ContentValues values = new ContentValues();
		values.put(IbalanceContract.SMSEntry.COLUMN_NAME_DATE, entry.date); // get date in milliseconds
		values.put(IbalanceContract.SMSEntry.COLUMN_NAME_SLOT, entry.sim_slot); // get date in milliseconds
		values.put(IbalanceContract.SMSEntry.COLUMN_NAME_COST, entry.cost); // get callcost
		values.put(IbalanceContract.SMSEntry.COLUMN_NAME_BALANCE, entry.main_bal); // get balance
		values.put(IbalanceContract.SMSEntry.COLUMN_NAME_NUMBER, entry.ph_number); // last dialled number
		values.put(IbalanceContract.SMSEntry.COLUMN_NAME_MESSAGE, entry.original_message);
		// 3. insert
		Log.d(TAG,"Db Details "+ values.toString());
		db.insert(IbalanceContract.SMSEntry.TABLE_NAME, // table
				null, // nullColumnHack
				values); // key/value -> keys = column names/ values = column
							// values

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
		int date_idx = cursor.getColumnIndex(IbalanceContract.SMSEntry.COLUMN_NAME_DATE),
				cost_idx = cursor.getColumnIndex(IbalanceContract.SMSEntry.COLUMN_NAME_COST) ,
				number_idx = cursor.getColumnIndex(IbalanceContract.SMSEntry.COLUMN_NAME_NUMBER),
				main_bal_idx = cursor.getColumnIndex(IbalanceContract.SMSEntry.COLUMN_NAME_BALANCE),
				message_idx = cursor.getColumnIndex(IbalanceContract.SMSEntry.COLUMN_NAME_MESSAGE),
				sim_slot_idx = cursor.getColumnIndex(IbalanceContract.SMSEntry.COLUMN_NAME_SLOT);
		if (cursor.moveToFirst()) {
			do {
				entry = new NormalSMS();
				entry.date = cursor.getLong(date_idx);
				entry.cost = cursor.getFloat(cost_idx);
				entry.ph_number = cursor.getString(number_idx);
				entry.main_bal = cursor.getFloat(main_bal_idx);
				entry.original_message = cursor.getString(message_idx);
				entry.sim_slot = cursor.getInt(sim_slot_idx);
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
		//Cursor Adapter
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

	


}

