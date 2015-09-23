package com.builder.ibalance.datainitializers;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.RemoteViews;

import com.builder.ibalance.BalanceWidget;
import com.builder.ibalance.R;
import com.builder.ibalance.database.BalanceHelper;
import com.builder.ibalance.database.MappingHelper;
import com.builder.ibalance.database.models.NormalCall;
import com.builder.ibalance.util.DataLoader;
import com.builder.ibalance.util.MyApplication;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;


public class DataInitializer extends AsyncTask<Context, Integer, Integer> {
	public static boolean mainActivityRunning = true;
	DataLoader mDataLoader;
	//static Context context;
	public static boolean done = false;
	static MappingHelper mMappingHelper;
	public class SmsLogs {
		public Date date;
		public String provider, state;
		public int count;

		public SmsLogs(Date date, String provider, String state, int count) {
			this.date = date;
			this.provider = provider;
			this.state = state;
			this.count = count;
		}

		void updateCount(int count) {
			this.count += count;
		}
	}

	// change from Int class to int
	// don't convert date, keep it in epoch time
	// no need to create new data object
	private static final String TAG = DataInitializer.class.getSimpleName();
	public static Map<String, String> Providers = new TreeMap<String, String>();
    public static Map<String, String> States = new TreeMap<String, String>();
	public static Map<Integer, ArrayList<String>> Cache = new TreeMap<Integer, ArrayList<String>>();
	public static Map<String, String[]> nameCache = new TreeMap<String, String[]>();
	public static Map<String, Object[]> mainmap = new TreeMap<String, Object[]>();
	public static Map<Date, ArrayList<Integer>> dateDurationMap = new TreeMap<Date, ArrayList<Integer>>();
	static Map<Integer, ArrayList<Integer>> Plans = new TreeMap<Integer, ArrayList<Integer>>();
	public static Map<String, SmsLogs> smsMap = new TreeMap<String, SmsLogs>();
	public static List<NormalCall> ussdDataList = new LinkedList<NormalCall>();

	public static void initializeUSSDData(Context ctx) {
		
		
		BalanceHelper mBalanceHelper = new BalanceHelper();
		// ussdDataList.clear();
		//ussdDataList = db.getAllEntries();
		//db.addDemoentries();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);
        Date fromDate = cal.getTime();  
        String fromdate = dateFormat.format(fromDate);
        //Log.d(TAG,"7 day old date = "+fromDate);
        ussdDataList.clear();
        ussdDataList =( mBalanceHelper.getEntriesFromDate(fromDate.getTime(),ctx));
		 //Log.d(TAG,ussdDataList.toString());
		mBalanceHelper.close();
	}

