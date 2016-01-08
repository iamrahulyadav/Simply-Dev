package com.builder.ibalance;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.builder.ibalance.adapters.PlansRecyclerAdapter;
import com.builder.ibalance.core.SimModel;
import com.builder.ibalance.database.helpers.ContactDetailHelper;
import com.builder.ibalance.database.helpers.MappingHelper;
import com.builder.ibalance.datainitializers.DataInitializer;
import com.builder.ibalance.messages.DataLoadingDone;
import com.builder.ibalance.util.ConstantsAndStatics;
import com.builder.ibalance.util.GlobalData;
import com.builder.ibalance.util.Helper;
import com.builder.ibalance.util.MyApplication;
import com.builder.ibalance.util.MyApplication.TrackerName;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;

public class RechargeFragment extends Fragment implements OnClickListener,AdapterView.OnItemClickListener
{
    HashMap<String,List<ParseObject>> cachedPlans = new HashMap<>();
    public final int PICK_CONTACT = 2015;
    private static final int CONTACT_PICKER_RESULT = 1001;
    final String TAG = RechargeFragment.class.getSimpleName();
    View rootView;
    TextView local_carrier, std_carrier;
    FloatingActionButton sim_switch;
    //ListView plansListView;
    RecyclerView plansListView;
    Button rechargeNow;
    ImageButton contactsPicker,callSummaryButton;
    int sim_slot = 0;
    String rechargePhoneNumber = "";
    EditText numberField,amountField;
    String currentCarrier = "Airtel", currentCircle= "Karnataka";
    Spinner carrierSpinner,circleSpinner;
    ArrayAdapter<CharSequence> carrierAdapter,circleAdapter;
    int spinnerPosition;
    View callSummary;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.fragment_recharge, container,
                false);

        sim_slot = BalanceFragment.sim_slot;
        local_carrier = (TextView) rootView.findViewById(R.id.local_same_carrier);
        std_carrier = (TextView) rootView.findViewById(R.id.std_same_carrier);
        sim_switch = (FloatingActionButton) rootView.findViewById(R.id.sim_switch);
        contactsPicker = (ImageButton) rootView.findViewById(R.id.conatact_select);
        contactsPicker.setOnClickListener(this);
        callSummaryButton = (ImageButton) rootView.findViewById(R.id.call_summary_expand_button);
        callSummaryButton.setOnClickListener(this);
        callSummary =  rootView.findViewById(R.id.callSummary);
        rechargeNow = (Button) rootView.findViewById(R.id.recharge_butt_id);
        rechargeNow.setOnClickListener(this);
        numberField = (EditText) rootView.findViewById(R.id.numberField);
        amountField = (EditText) rootView.findViewById(R.id.amountField);
        carrierSpinner = (Spinner) rootView.findViewById(R.id.recharge_carrier);
        circleSpinner = (Spinner) rootView.findViewById(R.id.recharge_circle);

        SharedPreferences preferences = this.getActivity().getSharedPreferences(ConstantsAndStatics.USER_PREF_KEY,Context.MODE_PRIVATE);
        if(preferences.getBoolean("USER_VERIFIED",false))
        {
            rechargePhoneNumber = preferences.getString("VERIFIED_NUMBER",null);
        }
        else
        {
            rechargePhoneNumber = ((TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
        }
        if (rechargePhoneNumber != null)
        {
            rechargePhoneNumber = Helper.normalizeNumber(rechargePhoneNumber);
            numberField.setText(rechargePhoneNumber);
        }
        numberField.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if(s==null)
                    return;
                if (s.toString().equals(rechargePhoneNumber) || s.length() < 10)
                {
                    //DoNothing
                } else
                {
                   numberChanged(Helper.normalizeNumber(s.toString())); //Do what you should when a number Changes
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });
        //Log.d(TAG, "UserCircle = "+ userCircle + " UserCarrier "+userCarrier);
        plansListView = (RecyclerView) rootView.findViewById(R.id.plans_list_view);
        plansListView.setItemAnimator(new SlideInLeftAnimator());
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(getActivity().getBaseContext());
        plansListView.setLayoutManager(linearLayoutManager2);
        plansListView.setNestedScrollingEnabled(true);

       // plansListView.setOnScrollListener(new );

        if(GlobalData.globalSimList == null)
        {
            GlobalData.globalSimList =  new Helper.SharedPreferenceHelper().getDualSimDetails();
        }

        if (GlobalData.globalSimList.size() >= 2)
        {
            sim_switch.setVisibility(View.VISIBLE);
            sim_switch.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (sim_slot == 0)
                    {
                        BalanceFragment.sim_slot = 1;
                        sim_slot = 1;

                    } else
                    {
                        BalanceFragment.sim_slot = 0;
                        sim_slot = 0;
                    }
                    Toast.makeText(MyApplication.context, "Switched to Sim " + (sim_slot + 1), Toast.LENGTH_LONG).show();
                   // getActivity().getActionBar().setTitle(GlobalData.globalSimList.get(sim_slot).carrier + " - " + (sim_slot + 1));
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(GlobalData.globalSimList.get(sim_slot).carrier + " - " + (sim_slot + 1));
                    if (DataInitializer.done == true)
                    {
                        loadData(sim_slot);
                    }
                }
            });
        } else
        {
            sim_switch.setVisibility(View.GONE);
        }

        SimModel currentSim = GlobalData.globalSimList.get(sim_slot);
        currentCarrier = currentSim.getCarrier();
        currentCircle = currentSim.getCircle();
        carrierAdapter = ArrayAdapter.createFromResource(this.getActivity(), R.array.carriers, android.R.layout.simple_spinner_item);
        carrierAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        carrierSpinner.setAdapter(carrierAdapter);
        spinnerPosition = carrierAdapter.getPosition(currentCarrier);

        //set the default according to value

        carrierSpinner.setTag(spinnerPosition);
        carrierSpinner.setSelection(spinnerPosition);
        carrierSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id)
            {
                currentCarrier = (String) parent.getItemAtPosition(pos);
                if(((int)carrierSpinner.getTag()) != pos)
                {
                    loadPlans(currentCarrier, currentCircle);
                }
                carrierSpinner.setTag(-1);
                //Log.d(TAG,"UserCarrier  "+ userCarrier);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {

            }
        });

        circleAdapter = ArrayAdapter.createFromResource(this.getActivity(), R.array.circles, android.R.layout.simple_spinner_item);
        circleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        circleSpinner.setAdapter(circleAdapter);
        spinnerPosition = circleAdapter.getPosition(currentCircle);
        //Log.d(TAG,"spinnerPosition Circle"+spinnerPosition );
        //set the default according to value

        circleSpinner.setTag(spinnerPosition);
        circleSpinner.setSelection(spinnerPosition);
        circleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id)
            {

                currentCircle = (String) parent.getItemAtPosition(pos);
                if(((int)circleSpinner.getTag()) != pos)
                {
                    loadPlans(currentCarrier, currentCircle);
                }
                circleSpinner.setTag(-1);
                //Log.d(TAG,"userCircle  "+ userCircle);

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        loadPlans(currentCarrier,currentCircle);
        if (DataInitializer.done == true)
        {
            //Log.d(TAG, "Data Already loaded");
            loadData(sim_slot);

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

    private void numberChanged(String normalizedNumber)
    {
        MappingHelper m = new MappingHelper();
        ArrayList<String> carrier_circle = m.getMapping(normalizedNumber);
        updateUserCarrierCircle(carrier_circle);
    }

    private void updateUserCarrierCircle(ArrayList<String> carrier_circle)
    {
        if(carrier_circle!=null)
        {
            if(DataInitializer.carriers==null)
            {
                DataInitializer.InitializeData();
            }
            currentCarrier = DataInitializer.carriers.get(carrier_circle.get(0));
            currentCircle = DataInitializer.circle.get(carrier_circle.get(1));

            spinnerPosition = carrierAdapter.getPosition(currentCarrier);
            carrierSpinner.setTag(spinnerPosition);
            carrierSpinner.setSelection(spinnerPosition);
            spinnerPosition = circleAdapter.getPosition(currentCircle);
            circleSpinner.setTag(spinnerPosition);
            circleSpinner.setSelection(spinnerPosition);
            //loadPlans(currentCarrier,currentCircle);
        }
    }

    void loadPlans(final String currentCarrier, final String currentCircle)
    {
       //V12Log.d(TAG,"Querying Parse with carrier = "+currentCarrier);
       //V12Log.d(TAG,"Querying Parse with Circle = "+currentCircle);
        List<ParseObject> plansList = cachedPlans.get(currentCarrier+currentCircle);
        if(plansList!=null)
        {
            populatePlans(plansList);
        }
        else
        {

            ParseQuery<ParseObject> query = ParseQuery.getQuery("PLANS");
            query.whereEqualTo("carrier", currentCarrier);
            query.whereEqualTo("circle", currentCircle);
            query.findInBackground(new FindCallback<ParseObject>()
            {
                public void done(List<ParseObject> plansList, ParseException e)
                {
                    if (e == null)
                    {
                        //V12Log.d(TAG, "Plans Retrieved " + plansList.size() + " plans");
                        //V12Log.d(TAG, "Plans Retrieved " + plansList.toString());
                        cachedPlans.put(currentCarrier+currentCircle,plansList);
                        populatePlans(plansList);
                    } else
                    {
                        //V12Log.d(TAG, "PLANS Error: " + e.getMessage());
                    }
                }
            });
        }
    }
    void populatePlans(List<ParseObject> plans)
    {
        if(this.isVisible())
            Helper.toastHelper(currentCarrier + " " + currentCircle);
        View plansLoading = rootView.findViewById(R.id.plans_loading);
            if(plans.size()>0)
            {
               // PlansAdapter mPlansAdapter = new PlansAdapter(plans);
                PlansRecyclerAdapter mPlansAdapter = new PlansRecyclerAdapter(plans);
                ScaleInAnimationAdapter animPlanAdapter =  new ScaleInAnimationAdapter(mPlansAdapter);
                plansListView.setAdapter(animPlanAdapter);
                //plansListView.setOnItemClickListener(this);
                plansLoading.setVisibility(View.GONE);
                plansListView.setVisibility(View.VISIBLE);
            }
        else
            {
                plansLoading.setVisibility(View.VISIBLE);
                plansListView.setVisibility(View.GONE);
                plansLoading.findViewById(R.id.progressBar).setVisibility(View.GONE);
                ((TextView)plansLoading.findViewById(R.id.info_text)).setText("Sorry No Popular Plans Available for the selected Operator and Circle");
            }

    }
    void setUsageDetails(int same_local, int others_local, int same_std, int others_std)
    {
        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.bar_style);
        ProgressBar mProgress = (ProgressBar) rootView.findViewById(R.id.local_progress_bar);

        mProgress.setMax(same_local + others_local); // Maximum Progress
        mProgress.setProgress(same_local);   // Main Progress
        // mProgress.setSecondaryProgress(b); // Secondary Progress
        mProgress.setProgressDrawable(drawable);

        Drawable drawable1 = res.getDrawable(R.drawable.bar_style);
        ProgressBar mProgress1 = (ProgressBar) rootView.findViewById(R.id.STD_progress_bar);
        mProgress1.setMax(same_std + others_std); // Maximum Progress
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


    private void loadData(int sim_slot)
    {

        int total_outgoing_duration = 0, outgoing_duration_local_same_carrier = 0, outgoing_duration_std_same_carrier = 0, outgoing_duration_local_diff_carrier = 0, outgoing_duration_std_diff_carrier = 0;
        String sim_carrier = "Airtel", sim_circle = "Karnataka";
        try
        {
            SimModel mSimModel = GlobalData.globalSimList.get(sim_slot);
            sim_carrier = mSimModel.getCarrier();
            sim_circle = mSimModel.getCircle();
        } catch (Exception e)
        {
            //This should not happen
            ParseObject pObj = new ParseObject("ERROR_LOGS");
            pObj.put("PLACE", "RechargeFragment_LoadData");
            pObj.put("Object", " GlobalSimList :" + GlobalData.globalSimList.toString());
            pObj.saveEventually();
        }
        local_carrier.setText("To " + sim_carrier);
        std_carrier.setText("To " + sim_carrier);
        ContactDetailHelper mContactDetailHelper = new ContactDetailHelper();
        outgoing_duration_local_same_carrier = mContactDetailHelper.getOutDurationLocalSameCarrier(sim_circle, sim_carrier);
        outgoing_duration_local_same_carrier = (outgoing_duration_local_same_carrier + 60 - 1) / 60;
        ((TextView) rootView.findViewById(R.id.local_same_mins)).setText(outgoing_duration_local_same_carrier + "mins");

        outgoing_duration_local_diff_carrier = mContactDetailHelper.getOutDurationLocalDiffCarrier(sim_circle, sim_carrier);
        outgoing_duration_local_diff_carrier = (outgoing_duration_local_diff_carrier + 60 - 1) / 60;
        ((TextView) rootView.findViewById(R.id.local_diff_mins)).setText(outgoing_duration_local_diff_carrier + "mins");

        outgoing_duration_std_same_carrier = mContactDetailHelper.getOutDurationSTDSameCarrier(sim_circle, sim_carrier);
        outgoing_duration_std_same_carrier = (outgoing_duration_std_same_carrier + 60 - 1) / 60;
        ((TextView) rootView.findViewById(R.id.std_same_mins)).setText(outgoing_duration_std_same_carrier + "mins");

        outgoing_duration_std_diff_carrier = mContactDetailHelper.getOutDurationSTDDiffCarrier(sim_circle, sim_carrier);
        outgoing_duration_std_diff_carrier = (outgoing_duration_std_diff_carrier + 60 - 1) / 60;
        ((TextView) rootView.findViewById(R.id.std_diff_mins)).setText(outgoing_duration_std_diff_carrier + "mins");

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
        setUsageDetails(outgoing_duration_local_same_carrier, outgoing_duration_local_diff_carrier, outgoing_duration_std_same_carrier, outgoing_duration_std_diff_carrier);


    }

    String valueToString(Object[] value)
    {
        return (String) (value[0] + "   " + value[1] + "  " + value[2] + "  " + value[3] + "  " + value[4] + "  " + value[5] + "  " + value[6] + "  " + value[7] + "  " + value[8]);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACT && resultCode == getActivity().RESULT_OK)
        {
            Uri contactUri = data.getData();
            Cursor cursor = MyApplication.context.getContentResolver().query(contactUri, null, null, null, null);
            cursor.moveToFirst();
            int column = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            rechargePhoneNumber = cursor.getString(column);
            cursor.close();
            if (rechargePhoneNumber != null)
            {
                rechargePhoneNumber = Helper.normalizeNumber(rechargePhoneNumber);
                numberField.setText(rechargePhoneNumber);
                numberChanged(rechargePhoneNumber);
            } else
            {
                Helper.toastHelper("Error: Invalid Contact");
            }
        }


    }

    private String getDurationFormatted(int totalSecs)
    {
        String min, sec, hr;
        Integer hrs, mins, secs;
        secs = totalSecs % 60;
        if (secs < 10)
            sec = "0" + secs;
        else
            sec = "" + secs;
        totalSecs = totalSecs / 60;
        mins = totalSecs % 60;
        if (mins < 10)
            min = "0" + mins;
        else
            min = "" + mins;
        totalSecs = totalSecs / 60;
        hrs = totalSecs;
        if (hrs < 10)
            hr = "0" + hrs;
        else
            hr = "" + hrs;
        return hr + ":" + min + ":" + sec;
    }

    @Override
    public void onClick(View v)
    {

        Tracker t = ((MyApplication) MyApplication.context).getTracker(
                TrackerName.APP_TRACKER);
        switch (v.getId())
        {
            case R.id.conatact_select:
                t.send(new HitBuilders.EventBuilder()
                        .setCategory("RECHARGE")
                        .setAction("CONTACT_PICKED")
                        .setLabel("")
                        .build());
                FlurryAgent.logEvent("CONTACT_PICKED");
                Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(i, PICK_CONTACT);

                break;
            case R.id.recharge_butt_id:
                if(numberField.getText()==null || numberField.getText().toString().length()<10)
                {
                    Helper.toastHelper("Mobile Number not valid");
                }
                else
                {

                  if(numberField.getText()==null || numberField.getText().toString().length()<10)
                {
                    Helper.toastHelper("Mobile Number not valid");
                    numberField.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(numberField, InputMethodManager.SHOW_IMPLICIT);
                }
                    else
                {
                    if(amountField.getText()==null ||amountField.getText().toString().equals("") )
                    {
                        Helper.toastHelper("Please Enter the Recharge Amount");
                        amountField.requestFocus();
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(amountField, InputMethodManager.SHOW_IMPLICIT);
                    }
                    else
                    {
                        t.send(new HitBuilders.EventBuilder()
                                .setCategory("RECHARGE_INIT")
                                .setAction(numberField.getText().toString())
                                .setLabel(amountField.getText().toString())
                                .build());
                        FlurryAgent.logEvent("RECHARGE_INIT");
                        Intent rechargeIntent = new Intent(this.getActivity(), RechargePopup.class);
                        rechargeIntent.putExtra("NUMBER", numberField.getText().toString());
                        rechargeIntent.putExtra("CARRIER", currentCarrier);
                        rechargeIntent.putExtra("CIRCLE", currentCircle);
                        rechargeIntent.putExtra("AMOUNT", Integer.parseInt(amountField.getText().toString()));
                        startActivity(rechargeIntent);
                        Helper.toastHelper("Recharging for " + rechargePhoneNumber);
                    }
                }
                }
                break;
            case R.id.call_summary_expand_button:
                //toggle visibility
                t.send(new HitBuilders.EventBuilder()
                        .setCategory("CALL_SUMMARY")
                        .setAction("TOGGLED")
                        .setLabel("")
                        .build());
                FlurryAgent.logEvent("CALL_SUMMARY");
                callSummary.setVisibility((callSummary.getVisibility() == View.GONE)?View.VISIBLE:View.GONE);
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




    @Override
    public void onPause()
    {

        FlurryAgent.endTimedEvent("RechargeScreen");
        super.onPause();
    }

    @Override
    public void onResume()
    {
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

    }


    /**
     * Callback method to be invoked when an item in this AdapterView has
     * been clicked.
     * <p/>
     * Implementers can call getItemAtPosition(position) if they need
     * to access the data associated with the selected item.
     *
     * @param parent   The AdapterView where the click happened.
     * @param view     The view within the AdapterView that was clicked (this
     *                 will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     * @param id       The row id of the item that was clicked.
     */

    /*
    final ScrollView sl = (ScrollView) findViewById(R.id.scroll_view);
        final RelativeLayout newactionBar = (RelativeLayout) findViewById(R.id.actionbar);
        newactionBar.setVisibility(View.VISIBLE);
        newactionBar.setVisibility(View.GONE);
        sl.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {

            @Override
            public void onScrollChanged() {

                int scrollX = sl.getScrollX(); //for horizontalScrollView
                int scrollY = sl.getScrollY(); //for verticalScrollView
                if (scrollY > 100) {
                    newactionBar.bringToFront();
                    newactionBar.animate().alpha(1.0f);
                    newactionBar.setVisibility(View.GONE);
                } else {
                    newactionBar.animate().alpha(0.0f);
                    newactionBar.setVisibility(View.GONE);

                }

                //DO SOMETHING WITH THE SCROLL COORDINATES

            }
        });
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
       //V12Log.d(TAG, view.getTag().toString());
        String temp = view.getTag().toString();
        amountField.setText(temp);
        if(numberField.getText()==null || numberField.getText().toString().length()<10)
        {
            Helper.toastHelper("Mobile Number not valid");
            numberField.requestFocus();
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(numberField, InputMethodManager.SHOW_IMPLICIT);
        }else
        {
            Intent rechargeIntent = new Intent(this.getActivity(), RechargePopup.class);
            rechargeIntent.putExtra("NUMBER", numberField.getText().toString());
            rechargeIntent.putExtra("CARRIER", currentCarrier);
            rechargeIntent.putExtra("CIRCLE", currentCircle);
            rechargeIntent.putExtra("AMOUNT", Integer.parseInt(amountField.getText().toString()));
            startActivity(rechargeIntent);
        }
    }
}
