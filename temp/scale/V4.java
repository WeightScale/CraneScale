package com.konst.module.scale;

import com.kostya.cranescale.SimpleCommandLineParser;

import java.util.Iterator;

/**
 * Created by Kostya on 30.04.2015.
 */
public class V4 extends Versions {

    String CMD_SENSOR_OFFSET    = "DCO";            //получить показание датчика веса минус офсет

    final String CMD_DATA_CFA         = "cfa";               //коэфициэнт А
    final String CMD_DATA_CFB         = "cfb";               //коэфициэнт Б
    final String CMD_DATA_WGM         = "wgm";               //вес максимальный
    final String CMD_DATA_LMT         = "lmt";               //лимит тензодатчика

    V4(ScaleModule scaleModule) {
        super(scaleModule);
    }

    @Override
    public boolean load() throws Exception { //загрузить данные
        //======================================================================
        loadFilterADC();
        //======================================================================
        loadTimeOff();
        //======================================================================
        try {
            offset = Integer.valueOf(getOffsetSensor());
        }catch (Exception e){
            throw new Exception("Сделать обнуление в настройках");
        }
        //======================================================================
        try {
            spreadsheet = command(CMD_SPREADSHEET);
            username = command(CMD_G_USER);
            password = command(CMD_G_PASS);
            phone = command(CMD_PHONE);
        }catch (Exception e){
            return false;
        }
        //======================================================================
        if (!isDataValid(command(CMD_DATA))) {
            return false;
        }
        weightMargin = (int) (weightMax * 1.2);
        marginTenzo = (int) ((weightMax / coefficientA) * 1.2);

        return true;
    }

    @Override
    public synchronized int updateWeight() {
        try {
            sensorTenzoOffset = Integer.valueOf(command(CMD_SENSOR_OFFSET));
            return weight = (int) (coefficientA * sensorTenzoOffset);
        }catch (Exception e){
            return sensorTenzoOffset = weight = Integer.MIN_VALUE;
        }
    }

    @Override
    public boolean isLimit() {
        return Math.abs(sensorTenzoOffset + offset) > limitTenzo;
    }

    @Override
    public synchronized boolean setOffsetScale() { //обнуление
        return setOffsetSensor();
    }

    @Override
    public boolean writeData() {
        return command(CMD_DATA +
                CMD_DATA_CFA + '=' + coefficientA + ' ' +
                CMD_DATA_WGM + '=' + weightMax + ' ' +
                CMD_DATA_LMT + '=' + limitTenzo).equals(CMD_DATA);
    }

    @Override
    public boolean isMargin() {
        return Math.abs(sensorTenzoOffset + offset) > marginTenzo;
    }

    @Override
    public boolean setScaleNull() {
        return setOffsetScale();
    }

    public synchronized boolean isDataValid(String d) {
        String[] parts = d.split(" ", 0);
        SimpleCommandLineParser data = new SimpleCommandLineParser(parts, "=");
        Iterator<String> iteratorData = data.getKeyIterator();
        try {
            while (iteratorData.hasNext()) {
                switch (iteratorData.next()) {
                    case CMD_DATA_CFA:
                        coefficientA = Float.valueOf(data.getValue(CMD_DATA_CFA));//получаем коэфициент
                        if (coefficientA == 0)
                            return false;
                        break;
                    case CMD_DATA_CFB:
                        coefficientB = Float.valueOf(data.getValue(CMD_DATA_CFB));//получить offset
                        break;
                    case CMD_DATA_WGM:
                        weightMax = Integer.parseInt(data.getValue(CMD_DATA_WGM));//получаем макимальнай вес
                        if (weightMax <= 0)
                            return false;
                        break;
                    case CMD_DATA_LMT:
                        limitTenzo = Integer.parseInt(data.getValue(CMD_DATA_LMT));//получаем макимальнай показание перегруза
                        break;
                    default:
                }
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
            Main.preferencesUpdate.write(CMD_CALL_TEMP, String.valueOf(coefficientTemp));
            Main.preferencesUpdate.write(CMD_SPREADSHEET, spreadsheet);
            Main.preferencesUpdate.write(CMD_G_USER, username);
            Main.preferencesUpdate.write(CMD_G_PASS, password);
            Main.preferencesUpdate.write(CMD_DATA_CFA, String.valueOf(coefficientA));
            Main.preferencesUpdate.write(CMD_DATA_WGM, String.valueOf(weightMax));*//*

        //editor.apply();
        return true;
    }*/

    /*@Override
    public boolean restorePreferences() {
            *//*if (Scales.isScales()) {
                //Preferences.load(context.getSharedPreferences(Preferences.PREF_UPDATE, Context.MODE_PRIVATE));
                command(CMD_FILTER + Main.preferencesUpdate.read(CMD_FILTER, String.valueOf(Main.default_adc_filter)));
                command(CMD_TIMER + Main.preferencesUpdate.read(CMD_TIMER, String.valueOf(Main.default_max_time_off)));
                command(CMD_CALL_BATTERY + Main.preferencesUpdate.read(CMD_BATTERY, String.valueOf(Main.default_max_battery)));
                command(CMD_CALL_TEMP + Main.preferencesUpdate.read(CMD_CALL_TEMP, "0"));
                command(CMD_SPREADSHEET + Main.preferencesUpdate.read(CMD_SPREADSHEET, "weightscale"));
                command(CMD_G_USER + Main.preferencesUpdate.read(CMD_G_USER, "kreogen.lg@gmail.com"));
                command(CMD_G_PASS + Main.preferencesUpdate.read(CMD_G_PASS, "htcehc25"));

                coefficientA = Float.valueOf(Main.preferencesUpdate.read(CMD_DATA_CFA, "0"));
                weightMax = Integer.valueOf(Main.preferencesUpdate.read(CMD_DATA_WGM, String.valueOf(Main.default_max_weight)));
                limitTenzo = (int) (weightMax / coefficientA);
                writeDataScale();
            }*//*
        return true;
    }*/

}