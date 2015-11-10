package com.builder.ibalance.messages;

/**
 * Created by Shabaz on 29-Sep-15.
 */
public class OutgoingCallMessage
{
    public String lastNumber = "";
    public int duration = 0;
    public long id = 0;



    public OutgoingCallMessage(String lastNumber, int duration,long id)
    {
        this.lastNumber = lastNumber;
        this.duration = duration;
        this.id = id;
    }
}
