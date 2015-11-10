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
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.TimeZone;



public class RecentListAdapter extends CursorAdapter
{

    //private Context mContext;
    String ruppee_symbol = "Rs.";
    BalanceHelper mBalanceHelper;
    CircleTransform mCircleTransform = new CircleTransform();
    ContactDetailHelper mContactDetailHelper;
    public RecentListAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        ruppee_symbol = context.getResources().getString(R.string.rupee_symbol);
        //mContext = context;
    }

    @Override
    public void bindView(View view, Context arg1, Cursor cursor) {
        Typeface tf = Typeface.createFromAsset(mContext.getAssets(), "Roboto-Regular.ttf");
        ImageView contactPicture = (ImageView) view.findViewById(R.id.recents_picture);
        TextView name = (TextView) view.findViewById(R.id.recents_name);
        name.setTypeface(tf);
        TextView number = (TextView) view.findViewById(R.id.recents_number);
        number.setTypeface(tf);
        TextView days_ago = (TextView) view.findViewById(R.id.recents_date_time);
        days_ago.setTypeface(tf);
        TextView duration = (TextView) view.findViewById(R.id.recents_duration);
        duration.setTypeface(tf);
        TextView carrier_circle = (TextView) view.findViewById(R.id.recents_carrier_circle);
        carrier_circle.setTypeface(tf);
        TextView callType = (TextView) view.findViewById(R.id.recents_type);
        callType.setTypeface(tf);
        TextView slot = (TextView) view.findViewById(R.id.recent_sim);
        slot.setTypeface(tf);
        TextView call_cost = (TextView) view.findViewById(R.id.recents_cost);
        call_cost.setTypeface(tf);
        int id_idx =  cursor.getColumnIndex(IbalanceContract.CallLogEntry.COLUMN_NAME_ID),
                date_idx = cursor.getColumnIndex(IbalanceContract.CallLogEntry.COLUMN_NAME_DATE),
                type_idx = cursor.getColumnIndex(IbalanceContract.CallLogEntry.COLUMN_NAME_TYPE),
                slot_idx = cursor.getColumnIndex(IbalanceContract.CallLogEntry.COLUMN_NAME_SLOT),
                dur_idx = cursor.getColumnIndex(IbalanceContract.CallLogEntry.COLUMN_NAME_DURATION),
                num_idx = cursor.getColumnIndex(IbalanceContract.CallLogEntry.COLUMN_NAME_NUMBER);
        String phNumber = cursor.getString(num_idx);
        view.setTag(phNumber);
        if(mContactDetailHelper==null)
            mContactDetailHelper = new ContactDetailHelper();
        ContactDetailModel contactDetails = mContactDetailHelper.getDetailsforRecentList(phNumber);
        if (contactDetails.image_uri!=null) {
            //Log.d("PAI", image_uri);
            Picasso.with(arg1).load(contactDetails.image_uri).transform(mCircleTransform).into(contactPicture);
        }
        else
            Picasso.with(arg1).load(R.drawable.default_contact_picture).transform(mCircleTransform).into(contactPicture);
        name.setText(contactDetails.name);
        carrier_circle.setText(" | "+contactDetails.carrier+","+contactDetails.circle);
        days_ago.setText(getDaysAgo(cursor.getLong(date_idx)));
        int simslot = cursor.getInt(slot_idx)+1;
        slot.setText("Sim: "+simslot);
        number.setText(phNumber);
        duration.setText(Helper.getTotalDurationFormatted(cursor.getInt(dur_idx)));
        int type = cursor.getInt(type_idx);
        Long id = cursor.getLong(id_idx);
        switch (type)
        {
            case CallLog.Calls.OUTGOING_TYPE:
                callType.setText(" | Outgoing");
                call_cost.setVisibility(View.VISIBLE);
                if(mBalanceHelper==null)
                {
                    mBalanceHelper = new BalanceHelper();
                }
                Float fcost = mBalanceHelper.getCostForId(id);
                if(fcost == null)
                {
                    call_cost.setText(ruppee_symbol+" --.--");
                }
                else
                {
                    call_cost.setText(ruppee_symbol+ String.format("%.2f",fcost));
                }
                break;
            case CallLog.Calls.INCOMING_TYPE:
                callType.setText(" | Incoming");

                call_cost.setVisibility(View.GONE);
                break;
            case CallLog.Calls.MISSED_TYPE:
                callType.setText(" | Missed");

                call_cost.setVisibility(View.GONE);
                break;
            case CallLog.Calls.VOICEMAIL_TYPE:
                callType.setText(" | Voice Mail");
                call_cost.setVisibility(View.GONE);
                break;
            default: //this should not happen
                callType.setText(" | Unknown");
                call_cost.setVisibility(View.GONE);
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
