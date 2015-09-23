package com.builder.ibalance.util;

import android.content.Context;
import android.content.Intent;

import com.apptentive.android.sdk.Apptentive;
import com.parse.ParsePushBroadcastReceiver;

public class ParsePushReceiver extends ParsePushBroadcastReceiver {

	@Override
	protected void onPushOpen(Context context, Intent intent) {
	    super.onPushOpen(context, intent);
	    Apptentive.setPendingPushNotification(context, intent);
	}

}
