package com.ptt.ptcaseweekapp;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
 * Created by masekp on 4/18/18.
 */

public class SensorDataSender extends Thread {

    private static final int SEND_INTERVAL_MS = 5000;

    private String m_url = "";
    private boolean m_isRunning = true;
    private Activity m_parentActivity = null;

    private ArrayList<SensorDataItem> m_dataToSend = new ArrayList<>();

    SensorDataSender( String url, Activity parentActivity )
    {
        m_url = url;
        m_parentActivity = parentActivity;
    }

    @Override
    public void run()
    {
        super.run();
        Log.d("sender", "Thread start. Url:" + m_url);

        while ( m_isRunning )
        {
            Log.d("sender", "Loop");
            try
            {
                Thread.sleep(SEND_INTERVAL_MS);
            }
            catch ( InterruptedException e )
            {
                Log.d("sender", "Interrupt ", e);
            }

            if( !m_isRunning)
            {
                break;
            }

            SendData();
        }

        Log.i( "sender", "Thread exit" );
    }

    void Finish()
    {
        m_isRunning = false;
    }

    private void SendData()
    {
        if( m_dataToSend.isEmpty() )
        {
            Log.d("sender", "SendData(): No data to send");
            return;
        }

        Gson gson = new Gson();
        String jsonData;

        synchronized (this)
        {
            jsonData = gson.toJson(m_dataToSend);
            m_dataToSend.clear();
        }

        try
        {
            Log.d("sender", "SendData(): Sending:" + jsonData );

            SendJsonToService(jsonData);
        }
        catch ( IOException e )
        {
           Log.e("sender", "Exception during sending: ", e);
        }
    }

    private void SendJsonToService( String jsonData ) throws IOException
    {
        byte[] postData = jsonData.getBytes();
        int postDataLength = postData.length;
        URL url = new URL(m_url);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput( true );
        conn.setInstanceFollowRedirects( false );
        conn.setRequestMethod( "POST" );
        conn.setRequestProperty( "Content-Type", "application/json" );
        conn.setRequestProperty( "charset", "utf-8" );
        conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ) );
        conn.setUseCaches( false );

        DataOutputStream writer = new DataOutputStream( conn.getOutputStream() );
        writer.write( postData );
        Log.i( "sender", postDataLength + " bytes of data sent." );
        Log.i( "sender", "Response: " + conn.getResponseCode() + " " + conn.getResponseMessage() );

        final String toastText =  m_parentActivity.getResources()
                .getString( R.string.sender_toast_text, postDataLength, conn.getResponseCode(), conn.getResponseMessage() );

        ShowToastInParentActivity( toastText );
    }

    private void ShowToastInParentActivity( final String toastText ) {
        m_parentActivity.runOnUiThread( new Runnable() {
            @Override
            public void run()
            {
                Toast.makeText( m_parentActivity
                        , toastText
                        , Toast.LENGTH_SHORT ).show();
            }
        });
    }

    synchronized void AddDataToSend(SensorDataItem data)
    {
        Log.d("sender", "Added " + data.toString() + " to the buffer");
        m_dataToSend.add(data);
    }
}
