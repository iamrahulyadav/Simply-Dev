package com.builder.ibalance.util;

import android.graphics.Typeface;

import com.builder.ibalance.R;

public  class ConstantsAndStatics {
	public static volatile boolean PASTE_SHARE_APP = false;
	public static String RUPEE_SYMBOL = MyApplication.context.getResources().getString(
			R.string.rupee_symbol);
	public static volatile boolean PASTE_DEVICE_ID = false;
	public static volatile boolean WAITING_FOR_REFRESH = false;
	public static volatile int REFRESH_TYPE = USSD_TYPES.MAIN_BALANCE;
	public static Typeface ROBOTO_REGULAR = Typeface.createFromAsset(MyApplication.context.getAssets(), "Roboto-Regular.ttf");
	public static boolean WAITING_FOR_SERVICE = false;
	public static String RECENT_EVENT = "UNKNOWN";
	public static String USER_PREF_KEY = "USER_DATA";

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
	//OLD one
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
	//New one
	public static class USSD_TYPES
	 {
		 public static final int NORMAL_CALL=1;
		 public static final int NORMAL_SMS = 2;
		 public static final int NORMAL_DATA = 3;
		 public static final int PACK_CALL= 4;
		 public static final int PACK_SMS = 5;
		 public static final int PACK_DATA = 6;
		 public static final int MAIN_BALANCE = 7;
		 public static final int PACK_SMS_CHECK = 8;
		 public static final int PACK_DATA_CHECK = 9;
	 }
}
