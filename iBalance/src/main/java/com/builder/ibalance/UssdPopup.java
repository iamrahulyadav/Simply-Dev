package com.builder.ibalance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appsflyer.AppsFlyerLib;
import com.apptentive.android.sdk.Apptentive;
import com.builder.ibalance.database.MappingHelper;
import com.builder.ibalance.database.MySQLiteHelper;
import com.builder.ibalance.datainitializers.DataInitializer;
import com.builder.ibalance.util.MyApplication;
import com.builder.ibalance.util.MyApplication.TrackerName;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.kahuna.sdk.KahunaAnalytics;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UssdPopup extends Activity {
	TextView field1,field2,field3,field4,head1,head2,head3,head4;
	Button dismiss,open_app;
	
	 @Override
	    protected void onStart() {
	        super.onStart();
	        KahunaAnalytics.start();
	        // Your Code Here
	    }

	    @Override
	    protected void onStop() {
	        super.onStop();
	        KahunaAnalytics.stop();
	        // Your Code Here
	    }

    public static ArrayList<String> getContactNamePicture( String number) {
        String name="Unkown",photo_uri=null;
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
        name_photo.add(name);
        name_photo.add(photo_uri);
        return name_photo;


    }
    ArrayList<String> getCarrieCircleTotalCost(String number)
    {
        //I need total duration called, call rate from shared pref, mapping of first 4 digits to short cuts,
        // Providers and State map to convert from short form to readable form
        //Call database table to know the tracked rate, the duration for how much it id tracked and have to calculate the total spent using that
        ArrayList<String> returnList = new ArrayList<>();
        MappingHelper mMappingHelper = new MappingHelper(MyApplication.context);
        if (number.startsWith("+91"))
            number = number.substring(3).replaceAll(" ","");
        else
            number = number.replaceAll(" ","");
        String ph ="0000";
        try {
            ph = number.substring(number.length() - 10,
                    number.length() - 6);
        } catch (Exception e1) {
            ph = "0000";
        }
        ArrayList<String> x = (ArrayList<String>) mMappingHelper.getMapping(Integer.parseInt(ph));
        // //Log.d("Data mapping",x.get(0)+"  "+x.get(1));
        returnList.add(DataInitializer.Providers.get(x.get(0)));
        returnList.add(DataInitializer.States.get(x.get(1)));
        returnList.add("N/A");
        if(DataInitializer.mainmap == null)
        {
            //Loading the whole data will take lot of time so skip for now
            return returnList;
        }
        MySQLiteHelper mSQLiteHelper = MySQLiteHelper.getInstance(this);
        SQLiteDatabase db = mSQLiteHelper.getReadableDatabase();
        String query = "select sum(COST), sum(DURATION) from CALL where NUMBER = \'+91"+number+"\'" + " OR NUMBER =\'"+number+"\'";
        //Log.d("Contacts Loader", "Query = "+ query);
        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        int duration= 0;
        float callCost = (float) 0.0;
        try{
            callCost = c.getFloat(0);
            duration = c.getInt(1);


        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if(c!=null)
                c.close();
            if(db!=null)
                db.close();
        }
        //Start calculation using total call duration
        Object[] number_details = DataInitializer.mainmap.get(number);
        String totalDuration = "0";
        if(number_details!=null)
        {
            // name,InCount,InDur,OutCount,OutDur,MissCount,Provider,State,imageUuri
            try
            {
                totalDuration = (int)number_details[4]+"";
                if(number.length()<10)
                {
                    callCost = 0;
                }
                else
                {
                    SharedPreferences mSharedPreferences = MyApplication.context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
                    float call_rate = mSharedPreferences.getFloat("CALL_RATE", 1.7f);
                    callCost = (float) ((float)(Integer.parseInt(totalDuration)-duration)*call_rate/100) + callCost;
                }
                returnList.add(2,String.format("%.2f",callCost));
            }
            catch (Exception e)
            {

            }
        }

        return returnList;
    }
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.ussd_popup);
		Typeface tf = Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf");
		Intent mintent = this.getIntent();
		field1 = (TextView)findViewById(R.id.field1);
		field1.setTypeface(tf);
		field2 =(TextView)findViewById(R.id.field2);
		field2.setTypeface(tf);
        field3 = (TextView)findViewById(R.id.field3);
		field3.setTypeface(tf);
        field4 = (TextView)findViewById(R.id.field4);
		field4.setTypeface(tf);
		head1 = (TextView)findViewById(R.id.head1);
		head1.setTypeface(tf);
		head2 =(TextView)findViewById(R.id.head2);
		head2.setTypeface(tf);
		head3 = (TextView)findViewById(R.id.head3);
		head3.setTypeface(tf);
		head4 = (TextView)findViewById(R.id.head4);
		head4.setTypeface(tf);
		/* NORMAL_CALL,//1
		 NORMAL_SMS,//2
		 NORMAL_DATA,//3
		 VOICE_PACK,//4
		 SMS_PACK,//5
		 DATA_PACK,//6
		 BALANCE,//7
	
*/

        String rupee_symbol = getResources().getString(R.string.rupee_symbol);
		switch (mintent.getIntExtra("TYPE", -1)) {
		case 1:
            View contactLayout = findViewById(R.id.ussd_contact_layout);
            contactLayout.setVisibility(View.VISIBLE);
            ImageView contact_picture = (ImageView) contactLayout.findViewById(R.id.ussd_contact_picture);
            TextView contact_name = (TextView) contactLayout.findViewById(R.id.ussd_contact_name);
            TextView contact_number = (TextView) contactLayout.findViewById(R.id.ussd_contact_number);
            TextView contact_carrier_circle = (TextView) contactLayout.findViewById(R.id.ussd_contact_carrier_circle);
            TextView contact_total_spent = (TextView) contactLayout.findViewById(R.id.ussd_contact_total_spent);
            TextView contact_call_cost = (TextView) contactLayout.findViewById(R.id.ussd_contact_call_cost);
			String cost = mintent.getStringExtra("CALL_COST");
			head1.setText("Call Cost");
			field1.setText(rupee_symbol+" "+cost);

			String balance = mintent.getStringExtra("BALANCE");
			head2.setText("Current Balance");
			field2.setText(rupee_symbol+" "+balance);
			
			String duration = mintent.getStringExtra("CALL_DURATION");
			head3.setText("Call Duration");
			field3.setText(getTimeFormatted(Integer.parseInt(duration)));
			String call_rate;
			try{
			 call_rate = String.format("%.1f", (Float.parseFloat(cost)/Float.parseFloat(duration))*100)+ " p/s";
			}
			catch(Exception e)
			{
				call_rate = "--";
			}
			head4.setText("Call Rate");
			field4.setText(call_rate);

            String number = mintent.getStringExtra("NUMBER");
            ArrayList<String> name_photo = getContactNamePicture(number);
            contact_name.setText(name_photo.get(0));
            Picasso.with(this).load(name_photo.get(1)).placeholder(R.drawable.default_contact_picture).into(contact_picture);
            contact_number.setText(number);
            ArrayList<String> carrier_circle_totalcost = getCarrieCircleTotalCost(number);
            contact_carrier_circle.setText(carrier_circle_totalcost.get(0)+","+carrier_circle_totalcost.get(1));
            contact_total_spent.setText(rupee_symbol+" "+carrier_circle_totalcost.get(2));
            contact_call_cost.setText("+ "+cost);
			break;
			//Normal SMS
		case 2:
			 cost = mintent.getStringExtra("SMS_COST");
			head1.setText("SMS Cost");
			field1.setText(rupee_symbol+" "+cost);
			balance = mintent.getStringExtra("BALANCE");
			head2.setText("Current Balance");
			field2.setText(rupee_symbol+" "+balance);
			((LinearLayout)findViewById(R.id.layout3)).setVisibility(View.GONE);
			String sms_rate = "0.0";
			try{
			 sms_rate = String.format("%.1f", (Float.parseFloat(cost)))+" "+ rupee_symbol+"/SMS";
			}
			catch(Exception e)
			{
				sms_rate = "--";
			}
			head4.setText("SMS Rate");
			field4.setText(sms_rate);
			break;
			//Normal Data
		case 3:
			cost = mintent.getStringExtra("DATA_COST");
			head1.setText("Data Cost");
			field1.setText(rupee_symbol+" "+cost);

			balance = mintent.getStringExtra("BALANCE");
			head2.setText("Current Balance");
			field2.setText(rupee_symbol+" "+balance);
			
			String data_consumed = mintent.getStringExtra("DATA_CONSUMED");
			head3.setText("Data Used");
			field3.setText(data_consumed+" MB");
			String data_rate;
			try{
				data_rate = String.format("%.1f", (Float.parseFloat(cost)/Float.parseFloat(data_consumed)))+ " p/10KB";
			}
			catch(Exception e)
			{
				data_rate = "--";
			}
			head4.setText("Data Rate");
			field4.setText(data_rate);
			break;
			//Data Pack
		case 6:
			 data_consumed = mintent.getStringExtra("DATA_CONSUMED");
			head1.setText("Data Used");
			field1.setText(data_consumed+" MB");

			String data_left = mintent.getStringExtra("DATA_LEFT");
			head2.setText("Data Left");
			field2.setText(data_left+" MB");
			
			balance = mintent.getStringExtra("BALANCE");
			head3.setText("Current Balance");
			field3.setText(rupee_symbol+" "+balance);
			head4.setText("Valid Till");
			field4.setText(mintent.getStringExtra("VALIDITY"));
			break;
		default:
			break;
		}
		
		dismiss = (Button) findViewById(R.id.ussd_dismiss);
		ImageView infoButton = (ImageView) findViewById(R.id.info_button);
		open_app = (Button) findViewById(R.id.ussd_open_app);
		infoButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Toast.makeText(getApplicationContext(), "This is a Custom message,With call rate info.",Toast.LENGTH_LONG).show();
				
			}
		});
		dismiss.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
				
			}
		});
		open_app.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent mIntent = new Intent(getApplicationContext(),SplashscreenActivity.class);
				mIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
				startActivity(mIntent);
				Tracker t = ((MyApplication) MyApplication.context).getTracker(
					    TrackerName.APP_TRACKER);
				t.send(new HitBuilders.EventBuilder()
			    .setCategory("APP_OPEN")
			    .setAction("POPUP")
			    .setLabel("POPUP")
			    .build());
				Apptentive.engage(UssdPopup.this, "POPUP_APP_OPEN");
				FlurryAgent.logEvent("POPUP_APP_OPEN");
				AppsFlyerLib.sendTrackingWithEvent(getApplicationContext(),"POPUP_APP_OPEN","");
     			finish();
				
			}
		});
	}
	private String getTimeFormatted(int totalSecs) {
		String min,sec,hr;
		Integer hrs,mins,secs;
		secs = totalSecs % 60;
		if(secs<10)
			sec = "0"+secs;
		else
			sec = ""+secs;
		totalSecs = totalSecs/60;
		mins = totalSecs %60;
		if(mins<10)
			min = "0"+mins;
		else
			min = ""+mins;
		totalSecs = totalSecs/60;
		hrs = totalSecs;
		if(hrs<10)
			hr = "0"+hrs;
		else
			hr = ""+hrs;
		return hr+":"+min+":"+sec;
	}
	

}
