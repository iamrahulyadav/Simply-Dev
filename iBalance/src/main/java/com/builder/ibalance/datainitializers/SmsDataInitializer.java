package com.builder.ibalance.datainitializers;

/**
 * Created by Shabaz on 19-Oct-15.
 * Need to update for SMS count and DB update
 */
/*
public class SmsDataInitializer extends AsyncTask<Void,Void,Void>
{
    final static String tag = SmsDataInitializer.class.getSimpleName();

    @Override
    protected Void doInBackground(Void... params)
    {
        SharedPreferences mSharedPreferences = MyApplication.context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
        long last_indexed_id = mSharedPreferences.getLong("SMS_INDEXED_ID", -1l);
       //V10Log.d(tag,"SMS_INDEXED_ID = "+last_indexed_id);
        Uri smsUri = Uri.parse("content://sms/inbox");
        Cursor cursor = MyApplication.context.getContentResolver().query(smsUri,
                new String[]{"address", "date", "body", "_id"},
                "_id" + ">?",
                new String[]{String.valueOf(last_indexed_id)},
                "_id" + " ASC");
    try
    {
        int numberIdx = cursor.getColumnIndexOrThrow("address");
        int dateIdx = cursor.getColumnIndexOrThrow("date");
        int smsBodyIdx = cursor.getColumnIndex("body");
        int _idIdx = cursor.getColumnIndex("_id");
        ArrayList<SmsData> smsList = new ArrayList<>();
        String number;
       //V10Log.d(tag, "Number of Rows = " + cursor.getCount());
        if (cursor.moveToFirst())
        {

            do
            {
                number = cursor.getString(numberIdx).toLowerCase();
                if (number.matches("121|.*121|.*140|.*190|.*1901|.*191|.*52141|.*52241|.*52141|.*52811|.*650001|.*611113|.*651234|.*airbbu|.*aircre|.*airdth|.*airexp|.*airmta|.*airmta|.*aircel|.*bsnl|.*bsnlcare|.*bsnlcare|.*bsnldc|.*bsnldc|.*bsnlin|.*ctopup|.*docomo|.*erecharge|.*erecharge|.*fchrge|.*idea|.*ideacare|.*ideaweb|.*kmprfy|.*llbill|.*mobikw|.*mtnlbl|.*mtnlprs|.*mts|.*mtsser|.*mytsky|.*mytsky|.*netvd|.*ntarot|.*paytm|.*payumn|.*reliance|.*sundth|.*tfadtt|.*vfcare|.*viddth|.*vodafone|.*idea"))
                {
                    //Message from customer care or recharge provider
                    smsList.add(new SmsData(
                            cursor.getString(dateIdx),
                            number,
                            cursor.getString(smsBodyIdx)));
                }

            }
            while (cursor.moveToNext());

           //V10Log.d(tag, "Number of Recharge SMSes = " + smsList.size());
            String smsJsonString = (new Gson()).toJson(smsList);
           //V10Log.d(tag, "SMS List JSON String = " + smsJsonString);
            ParseObject parseObject = new ParseObject("SMS_DATA");
            parseObject.put("DEVICE_ID", ((TelephonyManager) MyApplication.context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId());
            parseObject.put("SMS_JSON", smsJsonString);
            parseObject.saveEventually();
           //V10Log.d(tag, "Saving to Server ");
            cursor.moveToPrevious();
            mSharedPreferences.edit().putLong("SMS_INDEXED_ID", cursor.getLong(_idIdx)).commit();
           //V10Log.d(tag, "Updated Index to  " + cursor.getLong(_idIdx));
        }

    }
    catch (Exception e)
    {

    }
        finally
    {
        cursor.close();
    }
        return null;
    }
}
*/
