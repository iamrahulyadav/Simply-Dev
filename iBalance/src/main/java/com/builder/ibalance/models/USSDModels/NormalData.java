package com.builder.ibalance.models.USSDModels;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.parse.ParseObject;

/**
 * Created by Shabaz on 05-Jan-16.
 */
public class NormalData extends USSDBase
{
    public float cost;
    public float data_used;
    public int sim_slot;

    public void USSDDetails(long date,int USSD_TYPE,float main_bal, float cost,float data_used,String original_message)
    {
        baseDetails(date, USSD_TYPE, main_bal,original_message);
        this.cost = cost;
        this.data_used = data_used;
    }
    public ParseObject logDetails()
    {
        ParseObject p = new ParseObject("NORMAL_DATA_VALID");
        try
        {
            p.put("MESSAGE", this.original_message);
            p.put("COST", this.cost);
            p.put("DATA_USED", this.data_used);
            p.put("MAIN_BAL", this.main_bal);
            p.put("SLOT", this.sim_slot);
        }
        catch (Exception e)
        {
            //This should not happen
            Log.wtf("NORMAL_DATA_VALID","Apocalypse Arriving: The Details had a error");
            Crashlytics.logException(e);
        }
        return p;
    }
    public void eventDetails(int sim_slot)
    {
        this.sim_slot = sim_slot;
    }

    @Override
    public String toString()
    {
        return "NormalData{" +
                "cost=" + cost +
                ", data_used=" + data_used +
                ", sim_slot=" + sim_slot +
                '}' + super.toString();
    }

}
