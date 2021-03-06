package com.builder.ibalance.database.helpers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.builder.ibalance.database.DatabaseManager;

import java.util.ArrayList;

public class MappingHelper
{
    final String tag = MappingHelper.class.getSimpleName();
    SQLiteDatabase myDB;

    public MappingHelper()
    {
        //Log.d(tag, "Creating Databse");
        myDB = DatabaseManager.getInstance().getReadableDatabase();
    }

    public ArrayList<String> getMapping(String number)
    {
        int first4Digits = 9972;
        if (number.length() >= 10)
        {
            first4Digits = Integer.parseInt(number.substring(number.length() - 10, number.length() - 6));
        }
        return getMapping(first4Digits);
    }

    public ArrayList<String> getMapping(int number)
    {

        ArrayList<String> temp = new ArrayList<String>();
        if (myDB == null)
        {
            //Log.d(tag, "cant open");
            temp.add("Unknown");
            temp.add("Unknown");
            return temp;

        } else
        {
            String query = "SELECT * FROM NUMBER_MAPPING WHERE _id = \"" + number + "\"";

            Cursor cursor = myDB.rawQuery(query, null);
            try
            {
                if (cursor.moveToFirst() == false)
                {
                    temp.add("Unknown");
                    temp.add("Unknown");
                    return temp;

                } else
                {
                    temp.add(cursor.getString(1));
                    temp.add(cursor.getString(2));

                }

            } catch (Exception e)
            {
                //V10e.printStackTrace();
            } finally
            {
                cursor.close();
            }
            return temp;
        }
    }


}
