package com.builder.ibalance.messages;

/**
 * Created by Shabaz on 14-Jan-16.
 */
public class BalanceRefreshMessage {
    //If balance is -1.0 then assume it failed
    //For SMS pack round of the float
    float balance;
    String originalMessage;

    public BalanceRefreshMessage(float balance, String originalMessage) {

        this.balance = balance;
        this.originalMessage = originalMessage;
    }
    public boolean isSuccessful()
    {
        return balance > -1.0f;
    }
}
