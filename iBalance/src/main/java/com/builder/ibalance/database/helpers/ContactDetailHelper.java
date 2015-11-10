package com.builder.ibalance.database.helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.builder.ibalance.database.DatabaseManager;
import com.builder.ibalance.database.MappingHelper;
import com.builder.ibalance.database.models.ContactDetailModel;
import com.builder.ibalance.datainitializers.DataInitializer;
import com.builder.ibalance.util.MyApplication;

import java.sql.SQLException;
import java.util.ArrayList;

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
        mSqlDB = DatabaseManager.getInstance().getReadableDatabase();
    }

    public Cursor getAllContacts()
    {
        Cursor c = mSqlDB.query(
                IbalanceContract.ContactDetailEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);
        return c;
    }
    public ContactDetailModel getContactDetail(String phNumber) throws SQLException
    {

        Cursor c = mSqlDB.rawQuery("SELECT " + IbalanceContract.ContactDetailEntry.COLUMN_NAME_NAME + "," +
                IbalanceContract.ContactDetailEntry.COLUMN_NAME_CARRIER + "," +
                IbalanceContract.ContactDetailEntry.COLUMN_NAME_CIRCLE + " FROM " +
                IbalanceContract.ContactDetailEntry.TABLE_NAME + " WHERE " +
                IbalanceContract.ContactDetailEntry.COLUMN_NAME_NUMBER + " = \"" + phNumber + "\"", null);
        ContactDetailModel m ;
        if(c.moveToFirst())
        {
            m = new ContactDetailModel(phNumber,c.getString(0),c.getString(1),c.getString(2));
        }
        else
        {
            m = new ContactDetailModel(phNumber,phNumber,"Unknown","Unknown",0);
        }
        c.close();
        return m;
    }

    public ContactDetailModel getPopUpDetails(String phNumber)
    {
        if (phNumber.startsWith("+91"))
        {
            phNumber = phNumber.substring(3);
        }
        if(phNumber.startsWith("0"))
        {
            phNumber = phNumber.substring(1);
        }
        phNumber = phNumber.replaceAll(" ","");
        phNumber = phNumber.replaceAll("-", "");
        float call_rate = MyApplication.context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE).getFloat("CALL_RATE",1.7f);
        Cursor c = mSqlDB.rawQuery("SELECT "+IbalanceContract.ContactDetailEntry.COLUMN_NAME_NAME+","+
                IbalanceContract.ContactDetailEntry.COLUMN_NAME_IMAGE_URI+","+
                IbalanceContract.ContactDetailEntry.COLUMN_NAME_CARRIER+","+
                IbalanceContract.ContactDetailEntry.COLUMN_NAME_CIRCLE+","+
                IbalanceContract.ContactDetailEntry.COLUMN_NAME_OUT_DURATION +" FROM " +
                IbalanceContract.ContactDetailEntry.TABLE_NAME + " WHERE " +
                IbalanceContract.ContactDetailEntry.COLUMN_NAME_NUMBER + " = \"" + phNumber + "\"", null);
        ContactDetailModel m;
        if(c.moveToFirst())
        {
            String query = "select sum(COST), sum(DURATION) from CALL where  NUMBER =\'" + phNumber + "\'";
            Cursor c1 = mSqlDB.rawQuery(query,null);
            float callCost = (float) 0.0;
            int duration = 0;
            if(c1.moveToFirst())
            {
                try
                {
                    callCost = c1.getFloat(0);
                    duration = c1.getInt(1);
                } catch (Exception e)
                {
                   //V10e.printStackTrace();
                }
                finally
                {
                    c1.close();
                }
            }

           float total_duration = c.getInt(c.getColumnIndex(IbalanceContract.ContactDetailEntry.COLUMN_NAME_OUT_DURATION));
            float total_cost = callCost + ((total_duration-duration) * call_rate)/100;
            m = new ContactDetailModel(
                    c.getString(c.getColumnIndex(IbalanceContract.ContactDetailEntry.COLUMN_NAME_NAME)),
                    c.getString(c.getColumnIndex(IbalanceContract.ContactDetailEntry.COLUMN_NAME_CARRIER)),
                    c.getString(c.getColumnIndex(IbalanceContract.ContactDetailEntry.COLUMN_NAME_CIRCLE)),
                    c.getString(c.getColumnIndex(IbalanceContract.ContactDetailEntry.COLUMN_NAME_IMAGE_URI)),
                    total_cost
            );
        return m;
        }
        ArrayList<String> carrier_circle = new ArrayList<>();
        carrier_circle.add("Unknown");
        carrier_circle.add("Unknown");
        if(phNumber.length()>=10)
        {
            try
            {
                carrier_circle = (new MappingHelper()).
                        getMapping(Integer.parseInt(
                                phNumber.substring(
                                        phNumber.length() - 10,
                                        phNumber.length() - 6)));
                if(DataInitializer.carriers==null)
                {
                    DataInitializer.InitializeData();
                }
                carrier_circle.add(0,DataInitializer.carriers.get(carrier_circle.get(0)));
                carrier_circle.add(1,DataInitializer.circle.get(carrier_circle.get(1)));
            }
            catch (Exception e)
            {
               
            }
        }
        return new ContactDetailModel(phNumber,carrier_circle.get(0),carrier_circle.get(1),null,0.0f);
    }

    public ContactDetailModel getDetailsforRecentList(String phNumber)
    {
        if(phNumber!=null)
        {
            if (phNumber.startsWith("+91"))
            {
                phNumber = phNumber.substring(3);
            }
            if (phNumber.startsWith("0"))
            {
                phNumber = phNumber.substring(1);
            }
            phNumber = phNumber.replaceAll(" ", "").replaceAll("-", "");
            Cursor c = mSqlDB.rawQuery("SELECT " + IbalanceContract.ContactDetailEntry.COLUMN_NAME_NAME + "," +
                    IbalanceContract.ContactDetailEntry.COLUMN_NAME_IMAGE_URI + "," +
                    IbalanceContract.ContactDetailEntry.COLUMN_NAME_CARRIER + "," +
                    IbalanceContract.ContactDetailEntry.COLUMN_NAME_CIRCLE + " FROM " +
                    IbalanceContract.ContactDetailEntry.TABLE_NAME + " WHERE " +
                    IbalanceContract.ContactDetailEntry.COLUMN_NAME_NUMBER + " = \"" + phNumber + "\"", null);
            ContactDetailModel m;
            if (c.moveToFirst())
            {


                m = new ContactDetailModel(
                        c.getString(c.getColumnIndex(IbalanceContract.ContactDetailEntry.COLUMN_NAME_NAME)),
                        c.getString(c.getColumnIndex(IbalanceContract.ContactDetailEntry.COLUMN_NAME_CARRIER)),
                        c.getString(c.getColumnIndex(IbalanceContract.ContactDetailEntry.COLUMN_NAME_CIRCLE)),
                        c.getString(c.getColumnIndex(IbalanceContract.ContactDetailEntry.COLUMN_NAME_IMAGE_URI)),
                        0.0f
                );
                return m;
            }
        }
        return new ContactDetailModel(phNumber,"Unknown","Unknown",null,0.0f);
    }
    //Returns in  secs
    public int getOutDurationLocalSameCarrier(String sim_circle, String sim_carrier)
    {
        int total_duration = 0;
        Cursor c = mSqlDB.rawQuery("SELECT SUM("
                +IbalanceContract.ContactDetailEntry.COLUMN_NAME_OUT_DURATION +") FROM " +
                IbalanceContract.ContactDetailEntry.TABLE_NAME + " WHERE " +
                IbalanceContract.ContactDetailEntry.COLUMN_NAME_CIRCLE + " = \"" + sim_circle + "\" "
                +" AND "
                +IbalanceContract.ContactDetailEntry.COLUMN_NAME_CARRIER + " = \"" + sim_carrier + "\""
                , null);
        if(c.moveToFirst())
        {
            total_duration = c.getInt(0);
        }
        c.close();
        return total_duration;
    }

    public int getOutDurationLocalDiffCarrier(String sim_circle, String sim_carrier)
    {
        int total_duration = 0;
        Cursor c = mSqlDB.rawQuery("SELECT SUM("
                +IbalanceContract.ContactDetailEntry.COLUMN_NAME_OUT_DURATION +") FROM " +
                IbalanceContract.ContactDetailEntry.TABLE_NAME + " WHERE " +
                IbalanceContract.ContactDetailEntry.COLUMN_NAME_CIRCLE + " = \"" + sim_circle + "\" "
                +" AND "
                +IbalanceContract.ContactDetailEntry.COLUMN_NAME_CARRIER + " != \"" + sim_carrier + "\""
                , null);
        if(c.moveToFirst())
        {
            total_duration = c.getInt(0);
        }
        c.close();
        return total_duration;
    }

    public int getOutDurationSTDSameCarrier(String sim_circle, String sim_carrier)
    {
        int total_duration = 0;
        Cursor c = mSqlDB.rawQuery("SELECT SUM("
                +IbalanceContract.ContactDetailEntry.COLUMN_NAME_OUT_DURATION +") FROM " +
                IbalanceContract.ContactDetailEntry.TABLE_NAME + " WHERE " +
                IbalanceContract.ContactDetailEntry.COLUMN_NAME_CIRCLE + " != \"" + sim_circle + "\" "
                +" AND "
                +IbalanceContract.ContactDetailEntry.COLUMN_NAME_CARRIER + " = \"" + sim_carrier + "\""
                , null);
        if(c.moveToFirst())
        {
            total_duration = c.getInt(0);
        }
        c.close();
        return total_duration;
    }

    public int getOutDurationSTDDiffCarrier(String sim_circle, String sim_carrier)
    {
        int total_duration = 0;
        Cursor c = mSqlDB.rawQuery("SELECT SUM("
                +IbalanceContract.ContactDetailEntry.COLUMN_NAME_OUT_DURATION +") FROM " +
                IbalanceContract.ContactDetailEntry.TABLE_NAME + " WHERE " +
                IbalanceContract.ContactDetailEntry.COLUMN_NAME_CIRCLE + " != \"" + sim_circle + "\" "
                +" AND "
                +IbalanceContract.ContactDetailEntry.COLUMN_NAME_CARRIER + " != \"" + sim_carrier + "\""
                , null);
        if(c.moveToFirst())
        {
            total_duration = c.getInt(0);
        }
        c.close();
        return total_duration;
    }

    public String getName(String phNumber)
    {
        String name = "Unknown";
        if (phNumber.startsWith("+91"))
        {
            phNumber = phNumber.substring(3);
        }
        if(phNumber.startsWith("0"))
        {
            phNumber = phNumber.substring(1);
        }
        phNumber = phNumber.replaceAll(" ","");
        phNumber = phNumber.replaceAll("-", "");
        Cursor c = mSqlDB.rawQuery("SELECT "+IbalanceContract.ContactDetailEntry.COLUMN_NAME_NAME+" FROM " +
                IbalanceContract.ContactDetailEntry.TABLE_NAME + " WHERE " +
                IbalanceContract.ContactDetailEntry.COLUMN_NAME_NUMBER + " = \"" + phNumber + "\"", null);
        if(c.moveToFirst())
        {
            name = c.getString(0);
        }
        c.close();
        return name;
    }
}
