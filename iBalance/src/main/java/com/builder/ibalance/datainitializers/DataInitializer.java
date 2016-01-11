package com.builder.ibalance.datainitializers;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
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
import com.builder.ibalance.database.helpers.MappingHelper;
import com.builder.ibalance.database.helpers.BalanceHelper;
import com.builder.ibalance.database.helpers.CallLogsHelper;
import com.builder.ibalance.database.helpers.IbalanceContract;
import com.builder.ibalance.database.models.ContactDetailModel;
import com.builder.ibalance.database.models.DateDurationModel;
import com.builder.ibalance.messages.DataLoadingDone;
import com.builder.ibalance.models.USSDModels.NormalCall;
import com.builder.ibalance.util.Helper;
import com.builder.ibalance.util.MyApplication;
import com.crashlytics.android.Crashlytics;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import de.greenrobot.event.EventBus;


public class DataInitializer extends AsyncTask<Void, Integer, Integer> {
	//static Context context;
	public static volatile boolean done = false;
    int sim_slot = 0;
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
	public static Map<String, String> carriers = new TreeMap<String, String>();
    public static Map<String, String> circle = new TreeMap<String, String>();
	public static Map<Integer, ArrayList<String>> Cache = new TreeMap<Integer, ArrayList<String>>();
	public static Map<String, String[]> nameCache = new TreeMap<String, String[]>();
	public static Map<String, Object[]> mainmap = new TreeMap<String, Object[]>();
	public static Map<String, ContactDetailModel> contactDetailMap = new TreeMap<String, ContactDetailModel>();
	public static Map<Date, ArrayList<Integer>> dateDurationMap = new TreeMap<Date, ArrayList<Integer>>();
	public static Map<String, SmsLogs> smsMap = new TreeMap<String, SmsLogs>();
	public static List<NormalCall> ussdDataList = new LinkedList<NormalCall>();
    SharedPreferences mSharedPreferences = MyApplication.context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
	public static void initializeUSSDData(int sim_slot) {
		
		
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
        ussdDataList =( mBalanceHelper.getEntriesFromDate(fromDate.getTime(),sim_slot));
		 //Log.d(TAG,ussdDataList.toString());
	}
    /* (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        //V10Log.d(TAG, "POSTING DataLoadingDone Event");
        EventBus.getDefault().postSticky(new DataLoadingDone());
        //new SmsDataInitializer().execute();
    }
	@Override
	protected Integer doInBackground(Void... p) {
		{
            long startTime = System.nanoTime();
			// Cache.clear();
			done=false;


            InitializeData();

            //Copy the whole call log + create Main Map and Date Duration Map
            updateDetails();

			updateWidgetInitially(MyApplication.context);
			done = true;
            long endTime = System.nanoTime();
          //V12//V16Log.d(TAG, "DataInitializer Took Totally = " + ((endTime - startTime) / 1000000) + "ms");

			//Log.d("DataInit", "WORKING IN InitializeSmsMap");
			//InitializeSmsMap(MyApplication.context);
            //updateLocalDetails();
            //Log.d("DataInit", "WORKING IN InitializeMap");
            //InitializeMap(MyApplication.context);
            //Log.d("DataInit", "WORKING IN updateWidgetInitially");
		}
		return 0;
	}

    private void updateDetails()
    {
        long startTime = System.nanoTime();
        long last_indexed_id = mSharedPreferences.getLong("INDEXED_ID", -1l);
        boolean firstTime = mSharedPreferences.getBoolean("FIRST_TIME", true);
        /*//for US
        last_indexed_id = Long.MAX_VALUE;*/
       //V16Log.d(TAG, "INDEXED ID = " + last_indexed_id);
        CallLogsHelper mCallLogsHelper = new CallLogsHelper();
        Cursor callLogCursor = mCallLogsHelper.getAllSystemCallLogs(last_indexed_id);
        int num_rows = callLogCursor.getCount();
       //V16Log.d(TAG,"Number of Rows = "+num_rows);
        if(num_rows==0)
            return;
        int slot,duration,type;
        Calendar c =Calendar.getInstance();
        DateDurationModel dateDurationModel = new DateDurationModel(0l,0,0,0,0,0,0);
        ContactDetailModel contactDetail;
        int id_index = callLogCursor.getColumnIndex(CallLog.Calls._ID);
        int date_index =callLogCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration_index =callLogCursor.getColumnIndex(CallLog.Calls.DURATION);
        int type_index =callLogCursor.getColumnIndex(CallLog.Calls.TYPE);
        int number_index =callLogCursor.getColumnIndex(CallLog.Calls.NUMBER);
        long date,id ;
        long curr_date = 0l;
        //Update: did a better hack B-) sum it in a loop and if first time just insert else get and then add
        int total_duration = 0;


