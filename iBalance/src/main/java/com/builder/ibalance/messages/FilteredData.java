package com.builder.ibalance.messages;

import com.builder.ibalance.util.Tuple;

import java.util.ArrayList;

/**
 * Created by Shabaz on 13-Oct-15.
 */
public class FilteredData
{
    ArrayList<Tuple> mostCalled;
    ArrayList<Tuple> carrier;
    ArrayList<Tuple> circle;

    public FilteredData(ArrayList<Tuple> mostCalled, ArrayList<Tuple> carrier, ArrayList<Tuple> circle)
    {
        this.mostCalled = mostCalled;
        this.carrier = carrier;
        this.circle = circle;
    }

    public ArrayList<Tuple> getMostCalled()
    {
        return mostCalled;
    }

    public ArrayList<Tuple> getCarrierList()
    {
        return carrier;
    }

    public ArrayList<Tuple> getCircleList()
    {
        return circle;
    }
}
