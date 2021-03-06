package ro.pub.cs.systems.eim.practicaltest02.network;

import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.general.Utilities;

public class ClientThread extends Thread {

    private String address;
    private int port;
    private String stock;
    private TextView stockTextView;

    private Socket socket;

    public ClientThread(String address, int port, String stock, TextView stockTextView) {
        this.address = address;
        this.port = port;
        this.stock = stock;
        this.stockTextView = stockTextView;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(address, port);
            if (socket == null) {
                Log.e(Constants.TAG, "[CLIENT THREAD] Could not create socket!");
                return;
            }
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[CLIENT THREAD] Buffered Reader / Print Writer are null!");
                return;
            }
            Log.d(Constants.TAG, "[Client] Sent to server: " + stock);
            printWriter.println(stock);
            printWriter.flush();

            String stockInformation;
            while ((stockInformation = bufferedReader.readLine()) != null) {
                Log.d(Constants.TAG, "[Client] Received from server: " + stockInformation);
                final String finStockInformation = stockInformation;
                stockTextView.post(new Runnable() {
                   @Override
                    public void run() {
                       String text2 = stockTextView.getText().toString();
                       stockTextView.setText(text2 + " " + finStockInformation);
                   }
                });
            }
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } finally {
            if (socket != null) {
                try {
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
