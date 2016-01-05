package com.builder.ibalance.models.USSDModels;

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
