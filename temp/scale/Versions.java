package com.konst.module.scale;

/**
 * Created by Kostya on 29.03.2015.
 */
public abstract class Versions{
    ScaleModule scaleModule;

    public static int timeOff;                      //таймер выключения весов
    protected int sensor;                      //показание датчика веса
    public static float coefficientA;              //калибровочный коэффициент a
    public static float coefficientB;              //калибровочный коэффициент b
    public static int weightMax;                   //максимальный вес

    protected static int filterADC;                   //АЦП-фильтр (0-15)
    public int battery;                     //процент батареи (0-100%)
    public static int weight;                      //реальный вес

    protected int speed;                       //скорость передачи данных
    public int sensorTenzo;                 // показание датчика веса
    public static int weightMargin;                //предельный вес
    protected static int marginTenzo;      	       // предельное показани датчика
    protected static int offset;                      // offset
    protected float coefficientTemp;           // калибровочный коэффициент температуры
    protected static int sensorTenzoOffset;           // показание датчика веса минус offset
    public static int limitTenzo;                  // максимальное показание датчика

    final String CMD_FILTER           = "FAD";            //получить/установить АЦП-фильтр
    final String CMD_TIMER            = "TOF";            //получить/установить таймер выключения весов
    final String CMD_SPEED            = "BST";            //получить/установить скорость передачи данных
    final String CMD_GET_OFFSET       = "GCO";
    final String CMD_SET_OFFSET       = "SCO";            //установить offset
    final String CMD_SPREADSHEET      = "SGD";            //считать/записать имя таблици созданой в google disc
    final String CMD_G_USER           = "UGD";            //считать/записать account google disc
    final String CMD_G_PASS           = "PGD";            //считать/записать password google disc
    final String CMD_PHONE            = "PHN";            //считать/записать phone for sms boss
    final String CMD_DATA             = "DAT";            //считать/записать данные весов
    final String CMD_SENSOR           = "DCH";            //получить показание датчика веса
    final String CMD_BATTERY          = "GBT";            //получить передать заряд батареи
    final String CMD_DATA_TEMP        = "DTM";            //считать/записать данные температуры
    final String CMD_HARDWARE         = "HRW";            //получить версию hardware
    final String CMD_NAME             = "SNA";             //установить имя весов
    final String CMD_CALL_BATTERY     = "CBT";            //каллибровать процент батареи

    public static final int MAX_ADC_FILTER = 15;   //максимальное значение фильтра ацп
    protected final int DEFAULT_ADC_FILTER = 8;   //максимальное значение фильтра ацп
    protected final int MAX_TIME_OFF = 60;   //максимальное время бездействия весов в минутах
    protected final int MIN_TIME_OFF = 10;   //минимальное время бездействия весов в минутах

    public static String spreadsheet = "WeightCheck";
    public static String username = "weight.check.lg@gmail.com";
    public static String password = "htcehc26";
    public static String phone = "+380503285426";

    Versions(ScaleModule scaleModule){
        this.scaleModule = scaleModule;
    }

    //public abstract int updateWeight();
    public abstract boolean load() throws Exception;
    public abstract boolean setOffsetScale();
    public abstract boolean isLimit();
    public abstract boolean isMargin();
    public abstract int updateWeight();
    public abstract boolean setScaleNull();
    public abstract boolean writeData();


    protected String command(String command){
        return scaleModule.cmd(command);
    }
    public void dettach(){
        scaleModule.disconnect();
    }

    public String getFilterADC(){
        return command(CMD_FILTER);
    }
    public boolean setFilterADC(int filterADC){
        return command(CMD_FILTER + filterADC).equals(CMD_FILTER);
    }

    public String getTimeScaleOff(){
        return command(CMD_TIMER);
    }
    public boolean setTimeScaleOff(int timeOff){ return command(CMD_TIMER + timeOff).equals(CMD_TIMER); }

    protected String getSpeedModule(){
        return command(CMD_SPEED);
    }
    protected boolean setSpeedModule(int speed){
        return command(CMD_SPEED + speed).equals(CMD_SPEED);
    }

    protected String getOffsetSensor(){
        return command(CMD_GET_OFFSET);
    }
    protected boolean setOffsetSensor(){
        return command(CMD_SET_OFFSET).equals(CMD_SET_OFFSET);
    }

    public String getSensor(){ return command(CMD_SENSOR);  }

    public int getBatteryCharge(){
        try{
            battery = Integer.valueOf(command(CMD_BATTERY));
        }catch (Exception e){
            battery = -0;
        }
        return battery;
    }
    public int getTemperatureModule(){
        try {
            return (int) ((float) ((Integer.valueOf(command(CMD_DATA_TEMP)) - 0x800000) / 7169) / 0.81) - 273;
        }catch (Exception e){
            return -273;
        }
    }

    public String getName(){return scaleModule.getBtDeviceName();    }
    public String getAddress(){return scaleModule.getBtDeviceAddress();   }
    public int getVersionNum(){return scaleModule.numVersion;    }
    public String getHardware(){return command(CMD_HARDWARE); }

    public int getLimitTenzo(){return limitTenzo;}
    public int getMarginTenzo(){return marginTenzo;}

    public boolean setNameModule(String name){
        return command(CMD_NAME + name).equals(CMD_NAME);
    }

    public boolean doCalibrateBattery(int percent){ return command(CMD_CALL_BATTERY + percent).equals(CMD_CALL_BATTERY);  }

    /**
     * @throws Exception
     */
    public void loadFilterADC() throws Exception{
        try {
            filterADC = Integer.valueOf(getFilterADC());
            if(filterADC < 0 || filterADC > MAX_ADC_FILTER) {
                if(!setFilterADC(DEFAULT_ADC_FILTER))
                    throw new Exception("Фильтер АЦП не установлен в настройках");
                filterADC = DEFAULT_ADC_FILTER;
            }
        }catch (Exception e){
            throw new Exception("Фильтер АЦП не установлен в настройках");
        }
    }
    public void loadTimeOff() throws Exception{
        try{
            timeOff = Integer.valueOf(getTimeScaleOff());
            if (timeOff < MIN_TIME_OFF || timeOff > MAX_TIME_OFF) {
                if (!setTimeScaleOff(MIN_TIME_OFF))
                    throw new Exception("Таймер выключения не установлен в настройках");
                timeOff = MIN_TIME_OFF;
            }
        }catch (Exception e){
            throw new Exception("Таймер выключения не установлен в настройках");
        }
    }
    public void loadSpeedModule() throws Exception{
        try {
            speed = Integer.valueOf(getSpeedModule());
            if(speed < 1 || speed > 5) {
                if (!setSpeedModule(5))
                    throw new Exception("Скорость передачи не установлена в настройках");
                speed = 5;
            }
        }catch (Exception e){
            throw new Exception("Скорость передачи не установлена в настройках");
        }
    }

}
