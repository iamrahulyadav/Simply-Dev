package com.builder.ibalance.database;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import com.builder.ibalance.BuildConfig;
import com.builder.ibalance.database.helpers.IbalanceContract;
import com.builder.ibalance.util.MyApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Shabaz on 17-Aug-15.
 */

public class DatabaseManager extends SQLiteOpenHelper {
    // The Android's default system path of your application database.
    final static String tag = DatabaseManager.class.getSimpleName();
    private final static String DATABASE_NAME = "simply.db";
    private final static String OLD_DATABASE_NAME = "ibalance.db";
    private static final int DATABASE_VERSION = 4;
    private static DatabaseManager sInstance;
    public static synchronized DatabaseManager getInstance() {
        if (sInstance == null) {

            //to  trigger on update
            //Hack level 1729
            //All because I was copying a db which has db version 0!!!!!!!!!!!!
            //on getWriteableDB when version 0 upgrade is not called and creates a database
                if(BuildConfig.VERSION_CODE>=12)
                {

                    try
                    {
                        String myPath = MyApplication.context.getDatabasePath(DATABASE_NAME).toString();
                        SQLiteDatabase myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
                        if (myDataBase.getVersion() == 0)
                        {
                            myDataBase.execSQL("PRAGMA user_version = " + 2);
                        }
                        myDataBase.close();
                    }
                    catch (Exception e)
                    {
                        //Do Nothing a fresh install happened
                    }
                }
            sInstance = new DatabaseManager(MyApplication.context.getApplicationContext());




        }
        return sInstance;
    }
    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static method "getInstance()" instead.
     */
    private DatabaseManager(Context context)
    {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //V10Log.d(tag, "DatabaseManager");
        try {
            createDataBase();
        }
        catch (IOException e)
        {
            //V10Log.d(tag,"Failed to Create Databse");
            //V10e.printStackTrace();
        }


    }

    public void createDataBase() throws IOException {
        //V10Log.d(tag,"createDataBase");
        boolean dbExist = checkDataBase(DATABASE_NAME);

        if (dbExist) {
            // do nothing - database already exist
            //Log.d("database", "Database Already Exists");
        } else {

            // By calling this method and empty database will be created into
            // the default system path
            // of the application so that will be able to overwrite that
            // database with asset database.
            //Log.d(TAG, "Database does NOT Exist");
            this.getReadableDatabase();

            try {
                //Log.d(TAG, "Trying to copy");
                // copy the mapping mobile number to circle, operator database
                copyDataBase();
                createTables();

            }
            catch (IOException e)
            {
                //V10Log.d(tag,"CopyingFailed Check Assets");//what did you screw up???
                //V10e.printStackTrace();
            }
            //Create TABLEs Here


        }
    }

    void createTables()
    {
        SQLiteDatabase db = this.getWritableDatabase();
       //V10Log.d(tag, "OnCreate =" + IbalanceContract.CREATE_CALL_LOG_TABLE);
        db.execSQL(IbalanceContract.CREATE_CALL_LOG_TABLE);

        // create books table
        //Log.d("databse", CREATE_CALL_TABLE);
        db.execSQL(IbalanceContract.CREATE_CALL_TABLE);
        //Log.d("databse", "executed " + CREATE_CALL_TABLE);

        db.execSQL(IbalanceContract.CREATE_DATE_DURATION_TABLE);

        db.execSQL(IbalanceContract.CREATE_CONTACT_DETAIL_TABLE);

        db.execSQL(IbalanceContract.CREATE_PACK_CALL_TABLE);
        //Log.d("databse", "executed " + CREATE_VOICE_PACK_TABLE);

        db.execSQL(IbalanceContract.CREATE_SMS_TABLE);
        //Log.d("databse", "executed " + CREATE_SMS_TABLE);

        db.execSQL(IbalanceContract.CREATE_SMS_PACK_TABLE);
        //Log.d("databse", "executed " + CREATE_SMS_PACK_TABLE);

        db.execSQL(IbalanceContract.CREATE_DATA_TABLE);
        //Log.d("databse", "executed " + CREATE_DATA_TABLE);

        db.execSQL(IbalanceContract.CREATE_DATA_PACK_TABLE);
        //Log.d("databse", "executed " + CREATE_DATA_PACK_TABLE);

        db.execSQL(IbalanceContract.CREATE_RECHARGE_TABLE);
        //Log.d("databse", "executed " + CREATE_RECHARGE_TABLE);

        db.execSQL(IbalanceContract.CREATE_SUSPICIOUS_TABLE);
        //Log.d("databse", "executed " + CREATE_SUSPICIOUS_TABLE);
    }




    /* Check if the database already exist to avoid re-copying the file each
    * time you open the application.
    *
            * @return true if it exists, false if it doesn't
            */

