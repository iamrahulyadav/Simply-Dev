package com.builder.ibalance;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appsflyer.AppsFlyerLib;
import com.apptentive.android.sdk.Apptentive;
import com.builder.ibalance.database.helpers.BalanceHelper;
import com.builder.ibalance.database.models.NormalCall;
import com.builder.ibalance.datainitializers.DataInitializer;
import com.builder.ibalance.util.GlobalData;
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
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Highlight;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.melnykov.fab.FloatingActionButton;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BalanceFragment extends Fragment implements OnChartValueSelectedListener, OnClickListener{
	final String tag = BalanceFragment.class.getSimpleName();
	SharedPreferences mSharedPreferences;
	float currBalance = 0,currData= 0;
	TextView balanceTextView,dataTextView,predictionTextView;
	LinearLayout balance_layout;
	//Button balance_DetailsButton;
	public LineChart mLineChart ;
	ProgressBar balProgress;
	ArrayList<String> xVals;
	View rootView;
    static int sim_slot = 0;
	public  List<NormalCall> ussdDataList = null;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Typeface tf =  Typeface.createFromAsset(getResources().getAssets(), "Roboto-Regular.ttf");
		 rootView = inflater.inflate(R.layout.fragment_balance, container,
				false);
		//MainActivity.dateSelector.setVisible(false);
		final FloatingActionButton sim_switch = (FloatingActionButton) rootView.findViewById(R.id.sim_switch);
		mSharedPreferences = getActivity().getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
		predictionTextView = (TextView) rootView.findViewById(R.id.predictionView);
		predictionTextView.setTypeface(tf);
		balProgress =  (ProgressBar) rootView.findViewById(R.id.balscreen_prediction);
		balanceTextView = (TextView) rootView.findViewById(R.id.balanceView);
		balanceTextView.setTypeface(tf);
		dataTextView = (TextView) rootView.findViewById(R.id.dataView);
		dataTextView.setTypeface(tf);
		balance_layout = (LinearLayout) rootView.findViewById(R.id.bal_layout);
        mLineChart = (LineChart) rootView.findViewById(R.id.balcontainer);
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
                        sim_slot = 1;

                    }
                    else
                        sim_slot = 0;
                    Toast.makeText(MyApplication.context,"Switched to Sim "+(sim_slot+1),Toast.LENGTH_LONG).show();
                    getActivity().getActionBar().setTitle(GlobalData.globalSimList.get(sim_slot).carrier + " - " + (sim_slot + 1));
                    new USSDLoader().execute(sim_slot);
                }
            });
        }
		else
        {
            sim_switch.setVisibility(View.GONE);
        }
        getActivity().getActionBar().setTitle(GlobalData.globalSimList.get(sim_slot).carrier+" - "+(sim_slot+1));
        new USSDLoader().execute(sim_slot);
		return rootView;
	}
    class USSDLoader extends AsyncTask<Integer ,Void,Void>
    {


        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            rootView.findViewById(R.id.rootRL).setVisibility(View.GONE);
            rootView.findViewById(R.id.sim_switch).setVisibility(View.GONE);
            rootView.findViewById(R.id.ussd_progress).setVisibility(View.VISIBLE);

        }

        @Override
        protected Void doInBackground(Integer... params)
        {
            DataInitializer.initializeUSSDData(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);

            rootView.findViewById(R.id.rootRL).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.sim_switch).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.ussd_progress).setVisibility(View.GONE);
            intializeScreen(sim_slot);
        }
    }
	void intializeScreen(int sim_slot)
	{
		currBalance = mSharedPreferences.getFloat("CURRENT_BALANCE_"+sim_slot, mSharedPreferences.getFloat("CURRENT_BALANCE",(float)-1.0));
		currData = mSharedPreferences.getFloat("CURRENT_DATA_"+sim_slot, mSharedPreferences.getFloat("CURRENT_DATA",(float)-1.0));
		//Log.d(tag,"Balance  = "+currBalance);
		//Log.d(tag,"DATA  = "+currData);
		if (DataInitializer.done == true) 
		{
			//Log.d(tag, "Setting Predicted days");
			setPredictedDays(sim_slot);
		}
		
	
		if(currBalance<0.0)
			balanceTextView.setText(getResources().getString(R.string.rupee_symbol)+" --.--" );
		else
		balanceTextView.setText(getResources().getString(R.string.rupee_symbol)+" "+ currBalance);
		/*balance_DetailsButton = (Button) rootView.findViewById(R.id.detail_button);
		balance_DetailsButton.setOnClickListener(this);*/
		balance_layout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity( new Intent(getActivity(), HistoryActivity.class));
			}
		});
		if(currBalance>-1.0)
		{
		((RelativeLayout)rootView.findViewById(R.id.nobal)).setVisibility(View.GONE);
		mLineChart.setVisibility(View.VISIBLE);
		createGraph();
		}
		if(DataInitializer.ussdDataList.size()>0)
		{
			setData();
		}
		
		if(currData>0.0)
		{
			dataTextView.setText(currData+"MB");
		}
		dataTextView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Toast.makeText(getActivity(), "This Feature is  in Build!", Toast.LENGTH_SHORT).show();
			}
		});
	}
	@Override
	public void onResume() {
		//Log.d(tag,"ONResume");
		//intializeScreen();
		Tracker t = ((MyApplication) getActivity().getApplication()).getTracker(
			    TrackerName.APP_TRACKER);

			// Set screen name.
			t.setScreenName("BalanceScreen");
			
			// Send a screen view.
			t.send(new HitBuilders.ScreenViewBuilder().build());
			// Capture author info & user status

			Apptentive.engage(this.getActivity(), "BalanceScreen");
			//Log the timed event when the user starts reading the article
			//setting the third param to true creates a timed event
			FlurryAgent.logEvent("BalanceScreen", true);
			AppsFlyerLib.sendTrackingWithEvent(MyApplication.context,"Balance Screen","");
			 
			// End the timed event, when the user navigates away from article

		super.onResume();
	}
	
	@Override
	public void onPause() {

		FlurryAgent.endTimedEvent("BalanceScreen");
		super.onPause();
	}

	void setPredictedDays(int sim_slot) {
		//Log.d("TEST", "setPredictedDays Called");
		SharedPreferences mSharedPreferences = MyApplication.context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
		currBalance = mSharedPreferences.getFloat("CURRENT_BALANCE_"+sim_slot, (float)-1.0);
		//Log.d("TEST", "setPredictedDays currBalance = "+currBalance);
		if(currBalance>=0.0)
		{
		
		Integer predictedDays = mSharedPreferences.getInt("PREDICTED_DAYS", -1);
		//Log.d("TEST", "setPredictedDays predictedDays = "+predictedDays);
		if(predictedDays==-1)
		{
			predictionTextView.setText("Balance is predicted to get over on --");
		}
		else if(predictedDays==0)
	      {
	    	  predictionTextView.setText("Balance is predicted to get over Today");
	      }
		else
		{
			Calendar c = Calendar.getInstance();

			c.setTime(new Date()); //  use today date.

			c.add(Calendar.DATE, predictedDays); // Adds predicted days
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, MMM d",Locale.US);
			predictionTextView.setText("Balance is predicted to get over on "+simpleDateFormat.format(c.getTime()));
			
		}
		}
		balProgress.setVisibility(View.GONE);
	}

	private void createGraph() {
		mLineChart.setDrawGridBackground(false);
		mLineChart.setOnChartValueSelectedListener(this);
		// no description text
		mLineChart.setDescription("");
		mLineChart.setNoDataTextDescription("Please Make Calls for Balance tracking");

		// enable value highlighting
		mLineChart.setHighlightEnabled(true);
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
	
		
		BalanceMarkerView mk =new BalanceMarkerView(this.getActivity(),R.layout.custom_marker_popup);

		mLineChart.setMarkerView(mk);
	}

	private void setData() {

		 xVals = new ArrayList<String>();
		 ArrayList<Entry> yVals = new ArrayList<Entry>();
		 int i=0;
		 if(DataInitializer.ussdDataList==null)
		 {
			 BalanceHelper mBalanceHelper = new BalanceHelper();
			  //Log.d(tag, "Got Nullfrom DataInitializer creating a again");
				//mBalanceHelper.addDemoentries();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.getDefault());
		        Calendar cal = Calendar.getInstance();
		        cal.add(Calendar.DATE, -7);
		        Date fromDate = cal.getTime();  
		        String fromdate = dateFormat.format(fromDate);
		        //Log.d(tag,"7 day old date = "+fromDate);
		        ussdDataList = mBalanceHelper.getAllEntries();
				 //Log.d(tag ,ussdDataList.toString());
				mBalanceHelper.close();
		 }
		 else
			 ussdDataList = DataInitializer.ussdDataList;
		 //Log.d(tag, "Adding Values");
		 for(NormalCall be: ussdDataList) {
			 xVals.add((String)android.text.format.DateFormat.format("EEE, hh:mm a", be.date));
		 yVals.add(new Entry(be.bal, i));
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
		 setLimitLine();
		 mLineChart.getAxisRight().setEnabled(false);
		 // set data
		 mLineChart.setData(data);
		

	}
	void setLimitLine() {
		if(mLineChart!=null)
		{YAxis leftAxis = mLineChart.getAxisLeft();
		 leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
		 SharedPreferences mSharedPreferences = this.getActivity().getSharedPreferences("USER_DATA",Context.MODE_PRIVATE);
		 float balance_reminder_value = mSharedPreferences.getFloat("MINIMUM_BALANCE", (float) 10.0); 
		 LimitLine balanceReminderLine = new LimitLine(balance_reminder_value, "Minimum Balance = "+balance_reminder_value);
		 balanceReminderLine.setLabelPosition(LimitLabelPosition.POS_RIGHT);
		 balanceReminderLine.setTextSize(10f);
		 balanceReminderLine.setLineWidth(2f);
		 leftAxis.addLimitLine(balanceReminderLine);
		 //leftAxis.addLimitLine(ll2);
//		 leftAxis.setAxisMaxValue(220f);
		 //leftAxis.setAxisMinValue(0f);
		 leftAxis.setStartAtZero(true);
		}

	}
	

	public class BalanceMarkerView extends MarkerView {

	    private TextView tvContent;

	    public BalanceMarkerView (Context context, int layoutResource) {
	        super(context, layoutResource);
	        // this markerview only displays a textview
	        tvContent = (TextView) findViewById(R.id.tvContent);
	    }

	    // callbacks everytime the MarkerView is redrawn, can be used to update the
	    // content (user-interface)
	    @Override
	    public void refreshContent(Entry e, int dataSetIndex) {
	        tvContent.setText("Time:" +xVals.get(e.getXIndex())+"\nBalance: "+e.getVal() ); // set the entry-value as the display text
	        
	    }

	    @Override
	    public int getXOffset() {
	        // this will center the marker-view horizontally
	        return -(getWidth() / 2);
	    }

	    @Override
	    public int getYOffset() {
	        // this will cause the marker-view to be above the selected value
	        return -getHeight();
	    }

	
}
	@Override
	public void onClick(View v) {
		/*if(v.getId() == R.id.detail_button) 
		{
			//final Intent intent =;
			getActivity().startActivity( new Intent(getActivity(), HistoryActivity.class));
		}*/
		
	}

	@Override
	public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNothingSelected() {
		// TODO Auto-generated method stub
		
	}

}
