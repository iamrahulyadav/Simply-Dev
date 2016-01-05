package com.builder.ibalance.models.USSDModels;

import android.content.ContentValues;

import com.builder.ibalance.database.helpers.IbalanceContract;

/**
 * Created by Shabaz on 05-Jan-16.
 */
public class NormalCall extends USSDBase
{
    float call_cost;
    int call_duration,sim_slot;
    String ph_number;
    long id;

    public void USSDDetails(long date,int USSD_TYPE,float main_bal,float call_cost,int call_duration,String original_message)
    {
        baseDetails(date,USSD_TYPE, main_bal,original_message);
        this.call_cost = call_cost;
        this.call_duration = call_duration;
    }
    public void eventDetails(long id,String ph_number,int sim_slot)
    {
        this.id = id;
        this.ph_number = ph_number;
        this.sim_slot=sim_slot;
    }

    public ContentValues getEntryforDB()
    {
        ContentValues mContentValues = new ContentValues();
        /*public static final String TABLE_NAME = "CALL";
        public static final String COLUMN_NAME_ID = "_id";
        public static final String COLUMN_NAME_SLOT = "SLOT";
        public static final String COLUMN_NAME_DATE= "DATE";
        public static final String COLUMN_NAME_COST = "COST";
        public static final String COLUMN_NAME_DURATION = "DURATION";
        public static final String COLUMN_NAME_NUMBER = "NUMBER";
        public static final String COLUMN_NAME_BALANCE = "BALANCE";
        public static final String COLUMN_NAME_MESSAGE = "MESSAGE";*/
        mContentValues.put(IbalanceContract.CallEntry.COLUMN_NAME_ID,id);
        mContentValues.put(IbalanceContract.CallEntry.COLUMN_NAME_COST,call_cost);
        mContentValues.put(IbalanceContract.CallEntry.COLUMN_NAME_SLOT,sim_slot);
        mContentValues.put(IbalanceContract.CallEntry.COLUMN_NAME_DATE,date);
        mContentValues.put(IbalanceContract.CallEntry.COLUMN_NAME_DURATION,call_duration);
        mContentValues.put(IbalanceContract.CallEntry.COLUMN_NAME_NUMBER,ph_number);
        mContentValues.put(IbalanceContract.CallEntry.COLUMN_NAME_BALANCE,main_bal);
        mContentValues.put(IbalanceContract.CallEntry.COLUMN_NAME_MESSAGE,original_message);
        return mContentValues;
    }

    @Override
    public String toString()
    {
        return "NormalCall{" +
                "call_cost=" + call_cost +
                ", call_duration=" + call_duration +
                ", sim_slot=" + sim_slot +
                ", ph_number='" + ph_number + '\'' +
                ", id=" + id +
                '}' + super.toString();
    }
}
