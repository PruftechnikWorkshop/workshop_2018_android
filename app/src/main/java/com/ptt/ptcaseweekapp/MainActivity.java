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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button = findViewById(R.id.sendButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                if(m_dataSenderThread == null)
                {
                    EditText urlEdit = findViewById(R.id.urlEdit);
                    m_dataSenderThread = new SensorDataSender( urlEdit.getText().toString() );
                    m_dataSenderThread.start();
                    button.setText("Stop");

                    m_dataSenderThread.AddData(new SensorDataItem(1,0.1f));
                    m_dataSenderThread.AddData(new SensorDataItem(2,0.2f));
                    m_dataSenderThread.AddData(new SensorDataItem(3,0.3f));
                    m_dataSenderThread.AddData(new SensorDataItem(Long.MAX_VALUE, Float.MAX_VALUE));
                }
                else
                {
                    m_dataSenderThread.Finish();
                    try {
                        m_dataSenderThread.join(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.d("main", "join exception");
                    }
                    m_dataSenderThread = null;
                    button.setText("Start");
                }
            }
        });

    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        float[] values = sensorEvent.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];

        float totalAccel = (float) Math.sqrt( x*x + y*y + z*z );
        Log.d("sensor", "Acceleration: " + totalAccel);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i)
    {
        // Nothing
    }
}
