package com.ptt.ptcaseweekapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MainActivity extends AppCompatActivity implements SensorEventListener
{
    SensorDataSender m_dataSenderThread;
    private SensorManager m_sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView( R.layout.activity_main );

        final Button button = findViewById(R.id.sendButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                EditText urlEdit = findViewById(R.id.urlEdit);
                if(m_dataSenderThread == null)
                {
                    m_dataSenderThread = new SensorDataSender( urlEdit.getText().toString() );
                    m_dataSenderThread.start();
                    button.setText( "Stop sending" );
                    button.setBackgroundColor( getResources().getColor( android.R.color.holo_red_dark ) );
                    urlEdit.setEnabled( false );
                }
                else
                {
                    m_dataSenderThread.Finish();
                    try
                    {
                        m_dataSenderThread.join(10);
                    }
                    catch ( InterruptedException e )
                    {
                        Log.e("main", "Thread join exception: ", e);
                    }
                    m_dataSenderThread = null;
                    button.setText("Start sending");
                    button.setBackgroundColor( getResources().getColor( android.R.color.holo_green_dark ) );
                    urlEdit.setEnabled( true );
                }
            }
        });

        m_sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        float[] values = sensorEvent.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];

        float totalAcceleration = (float) Math.sqrt( x * x + y * y + z * z );
        Log.d( "sensor", "Acceleration: " + totalAcceleration );

        if(m_dataSenderThread != null)
        {
            SensorDataItem dataItem = new SensorDataItem( System.currentTimeMillis(), totalAcceleration );
            m_dataSenderThread.AddDataToSend( dataItem );
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i)
    {
        // Nothing
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        m_sensorManager.registerListener(this
                , m_sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
                , m_sensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        m_sensorManager.unregisterListener( this );
    }
}
