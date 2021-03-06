package com.kostya.cranescale;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import com.konst.module.ScaleModule;

/**
 * Created with IntelliJ IDEA.
 * User: Kostya
 * Date: 30.10.13
 * Time: 15:04
 * To change this template use File | Settings | File Templates.
 */
public class Main extends Application {
    public static Preferences preferencesScale;
    public static Preferences preferencesUpdate;

    public static final int microSoftware = 4;
    protected static String networkOperatorName;
    protected static String simNumber;
    protected static String telephoneNumber;
    protected static String networkCountry;

    protected static int stepMeasuring;                                // шаг измерения (округление)
    protected static int autoCapture;                                  //шаг захвата (округление)
    public static int timeDelayDetectCapture;                          //Время задержки для авто захвата после которого начинается захват в секундах

    public static  int day_closed;
    public static  int day_delete;

    protected static final int default_max_weight           = 1000; //вес максимальный по умолчанию килограммы
    protected static final int default_max_battery          = 100;  //максимальный заряд батареи проценты
    protected static final int default_max_time_off         = 60;   //максимальное время бездействия весов в минутах
    protected static final int default_min_time_off         = 10;   //минимальное время бездействия весов в минутах
    protected static final int default_max_time_auto_null   = 120;  //максимальное время срабатывания авто ноль секундах
    protected static final int default_limit_auto_null      = 50;   //предел ошибки при котором срабатывает авто ноль килограммы
    protected static final int default_max_step_scale       = 20;   //максимальный шаг измерения весов килограммы
    protected static final int default_max_auto_capture     = 100;  //максимальный значение авто захвата веса килограммы
    protected static final int default_delta_auto_capture   = 10;   //дельта значение авто захвата веса килограммы
    protected static final int default_min_auto_capture     = 20;   //минимальное значение авто захвата веса килограммы
    protected static final int default_day_close_check      = 10;   //максимальное количество дней для закрытия не закрытых чеков дней
    protected static final int default_day_delete_check     = 10;   //максимальное количество дней для удвления чеков дней
    protected static final int default_adc_filter           = 15;   //максимальное значение фильтра ацп

    protected static int versionNumber;
    public static String versionName = "";


    @Override
    public void onCreate() {
        super.onCreate();
        preferencesScale = new Preferences(getApplicationContext(), Preferences.PREFERENCES);
        preferencesUpdate = new Preferences(getApplicationContext(), Preferences.PREF_UPDATE);
        Preferences.load(getSharedPreferences(Preferences.PREFERENCES, Context.MODE_PRIVATE)); //загрузить настройки

        stepMeasuring           = preferencesScale.read(ActivityPreferences.KEY_STEP, default_max_step_scale);
        autoCapture             = preferencesScale.read(ActivityPreferences.KEY_AUTO_CAPTURE, default_max_auto_capture);
        day_delete              = preferencesScale.read(ActivityPreferences.KEY_DAY_CHECK_DELETE, default_day_delete_check);
        day_closed              = preferencesScale.read(ActivityPreferences.KEY_DAY_CLOSED_CHECK, default_day_close_check);
        ScaleModule.timerNull   = preferencesScale.read(ActivityPreferences.KEY_TIMER_NULL, default_max_time_auto_null);
        ScaleModule.weightError = preferencesScale.read(ActivityPreferences.KEY_MAX_NULL, default_limit_auto_null);
        timeDelayDetectCapture =  preferencesScale.read(ActivityPreferences.KEY_TIME_DELAY_DETECT_CAPTURE, 1);



        //getApplicationContext().startService(new Intent(getApplicationContext(), ServiceSmsCommand.class));// Запускаем сервис для приемеа смс команд
    }


}
