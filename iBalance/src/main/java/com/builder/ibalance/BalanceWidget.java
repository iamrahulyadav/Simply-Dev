package com.builder.ibalance;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.RemoteViews;

public class BalanceWidget extends AppWidgetProvider {
		final String tag = BalanceWidget.class.getSimpleName();
		
	  @Override
	  public void onUpdate(Context context, AppWidgetManager appWidgetManager,
	      int[] appWidgetIds) {

	    // Get all ids
	    ComponentName thisWidget = new ComponentName(context,
	        BalanceWidget.class);
	    int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
	    for (int widgetId : allWidgetIds) {

	      RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
	          R.layout.balance_widget_layout);
	      //Log.d(tag, "Setting Default text");
	      // Set the text
	     try{ 
	    	 SharedPreferences mSharedPreferences = context.getSharedPreferences("USER_DATA",Context.MODE_PRIVATE);
	      float currBalanace = mSharedPreferences.getFloat("CURRENT_BALANCE", (float) -1.0);
	      if(currBalanace==-1)
	    	  remoteViews.setTextViewText(R.id.widget_balance, context.getResources().getString(R.string.rupee_symbol)+" --.--");  
	      else
	      remoteViews.setTextViewText(R.id.widget_balance, context.getResources().getString(R.string.rupee_symbol)+" "+currBalanace);
	     }
	     catch(Exception e)
	     {
	    	 remoteViews.setTextViewText(R.id.widget_balance, "0.0");
	    	//V10e.printStackTrace();
	     }
	      // Register an onClickListener
	      Intent intent = new Intent(context, SplashscreenActivity.class);
	      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	   // Make the pending intent unique...
	      intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
	      intent.putExtra("FROM", "WIDGET");
	      PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
	      //Log.d(tag, "Adding click Listener");
	      // Get the layout for the App Widget and attach an on-click listener to the button
	      remoteViews.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
	      appWidgetManager.updateAppWidget(widgetId, remoteViews);
	      appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_layout);

	    }
	  }

}
