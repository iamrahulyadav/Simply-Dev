package com.builder.ibalance.models.USSDModels;

import com.builder.ibalance.messages.OutgoingSmsMessage;

/**
 * Created by Shabaz on 05-Jan-16.
 */
public class PackSMS extends USSDBase
{
    //details = new NormalSMS(new Date().getTime(), cost, mainBal, "9972115447", message);
    public long id;
    public int sim_slot,used_sms,rem_sms;
    public String ph_number,validity,pack_type;

    public void USSDDetails(long date,
                            int USSD_TYPE,
                            float main_bal,
                            String pack_type,
                            String validity,
                            int used_sms,
                            int rem_sms,
                            String original_message)
    {
        baseDetails(date, USSD_TYPE, main_bal,original_message);
        this.pack_type = pack_type;
        this.validity = validity;
        this.used_sms = used_sms;
        this.rem_sms = rem_sms;

    }
    public void eventDetails(String ph_number,long id,int sim_slot)
    {
        //This is as of now optional
        eventDetails(ph_number);
        this.id = id;
        this.sim_slot = sim_slot;
    }
    public void eventDetails(OutgoingSmsMessage mDetails)
    {
        //This is as of now optional
        this.ph_number = mDetails.lastNumber;
        this.id = mDetails.id;
        this.sim_slot = mDetails.sim_slot;
    }
    public void eventDetails(String ph_number)
    {
        this.ph_number = ph_number;
    }

    @Override
    public String toString()
    {
        return "PackSMS{" +
                "id=" + id +
                ", sim_slot=" + sim_slot +
                ", used_sms=" + used_sms +
                ", rem_sms=" + rem_sms +
                ", ph_number='" + ph_number + '\'' +
                ", validity='" + validity + '\'' +
                ", pack_type='" + pack_type + '\'' +
                "} " + super.toString();
    }

    public NormalSMS getBaseDetails()
    {


        return new NormalSMS(this);
    }
}
