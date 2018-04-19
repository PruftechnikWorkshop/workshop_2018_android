package com.ptt.ptcaseweekapp;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import com.google.gson.Gson;




/**
 * Created by masekp on 4/18/18.
 */

public class SensorDataSender extends Thread {

    private static final int iSEND_INTERVAL_MS = 5000;

    private String m_strUrl = "";
    private boolean m_bRunning = true;

    private ArrayList<SensorDataItem> m_aDataToSend = new ArrayList<>();

    SensorDataSender( String a_strUrl )
    {
        m_strUrl = a_strUrl;
    }

    @Override
    public void run()
    {
        super.run();
        Log.d("sender", "SensorDataSender.run()" + m_strUrl );

        while ( m_bRunning )
        {
            Log.d("sender", "Loop");
            try
            {
                Thread.sleep( iSEND_INTERVAL_MS );
            }
            catch ( InterruptedException e )
            {
                Log.d("sender", "Interrupt ", e);
            }

            SendData();
        }
    }

    public void Finish()
    {
        m_bRunning = false;
    }

    private void SendData()
    {
        if(m_aDataToSend.isEmpty())
        {
            Log.d("sender", "SendData(): No data to send");
            return;
        }

        Gson gson = new Gson();
        String jsonData;

        synchronized (this)
        {
            jsonData = gson.toJson(m_aDataToSend);
            m_aDataToSend.clear();
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

    private void SendJsonToService(String jsonData) throws IOException
    {
        byte[] postData = jsonData.getBytes();
        int postDataLength = postData.length;
        URL url = new URL( m_strUrl );

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput( true );
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod( "POST" );
        conn.setRequestProperty( "Content-Type", "application/json" );
        conn.setRequestProperty( "charset", "utf-8" );
        conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ) );
        conn.setUseCaches( false );

        DataOutputStream writer = new DataOutputStream( conn.getOutputStream() );
        writer.write( postData );
        Log.i( "sender", postDataLength + " bytes of data sent." );

        Log.i( "sender", "Response: " + conn.getResponseCode() + " " + conn.getResponseMessage() );
    }

    public synchronized void AddDataToSend( SensorDataItem a_fData )
    {
        Log.d("sender", "Added " + a_fData.toString() + " to buffer");
        m_aDataToSend.add(a_fData);
    }
}
