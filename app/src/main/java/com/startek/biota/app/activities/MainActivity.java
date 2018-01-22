package com.startek.biota.app.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.startek.biota.app.R;
import com.startek.biota.app.fragments.BaseFragment;
import com.startek.biota.app.fragments.LoginFragment;
import com.startek.biota.app.fragments.TransitionListener;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.hardware.FingerprintSensor;
import com.startek.biota.app.hardware.NfcReader;
import com.startek.biota.app.managers.FingerprintDeviceManager;
import com.startek.biota.app.models.EasyCard;
import com.startek.biota.app.utils.DialogHelper;

public class MainActivity extends AppCompatActivity implements TransitionListener {

    private final String TAG = "MainActivity";

    private NfcReader nfcReader;
    private EasyCard receivedEasyCard;
    private FingerprintSensor sensor;

    private View decorView;
    private int uiOptions;



    /**
     * http://stackoverflow.com/questions/25901135/navigation-bar-appears-briefly-when-switching-activites
     */
    private void initFullscreen()
    {
        // hide action bar

        getSupportActionBar().hide();

        // hide navigation bar

        // http://stackoverflow.com/questions/21724420/how-to-hide-navigation-bar-permanently-in-android-activity

        int currentApiVersion = android.os.Build.VERSION.SDK_INT;

        uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        // This work only for android 4.4+
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT)
        {
            decorView = getWindow().getDecorView();

            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()
                    {

                        @Override
                        public void onSystemUiVisibilityChange(int visibility)
                        {
                if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                {
                    decorView.setSystemUiVisibility(uiOptions);
                }
                }
            });
        }

        // http://stackoverflow.com/questions/21225424/hide-button-bar-in-android
//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
//        decorView.setSystemUiVisibility(uiOptions);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initFullscreen();

        setContentView(R.layout.activity_main);

        Global.setActivity(this);

        nfcReader = new NfcReader(this);

//        FingerprintSensor.findFirst(this); // 用來觸發 fingerprintDeviceManager.create(sensor);

        LoginFragment fragment = new LoginFragment();
