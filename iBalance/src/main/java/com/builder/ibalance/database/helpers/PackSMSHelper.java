package com.builder.ibalance.database.helpers;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.builder.ibalance.database.DatabaseManager;
import com.builder.ibalance.models.USSDModels.PackSMS;
import com.builder.ibalance.util.Helper;

/**
 * Created by Shabaz on 08-Jan-16.
 */
public class PackSMSHelper
{
    final String TAG = PackCallHelper.class.getSimpleName();
    DatabaseManager mMySQLiteHelper;
    public PackSMSHelper() {
        mMySQLiteHelper = DatabaseManager.getInstance();
    }

    public void addEntry(PackSMS entry)
    {
        /*    public static abstract class SMSPackEntry  {
        public static final String TABLE_NAME = "SMS_PACK";
        public static final String COLUMN_NAME_ID = "_id";
        public static final String COLUMN_NAME_USED_SMS = "USED";
        public static final String COLUMN_NAME_PACK_TYPE = "PACK_TYPE";
        public static final String COLUMN_NAME_REMAINING = "REMAINING";
        public static final String COLUMN_NAME_VALIDITY = "VALIDITY";*/
        // 1. get reference to writable DB
        SQLiteDatabase db = mMySQLiteHelper.getWritableDatabase();
        String phNumber = entry.ph_number;
        phNumber = Helper.normalizeNumber(phNumber);
        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(IbalanceContract.SMSPackEntry.COLUMN_NAME_ID, entry.id); // Id which matches the call log id
        values.put(IbalanceContract.SMSPackEntry.COLUMN_NAME_PACK_TYPE, entry.pack_type);
        values.put(IbalanceContract.SMSPackEntry.COLUMN_NAME_USED_SMS,entry.used_sms);
        values.put(IbalanceContract.SMSPackEntry.COLUMN_NAME_REMAINING, entry.rem_sms);
        values.put(IbalanceContract.SMSPackEntry.COLUMN_NAME_VALIDITY, entry.validity);
        Log.d(TAG,"Db Details "+ values.toString());
        // 3. insert
        db.insert(IbalanceContract.SMSPackEntry.TABLE_NAME, // table
                null, // nullColumnHack
                values); // key/value -> keys = column names/ values = column
        // values
    }
}