	@Override
	protected Integer doInBackground(Context... ctx) {
		{
			// Cache.clear();
			done=false;
			mDataLoader = (DataLoader) ctx[0];
			//context = ctx[0];
			//Log.d("DataInit", "WORKING IN BACKGROUND");
			//Log.d("DataInit", "WORKING IN InitializeData");
			InitializeData();
			//Log.d("DataInit", "WORKING IN InitializeMap");
			InitializeMap(MyApplication.context);
			//Log.d("DataInit", "WORKING IN updateWidgetInitially");
			updateWidgetInitially(MyApplication.context);
			done = true;
			//Log.d("DataInit", "WORKING IN InitializeSmsMap");
			//InitializeSmsMap(MyApplication.context);

		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@SuppressLint("NewApi")
	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		
		if(mainActivityRunning)
		{
			mDataLoader.dataLoaded();
		}
		else
		{
			//Log.d(TAG,"mMainActivity is null ACtivity closed");
		}
	}

	private void updateWidgetInitially(Context ctx) {
		//Log.d("DataInit","Came to update  widget inittially "+ "iin Data initializer");
		AppWidgetManager mgr=AppWidgetManager.getInstance(ctx);
		
		ComponentName thisWidget = new ComponentName(ctx,
	    		BalanceWidget.class);
	    SharedPreferences mSharedPreferences = ctx.getSharedPreferences("USER_DATA",Context.MODE_PRIVATE);
	      float currBalance = mSharedPreferences.getFloat("CURRENT_BALANCE", (float) -1.0);
	      //Log.d("DataInit widget bal ", ""+ currBalance);
	      // Set the text
	      
	      int predictedDays = -1 ;
      long firstDate = mSharedPreferences.getLong("FIRST_DATE", Long.parseLong("1420050600000"));
	      
	      int total_out_duration = mSharedPreferences.getInt("TOTAL_OUT_DURATION", 100);
	      //Log.d(TAG, "Toatl OUT Duration = "+total_out_duration);
	      
	      float call_rate = mSharedPreferences.getFloat("CALL_RATE", (float)1.7);
	      //Log.d(TAG, "CALL_RATE= "+call_rate);
	      
	      int numberOfDays = (int)( (new Date().getTime() - firstDate) / (1000 * 60 * 60 * 24));
	      //Log.d(TAG, "No of Days = "+ numberOfDays);
	      
	      float total_cost_inPaise = total_out_duration*call_rate;
	      //Log.d(TAG, "Toatal cost = "+total_cost_inPaise);
	       predictedDays = (int) (currBalance*numberOfDays/(total_cost_inPaise/100));
	      //Log.d(TAG, "predicted DAys = "+predictedDays);
	      Editor mEditor = mSharedPreferences.edit();
	      mEditor.putInt("PREDICTED_DAYS", predictedDays);
	      mEditor.commit();
		int[] allWidgetIds = mgr.getAppWidgetIds(thisWidget);
	    for (int widgetId : allWidgetIds) {
	      // create some random data

	      RemoteViews remoteViews = new RemoteViews(ctx.getPackageName(),
	          R.layout.balance_widget_layout);
	  
	      if(currBalance==-1)
	    	  remoteViews.setTextViewText(R.id.widget_balance, ctx.getResources().getString(R.string.rupee_symbol)+" --.--");  
	      else
	      {
	    	  remoteViews.setTextViewText(R.id.widget_balance, ctx.getResources().getString(R.string.rupee_symbol)+" "+currBalance);
	      }
	      if(predictedDays == -1)
	      {
	    	  remoteViews.setTextViewText(R.id.widget_prediction, "Please make a call to update your Balance");
	      }
	      else if(predictedDays==0)
	      {
	    	  remoteViews.setTextViewText(R.id.widget_prediction, "Your Balance will get over Today");
	      }
	      else
	      {
	    	  String readableDays = getReadableDays(predictedDays);
	    	  String text= "Your Balance is predicted to getover in "+readableDays+" Days";
	    	  final SpannableStringBuilder sb = new SpannableStringBuilder(text);

	          final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD); // Span to make text bold
	          sb.setSpan(bss, text.indexOf("in ")+3, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE); // make characters Bold 
	    	  remoteViews.setTextViewText(R.id.widget_prediction, sb);
	      }
	      mgr.updateAppWidget(widgetId, remoteViews);
	    }
		
	}

	private String getReadableDays(int predictedDays) {
		String ret;
		if(predictedDays<30)
			return predictedDays+"";
		else
		{
			
			ret = predictedDays % 30 +"";
			ret = (predictedDays/30) + " Months, " + ret;
		}
		return ret;
	}

	private void InitializeSmsMap(Context context) {
		if (!smsMap.isEmpty()) {
			//Log.d(TAG, "SMS Map Exists");
			return;
		}
		Uri smsUri = Uri.parse("content://sms/sent");
		Cursor cursor = context.getContentResolver().query(smsUri, null, null,
				null, null);
		int smsLen = cursor.getColumnIndex("body");
		int number = cursor.getColumnIndexOrThrow("address");
		int dateIndex = cursor.getColumnIndexOrThrow("date");
		smsMap.clear();
		mMappingHelper = new MappingHelper(context);

		String Provider, State;
		String phnumber = null;

		while (cursor.moveToNext()) {
			int totalMessages = 0;
			phnumber = cursor.getString(number).toString();

			Date date = new Date(Long.valueOf(cursor.getString(dateIndex)));
			try {
				if (phnumber.length() < 10)
					continue;
				if (phnumber.startsWith("+91"))
					phnumber = phnumber.substring(3);
				String first4digits = phnumber.substring(0, 4);
				try {
					// check if already there in cache
					ArrayList<String> x = (ArrayList<String>) DataInitializer.Cache
							.get(Integer.parseInt(first4digits));
					Provider = x.get(0);
					State = x.get(1);
				} catch (Exception e) {
					try {
						// if not there in cache, extract
						ArrayList<String> x = (ArrayList<String>) mMappingHelper
								.getMapping(Integer.parseInt(first4digits));
						// //Log.d("Data mapping",x.get(0)+"  "+x.get(1));
						Provider = DataInitializer.Providers.get(x.get(0));
						State = DataInitializer.States.get(x.get(1));
						DataInitializer.Cache.put((number), x);

					} catch (Exception ee) {
						// ee.printStackTrace();
						// if not found
						Provider = "Unknown";
						State = "Not Found";
						// //Log.d("cache", "not found");
					}
				}
				String messageBody = cursor.getString(smsLen);
				long messageLength = messageBody.length();
				double numberOfMessages = messageLength / 153.0;
				double numberOfMessagesRoundedUp = Math.ceil(numberOfMessages);

				totalMessages = (int) (totalMessages + numberOfMessagesRoundedUp);
				if (!smsMap.containsKey(phnumber)) {
					smsMap.put(phnumber, new SmsLogs(date, Provider, State,
							totalMessages));
				} else {
					SmsLogs smsLogObject = smsMap.get(phnumber);
					smsLogObject.updateCount(totalMessages);
				}

			} catch (Exception e) {
				Log.e(TAG, "Error inreading  SMS logs");
			}
		}
		cursor.close();
		mMappingHelper.close();
		/*
		 * for (Entry<String, SmsLogs> entry : smsMap.entrySet()) {
		 * 
		 * String key = entry.getKey(); SmsLogs value = smsMap.get(key);
		 * //Log.d(TAG, "phnumber = "+key); //Log.d(TAG, "Date "+ value.date);
		 * //Log.d(TAG,"Provider "+value.provider); //Log.d(TAG, value.state);
		 * //Log.d(TAG, "count = "+value.count);
		 * 
		 * 
		 * }
		 */
	}

