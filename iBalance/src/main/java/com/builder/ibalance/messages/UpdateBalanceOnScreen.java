package com.builder.ibalance.messages;

/**
 * Created by Shabaz on 25-Jan-16.
 */
public class UpdateBalanceOnScreen
{
    String type,validity;
    float balance;

    @Override
    public String toString()
    {
        return "UpdateBalanceOnScreen{" +
                "type='" + type + '\'' +
                ", validity='" + validity + '\'' +
                ", balance=" + balance +
                '}';
    }

    public String getType()
    {
        return type;
    }

    public String getValidity()
    {
        return validity;
    }

    public float getBalance()
    {
        return balance;
    }

    public UpdateBalanceOnScreen(String type, String validity, float balance)
    {

        this.type = type;
        this.validity = validity;
        this.balance = balance;
    }
}
