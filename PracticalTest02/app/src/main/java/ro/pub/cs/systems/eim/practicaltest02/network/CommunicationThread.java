package ro.pub.cs.systems.eim.practicaltest02.network;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;
import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.general.Utilities;
import ro.pub.cs.systems.eim.practicaltest02.model.StockInformation;

public class CommunicationThread extends Thread {

    private ServerThread serverThread;
    private Socket socket;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Buffered Reader / Print Writer are null!");
                return;
            }
            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client (stock");
            String stock = bufferedReader.readLine();
            if (stock == null || stock.isEmpty()) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client (stock");
                return;
            }

            HashMap<String, StockInformation> data = serverThread.getData();
            StockInformation stockInformation = null;
            boolean request = false;
//
//            if (data.containsKey(stock)) {
//                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
//            stockInformation = data.get(stock);
//
//                SimpleDateFormat sdf = new SimpleDateFormat();
//                Date d = sdf.parse(stockInformation.getTime(), new ParsePosition(0));
//
//                if(System.currentTimeMillis() - d.getTime() > 1000) {
//                    Log.i(Constants.TAG, "[COMMUNICATION THREAD] Data from cache is old...");
//                    request = true;
//                }
//                else {
//                    printWriter.println(stockInformation.toString());
//                    Log.i("cache", stockInformation.toString());
//                    printWriter.flush();
//                }
//            }

            if (data.containsKey(stock)) {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
                stockInformation = data.get(stock);


                printWriter.println(stockInformation.toString());
                Log.i("cache", stockInformation.toString());
                printWriter.flush();

            }else {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet  = new HttpGet(Constants.WEB_SERVICE_ADDRESS + stock + "&f=l1t1");


                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String pageSourceCode = httpClient.execute(httpGet, responseHandler);
                Log.d(Constants.TAG, pageSourceCode);

                if (pageSourceCode == null || pageSourceCode.isEmpty()) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                    return;
                }

                String[] tokens = pageSourceCode.split(",");
                String value = tokens[0];
                String time = tokens[1].replace("\"", "");

                stockInformation = new StockInformation(time, value);
                serverThread.setData(stock, stockInformation);
                printWriter.println(stockInformation.toString());
                printWriter.flush();
                Log.d(Constants.TAG, "[Server] Sent to client: " + stockInformation.toString());
            }
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } finally {
            if (socket != null) {
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

}
