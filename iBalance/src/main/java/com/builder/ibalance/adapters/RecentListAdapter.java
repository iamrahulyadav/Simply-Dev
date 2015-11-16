package com.builder.ibalance.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.provider.CallLog;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.builder.ibalance.R;
import com.builder.ibalance.database.helpers.BalanceHelper;
import com.builder.ibalance.database.helpers.ContactDetailHelper;
import com.builder.ibalance.database.helpers.IbalanceContract;
import com.builder.ibalance.database.models.ContactDetailModel;
import com.builder.ibalance.util.CircleTransform;
import com.builder.ibalance.util.Helper;
import com.builder.ibalance.util.MyApplication;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;


public class RecentListAdapter extends CursorAdapter
{
    Map<String,ContactDetailModel> contactDetailMap = new TreeMap<String,ContactDetailModel>();
    ViewHolder mHolder;
    String ruppee_symbol = "Rs.";
    BalanceHelper mBalanceHelper;
    CircleTransform mCircleTransform = new CircleTransform();
    ContactDetailHelper mContactDetailHelper;
    float call_rate = MyApplication.context.getSharedPreferences("USER_DATA", Context.MODE_PRIVATE).getFloat("CALL_RATE", 1.7f);
    ContactDetailModel contactDetails;
    public RecentListAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        ruppee_symbol = context.getResources().getString(R.string.rupee_symbol);
        //mContext = context;
    }
    static class ViewHolder
    {
        public ImageView contactPicture;
        public TextView name,number,days_ago,duration,carrier_circle,callType,slot,call_cost;
        public ViewHolder(View view)
        {
            Typeface tf = Typeface.createFromAsset(MyApplication.context.getAssets(), "Roboto-Regular.ttf");
            contactPicture = (ImageView) view.findViewById(R.id.recents_picture);
            name = (TextView) view.findViewById(R.id.recents_name);
            name.setTypeface(tf);
            number = (TextView) view.findViewById(R.id.recents_number);
            number.setTypeface(tf);
            days_ago = (TextView) view.findViewById(R.id.recents_date_time);
            days_ago.setTypeface(tf);
            duration = (TextView) view.findViewById(R.id.recents_duration);
            duration.setTypeface(tf);
            carrier_circle = (TextView) view.findViewById(R.id.recents_carrier_circle);
            carrier_circle.setTypeface(tf);
            callType = (TextView) view.findViewById(R.id.recents_type);
            callType.setTypeface(tf);
            slot = (TextView) view.findViewById(R.id.recent_sim);
            slot.setTypeface(tf);
            call_cost = (TextView) view.findViewById(R.id.recent_cost);
            call_cost.setTypeface(tf);
        }
    }
    @Override
    public void bindView(View view, Context arg1, Cursor cursor) {
        mHolder = (ViewHolder) view.getTag(R.id.KEY_HOLDER);
        if(mHolder==null)
            mHolder= new ViewHolder(view);
        view.setTag(R.id.KEY_HOLDER,mHolder);
        int id_idx =  cursor.getColumnIndex(IbalanceContract.CallLogEntry.COLUMN_NAME_ID),
                date_idx = cursor.getColumnIndex(IbalanceContract.CallLogEntry.COLUMN_NAME_DATE),
                type_idx = cursor.getColumnIndex(IbalanceContract.CallLogEntry.COLUMN_NAME_TYPE),
                slot_idx = cursor.getColumnIndex(IbalanceContract.CallLogEntry.COLUMN_NAME_SLOT),
                dur_idx = cursor.getColumnIndex(IbalanceContract.CallLogEntry.COLUMN_NAME_DURATION),
                num_idx = cursor.getColumnIndex(IbalanceContract.CallLogEntry.COLUMN_NAME_NUMBER);
        String phNumber = cursor.getString(num_idx);
        view.setTag(R.id.KEY_NUMBER,phNumber);
        if(mContactDetailHelper==null)
            mContactDetailHelper = new ContactDetailHelper();
        contactDetails = contactDetailMap.get(phNumber);
        if(contactDetails == null)
        {
            contactDetails = mContactDetailHelper.getDetailsforRecentList(phNumber);
            contactDetailMap.put(phNumber,contactDetails);
        }
        if (contactDetails.image_uri!=null) {
            //Log.d("PAI", image_uri);
            Picasso.with(arg1).load(contactDetails.image_uri).transform(mCircleTransform).into(mHolder.contactPicture);
        }
        else
            Picasso.with(arg1).load(R.drawable.default_contact_picture).transform(mCircleTransform).into(mHolder.contactPicture);
        mHolder.name.setText(contactDetails.name);
        mHolder.carrier_circle.setText(contactDetails.carrier+","+contactDetails.circle);
        mHolder.days_ago.setText(getDaysAgo(cursor.getLong(date_idx)));
        int simslot = cursor.getInt(slot_idx)+1;
        mHolder.slot.setText("Sim: "+simslot);
        mHolder.number.setText(phNumber);
        int duration = cursor.getInt(dur_idx);
        mHolder.duration.setText(Helper.getTotalDurationFormatted(duration));
        int type = cursor.getInt(type_idx);
        Long id = cursor.getLong(id_idx);
        switch (type)
        {
            case CallLog.Calls.OUTGOING_TYPE:
                mHolder.callType.setText(" | Outgoing");
                if(mBalanceHelper==null)
                {
                    mBalanceHelper = new BalanceHelper();
                }
                Float fcost = mBalanceHelper.getCostForId(id);
                if(fcost == null)
                {
                    fcost = (call_rate*duration)/100;
                    mHolder.call_cost.setText(ruppee_symbol+ String.format("%.2f",fcost));
                }
                else
                {
                    mHolder.call_cost.setText(ruppee_symbol+ String.format("%.2f",fcost));
                }
                break;
           /* case CallLog.Calls.INCOMING_TYPE:
                mHolder.callType.setText(" | Incoming");

                mHolder.call_cost.setVisibility(View.GONE);
                break;
            case CallLog.Calls.MISSED_TYPE:
                mHolder.callType.setText(" | Missed");
                mHolder.duration.setText("00:00:00");
                mHolder.call_cost.setVisibility(View.GONE);
                break;
            case CallLog.Calls.VOICEMAIL_TYPE:
                mHolder.callType.setText(" | Voice Mail");
                mHolder.call_cost.setVisibility(View.GONE);
                break;
            default: //this should not happen
                mHolder.callType.setText(" | Unknown");
                mHolder.call_cost.setVisibility(View.GONE);*/
        }
     /*   Long tmsecs = cursor.getLong(date_idx);//time
        String t = (String)android.text.format.DateFormat.format("hh:mm a",tmsecs );
        time.setText(t);
        days_ago.setText(getDaysAgo(tmsecs));
        //Log.d("Deduc Adapter", "Setting call cost "+ cursor.getString(2));

        balance.setText("Bal: "+ruppee_symbol+" "+cursor.getString(bal_idx));
        slot.setText("Sim: "+(cursor.getInt(slot_idx)+1)+"");
        call_cost.setText("Call Cost: "+ruppee_symbol +" "+ cursor.getString(cost_idx));
        call_rate.setText("Call Rate: " + String.format("%.1f", cursor.getFloat(cost_idx)*100/(int)cursor.getInt(dur_idx))+" p/s");
        String phnumber = cursor.getString(num_idx);
        name.setText((new ContactDetailHelper()).getName(phnumber));
        number.setText(phnumber);*/
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
            //future self but why?
            //future future because date format uses UTC
            return (String)android.text.format.DateFormat.format("dd MMM yy",time - 19800l );
        }
    }

    @Override
    public View newView(Context arg0, Cursor cursor, ViewGroup container) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_recent, container, false);

        return view;
    }

}
