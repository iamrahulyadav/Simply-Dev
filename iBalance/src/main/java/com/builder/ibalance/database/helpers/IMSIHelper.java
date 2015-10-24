package com.builder.ibalance.database.helpers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.builder.ibalance.database.DatabaseManager;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;

/**
 * Created by Shabaz on 17-Aug-15.
 */
public class IMSIHelper
{
    final String tag = IMSIHelper.class.getSimpleName();
    SQLiteDatabase mSqlDB;
    Cursor mCursor;
    // Define a projection that specifies which columns from the database
// you will actually use after this query.
    String[] projection = {
            IbalanceContract.IMSIEntry.COLUMN_NAME_CARRIER,
            IbalanceContract.IMSIEntry.COLUMN_NAME_CIRCLE
    };
    String selection =IbalanceContract.IMSIEntry.COLUMN_NAME_IMSI +"= ?";



    public IMSIHelper()
    {
        mSqlDB = DatabaseManager.getInstance().getReadableDatabase();
    }

    public ArrayList<String> getMapping(String imsi_number)
    {

        if (mSqlDB == null)
        {
           //V10Log.d(tag, "mSqlDB is null");
            return null;
        } else
        {

            ArrayList<String> carrier_circle = null;
            Cursor c = null;
            try{
               c = mSqlDB.query(
                        IbalanceContract.IMSIEntry.TABLE_NAME,  // The table to query
                        projection,                               // The columns to return
                        selection,                                // The columns for the WHERE clause
                        new String[]{imsi_number},                  // The values for the WHERE clause
                        null,                                     // don't group the rows
                        null,                                     // don't filter by row groups
                        null                                 // The sort order
                );
                if(c.moveToFirst())
                {
                    carrier_circle = new ArrayList<>();
                    carrier_circle.add(c.getString(c.getColumnIndex(IbalanceContract.IMSIEntry.COLUMN_NAME_CARRIER)));
                    carrier_circle.add(c.getString(c.getColumnIndex(IbalanceContract.IMSIEntry.COLUMN_NAME_CIRCLE)));

                }
                else
                {
                    Crashlytics.log("Failed Mapping for "+imsi_number);
                }
            }
            catch (Exception e)
            {
                Crashlytics.logException(e);
                Crashlytics.log("Failed Mapping for with Exception "+imsi_number);
            }
            finally
            {
                if(c!=null)
                c.close();
                return carrier_circle;
            }
        }
    }

}
