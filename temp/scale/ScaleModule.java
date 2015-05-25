package com.konst.module.scale;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Kostya on 30.04.2015.
 */
public class ScaleModule {

    private BluetoothDevice device;  //чужое устройство
    private Handler handler;
    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothSocket socket;  //соединение
    private OutputStream os;         //поток отправки
    private InputStream is;          //поток получения
    public Versions ScaleVersion;    //Интерфейс для разных версий весов

    static final int REQUEST_CONNECT = 1;
    static final int REQUEST_SEARCH_SCALE = 2;
    public static final int STATUS_OK = 3;
    public static final int STATUS_CONNECT_ERROR = 4;
    public static final int STATUS_SCALE_ERROR = 5;
    public static final int STATUS_SCALE_UNKNOWN = 6;
    public static final int STATUS_TERMINAL_ERROR = 7;

    String CMD_VERSION          = "VRS";            //получить версию весов

    private final String version;
    protected int numVersion;
    //static String data;                         //данные весов

    public ScaleModule(String version, BluetoothDevice device, Handler handler){
        this.version = version;
        this.device = device;
        this.handler = handler;
    }

    public synchronized boolean connect() { //соединиться с весами
        disconnect();
        BluetoothSocket tmp = null;
        // Get a BluetoothSocket for a connection with the given BluetoothDevice
        try {
            tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
        } catch (IOException e) {
            //
        }
        socket = tmp;
        if(!bluetoothAdapter.isEnabled())
            return  false;
        bluetoothAdapter.cancelDiscovery();

        try {
            socket.connect();
            is=socket.getInputStream();
            os=socket.getOutputStream();
        } catch (IOException e) {
            disconnect();
            return false;
        }
        return true;
    }

    public void disconnect() { //рассоединиться
        try {
            if(socket!=null)
                socket.close();
            if(is !=null)
                is.close();
            if(os !=null)
                os.close();
        } catch(IOException ioe) {
            socket = null;
            //return;
        }
        is = null;
        os = null;
        socket = null;
    }

    protected synchronized String cmd(String cmd) { //послать команду и получить ответ
        String cmd_rtn=""; //возвращённая команда
        if(os != null)
            try {
                int t = is.available();
                if(t>0)
                    is.read(new byte[t]);
                os.write(cmd.getBytes());
                os.write((byte)0x0D);
                os.write((byte)0x0A);
                os.flush(); //что этот метод делает?
                for(int i=0;i<400 && cmd_rtn.length()<129;i++) {
                    Thread.sleep(1);
                    if(is.available()>0) {
                        i=0;
                        char ch=(char)is.read(); //временный символ (байт)
                        if(ch==65535) {
                            connect();
                            break;
                        }
                        if(ch=='\r')
                            continue;
                        if(ch=='\n')
                            if(cmd_rtn.startsWith(cmd))
                                return cmd_rtn.replace(cmd,"");
                            else
                                return "";
                        cmd_rtn+=ch;
                    }
                }
            } catch(IOException ioe) {
                connect();
            } catch(InterruptedException iex) {}
        else
            connect();
        return "";
    }

    public Versions attach() throws Exception{
        final Throwable initException = null;
        Thread t = new ConnectScaleThread(this, handler);
        t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                initException.initCause(ex);
            }
        });
        t.start();
        try {
            t.join();
            if(initException != null){
                throw new Exception(initException);
            }
        } catch (InterruptedException e) {
            throw new Exception(e);
        }
        return ScaleVersion;
    }

    public boolean isScales() { //Является ли весами и какой версии
        String vrs = cmd(CMD_VERSION); //Получаем версию весов
        if (vrs.startsWith(version)) {
            try {
                numVersion = Integer.valueOf(vrs.replace(version, ""));
                ScaleVersion = selectVersion(numVersion);
            }catch (Exception e){
                return false;
            }
            return true;
        }
        return false;
    }

    public String getBtDeviceName(){
        return device.getName();
    }

    public String getBtDeviceAddress(){
        return device.getAddress();
    }

    private Versions selectVersion(int version) throws Exception{
        switch (version){
            case 1:
                return new V1(this);
            case 4:
                return new V4(this);
            default:
                throw new Exception("illegal version");
        }
    }

}
