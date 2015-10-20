package com.builder.ibalance.datainitializers;

import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.CallLog;
import android.util.Log;

import com.builder.ibalance.CallPatternFragment;
import com.builder.ibalance.database.helpers.CallLogsHelper;
import com.builder.ibalance.database.helpers.ContactDetailHelper;
import com.builder.ibalance.database.helpers.IbalanceContract;
import com.builder.ibalance.database.models.ContactDetailModel;
import com.builder.ibalance.messages.FilteredData;
import com.builder.ibalance.util.Tuple;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.greenrobot.event.EventBus;

public class FilteredDataInitializer extends AsyncTask<Long, Integer, Integer > {
	public static boolean mainActivityRunning = true;
	final String TAG = FilteredDataInitializer.class.getSimpleName();
	public static Map<String, Object[]> filteredMainMap = new TreeMap<String, Object[]>();
	public static Map<Date, ArrayList<Integer>> filteredDateDurationMap = new TreeMap<Date, ArrayList<Integer>>();
    public static Map<String, ContactDetailModel> contactDetailMap = new TreeMap<String, ContactDetailModel>();
	public static boolean dataLoaded = false;
    ArrayList<Tuple> mostCalled,carrierOutCount,circleOutDuration;
	CallPatternFragment mCallPatternFragment;
    long startDate=0l,endDate= 1544755421;
	public FilteredDataInitializer(long startDate,long endDate)
	{
        this.startDate = startDate;
        this.endDate = endDate;
	}
	class MutableInt
    {
        public int count =0;
        public MutableInt(int count)
        {
            this.count = count;
        }
        public void add(int delta)
        {
            count+=delta;
        }
    }
	@Override
	protected void onPostExecute(Integer result) {
		dataLoaded = true;
		//Log.d(TAG, "Filtered Data Loaded");
        FilteredData temp = new FilteredData(mostCalled,carrierOutCount,circleOutDuration);
        EventBus.getDefault().post(temp);
		super.onPostExecute(result);
	}

    public class customComparator implements Comparator<ContactDetailModel>
    {
        public int compare(ContactDetailModel object1, ContactDetailModel object2) {
            if (object1.out_count == object2.out_count)
                return 0;
            if (object1.out_count > object2.out_count)
                return -1;
            else
                return 1;
        }
    }
	
