package com.builder.ibalance.messages;

/**
 * Created by Shabaz on 08-Jan-16.
 */
public class OutgoingSmsMessage
{
    public long id;

    @Override
    public String toString()
    {
        return "OutgoingSmsMessage{" +
                "id=" + id +
                ", lastNumber='" + lastNumber + '\'' +
                ", sim_slot=" + sim_slot +
                '}';
    }

    public String lastNumber;
    public int sim_slot = 0;
    public OutgoingSmsMessage(long id, String number,int sim_slot)
    {
        this.id = id;
        this.lastNumber = number;
        this.sim_slot = 0;
    }
}
