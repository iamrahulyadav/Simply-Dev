package com.builder.ibalance.messages;

/**
 * Created by Shabaz on 29-Sep-15.
 */
public class OutgoingCallMessage
{
    public String lastNumber = "";
    public int duration = 0;



    public OutgoingCallMessage(String lastNumber, int duration)
    {
        this.lastNumber = lastNumber;
        this.duration = duration;
    }
}