	@Override
	protected Integer doInBackground(Long... params) {

		// Cache.clear();
		Log.d(TAG, "Filtered Data Initializer");
		Log.d(TAG, "From : "+(new Date(startDate).toString()));
		Log.d(TAG, "to : " + (new Date(endDate).toString()));
		dataLoaded = false;
		contactDetailMap.clear();
        if(mostCalled!=null)
            mostCalled.clear();
        if(carrierOutCount!=null)
            carrierOutCount.clear();
        if(circleOutDuration!=null)
            circleOutDuration.clear();
        long clockStartTime = System.nanoTime();
		//Just a Precaution, but it must not happen
		while (DataInitializer.done==false);
        ContactDetailHelper mContactDetailHelper = new ContactDetailHelper();
        CallLogsHelper mCallLogsHelper = new CallLogsHelper();
        Cursor callLogCursor = mCallLogsHelper.getFilteredLocalCallLogs(startDate,endDate);
       //V10Log.d(TAG, "Number of Rows = " + callLogCursor.getCount());
        int slot,duration,type;
        Calendar c =Calendar.getInstance();
        ContactDetailModel contactDetail;
        int duration_index =callLogCursor.getColumnIndex(IbalanceContract.CallLogEntry.COLUMN_NAME_DURATION);
        int type_index =callLogCursor.getColumnIndex(IbalanceContract.CallLogEntry.COLUMN_NAME_TYPE);
        int number_index =callLogCursor.getColumnIndex(IbalanceContract.CallLogEntry.COLUMN_NAME_NUMBER);

        String phNumber;
        if(!callLogCursor.moveToFirst())
        {
            callLogCursor.close();
            mostCalled = new ArrayList<>();
            circleOutDuration = new ArrayList<>();
            carrierOutCount = new ArrayList<>();
            return 0;
        }
       //V10Log.d(TAG,"Number of Rows = "+callLogCursor.getCount());
        try
        {

            while (callLogCursor.moveToNext())
            {
                type = callLogCursor.getInt(type_index);
                duration = callLogCursor.getInt(duration_index);
                phNumber = callLogCursor.getString(number_index);
                phNumber = phNumber.replace(" ", "");
                if (phNumber.startsWith("+91"))
                {
                    phNumber = phNumber.substring(3);
                }
                if(phNumber.startsWith("0"))
                {
                    phNumber = phNumber.substring(1);
                }
                phNumber = phNumber.replaceAll(" ", "");
                phNumber = phNumber.replaceAll("-", "");

                contactDetail = contactDetailMap.get(phNumber);
                //First Time Get it From Contact Detail Map
                if(contactDetail==null)
                {
                    contactDetail = mContactDetailHelper.getContactDetail(phNumber);
                    contactDetailMap.put(phNumber,contactDetail);
                }

                switch (type)
                {
                    case CallLog.Calls.INCOMING_TYPE:
                        contactDetail.increment_in_count();
                        contactDetail.add_to_in_duration(duration);
                        break;

                    case CallLog.Calls.OUTGOING_TYPE:
                        contactDetail.increment_out_count();
                        contactDetail.add_to_out_duration(duration);
                        break;

                    case CallLog.Calls.MISSED_TYPE:
                        contactDetail.increment_miss_count();
                        break;
                }

            }

            //Process all the ContactDetails
           //V10Log.d(TAG, "Contacts Created = " + contactDetailMap.size());

            List<ContactDetailModel> mList = new ArrayList<>();
            Map<String, MutableInt> carrierMap = new TreeMap<String, MutableInt>();
            Map<String, MutableInt> circleMap = new TreeMap<String, MutableInt>();
            ContactDetailModel temp;
            MutableInt ctemp;
            for (Map.Entry<String, ContactDetailModel> entry : contactDetailMap.entrySet())
            {
                temp = entry.getValue();
                ctemp = carrierMap.get(temp.carrier);
                if(ctemp == null)
                {
                    carrierMap.put(temp.carrier,new MutableInt(temp.out_count));
                }
                else
                {
                    ctemp.add(temp.out_count);
                }
                ctemp = circleMap.get(temp.circle);
                if(ctemp == null)
                {
                    circleMap.put(temp.circle,new MutableInt(temp.out_duration));
                }
                else
                {
                    ctemp.add(temp.out_duration);
                }
                mList.add(temp);
            }
            Collections.sort(mList,new customComparator());
            carrierOutCount = new ArrayList<Tuple>();
            for (Map.Entry<String, MutableInt> entry : carrierMap.entrySet())
            {
                if(entry.getValue().count == 0)
                    continue;
                carrierOutCount.add(new Tuple(entry.getKey(), entry.getValue().count));
            }

            circleOutDuration = new ArrayList<Tuple>();
            for (Map.Entry<String, MutableInt> entry : circleMap.entrySet())
            {
                if(entry.getValue().count == 0)
                    continue;
                //Fastest way to ceil
                circleOutDuration.add(new Tuple(entry.getKey(), ((entry.getValue().count+60-1)/60)));
            }
            mostCalled = new ArrayList<>();
            int len = mList.size();
            int count= 0;
            for(int i=0;i<len;i++)
            {
                //Take only top 5 or less
                if(mList.get(i).out_count == 0)
                    continue;
                count++;
                if(count>5)
                    break;
                mostCalled.add(new Tuple(mList.get(i).name, mList.get(i).out_count));
            }
           //V10Log.d(TAG,"Total ContactDetail List = "+mList.toString());
           //V10Log.d(TAG,"mostCalled List = "+mostCalled.toString());
           //V10Log.d(TAG,"Carrier List = "+carrierOutCount.toString());
           //V10Log.d(TAG, "Circle List = " + circleOutDuration.toString());
            long endTime = System.nanoTime();
           //V10Log.d(TAG, "Filtered Data Took  = " + ((endTime - clockStartTime) / 1000000) + "ms");
        }
        catch (SQLException e)
        {
           //V10e.printStackTrace();
        }
        finally
        {
            callLogCursor.close();
        }
		/*filteredMainMap.clear();
		filteredDateDurationMap.clear();
		MappingHelper mMappingHelper = new MappingHelper();
        Long startDate = params[0];//cal.getTimeInMillis();
      //  Long endDate = new Date().getTime();
        //CallLog.Calls.DATE + ">? AND "+ CallLog.Calls.DATE + "<?", , String.valueOf(endDate)
		Cursor managedCursor = (new CallLogsHelper()).
				getReadableDatabase().
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
/*		// Get Indexes
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
				*//*name_image[0] =
				name_image[1] = getContactsImage(phNumber);*//*

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
		//nameCache.clear();*/
		return 0;
	}

}
