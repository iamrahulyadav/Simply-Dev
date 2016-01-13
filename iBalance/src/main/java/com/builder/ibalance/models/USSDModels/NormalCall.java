package com.builder.ibalance.models.USSDModels;

import android.content.ContentValues;

import com.builder.ibalance.database.helpers.IbalanceContract;
import com.builder.ibalance.messages.OutgoingCallMessage;

/**
 * Created by Shabaz on 05-Jan-16.
 */
public class NormalCall extends USSDBase
{
    public float call_cost;
    public int call_duration,sim_slot;
    public String ph_number;
    public long id;

    public NormalCall()
    {
    }

    public void USSDDetails(long date,int USSD_TYPE,float main_bal,float call_cost,int call_duration,String original_message)
    {
        baseDetails(date,USSD_TYPE, main_bal,original_message);
        this.call_cost = call_cost;
        this.call_duration = call_duration;
    }

    public void USSDDetails(NormalCall ussDetails)
    {
        baseDetails(ussDetails.date,ussDetails.USSD_TYPE, ussDetails.main_bal,ussDetails.original_message);
        this.call_cost = ussDetails.call_cost;
        this.call_duration = ussDetails.call_duration;
    }
    public  NormalCall(PackCall packCallDetails)
    {
        baseDetails(packCallDetails.date,packCallDetails.USSD_TYPE, packCallDetails.main_bal,packCallDetails.original_message);
        this.call_cost = packCallDetails.pack_bal_left;
        if(this.call_cost<0)
            this.call_cost=0;
        this.call_duration = packCallDetails.call_duration;
        this.id = packCallDetails.id;
        this.ph_number = packCallDetails.ph_number;
        this.sim_slot= packCallDetails.sim_slot;
        //If duration not available then use
        if(call_duration<=0)
            call_duration = packCallDetails.call_duration;
    }
    public void setEventDetails(long id,String ph_number,int sim_slot)
    {
        this.id = id;
        this.ph_number = ph_number;
        this.sim_slot=sim_slot;
    }
    public void setEventDetails(OutgoingCallMessage tempCallEventDetails)
    {
        this.id = tempCallEventDetails.id;
        this.ph_number = tempCallEventDetails.lastNumber;
        this.sim_slot= tempCallEventDetails.sim_slot;
        //If duration not available then use
        if(call_duration<=0)
            call_duration = tempCallEventDetails.duration;
    }
    public ContentValues getEntryforDB()
    {
        ContentValues mContentValues = new ContentValues();
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
