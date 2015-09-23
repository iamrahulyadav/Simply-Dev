
package com.builder.ibalance.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.builder.ibalance.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.utils.PercentFormatter;

public class PieChartItem extends ChartItem {
	 ViewHolder holder = null;
	String heading="";
    public PieChartItem(ChartData<?> cd, Context c, String heading) {
        super(cd);
        this.heading = heading;

    }

    @Override
    public int getItemType() {
        return TYPE_PIECHART;
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
                    R.layout.list_item_piechart, null);
            holder.chart = (PieChart) convertView.findViewById(R.id.chart);
            holder.heading = (TextView) convertView.findViewById(R.id.heading);
            holder.heading.setTypeface(ttf);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // apply styling
        holder.heading.setText(heading);
        holder.chart.setDescription("");
        holder.chart.setHoleRadius(52f);
        holder.chart.setNoDataTextDescription("No Data Available, Make Calls for Analytics");
        holder.chart.setTransparentCircleRadius(57f);
        holder.chart.setCenterText("Most\nCalled");
        holder.chart.setCenterTextColor(Color.GRAY);
        holder.chart.setCenterTextSize(16f);
        holder.chart.setUsePercentValues(true);
        holder.chart.setRotationEnabled(false);
        holder.chart.setTouchEnabled(true);
        
      
        mChartData.setValueFormatter(new PercentFormatter());
        mChartData.setValueTextSize(11f);
        mChartData.setValueTextColor(Color.BLACK);
        // set data
        holder.chart.setData((PieData) mChartData);
        Typeface tf =  Typeface.createFromAsset(c.getResources().getAssets(), "Roboto-Thin.ttf");
          Legend l = holder.chart.getLegend();
          l.setEnabled(false);
//        l.setPosition(LegendPosition.RIGHT_OF_CHART);
//        l.setTypeface(tf);

        // do not forget to refresh the chart
        // holder.chart.invalidate();
        holder.chart.animateXY(900, 900);

        return convertView;
    }

    private static class ViewHolder {
        PieChart chart;
        TextView heading;
    }
}