        String number,normalizedNumber,query="",query_format = "INSERT INTO " +
                IbalanceContract.CallLogEntry.TABLE_NAME +
                "("+
                IbalanceContract.CallLogEntry.COLUMN_NAME_ID+","+
                IbalanceContract.CallLogEntry.COLUMN_NAME_SLOT+","+
                IbalanceContract.CallLogEntry.COLUMN_NAME_DATE+","+
                IbalanceContract.CallLogEntry.COLUMN_NAME_DURATION+","+
                IbalanceContract.CallLogEntry.COLUMN_NAME_TYPE+","+
                IbalanceContract.CallLogEntry.COLUMN_NAME_NUMBER+
                ") " +
                "VALUES ( ? , ? , ? , ? , ? , ? )";

        if(!callLogCursor.moveToFirst())
        {
            callLogCursor.close();
            return;
        }
        SQLiteStatement callLogInsertStatement = mCallLogsHelper.getWriteableDatabase().compileStatement(query_format);
        mMappingHelper = new MappingHelper();
       //V10Log.d(TAG,"Number of Rows = "+callLogCursor.getCount());
        mCallLogsHelper.getDatabase().beginTransaction();
        try
        {
            date = callLogCursor.getLong(date_index) + 19800l;
            if(firstTime)
            {
                mSharedPreferences.edit().putLong("FIRST_DATE", date).commit();
            }
            c.setTimeInMillis(date);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            curr_date = c.getTimeInMillis();
            if(!firstTime)
            {
                dateDurationModel = mCallLogsHelper.getDateDurationModel(curr_date);
            }
            //Sun -1 ... Sat -7
            dateDurationModel.setDay_of_the_week(c.get(Calendar.DAY_OF_WEEK));

            do
            {
                type = callLogCursor.getInt(type_index);
                duration = callLogCursor.getInt(duration_index);
                if (type > 4)
                {
                    //If Call Log type is other than incoming,Outgoing,Missed and Voice Mail then skip
                    continue;
                }
                if (type == CallLog.Calls.OUTGOING_TYPE)
                {
                    if (duration <= 0)
                    {
                        //If outgoing is 0s then its call didn't connect so skip it
                        continue;
                    }
                }
                id = callLogCursor.getLong(id_index);
                //Converting to IST
                date = callLogCursor.getLong(date_index) +19800l;
                number = callLogCursor.getString(number_index);
                slot = mCallLogsHelper.getSlot(callLogCursor);
                number = number.replace(" ", "");

                c.setTimeInMillis(date);
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                if (c.getTimeInMillis() > curr_date)
                {
                    curr_date = c.getTimeInMillis();
                    mCallLogsHelper.executeQuery(dateDurationModel.toString());
                    dateDurationModel.clear();
                    dateDurationModel.setDate(curr_date);
                    dateDurationModel.setDay_of_the_week(c.get(Calendar.DAY_OF_WEEK));
                }
                if(!firstTime)
                {
                    normalizedNumber = Helper.normalizeNumber(number);
                    //Check if you got the number in the previous pass
                    contactDetail = contactDetailMap.get(normalizedNumber);
                    if(contactDetail == null)
                    {
                        //If nt try to get from DB
                        contactDetail = mCallLogsHelper.getContactDetailFromDb(normalizedNumber);
                        if (contactDetail == null )
                        {
                            //If its a new Number then Get fresh Details
                            contactDetail = getContactDetails(number);
                        }
                        //Put the Details Fetched from Db into Map
                        contactDetailMap.put(normalizedNumber, contactDetail);
                    }
                }
                else
                {
                    //First Time Get it Fresh
                    contactDetail = getContactDetails(number);
                }
                switch (type)
                {
                    case CallLog.Calls.INCOMING_TYPE:
                        dateDurationModel.increment_in_count();
                        dateDurationModel.add_to_in_duration(duration);
                        contactDetail.increment_in_count();
                        contactDetail.add_to_in_duration(duration);
                        break;

                    case CallLog.Calls.OUTGOING_TYPE:
                        dateDurationModel.increment_out_count();
                        total_duration += duration;
                        dateDurationModel.add_to_out_duration(duration);
                        contactDetail.increment_out_count();
                        contactDetail.add_to_out_duration(duration);
                        break;

                    case CallLog.Calls.MISSED_TYPE:
                        dateDurationModel.increment_miss_count();
                        contactDetail.increment_miss_count();
                        break;
                }

                callLogInsertStatement.bindLong(1,id);
                callLogInsertStatement.bindLong(2,slot);
                callLogInsertStatement.bindLong(3,date);
                callLogInsertStatement.bindLong(4,duration);
                callLogInsertStatement.bindLong(5,type);
                callLogInsertStatement.bindString(6,number);

                callLogInsertStatement.executeInsert();
                //query = query_format + "(" + id + "," + slot + "," + date + "," + duration + "," + type + ", '" + number + "')";
                //Log.d(tag,"Query = "+query);
               // mCallLogsHelper.executeQuery(query + "");
            } while (callLogCursor.moveToNext());
            //Enter the last Entry
            mCallLogsHelper.executeQuery(dateDurationModel.toString());
            dateDurationModel.clear();
            callLogCursor.moveToPrevious();
            long indexed_id = callLogCursor.getLong(id_index);
           //V10Log.d(TAG, "INDEXED ID = " + indexed_id);
            Editor mEditor = mSharedPreferences.edit();
            mEditor.putLong("INDEXED_ID", indexed_id);
            mEditor.putBoolean("FIRST_TIME", false);
            total_duration += mSharedPreferences.getInt("TOTAL_OUT_DURATION",0);
            mEditor.putInt("TOTAL_OUT_DURATION", total_duration);
            mEditor.commit();
            callLogCursor.close();
            //Write All ContactDetails to Database
            if (firstTime)
            {
               //V10Log.d(TAG,"Contacts Created = "+contactDetailMap.size());
                //add total duration for first time
                for (Entry<String, ContactDetailModel> entry : DataInitializer.contactDetailMap.entrySet())
                {
                    mCallLogsHelper.insert(entry.getValue());
                }
            }
            else
            {
                for (Entry<String, ContactDetailModel> entry : DataInitializer.contactDetailMap.entrySet())
                {
                    mCallLogsHelper.insertOrReplace(entry.getValue());
                }
            }
            mCallLogsHelper.getDatabase().setTransactionSuccessful();
            long endTime = System.nanoTime();
          //V12//V16Log.d(TAG, "CreateTotalDetails Took  = " + ((endTime - startTime) / 1000000) + "ms");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        finally
        {
            mCallLogsHelper.getDatabase().endTransaction();
        }
    }