	public static void InitializeData() {
		// Providers.clear();
		Providers.put("AC", "AIRCEL");
		Providers.put("AT", "Airtel");
		Providers.put("CC", "BSNL");
		Providers.put("DP", "MTNL");
		Providers.put("ET", "Etisalat");
		Providers.put("ID", "IDEA");
		Providers.put("LM", "Loop");
		Providers.put("MT", "MTS");
		Providers.put("PG", "PING CDMA");
		Providers.put("RC", "Reliance");
		Providers.put("SP", "Spice");
		Providers.put("ST", "S Tel");
		Providers.put("T24", "T24");
		Providers.put("TD", "TATA DOCOMO");
		Providers.put("TI", "Tata Indicom");
		Providers.put("UN", "Uninor");
		Providers.put("VC", "Virgin");
		Providers.put("VF", "Vodafone");
		Providers.put("VD", "Videocon");
		// States.clear();
		States.put("AP", "Andhra Pradesh");
		States.put("AS", "Assam");
		States.put("BR", "Bihar");
		States.put("CH", "Chennai");
		States.put("DL", "Delhi");
		States.put("GJ", "Gujarat");
		States.put("HP", "Himachal Pradesh");
		States.put("HR", "Haryana");
		States.put("JK", "Jammu and Kashmir");
		States.put("KL", "Kerala");
		States.put("KA", "Karnataka");
		States.put("KO", "Kolkata");
		States.put("MH", "Maharashtra");
		States.put("MP", "Madhya Pradesh");
		States.put("MU", "Mumbai");
		States.put("NE", "Arunachal Pradesh (North East India)");
		States.put("OR", "Odisha");
		States.put("PB", "Punjab");
		States.put("RJ", "Rajasthan");
		States.put("TN", "Tamil Nadu");
		States.put("UE", "Uttar Pradesh (East)");
		States.put("UW", "Uttar Pradesh (West)");
		States.put("WB", "West Bengal");
		States.put("ZZ", "Customer Care (All Over India)");

		// Map<Integer, ArrayList<String>> Num = new TreeMap<Integer,
		// ArrayList<String>>();
		// Num.clear();

	}



	public static ArrayList<String> getContactName( String number) {
		String name="",photo_uri=null;
		ArrayList<String> name_photo = new ArrayList<String>();
		Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
		Cursor contactLookup;
		try{
	     contactLookup = MyApplication.context.getContentResolver().query(uri, new String[] {ContactsContract.PhoneLookup._ID,
	                                            ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI }, null, null, null);
		}
		catch(IllegalArgumentException e)
		{
			e.printStackTrace();
			name_photo.add(number);
			name_photo.add(photo_uri);
			return name_photo;
		}
		if(contactLookup==null)
		{
			name_photo.add(number);
			name_photo.add(photo_uri);
			return name_photo;
		}
		int indexName = contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME);
	    int indexPhoto = contactLookup.getColumnIndex(ContactsContract.Data.PHOTO_THUMBNAIL_URI);

	    try {
	        if (contactLookup != null && contactLookup.moveToNext()) {
	            name = contactLookup.getString(indexName);
	            photo_uri = contactLookup.getString(indexPhoto);
	        }
	    } finally {
	        if (contactLookup != null) {
	            contactLookup.close();
	        }
	    }
	    
