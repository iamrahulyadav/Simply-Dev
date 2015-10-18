package com.builder.ibalance;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.appsflyer.AppsFlyerLib;
import com.apptentive.android.sdk.Apptentive;
import com.builder.ibalance.database.models.ContactDetailModel;
import com.builder.ibalance.util.MyApplication;
import com.builder.ibalance.util.MyApplication.TrackerName;
import com.flurry.android.FlurryAgent;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Highlight;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.kahuna.sdk.KahunaAnalytics;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ContactDetailActivity extends Activity implements OnChartValueSelectedListener {
	@Override
	public boolean onNavigateUp()
    {
	    onBackPressed();
	    return true;
	}
	String phnumber;
	TextView name,number,total_duration,outgoing,cost,incoming,missed;
	ImageView contactPicture;
	PieChart mChart;
    ContactDetailModel c;




	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_detail);
		Intent it = this.getIntent();
		c = it.getParcelableExtra("DETAILS");
		ActionBar mActionBar = getActionBar();
		if(c!=null)
		{

            phnumber = c.number;
            //key = number values = name,InCount,InDur,OutCount,OutDur,MissCount,Provider,State,image_uri
			number = (TextView)findViewById(R.id.contact_detail_number);
			name  = (TextView)findViewById(R.id.contact_detail_name);
			mActionBar.setTitle(c.name);
			total_duration  = (TextView)findViewById(R.id.contact_detail_duration);
			outgoing = (TextView)findViewById(R.id.contact_detail_outgoing_calls);
			cost = (TextView)findViewById(R.id.contact_detail_cost);
			incoming = (TextView)findViewById(R.id.contact_detail_incoming_calls);
			missed = (TextView)findViewById(R.id.contact_detail_missed_calls);
            number.setText(c.number);
            name.setText(c.name);

            total_duration.append(getTotalDurationFormatted(c.in_duration+c.out_duration));
            outgoing.append(c.out_count+"");


            cost.append(String.format("%.2f", c.total_cost));
            incoming.append(c.in_count+"");
            missed.append(c.miss_count+"");
            addPieChart();
            if(c.image_uri!=null)
            {
                contactPicture = (ImageView) findViewById(R.id.contact_detail_picture);
                Picasso.with(this).load(c.image_uri).into(contactPicture);
            }
		}

	}
	private void addPieChart() {
		 mChart = (PieChart) findViewById(R.id.details_pie_chart);
		 mChart.setUsePercentValues(false);
		 // change the color of the center-hole
		 // mChart.setHoleColor(Color.rgb(235, 235, 235));
		 mChart.setHoleColorTransparent(true);
		 mChart.setHoleRadius(80f);
		 mChart.setDescription("");
		 mChart.setDrawCenterText(true);
		 mChart.setDrawHoleEnabled(true);
		 mChart.setRotationAngle(0);
		 // enable rotation of the chart by touch
		 mChart.setRotationEnabled(true);

		 // mChart.setDrawUnitsInChart(true);
		 // add a selection listener
		 mChart.setOnChartValueSelectedListener(this);
		 mChart.setHighlightEnabled(true);
		 mChart.setNoDataTextDescription("No Call Data Available");
		 

		 // mChart.setTouchEnabled(false);
		 mChart.setCenterText(getTotalDurationFormatted(c.in_duration) + "  Incoming\n" + getTotalDurationFormatted(c.out_duration)+"  Outgoing");
		 
		 setData();
		 mChart.animateXY(1500, 1500);
		 // mChart.spin(2000, 0, 360);
		 Legend l = mChart.getLegend();
		 l.setTextColor(getResources().getColor(R.color.dark_grey));
		 l.setPosition(LegendPosition.RIGHT_OF_CHART);
		 l.setXEntrySpace(7f);
		 l.setYEntrySpace(5f);
	}
	private void setData() {
		 ArrayList<Entry> yVals1 = new ArrayList<Entry>();
		 // IMPORTANT: In a PieChart, no values (Entry) should have the same
		 // xIndex (even if from different DataSets), since no values can be
		 // drawn above each other.
		 yVals1.add(new Entry((float)(c.in_duration), 0));
		 yVals1.add(new Entry((float)(c.out_duration), 1));
		 
		 
		 ArrayList<String> xVals = new ArrayList<String>();
		 xVals.add("Incoming");
		 xVals.add("Outgoing");
		 PieDataSet dataSet = new PieDataSet(yVals1, "Call Durations");
		 dataSet.setSliceSpace(3f);
		 // add a lot of colors
		 ArrayList<Integer> colors = new ArrayList<Integer>();
		 /*for (int c : ColorTemplate.VORDIPLOM_COLORS)
		 colors.add(c);
		 for (int c : ColorTemplate.JOYFUL_COLORS)
		 colors.add(c);
		 for (int c : ColorTemplate.COLORFUL_COLORS)
		 colors.add(c);
		 for (int c : ColorTemplate.LIBERTY_COLORS)
		 colors.add(c);
		 for (int c : ColorTemplate.PASTEL_COLORS)
		 colors.add(c);
		 colors.add(ColorTemplate.getHoloBlue());*/
		 colors.add(getResources().getColor(R.color.primary_green));
		 colors.add(getResources().getColor(R.color.logo_orange));
		 dataSet.setColors(colors);
		 PieData data = new PieData(xVals, dataSet);
		 //data.setValueFormatter(new PercentFormatter());
		 data.setDrawValues(false);
		 //data.setDrawValues(true);

		 data.setValueTextSize(0f);
//		 data.setValueTextColor(Color.WHITE);
		 mChart.setData(data);
		 // undo all highlights
		 mChart.highlightValues(null);
		 mChart.invalidate();
		
	}
	 @Override
	    protected void onStart() {
	       
	        KahunaAnalytics.start();
	  	  Apptentive.onStart(this);
	  	AppsFlyerLib.sendTrackingWithEvent(MyApplication.context,"Contact Detail","");
	  	FlurryAgent.logEvent("ContactDetailHelper", true);
		Apptentive.engage(this, "Contact Detail");
		 super.onStart();
	    }

	    @Override
	    protected void onStop() {
	       
	        KahunaAnalytics.stop();
	        FlurryAgent.endTimedEvent("ContactDetailHelper");
	  	  Apptentive.onStop(this);
	  	 super.onStop();
	    } 
	private String getTotalDurationFormatted(int totalSecs) {
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.contact, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		Tracker t = ((MyApplication) this.getApplication()).getTracker(
			    TrackerName.APP_TRACKER);
		switch (id) {
		case R.id.call:
			t.send(new HitBuilders.EventBuilder()
		    .setCategory("CALL")
		    .setAction("CALL")
		    .setLabel("")
		    .build());
	 	
	Apptentive.engage(this, "CALL");
	FlurryAgent.logEvent("CALL");
	AppsFlyerLib.sendTrackingWithEvent(MyApplication.context,"CALL","");
			Intent intent = new Intent(Intent.ACTION_DIAL);
			intent.setData(Uri.parse("tel:"+phnumber));
			startActivity(intent); 
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
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