    private boolean checkDataBase(String dbname) {
        boolean checkdb = false;
        try {
            String myPath = MyApplication.context.getDatabasePath(dbname).toString();
            File dbfile = new File(myPath);
            checkdb = dbfile.exists();
        } catch (SQLiteException e) {
           //V10System.out.println("Database doesn't exist");
        }

        return checkdb;
    }
    /*
	 * Copies  database from your local assets-folder to the just created
	 * empty database in the system folder, from where it can be accessed and
	 * handled. This is done by transfering bytestream.
	 */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void copyDataBase() throws IOException {
        //Log.d(TAG, "copy started");
        // Open your local db as the input stream
       //V10Log.d(tag,"copyDataBase");
        if(checkDataBase(OLD_DATABASE_NAME))
        {
            try
            {
                SQLiteDatabase.deleteDatabase(new File(MyApplication.context.getDatabasePath(OLD_DATABASE_NAME).toString()));

            }
            catch (Exception e)
            {

            }

        }
        InputStream myInput = MyApplication.context.getAssets().open(DATABASE_NAME);
        //Log.d(TAG, "assets  opened");
        // Path to the just created empty db
        String outFileName =MyApplication.context.getDatabasePath(DATABASE_NAME).toString();// DB_PATH

        if (sInstance != null)
            sInstance.close();

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


    @Override
    public synchronized SQLiteDatabase  getWritableDatabase() {
        return super.getWritableDatabase();
    }




    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db)
    {

    }


    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     * <p/>
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //V12Log.d(tag,"Oldversion = "+ oldVersion + "newVersion = "+ newVersion );
        if(oldVersion==2 && newVersion>2)
        {
            String tempTable = "temp_table";
            db.execSQL("ALTER TABLE " + IbalanceContract.CallEntry.TABLE_NAME + " RENAME TO " + tempTable);
            //V12Log.d(tag, "Call Entry Rename complete");
            db.execSQL(IbalanceContract.CREATE_CALL_TABLE);
            db.execSQL("INSERT INTO " + IbalanceContract.CallEntry.TABLE_NAME + " select * from " + tempTable);
            //V12Log.d(tag, "Call Entry Copy over complete");
            db.execSQL("DROP TABLE " + tempTable);
            //V12Log.d(tag, "TEMP  Call Entry DRop complete");
            db.execSQL("ALTER TABLE " + IbalanceContract.CallLogEntry.TABLE_NAME + " RENAME TO " + tempTable);
            //V12Log.d(tag, "Call LOG Entry Rename complete");
            db.execSQL(IbalanceContract.CREATE_CALL_LOG_TABLE);
            db.execSQL("INSERT INTO " + IbalanceContract.CallLogEntry.TABLE_NAME + "(" +
                    IbalanceContract.CallLogEntry.COLUMN_NAME_ID + " , " +
                    IbalanceContract.CallLogEntry.COLUMN_NAME_DATE + " , " +
                    IbalanceContract.CallLogEntry.COLUMN_NAME_TYPE + " , " +
                    IbalanceContract.CallLogEntry.COLUMN_NAME_DURATION + " , " +
                    IbalanceContract.CallLogEntry.COLUMN_NAME_SLOT + " , " +
                    IbalanceContract.CallLogEntry.COLUMN_NAME_NUMBER + " ) " +
                    " select  " +
                    "_ID , " +
                    IbalanceContract.CallLogEntry.COLUMN_NAME_DATE + " , " +
                    IbalanceContract.CallLogEntry.COLUMN_NAME_TYPE + " , " +
                    IbalanceContract.CallLogEntry.COLUMN_NAME_DURATION + " , " +
                    IbalanceContract.CallLogEntry.COLUMN_NAME_SLOT + " , " +
                    IbalanceContract.CallLogEntry.COLUMN_NAME_NUMBER + " " +
                    " from " + tempTable);
            //V12Log.d(tag, "Call LOG Entry CopyOver complete");
            //V12Log.d(tag, "Everything Successful");
        }
        if(newVersion==4)
        {

            db.execSQL(IbalanceContract.DROP_PACK_CALL_TABLE);
            db.execSQL(IbalanceContract.CREATE_PACK_CALL_TABLE);
            db.execSQL(IbalanceContract.DROP_SMS_TABLE);
            db.execSQL(IbalanceContract.CREATE_SMS_TABLE);
            db.execSQL(IbalanceContract.DROP_SMS_PACK_TABLE);
            db.execSQL(IbalanceContract.CREATE_SMS_PACK_TABLE);
            db.execSQL(IbalanceContract.DROP_DATA_TABLE);
            db.execSQL(IbalanceContract.CREATE_DATA_TABLE);
            db.execSQL(IbalanceContract.DROP_DATA_PACK_TABLE);
            db.execSQL(IbalanceContract.CREATE_DATA_PACK_TABLE);
        }



    }
}
