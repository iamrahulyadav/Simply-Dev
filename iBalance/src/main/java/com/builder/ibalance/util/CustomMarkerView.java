package com.builder.ibalance.util;

import android.content.Context;
import android.widget.TextView;

import com.builder.ibalance.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

public class CustomMarkerView extends MarkerView {

    private TextView tvContent;

    public CustomMarkerView (Context context, int layoutResource) {
        super(context, layoutResource);
        // this markerview only displays a textview
        tvContent = (TextView) findViewById(R.id.tvContent);
    }

    /**
     * This method enables a specified custom MarkerView to update it's content everytime the MarkerView is redrawn.
     *
     * @param e         The Entry the MarkerView belongs to. This can also be any subclass of Entry, like BarEntry or
     *                  CandleEntry, simply cast it at runtime.
     * @param highlight the highlight object contains information about the highlighted value such as it's dataset-index, the
     */
    @Override
    public void refreshContent(Entry e, Highlight highlight)
    {
        tvContent.setText("" +e.getVal()); // set the entry-value as the display text
    }

    /**
     * Use this to return the desired offset you wish the MarkerView to have on the x-axis. By returning -(getWidth() /
     * 2) you will center the MarkerView horizontally.
     *
     * @param xpos the position on the x-axis in pixels where the marker is drawn
     * @return
     */
    @Override
    public int getXOffset(float xpos)
    {
        // this will center the marker-view horizontally
        return -(getWidth() / 2);
    }

    /**
     * Use this to return the desired position offset you wish the MarkerView to have on the y-axis. By returning
     * -getHeight() you will cause the MarkerView to be above the selected value.
     *
     * @param ypos the position on the y-axis in pixels where the marker is drawn
     * @return
     */
    @Override
    public int getYOffset(float ypos)
    {
        // this will cause the marker-view to be above the selected value
        return -getHeight();
    }


}