	  //  //Log.d(TAG," Name = "+number);
	    if(name.equals(""))
	    	{
	    	name_photo.add(number);
	    	
	    	
	    	}
	    else
	    {
	    	name_photo.add(name);
	    }

    	name_photo.add(photo_uri);
	    return name_photo;
	    //return number;
		/*Uri uri;
		String[] projection;
		uri = Uri.parse("content://com.android.contacts/phone_lookup");
		projection = new String[] { "display_name" };
		uri = Uri.withAppendedPath(uri, Uri.encode(phoneNumber));

		Cursor cursor = context.getContentResolver().query(uri, projection,
				null, null, null);

		String contactName = "";

		if (cursor.moveToFirst()) {
			contactName = cursor.getString(0);
		}

		cursor.close();
		cursor = null;

		return contactName;*/
	}

	public static void InitializeMap(Context context) {
		SharedPreferences mSharedPreferences = context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);

        Long startDate = 0l;//cal.getTimeInMillis();
		// Cache.clear();
		//Log.d("DataInit", "InitializeMap");

		if (!mainmap.isEmpty() && !dateDurationMap.isEmpty()) {
			//Log.d(TAG, "Main and dateDurationMap Map Exists");
			startDate = mSharedPreferences.getLong("CACHE_DATE", 0l);
			//Log.d("TEST", "Logging from "+startDate);
		}
		/*mainmap.clear();
		dateDurationMap.clear();*/
		mMappingHelper = new MappingHelper(context);
      //  Long endDate = new Date().getTime();
        //CallLog.Calls.DATE + ">? AND "+ CallLog.Calls.DATE + "<?", , String.valueOf(endDate)
		Cursor managedCursor = context.getContentResolver().query(
				CallLog.Calls.CONTENT_URI, 
				new String[] {CallLog.Calls.NUMBER,CallLog.Calls.DATE,CallLog.Calls.TYPE,CallLog.Calls.DURATION}, 
				CallLog.Calls.DATE + ">?",
				new String[] { String.valueOf(startDate)}, CallLog.Calls.DATE + " ASC");
		Editor mEditor = mSharedPreferences.edit();
		mEditor.putLong("CACHE_DATE", (new Date()).getTime());
		//Log.d("DataInit", managedCursor.getCount() + " ");
		// Get Indexes
		int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
		int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
		int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
		int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
		// loop through the call log
		String phNumber;
		String[] name_image;
		String ph;
		String Provider, State;
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
		int temp_call_duration=0,total_callDuration=0;
		while (managedCursor.moveToNext()) {
			//managedCursor.getColumnIndex(CallLog.Calls.)
			phNumber = (managedCursor.getString(number));
			////Log.d("TEST",phNumber);
			if(phNumber == null)
				continue;
			name_image = nameCache.get(phNumber);
			if (name_image == null) {
				name_image = new String[2];
				name_image =  getContactName(phNumber).toArray(name_image);
				/*name_image[0] = 
				name_image[1] = getContactsImage(phNumber);*/

				if (name_image[0] == "")
					name_image[0] = "Unknown";
				nameCache.put(phNumber, name_image);
			}
			if (phNumber.startsWith("+91"))
				phNumber = phNumber.substring(3).replaceAll(" ","");
			else
				phNumber = phNumber.replaceAll(" ","");
			if(phNumber.startsWith("0"))
				phNumber = phNumber.substring(1).replaceAll(" ","");
			else
				phNumber = phNumber.replaceAll(" ","");
			// //Log.d(TAG,phNumber);
			// Extract other details
			String callType = managedCursor.getString(type);
			String callDate = managedCursor.getString(date);
			// //Log.d("datainit", callDate);
			
			String onlyDateString = sdf
					.format(new Date(Long.valueOf(callDate)));
			 ////Log.d("datainit", onlyDateString);
			// SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			// String onlyDateString = df.format(callDayTime);
			Date onlyDate = null;
			
			try {
				onlyDate = sdf.parse(onlyDateString);
			} catch (ParseException e2) {
				e2.printStackTrace();
			}
			// //Log.d("datainit", onlyDate.toString());
			String callDuration = managedCursor.getString(duration);
			
			int dircode = Integer.parseInt(callType);
			if(dircode == CallLog.Calls.OUTGOING_TYPE && Integer.parseInt(callDuration)<=0)
				continue;
			if (!dateDurationMap.containsKey(onlyDate)) {
				dateDurationMap.put(onlyDate,
						new ArrayList<Integer>(Arrays.asList(0, 0, 0, 0)));

			}
			ArrayList<Integer> datedata = (ArrayList<Integer>) dateDurationMap
					.get(onlyDate);
			// InCount-0 Indur-1 OutCount-2 OutDur-3
			switch (dircode) {
			case CallLog.Calls.OUTGOING_TYPE:
				datedata.set(2, (Integer) datedata.get(2) + 1) ;//outCount
				temp_call_duration = Integer.parseInt(callDuration);
				total_callDuration += temp_call_duration;
				datedata.set(3, (Integer) datedata.get(3) +temp_call_duration );//outDuration
				dateDurationMap.put(onlyDate,datedata);
				break;

			case CallLog.Calls.INCOMING_TYPE:
				datedata.set(0,(Integer) datedata.get(0) + 1);//inCount
				datedata.set(1,(Integer) datedata.get(1)+ Integer.parseInt(callDuration));//inDuration
				dateDurationMap.put(onlyDate,datedata);
				break;
			}

			// check for the existance of map entry

			if (!mainmap.containsKey(phNumber)) { 
				// Extract the first 4 digits of the number
				try {
					ph = phNumber.substring(phNumber.length() - 10,
							phNumber.length() - 6);
				} catch (Exception e1) {
					ph = "0000";
				}
				// Identify provider and state begin
				try {
					// check if already there in cache
					ArrayList<String> x = (ArrayList<String>) DataInitializer.Cache
							.get(Integer.parseInt(ph));
					Provider = DataInitializer.Providers.get(x.get(0));
					State =DataInitializer.States.get( x.get(1));
					// //Log.d("cache", "from cache");
				} catch (Exception e) {
					try {
						// if not there in cache, extract
						ArrayList<String> x = (ArrayList<String>) mMappingHelper.getMapping(Integer.parseInt(ph));
						// //Log.d("Data mapping",x.get(0)+"  "+x.get(1));
						Provider =DataInitializer.Providers.get(x.get(0));
						State = DataInitializer.States.get(x.get(1));
						DataInitializer.Cache.put((number), x);

						// //Log.d("cache", "from num");

					} catch (Exception ee) {
						// ee.printStackTrace();
						// if not found
						if (ph.length() >= 10)
							Provider = "Land Line";
						else
							Provider = "Unknown";
						State = "Not Found";
						// //Log.d("cache", "not found");
					}
				}
				// name,InCount,InDur,OutCount,OutDur,MissCount,Provider,State,imageUuri
				if(Provider==null)
				{
					Provider = "Unknown";
				}
				if(State==null)
				{
					State = "Not Found";
				}
				mainmap.put(phNumber, new Object[] { name_image[0], 0, 0, 0, 0,
						0, Provider, State, name_image[1] });

			}
			// //Log.d(TAG +"Cahche initialize done", Cache.size() +" ");
			Object[] data = (Object[]) mainmap.get(phNumber);
			Provider = (String) data[6];
			State = (String) data[7];
			// decide depending on the type of call log
			switch (dircode) {
			case CallLog.Calls.OUTGOING_TYPE:
				data[3] = (Integer) data[3] + 1;
				data[4] = (Integer) data[4] + Integer.parseInt(callDuration);
				mainmap.put(phNumber, data);
				break;

			case CallLog.Calls.INCOMING_TYPE:
				data[1] = (Integer) data[1] + 1;
				data[2] = (Integer) data[2] + Integer.parseInt(callDuration);
				mainmap.put(phNumber, data);
				break;

			case CallLog.Calls.MISSED_TYPE:
				data[5] = (Integer) data[5] + 1;
				mainmap.put(phNumber, data);
				break;

			}

		}
		managedCursor.close();
		//nameCache.clear();
		mMappingHelper.close();
		//SharedPreferences mSharedPreferences = context.getSharedPreferences("USER_DATA",Context.MODE_PRIVATE);
		// mEditor = mSharedPreferences.edit();
		if(startDate==0l)
		{
		//Log.d(TAG, "Shared Pref TOTAL_OUT_DURATION = "+total_callDuration);
		mEditor.putInt("TOTAL_OUT_DURATION", total_callDuration);
		Date dt =new Date(Long.parseLong("1420050600000"));
		for (Entry<Date, ArrayList<Integer>> entry : DataInitializer.dateDurationMap
				.entrySet()) {
			 dt = entry.getKey();
			break;
		}
		//Log.d(TAG, "First Date = "+dt.toString());
		mEditor.putLong("FIRST_DATE", dt.getTime());
		mEditor.commit();
		}
		
	}
	

}
