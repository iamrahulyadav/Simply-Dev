package com.builder.ibalance;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.builder.ibalance.adapters.RecentListRecycleAdapter;
import com.builder.ibalance.database.helpers.BalanceHelper;
import com.builder.ibalance.database.helpers.CallLogsHelper;
import com.builder.ibalance.datainitializers.DataInitializer;
import com.builder.ibalance.messages.DataLoadingDone;
import com.builder.ibalance.messages.MinimumBalanceMessage;
import com.builder.ibalance.messages.UpdateBalanceOnScreen;
import com.builder.ibalance.models.USSDModels.NormalCall;
import com.builder.ibalance.util.ConstantsAndStatics;
import com.builder.ibalance.util.GlobalData;
import com.builder.ibalance.util.Helper;
import com.builder.ibalance.util.MyApplication;
import com.builder.ibalance.util.MyApplication.TrackerName;
import com.flurry.android.FlurryAgent;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

public class BalanceFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, OnChartValueSelectedListener,View.OnLongClickListener,View.OnClickListener
{
    static int sim_slot = 0;
    final String tag = BalanceFragment.class.getSimpleName();
    //Button balance_DetailsButton;
    public LineChart mLineChart;
    public List<NormalCall> ussdDataList = null;
    SharedPreferences mSharedPreferences;
    float currBalance = 0, currData = 0;
    TextView balanceTextView, predictionTextView;
    LinearLayout balance_layout;
    ProgressBar balProgress;
    ArrayList<String> xVals;
    View rootView;
    float current_balance, minimum_balance;
    Button refreshMainBal;
    //ParallaxListView mListView;
    RecyclerView mListView;
    Typeface tf;
    boolean normalBalRefresh = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        tf = Typeface.createFromAsset(getResources().getAssets(), "Roboto-Regular.ttf");
        rootView = inflater.inflate(R.layout.fragment_balance, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getBaseContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(true);

        mListView = (RecyclerView) rootView.findViewById(R.id.recents_list);
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getActivity());
        mListView.setLayoutManager(linearLayoutManager1);
        mListView.setHasFixedSize(true);
        getActivity().invalidateOptionsMenu();

        ((AppCompatActivity) getActivity()).getSupportLoaderManager().initLoader(1, null, this);


