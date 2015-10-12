package com.builder.ibalance.database.helpers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.builder.ibalance.database.DatabaseManager;
import com.builder.ibalance.database.models.DateDurationModel;

/**
 * Created by Shabaz on 03-Oct-15.
 */
public class DateDurationMapHelper
{
   //Day, InCount-0 Indur-1 OutCount-2 OutDur-3 misscall
   final String tag = DateDurationMapHelper.class.getSimpleName();
    SQLiteDatabase mSqlDB;
    Cursor mCursor;
    String[] projection = {
    };
    String selection = "";

    public DateDurationMapHelper()
    {
        mSqlDB = DatabaseManager.getInstance().getReadableDatabase();
    }
    public void insertData(DateDurationModel model)
    {
        //Model.tooString gives the executable Query String
        mSqlDB.execSQL(model.toString());
    }

}
