package com.builder.ibalance.util;

import android.graphics.Typeface;

import com.builder.ibalance.R;

public  class ConstantsAndStatics {
	public static String RUPEE_SYMBOL = MyApplication.context.getResources().getString(
			R.string.rupee_symbol);
	public static Typeface ROBOTO_REGULAR = Typeface.createFromAsset(MyApplication.context.getAssets(), "Roboto-Regular.ttf");
	public static boolean WAITING_FOR_SERVICE = false;
	// name,InCount,InDur,OutCount,OutDur,MissCount,Provider,State,imageUuri
	 public static class MainMap
	 {
		 public static int NAME = 0;
		 public static int IN_COUNT = 1;
		 public static int IN_DURATION = 2;
		 public static int OUT_COUNT = 3;
		 public static int OUT_DURATION = 4; 
		 public static int MISSED_COUNT = 5;
		 public static int CARRIER = 6;
		 public static int CIRCLE = 7;
		 public static int IMAGE_URI = 8;
		
	 }
	 
	 public enum USSDMessageType
	 {
		 NORMAL_CALL,//1
		 NORMAL_SMS,//2
		 NORMAL_DATA,//3
		 VOICE_PACK,//4
		 SMS_PACK,//5
		 DATA_PACK,//6
		 BALANCE,//7
	 }
}
