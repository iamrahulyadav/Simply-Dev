package com.builder.ibalance.database.helpers;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.builder.ibalance.database.DatabaseManager;
import com.builder.ibalance.models.USSDModels.PackData;

/**
 * Created by Shabaz on 08-Jan-16.
 */
public class PackDataHelper
{
    final String TAG = PackDataHelper.class.getSimpleName();
    DatabaseManager mMySQLiteHelper;
    public PackDataHelper() {
        mMySQLiteHelper = DatabaseManager.getInstance();
    }

    public void addEntry(PackData entry)
    {
        // 1. get reference to writable DB
        SQLiteDatabase db = mMySQLiteHelper.getWritableDatabase();
        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        //Id is auto increment
        values.put(IbalanceContract.DataPackEntry.COLUMN_NAME_DATE, entry.date);
        values.put(IbalanceContract.DataPackEntry.COLUMN_NAME_DATA_CONSUMED, entry.data_used);
        values.put(IbalanceContract.DataPackEntry.COLUMN_NAME_DATA_LEFT,entry.data_left);
        values.put(IbalanceContract.DataPackEntry.COLUMN_NAME_BALANCE, entry.main_bal);
        values.put(IbalanceContract.DataPackEntry.COLUMN_NAME_TYPE, entry.pack_type);
        values.put(IbalanceContract.DataPackEntry.COLUMN_NAME_VALIDITY, entry.validity);
        values.put(IbalanceContract.DataPackEntry.COLUMN_NAME_SLOT, entry.sim_slot);
        values.put(IbalanceContract.DataPackEntry.COLUMN_NAME_MESSAGE, entry.original_message);
       //V16Log.d(TAG,"Db Details "+ values.toString());
        // 3. insert
        db.insert(IbalanceContract.DataPackEntry.TABLE_NAME, // table
                null, // nullColumnHack
                values); // key/value -> keys = column names/ values = column
        // values
    }
}
