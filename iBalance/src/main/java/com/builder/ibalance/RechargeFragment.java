package com.builder.ibalance;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.builder.ibalance.adapters.PlansAdapter;
import com.builder.ibalance.core.SimModel;
import com.builder.ibalance.database.helpers.ContactDetailHelper;
import com.builder.ibalance.datainitializers.DataInitializer;
import com.builder.ibalance.messages.DataLoadingDone;
import com.builder.ibalance.models.PlansModel;
import com.builder.ibalance.util.GlobalData;
import com.builder.ibalance.util.Helper;
import com.builder.ibalance.util.MyApplication;
import com.builder.ibalance.util.MyApplication.TrackerName;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.melnykov.fab.FloatingActionButton;
import com.parse.ParseObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class RechargeFragment extends Fragment implements OnClickListener {

	View rootView;
	final String TAG = RechargeFragment.class.getSimpleName();
	TextView local_carrier,std_carrier,mTextView;
	ProgressBar mProgressBar;
	FloatingActionButton sim_switch;
    ListView plansListView;
    Button rechargeNow;
    ImageButton contactsPicker;
    String dummyJson = "[{\"price\":225,\"carrier\":\"Airtel\",\"circle\":\"KARNATAKA\",\"validity\":\"30 days\",\"type\":\"Takltime\",\"talktime\":240,\"tags\":[\"FULL TT\"],\"benefits\":\"Talktime: 240 (30 Days Val) | 300 local A-A secs for 5 days\"},{\"price\":251,\"carrier\":\"Airtel\",\"circle\":\"KARNATAKA\",\"validity\":\"30 days\",\"type\":\"Takltime\",\"talktime\":180,\"tags\":[\"Topup\",\"2G\",\"3G\"],\"benefits\":\"1.25 GB + Rs.180 Talktime\"}]";
	int sim_slot = 0;
    String rechargePhoneNumber = null;
    private static final int CONTACT_PICKER_RESULT = 1001;
    EditText numberField;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_recharge, container,
				false);
		sim_slot = BalanceFragment.sim_slot;
        local_carrier = (TextView) rootView.findViewById(R.id.local_same_carrier);
        std_carrier = (TextView) rootView.findViewById(R.id.std_same_carrier);
		sim_switch = (FloatingActionButton) rootView.findViewById(R.id.sim_switch);
		contactsPicker = (ImageButton) rootView.findViewById(R.id.conatact_select);
        contactsPicker.setOnClickListener(this);
		rechargeNow = (Button) rootView.findViewById(R.id.recharge_butt_id);
        rechargeNow.setOnClickListener(this);
		//Log.d(TAG, "UserCircle = "+ userCircle + " UserCarrier "+userCarrier);
        plansListView = (ListView) rootView.findViewById(R.id.plans_list_view);
        Type listType = new TypeToken<ArrayList<PlansModel>>() {}.getType();
        numberField = (EditText) rootView.findViewById(R.id.numberField);
        rechargePhoneNumber = ((TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
        if(rechargePhoneNumber!=null)
        {
            numberField.setText(rechargePhoneNumber);
        }

        List<PlansModel> plansList = null;
        try
        {
            plansList = new Gson().fromJson(dummyJson, listType);

            PlansAdapter mPlansAdapter = new PlansAdapter(plansList);
            plansListView.setAdapter(mPlansAdapter);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Helper.toastHelper("Deserialization Failed");
        }
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
		/*Button whatsAppShareButton = (Button) rootView.findViewById(R.id.whatsapp_share_button);
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
		}*/
		return rootView;
	}
	void setUsageDetails(int same_local,int others_local,int same_std,int others_std)
	{
		Resources res = getResources();
		Drawable drawable = res.getDrawable(R.drawable.bar_style);
		ProgressBar mProgress = (ProgressBar) rootView.findViewById(R.id.local_progress_bar);

		mProgress.setMax(same_local+others_local); // Maximum Progress
		 mProgress.setProgress(same_local);   // Main Progress
		// mProgress.setSecondaryProgress(b); // Secondary Progress
		mProgress.setProgressDrawable(drawable);

		Drawable drawable1 = res.getDrawable(R.drawable.bar_style);
		ProgressBar mProgress1 = (ProgressBar) rootView.findViewById(R.id.STD_progress_bar);
		mProgress1.setMax(same_std+others_std); // Maximum Progress
			mProgress1.setProgress(same_std);   // Main Progress

		mProgress1.setProgressDrawable(drawable1);
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
        local_carrier.setText("To "+sim_carrier);
        std_carrier.setText("To "+sim_carrier);
        ContactDetailHelper mContactDetailHelper = new ContactDetailHelper();
        outgoing_duration_local_same_carrier = mContactDetailHelper.getOutDurationLocalSameCarrier(sim_circle, sim_carrier);
        outgoing_duration_local_same_carrier = (outgoing_duration_local_same_carrier+60-1)/60;
        ((TextView)rootView.findViewById(R.id.local_same_mins)).setText(outgoing_duration_local_same_carrier+"mins");

        outgoing_duration_local_diff_carrier = mContactDetailHelper.getOutDurationLocalDiffCarrier(sim_circle, sim_carrier);
        outgoing_duration_local_diff_carrier = (outgoing_duration_local_diff_carrier+60-1)/60;
        ((TextView)rootView.findViewById(R.id.local_diff_mins)).setText(outgoing_duration_local_diff_carrier+"mins");

        outgoing_duration_std_same_carrier = mContactDetailHelper.getOutDurationSTDSameCarrier(sim_circle, sim_carrier);
        outgoing_duration_std_same_carrier = (outgoing_duration_std_same_carrier+60-1)/60;
        ((TextView)rootView.findViewById(R.id.std_same_mins)).setText(outgoing_duration_std_same_carrier+"mins");

        outgoing_duration_std_diff_carrier = mContactDetailHelper.getOutDurationSTDDiffCarrier(sim_circle,sim_carrier);
        outgoing_duration_std_diff_carrier = (outgoing_duration_std_diff_carrier+60-1)/60;
        ((TextView)rootView.findViewById(R.id.std_diff_mins)).setText(outgoing_duration_std_diff_carrier+"mins");

		/*mTextView = (TextView) rootView.findViewById(R.id.local_same);
		mProgressBar = (ProgressBar) rootView.findViewById(R.id.local_same_wait);
		if(outgoing_duration_local_same_carrier!=0)
		mTextView.setText((outgoing_duration_local_same_carrier+60-1)/60+" mins");
		else
			mTextView.setText(0+" mins");
		mProgressBar.setVisibility(View.GONE);
		mTextView.setVisibility(View.VISIBLE);*/
		
		
		/*mTextView = (TextView) rootView.findViewById(R.id.local_others);
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
		mTextView.setVisibility(View.VISIBLE);*/
        setUsageDetails(outgoing_duration_local_same_carrier,outgoing_duration_local_diff_carrier,outgoing_duration_std_same_carrier,outgoing_duration_std_diff_carrier);

		
	}
	String valueToString(Object[] value)
	{
		return (String)(value[0]+"   "+value[1]+"  "+value[2]+"  "+value[3]+"  "+value[4]+"  "+value[5]+"  "+value[6]+"  "+value[7]+"  "+value[8]);
		
	}


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CONTACT_PICKER_RESULT)
        {

            Uri result = data.getData();
            String id = result.getLastPathSegment();

            Cursor cursor = MyApplication.context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? ",
                    new String[]{id}, null);

            int phoneIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            if (cursor.moveToFirst())
            {
                rechargePhoneNumber = cursor.getString(phoneIdx);
            }
            if(rechargePhoneNumber!=null)
            {

                numberField.setText(rechargePhoneNumber);
            }
        }
        else
        {
            Helper.toastHelper("Error: Inavlid Contact");
        }
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
        switch (v.getId())
        {
            case R.id.conatact_select:
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
                contactPickerIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
                startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);

                break;
            case R.id.recharge_butt_id:
                Helper.toastHelper("Recharging for "+rechargePhoneNumber);
                break;
            default:
        }


		/*//Log.d(TAG, "Share Clicked");
		Tracker t = ((MyApplication) getActivity().getApplication()).getTracker(
			    TrackerName.APP_TRACKER);
		boolean isWhatsappInstalled = whatsappInstalledOrNot();
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		switch (v.getId()) {
		case R.id.whatsapp_share_button:
			sendIntent.putExtra(Intent.EXTRA_TEXT, "To Track your prepaid Balance and know how much you spend on your contacts.\nTry out \"Simply\": https://goo.gl/v3YMrN ");
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

		}*/
	
		
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
