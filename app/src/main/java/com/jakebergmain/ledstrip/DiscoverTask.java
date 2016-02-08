package com.jakebergmain.ledstrip;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.preference.Preference;
import android.text.format.Formatter;
import android.util.Log;

import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

/**
 * Created by jake on 2/7/16.
 */
public class DiscoverTask extends AsyncTask<Void, Void, byte[]> {

    final String LOG_TAG = DiscoverTask.class.getSimpleName();

    final int PORT = 2390;
    final int RESPONSE_PORT = 55056;

    Context mContext = null;
    DiscoverCallback mCallback;
    
    ProgressDialog progressDialog;

    String packetContents = "0:0:0";

    /**
     * A task for discovering LED strips on the local network
     * @param mContext context
     * @param callback callback implementation so we can say if we found a device
     */
    public DiscoverTask(Context mContext, DiscoverCallback callback){
        this.mContext = mContext;
        this.mCallback = callback;
    }

    public interface DiscoverCallback {
        void onFoundDevice();
    }

    protected void onPreExecute(){
        // progress bar
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage("Searching for devices on local network");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    protected void onPostExecute(byte[] result){
        progressDialog.dismiss();

        if(result == null){
            // error
            // TODO
        } else {
            try {
                // set ip addr in SharedPreferences
                SharedPreferences preferences = mContext.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, 0);
                preferences.edit()
                        .putString(Constants.PREFERENCES_IP_ADDR, InetAddress.getByAddress(result).toString())
                        .apply();
                mCallback.onFoundDevice();

            } catch (Exception e){
                e.printStackTrace();
                // set ip addr to null in SharedPreferences
                SharedPreferences preferences = mContext.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, 0);
                preferences.edit()
                        .putString(Constants.PREFERENCES_IP_ADDR, null)
                        .apply();
            }
        }

    }

    protected byte[] doInBackground(Void... params){
        DatagramSocket socket = null;

        try {
            // open a socket
            socket = new DatagramSocket(RESPONSE_PORT);
            // get broadcast address to send packet to all devices on network
            InetAddress address = getBroadcastAddress();
            // packet contents
            byte[] bytes = packetContents.getBytes();
            // send a packet with above contents to all on local network to specified port
            Log.v(LOG_TAG, "sending packet to " + address.toString());
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, PORT);
            socket.send(packet);

            // listen for a response
            byte[] response = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(response, response.length);
            socket.setSoTimeout(1000);

            String text = "";
            int count = 0;
            // keep listening and sending packets until the LED strip responds
            while (!text.equals("acknowledged")) {
                try {
                    Log.v(LOG_TAG, "Listening for a response");
                    socket.receive(responsePacket);
                    text = new String(response, 0, responsePacket.getLength());
                    Log.v(LOG_TAG, "Received packet.  contents: " + text);
                } catch (SocketTimeoutException e) {
                    Log.w(LOG_TAG, "Socket timed out");
                    socket.send(packet);
                }
                count++;

                // nothing is responding so we throw and connection exception
                if(count > 30){
                    throw new ConnectException("Cannot find and connect to any LED strips.");
                }
            }

            // found a LED strip get the ip address of it and return it
            InetAddress ipAddr = responsePacket.getAddress();
            byte[] ipAddrByte = ipAddr.getAddress();
            return ipAddrByte;

        } catch (Exception e) {
            Log.e(LOG_TAG, "Error in DiscoverTask doInBackground()");
            e.printStackTrace();
        } finally {
            if(socket != null) {
                socket.close();
            }
        }

        return null;
    }



    /**
     * I have no clue how this works.  All I know is it return the Broadcast Address.
     * @return
     * @throws IOException
     */
    private InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    private String getIpAddress(){
        WifiManager wm = (WifiManager) mContext.getSystemService(mContext.WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }
}
