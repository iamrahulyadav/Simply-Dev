package com.builder.ibalance.models.USSDModels;

import android.util.Log;

import com.builder.ibalance.messages.OutgoingSmsMessage;
import com.builder.ibalance.util.Helper;
import com.crashlytics.android.Crashlytics;
import com.parse.ParseObject;

/**
 * Created by Shabaz on 05-Jan-16.
 */
public class NormalSMS extends USSDBase
{
    public float cost;
    public long id;
    public int sim_slot;
    public String ph_number;

    public NormalSMS() {}
    public NormalSMS(PackSMS ussdDetails)
    {
        baseDetails(ussdDetails.date,ussdDetails.USSD_TYPE, ussdDetails.main_bal,ussdDetails.original_message);
        this.id = ussdDetails.id;
        this.cost = 0.0f;
        this.ph_number = ussdDetails.ph_number;
        this.sim_slot = ussdDetails.sim_slot;
    }
    public ParseObject logDetails()
    {
        ParseObject p = new ParseObject("NORMAL_SMS_VALID");
        try
        {
            p.put("MESSAGE", this.original_message);
            p.put("COST", this.cost);
            p.put("MAIN_BAL", this.main_bal);
            p.put("SLOT", this.sim_slot);
            p.put("HASH", Helper.shift(this.ph_number));
        }
        catch (Exception e)
        {
            //This should not happen
            Log.wtf("NORMAL_SMS","Apocalypse Arriving: The Details had a error");
            Crashlytics.logException(e);
        }
        return p;
    }
    public void USSDDetails(long date,int USSD_TYPE,float main_bal, float cost,String original_message)
    {
        baseDetails(date, USSD_TYPE, main_bal,original_message);
        this.cost = cost;
    }

    public void eventDetails(OutgoingSmsMessage mDetails)
    {
        this.ph_number = mDetails.lastNumber;
        this.id = mDetails.id;
        this.sim_slot = mDetails.sim_slot;
    }

    public void eventDetails(String ph_number,long id,int sim_slot)
    {
        //This is as of now optional
        eventDetails(ph_number);
        this.id = id;
        this.sim_slot = sim_slot;
    }
    public void eventDetails(String ph_number)
    {
        this.ph_number = ph_number;
    }

    @Override
    public String toString()
    {
        return "NormalSMS{" +
                "cost=" + cost +
                ", id=" + id +
                ", sim_slot=" + sim_slot +
                ", ph_number='" + ph_number + '\'' +
                "} " + super.toString();
    }

}
