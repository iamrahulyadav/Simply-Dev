package com.builder.ibalance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.builder.ibalance.database.MappingHelper;
import com.builder.ibalance.util.AdvertisingIdClient;
import com.builder.ibalance.util.AdvertisingIdClient.AdInfo;
import com.builder.ibalance.util.MyApplication;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class Wizard extends Activity {
	Spinner carrier, circle, sims;
	String dualSim = "NO";
	String TAG = "Wizard";
	TelephonyManager mtelTelephonyManager;
	SharedPreferences sharedPreferences;
	Button okay;
	String userPhoneNumber = "Not Found";
	String advertisingId ="";
	MappingHelper mMappingHelper ;
	 static Map<String, String> Carriers = new TreeMap<String, String>();
	 static  Map<String, String> Circle = new TreeMap<String, String>();
	 static Map<String, String> Providers = new TreeMap<String, String>();
	 static  Map<String, String> States = new TreeMap<String, String>();
	 String userCarrier = "", userCircle="";//just defaults	
	 @Override
		protected void onCreate(Bundle savedInstanceState) {
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_wizard);
			new Thread(new Runnable() {        
			    public void run() {
			        try {
			            AdInfo adInfo = AdvertisingIdClient.getAdvertisingIdInfo(MyApplication.context);
			            advertisingId = adInfo.getId();
			        } catch (Exception e) {
			           //V10e.printStackTrace();
			        }                       
			    }
			}).start();
			mMappingHelper = new MappingHelper();
			sharedPreferences = getSharedPreferences("USER_DATA",Context.MODE_PRIVATE);
			carrier = (Spinner) findViewById(R.id.carrier_spinner);
			circle = (Spinner) findViewById(R.id.circle_spinner);
			sims = (Spinner) findViewById(R.id.noof_sims_spinner);
			okay = (Button) findViewById(R.id.okay_button);
			initCodes();
			tryToSetCarrierCircleAuto();
			
			
			ArrayAdapter<CharSequence> carrierAdapter = ArrayAdapter.createFromResource(this, R.array.carriers, android.R.layout.simple_spinner_item);
			carrierAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			carrier.setAdapter(carrierAdapter);
			int spinnerPosition = carrierAdapter.getPosition(userCarrier);

			//set the default according to value
			carrier.setSelection(spinnerPosition);
			carrier.setOnItemSelectedListener(new OnItemSelectedListener()  {

				@Override
				public void onItemSelected(AdapterView<?>  parent, View view, 
			            int pos, long id) {
					userCarrier = (String) parent.getItemAtPosition(pos);
					//Log.d(TAG,"UserCarrier  "+ userCarrier);
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					
				}
			});
			ArrayAdapter<CharSequence> circleAdapter = ArrayAdapter.createFromResource(this, R.array.circles, android.R.layout.simple_spinner_item);
			carrierAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			circle.setAdapter(circleAdapter);
			spinnerPosition = circleAdapter.getPosition(userCircle);
			//Log.d(TAG,"spinnerPosition Circle"+spinnerPosition );
			//set the default according to value
			circle.setSelection(spinnerPosition);
			circle.setOnItemSelectedListener(new OnItemSelectedListener()  {

				@Override
				public void onItemSelected(AdapterView<?>  parent, View view, 
			            int pos, long id) {
					userCircle = (String) parent.getItemAtPosition(pos);
					//Log.d(TAG,"userCircle  "+ userCircle);
					
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					
				}
			});
			
			ArrayAdapter<CharSequence> simsAdapter = ArrayAdapter.createFromResource(this, R.array.sims, android.R.layout.simple_spinner_item);
			carrierAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			sims.setAdapter(simsAdapter);
			spinnerPosition = simsAdapter.getPosition("NO");
			//Log.d(TAG,"spinnerPosition sims"+spinnerPosition );
			//set the default according to value
			sims.setSelection(spinnerPosition);
			sims.setOnItemSelectedListener(new OnItemSelectedListener()  {

				@Override
				public void onItemSelected(AdapterView<?>  parent, View view, 
			            int pos, long id) {
					try{dualSim =  (String) parent.getItemAtPosition(pos);
					}
					catch(Exception e)
					{
						dualSim = "NO";
						e.printStackTrace();
					}
					//Log.d(TAG,"dualSim  "+ dualSim);
					
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					
				}
			});
			okay.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					setCarrierCircle();
					startActivity(new Intent(getApplicationContext(),SplashscreenActivity.class));
				}
			});
			

		}
	private void setCarrierCircle() {
		//Log.d(TAG, "Set CARRIER: "+userCarrier);
		//Log.d(TAG, "Set CIRCLE: "+userCircle);
		Editor editor = sharedPreferences.edit();
		editor.putString("CARRIER", userCarrier);
		editor.putString("CIRCLE", userCircle);
		editor.putString("NUMBER", userPhoneNumber);
		editor.putString("DUAL_SIM", dualSim);
		editor.putString("DEVICE_ID", mtelTelephonyManager.getDeviceId());
		editor.putBoolean("WIZARD",true);
		//Log.d("WIZARD", "ALL Set");
		editor.commit();
		ParseObject pObj = new ParseObject("IBALANCE_USERS");
		pObj.put("DEVICE_ID",
				mtelTelephonyManager.getDeviceId());
		pObj.put("SERVICE_STATUS", "OFF");
		pObj.put("CARRIER",sharedPreferences.getString("CARRIER", "Unknown"));
		pObj.put("CIRCLE",sharedPreferences.getString("CIRCLE", "Unknown"));
		Log.d(TAG, "GAID = "+advertisingId);
		pObj.put("GA_ID",advertisingId);
		pObj.put("DUAL_SIM", dualSim);
		try {
			pObj.put("VERSION", getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		pObj.put("NUMBER",sharedPreferences.getString("NUMBER", "Not Found"));
		pObj.saveEventually();
	}
	private void tryToSetCarrierCircleAuto() {
			try {
				//Log.d(TAG, "Not in shared prefs");

				//
				mtelTelephonyManager = (TelephonyManager) this
						.getSystemService(Context.TELEPHONY_SERVICE);
				userPhoneNumber = mtelTelephonyManager.getLine1Number();
				//Log.d(TAG + " Number", userPhoneNumber);
				//Log.d("SHABAZ len = ", userPhoneNumber.length() + " ");

				if (userPhoneNumber.length() > 0) {
					String first4Digits = userPhoneNumber.substring(0, 4);
					//Log.d(TAG + "first4Dig", first4Digits);

					
					
					ArrayList<String> temp = mMappingHelper.getMapping(
							Integer.parseInt(first4Digits));
					userCarrier = Providers.get(temp.get(0));
					userCircle = States.get(temp.get(1));
					//Log.d(TAG + "Provider",userCarrier+temp.get(0) );
					//Log.d(TAG + "State", userCircle+temp.get(1));


				}
				

			} catch (Exception e) {
				//Log.d(TAG, "Problem in setting");
				e.printStackTrace();
			}


		
	}
	void initCodes() {
		Carriers.put( "AIRCEL","AC");
		Carriers.put( "Airtel","AT");
		Carriers.put( "BSNL","CC");
		Carriers.put( "MTNL","DP");
		Carriers.put( "Etisalat","ET");
		Carriers.put( "IDEA","ID");
		Carriers.put( "Loop","LM");
		Carriers.put( "MTS","MT");
		Carriers.put( "PING CDMA","PG");
		Carriers.put( "Reliance","RC");
		Carriers.put( "Spice","SP");
		Carriers.put( "S Tel","ST");
		Carriers.put( "T24)","T24");
		Carriers.put( "TATA DOCOMO","TD");
		Carriers.put( "Tata Indicom","TI");
		Carriers.put( "Uninor","UN");
		Carriers.put( "Virgin","VC");
		Carriers.put( "Vodafone","VF");
		Carriers.put( "Videocon","VD");
		// Circle.clear();
		Circle.put("Andhra Pradesh","AP");
		Circle.put( "Assam","AS");
		Circle.put( "Bihar","BR");
		Circle.put( "Chennai","CH");
		Circle.put( "Delhi","DL");
		Circle.put( "Gujarat","GJ");
		Circle.put( "Himachal Pradesh","HP");
		Circle.put( "Haryana","HR");
		Circle.put( "Jammu and Kashmir","JK");
		Circle.put( "Kerala","KL");
		Circle.put( "Karnataka","KA");
		Circle.put( "Kolkata","KO");
		Circle.put( "Maharashtra","MH");
		Circle.put( "Madhya Pradesh","MP");
		Circle.put( "Mumbai","MU");
		Circle.put( "Arunachal Pradesh (North East India)","NE");
		Circle.put( "Odisha","OR");
		Circle.put( "Punjab","PB");
		Circle.put( "Rajasthan","RJ");
		Circle.put( "Tamil Nadu","TN");
		Circle.put( "Uttar Pradesh (East)","UE");
		Circle.put( "Uttar Pradesh (West)","UW");
		Circle.put( "West Bengal","WB");
		
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
		 //circle.clear();
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
	}


	   @Override
	    protected void onStart() {
	        super.onStart();
	        // Your Code Here
	    }

	    @Override
	    protected void onStop() {
	        super.onStop();
	        // Your Code Here
	    } 

}
