package com.builder.ibalance.util;

/**
 * Created by Shabaz on 13-Oct-15.
 */
public class Tuple
{
    String string_val;
    int int_val;
    public Tuple(String s,int i)
    {
        string_val = s;
        int_val = i;
    }

    public String getString_val()
    {
        return string_val;
    }

    public int getInt_val()
    {
        return int_val;
    }


    @Override
    public String toString()
    {
        return string_val+" : "+int_val+"\n";
    }
}
