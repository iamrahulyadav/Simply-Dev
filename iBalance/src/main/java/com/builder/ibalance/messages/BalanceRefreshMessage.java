package com.builder.ibalance.messages;

/**
 * Created by Shabaz on 14-Jan-16.
 */
public class BalanceRefreshMessage {
    //If balance is -1.0 then assume it failed
    //For SMS pack round of the float
    float balance;
    String originalMessage;

    public String getValidity()
    {
        return validity;
    }

    String validity;

    public float getBalance()
    {
        return balance;
    }

    public String getOriginalMessage()
    {
        return originalMessage;
    }

    public void setOriginalMessage(String originalMessage)
    {
        this.originalMessage = originalMessage;
    }

    public String detailstoLog()
    {
        return balance+", "+validity;
    }
    @Override
    public String toString()
    {
        return "BalanceRefreshMessage{" +
                "balance=" + balance +
                ", originalMessage='" + originalMessage + '\'' +
                ", validity='" + validity + '\'' +
                '}';
    }

    public BalanceRefreshMessage(float balance, String validity, String originalMessage) {

        this.balance = balance;
        this.validity = validity;
        this.originalMessage = originalMessage;
    }
    public boolean isSuccessful()
    {
        return Float.compare(balance,Float.MIN_VALUE)!=0;
    }
}
