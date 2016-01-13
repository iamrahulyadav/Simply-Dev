package com.builder.ibalance.util;

import android.content.Intent;
import android.net.Uri;

/**
 * Created by Shabaz on 13-Jan-16.
 */
public class DialUSSD
{
    String USSDCode;
    int sim_slot;
    Intent ussdIntent;
    public DialUSSD(String USSDCode, int sim_slot)
    {
        this.USSDCode = USSDCode.replace("*", Uri.encode("*")).replace("#",Uri.encode("#"));
        this.sim_slot = sim_slot;
        ussdIntent = new Intent(Intent.ACTION_CALL);
        Uri data = Uri.parse("tel:" + USSDCode);
        ussdIntent.setData(data);
    }
}
