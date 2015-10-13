package com.builder.ibalance.database.helpers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.builder.ibalance.database.DatabaseManager;
import com.builder.ibalance.database.models.DateDurationModel;

import java.util.ArrayList;
import java.util.Date;

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


 public ArrayList<DateDurationModel> getCallPatterDetails(long startDate, long endDate)
 {
     Log.d(tag,"Querying from "+ (new Date(startDate)).toString() +" to "+(new Date(endDate)).toString());
  ArrayList<DateDurationModel> avgDateDurationDetail = new ArrayList<>();
     String query = "SELECT AVG(" + IbalanceContract.DateDurationEntry.COLUMN_NAME_IN_COUNT
             + ") , AVG ("+IbalanceContract.DateDurationEntry.COLUMN_NAME_IN_DURATION
             + ") , AVG (" + IbalanceContract.DateDurationEntry.COLUMN_NAME_OUT_COUNT
             + ") , AVG (" + IbalanceContract.DateDurationEntry.COLUMN_NAME_OUT_DURATION
             + ") , " + IbalanceContract.DateDurationEntry.COLUMN_NAME_WEEK_DAY
             +" FROM " + IbalanceContract.DateDurationEntry.TABLE_NAME
             +" WHERE "+ IbalanceContract.DateDurationEntry.COLUMN_NAME_DATE +">="+startDate
             +" AND "+ IbalanceContract.DateDurationEntry.COLUMN_NAME_DATE +"<="+endDate
             + " GROUP BY " + IbalanceContract.DateDurationEntry.COLUMN_NAME_WEEK_DAY;
     Log.d(tag,"Query = "+query);
  Cursor c = mSqlDB.rawQuery(query, null);

  DateDurationModel temp;
    //0 - in_count 1 - inDuration secs 2- out_c 3-out_d secs 4-day_of_the_WEEK
     Log.d(tag,"Columns Returned = "+ c.getCount());
     StringBuilder builder = new StringBuilder();

  while(c.moveToNext())
  {
      builder.append(getDay(c.getInt(4))+": ");
      builder.append("In Count "+": "+c.getFloat(0));
      builder.append("In Duration "+": "+c.getFloat(1));
      builder.append("Out Count "+": "+c.getFloat(2));
      builder.append("Out Duration "+": "+c.getFloat(3));
      builder.append("\n");
      temp = new DateDurationModel(
              (int)Math.ceil(c.getInt(0)),
              (int)Math.ceil(c.getFloat(1)/60),
              (int)Math.ceil(c.getInt(2)),
              (int)Math.ceil(c.getFloat(3)/60),
              c.getInt(4));
      avgDateDurationDetail.add(temp);
  }
     Log.d(tag, builder.toString());
     c.close();
  return avgDateDurationDetail;
 }
    String getDay(int i)
    {
        switch (i)
        {
            case 1: return "Sun";
            case 2: return "Mon";
            case 3: return "Tue";
            case 4: return "Wed";
            case 5: return "Thu";
            case 6: return "Fri";
            case 7: return "Sat";
            default: return "N/A";
        }
    }
}
