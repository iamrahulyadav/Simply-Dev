package com.builder.ibalance.models.USSDModels;

/**
 * Created by Shabaz on 05-Jan-16.
 */
public class PackCall extends USSDBase
{
    public float  pack_bal_used, pack_bal_left;
    public int call_duration, sim_slot, pack_duration_used, pack_duration_left;
    public String ph_number, used_metric, left_metric,validity;
    public long id;
    public String pack_name;

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

    public void eventDetails(long id, String ph_number, int sim_slot)
    {
        this.id = id;
        this.ph_number = ph_number;
        this.sim_slot = sim_slot;
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
