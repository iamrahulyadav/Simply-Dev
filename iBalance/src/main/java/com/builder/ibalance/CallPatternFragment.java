package com.builder.ibalance;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.builder.ibalance.database.helpers.DateDurationMapHelper;
import com.builder.ibalance.database.models.DateDurationModel;
import com.builder.ibalance.datainitializers.FilteredDataInitializer;
import com.builder.ibalance.messages.FilteredData;
import com.builder.ibalance.util.BarChartItem;
import com.builder.ibalance.util.ChartItem;
import com.builder.ibalance.util.LineChartItem;
import com.builder.ibalance.util.MyApplication;
import com.builder.ibalance.util.MyApplication.TrackerName;
import com.builder.ibalance.util.PieChartItem;
import com.builder.ibalance.util.Tuple;
import com.flurry.android.FlurryAgent;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import de.greenrobot.event.EventBus;

public class CallPatternFragment extends Fragment implements
        OnChartGestureListener, OnItemSelectedListener
{
    final static String TAG = CallPatternFragment.class.getSimpleName();
    int dummy = 0;
    BarChartItem day_count, carrier_duration, circle_count, in_out_duration;
    PieChartItem most_called;
    LineChartItem count_duration;
    View view;
    ArrayList<Integer> outCount, inDuration, outDuration;
    ArrayList<String> weekDays = new ArrayList<String>();
    MenuItem dateSelector;
    SharedPreferences mSharedPreferences = MyApplication.context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE);
    Spinner spinner;
    DateDurationMapHelper dateDurationMapHelper = new DateDurationMapHelper();
    ArrayList<Tuple> mostCalledList, carrierOutCountList, circleOutDurationList;
    ArrayList<ChartItem> list;
    ListView mListView;
    ProgressBar mProgressBar;
    long startDate = 0l;
    long endDate = 0l;
    int filter = mSharedPreferences.getInt("FILTER", 0);
    public static <K, V extends Comparable<V>> Map<K, V> sortByValues(
            final Map<K, V> map)
    {
        Comparator<K> valueComparator = new Comparator<K>()
        {
            public int compare(K k1, K k2)
            {
                int compare = map.get(k2).compareTo(map.get(k1));
                if (compare == 0)
                    return 1;
                else
                    return compare;
            }
        };
        Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.fragment_call_pattern, container,
                false);
        //Log.d("SPINNER", "onCreateView ");
        mProgressBar = (ProgressBar) view.findViewById(R.id.data_loading);
        mListView = (ListView) view.findViewById(R.id.listView1);
        list = new ArrayList<ChartItem>();

        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onStart()
    {

        weekDays.add("Sun");
        weekDays.add("Mon");
        weekDays.add("Tue");
        weekDays.add("Wed");
        weekDays.add("Thu");
        weekDays.add("Fri");
        weekDays.add("Sat");
        EventBus.getDefault().register(this);

        filter = mSharedPreferences.getInt("FILTER", 0);

        //Get IST Time zone date-time
        Calendar c = Calendar.getInstance(TimeZone.getDefault());
        endDate = c.getTimeInMillis();
        //Get nex day same time
        c.add(Calendar.DAY_OF_MONTH, +1);
        //Get Midnight time
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        if (filter == 2)//all data
        {
            startDate = 0l;

            mProgressBar.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
            new FilteredDataInitializer(startDate, endDate).execute();
        } else if (filter == 1)
        {

            //Get 1 Month  Prior midnight time
            c.add(Calendar.MONTH, -1);
           //V10Log.d(TAG, "One Month Prior date = " + c.toString());
            ;
            startDate = c.getTimeInMillis();
            mProgressBar.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
            new FilteredDataInitializer(startDate, endDate).execute();
            //Log.d(TAG,"else WEEKS  "+ FilteredDataInitializer.filteredDateDurationMap.size());
        } else if (filter == 0)
        {
            //Get 7 Days Prior midnight time
            c.add(Calendar.DAY_OF_MONTH, -7);
           //V10Log.d(TAG, "Seven Days Prior date = " + c.toString());
            startDate = c.getTimeInMillis();

            mProgressBar.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
            new FilteredDataInitializer(startDate, endDate).execute();
            //Log.d(TAG,"else WEEKS  "+ FilteredDataInitializer.filteredDateDurationMap.size());
        }
        super.onStart();
    }

    @Override
    public void onStop()
    {
       //V10Log.d(TAG, "onStop");
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        MyApplication.getRefWatcher().watch(this);
    }

    void clearCharts()
    {
        if (day_count != null)
        {
            day_count.clearChart();

            carrier_duration.clearChart();
            circle_count.clearChart();
            in_out_duration.clearChart();
            most_called.clearChart();
            count_duration.clearChart();
        }
    }

    @Override
    public void onResume()
    {
        dummy = 0;
       //V10Log.d(TAG, "ONrESUME");
        Tracker t = ((MyApplication) getActivity().getApplication()).getTracker(
                TrackerName.APP_TRACKER);

        // Set screen name.
        t.setScreenName("CallPatternScreen");

        // Send a screen view.
        t.send(new HitBuilders.ScreenViewBuilder().build());
        //dateSelector.setVisible(true);

        //Log the timed event when the user starts reading the article
        //setting the third param to true creates a timed event
        FlurryAgent.logEvent("CallPatternScreen", true);
       //V10AppsFlyerLib.sendTrackingWithEvent(MyApplication.context, "CallPatternScreen", "");
        // End the timed event, when the user navigates away from article

        super.onResume();
    }

    @Override
    public void onPause()
    {
       //V10Log.d(TAG, "onPause");
        FlurryAgent.endTimedEvent("CallPatternScreen");
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
       //V10Log.d(TAG, "onCreateOptionsMenu");
        super.onCreateOptionsMenu(menu, inflater);
        dateSelector = menu.findItem(R.id.menu_spinner);
        dateSelector.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        dateSelector.setVisible(true);
        View view = dateSelector.getActionView();
        if (view instanceof Spinner)
        {
            spinner = (Spinner) view;
            spinner.setAdapter(ArrayAdapter.createFromResource(this.getActivity(),
                    R.array.analytics_period,
                    R.layout.date_spinner_item));
            spinner.setSelection(filter, false);
            spinner.setOnItemSelectedListener(this);

        }
    }

    public void loadData(FilteredData mFilteredData)
    {

       //V10Log.d(TAG, "Loading Charts");
        list.clear();
        mostCalledList = mFilteredData.getMostCalled();
        circleOutDurationList = mFilteredData.getCircleList();
        carrierOutCountList = mFilteredData.getCarrierList();
        initializeWeekData(startDate, endDate);
        {
            list.add(new PieChartItem(setMostCalledData(), getActivity(), "Top 5 People Called(no. of calls)"));
            list.add(new BarChartItem(setDay_OutCountData(), getActivity(), "Which Day Are You Calling The Most?"));
            list.add(new BarChartItem(setCircle_outDurationData(), getActivity(), "Which State Are You Calling The Most?"));
            list.add(new BarChartItem(setCarrier_outCountData(), getActivity(), "Which Network Are You Calling The Most?"));
            list.add(new BarChartItem(setInOut_DurationData(), getActivity(), "What's Your Incoming and Outgoing Pattern?"));
            list.add(new LineChartItem(setOutCount_outDurationData(), getActivity(), "How much Time is Spent per Call?"));

        }
        mProgressBar.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);

        ChartDataAdapter cda = new ChartDataAdapter(getActivity(), list);
        mListView.setAdapter(cda);
        //Log.d(TAG, "Finished Loading Charts");

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
                               long id)
    {
       //V10Log.d("SPINNER", "position = "+position);
       //V10Log.d("SPINNER", "filter = "+filter);
        if (filter == position)
        {
           //V10Log.d(TAG, "Not changing Charts");
            return;
        }
       //V10Log.d(TAG, "Changing Charts");
        mListView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        Calendar c = Calendar.getInstance(TimeZone.getDefault());
        endDate = c.getTimeInMillis();
        //Will Get Data Back By EventBus
        switch (position)
        {
            case 0:
                c.add(Calendar.DAY_OF_MONTH, +1);
                //Get Midnight time
                c.set(Calendar.HOUR, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                //Get 7 Days Prior midnight time
                c.add(Calendar.DAY_OF_MONTH, -7);
               //V10Log.d(TAG, "Seven Days Prior date = " + c.toString());
                ;
                startDate = c.getTimeInMillis(); //exact 1 week
                mProgressBar.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);
                new FilteredDataInitializer(startDate, endDate).execute();
                break;
            case 1:
                c.add(Calendar.DAY_OF_MONTH, +1);
                //Get Midnight time
                c.set(Calendar.HOUR, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                //Get 7 Days Prior midnight time
                c.add(Calendar.MONTH, -1);
               //V10Log.d(TAG, "One month Prior date = " + c.toString());
                ;
                startDate = c.getTimeInMillis(); //exact 1 week
                mProgressBar.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);
                new FilteredDataInitializer(startDate, endDate).execute();
                break;
            case 2:
                mProgressBar.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);
                startDate = 0l;
                new FilteredDataInitializer(startDate, endDate).execute();
                break;

            default:
                break;
        }
        filter = position;
        mSharedPreferences.edit().putInt("FILTER",position).commit();


    }

    // InCount-0 Indur-1 OutCount-2 OutDur-3
    void initializeWeekData(long startDate, long endDate)
    {
        //Asof now not using inCount but querying it for future use
        outCount = new ArrayList<Integer>(Collections.nCopies(7, 0));
        inDuration = new ArrayList<Integer>(Collections.nCopies(7, 0));
        outDuration = new ArrayList<Integer>(Collections.nCopies(7, 0));
        ArrayList<DateDurationModel> avgDateDuarationDetail =
                dateDurationMapHelper.getCallPatterDetails(startDate, endDate);
        for (DateDurationModel m : avgDateDuarationDetail)
        {
            outCount.set(m.day_of_the_week - 1, m.out_count);
            inDuration.set(m.day_of_the_week - 1, m.in_duration);
            outDuration.set(m.day_of_the_week - 1, m.out_duration);
        }
    }

    int getWeeksBetween(Date a, Date b)
    {

        if (b.before(a))
        {
            return -getWeeksBetween(b, a);
        }
        a = resetTime(a);
        b = resetTime(b);
        //Log.d(TAG+ "WeekData", (String) android.text.format.DateFormat.format("EEE", a));
        //Log.d(TAG+ "WeekData", (String) android.text.format.DateFormat.format("EEE", b));
        Calendar cal = new GregorianCalendar();
        cal.setTime(a);
        int weeks = 0;
        while (cal.getTime().before(b))
        {
            // add another week
            cal.add(Calendar.WEEK_OF_YEAR, 1);
            weeks++;
        }
        return weeks;
    }

    Date resetTime(Date d)
    {
        Calendar cal = new GregorianCalendar();
        cal.setTime(d);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private BarData setDay_OutCountData()
    {
        //Log.d(TAG, "SetDAta Fragment");
        //reverseMap = new TreeMap<Date, ArrayList<Integer>>(
        //		Collections.reverseOrder());
        //reverseMap.putAll(dateDurationMap);
        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        int todayIndex = c.get(Calendar.DAY_OF_WEEK);
        todayIndex -= 1;
        //Log.d(TAG+ "WeekData outC","todayIndex = "+todayIndex);
        //Log.d(TAG+ "WeekData outC", "Out COUNT "+ outCount.toString());
        int j = 0;
        for (int i = todayIndex + 1; i < outCount.size(); i++)
        {
            xVals.add(weekDays.get(i));
            yVals1.add(new BarEntry(outCount.get(i), j));
            j++;
        }
        for (int i = 0; i <= todayIndex; i++)
        {
            xVals.add(weekDays.get(i));
            yVals1.add(new BarEntry(outCount.get(i), j));
            j++;
            ;
        }

        BarDataSet set1 = new BarDataSet(yVals1, "Weekly OutGoing Count(avg)");
        set1.setColor(MyApplication.context.getResources().getColor(R.color.primary_green));
        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set1);
        BarData data = new BarData(xVals, dataSets);
        // data.setValueFormatter(new MyValueFormatter());
        data.setValueTextSize(10f);
        // day_outCount_chart.setData(data);
        //Log.d(TAG, "Setting OKAy");
        return data;

    }

    private BarData setCircle_outDurationData()
    {
        //Log.d(TAG, "SetDAta Fragment");
        Object set2 = null;
        Map<String, Integer> temp = new TreeMap<String, Integer>();
        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        int j = 0;
        for (Tuple t : circleOutDurationList)
        {
            xVals.add(t.getString_val());
            yVals1.add(new BarEntry(t.getInt_val(), j));
            j++;
        }
        // name,InCount,InDur,OutCount,OutDur,MissCount,Provider,State,imageUuri
        /*for (Entry<String, Object[]> entry : mainMap.entrySet()) {

			Object[] value = entry.getValue();
			if (temp.get(value[7].toString()) == null)
				{
				if( ((Integer)value[3]).intValue()!=0)
				temp.put(value[7].toString(),
						Integer.parseInt(value[3].toString()));
				}
			else
				temp.put(
						value[7].toString(),
						Integer.parseInt(temp.get(value[7].toString())
								.toString())
								+ Integer.parseInt(value[3].toString()));
		}
		for (Entry<String, Integer> entry : temp.entrySet()) {
			xVals.add(entry.getKey());
			yVals1.add(new BarEntry(entry.getValue(), j));
			j++;
		}*/
        //	//Log.d("TEST","Circle xval " + xVals.size() + "  yVal = " + yVals1.size());
        BarDataSet set1 = new BarDataSet(yVals1, "OutGoing Duration (mins)");
        set1.setColor(getResources().getColor(R.color.primary_green));
        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set1);


        BarData data = new BarData(xVals, dataSets);
        // data.setValueFormatter(new MyValueFormatter());
        data.setValueTextSize(10f);
        // circle_outDuration_chart.setData(data);
        //Log.d(TAG, "Setting OKAy");
        return data;
    }


    private BarData setCarrier_outCountData()
    {
        //Log.d(TAG, "SetDAta Fragment");
        Map<String, Integer> temp = new TreeMap<String, Integer>();
        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        int j = 0;
        for (Tuple t : carrierOutCountList)
        {
            xVals.add(t.getString_val());
            yVals1.add(new BarEntry((t.getInt_val()), j));
            j++;
        }
        // name,InCount,InDur,OutCount,OutDur,MissCount,Provider,State,imageUuri
		/*for (Entry<String, Object[]> entry : mainMap.entrySet()) {

			Object[] value = entry.getValue();
			try{
			if (temp.get(value[6].toString()) == null)
			{
				if( ((Integer)value[4]).intValue()!=0)
				temp.put(value[6].toString(),
						Integer.parseInt(value[4].toString()));
			}
			else
				temp.put(
						value[6].toString(),
						Integer.parseInt(temp.get(value[6].toString())
								.toString())
								+ Integer.parseInt(value[4].toString()));
			}
			catch(Exception e)
			{
				ParseObject pObj = new ParseObject("ERROR_LOGS");
				pObj.put("PLACE","CallPattern_Operator");
				if(value!=null)
				pObj.put("Object",valueToString(value) );
				else
					pObj.put("Object","It is Null" );
				pObj.saveEventually();
			}

			*//*
			 * dateData.add(new DataFeeder(key, value.get(0), value.get(1),
			 * value .get(2), value.get(3)));
			 *//*
		}
		for (Entry<String, Integer> entry : temp.entrySet()) {
			xVals.add(entry.getKey());
			yVals1.add(new BarEntry(((entry.getValue()+60-1)/60), j));
			j++;
		}*/
        ////Log.d("TEST","operator xval " + xVals.size() + "  yVal = " + yVals1.size());
        BarDataSet set1 = new BarDataSet(yVals1, "OutGoing Count ");
        set1.setColor(getResources().getColor(R.color.primary_green));
        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set1);
        BarData data = new BarData(xVals, dataSets);
        // data.setValueFormatter(new MyValueFormatter());
        data.setValueTextSize(10f);
        // operator_outDuration_chart.setData(data);
        //Log.d(TAG, "Setting OKAy");
        return data;

    }

    String valueToString(Object[] value)
    {
        return (String) (value[0] + "   " + value[1] + "  " + value[2] + "  " + value[3] + "  " + value[4] + "  " + value[5] + "  " + value[6] + "  " + value[7] + "  " + value[8]);

    }

    private LineData setOutCount_outDurationData()
    {
        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<com.github.mikephil.charting.data.Entry> yVals1 = new ArrayList<com.github.mikephil.charting.data.Entry>();
        ArrayList<com.github.mikephil.charting.data.Entry> yVals2 = new ArrayList<com.github.mikephil.charting.data.Entry>();

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        int todayIndex = c.get(Calendar.DAY_OF_WEEK);
        todayIndex -= 1;
        //Log.d(TAG+ "WeekData outCount OutDuration","todayIndex = "+todayIndex);
       //V10Log.d(TAG + "WeekData outC", "Out COUNT " + outCount.toString());
       //V10Log.d(TAG + "WeekData outC", "OutDuration " + outDuration.toString());
        int j = 0;
        for (int i = todayIndex + 1; i < outCount.size(); i++)
        {
            xVals.add(weekDays.get(i));
            yVals1.add(new BarEntry(outCount.get(i), j));
            yVals2.add(new BarEntry(outDuration.get(i), j));
            j++;
        }
        for (int i = 0; i <= todayIndex; i++)
        {
            xVals.add(weekDays.get(i));
            yVals1.add(new BarEntry(outCount.get(i), j));
            yVals2.add(new BarEntry(outDuration.get(i), j));
            j++;
            ;
        }

        LineDataSet set1 = new LineDataSet(yVals1, "OutGoing Count(Avg.)");
        set1.setColor(getResources().getColor(R.color.primary_green));
        set1.setLineWidth(5f);
        //set1.addColor(Color.rgb(104, 025, 220));
        LineDataSet set2 = new LineDataSet(yVals2, " Avg. OutGoing Duration(mins)");
        set2.setColor(Color.rgb(104, 220, 10));
        set2.setLineWidth(5f);
        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set1);
        dataSets.add(set2);
        LineData data = new LineData(xVals, dataSets);
        // data.setValueFormatter(new MyValueFormatter());
        data.setValueTextSize(10f);
        // outCount_OutDuration_Chart.setData(data);
        //Log.d(TAG, "Setting OKAy");
        return data;
    }

    private BarData setInOut_DurationData()
    {
        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        ArrayList<BarEntry> yVals2 = new ArrayList<BarEntry>();

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        int todayIndex = c.get(Calendar.DAY_OF_WEEK);
        todayIndex -= 1;
        //Log.d(TAG+ "WeekData InDuration OutDuration","todayIndex = "+todayIndex);
        //Log.d(TAG+ "WeekData InDuration", "InDuration "+ inDuration.toString());
        //Log.d(TAG+ "WeekData OutDuration", "OutDuration "+ outDuration.toString());
        int j = 0;
        for (int i = todayIndex + 1; i < inDuration.size(); i++)
        {
            xVals.add(weekDays.get(i));
            yVals1.add(new BarEntry(inDuration.get(i), j));
            yVals2.add(new BarEntry(outDuration.get(i), j));
            j++;
        }
        for (int i = 0; i <= todayIndex; i++)
        {
            xVals.add(weekDays.get(i));
            yVals1.add(new BarEntry(inDuration.get(i), j));
            yVals2.add(new BarEntry(outDuration.get(i), j));
            j++;
            ;
        }
        BarDataSet set1 = new BarDataSet(yVals1, "Avg. Incoming Duration(mins)");
        set1.setColor(getResources().getColor(R.color.primary_green));
        BarDataSet set2 = new BarDataSet(yVals2, "Avg. OutGoing Duration(mins)");
        set2.setColor(Color.rgb(164, 228, 251));
        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set1);
        dataSets.add(set2);
        BarData data = new BarData(xVals, dataSets);
        // data.setValueFormatter(new MyValueFormatter());
        data.setValueTextSize(10f);
        data.setGroupSpace(80f);
        // inOut_Duration_Chart.setData(data);
        return data;

    }

    private PieData setMostCalledData()
    {
        // IMPORTANT: In a PieChart, no values (Entry) should have the same
        // xIndex (even if from different DataSets), since no values can be
        // drawn above each other.
        Map<String, Integer> temp = new TreeMap<String, Integer>();
        ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<com.github.mikephil.charting.data.Entry> yVals = new ArrayList<com.github.mikephil.charting.data.Entry>();
        int j = 0;
        int count = 0;
        int otherval = 0;
        otherval = otherval * 0;
        // name,InCount,InDur,OutCount,OutDur,MissCount,Provider,State,imageUuri
        for (Tuple t : mostCalledList)
        {
            xVals.add(t.getString_val());
            yVals.add(new com.github.mikephil.charting.data.Entry(t.getInt_val(), j));
        }
		/*for (Entry<String, Object[]> entry : mainMap.entrySet()) {

			Object[] value = entry.getValue();
			if (temp.get(value[0].toString()) == null) {
				count = Integer.parseInt(value[3].toString());
				otherval += count;
				temp.put(value[0].toString(), count);
			} else {
				count = Integer.parseInt(temp.get(value[0].toString())
						.toString()) + Integer.parseInt(value[3].toString());
				otherval += count;
				temp.put(value[0].toString(), count);
			}

			// //Log.d(TAG,"Mostcalled"+value[0].toString());
			*//*
			 * dateData.add(new DataFeeder(key, value.get(0), value.get(1),
			 * value .get(2), value.get(3)));
			 *//*
		}
		count = 0;

		for (Entry<String, Integer> entry : sortByValues(temp).entrySet()) {
			if (j < 5) {
				xVals.add(entry.getKey());
				if(entry.getValue()<=0)
					break;
				yVals.add(new com.github.mikephil.charting.data.Entry(entry
						.getValue(), j));
				count += entry.getValue();
			}

			j++;
		}*/
		/*
		 * xVals.add("Others"); yVals.add(new
		 * com.github.mikephil.charting.data.Entry(otherval-count,j));
		 */

        PieDataSet dataSet = new PieDataSet(yVals, "MostCalled");
        dataSet.setSliceSpace(3f);
        // add a lot of colors
        ArrayList<Integer> colors = new ArrayList<Integer>();
//		for (int c : ColorTemplate.VORDIPLOM_COLORS)
//			colors.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);
        colors.add(ColorTemplate.getHoloBlue());
        dataSet.setColors(colors);
        PieData data = new PieData(xVals, dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.GRAY);
        // mostCalled_Chart.setData(data);
        // undo all highlights
        // mostCalled_Chart.invalidate();
        return data;

    }


    /**
     * Callbacks when a touch-gesture has started on the chart (ACTION_DOWN)
     *
     * @param me
     * @param lastPerformedGesture
     */
    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture)
    {

    }

    /**
     * Callbacks when a touch-gesture has ended on the chart (ACTION_UP, ACTION_CANCEL)
     *
     * @param me
     * @param lastPerformedGesture
     */
    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture)
    {

    }

    @Override
    public void onChartLongPressed(MotionEvent me)
    {
       //V12Log.i("LongPress", "Chart longpressed.");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me)
    {
       //V12Log.i("DoubleTap", "Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me)
    {
       //V12Log.i("SingleTap", "Chart single-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent e1, MotionEvent e2, float velocityX,
                             float velocityY)
    {
       //V12Log.i("Fling", "Chart flinged. VeloX: " + velocityX + ", VeloY: + velocityY);

		/*
		 * ////Log.d("CallLog", "Fling detected"); // Swipe left (next) if
		 * (e1.getX() > e2.getX()) { ////Log.d("CallLog", "Fling left");
		 * 
		 * mViewFlipper.showNext(); }
		 * 
		 * // Swipe right (previous) if (e1.getX() < e2.getX()) {
		 * ////Log.d("CallLog", "Fling right"); //
		 * viewFlipper.setOutAnimation(ctx, // android.R.anim.slide_out_right);
		 * mViewFlipper.showPrevious(); }
		 */

        // return super(e1, e2, velocityX, velocityY);

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY)
    {

    }

    /**
     * Callbacks when the chart is moved / translated via drag gesture.
     *
     * @param me
     * @param dX translation distance on the x-axis
     * @param dY translation distance on the y-axis
     */
    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY)
    {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
        // TODO Auto-generated method stub

    }

    public void onEvent(FilteredData mFilteredData)
    {
       //V10Log.d(TAG, "Filtered Data Received");
        loadData(mFilteredData);
    }

    /**
     * adapter that supports 3 different item types
     */
    private class ChartDataAdapter extends ArrayAdapter<ChartItem>
    {
        public ChartDataAdapter(Context context, List<ChartItem> objects)
        {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            return getItem(position).getView(position, convertView,
                    getContext());
        }

        @Override
        public int getItemViewType(int position)
        {
            // return the views type
            return getItem(position).getItemType();
        }

        @Override
        public int getViewTypeCount()
        {
            return 3; // we have 3 different item-types
        }
    }


}
