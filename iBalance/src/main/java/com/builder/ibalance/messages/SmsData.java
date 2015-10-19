package com.builder.ibalance.messages;

/**
 * Created by Shabaz on 19-Oct-15.
 */
public class SmsData
{
    private String date;
    private String address;
    private String content;
    public SmsData(String date, String address, String content)
    {
        this.date = date;
        this.address = address;
        this.content = content;
    }


}
