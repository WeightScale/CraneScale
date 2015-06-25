//Создаёт интерфейс управления весами
package com.kostya.cranescale;

import android.annotation.TargetApi;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.*;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.*;
import android.widget.*;
import com.konst.module.Module;
import com.konst.module.ScaleModule;
import com.konst.module.ScaleModule.HandlerBatteryTemperature;
import com.kostya.cranescale.provider.WeightDocDbAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ActivityScales extends FragmentActivity implements View.OnClickListener, View.OnLongClickListener {

    private class ZeroThread extends Thread {
        private final ProgressDialog dialog;

        ZeroThread(Context context) {
            // Создаём новый поток
            super(getString(R.string.Zeroing));
            dialog = new ProgressDialog(context);
            dialog.setCancelable(false);
            dialog.setIndeterminate(false);
            dialog.show();
            dialog.setContentView(R.layout.custom_progress_dialog);
            TextView tv1 = (TextView) dialog.findViewById(R.id.textView1);
            tv1.setText(R.string.Zeroing);
            //start(); // Запускаем поток
        }

        @Override
        public void run() {
            ScaleModule.setOffsetScale();
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }
    private class AutoWeightThread extends Thread {
        private boolean start;
        private boolean cancelled;

        AutoWeightThread() {
            //closed = false;
        }

        @Override
        public synchronized void start() {
            super.start();
            start = true;
        }

        @Override
        public void run() {
            while (!cancelled) {

                weightViewIsSwipe = false;
                numStable = 0;

                while (!cancelled && !isCapture() && !weightViewIsSwipe) {                                              //ждём начала нагружения
                    try { Thread.sleep(50); } catch (InterruptedException ignored) { }
                }
                //sendBroadcast(new Intent(ACTION_START_WEIGHTING));
                isStable = false;
                while (!cancelled && !(isStable || weightViewIsSwipe)) {                                                //ждем стабилизации веса или нажатием выбора веса
                    try { Thread.sleep(50); } catch (InterruptedException ignored) { }
                    if (!touchWeightView) {                                                                             //если не прикасаемся к индикатору тогда стабилизируем вес
                        isStable = processStable(getWeightToStepMeasuring(moduleWeight));
                        handler.sendMessage(handler.obtainMessage(numStable));
                    }
                }
                vibrator.vibrate(100);
                numStable = COUNT_STABLE;
                if (cancelled) {
                    break;
                }
                /*if (Scales.flagStable)                                                                                   //сохраняем стабильный вес
                {
                    sendBroadcast(new Intent(ACTION_STORE_WEIGHTING).putExtra(EXTRA_WEIGHT_STABLE, Scales.weight));
                }*/
                weightViewIsSwipe = false;

                while (!cancelled && getWeightToStepMeasuring(moduleWeight) >= Main.default_min_auto_capture) {           // ждем разгрузки весов
                    try { Thread.sleep(50);} catch (InterruptedException ignored) {}
                }
                vibrator.vibrate(100);
                handler.sendMessage(handler.obtainMessage(0));
                /*if (!cancelled) {
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException ignored) {
                    }
                } else {
                    if (Scales.flagStable && weightType == WeightType.SECOND) {                                           //Если тара зафоксирована и выход через кнопку назад
                        weightType = WeightType.NETTO;
                    }
                    break;
                }

                if (weightType == WeightType.SECOND) {
                    cancelled = true;
                }*/

                //sendBroadcast(new Intent(ACTION_STOP_WEIGHTING));
            }
            start = false;
        }

        Handler handler =  new Handler(){
            @Override
            public void handleMessage(Message msg) {
                weightTextView.setSecondaryProgress(msg.what);
            }
        };

        private void cancel() {
            cancelled = true;
        }

        public boolean isStart() {
            return start;
        }
    }


    private AutoWeightThread autoWeightThread = new AutoWeightThread();
    private BluetoothAdapter bluetooth; //блютуз адаптер
    //private final ArrayList<BluetoothDevice> foundDevice = new ArrayList<>(); //чужие устройства
    private BroadcastReceiver broadcastReceiver; //приёмник намерений
    private Vibrator vibrator; //вибратор
	private AlertDialog.Builder dialog;
    private TabHost mTabHost;
    //private MyTabsAdapter mTabsAdapter;
    FragmentTransaction fragmentTransaction;
    //NewDocFragment newDocFragment = new NewDocFragment();
    //ListDocFragment listDocFragment = new ListDocFragment();

	private TextView textViewWeight; //вес
	private ProgressBar progressBarWeight; //вес
	private ProgressBar /*progressBarBattery,*/ progressBarBattery1; //текст батареи
    private ProgressBar progressBarStable; //Показывает стабилизацию
    private WeightTextView weightTextView;
    private BatteryProgressBar progressBarBattery; //текст батареи
    private TemperatureProgressBar temperatureProgressBar;
    private ImageView imageViewRemote;
    private WeightListAdapter arrayAdapterListWeight;
    private SimpleGestureFilter detectorWeightView;
    private LinearLayout linearBatteryTemp;              //лайаут для батарея температура
    public static final String PARAM_PENDING_INTENT = "pendingIntent";
    public static final String PARAM_DEVICE = "device";

    public static final int COUNT_STABLE = 64;                            //колличество раз стабильно был вес
    public static final int DIVIDER_AUTO_NULL = 3;                         //делитель для авто ноль

    static final int REQUEST_SEARCH_SCALE = 2;

    int moduleWeight;
    int moduleSensorValue;
    protected int tempWeight;
    public int numStable;

    private boolean doubleBackToExitPressedOnce;
    public static boolean isScaleConnect;
    private boolean touchWeightView;
    private boolean weightViewIsSwipe;
    protected boolean isStable;


    @Override
    public void onCreate(Bundle savedInstanceState) {
		//TODO лог всех событий внизу экрана
		super.onCreate(savedInstanceState);

        bluetooth = BluetoothAdapter.getDefaultAdapter();
        if (bluetooth == null) {
            Toast.makeText(getBaseContext(), R.string.bluetooth_no, Toast.LENGTH_LONG).show();
            finish();
        } else {
            //setContentView(R.layout.test);
            setupScale();
            setupSliding();
            //mTabHost = (TabHost) findViewById(android.R.id.tabhost);
            //mTabHost.setup();
            //ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
            //mTabsAdapter = new MyTabsAdapter(this, mTabHost, mViewPager);
            //mTabsAdapter.addTab(mTabHost.newTabSpec("input").setIndicator(createTabView(ActivityScales.this, "приход")), NewDocFragment.class);
            //if (savedInstanceState != null) {
                //mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
            //}

        }

    }

    /*@Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("tab", mTabHost.getCurrentTabTag()); //save the tab selected
        super.onSaveInstanceState(outState);
    }*/

    @Override
    protected void onStart() {
        super.onStart();
        if (!autoWeightThread.isStart()) {
            autoWeightThread.start();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonMenu:
                openOptionsMenu();
                break;
            case R.id.buttonBack:
                onBackPressed();
                break;
            /*case R.id.imageViewRemote:
                vibrator.vibrate(200);
                break;*/
            default:
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.imageViewRemote:
                vibrator.vibrate(100);
                openSearch();
                break;
            case R.id.buttonZero:
                vibrator.vibrate(100);
                new ZeroThread(this).start();
                break;
            default:
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            //exit();
            return;
        }
        bluetooth.cancelDiscovery();
        doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.press_again_to_exit /*Please click BACK again to exit*/, Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;

            }
        }, 2000);
    }

    @Override
    public void onDestroy() { //при разрушении активности
        super.onDestroy();
        exit();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_scales, menu);
        return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem  item) {
        switch (item.getItemId()){
            case R.id.preferences:
                startActivity(new Intent(this,ActivityPreferences.class));
                break;
            case R.id.search:
                openSearch();
                break;
            case R.id.exit:
                onBackPressed();
                break;
            default:
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setProgressBarIndeterminateVisibility(false);
        AlertDialog.Builder dialog;
        switch (resultCode) {
            case RESULT_OK:
                scaleModule.handleResultConnect(Module.ResultConnect.STATUS_LOAD_OK);
                break;
            case RESULT_CANCELED:
                scaleModule.handleConnectError(Module.ResultError.CONNECT_ERROR, "Connect error");
                break;
            default:
        }
    }

    final HandlerBatteryTemperature handlerBatteryTemperature = new HandlerBatteryTemperature() {
        @Override
        public int onEvent(final int battery, final int temperature) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBarBattery.updateProgress(battery);
                    temperatureProgressBar.updateProgress(temperature);
                }
            });
            return 5; //Обновляется через секунд
        }
    };

    final ScaleModule.HandlerWeight handlerWeight = new ScaleModule.HandlerWeight(){

        @Override
        public int onEvent(final ScaleModule.ResultWeight resultWeight, final int weight, final int sensor) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (resultWeight){
                        case WEIGHT_NORMAL:
                            moduleWeight = weight;
                            moduleSensorValue = sensor;
                            progressBarWeight.setProgress(sensor);
                            Rect bounds = progressBarWeight.getProgressDrawable().getBounds();
                            weightTextView.updateProgress(getWeightToStepMeasuring(weight), Color.BLACK, getResources().getDimension(R.dimen.text_big));
                            progressBarWeight.setProgressDrawable(getResources().getDrawable(R.drawable.progress_weight));
                            progressBarWeight.getProgressDrawable().setBounds(bounds);
                            break;
                        case WEIGHT_LIMIT:
                            moduleWeight = weight;
                            moduleSensorValue = sensor;
                            progressBarWeight.setProgress(sensor);
                            bounds = progressBarWeight.getProgressDrawable().getBounds();
                            weightTextView.updateProgress(getWeightToStepMeasuring(weight), Color.RED, getResources().getDimension(R.dimen.text_big));
                            progressBarWeight.setProgressDrawable(getResources().getDrawable(R.drawable.progress_weight_danger));
                            progressBarWeight.getProgressDrawable().setBounds(bounds);
                            break;
                        case WEIGHT_MARGIN:
                            moduleWeight = weight;
                            moduleSensorValue = sensor;
                            progressBarWeight.setProgress(sensor);
                            weightTextView.updateProgress(getString(R.string.OVER_LOAD), Color.RED, getResources().getDimension(R.dimen.text_large_xx));
                            vibrator.vibrate(100);
                            break;
                        case WEIGHT_ERROR:
                            weightTextView.updateProgress(getString(R.string.NO_CONNECT), Color.BLACK, getResources().getDimension(R.dimen.text_large_x));
                            progressBarWeight.setProgress(0);
                            break;
                        default:
                    }

                }
            });
            return 1; // Обновляем через милисикунды
        }

    };

    public final ScaleModule scaleModule = new ScaleModule() {

        @Override
        public void handleResultConnect(ResultConnect resultConnect) {
            switch (resultConnect){
                case STATUS_LOAD_OK:
                    try {
                        setTitle(getString(R.string.app_name) + " \"" + ScaleModule.getNameBluetoothDevice() + "\", v." + ScaleModule.getNumVersion()); //установить заголовок
                    } catch (Exception e) {
                        setTitle(getString(R.string.app_name) + " , v." + ScaleModule.getNumVersion()); //установить заголовок
                    }
                    Main.preferencesScale.write(ActivityPreferences.KEY_LAST, ScaleModule.getAddressBluetoothDevice());
                    progressBarWeight.setMax(ScaleModule.getMarginTenzo());
                    progressBarWeight.setSecondaryProgress(ScaleModule.getLimitTenzo());
                    handlerBatteryTemperature.process(true);
                    handlerWeight.process(true);
                    break;
                case STATUS_SCALE_UNKNOWN:
                    dialog = new AlertDialog.Builder(ActivityScales.this);
                    dialog.setTitle("Ошибка в настройках");
                    dialog.setCancelable(false);
                    dialog.setNegativeButton(getString(R.string.Close), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            onBackPressed();
                        }
                    });
                    dialog.setMessage("Запросите настройки у администратора. Настройки должен выполнять опытный пользователь");
                    Toast.makeText(getBaseContext(),R.string.preferences_error, Toast.LENGTH_SHORT).show();
                    setTitle(getString(R.string.app_name)+": админ настройки неправельные");
                    dialog.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(ActivityScales.this, ActivityTuning.class));
                            dialogInterface.dismiss();
                        }
                    });
                    dialog.show();
                    break;
                default:
            }
        }

        @Override
        public void handleConnectError(ResultError resultError, String error) {
            switch (resultError){
                case TERMINAL_ERROR:
                    dialog = new AlertDialog.Builder(ActivityScales.this);
                    dialog.setTitle(getString(R.string.preferences_error));
                    dialog.setCancelable(false);
                    dialog.setNegativeButton(getString(R.string.Close), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            doubleBackToExitPressedOnce = true;
                            onBackPressed();
                        }
                    });
                    dialog.setMessage(error);
                    Toast.makeText(getBaseContext(), R.string.preferences_error, Toast.LENGTH_SHORT).show();
                    setTitle(getString(R.string.app_name) + ": " + getString(R.string.preferences_error));
                    dialog.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intentPreferences = new Intent("com.kostya.cranescale.PREFERENCES_ACTIVITY");
                            //intentPreferences.putExtra("scale", scale);
                            startActivity(intentPreferences);
                            dialogInterface.dismiss();
                        }
                    });
                    dialog.show();
                case CONNECT_ERROR:
                    setTitle(getString(R.string.app_name) + getString(R.string.NO_CONNECT)); //установить заголовок
                    imageViewRemote.setImageDrawable(getResources().getDrawable(R.drawable.rss_off));
                    break;
                default:
            }
        }
    };

    private static View createTabView(final Context context, final CharSequence text) {
        View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
        TextView tv = (TextView) view.findViewById(R.id.tabsText);
        tv.setText(text);
        return view;
    }

    void setupScale() {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.scales);


        linearBatteryTemp = (LinearLayout) findViewById(R.id.linearSectionScale);
        linearBatteryTemp.setVisibility(View.INVISIBLE);
        //LinearLayout scaleSection = (LinearLayout) findViewById(R.id.scaleSection);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = 1.0f;
        getWindow().setAttributes(layoutParams);

        Settings.System.putInt(getContentResolver(), Settings.System.AUTO_TIME, 1);       //Включаем автообновления дата время
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            Settings.System.putInt(getContentResolver(), Settings.System.AUTO_TIME_ZONE, 1);  //Включаем автообновления дата время

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo != null) {
            if (networkInfo.isAvailable()) //Если используется
                new Internet(this).turnOnWiFiConnection(false); // для телефонов у которых один модуль wifi и bluetooth
        }

        try {
            PackageManager packageManager = getPackageManager();
            if (packageManager != null) {
                PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
                Main.versionNumber = packageInfo.versionCode;
                Main.versionName = packageInfo.versionName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            //new ErrorDBAdapter(this).insertNewEntry("100", e.getMessage());
        }

        broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) { //обработчик Bluetooth
                String action = intent.getAction();
                if (action != null) {
                    switch (action) {
                        case BluetoothAdapter.ACTION_STATE_CHANGED:
                            switch (bluetooth.getState()) {
                                case BluetoothAdapter.STATE_OFF:
                                    Toast.makeText(getBaseContext(), R.string.bluetooth_off, Toast.LENGTH_SHORT).show();
                                    new Internet(getApplicationContext()).turnOnWiFiConnection(false);
                                    bluetooth.enable();
                                    break;
                                case BluetoothAdapter.STATE_TURNING_ON:
                                    Toast.makeText(getBaseContext(), R.string.bluetooth_turning_on, Toast.LENGTH_SHORT).show();
                                    break;
                                case BluetoothAdapter.STATE_ON:
                                    Toast.makeText(getBaseContext(), R.string.bluetooth_on, Toast.LENGTH_SHORT).show();
                                    break;
                                default:
                                    break;
                            }
                            break;
                        case BluetoothDevice.ACTION_ACL_DISCONNECTED://устройство отсоеденено
                            vibrator.vibrate(200);
                            linearBatteryTemp.setVisibility(View.INVISIBLE);
                            imageViewRemote.setImageDrawable(getResources().getDrawable(R.drawable.rss_off));
                            //imageNewCheck.setEnabled(false);
                            isScaleConnect = false;
                            break;
                        case BluetoothDevice.ACTION_ACL_CONNECTED://найдено соеденено
                            vibrator.vibrate(200);
                            linearBatteryTemp.setVisibility(View.VISIBLE);
                            imageViewRemote.setImageDrawable(getResources().getDrawable(R.drawable.rss_on));
                            //imageNewCheck.setEnabled(true);
                            isScaleConnect = true;
                            break;
                        default:
                    }
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(broadcastReceiver, intentFilter);

        if (bluetooth != null) {
            if (bluetooth.isEnabled()) {
                Toast.makeText(getBaseContext(), R.string.bluetooth_on, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getBaseContext(), R.string.bluetooth_off, Toast.LENGTH_SHORT).show();
                bluetooth.enable();
            }
        }

        assert bluetooth != null;
        while (!bluetooth.isEnabled()) ;//ждем включения bluetooth

        //textView3=(TextView)findViewById(R.id.textView3);
        imageViewRemote = (ImageView) findViewById(R.id.imageViewRemote);
        //imageViewRemote.setOnClickListener(this);
        imageViewRemote.setOnLongClickListener(this);

        //imageNewCheck = (ImageView) findViewById(R.id.imageNewCheck);
        //imageNewCheck.setOnLongClickListener(this);

        //findViewById(R.id.imageButtonUp).setOnLongClickListener(this);
        findViewById(R.id.buttonMenu).setOnClickListener(this);
        findViewById(R.id.buttonBack).setOnClickListener(this);
        findViewById(R.id.buttonZero).setOnLongClickListener(this);

        setupWeightView();

        progressBarWeight = (ProgressBar) findViewById(R.id.progressBarWeight);


        progressBarBattery = new BatteryProgressBar(this);
        progressBarBattery = (BatteryProgressBar) findViewById(R.id.progressBarBattery);
        temperatureProgressBar = (TemperatureProgressBar) findViewById(R.id.progressBarTemperature);

        progressBarBattery.updateProgress(0);
        temperatureProgressBar.updateProgress(0);

        connectBluetooth();
        //listCheckSetup();
    }

    void setupSliding(){
        final ImageView ibHandle = (ImageView) findViewById(R.id.handle);
        final SlidingDrawer slidingDrawer = (SlidingDrawer) findViewById(R.id.drawer);
        slidingDrawer.open();
        slidingDrawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {
            public void onDrawerOpened() {
                //((FrameLayout)findViewById(R.id.fr)).setVisibility(View.INVISIBLE);
                ((FrameLayout)findViewById(R.id.fr)).setClickable(false);
                ibHandle.setImageResource(android.R.drawable.ic_menu_slideshow);
            }
        });

        slidingDrawer.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {
            public void onDrawerClosed() {
                //((FrameLayout)findViewById(R.id.fr)).setVisibility(View.VISIBLE);
                ((FrameLayout)findViewById(R.id.fr)).setClickable(true);
                ibHandle.setImageResource(R.drawable.ic_action_sliding_up);
            }
        });

        slidingDrawer.setOnDrawerScrollListener(new SlidingDrawer.OnDrawerScrollListener() {

			public void onScrollEnded() {
                return;
				// TODO Auto-generated method stub

			}

			public void onScrollStarted() {
                return;
				// TODO Auto-generated method stub
			}
		});

        TextView textNewDocument = (TextView)findViewById(R.id.textNewDocument);
        textNewDocument.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onClick(View v) {
                //mTabsAdapter.addTab(mTabHost.newTabSpec("input").setIndicator(createTabView(ActivityScales.this, "приход")), NewDocFragment.class);
                new WeightDocDbAdapter(ActivityScales.this).insertNewEntry(WeightDocDbAdapter.TYPE_SINGLE);
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fr, new OpenDocFragment());
                fragmentTransaction.commit();
                slidingDrawer.close();
            }
        });

        TextView textWeightDocument = (TextView)findViewById(R.id.textWeightDocument);
        textWeightDocument.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onClick(View v) {
                //mTabsAdapter.addTab(mTabHost.newTabSpec("input").setIndicator(createTabView(ActivityScales.this, "приход")), NewDocFragment.class);
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fr, new ListDocFragment());
                fragmentTransaction.commit();
                slidingDrawer.close();
            }
        });

    }

    void connectBluetooth() {

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Set<String> def = Main.preferencesScale.read(ActivityPreferences.KEY_DEVICES, new HashSet<String>());
            String define_device = Main.preferencesScale.read(ActivityPreferences.KEY_LAST, "");
            if (!define_device.isEmpty()) {
                foundDevice.add(bluetooth.getRemoteDevice(define_device));
            }
            if (!def.isEmpty()) {
                for (String str : def) {
                    if (!str.equals(define_device)) {
                        foundDevice.add(bluetooth.getRemoteDevice(str));
                    }
                }
            }
        } else {
            for (int i = 0; Main.preferencesScale.contains(ActivityPreferences.KEY_ADDRESS + i); i++) { //заполнение списка
                foundDevice.add(bluetooth.getRemoteDevice(Main.preferencesScale.read(ActivityPreferences.KEY_ADDRESS + i, "")));
            }
        }*/

        String define_device = Main.preferencesScale.read(ActivityPreferences.KEY_LAST, "");

        try {
            scaleModule.init(Main.versionName, define_device);
        } catch (Throwable throwable) {
            Intent intent = new Intent(getBaseContext(), ActivitySearch.class);
            startActivityForResult(intent, REQUEST_SEARCH_SCALE);
        }




        /*BluetoothDevice bluetoothDevice = null;
        if (foundDevice.isEmpty()) {
            Intent intent = new Intent(getBaseContext(), ActivitySearch.class);
            startActivityForResult(intent, REQUEST_SEARCH_SCALE);
        } else{
            if (foundDevice.size() == 1) {
                bluetoothDevice = (BluetoothDevice)foundDevice.toArray()[0];
            } else if (Main.preferencesScale.read(ActivityPreferences.KEY_LAST, "").isEmpty()) {
                //new ErrorDBAdapter(this).insertNewEntry("101", getString(R.string.error_choice));
            } else {
                bluetoothDevice = bluetooth.getRemoteDevice(Main.preferencesScale.read(ActivityPreferences.KEY_LAST, ""));
            }


        }*/
    }

    private void setupWeightView() {

        weightTextView = new WeightTextView(this);
        weightTextView = (WeightTextView) findViewById(R.id.weightTextView);
        weightTextView.setMax(COUNT_STABLE);
        weightTextView.setSecondaryProgress(numStable = 0);

        SimpleGestureFilter.SimpleGestureListener weightViewGestureListener = new SimpleGestureFilter.SimpleGestureListener() {
            @Override
            public void onSwipe(int direction) {

                switch (direction) {
                    case SimpleGestureFilter.SWIPE_RIGHT:
                    case SimpleGestureFilter.SWIPE_LEFT:
                        /*if (saveWeight(Scales.weight)) {
                            //if (((OnCheckEventListener)getSupportFragmentManager().getFragments().get(mViewPager.getCurrentItem())).someEventSaveWeight(Scales.weight)){
                            //((Button)mViewPager.getFocusedChild().findViewById(R.id.buttonGross)).setEnabled(false);//buttonGross.setEnabled(false);
                            weightViewIsSwipe = true;
                            buttonFinish.setEnabled(true);
                            buttonFinish.setAlpha(255);
                            flagExit = true;
                        }
                        if (weightType == WeightType.SECOND) {
                            weightTypeUpdate();
                        }*/
                        break;
                    default:
                }
            }

            @Override
            public void onDoubleTap() {
                vibrator.vibrate(100);
                new ZeroThread(ActivityScales.this).start();
            }
        };

        detectorWeightView = new SimpleGestureFilter(this, weightViewGestureListener);
        detectorWeightView.setSwipeMinVelocity(50);
        weightTextView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                detectorWeightView.setSwipeMaxDistance(v.getMeasuredWidth());
                detectorWeightView.setSwipeMinDistance(detectorWeightView.getSwipeMaxDistance() / 3);
                detectorWeightView.onTouchEvent(event);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        touchWeightView = true;
                        vibrator.vibrate(5);
                        int progress = (int) (event.getX() / (detectorWeightView.getSwipeMaxDistance() / weightTextView.getMax()));
                        weightTextView.setSecondaryProgress(progress);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        touchWeightView = false;
                        break;
                    default:
                }
                return false;
            }
        });
    }

    private void setupListViewWeight(){
        ListView listViewWeight = (ListView) findViewById(R.id.listViewWeights);
        arrayAdapterListWeight = new WeightListAdapter(this,R.layout.list_item_weight,new ArrayList<WeightDescriptor>());
        listViewWeight.setAdapter(arrayAdapterListWeight);
    }

    public void removeWeightDescriptorOnClickHandler(View v) {
        WeightDescriptor itemToRemove = (WeightDescriptor)v.getTag();
        arrayAdapterListWeight.remove(itemToRemove);
    }

    private void exit() {
        autoWeightThread.cancel();
        while (autoWeightThread.isStart()) ;
        if (broadcastReceiver != null)
            unregisterReceiver(broadcastReceiver);
        scaleModule.removeCallbacksAndMessages(null);
        scaleModule.dettach();
        bluetooth.disable();
        //while (bluetooth.isEnabled()) ;
        //startService(new Intent(this, ServiceSentSheetServer.class));// Запускаем сервис для передачи данных на google disk//todo временно отключен
        //startService(new Intent(this, ServiceProcessTask.class));
        //finish();
    }

    void openSearch() {
        autoWeightThread.cancel();
        while (autoWeightThread.isStart());
        scaleModule.dettach();
        startActivityForResult(new Intent(getBaseContext(), ActivitySearch.class), REQUEST_SEARCH_SCALE);
    }

    public boolean isCapture() {
        boolean capture = false;
        while (getWeightToStepMeasuring(moduleWeight) > Main.autoCapture) {
            if (!capture) {
                try {TimeUnit.SECONDS.sleep(Main.timeDelayDetectCapture);} catch (InterruptedException ignored) {}
                capture = true;
            } else {
                return true;
            }
        }
        return false;
    }

    public boolean processStable(int weight) {
        if (tempWeight - Main.stepMeasuring <= weight && tempWeight + Main.stepMeasuring >= weight) {
            if (++numStable >= COUNT_STABLE) {
                return true;
            }
        } else {
            numStable = 0;
        }
        tempWeight = weight;
        return false;
    }

    private int getWeightToStepMeasuring(int weight){
        return (weight / Main.stepMeasuring * Main.stepMeasuring);
    }
}