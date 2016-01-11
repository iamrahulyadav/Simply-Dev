package com.builder.ibalance.database.helpers;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.builder.ibalance.database.DatabaseManager;
import com.builder.ibalance.models.USSDModels.PackCall;
import com.builder.ibalance.util.Helper;

/**
 * Created by Shabaz on 06-Jan-16.
 */
public class PackCallHelper
{
    final String TAG = PackCallHelper.class.getSimpleName();
    DatabaseManager mMySQLiteHelper;
    public PackCallHelper() {
        mMySQLiteHelper = DatabaseManager.getInstance();
    }

    public void addEntry(PackCall entry)
    {
        /*        public static final String TABLE_NAME = "PACK_CALL";
        public static final String COLUMN_NAME_ID = "_id";
        public static final String COLUMN_NAME_PACK_NAME = "PACK_NAME";
        public static final String COLUMN_NAME_DURATION_USED = "DURATION_USED";
        public static final String COLUMN_NAME_DURATION_LEFT = "DURATION_LEFT";
        public static final String COLUMN_NAME_DURATION_USED_METRIC = "DURATION_USED_METRIC";
        public static final String COLUMN_NAME_DURATION_LEFT_METRIC  = "DURATION_LEFT_METRIC";
        public static final String COLUMN_NAME_PACK_BAL_LEFT = "PACK_BAL_LEFT";
        public static final String COLUMN_NAME_VALIDITY = "VALIDITY";*/
        // 1. get reference to writable DB
        SQLiteDatabase db = mMySQLiteHelper.getWritableDatabase();
        String phNumber = entry.ph_number;
        phNumber = Helper.normalizeNumber(phNumber);
        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(IbalanceContract.PackCallEntry.COLUMN_NAME_ID, entry.id); // Id which matches the call log id
        values.put(IbalanceContract.PackCallEntry.COLUMN_NAME_PACK_NAME, entry.pack_name);
        values.put(IbalanceContract.PackCallEntry.COLUMN_NAME_DURATION_USED,entry.pack_duration_used);
        values.put(IbalanceContract.PackCallEntry.COLUMN_NAME_DURATION_LEFT, entry.pack_duration_left);
        values.put(IbalanceContract.PackCallEntry.COLUMN_NAME_DURATION_USED_METRIC, entry.used_metric);
        values.put(IbalanceContract.PackCallEntry.COLUMN_NAME_DURATION_LEFT_METRIC, entry.left_metric);
        values.put(IbalanceContract.PackCallEntry.COLUMN_NAME_PACK_BAL_LEFT, entry.pack_bal_left);
        values.put(IbalanceContract.PackCallEntry.COLUMN_NAME_VALIDITY, entry.validity);
       //V16Log.d(TAG,"Db Details "+ values.toString());
        // 3. insert
        db.insert(IbalanceContract.PackCallEntry.TABLE_NAME, // table
                null, // nullColumnHack
                values); // key/value -> keys = column names/ values = column
        // values
    }
}
