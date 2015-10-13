package com.builder.ibalance.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.builder.ibalance.database.models.RechargeEntry;

import java.util.Date;

public class RechargeHelper {
	DatabaseManager mMySQLiteHelper;
	public RechargeHelper() {
		mMySQLiteHelper = DatabaseManager.getInstance();
	}
	
	public void addRechargeEntry(RechargeEntry entry)
	{
		SQLiteDatabase db = mMySQLiteHelper.getWritableDatabase();

		// 2. create ContentValues to add key "column"/value
		ContentValues values = new ContentValues();
		values.put("DATE", entry.date); // get date in milliseconds

		values.put("RECHARGE_AMOUNT",entry.RechargeAmount); // get callcost
		values.put("BALANCE",entry.Balance); // get balance

		// 3. insert
		db.insert("RECHARGE", // table
				null, //nullColumnHack
				values); // key/value -> keys = column names/ values = column values

	}
	public void adDummyData()
	{
		RechargeEntry rc = new RechargeEntry((new Date().getTime())-100000, (float)200.0, (float)220.0);
		addRechargeEntry(rc);
		 rc = new RechargeEntry((new Date().getTime())-160000, (float)200.0, (float)220.0);
		 addRechargeEntry(rc);
	}
	public Bundle getlastEntry()
	{
		String query = "SELECT  * FROM " + "RECHARGE" + " ORDER BY date DESC LIMIT 1";
		/*// 2. get reference to writable DB
		String myPath = myContext.getDatabasePath(this.DB_NAME).toString();// DB_PATH
																			// +
																			// DB_NAME;
		myDataBase = SQLiteDatabase.openDatabase(myPath, null,
				SQLiteDatabase.OPEN_READONLY);*/
		SQLiteDatabase myDataBase = mMySQLiteHelper.getReadableDatabase();
		Bundle mBundle = new Bundle();
		Cursor cursor = myDataBase.rawQuery(query, null);
		if(cursor.moveToFirst())
		{
			mBundle.putLong("DATE", cursor.getLong(1));
			mBundle.putFloat("RECHARGE_AMOUNT", cursor.getFloat(2));
			cursor.close();
			return mBundle;
		}
		else
			{
			cursor.close();
				return null;
			}
	}
	public Cursor getData() {
		// 1. build the query
		String query = "SELECT  * FROM " + "RECHARGE" + " ORDER BY date DESC";
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
