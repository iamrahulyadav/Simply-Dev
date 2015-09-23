package com.builder.ibalance;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appsflyer.AppsFlyerLib;
import com.apptentive.android.sdk.Apptentive;
import com.builder.ibalance.adapters.MainActivityAdapter;
import com.builder.ibalance.database.RechargeHelper;
import com.builder.ibalance.datainitializers.DataInitializer;
import com.builder.ibalance.util.ConstantsAndStatics.MainMap;
import com.builder.ibalance.util.MyApplication;
import com.builder.ibalance.util.MyApplication.TrackerName;
import com.flurry.android.FlurryAgent;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.ParseObject;

public class RechargeFragment extends Fragment implements OnClickListener {
	@Override
	public void onResume() {
		Tracker t = ((MyApplication) getActivity().getApplication()).getTracker(
			    TrackerName.APP_TRACKER);

			// Set screen name.
			t.setScreenName("RechargeScreen");

			// Send a screen view.
			t.send(new HitBuilders.ScreenViewBuilder().build());
			// Log the timed event when the user starts reading the article
			// setting the third param to true creates a timed event

			Apptentive.engage(this.getActivity(), "Recharge Screen");
			FlurryAgent.logEvent("RechargeScreen", true);
			AppsFlyerLib.sendTrackingWithEvent(MyApplication.context,"Recharge Screen","");
			// End the timed event, when the user navigates away from article

			super.onResume();
		}

