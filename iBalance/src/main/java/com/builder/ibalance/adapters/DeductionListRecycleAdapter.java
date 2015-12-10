package com.builder.ibalance.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.builder.ibalance.R;
import com.builder.ibalance.database.helpers.ContactDetailHelper;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by sunny on 12/7/15.
 */
public class DeductionListRecycleAdapter extends RecyclerView.Adapter<DeductionListRecycleAdapter.ViewHolder> {
    Cursor cursor;
    Context context;

    public DeductionListRecycleAdapter(Context context, Cursor c, boolean autoRequery) {
       // super(context, c, autoRequery);
        this.context= context;
        this.cursor = c;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_deduction, parent, false);

        ViewHolder mViewHolder = new ViewHolder(view);

        view.setOnClickListener(mViewHolder);
        return mViewHolder;

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        cursor.moveToPosition(position);

        int date_idx = cursor.getColumnIndex("DATE"),
                slot_idx = cursor.getColumnIndex("SLOT"),
                cost_idx = cursor.getColumnIndex("COST"),
                dur_idx = cursor.getColumnIndex("DURATION"),
                num_idx = cursor.getColumnIndex("NUMBER"),
                bal_idx = cursor.getColumnIndex("BALANCE"),
                msg_idx =cursor.getColumnIndex("MESSAGE");
        Long tmsecs = cursor.getLong(date_idx);//time
        String t = (String)android.text.format.DateFormat.format("hh:mm a",tmsecs );
        holder.time.setText(t);
        holder.days_ago.setText(getDaysAgo(tmsecs));
        String ruppee_symbol = context.getResources().getString(R.string.rupee_symbol);
        holder.balance.setText("Bal: "+ruppee_symbol+" "+cursor.getString(bal_idx));
        holder.slot.setText("Sim: "+(cursor.getInt(slot_idx)+1)+"");
        holder.call_cost.setText("Call Cost: "+ruppee_symbol +" "+ cursor.getString(cost_idx));
        holder.call_rate.setText("Call Rate: " + String.format("%.1f", cursor.getFloat(cost_idx)*100/(int)cursor.getInt(dur_idx))+" p/s");
        String phnumber = cursor.getString(num_idx);
        holder.name.setText((new ContactDetailHelper()).getName(phnumber));
        holder.number.setText(phnumber);
    }


    @Override
    public int getItemCount() {
        return cursor.getCount();

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView name,number,days_ago,time,call_cost,call_rate,balance,slot;
        public ViewHolder(View itemView) {

            super(itemView);
            Typeface tf = Typeface.createFromAsset(context.getAssets(), "Roboto-Regular.ttf");
            name = (TextView) itemView.findViewById(R.id.recents_name);
            name.setTypeface(tf);
            number = (TextView) itemView.findViewById(R.id.recents_number);
            number.setTypeface(tf);
            days_ago = (TextView) itemView.findViewById(R.id.days_ago);
            days_ago.setTypeface(tf);
            time = (TextView) itemView.findViewById(R.id.time);
            time.setTypeface(tf);
            call_cost = (TextView) itemView.findViewById(R.id.call_cost);
            call_cost.setTypeface(tf);
            call_rate = (TextView) itemView.findViewById(R.id.call_rate);
            call_rate.setTypeface(tf);
            balance = (TextView) itemView.findViewById(R.id.balance);
            call_rate.setTypeface(tf);
            slot = (TextView) itemView.findViewById(R.id.slot);
            call_rate.setTypeface(tf);

        }

        @Override
        public void onClick(View v) {
            return;
        }
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

}