//        FunctionListFragment fragment = new FunctionListFragment();
        replaceFragment(fragment, "LoginFragment", true);

        if(savedInstanceState == null)
        {
            resolveIntent(getIntent());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        attachSensor();
    }

    @Override
    protected void onResume() {
        super.onResume();

        setFullscreen();

        nfcReader.attach();

        // 在 app 內部，確保每一頁都可以收到 USB 插拔 事件，以利後續動作。請使用 onUsbChanged 處理後續動作
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        this.registerReceiver(usbChangedReceiver, filter);

        attachSensor(); // <-- 避免程式執行到一半插入指紋機導致讀不倒指紋機(在此再做一次取指紋機的動作)
    }

    public void setFullscreen()
    {
        if(decorView != null)
        {
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        if(receivedEasyCard != null)
            onEasyCardReceived(receivedEasyCard);

        receivedEasyCard = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcReader.detach();

        this.unregisterReceiver(usbChangedReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
        detachSensor();
    }

    private void attachSensor()
    {
//        if(sensor == null)
//            sensor = FingerprintSensor.findFirst(this);
//
//        if(sensor == null)
//        {
//            Log.d(TAG, "attachSensor, sensor == null");
//            return;
//        }
//
//        if(!sensor.open())
//        {
//            Log.d(TAG, "attachSensor, !sensor.open()");
//            return;
//        }

        if(sensor == null)
        {
            sensor = FingerprintSensor.findFirst(this);

            if(sensor == null)
            {
                Log.d(TAG, "attachSensor, sensor == null");
                return;
            }

            if(!sensor.open())
            {
                Log.d(TAG, "attachSensor, !sensor.open()");
                return;
            }
        }
    }

    private void detachSensor()
    {
        if(sensor != null)
        {
            if(!sensor.close())
            {
                Log.d(TAG, "detachSensor, !sensor.close()");
                return; // 到底要不要在此直接 return 呢 ???
            }
            sensor = null;
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        resolveIntent(intent);
    }

    private synchronized boolean resolveIntent(Intent intent)
    {
//        Log.d(TAG, "resolveIntent");

        EasyCard easyCard = null;

        try
        {
            if(intent == null)
            {
//                Log.d(TAG, "resolveIntent, intent == null");
                return false;
            }

            easyCard = nfcReader.parse(intent);
            if(easyCard == null)
            {
//                Log.d(TAG, "resolveIntent, easyCard == null");
                return false;
            }

            return true;
        }
        finally
        {
            receivedEasyCard = easyCard;
        }
    }

    private void onEasyCardReceived(EasyCard easyCard)
    {
        FragmentManager fm = getFragmentManager();
        int count = fm.getBackStackEntryCount();
        if (count > 0) {
            FragmentManager.BackStackEntry entry = fm.getBackStackEntryAt(count - 1);
            BaseFragment f =  (BaseFragment) fm.findFragmentByTag(entry.getName());
            f.onEasyCardReceived(easyCard);
        }
    }

    private void replaceFragment(Fragment f, String tag, boolean addToBackStack) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.frameLayout, f, tag);
        if (addToBackStack) {
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.addToBackStack(tag);
        }
        ft.commit();
    }

//    public void onButtonClick(View view)
//    {
//        Log.e(TAG, "請在 fragment 中註冊對應按鈕事件");
//    }


    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        int count = fm.getBackStackEntryCount();
        if (count > 0) { // call BaseFragment.onBackPressed to determine remove fragment or not
            FragmentManager.BackStackEntry entry = fm.getBackStackEntryAt(count - 1);
            BaseFragment f =  (BaseFragment) fm.findFragmentByTag(entry.getName());
            f.onBackPressed();
        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    public void backToRoot() {
        finish();
    }

    @Override
    public void back() {
        FragmentManager fm = getFragmentManager();
        int count = fm.getBackStackEntryCount();
        if (count > 1) { // when count = 1 -> we just have only one fragment 『LoginFragement』
            fm.popBackStack();
        }
        else{

            super.onBackPressed();
        }
    }

    @Override
    public void showFragment(Fragment f, String tag) {
        replaceFragment(f, tag, true);
    }

    public EasyCard getReceivedEasyCard()
    {
        return receivedEasyCard;
    }

    public FingerprintSensor getFingerprintSensor()
    {
        return sensor;
    }

    protected void onUsbChanged(boolean attached)
    {
        Log.d(TAG, "onUsbChanged, fingerprinter is " + (attached ? "attached" : "detached"));

        if(!attached)
        {
            DialogHelper.alert(MainActivity.this, getString(R.string.usb_detached));
            sensor = null;

            FingerprintDeviceManager fingerprintDeviceManager = new FingerprintDeviceManager(MainActivity.this);
            fingerprintDeviceManager.delete();
        }

        FragmentManager fm = getFragmentManager();
        int count = fm.getBackStackEntryCount();
        if (count > 0) {
            FragmentManager.BackStackEntry entry = fm.getBackStackEntryAt(count - 1);
            BaseFragment f =  (BaseFragment) fm.findFragmentByTag(entry.getName());
            f.onUsbChanged(attached);
        }
    }

    private BroadcastReceiver usbChangedReceiver = new BroadcastReceiver() {

        private static final String TAG = "usbChangedReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "onReceive");

            String action = intent.getAction();
            Log.d(TAG, String.format("onReceive, action: %s", action));

            synchronized (this)
            {
                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                if(device == null)
                {
                    Log.d(TAG, String.format("onReceive, device == null"));
                    return;
                }

                if(UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action))
                {
                    // 指紋機插上插頭時，isFingerprintSensor判斷才會正確
                    if(!FingerprintSensor.isFingerprintSensor(MainActivity.this, device))
                    {
                        Log.d(TAG, String.format("onReceive, !FingerprintSensor.isFingerprintSensor(device)"));
                        return;
                    }

                    if(FingerprintSensor.isFingerprintSensor(MainActivity.this, device))
                        onUsbChanged(true); // true stand for attached
                }

                if(UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action))
                {
                    // 指紋機拔除插頭時，isFingerprintSensor永遠為 false，因此不可加上以下邏輯
//                    if(!FingerprintSensor.isFingerprintSensor(device))
//                    {
//                        Log.d(TAG, String.format("!FingerprintSensor.isFingerprintSensor(device)"));
//                        return;
//                    }

                    FingerprintSensor sensor = FingerprintSensor.findFirst(MainActivity.this);
                    if(sensor == null)
                        onUsbChanged(false); // false stand for detached
                }
            }
        }
    };
}