        final FloatingActionButton sim_switch = (FloatingActionButton) rootView.findViewById(R.id.sim_switch);
        mSharedPreferences = getActivity().getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
        predictionTextView = (TextView) rootView.findViewById(R.id.predictionView);
        predictionTextView.setTypeface(tf);
        balProgress = (ProgressBar) rootView.findViewById(R.id.balscreen_prediction);
        balanceTextView = (TextView) rootView.findViewById(R.id.balanceView);
        balanceTextView.setTypeface(tf);
        balance_layout = (LinearLayout) rootView.findViewById(R.id.bal_layout);
        mLineChart = (LineChart) rootView.findViewById(R.id.balcontainer);
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
                        sim_slot = 1;

                    } else
                    {
                        sim_slot = 0;
                    }
                    Toast.makeText(MyApplication.context, "Switched to Sim " + (sim_slot + 1), Toast.LENGTH_SHORT).show();
                    ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(GlobalData.globalSimList.get(sim_slot).carrier + " - " + (sim_slot + 1));

                    new USSDLoader().execute(sim_slot);
                }
            });
        } else
        {
            sim_switch.setVisibility(View.GONE);
        }
        String carrier = "Unknown";
        try
        {
            //V12Log.d(tag,"Bal Frag Loading Slot = "+sim_slot);
            carrier = GlobalData.globalSimList.get(sim_slot).carrier;

            current_balance = mSharedPreferences.getFloat("CURRENT_BALANCE_" + sim_slot, (float) -200.0);
            minimum_balance = mSharedPreferences.getFloat("MINIMUM_BALANCE", (float) 10.0);
            if (current_balance > -100.0f)
            {
                if (current_balance < minimum_balance)
                {
                    if ((sim_slot == 0 && MainActivity.sim1BalanceReminderShown == false) || (sim_slot == 1 && MainActivity.sim2BalanceReminderShown == false))
                    {
                        if(!((MainActivity)getActivity()).showingUpdateDialog)
                            createReminderDialog(this.getActivity());
                    }
                }
            }
        }//Catch exception when no sim are detected
        catch (Exception e)
        {
        }
        /*getActivity().getActionBar().setTitle(carrier+"-"+(sim_slot+1));*/
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(carrier + "-" + (sim_slot + 1));
        new USSDLoader().execute(sim_slot);
        return rootView;
    }

    private void callingScreen(String phoneNumber)
    {
        Helper.logGA("CALL","RECENTS");
        Helper.logFlurry("CALL","SOURCE","RECENTS");
        Intent intent = new Intent(Intent.ACTION_DIAL);


        Uri data = Uri.parse("tel:" + phoneNumber);


        intent.setData(data);


        startActivity(intent);
    }

    private void createReminderDialog(Context context)
    {
        if (sim_slot == 0)
        {
            MainActivity.sim1BalanceReminderShown = true;
        } else
        {
            MainActivity.sim2BalanceReminderShown = true;
        }
        AlertDialog.Builder alertbox = new AlertDialog.Builder(context);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mView = inflater.inflate(R.layout.recharge_reminder, null);
        alertbox.setView(mView);
        TextView infoText = (TextView) mView.findViewById(R.id.low_bal_text_id);
        Button rechargeNow = (Button) mView.findViewById(R.id.recharge_now_button);
        infoText.setText(" Your Current Balance is Rs." + current_balance);

        final AlertDialog alert = alertbox.create();

        rechargeNow.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ((MainActivity) getActivity()).goToRechargepage();
                alert.dismiss();
            }
        });

        Tracker t = ((MyApplication) context.getApplicationContext()).getTracker(TrackerName.APP_TRACKER);
        t.send(new HitBuilders.EventBuilder().setCategory("LOW_BALANCE").setAction(current_balance + "").setLabel("").build());
        Helper.logGA("LOW_BALANCE",""+current_balance);
        Helper.logFlurry("LOW_BALANCE","DEVICE_ID",Helper.getDeviceId(),"BALANCE",current_balance+"");
        alert.show();
        //AppsFlyerLib.sendTrackingWithEvent(MyApplication.context, "LOW_BALANCE", "current_balance");
        // Set the message to display

        // Set a positive/yes button and create a listener

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {


        return new CursorLoader(getActivity().getApplicationContext())
        {
            @Override
            public Cursor loadInBackground()
            {
                Cursor cursor = new CallLogsHelper().getAllOutGoingLocalCallLogs();
                return cursor;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {

        RecentListRecycleAdapter mRecentListAdapter = new RecentListRecycleAdapter(getActivity().getBaseContext(), data, false);
        ScaleInAnimationAdapter animPlanAdapter =  new ScaleInAnimationAdapter(mRecentListAdapter);
        mListView.setAdapter(animPlanAdapter);

    }

    @Override
    public void onLoaderReset(Loader loader)
    {

    }

    @Override
    public void onStart()
    {
        super.onStart();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onStop()
    {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    public void onEvent(DataLoadingDone m)
    {
        setPredictedDays(sim_slot);
    }

    public void onEvent(MinimumBalanceMessage m)
    {
        if (mLineChart != null)
        {
            updateLimitLine(m.minimum_bal);
            mLineChart.notifyDataSetChanged();
            mLineChart.invalidate();
        }
    }

    void intializeScreen(int sim_slot)
    {
        //V12Log.d(tag,"Bal Frag intializeScreen");
        if (sim_slot == 0)
        {
            //V12Log.d(tag,"Bal Frag sim_slot = "+sim_slot);
            currBalance = mSharedPreferences.getFloat("CURRENT_BALANCE_" + sim_slot, mSharedPreferences.getFloat("CURRENT_BALANCE",  -200.0f));
        } else
        {
            //V12Log.d(tag,"Bal Frag sim_slot = "+sim_slot);
            currBalance = mSharedPreferences.getFloat("CURRENT_BALANCE_" + sim_slot,  -200.0f);
        }

        //Log.d(tag,"Balance  = "+currBalance);
        //Log.d(tag,"DATA  = "+currData);
        if (DataInitializer.done == true)
        {
           //V16Log.d(tag, "Setting Predicted days");
            setPredictedDays(sim_slot);
        }


        if (currBalance < -100.0)
            balanceTextView.setText(getResources().getString(R.string.rupee_symbol) + " --.--");
        else
            balanceTextView.setText(getResources().getString(R.string.rupee_symbol) + " " + currBalance);

        balance_layout.setOnClickListener(this);
        /*new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(MyApplication.context, HistoryActivity.class));

            }
        })*/;
        LinearLayout dataLayout1 = (LinearLayout) rootView.findViewById(R.id.pack_data_layout);
        dataLayout1.setVisibility(View.VISIBLE);//In case it was deleted
        dataLayout1.setOnClickListener(this);
        dataLayout1.setOnLongClickListener(this);
        //V12Log.d(tag, "Bal Frag currBalance = " + currBalance);

        if (currBalance > -100.0)
        {
            //mListView = (ParallaxListView) rootView.findViewById(R.id.recents_list);
            rootView.findViewById(R.id.nobal).setVisibility(View.GONE);
            mListView = (RecyclerView) rootView.findViewById(R.id.recents_list);
            mListView.setVisibility(View.VISIBLE);
            mLineChart.setVisibility(View.VISIBLE);
            if (mLineChart == null)
            {

				/*mLineChart = (LineChart)this.getActivity().getLayoutInflater().inflate(R.layout.balance_deduction_graph, null, false);
                final float scale = getResources().getDisplayMetrics().density;
				mLineChart.setLayoutParams(new AbsListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, (int)(300*scale)));
				*/
            }

            createGraph();
          /*  mLineChart = (LineChart) rootView.findViewById(R.id.balcontainer);
            createGraph();*/
            //have to set the header before setting the adapter
            //mListView.removeHeaderView(mLineChart);

            //chartImage = (ImageView)rootView.findViewById(R.id.collapsing_image);
            //chartImage.setBackgroundColor(getResources().getColor(R.color.white));
			/*LinearLayout chartItem = (LinearLayout)rootView.findViewById(R.id.collapsing_image);
			chartItem.addView(mLineChart);
*/
            //RecentListAdapter mRecentListAdapter = null;
			/*RecentListRecycleAdapter mRecentListAdapter = null;
            if(mRecentListAdapter==null)
            {
                Cursor recentListCursor = new CallLogsHelper().getAllOutGoingLocalCallLogs();
                mRecentListAdapter = new RecentListRecycleAdapter(getActivity(),recentListCursor , false);
            }
			mListView.setAdapter(mRecentListAdapter);*/
			/*mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id)
				{
					Tracker t = ((MyApplication) MyApplication.context).getTracker(
							TrackerName.APP_TRACKER);
					t.send(new HitBuilders.EventBuilder()
							.setCategory("CALL")
							.setAction("RECENTS")
							.setLabel("")
							.build());
					FlurryAgent.logEvent("CALL");
					Intent intent = new Intent(Intent.ACTION_DIAL);
					*//** Creating a uri object to store the telephone number *//*
					Uri data = Uri.parse("tel:" + view.getTag(R.id.KEY_NUMBER));
					*//** Setting intent data *//*
					intent.setData(data);
					*//** Starting the caller activity by the implicit intent *//*
					startActivity(intent);
				}
			});*/
		/*((RelativeLayout)rootView.findViewById(R.id.nobal)).setVisibility(View.GONE);
		mLineChart.setVisibility(View.VISIBLE);*/


        } else

        {
            mListView.setVisibility(View.GONE);
            mLineChart.setVisibility(View.GONE);
            //ViewGroup container = (ViewGroup) rootView.findViewById(R.id.parallax_container);
            //rootView.findViewById(R.id.recents_list).setVisibility(View.GONE);
            rootView.findViewById(R.id.nobal).setVisibility(View.VISIBLE);
            //this.getActivity().getLayoutInflater().inflate(R.layout.no_balance_layout, container, true);
            refreshMainBal = (Button) rootView.findViewById(R.id.bal_refresh);
            refreshMainBal.setOnClickListener(this);/*new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {

                    *//*if (!Helper.contactExists("+919739663487"))
                    {
                        //V10Log.d(tag, "Whatsapp contact not found adding contact");

                        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
                        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
                        intent.putExtra(ContactsContract.Intents.Insert.EMAIL, "simplyappcontact@gmail.com").putExtra(ContactsContract.Intents.Insert.NAME, "Simply App").putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK).putExtra(ContactsContract.Intents.Insert.PHONE, "+919739663487").putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);

                        startActivity(intent);
                    } else
                    {
                        //Sending Device Id doesn't work
                        ConstantsAndStatics.PASTE_DEVICE_ID=true;
                        startActivity(Helper.openWhatsApp("+919739663487", ((TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId()));
                    }*//*
                }
            });*/
        }


        if (DataInitializer.ussdDataList.size() > 0)
        {


            setData();

        }
        else {
            mLineChart.clear();
        }


        boolean isDatapackActive=false, isSmsPackActive = false, isCallpackActive = false;


        isCallpackActive = mSharedPreferences.getBoolean("PACK_CALL_ACTIVE_" + sim_slot,false);
        isDatapackActive = mSharedPreferences.getBoolean("PACK_DATA_ACTIVE_" + sim_slot,false);
        isSmsPackActive = mSharedPreferences.getBoolean("PACK_SMS_ACTIVE_" + sim_slot,false);
        int activeAcc = 1;//default for balance, data will be ther unless it is deleted
        if(isCallpackActive) activeAcc++;
        if(isSmsPackActive) activeAcc++;
        //If user explicity deleted data pack then it should be hidden, Disable only if some other is active otherwise show empty
        if(!isDatapackActive && activeAcc>1)
        {
            rootView.findViewById(R.id.pack_data_layout).setVisibility(View.GONE);
        }
        if(isDatapackActive || rootView.findViewById(R.id.pack_data_layout).getVisibility() == View.VISIBLE) activeAcc++;


        float layoutWeight = 1.0f/(float)activeAcc;
        if(layoutWeight>0.5f)
            layoutWeight = 0.5f;
        //Resize Main Balance
        LinearLayout balanceLayout = (LinearLayout) rootView.findViewById(R.id.bal_layout);
        LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) balanceLayout.getLayoutParams();
        params1.weight = layoutWeight;
        if(isDatapackActive)
        {
            LinearLayout dataLayout = (LinearLayout) rootView.findViewById(R.id.pack_data_layout);
            dataLayout.setVisibility(View.VISIBLE);//In case it was deleted
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dataLayout.getLayoutParams();
            params.weight = layoutWeight;
            dataLayout.setLayoutParams(params);
            dataLayout.setOnLongClickListener(this);
            dataLayout.setOnClickListener(this);
            float data_left = mSharedPreferences.getFloat("PACK_DATA_REMAINING_" + sim_slot,0.0f);
            String validity = mSharedPreferences.getString("PACK_DATA_VALIDITY_" + sim_slot,"N/A");
            ((TextView)dataLayout.findViewById(R.id.pack_data_left)).setText(String.format("%.2f",data_left)+" MB");
            ((TextView)dataLayout.findViewById(R.id.pack_data_validity)).setText(validity!=null?validity:"N/A");

        }
        if(isCallpackActive)
        {
            LinearLayout callPackLayout = (LinearLayout) rootView.findViewById(R.id.pack_call_layout);
            callPackLayout.setVisibility(View.VISIBLE);//In case it was deleted
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) callPackLayout.getLayoutParams();
            params.weight = layoutWeight;
            callPackLayout.setLayoutParams(params);
            callPackLayout.setOnLongClickListener(this);
            callPackLayout.setOnClickListener(this);
            boolean isMinType = mSharedPreferences.getBoolean("MIN_TYPE_" + sim_slot,false);
            if(isMinType)
            {
                int pack_dur_left = mSharedPreferences.getInt("PACK_CALL_DUR_REMAINING_" + sim_slot, 0);
                String metric = mSharedPreferences.getString("PACK_CALL_DUR_METRIC_" + sim_slot, "s");
                metric = metric.toLowerCase();
                ((TextView) callPackLayout.findViewById(R.id.pack_call_left)).setText(pack_dur_left+" "+metric);

            }
            else
            {
                float pack_bal_left = mSharedPreferences.getFloat("PACK_CALL_BAL_REMAINING_" + sim_slot, 0.0f);

                ((TextView) callPackLayout.findViewById(R.id.pack_call_left)).setText(getResources().getString(R.string.rupee_symbol)+String.format("%.2f", pack_bal_left) );

            }
            String validity = mSharedPreferences.getString("PACK_CALL_VALIDITY_" + sim_slot, "N/A");
            ((TextView) callPackLayout.findViewById(R.id.pack_call_validity)).setText(validity != null ? validity : "N/A");

        }
        if(isSmsPackActive)
        {
            LinearLayout smsPackLayout = (LinearLayout) rootView.findViewById(R.id.pack_sms_layout);
            smsPackLayout.setVisibility(View.VISIBLE);//In case it was deleted
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) smsPackLayout.getLayoutParams();
            params.weight = layoutWeight;
            smsPackLayout.setLayoutParams(params);
            smsPackLayout.setOnLongClickListener(this);
            smsPackLayout.setOnClickListener(this);
            int sms_left = mSharedPreferences.getInt("PACK_SMS_REMAINING_" + sim_slot,0);
            String validity = mSharedPreferences.getString("PACK_SMS_VALIDITY_" + sim_slot,"N/A");
            ((TextView)smsPackLayout.findViewById(R.id.pack_sms_left)).setText( sms_left+"");
            ((TextView)smsPackLayout.findViewById(R.id.pack_sms_validity)).setText(validity!=null?validity:"N/A");

        }


    }
    /**
     * Called when a view has been clicked and held.
     *
     * @param v The view that was clicked and held.
     * @return true if the callback consumed the long click, false otherwise.
     */
    @Override
    public boolean onLongClick(final View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());

        builder.setTitle("Confirm");




        switch (v.getId())
        {

            case R.id.pack_data_layout:
                builder.setMessage("Delete Data Pack ?");
                builder.setPositiveButton("Delete",  new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences pref = MyApplication.context.getSharedPreferences(ConstantsAndStatics.USER_PREF_KEY,Context.MODE_PRIVATE);
                        pref.edit().putBoolean("PACK_DATA_ACTIVE_"+sim_slot,false)
                        .putFloat("PACK_DATA_REMAINING_" + sim_slot,0.0f)
                        .putString("PACK_DATA_VALIDITY_" + sim_slot,"N/A").apply();
                        v.setVisibility(View.GONE);
                        dialog.cancel();
                    }});
                break;
            case R.id.pack_call_layout:
                builder.setMessage("Delete Call Pack ?");
                builder.setPositiveButton("Delete",  new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences pref = MyApplication.context.getSharedPreferences(ConstantsAndStatics.USER_PREF_KEY,Context.MODE_PRIVATE);
                        pref.edit().putBoolean("PACK_CALL_ACTIVE_"+sim_slot,false)
                                .putInt("PACK_CALL_DUR_REMAINING_" + sim_slot, 0)
                                .putString("PACK_CALL_DUR_METRIC_" + sim_slot, "s")
                                .putFloat("PACK_CALL_BAL_REMAINING_" + sim_slot, 0.0f)
                                .putString("PACK_CALL_VALIDITY_" + sim_slot, "N/A")
                                .apply();
                        v.setVisibility(View.GONE);
                        dialog.cancel();
                    }});
                break;
            case R.id.pack_sms_layout:
                builder.setMessage("Delete SMS Pack ?");
                builder.setPositiveButton("Delete",  new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences pref = MyApplication.context.getSharedPreferences(ConstantsAndStatics.USER_PREF_KEY,Context.MODE_PRIVATE);
                        pref.edit().putBoolean("PACK_SMS_ACTIVE_"+sim_slot,false)
                                .putInt("PACK_SMS_REMAINING_" + sim_slot,0)
                                .putString("PACK_SMS_VALIDITY_" + sim_slot,"N/A").apply();
                        v.setVisibility(View.GONE);
                        dialog.cancel();
                    }});
                break;

            default:return false;
        }
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.show();
        return true;
    }

    @Override
    public void onResume()
    {
        //Log.d(tag,"ONResume");
        //intializeScreen();


        // End the timed event, when the user navigates away from article

        super.onResume();
        FlurryAgent.logEvent("BalanceScreen",true);
    }

    @Override
    public void onPause()
    {

        FlurryAgent.endTimedEvent("BalanceScreen");
        super.onPause();
    }

    void setPredictedDays(int sim_slot)
    {
        //Log.d("TEST", "setPredictedDays Called");
        SharedPreferences mSharedPreferences = MyApplication.context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
        currBalance = mSharedPreferences.getFloat("CURRENT_BALANCE_" + sim_slot, (float) -200.0);
        //Log.d("TEST", "setPredictedDays currBalance = "+currBalance);
        if (currBalance >= 0.0)
        {

            Integer predictedDays = mSharedPreferences.getInt("PREDICTED_DAYS", -1);
            //Log.d("TEST", "setPredictedDays predictedDays = "+predictedDays);
            if (predictedDays == -1)
            {
                predictionTextView.setText("Balance is predicted to get over on --");
            } else if (predictedDays == 0)
            {
                predictionTextView.setText("Balance is predicted to get over Today");
            } else
            {
                Calendar c = Calendar.getInstance();

                c.setTime(new Date()); //  use today date.

                c.add(Calendar.DATE, predictedDays); // Adds predicted days
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, MMM d", Locale.US);
                predictionTextView.setText("Balance is predicted to get over on " + simpleDateFormat.format(c.getTime()));

            }
        }
        balProgress.setVisibility(View.GONE);
    }

    private void createGraph()
    {
        mLineChart.setDrawGridBackground(false);
        mLineChart.setOnChartValueSelectedListener(this);
        // no description text
        mLineChart.setDescription("");
        mLineChart.setNoDataText("Your Call Deductions will come here");
        mLineChart.setNoDataTextDescription("after you start making calls");

        // enable value highlighting
        //mLineChart.setHighlightEnabled(true);
        // enable touch gestures
        mLineChart.setTouchEnabled(true);
        // enable scaling and dragging
        mLineChart.setDragEnabled(true);
        mLineChart.setScaleEnabled(true);
        mLineChart.setDrawGridBackground(false);
        //mLineChart.setBackgroundColor(getResources().getColor(R.color.white));
        // if disabled, scaling can be done on x- and y-axis separately
        mLineChart.setPinchZoom(true);
        // set an alternative background color
        // add data
        mLineChart.animateX(1000);
        // get the legend (only possible after setting data)
        Typeface tf = Typeface.createFromAsset(getResources().getAssets(), "Roboto-Thin.ttf");
        Legend l = mLineChart.getLegend();
        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(LegendForm.CIRCLE);
        l.setTypeface(tf);
        l.setTextSize(11f);
        l.setTextColor(Color.BLACK);
        l.setPosition(LegendPosition.BELOW_CHART_LEFT);
        XAxis xaxis = mLineChart.getXAxis();
        xaxis.setTypeface(tf);
        YAxis yaxis = mLineChart.getAxisLeft();
        yaxis.setTypeface(tf);
        xaxis.setPosition(XAxisPosition.BOTTOM);
        xaxis.setAvoidFirstLastClipping(true);


        BalanceMarkerView mk = new BalanceMarkerView(MyApplication.context, R.layout.custom_marker_popup);

        mLineChart.setMarkerView(mk);
    }

    private void setData()
    {

        xVals = new ArrayList<String>();
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        int i = 0;
        if (DataInitializer.ussdDataList == null)
        {
            //TODO remove this hack and use LIMIT in query
            BalanceHelper mBalanceHelper = new BalanceHelper();
            //Log.d(tag, "Got Nullfrom DataInitializer creating a again");
            //mBalanceHelper.addDemoentries();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -7);
            Date fromDate = cal.getTime();
            String fromdate = dateFormat.format(fromDate);
            //Log.d(tag,"7 day old date = "+fromDate);
            ussdDataList = mBalanceHelper.getAllEntries(sim_slot);
            //Log.d(tag ,ussdDataList.toString());
        } else ussdDataList = DataInitializer.ussdDataList;
        //Log.d(tag, "Adding Values");
        for (NormalCall be : ussdDataList)
        {
            xVals.add((String) android.text.format.DateFormat.format("EEE, hh:mm a", be.date));
            yVals.add(new Entry(be.main_bal, i));
            i++;
        }
        //Log.d(tag, "Finished  Adding Values");
        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals, "Balance");
        set1.setColor(getResources().getColor(R.color.primary_green));
        //set1.setDrawFilled(true);
        set1.setDrawCircles(true);
        set1.setCircleColor(getResources().getColor(R.color.primary_green));

        set1.setLineWidth(2f);
        // set1.setFillColor(Color.GREEN);
        // set1.setShader(new LinearGradient(0, 0, 0, mChart.getHeight(),
        // Color.BLACK, Color.WHITE, Shader.TileMode.MIRROR));
        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);
        data.setDrawValues(false);
