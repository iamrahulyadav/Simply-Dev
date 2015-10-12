/*
package com.builder.ibalance.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.builder.ibalance.util.MyApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MySQLiteHelper extends SQLiteOpenHelper {

	// The Android's default system path of your application database.
	final static String TAG = MySQLiteHelper.class.getSimpleName();
	private final static String DB_NAME = "ibalance.db";

	final String CREATE_CALL_TABLE = "CREATE TABLE CALL ( "
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT , " + "DATE INTEGER  , "
			+ "COST FLOAT, "  + "DURATION INTEGER, "
			+ "NUMBER TEXT, "+ "BALANCE FLOAT, " +"MESSAGE TEXT"+" )";
	
	final String CREATE_VOICE_PACK_TABLE = "CREATE TABLE VOICE_PACK ( "
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT , " + "DATE INTEGER  , "
			 + "DURATION INTEGER, "+ "NUMBER TEXT, "+ "REMAINING INTEGER, " +"VALIDITY TEXT, " +"MESSAGE TEXT"+ " )";
	
	final String CREATE_SMS_TABLE = "CREATE TABLE SMS  ( "
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT , " + "DATE INTEGER  , "
			+ "COST FLOAT, "  + "NUMBER TEXT, "+ "BALANCE FLOAT, " +"MESSAGE TEXT "+" )";
	
	final String CREATE_SMS_PACK_TABLE = "CREATE TABLE SMS_PACK ( "
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT , " + "DATE INTEGER  , "
			 + "NUMBER TEXT ,  "+ "REMAINING INTEGER, " +" VALIDITY TEXT, " +"MESSAGE TEXT"+ " )";
	
	final String CREATE_DATA_TABLE = "CREATE TABLE DATA ( "
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT , " + "DATE INTEGER  , "
			+ "COST FLOAT, "  +"DATA_CONSUMED FLOAT , "+ "BALANCE FLOAT, " +"MESSAGE TEXT" + " )";
	//TYPE: 0-2G 1-3G 2-4G
	final String CREATE_DATA_PACK_TABLE = "CREATE TABLE DATA_PACK ( "
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT , " + "DATE INTEGER  , "
			 + "TYPE INTEGER, "+ "DATA_CONSUMED FLOAT,  "+ "REMAINING INTEGER, " +"VALIDITY TEXT, "+"BALANCE FLOAT , " +"MESSAGE TEXT"+ " )";
	
	final String CREATE_RECHARGE_TABLE = "CREATE TABLE RECHARGE ( "
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT , "+ "DATE INTEGER  , " + "RECHARGE_AMOUNT FLOAT, "
			+ "BALANCE FLOAT, " +"MESSAGE TEXT" + " )";
	
	final String CREATE_SUSPICIOUS_TABLE = "CREATE TABLE SUSPICIOUS ( "
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT , "+ "DATE INTEGER  , " + "AMOUNT FLOAT " + " )";
	
	private SQLiteDatabase myDataBase;
	// static Context ctx;
	//private final Context myContext;
	Cursor cursor = null;
	private static MySQLiteHelper mMySQLiteHelper = null;

	public static MySQLiteHelper getInstance(Context ctx) {
		if (mMySQLiteHelper == null) {
			mMySQLiteHelper = new MySQLiteHelper(ctx.getApplicationContext());
		}
		return mMySQLiteHelper;
	}

	public MySQLiteHelper(Context context) {

		super(context, DB_NAME, null, 1);
		try {
			createDataBase();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//this.myContext = context;
	}

	*/
/**
	 * Creates a empty database on the system and rewrites it with your own
	 * database.
	 * *//*

	public void createDataBase() throws IOException {

		boolean dbExist = checkDataBase();

		if (dbExist) {
			// do nothing - database already exist
			//Log.d("database", "Database Already Exists");
		} else {

			// By calling this method and empty database will be created into
			// the default system path
			// of your application so we are gonna be able to overwrite that
			// database with our database.
			//Log.d(TAG, "Databse does NOT Exist");
			this.getReadableDatabase();

			try {
				//Log.d(TAG, "Trying to copy");
				// copy the mapping mobile number to circle, operator database
				copyDataBase();
				// create tables
				SQLiteDatabase db = this.getWritableDatabase();

				// create books table
				//Log.d("databse", CREATE_CALL_TABLE);
				db.execSQL(CREATE_CALL_TABLE);
				//Log.d("databse", "executed " + CREATE_CALL_TABLE);
				
				db.execSQL(CREATE_VOICE_PACK_TABLE);
				//Log.d("databse", "executed " + CREATE_VOICE_PACK_TABLE);
				
				db.execSQL(CREATE_SMS_TABLE);
				//Log.d("databse", "executed " + CREATE_SMS_TABLE);
				
				db.execSQL(CREATE_SMS_PACK_TABLE);
				//Log.d("databse", "executed " + CREATE_SMS_PACK_TABLE);
				
				db.execSQL(CREATE_DATA_TABLE);
				//Log.d("databse", "executed " + CREATE_DATA_TABLE);
				
				db.execSQL(CREATE_DATA_PACK_TABLE);
				//Log.d("databse", "executed " + CREATE_DATA_PACK_TABLE);

				db.execSQL(CREATE_RECHARGE_TABLE);
				//Log.d("databse", "executed " + CREATE_RECHARGE_TABLE);
				
				db.execSQL(CREATE_SUSPICIOUS_TABLE);
				//Log.d("databse", "executed " + CREATE_SUSPICIOUS_TABLE);

				
				db.close();

			} catch (IOException e) {
				//Log.d("database", "copy failed");
				throw new Error("Error copying database");

			}
		}

	}

	*/
/**
	 * Check if the database already exist to avoid re-copying the file each
	 * time you open the application.
	 * 
	 * @return true if it exists, false if it doesn't
	 *//*


	private boolean checkDataBase() {
		boolean checkdb = false;
		try {
			String myPath = MyApplication.context.getDatabasePath(DB_NAME).toString();
			//Log.d("database path", myPath);
			File dbfile = new File(myPath);
			checkdb = dbfile.exists();
		} catch (SQLiteException e) {
			System.out.println("Database doesn't exist");
		}

		return checkdb;
	}

	*/
/*
	 * Copies your database from your local assets-folder to the just created
	 * empty database in the system folder, from where it can be accessed and
	 * handled. This is done by transfering bytestream.
	 *//*

	private void copyDataBase() throws IOException {
		//Log.d(TAG, "copy started");
		// Open your local db as the input stream
		InputStream myInput = MyApplication.context.getAssets().open(DB_NAME);
		//Log.d(TAG, "assets  opened");
		// Path to the just created empty db
		String outFileName = MyApplication.context.getDatabasePath(DB_NAME).toString();// DB_PATH
																				// +
																				// DB_NAME;
		//Log.d("databse file", outFileName);
		if (myDataBase != null)
			myDataBase.close();

		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}

		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();
		//Log.d(TAG, "data base copied sucessfully");

	}

	public SQLiteDatabase openDataBase() throws SQLException {

		// Open the database
		String myPath = MyApplication.context.getDatabasePath(DB_NAME).toString();// DB_PATH
																			// +
																			// DB_NAME;
		myDataBase = SQLiteDatabase.openDatabase(myPath, null,
				SQLiteDatabase.OPEN_READONLY);
		return myDataBase;
	}

	@Override
	public synchronized void close() {

		if (myDataBase != null)
			myDataBase.close();

		super.close();

	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}
}
*/
