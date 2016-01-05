package com.builder.ibalance.models.USSDModels;

/**
 * Created by Shabaz on 05-Jan-16.
 */
public class NormalSMS extends USSDBase
{
    float cost;
    long id;
    int sim_slot;
    String ph_number;

    public void USSDDetails(long date,int USSD_TYPE,float main_bal, float cost,String original_message)
    {
        baseDetails(date, USSD_TYPE, main_bal,original_message);
        this.cost = cost;
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