    public ContactDetailModel getContactDetails(String phNumber)
    {
        if(phNumber == null)
            return new ContactDetailModel("000000","Unknown","Unknown","Unknown","",0,0,0,0,0);

        String[] name_image;
        ContactDetailModel contactDetail;
        String original_number = phNumber;
        if (phNumber.startsWith("+91"))
        {
            phNumber = phNumber.substring(3);
        }
        if(phNumber.startsWith("0"))
        {
            phNumber = phNumber.substring(1);
        }
        phNumber = phNumber.replaceAll(" ","");
        phNumber = phNumber.replaceAll("-", "");
        contactDetail = contactDetailMap.get(phNumber);
        if(contactDetail == null)
        {
            //Get Name and Image URI
            name_image = nameCache.get(original_number);
            if (name_image == null) {
                name_image = new String[2];
                name_image =  getContactName(original_number).toArray(name_image);

                if (name_image[0] == "")
                    name_image[0] = "Unknown";
                nameCache.put(original_number, name_image);
            }


            //Get Carrier and Circle
            String ph = "0000",carrier = "Unknown",circle = "Unknown";
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
                carrier = DataInitializer.carriers.get(x.get(0));
                circle =DataInitializer.circle.get( x.get(1));
                // //Log.d("cache", "from cache");
            } catch (Exception e) {
                try {
                    // if not there in cache, extract
                    ArrayList<String> x = (ArrayList<String>) mMappingHelper.getMapping(Integer.parseInt(ph));
                    // //Log.d("Data mapping",x.get(0)+"  "+x.get(1));
                    carrier =DataInitializer.carriers.get(x.get(0));
                    circle = DataInitializer.circle.get(x.get(1));
                    if(carrier==null)
                        carrier = "Unkown";
                    if(circle == null)
                        circle = "Unknown";
                    DataInitializer.Cache.put(Integer.parseInt(ph), x);

                    // //Log.d("cache", "from num");

                } catch (Exception ee) {
                    // if not found
                    if (ph.length() >= 10)
                        carrier = "Land Line";
                    else
                        circle = "Unknown";
                }
            }
            if(carrier==null)
                carrier = "Unkown";
            if(circle == null)
                circle = "Unknown";
            contactDetail = new ContactDetailModel(phNumber,name_image[0],carrier,circle,name_image[1],0,0,0,0,0);
            contactDetailMap.put(phNumber,contactDetail);
        }
        return contactDetail;
    }


    /*public static void InitializeMap(Context context) {
        long startTime = System.nanoTime();
        SharedPreferences mSharedPreferences = context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
       //V10Log.d(TAG,"InitializeMap");
        Long startDate = 0l;//cal.getTimeInMillis();
        // Cache.clear();
        //Log.d("DataInit", "InitializeMap");

        if (!mainmap.isEmpty() && !dateDurationMap.isEmpty()) {
            //Log.d(TAG, "Main and dateDurationMap Map Exists");
            startDate = mSharedPreferences.getLong("CACHE_DATE", 0l);
            //Log.d("TEST", "Logging from "+startDate);
        }
		*//*mainmap.clear();
		dateDurationMap.clear();*//*
        mMappingHelper = new MappingHelper();
        //  Long endDate = new IndianDate().getTime();
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
       //V10Log.d(TAG,"QUERY Returned "+managedCursor.getCount());
		*//*Cursor managedCursor = context.getContentResolver().query(
				CallLog.Calls.CONTENT_URI,
				new String[] {CallLog.Calls.NUMBER,CallLog.Calls.DATE,CallLog.Calls.TYPE,CallLog.Calls.DURATION},
				CallLog.Calls.DATE + ">?",
				new String[] { String.valueOf(startDate)}, CallLog.Calls.DATE + " ASC");*//*
        Editor mEditor = mSharedPreferences.edit();
        mEditor.putLong("CACHE_DATE", (new Date()).getTime());
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
            if(phNumber == null)
                continue;
            name_image = nameCache.get(phNumber);
            if (name_image == null) {
                name_image = new String[2];
                name_image =  getContactName(phNumber).toArray(name_image);
				*//*name_image[0] =
				name_image[1] = getContactsImage(phNumber);*//*

                if (name_image[0] == "")
                    name_image[0] = "Unknown";
                nameCache.put(phNumber, name_image);
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
                        DataInitializer.Cache.put(Integer.parseInt(ph), x);

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
        //SharedPreferences mSharedPreferences = context.getSharedPreferences("USER_DATA",Context.MODE_PRIVATE);
        // mEditor = mSharedPreferences.edit();
        if(startDate==0l)
        {
            //Log.d(TAG, "Shared Pref TOTAL_OUT_DURATION = "+total_callDuration);
            mEditor.putInt("TOTAL_OUT_DURATION", total_callDuration);
            Date dt =new Date(Long.parseLong("1420050619800"));
            for (Entry<Date, ArrayList<Integer>> entry : DataInitializer.dateDurationMap
                    .entrySet()) {
                dt = entry.getKey();
                break;
            }
            //Log.d(TAG, "First Date = "+dt.toString());
            mEditor.putLong("FIRST_DATE", dt.getTime());
            mEditor.commit();
        }

        long endTime = System.nanoTime();
       //V10Log.d(TAG, "Initialize Map Took  = " + ((endTime - startTime) / 1000000) + "ms");
    }*/






	private void updateWidgetInitially(Context ctx) {
        //Todo make widget dependant on Sim
		//Log.d("DataInit","Came to update  widget inittially "+ "iin Data initializer");
		AppWidgetManager mgr=AppWidgetManager.getInstance(ctx);
		
		ComponentName thisWidget = new ComponentName(ctx,
	    		BalanceWidget.class);
	    SharedPreferences mSharedPreferences = ctx.getSharedPreferences("USER_DATA",Context.MODE_PRIVATE);
	      float currBalance = mSharedPreferences.getFloat("CURRENT_BALANCE_"+sim_slot, (float) -1.0);
	     //V10Log.d("DataInit widget bal ", ""+ currBalance);
	      // Set the text
	      
	      int predictedDays = -1 ;
      long firstDate = mSharedPreferences.getLong("FIRST_DATE", Long.parseLong("1420050619800"));
	      
	      int total_out_duration = mSharedPreferences.getInt("TOTAL_OUT_DURATION", 100);
	     //V10Log.d(TAG, "Toatl OUT Duration = "+total_out_duration);
	      
	      float call_rate = mSharedPreferences.getFloat("CALL_RATE", (float)1.7);
	     //V10Log.d(TAG, "CALL_RATE= "+call_rate);
	      
	      int numberOfDays = (int)( (new Date().getTime()+19800l - firstDate) / (1000 * 60 * 60 * 24));
	     //V10Log.d(TAG, "No of Days = "+ numberOfDays);
	      
	      float total_cost_inPaise = total_out_duration*call_rate;
	     //V10Log.d(TAG, "Toatal cost = "+total_cost_inPaise);
	       predictedDays = (int) (currBalance*numberOfDays/(total_cost_inPaise/100));
	     //V10Log.d(TAG, "predicted DAys = "+predictedDays);
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
	      if(predictedDays <= -1)
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
		mMappingHelper = new MappingHelper();

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
						Provider = DataInitializer.carriers.get(x.get(0));
						State = DataInitializer.circle.get(x.get(1));
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
				double numberOfMessages = messageLength / 153.0d;
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
		// carriers.clear();
		carriers.put("AC", "Aircel");
		carriers.put("AT", "Airtel");
		carriers.put("CC", "BSNL");
		carriers.put("DP", "MTNL");
		carriers.put("ET", "Etisalat-DB");
		carriers.put("ID", "Idea");
		carriers.put("LM", "Loop");
		carriers.put("MT", "MTS");
		carriers.put("PG", "PING CDMA");
		carriers.put("RC", "Reliance-GSM");
		carriers.put("SP", "Spice");
		carriers.put("ST", "S-Tel");
		carriers.put("T24", "T24");
		carriers.put("TD", "Tata-Docomo");
		carriers.put("TI", "Tata Indicom");
		carriers.put("UN", "Uninor");
		carriers.put("VC", "Virgin");
		carriers.put("VF", "Vodafone");
		carriers.put("VD", "Videocon");
		// circle.clear();
		circle.put("AP", "Andhra Pradesh");
		circle.put("AS", "Assam");
		circle.put("BR", "Bihar-Jharkhand");
		circle.put("CH", "Chennai");
		circle.put("DL", "Delhi-NCR");
		circle.put("GJ", "Gujarat");
		circle.put("HP", "Himachal Pradesh");
		circle.put("HR", "Haryana");
		circle.put("JK", "Jammu-Kashmir");
		circle.put("KL", "Kerala");
		circle.put("KA", "Karnataka");
		circle.put("KO", "Kolkata");
		circle.put("MH", "Maharashtra-Goa");
		circle.put("MP", "Madhya Pradesh-Chattisgarh");
		circle.put("MU", "Mumbai");
		circle.put("NE", "North East");
		circle.put("OR", "Orissa");
		circle.put("PB", "Punjab");
		circle.put("RJ", "Rajasthan");
		circle.put("TN", "Tamil Nadu");
		circle.put("UE", "UP(EAST))");
		circle.put("UW", "UP(WEST)-Uttarakhand");
		circle.put("WB", "West Bengal");
		circle.put("ZZ", "Customer Care (All Over India)");

		// Map<Integer, ArrayList<String>> Num = new TreeMap<Integer,
		// ArrayList<String>>();
		// Num.clear();

	}



	public static ArrayList<String> getContactName( String number) {
		String name="",photo_uri=null;
		ArrayList<String> name_photo = new ArrayList<String>();
		Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
		Cursor contactLookup;
        if(number.contains("135514"))
        {
         int i = 0;
        }
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
	}



	

}
