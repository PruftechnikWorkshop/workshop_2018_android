package com.ptt.ptcaseweekapp;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements SensorEventListener
{
    SensorDataSender    m_dataSenderThread;
    SensorManager       m_sensorManager;

    long m_lastDataUpdateTimestamp = System.currentTimeMillis();
    private static final long DATA_UPDATE_INTERVAL_MS = 500;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        m_sensorManager = (SensorManager) getSystemService( SENSOR_SERVICE );

        setContentView( R.layout.activity_main );

        UpdateControlsState();
    }

    @Override
    public void onConfigurationChanged( Configuration newConfig )
    {
        super.onConfigurationChanged( newConfig );
        setContentView( R.layout.activity_main );

        UpdateControlsState();
    }

    public void OnSendButtonClicked( View v )
    {
        final Button button = findViewById( R.id.sendButton );
        if( m_dataSenderThread == null )
        {
            StartSenderThread();
        }
        else
        {
            StopSenderThread();
        }
    }

    private void StartSenderThread()
    {
        if ( m_dataSenderThread != null )
        {
            StopSenderThread();
        }

        final EditText urlEdit = findViewById( R.id.urlEdit );
        m_dataSenderThread = new SensorDataSender( urlEdit.getText().toString(), this );
        m_dataSenderThread.start();

        UpdateControlsState();
    }

    private void StopSenderThread()
    {
        m_dataSenderThread.Finish();
        try
        {
            m_dataSenderThread.join(10);
        }
        catch ( InterruptedException e )
        {
            Log.e("main", "Thread join exception: ", e );
        }

        m_dataSenderThread = null;

        UpdateControlsState();
    }

    @Override
    public void onSensorChanged( SensorEvent sensorEvent )
    {
        if ( System.currentTimeMillis() - m_lastDataUpdateTimestamp < DATA_UPDATE_INTERVAL_MS )
        {
            return;
        }

        m_lastDataUpdateTimestamp = System.currentTimeMillis();

        float[] values = sensorEvent.values;

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
    public void onAccuracyChanged( Sensor sensor, int i )
    {
        // Nothing
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        m_sensorManager.registerListener(this
                , m_sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
                , SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        m_sensorManager.unregisterListener( this );
    }

    void UpdateButtonState()
    {
        final Button button = findViewById( R.id.sendButton );

        if ( m_dataSenderThread == null )
        {
            button.setText( getResources().getString( R.string.button_start_sending ) );
            button.setBackgroundColor( getResources().getColor( android.R.color.holo_green_dark ) );
        }
        else
        {
            button.setText( getResources().getString( R.string.button_stop_sending ) );
            button.setBackgroundColor( getResources().getColor( android.R.color.holo_red_dark ) );
        }
    }

    void UpdateUrlEditState()
    {
        final EditText urlEdit = findViewById( R.id.urlEdit );
        if ( m_dataSenderThread == null )
        {
            urlEdit.setEnabled( true );
        }
        else
        {
            urlEdit.setEnabled( false );
        }
    }

    void UpdateControlsState()
    {
        UpdateUrlEditState();
        UpdateButtonState();
    }


    // This is here solely to clear focus on urlEdit when user taps outside
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }
}
