package com.builder.ibalance.database;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

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

// TODO : Move the MysqliteHeper tp Database Manager
public class DatabaseManager extends SQLiteOpenHelper {
    // The Android's default system path of your application database.
    final static String tag = DatabaseManager.class.getSimpleName();
    private final static String DATABASE_NAME = "simply.db";
    private final static String OLD_DATABASE_NAME = "ibalance.db";
    private static final int DATABASE_VERSION = 2;
    private static DatabaseManager sInstance;

    public static synchronized DatabaseManager getInstance() {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DatabaseManager(MyApplication.context.getApplicationContext());
        }
        return sInstance;
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

        db.execSQL(IbalanceContract.CREATE_VOICE_PACK_TABLE);
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
    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static method "getInstance()" instead.
     */
    private DatabaseManager(Context context) {

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

    /**
     * Create and/or open a database that will be used for reading and writing.
     * The first time this is called, the database will be opened and
     * {@link #onCreate}, {@link #onUpgrade} and/or {@link #onOpen} will be
     * called.
     * <p/>
     * <p>Once opened successfully, the database is cached, so you can
     * call this method every time you need to write to the database.
     * (Make sure to call {@link #close} when you no longer need the database.)
     * Errors such as bad permissions or a full disk may cause this method
     * to fail, but future attempts may succeed if the problem is fixed.</p>
     * <p/>
     * <p class="caution">Database upgrade may take a long time, you
     * should not call this method from the application main thread, including
     * from {@link ContentProvider#onCreate ContentProvider.onCreate()}.
     *
     * @return a read/write database object valid until {@link #close} is called
     * @throws SQLiteException if the database cannot be opened for writing
     */
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
        /*Cursor c = db.rawQuery("SELECT * FROM CALL LIMIT 500",null);
        ArrayList<NormalCall> tempList = new ArrayList<>();
        //using only one object for optimisation
        NormalCall tempNormalCall = new NormalCall(0l,0.0f,0.0f,0,"");
        *//*+ "_id INTEGER PRIMARY KEY AUTOINCREMENT , " + "DATE INTEGER  , "
                + "COST FLOAT, "  + "DURATION INTEGER, "
                + "NUMBER TEXT, "+ "BALANCE FLOAT, " +"MESSAGE TEXT"+" )";*//*
        int date_idx = c.getColumnIndex("DATE"),
                cost_idx = c.getColumnIndex("COST"),
                dur_idx = c.getColumnIndex("DURATION"),
                num_idx = c.getColumnIndex("NUMBER"),
                bal_idx = c.getColumnIndex("BALANCE"),
                msg_idx = c.getColumnIndex("MESSAGE");
        Collections.reverse(tempList);
        while(c.moveToNext())
        {
            tempList.add(new NormalCall(
                    c.getLong(date_idx),
                    0,
                    c.getFloat(cost_idx),
                    c.getFloat(bal_idx),
                    c.getInt(dur_idx),
                    c.getString(num_idx),
                    c.getString(msg_idx)
            ));
        }
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
        BalanceHelper mBalanceHelper = new BalanceHelper();
        for(NormalCall temp:tempList)
        {
          mBalanceHelper.addEntry(temp);
        }*/


    }
}
