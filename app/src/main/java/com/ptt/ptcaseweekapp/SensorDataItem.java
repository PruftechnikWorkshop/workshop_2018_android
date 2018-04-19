package com.ptt.ptcaseweekapp;

/**
 * Created by masekp on 4/18/18.
 */

public class SensorDataItem
{
    public long timeStamp = -1;
    public float signalValue = Float.NaN;

    public SensorDataItem( long a_lTimestamp, float a_fValue )
    {
        timeStamp = a_lTimestamp;
        signalValue = a_fValue;
    }
}
