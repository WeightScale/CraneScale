//Активность настроек
package com.kostya.cranescale;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;
import com.konst.module.InterfaceVersions;
import com.konst.module.ScaleModule;
import com.konst.module.Versions;

public class ActivityPreferences extends PreferenceActivity {

	static final String KEY_STEP = "step";
	private static final String KEY_NAME = "name";
	public static final String KEY_ADDRESS = "address";
	static final String KEY_DEVICES = "devices";
	private static final String KEY_NULL = "null";
	static final String KEY_AUTO_CAPTURE = "auto_capture";
	public static final String KEY_DAY_CLOSED_CHECK = "day_closed_check";
	public static final String KEY_DAY_CHECK_DELETE = "day_check_delete";
	private static final String KEY_FILTER = "filter";
	private static final String KEY_ABOUT = "about";
	private static final String KEY_TIMER = "timer";
	static final String KEY_LAST = "last";
	static final String KEY_TIMER_NULL = "timer_null";
	static final String KEY_MAX_NULL = "max_null";
	static final String KEY_UPDATE = "update";
	public static final String KEY_FLAG_UPDATE = "flag_update";
    public static final String KEY_TIME_DELAY_DETECT_CAPTURE = "key_time_delay_capture";


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		Preference name=findPreference("name");
		//name.setSummary(Scales.getName());
        name.setSummary(ScaleModule.getName());
		name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object o) {
				if (o.toString().equals("")){
					Toast.makeText(getApplicationContext(), R.string.preferences_no, Toast.LENGTH_SHORT).show();
                    return false;
                }else {
					//Scales.command(Scales.NAME + o.toString());
                    ScaleModule.setModuleName(o.toString());
					preference.setSummary(o.toString());
					Toast.makeText(getApplicationContext(), R.string.preferences_yes, Toast.LENGTH_SHORT).show();
                    return true;
				}
			}
		});
		name=findPreference("address");
		name.setSummary(ScaleModule.getAddress());

		name=findPreference("timer");
		name.setSummary("Выключение весов в режиме баздействия через "+String.valueOf(ScaleModule.Version.timeOff) +" мин");
		name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object o) {
				if(o.toString().equals("") || o.toString().equals("0") || Integer.valueOf(o.toString()) > 60){
                    Toast.makeText(getApplicationContext(), R.string.preferences_no, Toast.LENGTH_SHORT).show();
                    return false;
                }else {
                    if(ScaleModule.setModuleTimeOff(Integer.valueOf(o.toString()))){
                        ScaleModule.Version.timeOff = Integer.valueOf(o.toString());
                        preference.setSummary("Выключение весов в режиме баздействия через " + ScaleModule.Version.timeOff + " мин");
                        Toast.makeText(getApplicationContext(), R.string.preferences_yes, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    return false;
				}
			}
		});
		name=findPreference("nullw");
        if (name != null) {
            name.setSummary(getString(R.string.sum_zeroing));
            if (!ScaleModule.isAttach()) {
                name.setEnabled(false);
            }
            name.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (ScaleModule.setScaleNull()) {
                        Toast.makeText(getApplicationContext(), R.string.preferences_yes, Toast.LENGTH_SHORT).show();
                        return true;
                    }

                    Toast.makeText(getApplicationContext(), R.string.preferences_yes, Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
        }

		name=findPreference("step");
		name.setSummary("Шаг измерения весов "+String.valueOf(Main.preferencesScale.read(KEY_STEP, Main.default_max_step_scale))+" кг");
		name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object o) {
				if(o.toString().equals("") || o.toString().equals("0") || Integer.valueOf(o.toString())>20){
                    Toast.makeText(getApplicationContext(), R.string.preferences_no, Toast.LENGTH_SHORT).show();
                    return false;
                }else {
					preference.setSummary("Шаг измерения весов " + o.toString() + " кг");
					Preferences.write("step",Integer.valueOf(o.toString()));
                    Main.stepMeasuring = Integer.valueOf(o.toString());
					Toast.makeText(getApplicationContext(), R.string.preferences_yes, Toast.LENGTH_SHORT).show();
                    return true;
				}
			}
		});
        name=findPreference("filter");
        name.setSummary("Фильтер АЦП "+ ScaleModule.Version.filterADC +" чем больше число тем точнее, но медленей измерения.");
        name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                if(o.toString().equals("") || Integer.valueOf(o.toString()) > InterfaceVersions.MAX_ADC_FILTER)
                    Toast.makeText(getApplicationContext(), R.string.preferences_no, Toast.LENGTH_SHORT).show();
                else {
                    ScaleModule.setModuleFilterADC(Integer.valueOf(o.toString()));
                    preference.setSummary("Фильтер АЦП "+ o.toString() +" чем больше число тем точнее, но медленей измерения.");
                    Toast.makeText(getApplicationContext(), R.string.preferences_yes, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
		name = findPreference(KEY_ABOUT);
		if (name != null) {
			name.setSummary(getString(R.string.version) + Main.versionName + ' ' + Integer.toString(Main.versionNumber));
			name.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					startActivity(new Intent().setClass(getApplicationContext(), ActivityAbout.class));
					return false;
				}
			});
		}
	}
}
