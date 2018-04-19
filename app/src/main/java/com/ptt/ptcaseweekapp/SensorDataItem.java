package com.ptt.ptcaseweekapp;

/**
 * Created by masekp on 4/18/18.
 */

public class SensorDataItem
{
    private long timeStamp = -1;
    private float signalValue = Float.NaN;

    SensorDataItem(long timeStamp, float signalValue)
    {
        this.timeStamp = timeStamp;
        this.signalValue = signalValue;
    }

    @Override
    public String toString()
    {
        return "SensorDataItem( " + timeStamp + ", " + signalValue + " )";
    }
}
