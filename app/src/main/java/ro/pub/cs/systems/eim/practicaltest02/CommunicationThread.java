package ro.pub.cs.systems.eim.practicaltest02;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Time;
import java.time.Month;
import java.util.HashMap;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;


public class CommunicationThread extends Thread {

    private final ServerThread serverThread;
    private final Socket socket;

    // Constructor of the thread, which takes a ServerThread and a Socket as parameters
    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        // It first checks whether the socket is null, and if so, it logs an error and returns.
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            // Create BufferedReader and PrintWriter instances for reading from and writing to the socket
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client (city / information type!");

            // Read the city and informationType values sent by the client
            String money= bufferedReader.readLine();
           // String informationType = bufferedReader.readLine();
            if (money == null || money.isEmpty()) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client (city / information type!");
                return;
            }


            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(Constants.WEB_SERVICE_ADDRESS + money);
            System.out.println(httpGet.toString());
            HttpResponse httpGetResponse = null;
            try {
                httpGetResponse = httpClient.execute(httpGet);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            HttpEntity httpGetEntity = httpGetResponse.getEntity();
            System.out.println(httpGetEntity.toString());

            String pageSourceCode;

            if (httpGetEntity != null) {
                try {
                    pageSourceCode = EntityUtils.toString(httpGetEntity);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            } else {
                return;
            }
            System.out.println("pagesourcecode is " + pageSourceCode);

            JSONObject content1 = new JSONObject(pageSourceCode);

            System.out.println("Content is  " + content1.toString());

            JSONObject content2 = content1.getJSONObject("bpi");

            JSONObject content3 = content1.getJSONObject("time");
         //   JSONObject content4 = content3.getJSONObject("updated");
            String time = content3.getString("updated");



            JSONObject weatherArray;
            if(money.equals("EUR")) {
                weatherArray = content2.getJSONObject("EUR");
            }else{
                 weatherArray = content2.getJSONObject("USD");
            }

           // JSONObject main = content.getJSONObject(Constants.MAIN);
           // String temperature = main.getString(Constants.TEMP);
           // JSONObject money = weatherArray.get("rate");
            String rate = weatherArray.getString("rate");

            System.out.println("RATE "+ rate);

           MoneyInfo info = new MoneyInfo(money, rate, time);

            System.out.println("time is " + time);

        //   Time timeNow = Time.valueOf(time.split("T")[1].split("\\.")[0]);

          //  System.out.println("NOW "+ timeNow);
            // Cache the information for the given city

            // compare date

          //  int compare = msgPushedTimestamp.compareTo(logFileTimestamp);


            HashMap<String, MoneyInfo> data = serverThread.getData();
            MoneyInfo weatherForecastInformation;
            int ok =0;
            if (data.containsKey(money)) {
          //      Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
                weatherForecastInformation = data.get(money);

                // compar
                int compare = time.compareTo(weatherForecastInformation.getTime1());
                if(compare > 0){
                    System.out.println("time is " + time);
                    System.out.println("time is " + weatherForecastInformation.getTime1());
                    serverThread.setData(money, info);

                }else {
                    rate =  weatherForecastInformation.getValue();
                }

            } else {
                serverThread.setData(money, info);

            }

      //      serverThread.setData(money, info);


            // Send the result back to the client
            printWriter.println(rate);
            printWriter.flush();
        } catch (IOException | JSONException ioException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } finally {
            try {
                socket.close();
            } catch (IOException ioException) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                if (Constants.DEBUG) {
                    ioException.printStackTrace();
                }
            }
        }
    }

}