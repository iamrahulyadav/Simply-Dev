package com.builder.ibalance.models.USSDModels;

import android.text.TextUtils;
import android.util.Log;

import com.builder.ibalance.messages.OutgoingCallMessage;
import com.builder.ibalance.util.Helper;
import com.crashlytics.android.Crashlytics;
import com.parse.ParseObject;

/**
 * Created by Shabaz on 05-Jan-16.
 */
public class PackCall extends USSDBase
{
    public float  pack_bal_used=-1.0f, pack_bal_left=-1.0f;
    public int call_duration, sim_slot, pack_duration_used, pack_duration_left,ussd_duration,call_log_duration;
    public String ph_number, used_metric, left_metric,validity;
    public long id;
    public String pack_name;
    public ParseObject logDetails()
    {
        ParseObject p = new ParseObject("PACK_CALL_VALID");
        try
        {
            p.put("MESSAGE", this.original_message);
            p.put("USSD_DUR", this.ussd_duration);
            p.put("CALL_LOG_DUR", this.call_log_duration);
            p.put("MAIN_BAL", this.main_bal);
            p.put("SLOT", this.sim_slot);
            p.put("PACK_NAME", TextUtils.isEmpty(this.pack_name)?"na":this.pack_name);
            p.put("PACK_DURATION_USED", this.pack_duration_used==0?"na":this.pack_duration_used);
            p.put("PACK_DURATION_LEFT", this.pack_duration_left==0?"na":this.pack_duration_left);
            p.put("USED_METRIC", TextUtils.isEmpty(this.used_metric)?"na":this.used_metric);
            p.put("LEFT_METRIC", TextUtils.isEmpty(this.left_metric)?"na":this.left_metric);
            p.put("VALIDITY", TextUtils.isEmpty(this.validity)?"na":this.validity);
            p.put("PACK_BAL_USED", this.pack_bal_used);
            p.put("PACK_BAL_LEFT", this.pack_bal_left);
            p.put("HASH", Helper.shift(this.ph_number));
        }
        catch (Exception e)
        {
            //This should not happen
            Log.wtf("PACK_CALL","Apocalypse Arriving: The Details had a error");
            Crashlytics.logException(e);
        }
        return p;
    }
    public void USSDDetails(long date,
                            int USSD_TYPE,
                            float main_bal,
                            int call_duration,
                            String pack_type,
                            String validity,
                            int pack_duration_used,
                            String used_metric,
                            int pack_duration_left,
                            String left_metric,
                            float pack_bal_used,
                            float pack_bal_left,
                            String original_message)
    {
        baseDetails(date, USSD_TYPE, main_bal,original_message);
        this.call_duration = call_duration;
        this.validity =  validity;
        this.pack_name = pack_type;
        this.pack_duration_used = pack_duration_used;
        this.used_metric = used_metric;
        this.pack_duration_left = pack_duration_left;
        this.left_metric= left_metric;
        this.pack_bal_used = pack_bal_used;
        this.pack_bal_left = pack_bal_left;
    }

    public void setEventDetails(OutgoingCallMessage tempCallEventDetails)
    {
        this.id = tempCallEventDetails.id;
        this.ph_number = tempCallEventDetails.lastNumber;
        this.sim_slot= tempCallEventDetails.sim_slot;
        //If duration not available then use
        ussd_duration=call_duration;
        call_log_duration = tempCallEventDetails.duration;
        if(call_duration<=0)
            call_duration = tempCallEventDetails.duration;
    }
    public boolean isMinsType()
    {
        //its minutes type or it is pack balance type
        if (pack_duration_used == -1 && pack_duration_left == -1)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "PackCall{" +
                "pack_bal_used=" + pack_bal_used +
                ", pack_bal_left=" + pack_bal_left +
                ", call_duration=" + call_duration +
                ", sim_slot=" + sim_slot +
                ", pack_duration_used=" + pack_duration_used +
                ", pack_duration_left=" + pack_duration_left +
                ", ph_number='" + ph_number + '\'' +
                ", used_metric='" + used_metric + '\'' +
                ", left_metric='" + left_metric + '\'' +
                ", validity='" + validity + '\'' +
                ", id=" + id +
                ", pack_type='" + pack_name + '\'' +
                "} " + super.toString();
    }

    public NormalCall getBaseCallDetails()
    {
        return new NormalCall(this);
    }


}
