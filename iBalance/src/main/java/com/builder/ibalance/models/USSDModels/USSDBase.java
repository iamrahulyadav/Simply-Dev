package com.builder.ibalance.models.USSDModels;

/**
 * Created by Shabaz on 05-Jan-16.
 */
public class USSDBase
{
    public long date;
    public float main_bal;
    //Always take it from ConstantsAndStatic.USSD_TYPES class
    public int USSD_TYPE;
    public String original_message;
    public void baseDetails(long date , int USSD_TYPE, float main_bal, String original_message)
    {
        this.date = date;
        this.USSD_TYPE = USSD_TYPE;
        this.main_bal = main_bal;
        this.original_message = original_message;
    }
    public int getType()
    {
        return USSD_TYPE;
    }

    @Override
    public String toString()
    {
        return "USSDBase{" +
                "date=" + date +
                ", main_bal=" + main_bal +
                ", USSD_TYPE=" + USSD_TYPE +
                ", original_message='" + original_message + '\'' +
                '}';
    }
}
