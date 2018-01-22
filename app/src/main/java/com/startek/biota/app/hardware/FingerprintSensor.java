package com.startek.biota.app.hardware;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.startek.biota.app.R;
import com.startek.biota.app.managers.FingerprintDeviceManager;
import com.startek.biota.app.models.Human;
import com.startek.biota.app.models.InternalLog;
import com.startek.biota.app.models.Nfc;
import com.startek.biota.app.utils.Converter;
import com.startek.biota.app.managers.MatchLogManager;
import com.startek.biota.app.utils.DialogHelper;
import com.startek.fm210.tstlib;
import org.jdeferred.DoneCallback;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import com.startek.biota.app.global.Global;
import com.startek.biota.app.managers.FileManager;

/**
 * 指紋機相關操作
 *
 * http://developer.android.com/intl/zh-tw/guide/topics/connectivity/usb/host.html
 * http://mobilemerit.com/android-app-for-usb-host-with-source-code/
 * http://cms.35g.tw/coding/android-獲取usb-otg插入狀置的資訊
 * http://www.crifan.com/android_try_to_auto_grant_usb_device_operation_permission/
 *
 * 1. 使用 usb 連接手機與電腦
 * 2. 確保只有一個手機連接到電腦，否則會出現錯誤『error: more than one device and emulator』
 * 3. Shell> cd /Users/skt90u/Library/Android/sdk/platform-tools
 * 4. Shell> ./adb devices
 * 5. Shell>  ./adb tcpip 5555
 * 6. 開啟 Adnroid App 『ADB wireless by Henry』
 * 7. Shell>  ./adb connect 192.168.0.124 <-- 請依照 『ADB wireless by Henry』指定的IP
 *
 * http://stackoverflow.com/questions/19485467/adb-wireless-device-unauthorized
 *
 * adb 移除所有 device
 *
 * -- Unplug all devices and close all emulators
 * -- Run ./adb kill-server, or otherwise kill off your running adb process
 * -- Try running adb devices again, and you should get an empty list
 * -- Try firing up emulators or plugging in devices, and they should start
 *
 * https://groups.google.com/forum/#!topic/android-developers/JTFoSlPqWh4
 * USB Host Diagnostic
 */
public class FingerprintSensor
{
    private static final String TAG = "FingerprintSensor";

    private static final int RESULT_UNKNOWN = -999;
    private static final int RESULT_SUCCESS = 0;
    private static final int RESULT_FATAL = -2; // abnormal behavior disconnect or device error

    private static final String ACTION_USB_PERMISSION = "com.startek.fingerprint.USB_PERMISSION";

    private static final int U_CLASS_A = 65;
    private static final int U_CLASS_B = 66;
    private static final int U_CLASS_C = 67;
    private static final int U_CLASS_D = 68;
    private static final int U_CLASS_E = 69;
    private static final int U_CLASS_R = 82;

    private static final int    U_INSUFFICIENT_FP     = -31;
    private static final int    U_NOT_YET             = -32;


    private static final int STATUS_NO_PERMISSION = 101;
    private static final int STATUS_NO_GRANT_PERMISSION_CALLBACK = 102;
    private static final int STATUS_NO_ACTION_USB_PERMISSION = 103;
    private static final int STATUS_NO_CONNECTION = 104;
    private static final int STATUS_PERMISSION_DENIED = 105;

    private static final int STATUS_CONNECTED = 201;
    private static final int STATUS_DISCONNECTED = 202;
    private static final int STATUS_IN_USE = 203;
    private static final int STATUS_READY = 204;

    private static final int STATUS_FAILED_TO_CLOSE = 301;
    private static final int STATUS_FAILED_TO_OPEN_DEVICE = 302;
    private static final int STATUS_FAILED_TO_CLAIM_INTERFACE = 303;
    private static final int STATUS_FAILED_TO_CONNECT_CAPTURE_DRIVER = 304;
    private static final int STATUS_FAILED_TO_RELEASE_INTERFACE = 305;
    private static final int STATUS_FAILED_TO_GET_EXTRA_DEVICE = 306;