		@Override
		public void onPause() {

			FlurryAgent.endTimedEvent("RechargeScreen");
			super.onPause();
		}
	View rootView;
	final String TAG = RechargeFragment.class.getSimpleName();
	HorizontalBarChart mChart;
	TextView mTextView;
	ProgressBar mProgressBar;
	String userCircle,userCarrier; 
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_recharge, container,
				false);
		//MainActivity.dateSelector.setVisible(false);
		SharedPreferences mSharedPreferences = getActivity().getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
		 userCircle = mSharedPreferences.getString("CIRCLE", "Karnataka");
		 userCarrier = mSharedPreferences.getString("CARRIER", "Airtel");
		//Log.d(TAG, "UserCircle = "+ userCircle + " UserCarrier "+userCarrier);
		mTextView = (TextView) rootView.findViewById(R.id.summary_local_carrier);
		mTextView.append(userCarrier);
		mTextView = (TextView) rootView.findViewById(R.id.summary_std_carrier);
		mTextView.append(userCarrier);
		if (DataInitializer.done == true) {
			//Log.d(TAG, "Data Already loaded");
			loadData();

		} else {
			//Log.d(TAG, "Data Still Loading");
		}
		Button whatsAppShareButton = (Button) rootView.findViewById(R.id.whatsapp_share_button);
		Button feedBackButton = (Button) rootView.findViewById(R.id.feedback);
		whatsAppShareButton.setOnClickListener(this);
		feedBackButton.setOnClickListener(this);
		Bundle mBundle = new RechargeHelper().getlastEntry();
		SimpleDateFormat sdf = new SimpleDateFormat("d, MMM");
		if(mBundle!=null)
		{
		
		TextView mTextView = (TextView) rootView.findViewById(R.id.summary_date);
		mTextView.setText(sdf.format(new Date(mBundle.getLong("DATE"))));

		mTextView = (TextView) rootView.findViewById(R.id.summary_amount);
		mTextView.append(String.format(" %.0f",mBundle.getFloat("RECHARGE_AMOUNT")));
		mTextView = (TextView) rootView.findViewById(R.id.no_recharge);
		mTextView.setVisibility(View.GONE);
		LinearLayout mLayout = (LinearLayout) rootView.findViewById(R.id.last_recharge_layout);
		mLayout.setVisibility(View.VISIBLE);
		}
		return rootView;
	}
	public void loadDataAsync(MainActivityAdapter mMainActivityAdapter) {
		loadData();
		mMainActivityAdapter.notifyDataSetChanged();

	}
	
	private void loadData() {
		
		int total_outgoing_duration=0,outgoing_duration_local_same_carrier=0,outgoing_duration_std_same_carrier=0,outgoing_duration_local_diff_carrier=0,outgoing_duration_std_diff_carrier=0;
		Object[] values = null;
		for (Entry<String, Object[]> entry : DataInitializer.mainmap.entrySet()) {
			values = (Object[]) entry.getValue();
			try{
			total_outgoing_duration+= (Integer) values[MainMap.OUT_DURATION];
			////Log.d(TAG, "MapCircle = "+ values[MainMap.CIRCLE] + " MapCarrier "+values[MainMap.CARRIER]);
			if(values[MainMap.CIRCLE].toString().equals(userCircle)) //local
			{ ////Log.d(TAG, "LOCAL");
				if(values[MainMap.CARRIER].toString().equals(userCarrier))//same Circle
				{
					////Log.d(TAG, "LOCAL same");
					outgoing_duration_local_same_carrier+= (Integer) values[MainMap.OUT_DURATION];
				}
				else //diffrent circle
				{
					////Log.d(TAG, "LOCAL diff");
					outgoing_duration_local_diff_carrier+= (Integer) values[MainMap.OUT_DURATION];
				}
			}
			else //std
			{
				if(values[MainMap.CARRIER].toString().equals(userCarrier))//same Circle
				{
					////Log.d(TAG, "STD same");
					outgoing_duration_std_same_carrier+= (Integer) values[MainMap.OUT_DURATION];
				}
				else //diffrent circle
				{
					////Log.d(TAG, "STD diff");
					outgoing_duration_std_diff_carrier+= (Integer) values[MainMap.OUT_DURATION];	
				}
			}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				ParseObject pObj = new ParseObject("ERROR_LOGS");
				pObj.put("PLACE","RechargeFragment_LoadData");
				if(values!=null)
				pObj.put("Object",valueToString(values) );
				else
					pObj.put("Object","It is Null" );
				pObj.saveEventually();
			}
			
		
		}
		mTextView = (TextView) rootView.findViewById(R.id.local_same);
		mProgressBar = (ProgressBar) rootView.findViewById(R.id.local_same_wait);
		if(outgoing_duration_local_same_carrier!=0)
		mTextView.setText((outgoing_duration_local_same_carrier+60-1)/60+" mins");
		else
			mTextView.setText(0+" mins");
		mProgressBar.setVisibility(View.GONE);
		mTextView.setVisibility(View.VISIBLE);
		
		
		mTextView = (TextView) rootView.findViewById(R.id.local_others);
		mProgressBar = (ProgressBar) rootView.findViewById(R.id.local_other_wait);
		if(outgoing_duration_local_diff_carrier!=0)
		mTextView.setText((outgoing_duration_local_diff_carrier+60-1)/60+" mins");
		else
			mTextView.setText(0+" mins");
		mProgressBar.setVisibility(View.GONE);
		mTextView.setVisibility(View.VISIBLE);
		
		
		mTextView = (TextView) rootView.findViewById(R.id.std_same);
		mProgressBar = (ProgressBar) rootView.findViewById(R.id.std_same_wait);
		if(outgoing_duration_std_same_carrier!=0)
		mTextView.setText((outgoing_duration_std_same_carrier+60-1)/60+" mins");
		else
			mTextView.setText(0+" mins");
		mProgressBar.setVisibility(View.GONE);
		mTextView.setVisibility(View.VISIBLE);
		
		
		mTextView = (TextView) rootView.findViewById(R.id.std_others);
		mProgressBar = (ProgressBar) rootView.findViewById(R.id.std_other_wait);
		if(outgoing_duration_std_diff_carrier!=0)
		mTextView.setText((outgoing_duration_std_diff_carrier+60-1)/60+" mins");
		else
			mTextView.setText(0+" mins");
		mProgressBar.setVisibility(View.GONE);
		mTextView.setVisibility(View.VISIBLE);

		
	}
	String valueToString(Object[] value)
	{
		return (String)(value[0]+"   "+value[1]+"  "+value[2]+"  "+value[3]+"  "+value[4]+"  "+value[5]+"  "+value[6]+"  "+value[7]+"  "+value[8]);
		
	}
	private String getDurationFormatted(int totalSecs) {
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
	@Override
	public void onClick(View v) {
		//Log.d(TAG, "Share Clicked");
		Tracker t = ((MyApplication) getActivity().getApplication()).getTracker(
			    TrackerName.APP_TRACKER);
		boolean isWhatsappInstalled = whatsappInstalledOrNot();
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		
		switch (v.getId()) {
		case R.id.whatsapp_share_button:
			sendIntent.putExtra(Intent.EXTRA_TEXT, "Have a complete control of your prepaid Balance and stay protected from unauthorized deduction.\nTry out \"Simply\":\nhttps://play.google.com/store/apps/details?id=com.builder.ibalance ");
			sendIntent.setType("text/plain");
			if(isWhatsappInstalled)
			{
				
					// Build and send an Event.
					t.send(new HitBuilders.EventBuilder()
					    .setCategory("SHARE")
					    .setAction("WhatsApp")
					    .setLabel("")
					    .build());
				 	
				Apptentive.engage(this.getActivity(), "WhatsApp_Share");
				FlurryAgent.logEvent("WhatsApp_Share");
				AppsFlyerLib.sendTrackingWithEvent(MyApplication.context,"WhatsApp_Share","");
				sendIntent.setPackage("com.whatsapp");
				startActivity(sendIntent);
			}
			else
			{
					// Build and send an Event.
					t.send(new HitBuilders.EventBuilder()
					    .setCategory("SHARE")
					    .setAction("OTHER_SHARE")
					    .setLabel("")
					    .build());
					Apptentive.engage(this.getActivity(), "OTHER_SHARE");
					AppsFlyerLib.sendTrackingWithEvent(MyApplication.context,"OTHER_SHARE","");
					FlurryAgent.logEvent("Other_Share");
					startActivity(sendIntent);
			}
			
			break;
		case R.id.feedback:
			
				t.send(new HitBuilders.EventBuilder()
			    .setCategory("Feedack")
			    .setAction("Email")
			    .setLabel("")
			    .build());

				FlurryAgent.logEvent("Email_FeedBack");
				Apptentive.engage(this.getActivity(), "Email_FeedBack");
				AppsFlyerLib.sendTrackingWithEvent(MyApplication.context,"Email_FeedBack","");
				sendIntent.setType("text/html");
				//sendIntent.setType("message/rfc822");
				sendIntent.putExtra(Intent.EXTRA_EMAIL  , new String[]{"simplyappcontact@gmail.com"});
				//sendIntent.putExtra(Intent.EXTRA_EMAIL, "ibalanceapp@gmail.com");
				sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Simply FeedBack");
				sendIntent.putExtra(Intent.EXTRA_TEXT, "");
				startActivity(Intent.createChooser(sendIntent, "Send FeedBack"));
			
			break;

		}
	
		
	}
	

	private boolean whatsappInstalledOrNot() {
		PackageManager pm = getActivity().getPackageManager();
		boolean app_installed = false;
		try {
			pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
			app_installed = true;
		} catch (PackageManager.NameNotFoundException e) {
			app_installed = false;
		}
		return app_installed;
	}

}
