package com.ptt.ptcaseweekapp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
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

    SensorDataSender(String a_strUrl)
    {
        m_strUrl = a_strUrl;
    }

    @Override
    public void run()
    {
        super.run();
        Log.d("sender", "SensorDataSender.run()" + m_strUrl);

        while (m_bRunning)
        {
            Log.d("sender", "Loop");
            try
            {
                Thread.sleep(iSEND_INTERVAL_MS);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
                Log.d("sender", "Interrupt");
            }

            SendData();
        }
    }

    public void Finish()
    {
        m_bRunning = false;
    }

    private synchronized void SendData()
    {
        if(m_aDataToSend.isEmpty())
        {
            Log.d("sender", "SendData(): No data to send");
            return;
        }

        Gson gson = new Gson();

        Log.d("sender", "SendData(): Sending:" + gson.toJson(m_aDataToSend));


        String jsonData = gson.toJson(m_aDataToSend);
        m_aDataToSend.clear();

        try
        {
            SendJsonToService(jsonData);
        }
        catch (IOException e)
        {
           Log.e("sender", "Exception", e);
        }
    }

    private void SendJsonToService(String jsonData) throws IOException {
//        URL url = new URL(m_strUrl);
//        URLConnection conn = url.openConnection();
//
//        conn.setDoOutput(true);
//
//
//        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
//
//        writer.write(jsonData);
//        writer.flush();
////
////        String line;
////        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
////
////        while ((line = reader.readLine()) != null) {
////            System.out.println(line);
////        }
////        reader.close();
//        writer.close();


        byte[] postData       = jsonData.getBytes();
        int    postDataLength = postData.length;
        String request        = m_strUrl;
        URL    url            = new URL( request );
        HttpURLConnection conn= (HttpURLConnection) url.openConnection();
        conn.setDoOutput( true );
        conn.setInstanceFollowRedirects( false );
        conn.setRequestMethod( "POST" );
        conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty( "charset", "utf-8");
        conn.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
        conn.setUseCaches( false );

        DataOutputStream wr = new DataOutputStream( conn.getOutputStream());
        wr.write( postData );
    }

    public synchronized void AddData(SensorDataItem a_fData)
    {
        Log.d("sender", "Added " + a_fData + "to buffer");
        m_aDataToSend.add(a_fData);
    }
}