//		 LimitLine ll1 = new LimitLine(130f, "Upper Limit");
//		 ll1.setLineWidth(4f);
//		 ll1.setLabelPosition(LimitLabelPosition.POS_RIGHT);
//		 ll1.setTextSize(10f);
//		 LimitLine ll2 = new LimitLine(-30f, "Lower Limit");
//		 ll2.setLineWidth(4f);
//		 ll2.setLabelPosition(LimitLabelPosition.POS_RIGHT);
//		 ll2.setTextSize(10f);
        if (mSharedPreferences == null)
            mSharedPreferences = MyApplication.context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
        updateLimitLine(mSharedPreferences.getFloat("MINIMUM_BALANCE", (float) 10.0));
        mLineChart.getAxisRight().setEnabled(false);
        // set data
        mLineChart.setData(data);


    }

    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    void updateLimitLine(float minimum_bal)
    {
        if (mLineChart != null)
        {
            YAxis leftAxis = mLineChart.getAxisLeft();
            leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
            LimitLine balanceReminderLine = new LimitLine(minimum_bal, "Minimum Balance = " + minimum_bal);
            balanceReminderLine.setLabelPosition(LimitLabelPosition.RIGHT_TOP);
            balanceReminderLine.setTextSize(10f);
            balanceReminderLine.setLineWidth(2f);
            leftAxis.addLimitLine(balanceReminderLine);
            //leftAxis.addLimitLine(ll2);
//		 leftAxis.setAxisMaxValue(220f);
            //leftAxis.setAxisMinValue(0f);
            leftAxis.setStartAtZero(true);
        }

    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h)
    {

    }

    @Override
    public void onNothingSelected()
    {

    }


    public void onEvent(UpdateBalanceOnScreen message)
    {
       //V17Log.d(tag,"Updating Screen "+message.toString());
        switch (message.getType())
        {
            case "MAIN_BAL" :
                if(balanceTextView == null)
                balanceTextView = (TextView) rootView.findViewById(R.id.balance_text);
               //V17Log.d(tag,"Updating to "+message.getBalance());
                balanceTextView.setText(getResources().getString(R.string.rupee_symbol)+String.format(" %.2f",message.getBalance()));
                if(!normalBalRefresh)
                {
                   //V17Log.d(tag,"Updating Balance Screen");
                    intializeScreen(sim_slot);
                }
                break;
            case "DATA_2G" :
            case "DATA_3G" :
                break;
            case "SMS_BAL" :
                break;
        }
    }
    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v)
    {
        Intent refreshIntent = new Intent(getActivity(),BalanceRefreshActivity.class);
        switch (v.getId())
        {
            case R.id.bal_layout:
                normalBalRefresh = true;
            case R.id.bal_refresh:
                refreshIntent.putExtra("SIM_SLOT",sim_slot).putExtra("TYPE","MAIN_BAL");
                startActivity(refreshIntent);
                break;
            case R.id.pack_data_layout:
                Toast.makeText(this.getActivity(),"Data Refresh is coming soon",Toast.LENGTH_LONG).show();
                //refreshIntent.putExtra("SIM_SLOT",sim_slot).putExtra("TYPE","MAIN_BAL");
                break;
            case R.id.pack_call_layout:
                Toast.makeText(this.getActivity(),"Call Pack Refresh is coming soon",Toast.LENGTH_LONG).show();
                //refreshIntent.putExtra("SIM_SLOT",sim_slot).putExtra("TYPE","MAIN_BAL");
                break;
            case R.id.pack_sms_layout:
                Toast.makeText(this.getActivity(),"SMS Refresh is coming soon ",Toast.LENGTH_LONG).show();
                //refreshIntent.putExtra("SIM_SLOT",sim_slot).putExtra("TYPE","MAIN_BAL");
                break;
        }

    }

    class USSDLoader extends AsyncTask<Integer, Void, Void>
    {


        @Override
        protected void onPreExecute()
        {
            //V12Log.d(tag,"Bal Frag onPreExecute");

            super.onPreExecute();
            //((AppCompatActivity)getActivity()).findViewById(R.id.spinner_nav).setVisibility(View.INVISIBLE);
            rootView.findViewById(R.id.rootRL).setVisibility(View.GONE);
            if (GlobalData.globalSimList.size() >= 2)
            {
                rootView.findViewById(R.id.sim_switch).setVisibility(View.GONE);
            }
            rootView.findViewById(R.id.ussd_progress).setVisibility(View.VISIBLE);

        }

        @Override
        protected Void doInBackground(Integer... params)
        {
            //V12Log.d(tag,"Bal Frag doInBackground");
            DataInitializer.initializeUSSDData(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            //V12Log.d(tag,"Bal Frag onPostExecute");
            super.onPostExecute(aVoid);
            if (isAdded())
            {
                rootView.findViewById(R.id.rootRL).setVisibility(View.VISIBLE);
                if (GlobalData.globalSimList.size() >= 2)
                {
                    rootView.findViewById(R.id.sim_switch).setVisibility(View.VISIBLE);
                }
                rootView.findViewById(R.id.ussd_progress).setVisibility(View.GONE);
                intializeScreen(sim_slot);
            } else
            {
                //V12Log.d(tag,"Bal Frag not added");
            }
        }
    }

    public class BalanceMarkerView extends MarkerView
    {

        private TextView tvContent;

        public BalanceMarkerView(Context context, int layoutResource)
        {
            super(context, layoutResource);
            // this markerview only displays a textview
            tvContent = (TextView) findViewById(R.id.tvContent);
        }

        /**
         * This method enables a specified custom MarkerView to update it's content everytime the MarkerView is redrawn.
         *
         * @param e         The Entry the MarkerView belongs to. This can also be any subclass of Entry, like BarEntry or
         *                  CandleEntry, simply cast it at runtime.
         * @param highlight the highlight object contains information about the highlighted value such as it's dataset-index, the
         */
        // callbacks everytime the MarkerView is redrawn, can be used to update the
        // content (user-interface)
        @Override
        public void refreshContent(Entry e, Highlight highlight)
        {
            tvContent.setText("Time:" + xVals.get(e.getXIndex()) + "\nBalance: " + e.getVal()); // set the entry-value as the display text
        }


        /**
         * Use this to return the desired offset you wish the MarkerView to have on the x-axis. By returning -(getWidth() /
         * 2) you will center the MarkerView horizontally.
         *
         * @param xpos the position on the x-axis in pixels where the marker is drawn
         * @return
         */
        @Override
        public int getXOffset(float xpos)
        {
            // this will center the marker-view horizontally
            return -(getWidth() / 2);
        }

        /**
         * Use this to return the desired position offset you wish the MarkerView to have on the y-axis. By returning
         * -getHeight() you will cause the MarkerView to be above the selected value.
         *
         * @param ypos the position on the y-axis in pixels where the marker is drawn
         * @return
         */
        @Override
        public int getYOffset(float ypos)
        {
            // this will cause the marker-view to be above the selected value
            return -getHeight();
        }


    }


}


