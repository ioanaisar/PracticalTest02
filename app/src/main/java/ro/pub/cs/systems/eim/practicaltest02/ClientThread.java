package ro.pub.cs.systems.eim.practicaltest02;

import android.widget.ImageView;
import android.widget.TextView;

import java.net.Socket;

import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ClientThread extends Thread {

    private final String address;
    private final int port;
    private final String typeMoney;
    private final TextView resultText;

    private Socket socket;

    public ClientThread(String address, int port, String money, TextView resultText){
        this.address = address;
        this.port = port;
        this.typeMoney = money;
        this.resultText = resultText;


    }
    @Override
    public void run() {
        try {
            // tries to establish a socket connection to the server
            socket = new Socket(address, port);

            // gets the reader and writer for the socket
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);

            // sends the city and information type to the server
            printWriter.println(typeMoney);
            printWriter.flush();
            String finalInformation;


            // reads the weather information from the server
            while ((finalInformation = bufferedReader.readLine()) != null) {
                final String finalizedInfo = finalInformation;
                Log.e(Constants.TAG, "[CLIENT THREAD] a actualizat: " + finalizedInfo);
                // updates the UI with the weather information. This is done using postt() method to ensure it is executed on UI thread
                resultText.post(() -> resultText.setText(finalizedInfo));


            }
        } // if an exception occurs, it is logged
        catch (IOException ioException) {
            Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } finally {
            if (socket != null) {
                try {
                    // closes the socket regardless of errors or not
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }


}
