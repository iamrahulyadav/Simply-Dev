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
import com.builder.ibalance.database.helpers.ContactDetailHelper;

import java.util.Calendar;
import java.util.TimeZone;

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
		TextView balance = (TextView) view.findViewById(R.id.balance);
		call_rate.setTypeface(tf);
        TextView slot = (TextView) view.findViewById(R.id.slot);
		call_rate.setTypeface(tf);
		int date_idx = cursor.getColumnIndex("DATE"),
				slot_idx = cursor.getColumnIndex("SLOT"),
				cost_idx = cursor.getColumnIndex("COST"),
				dur_idx = cursor.getColumnIndex("DURATION"),
				num_idx = cursor.getColumnIndex("NUMBER"),
				bal_idx = cursor.getColumnIndex("BALANCE"),
				msg_idx =cursor.getColumnIndex("MESSAGE");
		Long tmsecs = cursor.getLong(date_idx);//time
		String t = (String)android.text.format.DateFormat.format("hh:mm a",tmsecs );
		time.setText(t);
		days_ago.setText(getDaysAgo(tmsecs));
		//Log.d("Deduc Adapter", "Setting call cost "+ cursor.getString(2));
		String ruppee_symbol = arg1.getResources().getString(R.string.rupee_symbol);
        balance.setText("Bal: "+ruppee_symbol+" "+cursor.getString(bal_idx));
        slot.setText("Sim: "+(cursor.getInt(slot_idx)+1)+"");
		call_cost.setText("Call Cost: "+ruppee_symbol +" "+ cursor.getString(cost_idx));
		call_rate.setText("Call Rate: " + String.format("%.1f", cursor.getFloat(cost_idx)*100/(int)cursor.getInt(dur_idx))+" p/s");
		String phnumber = cursor.getString(num_idx);
		name.setText((new ContactDetailHelper()).getName(phnumber));
		number.setText(phnumber);
	}
	private String getDaysAgo(Long time){
		Calendar date = Calendar.getInstance(TimeZone.getDefault());
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
	    else
			if(days == 1) return "Yesterday";
	    else
			if(days<=10)
			{
				return days + " days ago";
			}
		else

			{
				//Converting back to UTC
				return (String)android.text.format.DateFormat.format("dd-MMM-YY",time - 19800l );
			}
	}

	@Override
	public View newView(Context arg0, Cursor cursor, ViewGroup container) {
		View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_deduction, container, false);
		
		return view;
	}

}
