package com.builder.ibalance.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.builder.ibalance.R;
import com.builder.ibalance.datainitializers.DataInitializer;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DeductionsAdapter extends CursorAdapter{
	
	//private Context mContext;

	public DeductionsAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		//mContext = context;
	}

	@Override
	public void bindView(View view, Context arg1, Cursor cursor) {
		Typeface tf = Typeface.createFromAsset(mContext.getAssets(), "Roboto-Regular.ttf");
		TextView name = (TextView) view.findViewById(R.id.contact_name);
		name.setTypeface(tf);
		TextView number = (TextView) view.findViewById(R.id.contact_number);
		number.setTypeface(tf);
		TextView days_ago = (TextView) view.findViewById(R.id.days_ago);
		days_ago.setTypeface(tf);
		TextView time = (TextView) view.findViewById(R.id.time);
		time.setTypeface(tf);
		TextView call_cost = (TextView) view.findViewById(R.id.call_cost);
		call_cost.setTypeface(tf);
		TextView call_rate = (TextView) view.findViewById(R.id.call_rate);
		call_rate.setTypeface(tf);
		Long tmsecs = cursor.getLong(1);//time
		String t = (String)android.text.format.DateFormat.format("hh:mm a",tmsecs );
		time.setText(t);
		days_ago.setText(getDaysAgo(tmsecs));
		//Log.d("Deduc Adapter", "Setting call cost "+ cursor.getString(2));
		String ruppee_symbol = arg1.getResources().getString(R.string.rupee_symbol);
		int date_idx = cursor.getColumnIndex("DATE"),
				slot_idx = cursor.getColumnIndex("SLOT"),
				cost_idx = cursor.getColumnIndex("COST"),
				dur_idx = cursor.getColumnIndex("DURATION"),
				num_idx = cursor.getColumnIndex("NUMBER"),
				bal_idx = cursor.getColumnIndex("BALANCE"),
				msg_idx =cursor.getColumnIndex("MESSAGE");
		call_cost.setText("Call Cost: "+ruppee_symbol +" "+ cursor.getString(cost_idx));
		call_rate.setText("Call Rate: " + String.format("%.1f", cursor.getFloat(cost_idx)*100/(int)cursor.getInt(dur_idx))+" p/s");
		String phnumber = cursor.getString(num_idx);
		if(phnumber.startsWith("+91"))
			phnumber = phnumber.substring(3);
		Object[] data = DataInitializer.mainmap.get(phnumber);
		if(data == null)
		{
			//Log.d("TEST", "Data null number = "+ phnumber);
			name.setText("Unknown");
		}
		else
		name.setText(data[0].toString());
		number.setText(phnumber);
		/*tv4.setText("callDuration" + cursor.getString(4));
		tv5.setText("lastNumber" + ));*/
	}
	private String getDaysAgo(Long time){
		Calendar date = new GregorianCalendar();
		// reset hour, minutes, seconds and millis
		date.set(Calendar.HOUR_OF_DAY, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);

		// next day
		date.add(Calendar.DAY_OF_MONTH, 1);
		//Log.d("DATE", time+"");
		//time = getNextDate(time);
	    long days = (date.getTimeInMillis() - time) / 86400000;
	    //Log.d("DATE", "Diff days "+  days);

	    if(days == 0) return "Today";
	    else if(days == 1) return "Yesterday";
	    else return days + " days ago";
	}
	public  Long getNextDate(Long  curTime) {
		  Date date = new Date(curTime);
		  final Calendar calendar = Calendar.getInstance();
		// reset hour, minutes, seconds and millis
		  calendar.set(Calendar.HOUR_OF_DAY, 0);
		  calendar.set(Calendar.MINUTE, 0);
		  calendar.set(Calendar.SECOND, 0);
		  calendar.set(Calendar.MILLISECOND, 0);

		  calendar.setTime(date);
		  calendar.add(Calendar.DAY_OF_YEAR, 1);
		  return calendar.getTime().getTime(); 
		}
	@Override
	public View newView(Context arg0, Cursor cursor, ViewGroup container) {
		View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_deduction, container, false);
		
		return view;
	}

}
