package com.builder.ibalance.database.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.builder.ibalance.database.DatabaseManager;
import com.builder.ibalance.database.models.NormalCall;
import com.builder.ibalance.util.MyApplication;

import java.util.LinkedList;
import java.util.List;

public class BalanceHelper {
	final String TAG = BalanceHelper.class.getSimpleName();
	DatabaseManager mMySQLiteHelper;
	public BalanceHelper() {
		mMySQLiteHelper = DatabaseManager.getInstance();
	}

	public void addDemoentries() {
		//Log.d(TAG, "Settig Demo Data");
		SQLiteDatabase db = mMySQLiteHelper.getWritableDatabase();
		String[] q = {
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1425033746204,0.1,108.04,6,'+919535534267')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1425033794144,0.05,107.99,3,'+919535534267')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1425034090614,0.06,107.83,4,'+919535534267')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1425034091606,0.06,107.83,4,'+917795490992')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1425034886139,0.06,107.77,4,'+917795490992')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1425300205595,0.59,104.39,37,'+918431670783')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1425319038672,0.5,103.9,31,  '+919880393697')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1425469221311,0.19,103.61,12,'+919066663715')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1425469471124,0.21,103.26,13,'+919066663715')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1425469709301,0.21,103.26,13,'+919066663715')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1425469715325,0.21,103.26,13,'+919066663715')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1425486509285,0.4,102.41,25,'+9190666637155')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1425705464783,1.02,99.72,64,'+9189710049815')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1425705864490,1.02,99.72,64,'+9189710049815')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1425707730034,1.14,98.42,71,'+9189710049815')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1425713714271,1.06,97.37,66,'+9172591355145')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1425713751945,1.06,97.37,66,'+9172591355145')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1425713812937,0.21,97.16,13,'+9181520615875')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1425714269514,0.61,96.55,38,'+9174119598345')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1425734337845,0.38,95.59,24,'+9198803936975')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1425873430785,0.34,95.05,21,'+9170225993425')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1425873431524,0.34,95.05,21,'+9170225993425')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1425873434495,0.34,95.05,21,'+9170225993425')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1425873440226,0.34,95.05,21,'+9170225993425')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1425879844147,0.13,94.93,8,'+91702259934225')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1426043774475,0.14,94.78,9,'+91974259800325')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1426058751502,0.22,94.56,14,'+9195385991615')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1426123738838,0.82,93.74,51,'+9198803936975')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1426161309399,0.26,92.54,16,'+9177954909925')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1426314946477,0.61,91.56,36,'+9195385991615')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1426316367352,0.61,91.56,36,'+9195385991615')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1426326784911,0.48,91.09,28,'+9195385991615')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1426354220960,0.41,90.44,24,'+9172591355145')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1426488741655,0.88,89.56,52,'+9189710049815')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1426768493594,0.31,86.46,18,'+9197396634875')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1426843484946,0.6,85.87,35,'+91888412231175')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1426858436776,0.44,84.44,26,'+9181520615875')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1426859260082,0.53,83.91,31,'+9196203724105')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1426859375909,1.16,82.76,68,'+9196203724105')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1426860407311,0.75,82.01,44,'+9196203724105')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1426861580435,0.66,81.34,39,'+9196203724105')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1427025485215,0.73,74.87,43,'+9196203724105')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1427040197605,0.32,74.54,19,'+9198803936975')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1427047218588,0.46,74.09,27,'+9180507998815')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1427127445600,0.17,73.62,10,'+9198803936975')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1427282389841,0.48,73.14,28,'+9180507998815')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1427342821910,0.34,72.8,20,'+91988039369715')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1427466301313,0.37,72.43,22,'+9189048453905')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1427518629710,0.75,71.68,44,'+9189048453905')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1427519049973,0.63,71.05,37,'+9174119598345')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1427632150976,1.85,68.65,109,'+919880393697')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1427636484841,0.87,67.24,51,'+9197393327827')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1427643338067,0.46,66.78,27,'+9197393327827')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1427643610540,1.34,65.44,79,'+9184317078167')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1427644919187,0.02,65.42,1,'+9188926942067')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1427644984424,0.2,65.22,12,'+9188926942067')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1427645459866,0.22,64.57,13,'+9188926942067')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1427645983213,0.07,64.5,4,  '+9188926942067')",
				"INSERT INTO USSD (date,callcost,bal,callduration,lastnumber) VALUES (1427646006184,0.17,64.33,10,'+9188926942067')" };
		for (int i = 0; i < q.length; i++) {
			db.execSQL(q[i]);
		}
		db.close();
		//Log.d(TAG, "Finished Setting  Data");
	}

	public void addEntry(NormalCall entry) {

		// 1. get reference to writable DB
		SQLiteDatabase db = mMySQLiteHelper.getWritableDatabase();

		// 2. create ContentValues to add key "column"/value
		ContentValues values = new ContentValues();
		values.put("DATE", entry.date); // get date in milliseconds
		values.put(IbalanceContract.CallEntry.COLUMN_NAME_SLOT,entry.slot);
		values.put("COST", entry.callCost); // get callcost
		values.put("BALANCE", entry.bal); // get balance
		values.put("DURATION", entry.callDuration); // get callduration
		values.put("NUMBER", entry.lastNumber); // last dialled number
		values.put("MESSAGE", entry.message);
		// 3. insert
		db.insert("CALL", // table
				null, // nullColumnHack
				values); // key/value -> keys = column names/ values = column
							// values

		// 4. close
		db.close();
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

        //TODO need to change it IBalance Contract
        int date_idx = cursor.getColumnIndex("DATE"),
                slot_idx = cursor.getColumnIndex("SLOT"),
                cost_idx = cursor.getColumnIndex("COST"),
                dur_idx = cursor.getColumnIndex("DURATION"),
                num_idx = cursor.getColumnIndex("NUMBER"),
                bal_idx = cursor.getColumnIndex("BALANCE"),
                msg_idx =cursor.getColumnIndex("MESSAGE");
        while (cursor.moveToNext())
        {
            entry = new NormalCall();
            entry.date = cursor.getLong(date_idx);
            entry.slot =  cursor.getInt(slot_idx);
            entry.callCost = cursor.getFloat(cost_idx);

            entry.callDuration = cursor.getInt(dur_idx);
            entry.lastNumber = cursor.getString(num_idx);
            entry.bal = cursor.getFloat(bal_idx);
            entry.message = cursor.getString(msg_idx);
            // Add book to entry
            entries.add(entry);
		}
		cursor.close();

		// //Log.d("getAllUSSD Entries()", entries.toString());
		myDataBase.close();
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
        Log.d(TAG,query);
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
        //TODO need to change it IBalance Contract
        int date_idx = cursor.getColumnIndex("DATE"),
                slot_idx = cursor.getColumnIndex("SLOT"),
                cost_idx = cursor.getColumnIndex("COST"),
                dur_idx = cursor.getColumnIndex("DURATION"),
                num_idx = cursor.getColumnIndex("NUMBER"),
                bal_idx = cursor.getColumnIndex("BALANCE"),
                msg_idx =cursor.getColumnIndex("MESSAGE");
		if (cursor.moveToFirst()) {
			do {
				entry = new NormalCall();
				entry.date = (Long.parseLong(cursor.getString(date_idx)));
				entry.callCost = Float.parseFloat(cursor.getString(cost_idx));
				total_call_cost += entry.callCost;

				entry.callDuration = (Integer.parseInt(cursor.getString(dur_idx)));
				entry.lastNumber = cursor.getString(num_idx);
				entry.bal = Float.parseFloat(cursor.getString(bal_idx));
				total_call_duration += entry.callDuration;
				

				// Add book to entry
				entries.add(entry);
			} while (cursor.moveToNext());

			try {
				//Log.d(TAG, "Total total_call_cost = " + total_call_cost);
				//Log.d(TAG, "Total Call Duration = " + total_call_duration);
				editor.putFloat("CALL_RATE", (float) total_call_cost * 100
						/ total_call_duration);
				//Log.d(TAG, "Normally CALL_RATE =" + (float) total_call_cost* 100 / total_call_duration);
				editor.commit();
			} catch (Exception e) {
				editor.putFloat("CALL_RATE", (float) 1.7);
				//Log.d(TAG, "Exception CALL_RATE = 1.7");
				editor.commit();
			}
		} else {
			editor.putFloat("CALL_RATE", (float) 1.7);
			//Log.d(TAG, "No data callRate = 1.7");
			editor.commit();
		}

		//Log.d("getEntriesFromDate()", entries.toString());
		myDataBase.close();
		// return entries
		return entries;
	}

	public void close() {
		mMySQLiteHelper.close();
		
	}
}
