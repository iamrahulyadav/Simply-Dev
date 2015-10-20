package com.builder.ibalance;

import android.app.Fragment;
import android.content.Intent;
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
import android.widget.Toast;

import com.builder.ibalance.core.SimModel;
import com.builder.ibalance.database.RechargeHelper;
import com.builder.ibalance.database.helpers.ContactDetailHelper;
import com.builder.ibalance.datainitializers.DataInitializer;
import com.builder.ibalance.messages.DataLoadingDone;
import com.builder.ibalance.util.GlobalData;
import com.builder.ibalance.util.MyApplication;
import com.builder.ibalance.util.MyApplication.TrackerName;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.melnykov.fab.FloatingActionButton;
import com.parse.ParseObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.greenrobot.event.EventBus;

public class RechargeFragment extends Fragment implements OnClickListener {

	View rootView;
	final String TAG = RechargeFragment.class.getSimpleName();
	TextView local_carrier,std_carrier,mTextView;
	ProgressBar mProgressBar;
	FloatingActionButton sim_switch;
	int sim_slot = 0;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_recharge, container,
				false);
		sim_slot = BalanceFragment.sim_slot;
        local_carrier = (TextView) rootView.findViewById(R.id.summary_local_carrier);
        std_carrier = (TextView) rootView.findViewById(R.id.summary_std_carrier);
		sim_switch = (FloatingActionButton) rootView.findViewById(R.id.sim_switch);
		//Log.d(TAG, "UserCircle = "+ userCircle + " UserCarrier "+userCarrier);
		if(GlobalData.globalSimList.size()>=2)
		{
			sim_switch.setVisibility(View.VISIBLE);
			sim_switch.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					if(sim_slot == 0)
					{
						BalanceFragment.sim_slot = 1;
						sim_slot = 1;

					}
					else
					{
						BalanceFragment.sim_slot = 0;
						sim_slot = 0;
					}
					Toast.makeText(MyApplication.context, "Switched to Sim " + (sim_slot + 1), Toast.LENGTH_LONG).show();
					getActivity().getActionBar().setTitle(GlobalData.globalSimList.get(sim_slot).carrier + " - " + (sim_slot + 1));
                    if (DataInitializer.done == true)
                    {
                        loadData(sim_slot);
                    }
                }
			});
		}
		else
		{
			sim_switch.setVisibility(View.GONE);
		}
		if (DataInitializer.done == true) {
			//Log.d(TAG, "Data Already loaded");
			loadData(sim_slot);

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

    @Override
    public void onStart()
    {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop()
    {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
    public void onEvent(DataLoadingDone m)
    {
        loadData(sim_slot);
    }

	
	private void loadData(int sim_slot) {
		
		int total_outgoing_duration=0,outgoing_duration_local_same_carrier=0,outgoing_duration_std_same_carrier=0,outgoing_duration_local_diff_carrier=0,outgoing_duration_std_diff_carrier=0;
        String sim_carrier = "Airtel",sim_circle = "Karnataka";
        try
        {
            SimModel mSimModel = GlobalData.globalSimList.get(sim_slot);
            sim_carrier = mSimModel.getCarrier();
            sim_circle = mSimModel.getCircle();
        }
        catch (Exception e)
        {
            //This should not happen
            ParseObject pObj = new ParseObject("ERROR_LOGS");
            pObj.put("PLACE","RechargeFragment_LoadData");
                pObj.put("Object"," GlobalSimList :"+GlobalData.globalSimList.toString() );
            pObj.saveEventually();
        }
        local_carrier.setText("Local mins to "+sim_carrier);
        std_carrier.setText("STD mins to "+sim_carrier);
        ContactDetailHelper mContactDetailHelper = new ContactDetailHelper();
        outgoing_duration_local_same_carrier = mContactDetailHelper.getOutDurationLocalSameCarrier(sim_circle, sim_carrier);

        outgoing_duration_local_diff_carrier = mContactDetailHelper.getOutDurationLocalDiffCarrier(sim_circle, sim_carrier);

        outgoing_duration_std_same_carrier = mContactDetailHelper.getOutDurationSTDSameCarrier(sim_circle, sim_carrier);

        outgoing_duration_std_diff_carrier = mContactDetailHelper.getOutDurationSTDDiffCarrier(sim_circle,sim_carrier);

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
		//ToDo change Link
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

				FlurryAgent.logEvent("WhatsApp_Share");
				//V10AppsFlyerLib.sendTrackingWithEvent(MyApplication.context,"WhatsApp_Share","");
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
					//V10AppsFlyerLib.sendTrackingWithEvent(MyApplication.context,"OTHER_SHARE","");
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
				//V10AppsFlyerLib.sendTrackingWithEvent(MyApplication.context,"Email_FeedBack","");
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

    @Override
    public void onPause() {

        FlurryAgent.endTimedEvent("RechargeScreen");
        super.onPause();
    }
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
        FlurryAgent.logEvent("RechargeScreen", true);
       //V10AppsFlyerLib.sendTrackingWithEvent(MyApplication.context, "Recharge Screen", "");
        // End the timed event, when the user navigates away from article

        super.onResume();
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();

        MyApplication.getRefWatcher().watch(this);
    }



}
