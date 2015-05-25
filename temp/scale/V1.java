package com.konst.module.scale;

/**
 * Created by Kostya on 30.04.2015.
 */
public class V1 extends Versions /*implements ScaleInterface*/{


    V1(ScaleModule scaleModule) {
        super(scaleModule);
    }

    @Override
    public boolean load() throws Exception{ //загрузить данные
        loadFilterADC();
        //==============================================================================================================
        loadTimeOff();
        //==============================================================================================================
        if(!isDataValid(command(CMD_DATA)))
            return false;
        weightMargin=(int)(weightMax * 1.2);
        return true;
    }
    @Override
    public synchronized int updateWeight() {
        try {
            sensorTenzo = Integer.valueOf(command(CMD_SENSOR));
            return weight = (int) (coefficientA * sensorTenzo + coefficientB);
        }catch (Exception e){
            return sensorTenzo = weight = Integer.MIN_VALUE;
        }
    }
    @Override
    public boolean isLimit() {
        return Math.abs(weight) > weightMax;
    }
    @Override
    public synchronized boolean setOffsetScale() { //обнуление
        try {
            coefficientB =- coefficientA * Integer.parseInt(command(CMD_SENSOR));
        }catch (Exception e){
            return false;
        }
        return true;
    }
    @Override
    public boolean writeData() {
        return command(CMD_DATA + 'S' + coefficientA + ' ' + coefficientB + ' ' + weightMax).equals(CMD_DATA);
    }
    @Override
    public boolean isMargin() {
        return Math.abs(weight) < weightMargin;
    }
    @Override
    public boolean setScaleNull() {
        String str = command(CMD_SENSOR);
        if (str.isEmpty()) {
            return false;
        }

        if (setOffsetScale()) {
            if (writeData()) {
                sensorTenzo = Integer.valueOf(str);
                return true;
            }
        }
        return false;
    }

    public synchronized boolean isDataValid(String d) {
        StringBuilder dataBuffer=new StringBuilder(d);
        try {
            dataBuffer.deleteCharAt(0);
            String str=dataBuffer.substring(0,dataBuffer.indexOf(" "));
            coefficientA=Float.valueOf(str);
            dataBuffer.delete(0,dataBuffer.indexOf(" ")+1);
            str=dataBuffer.substring(0,dataBuffer.indexOf(" "));
            coefficientB=Float.valueOf(str);
            dataBuffer.delete(0,dataBuffer.indexOf(" ")+1);
            weightMax=Integer.valueOf(dataBuffer.toString());
            if(weightMax<=0) {
                weightMax=1000;
                //writeData();
            }
        }catch (Exception e){
            return false;
        }
        return true;
    }

    /*@Override
    public boolean backupPreference() {
        //SharedPreferences.Editor editor = context.getSharedPreferences(Preferences.PREF_UPDATE, Context.MODE_PRIVATE).edit();

            *//*Main.preferencesUpdate.write(CMD_FILTER, String.valueOf(filter));
            Main.preferencesUpdate.write(CMD_TIMER, String.valueOf(timer));
            Main.preferencesUpdate.write(CMD_BATTERY, String.valueOf(battery));
            Main.preferencesUpdate.write(CMD_DATA_CFA, String.valueOf(coefficientA));
            Main.preferencesUpdate.write(CMD_DATA_CFB, String.valueOf(coefficientB));
            Main.preferencesUpdate.write(CMD_DATA_WGM, String.valueOf(weightMax));*//*

        //editor.apply();
        return true;
    }*/

}