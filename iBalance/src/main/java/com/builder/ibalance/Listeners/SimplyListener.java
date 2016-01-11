package com.builder.ibalance.Listeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.builder.ibalance.util.ConstantsAndStatics;

/**
 * Created by Shabaz on 07-Dec-15.
 */
public class SimplyListener extends BroadcastReceiver
{
    String TAG = SimplyListener.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent)
    {
        //android.intent.action.NEW_OUTGOING_CALL
        switch (intent.getAction())
        {
            case Intent.ACTION_NEW_OUTGOING_CALL:
               //V16Log.d(TAG,"Outgoing Call");
                ConstantsAndStatics.RECENT_EVENT = Intent.ACTION_NEW_OUTGOING_CALL;
                break;
            case Intent.ACTION_DATE_CHANGED:
               //V16Log.d(TAG,"Data State Changed");
                ConstantsAndStatics.RECENT_EVENT = Intent.ACTION_DATE_CHANGED;
                break;
        }

    }
}
