package com.builder.ibalance.models.USSDModels;

import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.parse.ParseObject;

/**
 * Created by Shabaz on 05-Jan-16.
 */
public class PackData extends USSDBase
{
    public float data_used,data_left;
    public int sim_slot;
    public String validity,pack_type;
    //details = new DataPack((new Date()).getTime(),
    // dataUsed, dataLeft, mainBal, validity, message);

    public void USSDDetails(long date,
                             int USSD_TYPE,
                             float main_bal,
                             String pack_type,
                             String validity,
                             float data_used,
                             float data_left,
                             String original_message)
    {
        baseDetails(date, USSD_TYPE, main_bal, original_message);
        this.data_used = data_used;
        this.data_left = data_left;
        this.pack_type = pack_type;
        this.validity = validity;
    }
    public ParseObject logDetails()
    {
        ParseObject p = new ParseObject("NORMAL_DATA_VALID");
        try
        {
            p.put("MESSAGE", this.original_message);
            p.put("DATA_USED", this.data_used);
            p.put("DATA_LEFT", this.data_left);
            p.put("PACK_TYPE", TextUtils.isEmpty(this.pack_type)?"na":this.pack_type);
            p.put("VALIDITY", TextUtils.isEmpty(this.validity)?"na":this.validity);
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
        return "PackData{" +
                "data_used=" + data_used +
                ", data_left=" + data_left +
                ", sim_slot=" + sim_slot +
                ", validity='" + validity + '\'' +
                ", pack_type='" + pack_type + '\'' +
                "} " + super.toString();
    }

}
