package com.builder.ibalance.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.builder.ibalance.R;
import com.builder.ibalance.util.MyApplication;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by Shabaz on 02-Nov-15.
 */
public class PlansAdapter extends BaseAdapter
{

    TextView priceText,validityText,typeText,talktimeText, benefitsText;
    private LayoutInflater inflater;
    private List<ParseObject> plansList;
    Typeface tf;
    public PlansAdapter(List<ParseObject> plansList) {
        inflater = (LayoutInflater) MyApplication.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        tf = Typeface.createFromAsset(MyApplication.context.getResources().getAssets(), "Roboto-Regular.ttf");
        this.plansList = plansList;

    }

    @Override
    public int getCount() {
        return plansList.size();
    }

    @Override
    public Object getItem(int location) {
        return plansList.get(location);

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //TODO: use View holder pattern

        if (convertView == null)
            convertView = inflater.inflate(R.layout.plans_list_item, null);
        ParseObject mPlan = plansList.get(position);
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
        convertView.setTag(mPlan.getInt("price") + "");
        return convertView;
    }
}
