//Активность для стартовой настройки весов
package com.kostya.cranescale;

//import android.content.SharedPreferences;

import android.graphics.Point;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;
import com.konst.module.ScaleModule;
import com.konst.module.Versions;

//import android.preference.PreferenceManager;

public class ActivityTuning extends PreferenceActivity {
    private final Point point1 = new Point(Integer.MIN_VALUE, 0);
    private final Point point2 = new Point(Integer.MIN_VALUE, 0);
    private boolean flag_restore;
    //boolean flag_change = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.tuning);
        String KEY_POINT1 = "point1";
        Preference name = findPreference(KEY_POINT1);
        if (name != null) {
            name.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String str = ScaleModule.getModuleSensor();
                    if (str.isEmpty()) {
                        Toast.makeText(getApplicationContext(), R.string.preferences_no, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    //Scales.sensor=Integer.valueOf(str);
                    point1.x = ScaleModule.Version.sensorTenzo = Integer.valueOf(str);
                    point1.y = 0;
                    Toast.makeText(getApplicationContext(), R.string.preferences_yes, Toast.LENGTH_SHORT).show();
                    flag_restore = true;
                    return true;
                }
            });
        }
        if (name != null) {
            String KEY_POINT2 = "point2";
            name = findPreference(KEY_POINT2);
            name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    String str = ScaleModule.getModuleSensor();
                    if (str.isEmpty()) {
                        Toast.makeText(getApplicationContext(), R.string.preferences_no, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    point2.x = ScaleModule.Version.sensorTenzo = Integer.valueOf(str);
                    point2.y = Integer.valueOf(o.toString());
                    Toast.makeText(getApplicationContext(), R.string.preferences_yes, Toast.LENGTH_SHORT).show();
                    flag_restore = true;
                    return true;
                }
            });
        }
        if (name != null) {
            String KEY_WEIGHT_MAX = "weightMax";
            name = findPreference(KEY_WEIGHT_MAX);
            name.setTitle(getString(R.string.Max_weight) + ScaleModule.Version.weightMax + getString(R.string.scales_kg));
            name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    if (o.toString().isEmpty() || Integer.valueOf(o.toString()) < Main.default_max_weight) {
                        Toast.makeText(getApplicationContext(), R.string.preferences_no, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    ScaleModule.Version.weightMax = Integer.valueOf(o.toString());
                    ScaleModule.Version.weightMargin = (int) (ScaleModule.Version.weightMax * 1.2);
                    ScaleModule.weightError = (int) (ScaleModule.Version.weightMax * 0.001);
                    preference.setTitle(getString(R.string.Max_weight) + ScaleModule.Version.weightMax + getString(R.string.scales_kg));
                    Toast.makeText(getApplicationContext(), R.string.preferences_yes, Toast.LENGTH_SHORT).show();
                    flag_restore = true;
                    return true;
                }
            });
        }
        if (name != null) {
            String KEY_COEFFICIENT_A = "coefficientA";
            name = findPreference(KEY_COEFFICIENT_A);
            name.setTitle(getString(R.string.ConstantA) + Float.toString(ScaleModule.Version.coefficientA));
            name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    try {
                        ScaleModule.Version.coefficientA = Float.valueOf(o.toString());
                        preference.setTitle(getString(R.string.ConstantA) + Float.toString(ScaleModule.Version.coefficientA));
                        Toast.makeText(getApplicationContext(), R.string.preferences_yes, Toast.LENGTH_SHORT).show();
                        flag_restore = true;
                    }catch (Exception e){
                        Toast.makeText(getApplicationContext(), R.string.preferences_no, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    return true;
                }
            });
        }
        if (name != null) {
            String KEY_CALL_BATTERY = "call_battery";
            name = findPreference(KEY_CALL_BATTERY);
            name.setTitle(getString(R.string.Battery) + ScaleModule.battery + '%');
            name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    if (o.toString().isEmpty() || "0".equals(o.toString()) || Integer.valueOf(o.toString()) > Main.default_max_battery) {
                        Toast.makeText(getApplicationContext(), R.string.preferences_no, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    if (ScaleModule.setModuleCalibrateBattery(Integer.valueOf(o.toString()))) {
                        ScaleModule.battery = Integer.valueOf(o.toString());
                        preference.setTitle(getString(R.string.Battery) + ScaleModule.battery + '%');
                        Toast.makeText(getApplicationContext(), R.string.preferences_yes, Toast.LENGTH_SHORT).show();
                        return true;
                    }

                    Toast.makeText(getApplicationContext(), R.string.preferences_no, Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
        }
        /*if (name != null) {
            String KEY_SHEET = "sheet";
            name = findPreference(KEY_SHEET);
            name.setTitle(getString(R.string.Table) + '"' + Scales.spreadsheet + '"');
            name.setSummary(getString(R.string.TEXT_MESSAGE7));
            name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    if (o.toString().isEmpty()) {
                        Toast.makeText(getApplicationContext(), R.string.preferences_no, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    if (Scales.command(ScaleInterface.CMD_SPREADSHEET + o).equals(ScaleInterface.CMD_SPREADSHEET)) {
                        preference.setTitle(getString(R.string.Table) + '"' + o + '"');
                        Scales.spreadsheet = o.toString();
                        Toast.makeText(getApplicationContext(), R.string.preferences_yes, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    preference.setTitle(getString(R.string.Table) + "???");
                    Toast.makeText(getApplicationContext(), R.string.preferences_no, Toast.LENGTH_SHORT).show();

                    return false;
                }
            });
        }*/
        /*if (name != null) {
            String KEY_NAME = "name";
            name = findPreference(KEY_NAME);
            name.setSummary("Account Google: " + Scales.username);
            name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    if (o.toString().isEmpty()) {
                        Toast.makeText(getApplicationContext(), R.string.preferences_no, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    if (Scales.command(ScaleInterface.CMD_G_USER + o).equals(ScaleInterface.CMD_G_USER)) {
                        preference.setSummary("Account Google: " + o);
                        Scales.username = o.toString();
                        Toast.makeText(getApplicationContext(), R.string.preferences_yes, Toast.LENGTH_SHORT).show();
                        return true;
                    }

                    preference.setSummary("Account Google: ???");
                    Toast.makeText(getApplicationContext(), R.string.preferences_no, Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
        }*/
        /*if (name != null) {
            String KEY_PASSWORD = "password";
            name = findPreference(KEY_PASSWORD);
            name.setSummary("Password account Google - " + Scales.password);
            name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    if (o.toString().isEmpty()) {
                        Toast.makeText(getApplicationContext(), R.string.preferences_no, Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    if (Scales.command(ScaleInterface.CMD_G_PASS + o).equals(ScaleInterface.CMD_G_PASS)) {
                        preference.setSummary("Password account Google: " + o);
                        Scales.password = o.toString();
                        Toast.makeText(getApplicationContext(), R.string.preferences_yes, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    preference.setSummary("Password account Google: ???");
                    Toast.makeText(getApplicationContext(), R.string.preferences_no, Toast.LENGTH_SHORT).show();

                    return false;
                }
            });
        }*/
        /*if (name != null) {
            String KEY_PHONE = "phone_msg";
            name = findPreference(KEY_PHONE);
            name.setSummary("Phone for Boss - " + Scales.phone);
            name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    if (o.toString().isEmpty()) {
                        Toast.makeText(getApplicationContext(), R.string.preferences_no, Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    if (Scales.command(ScaleInterface.CMD_PHONE + o).equals(ScaleInterface.CMD_PHONE)) {
                        preference.setSummary("Phone for Boss: " + o);
                        Scales.phone = o.toString();
                        Toast.makeText(getApplicationContext(), R.string.preferences_yes, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    preference.setSummary("Phone for Boss: ???");
                    Toast.makeText(getApplicationContext(), R.string.preferences_no, Toast.LENGTH_SHORT).show();

                    return false;
                }
            });
        }*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (flag_restore) {
            if (point1.x != Integer.MIN_VALUE && point2.x != Integer.MIN_VALUE) {
                ScaleModule.Version.coefficientA = (float) (point1.y - point2.y) / (point1.x - point2.x);
                ScaleModule.Version.coefficientB = point1.y - ScaleModule.Version.coefficientA * point1.x;
            }
            ScaleModule.Version.limitTenzo = (int) (ScaleModule.Version.weightMax / ScaleModule.Version.coefficientA);
            if (ScaleModule.Version.limitTenzo > 0xffffff) {
                ScaleModule.Version.limitTenzo = 0xffffff;
                ScaleModule.Version.weightMax = (int) (0xffffff * ScaleModule.Version.coefficientA);
            }
            boolean b = ScaleModule.writeData();
            if (b) {
                Toast.makeText(getApplicationContext(), R.string.preferences_yes, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.preferences_no, Toast.LENGTH_SHORT).show();
            }
        }/*else
            b = Scales.vClass.writeDataScale();*/

    }
}
