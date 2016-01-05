package com.builder.ibalance.models.USSDModels;

/**
 * Created by Shabaz on 05-Jan-16.
 */
public class PackData extends USSDBase
{
    float data_used,data_left;
    int sim_slot;
    String validity,pack_type;
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
