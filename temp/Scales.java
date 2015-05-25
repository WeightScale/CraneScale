//Класс весов
package com.kostya.cranescale;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import com.kostya.cranescale.Versions.ScaleInterface;

class Scales {
	private static BluetoothDevice device;  //чужое устройство
    private static final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private static BluetoothSocket socket;  //соединение
	private static OutputStream os;         //поток отправки
	private static InputStream is;          //поток получения
	public static ScaleInterface vScale;    //Интерфейс для разных версий весов

	static String version;                  //версия весов
	static String data;                     //данные весов
	static float coefficientA;              //калибровочный коэффициент a
	static float coefficientB;              //калибровочный коэффициент b
	public static int sensorTenzo;          // показание датчика веса
	protected static float coefficientTemp; // калибровочный коэффициент температуры
	protected static int sensorTenzoOffset; // показание датчика веса минус offset
	protected static int offset;                                      // offset
	static int sensor;                      //показание датчика веса
	static int battery;                     //процент батареи (0-100%)
	static int filter;                      //АЦП-фильтр (0-15)
	static int timer;                       //таймер выключения весов
	static int speed;                       //скорость передачи данных
	static int step;                        //шаг измерения (округление)
    static int autoCapture;                 //шаг захвата (округление)
	static int weight;                      //реальный вес
	static int weightMax;                   //максимальный вес
	static int weightMargin;                //предельный вес
	static int weightError;                 //погрешность веса
	protected static int limitTenzo;        // максимальное показание датчика
	protected static int marginTenzo;      	// предельное показани датчика
	protected static int timerNull;         // время срабатывания авто нуля
	protected static int numStable;
	protected static boolean flagStable;

	static final int COUNT_STABLE = 16;                            //колличество раз стабильно был вес
	static final int DIVIDER_AUTO_NULL = 3;                         //делитель для авто ноль

    private static final int SIZE = 8;
    private static final int indexMask = SIZE-1;
    private static byte writeCount;
    private static int readCount;
    private static final int[] weightArray = new int[SIZE];

    private static int tempWeight;
    public static int num_stable;
    static  boolean flag_stable;

	public static final String VERSION="VRS";      //получить версию весов
	static final String NAME="SNA";         //установить имя весов
	static final String SENSOR="DCH";       //получить показание датчика веса
	private static final String BATTERY="GBT";      //получить процент батареи
	static final String FILTER="FAD";       //получить/установить АЦП-фильтр
	static final String TIMER="TOF";        //получить/установить таймер выключения весов
	static final String SPEED="BST";        //получить/установить скорость передачи данных
	public static final String DATA="DAT";         //считать/записать данные весов

	static boolean stable(int weight) {
		if (tempWeight - step <= weight && tempWeight + step >= weight) {
			if (++numStable >= COUNT_STABLE) {
				return true;
			}
		} else {
			numStable = 0;
		}
		tempWeight = weight;
		return false;
	}

    // запись в буфер, возвращает true если значение записано
    static boolean Write(int value){
        weightArray[writeCount++ & indexMask] = value;
        return true;
    }

    static int sumOf(int... integers){
        int total = 0;
        for (int i = 0; i < integers.length; total += integers[i++]);
        return total/SIZE;
    }
    // полон ли буфер
    static boolean IsFull()
    {
        return ((writeCount - readCount) & (indexMask)) != 0;
    }

	static synchronized boolean connect(BluetoothDevice bd) { //соединиться с весами
		disconnect();
		device=bd;
        BluetoothSocket tmp = null;
        // Get a BluetoothSocket for a connection with the given BluetoothDevice
        try {
            tmp = bd.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
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

	static void disconnect() { //рассоединиться
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

	 static synchronized String  command(String cmd) { //послать команду и получить ответ
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
                            connect(device);
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
                connect(device);
            } catch(InterruptedException iex) {}
        else
            connect(device);
		return "";
	}

	public static boolean isScales() { //Является ли весами и какой версии
		String vrs = command(ScaleInterface.CMD_VERSION); //Получаем версию весов
		if (vrs.startsWith(Main.versionName)) {
			version = vrs.replace(Main.versionName, "");
			try {
				vScale = new Versions().getVersion(Integer.valueOf(version));
			}catch (Exception e){
				return false;
			}
			return true;
		}
		return false;
	}

	static String getName() {
		return device.getName();
	}

	static String getAddress() {
		return device.getAddress();
	}

	static synchronized void nulling() { //обнуление
		coefficientB=-coefficientA*sensor;
		//writeData();
	}

	static synchronized int readWeight() {
		String str=command(SENSOR);
		if(str.equals(""))
			sensor=weight=Integer.MIN_VALUE;
		else {
			sensor=Integer.valueOf(str);
			weight=(int)(coefficientA*sensor+coefficientB);
            weight=weight/step*step;
		}
		return weight;
	}

	static int readBattery() {
		String str=command(BATTERY);
		if(str.equals(""))
			battery=Integer.MIN_VALUE;
		else
			battery=Integer.valueOf(str);
		return battery;
	}

	public static int getTemp() { //Получить температуру
		String str = command(Versions.ScaleInterface.CMD_DATA_TEMP);
		try {
			return (int) ((float) ((Integer.valueOf(command(ScaleInterface.CMD_DATA_TEMP)) - 0x800000) / 7169) / 0.81) - 273;
		}catch (Exception e){
			return -273;
		}
        /*if (str.isEmpty()) {
            return -273;
        }
        return (int) ((float) ((Integer.valueOf(str) - 0x800000) / 7169) / 0.81) - 273;*/
	}

	static void writeData() {
		command(DATA+"S"+coefficientA+" "+coefficientB+" "+weightMax);
	}

	private static synchronized boolean isDataValid(String d) {
		//data=d;
		StringBuilder dataBuffer=new StringBuilder(d);
		if(dataBuffer.toString().isEmpty())
			return false;
		dataBuffer.deleteCharAt(0);
		if(dataBuffer.indexOf(" ")==-1)
			return false;
		String str=dataBuffer.substring(0,dataBuffer.indexOf(" "));
		if(!isFloat(str))
			return false;
		coefficientA=Float.valueOf(str);
		dataBuffer.delete(0,dataBuffer.indexOf(" ")+1);
		if(dataBuffer.indexOf(" ")==-1)
			return false;
		str=dataBuffer.substring(0,dataBuffer.indexOf(" "));
		if(!isFloat(str))
			return false;
		coefficientB=Float.valueOf(str);
		dataBuffer.delete(0,dataBuffer.indexOf(" ")+1);
		if(!isInteger(dataBuffer.toString()))
			return false;
		weightMax=Integer.valueOf(dataBuffer.toString());
		if(weightMax<=0) {
			weightMax=1000;
			//writeData();
		}
		return true;
	}

	private static boolean isInteger(String str) {
		try {
			Integer.valueOf(str);
			return true;
		} catch(NumberFormatException e) {
			return false;
		}
	}

	private static boolean isFloat(String str) {
		try {
			Float.valueOf(str);
			return true;
		} catch(NumberFormatException e) {
			return false;
		}
	}

}
