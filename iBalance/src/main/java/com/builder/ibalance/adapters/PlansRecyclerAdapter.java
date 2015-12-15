package com.builder.ibalance.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Spinner;
import android.widget.TextView;

import com.builder.ibalance.R;
import com.builder.ibalance.RechargePopup;
import com.builder.ibalance.util.Helper;
import com.builder.ibalance.util.MyApplication;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by sunny on 12/2/15.
 */
public class PlansRecyclerAdapter extends RecyclerView.Adapter<PlansRecyclerAdapter.ViewHolder> {

    TextView priceText,validityText,typeText,talktimeText, benefitsText;
    View container;
    private int lastPosition = -1;
    private LayoutInflater inflater;
    private List<ParseObject> plansList;
    Typeface tf;
    public PlansRecyclerAdapter(List<ParseObject> plansList) {
        inflater = (LayoutInflater) MyApplication.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        tf = Typeface.createFromAsset(MyApplication.context.getResources().getAssets(), "Roboto-Regular.ttf");
        this.plansList = plansList;

    }

@Override
public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.plans_list_item, viewGroup, false);
        ViewHolder mViewHolder = new ViewHolder(v);
        v.setOnClickListener(mViewHolder);
        return mViewHolder;

        }

@Override
public void onBindViewHolder(ViewHolder convertView, int i) {
    ParseObject mPlan = plansList.get(i);
    priceText.setText(mPlan.getInt("price") + "");
    typeText.setText(mPlan.getString("type"));
    String temp = mPlan.getString("validity");
    validityText.setText(temp);
    Double talktime = mPlan.getDouble("talktime");
    if(talktime==null)
        temp = "N/A";
    else
        temp = talktime+"";
    talktimeText.setText(temp);
    benefitsText.setText(mPlan.getString("benefits"));
}


@Override
public int getItemCount() {
        return plansList.size();
        }

public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    ViewHolder(View convertView) {
        super(convertView);
        container = convertView.findViewById(R.id.container);
        priceText = (TextView) convertView.findViewById(R.id.price_id);
        priceText.setTypeface(tf);
        typeText = (TextView) convertView.findViewById(R.id.types_id);
        typeText.setTypeface(tf);
        validityText = (TextView) convertView.findViewById(R.id.Validity_id);
        validityText.setTypeface(tf);

        talktimeText = (TextView) convertView.findViewById(R.id.Talktime_id);
        talktimeText.setTypeface(tf);
        benefitsText = (TextView) convertView.findViewById(R.id.benefits_id);
        benefitsText.setTypeface(tf);

    }

    @Override
    public void onClick(View v) {

        View parentView = (View) v.getParent().getParent();
        TextView numberField = (TextView) parentView.findViewById(R.id.numberField);
        Spinner rechargeCarrier = (Spinner)parentView.findViewById(R.id.recharge_carrier);
        Spinner rechargeCircle  = (Spinner)parentView.findViewById(R.id.recharge_circle);
                ((TextView) parentView.findViewById(R.id.amountField)).setText(((TextView) v.findViewById(R.id.price_id)).getText().toString());
        if(numberField.getText()==null || numberField.getText().toString().length()<10)
        {
            Helper.toastHelper("Mobile Number not valid");
            numberField.requestFocus();
            InputMethodManager imm = (InputMethodManager) MyApplication.context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(numberField, InputMethodManager.SHOW_IMPLICIT);
        }else
        {
            Intent rechargeIntent = new Intent(MyApplication.context, RechargePopup.class);
            rechargeIntent.putExtra("NUMBER", numberField.getText().toString());
            rechargeIntent.putExtra("CARRIER", rechargeCarrier.getSelectedItem().toString());
            rechargeIntent.putExtra("CIRCLE", rechargeCircle.getSelectedItem().toString());
            rechargeIntent.putExtra("AMOUNT", Integer.parseInt(((TextView) v.findViewById(R.id.price_id)).getText().toString()));
            v.getContext().startActivity(rechargeIntent);
        }

    }
}

}
