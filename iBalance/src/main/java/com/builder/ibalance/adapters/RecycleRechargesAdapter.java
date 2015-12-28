package com.builder.ibalance.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.builder.ibalance.R;
import com.builder.ibalance.util.ConstantsAndStatics;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sunny on 12/7/15.
 */
public class RecycleRechargesAdapter extends RecyclerView.Adapter<RecycleRechargesAdapter.ViewHolder> {
    Cursor cursor;
    Context context;

    public RecycleRechargesAdapter(Context context, Cursor c, boolean autoRequery) {
        this.context= context;
        this.cursor = c;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_recharge, parent, false);

        ViewHolder mViewHolder = new ViewHolder(view);
        return mViewHolder;

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        cursor.moveToPosition(position);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d, MMM, yyyy");
        holder.rechargeDateTextView.setText("Date: "+sdf.format(new Date(cursor.getLong(1))));
        holder.rechargeAmountTextView.setText("RechargeAmount: "+ConstantsAndStatics.RUPEE_SYMBOL+" "+String.format("%.0f", cursor.getFloat(2)));
        holder.rechargeBalanceTextView.setText("Balance: "+ConstantsAndStatics.RUPEE_SYMBOL+" "+cursor.getString(3));

    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView  rechargeDateTextView,rechargeAmountTextView,rechargeBalanceTextView;
        public ViewHolder(View itemView) {
            super(itemView);
            rechargeDateTextView = (TextView) itemView.findViewById(R.id.rechargeDate);
            rechargeDateTextView.setTypeface(ConstantsAndStatics.ROBOTO_REGULAR);
            rechargeAmountTextView = (TextView) itemView.findViewById(R.id.rechargeAmount);
            rechargeAmountTextView.setTypeface(ConstantsAndStatics.ROBOTO_REGULAR);
            rechargeBalanceTextView = (TextView) itemView.findViewById(R.id.rechargeBalance);
            rechargeBalanceTextView.setTypeface(ConstantsAndStatics.ROBOTO_REGULAR);
        }
    }
}