    /**
     * 判斷指定USB裝置是否為指紋機
     */
    public static boolean isFingerprintSensor(Activity activity, UsbDevice device)
    {
        String function = "isFingerprintSensor";

        int pid = device.getProductId();
        int vid = device.getVendorId();
        log(activity, function, String.format("pid: %d, vid:%d", pid, vid));

        boolean result = (pid==0x8220 && vid==0x0bca) ||
                (pid==0x8220 && vid==0x0b39) ||
                (pid==0x8210 && vid==0x0b39) ||
                (pid==33312  && vid==3018); // 這次提供的測試機對應參數

        if(!result) // just for debug
        {
            String message = String.format("目前使用 USB 裝置不是指紋機 (pid: %d, vid: %d)", pid, vid);
            log(activity, function, message);
//            DialogHelper.alert(activity, message);
        }

        return result;
    }

    /**
     * 尋找第一台指紋機
     */
    public static FingerprintSensor findFirst(Activity activity)
    {
        String function = "findFirst";

        UsbDevice found = null;

        try
        {
            UsbManager manager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);

            Collection<UsbDevice> devices = manager.getDeviceList().values();

            if(devices.size() == 0) // just for debug
            {
                String message = String.format("目前無任何 USB 裝置");
                Log.e(TAG, message);
//                log(activity, function, message);
            }

            for(UsbDevice device : devices)
            {
                if(isFingerprintSensor(activity, device))
                {
                    found = device;
                    break;
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        FingerprintSensor sensor = found != null ? new FingerprintSensor(activity, found) : null;

        if(sensor != null)
        {
            FingerprintDeviceManager fingerprintDeviceManager = new FingerprintDeviceManager(activity);
            fingerprintDeviceManager.create(sensor);
        }

        return sensor;
    }

    private tstlib adapter;
    private Activity activity;
    private UsbManager manager;
    private UsbDevice device;
    private UsbInterface usbInterface;
    private UsbDeviceConnection connection;
    private boolean isConnected;

    public FingerprintSensor(Activity activity, UsbDevice device)
    {
        try
        {
            this.activity = activity;
            this.adapter = new tstlib(activity);
            this.manager = (UsbManager) activity.getSystemService(Context.USB_SERVICE);
            this.device = device;
            this.usbInterface = device.getInterface(0);
            this.status = STATUS_NO_CONNECTION;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private int status;
    private void setStatus(int status)
    {
        this.status = status;
    }
    private int getStatus()
    {
        return status;
    }

    private String getStatusString()
    {
        switch (status)
        {
            case STATUS_NO_PERMISSION: return "無裝置使用權限";
            case STATUS_NO_GRANT_PERMISSION_CALLBACK: return "未設定 GRANT_PERMISSION_CALLBACK";
            case STATUS_NO_ACTION_USB_PERMISSION: return "接收到的 ACTION 並非 ACTION_USB_PERMISSION";
            case STATUS_NO_CONNECTION: return "裝置尚未連線";
            case STATUS_PERMISSION_DENIED: return "裝置要求要求權限失敗";


            case STATUS_CONNECTED: return "裝置已連線";
            case STATUS_DISCONNECTED: return "裝置已斷線";
            case STATUS_IN_USE: return "裝置使用中";
            case STATUS_READY: return "裝置狀態正常";

            case STATUS_FAILED_TO_CLOSE: return "裝置無法關閉";
            case STATUS_FAILED_TO_OPEN_DEVICE: return "無法開啟裝置";
            case STATUS_FAILED_TO_CLAIM_INTERFACE: return "裝置無法要求介面";
            case STATUS_FAILED_TO_CONNECT_CAPTURE_DRIVER: return "無法連接指紋機驅動程式";
            case STATUS_FAILED_TO_RELEASE_INTERFACE: return "裝置無法釋放介面";
            case STATUS_FAILED_TO_GET_EXTRA_DEVICE: return "無法取得額外裝置";

            default:
            {
                return String.format("未知狀態(%d)", status);
            }
        }
    }

    private static void log(Activity activity, String function, String message)
    {
        InternalLog.d(
                activity,
                TAG,
                function,
                message);
    }

    public boolean open()
    {
        String function = "open";

        // 避免因為指紋機在驗證或者註冊的時候，NFC事件來搗蛋，導致程式當機
        if(getStatus() == STATUS_IN_USE)
        {
            log(activity, function, getStatusString());
            return false;
        }

        if(!checkPermission(new DoneCallback() {
            @Override
            public void onDone(Object result) {
                open();
            }
        }))
        {
            setStatus(STATUS_NO_PERMISSION);
            log(activity, function, getStatusString());
            return false;
        }

        try
        {
            if(close() == false)
            {
                setStatus(STATUS_FAILED_TO_CLOSE);
                log(activity, function, getStatusString());
                return false;
            }

            connection =  manager.openDevice(device);

            if(connection == null)
            {
                setStatus(STATUS_FAILED_TO_OPEN_DEVICE);
                log(activity, function, getStatusString());
                return false;
            }

            if(!connection.claimInterface(usbInterface, true))
            {
                setStatus(STATUS_FAILED_TO_CLAIM_INTERFACE);
                log(activity, function, getStatusString());
                return false;
            }

            isConnected = RESULT_SUCCESS == adapter.FP_ConnectCaptureDriver(connection.getFileDescriptor());

            if(!isConnected)
            {
                setStatus(STATUS_FAILED_TO_CONNECT_CAPTURE_DRIVER);
                log(activity, function, getStatusString());
                return false;
            }

            setStatus(STATUS_CONNECTED);
            log(activity, function, getStatusString());
            return isConnected;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean close()
    {
        String function = "close";

        // 避免因為指紋機在驗證或者註冊的時候，NFC事件來搗蛋，導致程式當機
        if(getStatus() == STATUS_IN_USE)
        {
            log(activity, function, getStatusString());
            return false;
        }

        if(connection == null)
        {
            setStatus(STATUS_NO_CONNECTION);
            log(activity, function, getStatusString());
            return true;
        }

        try
        {
            if(isConnected)
            {
                adapter.FP_DisconnectCaptureDriver();
                isConnected = false;

                setStatus(STATUS_DISCONNECTED);
                log(activity, function, getStatusString());
            }

            if(!connection.releaseInterface(usbInterface))
            {
                setStatus(STATUS_FAILED_TO_RELEASE_INTERFACE);
                log(activity, function, getStatusString());
                return false;
            }

            connection.close();
            connection = null;

            setStatus(STATUS_NO_CONNECTION);
            log(activity, function, getStatusString());

            return true;
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // ----------------------------------------
    // Enroll
    // ----------------------------------------

    public static final int EVENT_EXCEPTION = 0;
    public static final int EVENT_CAPTURE_TIMEOUT = 1;
    public static final int EVENT_STOP_IMMEDIATELY = 2;

    public static final int EVENT_ENROLL_ACTION_FINGER_PRESS  = 101;
    public static final int EVENT_ENROLL_ACTION_SHOW_IMAGE    = 102;
    public static final int EVENT_ENROLL_ACTION_SAVE_IMAGE    = 103;
    public static final int EVENT_ENROLL_ACTION_FINGER_REMOVE = 104;
    public static final int EVENT_ENROLL_ACTION_SAVE_MINUTIAE = 105;
    public static final int EVENT_ENROLL_ACTION_SAVE_DAT      = 106;
    public static final int EVENT_ENROLL_RESULT_SUCCESS       = 107;
    public static final int EVENT_ENROLL_RESULT_FAILURE       = 108;

    public static final int EVENT_VERIFY_ACTION_FINGER_PRESS   = 201;
    public static final int EVENT_VERIFY_ACTION_SHOW_IMAGE     = 202;
    public static final int EVENT_VERIFY_ACTION_SAVE_IMAGE     = 203;
    public static final int EVENT_VERIFY_ACTION_VERIFY_START   = 204;
    public static final int EVENT_VERIFY_RESULT_SUCCESS        = 205;
    public static final int EVENT_VERIFY_RESULT_FAILURE        = 206;

    private String eventToString(int event)
    {
        switch (event)
        {
            case EVENT_EXCEPTION: return "EVENT_EXCEPTION";
            case EVENT_CAPTURE_TIMEOUT: return "EVENT_CAPTURE_TIMEOUT";
            case EVENT_STOP_IMMEDIATELY: return "EVENT_STOP_IMMEDIATELY";

            case EVENT_ENROLL_ACTION_FINGER_PRESS: return "EVENT_ENROLL_ACTION_FINGER_PRESS";
            case EVENT_ENROLL_ACTION_SHOW_IMAGE: return "EVENT_ENROLL_ACTION_SHOW_IMAGE";
            case EVENT_ENROLL_ACTION_SAVE_IMAGE: return "EVENT_ENROLL_ACTION_SAVE_IMAGE";
            case EVENT_ENROLL_ACTION_FINGER_REMOVE: return "EVENT_ENROLL_ACTION_FINGER_REMOVE";
            case EVENT_ENROLL_ACTION_SAVE_MINUTIAE: return "EVENT_ENROLL_ACTION_SAVE_MINUTIAE";
            case EVENT_ENROLL_ACTION_SAVE_DAT: return "EVENT_ENROLL_ACTION_SAVE_DAT";
            case EVENT_ENROLL_RESULT_SUCCESS: return "EVENT_ENROLL_RESULT_SUCCESS";
            case EVENT_ENROLL_RESULT_FAILURE: return "EVENT_ENROLL_RESULT_FAILURE";

            case EVENT_VERIFY_ACTION_FINGER_PRESS: return "EVENT_VERIFY_ACTION_FINGER_PRESS";
            case EVENT_VERIFY_ACTION_SHOW_IMAGE: return "EVENT_VERIFY_ACTION_SHOW_IMAGE";
            case EVENT_VERIFY_ACTION_SAVE_IMAGE: return "EVENT_VERIFY_ACTION_SAVE_IMAGE";
            case EVENT_VERIFY_ACTION_VERIFY_START: return "EVENT_VERIFY_ACTION_VERIFY_START";
            case EVENT_VERIFY_RESULT_SUCCESS: return "EVENT_VERIFY_RESULT_SUCCESS";
            case EVENT_VERIFY_RESULT_FAILURE: return "EVENT_VERIFY_RESULT_FAILURE";
        }

        return String.format("Event(%d)", event);
    }

    private String objToString(Object o)
    {
        if (o instanceof Exception) {
            return String.format("Exception - %s", ((Exception) o).getMessage());
        }

        if (o instanceof String) {
            return (String) o;
        }

        return "";
    }

    public class EnrollThread extends Thread
    {
        private static final String TAG = "EnrollThread";

        public Handler handler;
        public int fingerBtnId;
        public int maxScanTimes;

        private boolean stopImmediately;
        private int timeoutSeconds;
        private Date startTime;
        private Date currentTime;

        private String client_action;

        /**
         * @param handler:
         * @param maxScanTimes: 最多少掃描幾次
         */
        public EnrollThread(Handler handler, int fingerBtnId, int maxScanTimes, int timeoutSeconds, String client_action)
        {
            this.handler = handler;
            this.fingerBtnId = fingerBtnId;
            this.maxScanTimes = maxScanTimes;
            this.timeoutSeconds = timeoutSeconds;
            this.stopImmediately = false;
            this.client_action = client_action;
        }

        private boolean isTimeout()
        {
            if(startTime == null)
            {
                startTime = Calendar.getInstance().getTime();
            }

            currentTime = Calendar.getInstance().getTime();

            long diffInSeconds = (currentTime.getTime() - startTime.getTime()) / 1000;

            return (diffInSeconds > timeoutSeconds);
        }

        @Override
        public synchronized void run()
        {
            int scanTime = 0;

            try
            {
                setStatus(STATUS_IN_USE);

                MatchLogManager matchLogManager = new MatchLogManager(activity);

                if(!isConnected)
                {
                    String message = activity.getString(R.string.sensor_is_not_connected) + String.format("(狀態：%s)", getStatusString());
                    sendMessage(scanTime, EVENT_EXCEPTION, new Exception(message));
                    return;
                }

                Calendar scanStart = Calendar.getInstance();
                Calendar scanStop = Calendar.getInstance();
                Calendar compareStart = Calendar.getInstance();
                Calendar compareStop = Calendar.getInstance();
                int score = 0;

                int result = RESULT_UNKNOWN;

                result = adapter.FP_CreateEnrollHandle();

                for(scanTime=0; scanTime<maxScanTimes; scanTime++)
                {
                    sendMessage(scanTime, EVENT_ENROLL_ACTION_FINGER_PRESS, activity.getString(R.string.sensor_please_press));

                    while (RESULT_SUCCESS != (result = adapter.FP_Capture()))
                    {
//                        Bitmap bitmap = getBufferImage();
//                        sendMessage(scanTime, EVENT_ENROLL_ACTION_SHOW_IMAGE, bitmap);

                        if(isTimeout()) {
                            sendMessage(scanTime, EVENT_CAPTURE_TIMEOUT, activity.getString(R.string.sensor_capture_timeout));
                            return;
                        }

                        if(stopImmediately) {
                            sendMessage(scanTime, EVENT_STOP_IMMEDIATELY, activity.getString(R.string.sensor_stop_immediately));
                            return;
                        }
                    }

                    String bmpPath = FileManager.getEnrollBmpPath(fingerBtnId, scanTime);
                    adapter.FP_SaveImageBMP(bmpPath);

                    sendMessage(scanTime, EVENT_ENROLL_ACTION_SAVE_IMAGE, bmpPath);

                    byte[] minu_code1 = new byte[512];
                    byte[] minu_code2 = new byte[512];

                    compareStart = Calendar.getInstance();

                    result = adapter.FP_GetTemplate(minu_code1);

                    result = adapter.FP_ISOminutiaEnroll(minu_code1, minu_code2);

                    compareStop = Calendar.getInstance();

                    sendMessage(scanTime, EVENT_ENROLL_ACTION_FINGER_REMOVE, activity.getString(R.string.sensor_remove_finger));

                    while(adapter.FP_CheckBlank()==-1);

                    score = adapter.Score();

                    if(result == U_CLASS_A || result == U_CLASS_B)
                    {
                        scanStop = Calendar.getInstance();

                        matchLogManager.createMatchLogRi(
                                Converter.encodeBase64String(minu_code2),
                                bmpPath,
                                scanStart.getTime(),
                                scanStop.getTime().getTime() - scanStart.getTime().getTime(),
                                score,
                                compareStop.getTime().getTime() - compareStart.getTime().getTime(),
                                true,
                                client_action
                        );

                        String datPath = FileManager.getEnrollDatFilename(fingerBtnId);
                        adapter.FP_SaveISOminutia(minu_code2, datPath);

                        String minutiae = Converter.encodeBase64String(minu_code2);
                        sendMessage(scanTime, EVENT_ENROLL_ACTION_SAVE_MINUTIAE, minutiae);
                        sendMessage(scanTime, EVENT_ENROLL_ACTION_SAVE_DAT, datPath);
                        sendMessage(scanTime, EVENT_ENROLL_RESULT_SUCCESS, activity.getString(R.string.sensor_enroll_success));
                        return;
                    }
                    else if(scanTime==maxScanTimes-1)
                    {
                        scanStop = Calendar.getInstance();

                        matchLogManager.createMatchLogRi(
                                Converter.encodeBase64String(minu_code2),
                                bmpPath,
                                scanStart.getTime(),
                                scanStop.getTime().getTime() - scanStart.getTime().getTime(),
                                score,
                                compareStop.getTime().getTime() - compareStart.getTime().getTime(),
                                false,
                                client_action
                        );

                        String reason = (result == U_CLASS_C || result == U_CLASS_D || result == U_CLASS_E) ? activity.getString(R.string.sensor_enroll_low_quality)
                                : (result == U_NOT_YET) ? activity.getString(R.string.sensor_enroll_not_complete)
                                //: (result == S_FP_INVALID) ? activity.getString(R.string.sensor_p_code_invalid)
                                : (result == U_INSUFFICIENT_FP) ? activity.getString(R.string.sensor_invalid_input_handle)
                                : String.format("UNKNOWN_ERROR(%d)", result);

                        sendMessage(scanTime, EVENT_ENROLL_RESULT_FAILURE, String.format(activity.getString(R.string.sensor_enroll_failure), reason));

                        return;
                    }
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                sendMessage(scanTime, EVENT_EXCEPTION, ex);
            }
            finally
            {
                setStatus(STATUS_READY);
                adapter.FP_DestroyEnrollHandle();
                System.gc();
            }
        }

        private boolean sendMessage(int scanTime, int action, Object obj)
        {
            int what = action;
            int arg1 = scanTime;
            int arg2 = -1; // useless

            Message m = handler.obtainMessage(what, arg1, arg2, obj);

            Log.d(TAG, String.format("scanTime: %d, event:%s, obj: %s", scanTime, eventToString(action), objToString(obj)));

            return handler.sendMessage(m);
        }

        private Handler outerHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {

                int action = msg.what;
//                String message = (String)msg.obj;

                switch (action)
                {
                    case FingerprintSensor.EVENT_STOP_IMMEDIATELY:
                    {
                        stopImmediately = true;
                    }break;
                }
                super.handleMessage(msg);
            }
        };

        public Handler getHandler() {return outerHandler;};
    }

    /**
     * 將指紋資料存入指定檔案
     */
    public void enroll(Handler handler, int fingerBtnId, int maxScanTimes, int timeoutSeconds, String client_action)
    {
        new EnrollThread(handler, fingerBtnId, maxScanTimes, timeoutSeconds, client_action).start();
    }

    // ----------------------------------------
    // Verify
    // ----------------------------------------


    public class VerifyThread extends Thread
    {
        private static final String TAG = "VerifyThread";

        private Handler handler;
        private Human human;
        private Nfc nfc;

        private boolean stopImmediately;
        private int timeoutSeconds;
        private Date startTime;
        private Date currentTime;

        private String client_action;

        public VerifyThread(Handler handler, Human human, Nfc nfc, int timeoutSeconds, String client_action)
        {
            this.handler = handler;
            this.human = human;
            this.nfc = nfc;
            this.timeoutSeconds = timeoutSeconds;
            this.stopImmediately = false;
            this.client_action = client_action;
        }

        private boolean isTimeout()
        {
            if(startTime == null)
            {
                startTime = Calendar.getInstance().getTime();
            }

            currentTime = Calendar.getInstance().getTime();

            long diffInSeconds = (currentTime.getTime() - startTime.getTime()) / 1000;

            return (diffInSeconds > timeoutSeconds);
        }

        @Override
        public synchronized void run()
        {
            try
            {
                setStatus(STATUS_IN_USE);

                MatchLogManager matchLogManager = new MatchLogManager(activity);

                if (!isConnected) {
                    String message = activity.getString(R.string.sensor_is_not_connected) + String.format("(狀態：%s)", getStatusString());
                    sendMessage(EVENT_EXCEPTION, new Exception(message));
                    return;
                }

                if(!human.hasDatFile()) {
                    // 查無指紋資料
                    sendMessage(EVENT_EXCEPTION, new Exception(activity.getString(R.string.sensor_verify_failure_no_dat_file)));
                    return;
                }

                Calendar scanStart = Calendar.getInstance();
                Calendar scanStop = Calendar.getInstance();
                Calendar compareStart = Calendar.getInstance();
                Calendar compareStop = Calendar.getInstance();
                int score = 0;
                int scoreLimit = Global.getConfig().getVerifyScoreLimit();

                int result = RESULT_UNKNOWN;
                byte[] minu_code1 = new byte[512];
                byte[] minu_code2 = new byte[512];

                // 請按壓指紋
                sendMessage(EVENT_VERIFY_ACTION_FINGER_PRESS, activity.getString(R.string.sensor_please_press));

                while (RESULT_SUCCESS != (result = adapter.FP_Capture()))
                {
//                    Bitmap bitmap = getBufferImage();
//                    sendMessage(EVENT_VERIFY_ACTION_SHOW_IMAGE, bitmap);

                    if(isTimeout()) {
                        sendMessage(EVENT_CAPTURE_TIMEOUT, activity.getString(R.string.sensor_capture_timeout));
                        return;
                    }

                    if(stopImmediately) {
                        sendMessage(EVENT_STOP_IMMEDIATELY, activity.getString(R.string.sensor_stop_immediately));
                        return;
                    }
                }

                scanStop = Calendar.getInstance();

                String bmpPath = FileManager.getVerifyBmpPath();
                adapter.FP_SaveImageBMP(bmpPath);

                // 指紋截取完畢
                sendMessage(EVENT_VERIFY_ACTION_SAVE_IMAGE, bmpPath);

                compareStart = Calendar.getInstance();

                result = adapter.FP_GetTemplate(minu_code1);

                // 開始進行使用者驗證

                sendMessage(EVENT_VERIFY_ACTION_VERIFY_START, activity.getString(R.string.sensor_verify_start));

                for(String datPath:human.getDatPaths())
                {
//                    try
//                    {
//                        System.gc();
//                        Thread.sleep(1000);
//                    }
//                    catch(Exception e){}

                    if(RESULT_SUCCESS != (result = adapter.FP_LoadISOminutia(minu_code2, datPath)))continue; // 載入 dat 檔案失敗

                    result = adapter.FP_ISOminutiaMatchEx(minu_code1, minu_code2);
                    // Return Value
                    //  - OK               : The verification of fingerprint image with final fingerprint code meets the requirement of security.
                    //  - FAIL             : The fingerprint image is not identical with the final fingerprint code on the required security.
                    //  - S_MEM_ERR        : Insufficient memory for image processing.
                    //  - S_FPCODE_INVALID : the input fp_code is illegal.
                    //  - S_SECURITY_ERR   : improper security level setting.

                    compareStop = Calendar.getInstance();

                    if(result >= -1) // <-- Light 提供範例
                    {
                        score = adapter.Score();
                        if(score >= scoreLimit )
                        {
                            matchLogManager.createMatchLogRv(
                                    Converter.encodeBase64String(minu_code2),
                                    bmpPath,
                                    human == null ? "" : human.id,
                                    human == null ? "" : human.bind_id,
                                    nfc == null ? "" : nfc.tagId,
                                    scanStart.getTime(),
                                    scanStop.getTime().getTime() - scanStart.getTime().getTime(),
                                    score,
                                    compareStop.getTime().getTime() - compareStart.getTime().getTime(),
                                    true,
                                    client_action
                            );

                            // 驗證通過
                            sendMessage(EVENT_VERIFY_RESULT_SUCCESS, activity.getString(R.string.sensor_verify_success));
                            // sendMessage(EVENT_VERIFY_RESULT_SUCCESS, String.format(activity.getString(R.string.sensor_verify_success_detail), score, scoreLimit));

                            return;
                        }
                    }
                }

                // 指紋驗證失敗
                sendMessage(EVENT_VERIFY_RESULT_FAILURE, activity.getString(R.string.sensor_verify_failure));
                // sendMessage(EVENT_VERIFY_RESULT_FAILURE, String.format(activity.getString(R.string.sensor_verify_failure_detail), score, scoreLimit));

                matchLogManager.createMatchLogRv(
                        Converter.encodeBase64String(minu_code2),
                        bmpPath,
                        human == null ? "" : human.id,
                        human == null ? "" : human.bind_id,
                        nfc == null ? "" : nfc.tagId,
                        scanStart.getTime(),
                        scanStop.getTime().getTime() - scanStart.getTime().getTime(),
                        score,
                        compareStop.getTime().getTime() - compareStart.getTime().getTime(),
                        false,
                        client_action
                );
            } catch (Exception ex) {
                sendMessage(EVENT_EXCEPTION, ex);
            }
            finally {
                setStatus(STATUS_READY);
                System.gc();
            }
        }

        private boolean sendMessage(int action, Object obj)
        {
            int what = action;
            int arg1 = -1;
            int arg2 = -1; // useless

            Message m = handler.obtainMessage(what, arg1, arg2, obj);

            Log.d(TAG, String.format("event:%s, obj: %s", eventToString(action), objToString(obj)));

            return handler.sendMessage(m);
        }



        private Handler outerHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {

                int action = msg.what;
//                String message = (String)msg.obj;

                switch (action)
                {
                    case FingerprintSensor.EVENT_STOP_IMMEDIATELY:
                    {
                        stopImmediately = true;
                    }break;
                }
                super.handleMessage(msg);
            }
        };

        public Handler getHandler() {return outerHandler;};

        public Human getHuman() {
            return human;
        }
    }

    private static byte[] bmpBuffer= new byte[1078+(640*480)];

    private Bitmap getBufferImage()
    {
        try
        {
            adapter.FP_GetImageBuffer(bmpBuffer);
            return BitmapFactory.decodeByteArray(bmpBuffer, 0, bmpBuffer.length);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * 將指紋資料與指定檔案做比對
     */
    public VerifyThread verify(Handler handler, Human human, Nfc nfc, int timeoutSeconds, String client_action)
    {
        VerifyThread thread = new VerifyThread(handler, human, nfc, timeoutSeconds, client_action);

        thread.start();

        return thread;
    }

    private Bitmap getImage()
    {
        if(!isConnected) return null;

        Bitmap bmp = null;

        try
        {
            byte[] bmpBuffer = new byte[1078+(640*480)];

            adapter.FP_GetImageBuffer(bmpBuffer);

            bmp = BitmapFactory.decodeByteArray(bmpBuffer, 0, bmpBuffer.length);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return bmp;
    }

    /**
     * 指紋機 UID
     */
    public String getSerialNumber()
    {
        return Build.VERSION.SDK_INT >= 21 ? device.getSerialNumber() : "";
    }

    /**
     * 產品識別碼
     */
    public int getProductId()
    {
        return device.getProductId();
    }

    /**
     * 廠商識別碼
     */
    public int getVendorId()
    {
        return device.getVendorId();
    }

    /**
     * 版本
     */
    public String getVersion()
    {
        //return Build.VERSION.SDK_INT >= 23 ? device.getVersion() : "";
        return ""; // api version 23 以下，無法取得版本資訊
    }

    /**
     * 速度
     */
    public int getDeviceProtocol() {
        return device.getDeviceProtocol();
    }

    /**
     * 製造商
     *
     * fingerprint_device.addr
     */
    public String getManufacturerName() {
        return Build.VERSION.SDK_INT >= 21 ? device.getManufacturerName() : "";
    }

    /**
     * 位置識別碼
     *
     * fingerprint_device.addr
     */
    public String getAddr()
    {
        return ""; // 無對應資料
    }

    /**
     * 資料更新時間 (FP_Capture)
     *
     * 20160406 Norman, Light 交代【針對資料更新時間(USB插入時間)，Android 對 USB 內的屬性中，沒有資料更新時間(USB插入時間)，請留空白即可】
     */
    public Date getLastUpdateTime() {
        //return lastUpdateTime;
        return null;
    }

    /**
     * 產品名稱
     *
     * fingerprint_device.product
     */
    public String getProductName() {
        return Build.VERSION.SDK_INT >= 21 ? device.getProductName() : "";
    }

    private boolean checkPermission(DoneCallback afterGrantPermission)
    {
        String function = "checkPermission";

        if(manager.hasPermission(device))
        {
            log(activity, function, "已經具備裝置的使用權限");
            return true;
        }

        this.afterGrantPermission = afterGrantPermission;

        registerPermissionReceiver(Global.getContext());

        PendingIntent permissionIntent = PendingIntent.getBroadcast(activity, 0, new Intent(ACTION_USB_PERMISSION), 0);
        manager.requestPermission(device, permissionIntent);
        log(activity, function, "正在要求裝置使用權限 ...");

        return false;
    }

    private void registerPermissionReceiver(Context context) {
        Log.i(TAG, "register permission receiver");
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        context.registerReceiver(permissonReceiver, filter);
    }

    private void unregisterPermissionReceiver(Context context) {
        Log.i(TAG, "unregister permission receiver");
        context.unregisterReceiver(permissonReceiver);
    }

    private DoneCallback afterGrantPermission;

    BroadcastReceiver permissonReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            String function = "onReceive";
            String action = intent.getAction();

            if (!ACTION_USB_PERMISSION.equals(action))
            {
                setStatus(STATUS_NO_ACTION_USB_PERMISSION);
                log(activity, function, String.format("%s (action = %s)", getStatusString(), action));
                return;
            }

            unregisterPermissionReceiver(context);

            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (device == null)
            {
                setStatus(STATUS_FAILED_TO_GET_EXTRA_DEVICE);
                log(activity, function, getStatusString());
                return;
            }

            if (!intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
            {
                setStatus(STATUS_PERMISSION_DENIED);
                log(activity, function, getStatusString());
                return;
            }

            if(afterGrantPermission == null)
            {
                setStatus(STATUS_NO_GRANT_PERMISSION_CALLBACK);
                log(activity, function, getStatusString());
                return;
            }

            synchronized (this) {
                afterGrantPermission.onDone(FingerprintSensor.this);
            }

        }
    };

    // ----------------------------------------

//    public boolean verify(VerifyListener listener) {
//        queue.add(new RetrieveImageAction(this, listener, objectHandle, pictureSampleSize));
//    }
}
