package com.builder.ibalance.datainitializers;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;

import com.builder.ibalance.CallPatternFragment;
import com.builder.ibalance.database.MappingHelper;
import com.builder.ibalance.database.helpers.CallLogsHelper;
import com.builder.ibalance.database.helpers.IbalanceContract;
import com.builder.ibalance.util.MyApplication;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class FilteredDataInitializer extends AsyncTask<Long, Integer, Integer > {
	public static boolean mainActivityRunning = true;
	final String TAG = FilteredDataInitializer.class.getSimpleName();
	public static Map<String, Object[]> filteredMainMap = new TreeMap<String, Object[]>();
	public static Map<Date, ArrayList<Integer>> filteredDateDurationMap = new TreeMap<Date, ArrayList<Integer>>();
	public static boolean dataLoaded = false;
	CallPatternFragment mCallPatternFragment;
	public FilteredDataInitializer(CallPatternFragment mCallPatternFragment)
	{
		
		this.mCallPatternFragment = mCallPatternFragment;
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		dataLoaded = true;
		//Log.d(TAG, "Filtered Data Loaded");
		if(mainActivityRunning)
		{
			mCallPatternFragment.dataLoaded();
		}
		super.onPostExecute(result);
	}

	public static ArrayList<String> getContactName( String number) {
		String name="",photo_uri=null;
		Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

	    Cursor contactLookup = MyApplication.context.getContentResolver().query(uri, new String[] {ContactsContract.PhoneLookup._ID,
	                                            ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI }, null, null, null);

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
	    ArrayList<String> name_photo = new ArrayList<String>();
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

		
		}
	
	
	@Override
	protected Integer doInBackground(Long... params) {

		// Cache.clear();
		//Log.d("DataInit", "InitializeMap");
		dataLoaded = false;
	
		filteredMainMap.clear();
		filteredDateDurationMap.clear();
		MappingHelper mMappingHelper = new MappingHelper();
        Long startDate = params[0];//cal.getTimeInMillis();
      //  Long endDate = new Date().getTime();
        //CallLog.Calls.DATE + ">? AND "+ CallLog.Calls.DATE + "<?", , String.valueOf(endDate)
		Cursor managedCursor = (new CallLogsHelper()).
				getDatabase().
				query(
						IbalanceContract.CallLogEntry.TABLE_NAME,
						null,
						IbalanceContract.CallLogEntry.COLUMN_NAME_DATE + ">?",
						new String[] { String.valueOf(startDate)},
						null,
						null,
						IbalanceContract.CallLogEntry.COLUMN_NAME_DATE+ " ASC"
				);
		Log.d(TAG, "QUERY Returned " + managedCursor.getCount());
		/*Cursor managedCursor = context.getContentResolver().query(
				CallLog.Calls.CONTENT_URI,
				new String[] {CallLog.Calls.NUMBER,CallLog.Calls.DATE,CallLog.Calls.TYPE,CallLog.Calls.DURATION},
				CallLog.Calls.DATE + ">?",
				new String[] { String.valueOf(startDate)}, CallLog.Calls.DATE + " ASC");*/
		//Log.d("DataInit", managedCursor.getCount() + " ");
		// Get Indexes
		int number = managedCursor.getColumnIndex(IbalanceContract.CallLogEntry.COLUMN_NAME_NUMBER);
		int type = managedCursor.getColumnIndex(IbalanceContract.CallLogEntry.COLUMN_NAME_TYPE);
		int date = managedCursor.getColumnIndex(IbalanceContract.CallLogEntry.COLUMN_NAME_DATE);
		int duration = managedCursor.getColumnIndex(IbalanceContract.CallLogEntry.COLUMN_NAME_DURATION);
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
			if(phNumber==null)
				continue;
			name_image = DataInitializer.nameCache.get(phNumber);
			
			if (name_image == null) {
				name_image = new String[2];
				name_image =  getContactName(phNumber).toArray(name_image);
				/*name_image[0] = 
				name_image[1] = getContactsImage(phNumber);*/

				if (name_image[0] == "")
					name_image[0] = "Unknown";
				DataInitializer.nameCache.put(phNumber, name_image);
			}
			if (phNumber.startsWith("+91"))
			{
				phNumber = phNumber.substring(3).replaceAll(" ","");
				phNumber = phNumber.replaceAll("-", "");
			}
			else
			{
				phNumber = phNumber.replaceAll(" ","");
				phNumber = phNumber.replaceAll("-","");
			}
			if(phNumber.startsWith("0"))
			{
				phNumber = phNumber.substring(1).replaceAll(" ","");
				phNumber = phNumber.replaceAll("-", "");
			}
			else
			{
				phNumber = phNumber.replaceAll(" ","");
				phNumber = phNumber.replaceAll("-","");
			}
			// //Log.d(TAG,phNumber);
			// Extract other details
			String callType = managedCursor.getString(type);
			String callDate = managedCursor.getString(date);
			// //Log.d("datainit", callDate);
			
			String onlyDateString = sdf
					.format(new Date(Long.valueOf(callDate)));
			// //Log.d("dataint", onlyDateString);
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
			if (!filteredDateDurationMap.containsKey(onlyDate)) {
				filteredDateDurationMap.put(onlyDate,
						new ArrayList<Integer>(Arrays.asList(0, 0, 0, 0)));

			}
			ArrayList<Integer> datedata = (ArrayList<Integer>) filteredDateDurationMap
					.get(onlyDate);
			// InCount-0 Indur-1 OutCount-2 OutDur-3
			switch (dircode) {
			case CallLog.Calls.OUTGOING_TYPE:
				datedata.set(2, (Integer) datedata.get(2) + 1) ;//outCount
				temp_call_duration = Integer.parseInt(callDuration);
				total_callDuration += temp_call_duration;
				datedata.set(3, (Integer) datedata.get(3) +temp_call_duration );//outDuration
				filteredDateDurationMap.put(onlyDate,datedata);
				break;

			case CallLog.Calls.INCOMING_TYPE:
				datedata.set(0,(Integer) datedata.get(0) + 1);//inCount
				datedata.set(1,(Integer) datedata.get(1)+ Integer.parseInt(callDuration));//inDuration
				filteredDateDurationMap.put(onlyDate,datedata);
				break;
			}

			// check for the existance of map entry

			if (!filteredMainMap.containsKey(phNumber)) { 
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
					Provider = DataInitializer.carriers.get(x.get(0));
					State =DataInitializer.circle.get( x.get(1));
					// //Log.d("cache", "from cache");
				} catch (Exception e) {
					try {
						// if not there in cache, extract
						ArrayList<String> x = (ArrayList<String>) mMappingHelper.getMapping(Integer.parseInt(ph));
						// //Log.d("Data mapping",x.get(0)+"  "+x.get(1));
						Provider =DataInitializer.carriers.get(x.get(0));
						State = DataInitializer.circle.get(x.get(1));
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
				if(Provider == null)
				Provider = "Unknown";
				if(State==null)
					State = "Not Found";
				// name,InCount,InDur,OutCount,OutDur,MissCount,Provider,State,imageUuri
				filteredMainMap.put(phNumber, new Object[] { name_image[0], 0, 0, 0, 0,
						0, Provider, State, name_image[1] });

			}
			// //Log.d(TAG +"Cahche initialize done", Cache.size() +" ");
			Object[] data = (Object[]) filteredMainMap.get(phNumber);
			Provider = (String) data[6];
			State = (String) data[7];
			// decide depending on the type of call log
			switch (dircode) {
			case CallLog.Calls.OUTGOING_TYPE:
				data[3] = (Integer) data[3] + 1;
				data[4] = (Integer) data[4] + Integer.parseInt(callDuration);
				filteredMainMap.put(phNumber, data);
				break;

			case CallLog.Calls.INCOMING_TYPE:
				data[1] = (Integer) data[1] + 1;
				data[2] = (Integer) data[2] + Integer.parseInt(callDuration);
				filteredMainMap.put(phNumber, data);
				break;

			case CallLog.Calls.MISSED_TYPE:
				data[5] = (Integer) data[5] + 1;
				filteredMainMap.put(phNumber, data);
				break;

			}

		}
		managedCursor.close();
		//nameCache.clear();
		mMappingHelper.close();
		return 0;
	}

}
