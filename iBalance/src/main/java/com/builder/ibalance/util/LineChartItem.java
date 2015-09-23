
package com.builder.ibalance.util;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.builder.ibalance.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.LineData;

public class LineChartItem extends ChartItem {
	 ViewHolder holder = null;
	String heading="";
    public LineChartItem(ChartData<?> cd, Context c, String heading) {
        super(cd);
        this.heading = heading;
       
    }

    @Override
    public int getItemType() {
        return TYPE_LINECHART;
    }
    
    public void clearChart()
    {
    	if(holder!=null)
    		if(holder.chart!=null)
    			holder.chart.clear();
    }

    @Override
    public View getView(int position, View convertView, Context c) {

        Typeface ttf =  Typeface.createFromAsset(c.getResources().getAssets(), "Roboto-Regular.ttf");
        if (convertView == null) {

            holder = new ViewHolder();

            convertView = LayoutInflater.from(c).inflate(
                    R.layout.list_item_linechart, null);
            holder.chart = (LineChart) convertView.findViewById(R.id.chart);
            holder.heading = (TextView) convertView.findViewById(R.id.heading);
            holder.heading.setTypeface(ttf);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // apply styling
        // holder.chart.setValueTypeface(mTf);
        holder.heading.setText(heading);
        holder.chart.setDescription("");
        holder.chart.setNoDataTextDescription("No Data Available, Make Calls for Analytics");
        holder.chart.setDrawGridBackground(false);
        holder.chart.setPinchZoom(true);
        Typeface tf =  Typeface.createFromAsset(c.getResources().getAssets(), "Roboto-Thin.ttf");
        XAxis xAxis = holder.chart.getXAxis();
        xAxis.setPosition(XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setTypeface(tf);

        YAxis leftAxis = holder.chart.getAxisLeft();
        leftAxis.setLabelCount(5);
        leftAxis.setTypeface(tf);
        YAxis rightAxis = holder.chart.getAxisRight();
        rightAxis.setLabelCount(5);
        rightAxis.setDrawGridLines(false);
        rightAxis.setTypeface(tf);
        // set data
        holder.chart.setData((LineData) mChartData);

        // do not forget to refresh the chart
        // holder.chart.invalidate();
        holder.chart.animateX(750);

        return convertView;
    }

    private static class ViewHolder {
        LineChart chart;
        TextView heading;
    }
}
