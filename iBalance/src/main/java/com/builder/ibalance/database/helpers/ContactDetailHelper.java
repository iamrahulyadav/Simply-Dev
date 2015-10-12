package com.builder.ibalance.database.helpers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.builder.ibalance.database.DatabaseManager;
import com.builder.ibalance.database.models.ContactDetailModel;

/**
 * Created by Shabaz on 03-Oct-15.
 */
public class ContactDetailHelper
{
    //Number, name,InCount,InDur,OutCount,OutDur,MissCount,Provider,State,imageUri
    final String tag = ContactDetailHelper.class.getSimpleName();
    SQLiteDatabase mSqlDB;
    Cursor mCursor;
    String[] projection = {
    };
    String selection = "";

    public ContactDetailHelper()
    {
        mSqlDB = DatabaseManager.getInstance().getWritableDatabase();
    }
    public void insertData(ContactDetailModel model)
    {
        //Model.tooString gives the executable Query String
        mSqlDB.execSQL(model.toString());
    }
}
