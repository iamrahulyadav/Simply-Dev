package com.builder.ibalance.util;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.builder.ibalance.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

public class BarChartItem extends ChartItem {
	 ViewHolder holder = null;
	String heading="";
    public BarChartItem(ChartData<?> cd, Context c, String heading) {
        super(cd);
        this.heading = heading;
    }

    @Override
    public int getItemType() {
        return TYPE_BARCHART;
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
                    R.layout.list_item_barchart,null);
            holder.chart = (BarChart) convertView.findViewById(R.id.chart);
            holder.heading = (TextView) convertView.findViewById(R.id.heading);
            holder.heading.setTypeface(ttf);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // apply styling
        holder.heading.setText(heading);
        holder.chart.setNoDataTextDescription("No Data Available, Make Calls for Analytics");
        holder.chart.setDescription("");
        holder.chart.setDrawGridBackground(false);
        holder.chart.setDrawBarShadow(false);
        holder.chart.setPinchZoom(true);
        BarChartMarkerView mk = new BarChartMarkerView(c,R.layout.barchart_marker_view);

		holder.chart.setMarkerView(mk);
        Typeface tf =  Typeface.createFromAsset(c.getResources().getAssets(), "Roboto-Thin.ttf");
        XAxis xAxis = holder.chart.getXAxis();
        xAxis.setPosition(XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setTypeface(tf);
        YAxis leftAxis = holder.chart.getAxisLeft();
        leftAxis.setLabelCount(5,true);
        leftAxis.setSpaceTop(20f);
        leftAxis.setTypeface(tf);
        YAxis rightAxis = holder.chart.getAxisRight();
        rightAxis.setLabelCount(5,true);
        rightAxis.setSpaceTop(20f);
        rightAxis.setTypeface(tf);
        
        // set data
        holder.chart.setData((BarData) mChartData);
        
        // do not forget to refresh the chart
//        holder.chart.invalidate();
        holder.chart.animateY(700);
        return convertView;
    }
    
    private static class ViewHolder {
        BarChart chart;
        TextView heading;
    }
    public class BarChartMarkerView extends MarkerView {

        private TextView tvContent;

        public BarChartMarkerView (Context context, int layoutResource) {
            super(context, layoutResource);
            
            // this markerview only displays a textview
            tvContent = (TextView) findViewById(R.id.bar_popup);
        }

        /**
         * This method enables a specified custom MarkerView to update it's content everytime the MarkerView is redrawn.
         *
         * @param e         The Entry the MarkerView belongs to. This can also be any subclass of Entry, like BarEntry or
         *                  CandleEntry, simply cast it at runtime.
         * @param highlight the highlight object contains information about the highlighted value such as it's dataset-index, the
         */
        // callbacks everytime the MarkerView is redrawn, can be used to update the
        // content (user-interface)
        @Override
        public void refreshContent(Entry e, Highlight highlight)
        {
            tvContent.setText(e.getVal()+""); // set the entry-value as the display text
        }


        


        @Override
        public int getXOffset() {
            // this will center the marker-view horizontally
            return -(getWidth() / 2);
        }

        @Override
        public int getYOffset() {
            // this will cause the marker-view to be above the selected value
            return -getHeight();
        }


    }
}

