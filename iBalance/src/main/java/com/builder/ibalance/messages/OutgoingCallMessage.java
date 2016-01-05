package com.builder.ibalance.messages;

/**
 * Created by Shabaz on 29-Sep-15.
 */
public class OutgoingCallMessage
{
    public String lastNumber = "";
    public int duration = 0;
    public long id = 0;
    public int sim_slot = 0;


    @Override
    public String toString()
    {
        return "OutgoingCallMessage{" +
                "lastNumber='" + lastNumber + '\'' +
                ", duration=" + duration +
                ", id=" + id +
                ", sim_slot=" + sim_slot +
                '}';
    }

    public OutgoingCallMessage(long id, int sim_slot, int duration, String lastNumber)
    {
        this.sim_slot = sim_slot;
        this.lastNumber = lastNumber;
        this.duration = duration;
        this.id = id;
    }
}
