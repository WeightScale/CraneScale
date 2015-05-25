/***************************************
 * 
 *
 *  
 ***************************************/

package com.kostya.cranescale;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * 
 **/
class BluetoothRfcommClient {

    // Unique UUID for this application
    private static final UUID sppUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    	//UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private static  int mState = 0;

    // Constants that indicate the current connection state
    private static final int STATE_NONE = 0;       // we're doing nothing
    //public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    private static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    private static final int STATE_CONNECTED = 3;  // now connected to a remote device
    /**
     * Constructor. Prepares a new BluetoothChat session.
     * - context - The UI Activity Context
     * - handler - A Handler to send messages back to the UI Activity
     */
    public BluetoothRfcommClient(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        //mState = STATE_NONE;
        Handler mHandler = handler;
    }

    /**
     * Set the current state o
     * */
    private synchronized void setState(int state) {
        mState = state;
        // Give the new state to the Handler so the UI Activity can update
        //mHandler.obtainMessage(KLineAdapterView.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the Rfcomm client service. 
     * */
    public synchronized void start() {
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;}
        
        setState(STATE_NONE);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * - device - The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {

        // Cancel any thread attempting to make a connection
        //int i = device.getBondState();
        //int t = mAdapter.getProfileConnectionState(1);
        //int s = mmSocket.
        //if (i == BluetootD.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * - socket - The BluetoothSocket on which the connection was made
     * - device - The BluetoothDevice that has been connected
     */
    synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;}

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.setPriority(Thread.MIN_PRIORITY);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        //Message msg = mHandler.obtainMessage(KLineAdapterView.MESSAGE_DEVICE_NAME);
        //Bundle bundle = new Bundle();
        //bundle.putString(KLineAdapterView.EXTRA_DEV_NAME, device.getName());
        //msg.setData(bundle);
        //mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;}
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;}
        setState(STATE_NONE);
    }
    //===========================================================================================================
    public void writeByte(byte out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED)
                return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.writeByte(out);
    }
    //===========================================================================================================
    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * - out - The bytes to write - ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED)
                return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
            r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        setState(STATE_NONE);
        // Send a failure message back to the Activity
        //Message msg = mHandler.obtainMessage(KLineAdapterView.MESSAGE_TOAST);
        //Bundle bundle = new Bundle();
        //bundle.putString(KLineAdapterView.TOAST, "соединение неустановлено");
        //msg.setData(bundle);
        //mHandler.sendMessage(msg);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        setState(STATE_NONE);
        // Send a failure message back to the Activity
        //Message msg = mHandler.obtainMessage(KLineAdapterView.MESSAGE_TOAST);
        //Bundle bundle = new Bundle();
        //bundle.putString(KLineAdapterView.TOAST, "соединение оборвалось");
        //msg.setData(bundle);
        //mHandler.sendMessage(msg);
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        public final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(sppUUID);
            } catch (IOException e) {
                //
            }
            mmSocket = tmp;
        }
        @Override
        public void run() {
            setName("ConnectThread");
            // Always cancel discovery because it will slow down a connection
             mAdapter.cancelDiscovery();
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a  successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                //connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    //
                }
                // Start the service over to restart listening mode
                //BluetoothRfcommClient.this.start();
                return;
            }
            // Reset the ConnectThread because we're done
            synchronized (BluetoothRfcommClient.this) {
                mConnectThread = null;
            }
            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                //
            }
        }
    }
    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        @Override
        public void run() {
            //byte [] buffer = new byte[1024];
            //int bytes;
            int bt;
            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    //if(KLineAdapterView.state_packet == KLineAdapterView.PACKET_BYTE){
                    synchronized (this){
                        bt = mmInStream.read();
                        //mHandler.obtainMessage(KLineAdapterView.MESSAGE_READ,bt,-1).sendToTarget();
                        //bt = mmInStream.read(buffer);
                        //char[] ch = (char[])buffer;
                        //mHandler.obtainMessage(KLineAdapterView.MESSAGE_READ,bt,-1,buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    //
                    connectionLost();
                    break;
                }
            }
        }

        private int UByte(byte b){
            if(b<0) // if negative
                return (b&0x7F) + 128;
            else
                return (int)b;
        }

        /**
         * Write to the connected OutStream.
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                // Share the sent message back to the UI Activity
                //mHandler.obtainMessage(KLineAdapterView.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (IOException e) {
                //
            }
        }
        //========================================================
        public void writeByte(byte b) {
            try {
                mmOutStream.write(b);
                // Share the sent message back to the UI Activity
                //mHandler.obtainMessage(KLineAdapterView.MESSAGE_WRITE, -1, -1, b).sendToTarget();
            } catch (IOException e) {
                //
            }
        }

        public void cancel() {
            try {mmSocket.close();} catch (IOException e) {
                //
            }
        }
    }
}
