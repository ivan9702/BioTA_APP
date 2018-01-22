package com.startek.biota.app.global;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import labs.anton.icenet.IceNet;
import labs.anton.icenet.IceNetConfig;

import com.startek.biota.app.R;
import com.startek.biota.app.activities.MainActivity;
import com.startek.biota.app.config.Config;
import com.startek.biota.app.constant.PreferencesKey;
import com.startek.biota.app.database.Cache;
import com.startek.biota.app.hardware.FingerprintSensor;
import com.startek.biota.app.models.Human;
import com.startek.biota.app.models.RunningLog;
import com.startek.biota.app.network.webservices.WsApDeviceR2;
import com.startek.biota.app.receivers.EmailReceiver;
import com.startek.biota.app.utils.Cloner;
import com.startek.biota.app.utils.PreferencesUtils;
import com.startek.biota.app.network.webservices.WsApDeviceC;
import com.startek.biota.app.network.webservices.WsApDeviceU;

import org.jdeferred.DoneCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


// http://programmerguru.com/android-tutorial/how-to-sync-remote-mysql-db-to-sqlite-on-android/
// sync service
/*
1. server 發送 push-notification 通知 android 需要更新資料
2. 使用 intent service 更新資料, ??? 兩檯機器同時修改通一個使用者資料
3. 更新完成 使用 eventbus 發送 onDataUpdated

上傳資料
新增，修改資料標記為 dirty

https://github.com/skjolber/external-nfc-api
https://github.com/grundid/nfctools

http://mvnrepository.com

Bitmap mamangement
http://developer.android.com/intl/zh-tw/training/displaying-bitmaps/cache-bitmap.html

Broadcast receiver
http://www.vogella.com/tutorials/AndroidBroadcastReceiver/article.html#broadcastreceiver_asynchronousprocessing
*/

/**
 * 用繼承 Application，建立共用變數
 *
 * https://github.com/sierpito/demo-ormlite-with-sqlcipher/blob/master/src/main/java/com/demo/sqlcipher/User.java
 *
 * tools
 *  rgb -> html
 *  https://www.easycalculation.com/colorconverter/rgb-coder.php
 *
 *  xml to code
 *  1. (很好用) https://www.buzzingandroid.com/tools/android-layout-finder/
 *  2. (不好用) http://www.theappguruz.com/tag-tools/xml-layout/index.php
 *
 * json formatter
 *  1. (最方便) http://jsonviewer.stack.hu/
 *  2. http://www.jsoneditoronline.org/
 *  3. https://jsonformatter.curiousconcept.com/
 *
 *  android button maker
 *  http://angrytools.com/android/button/
 *
 *  parcelabler
 *  http://www.parcelabler.com/
 *
 *  dpi calculator
 *      http://jennift.com/dpical.html
 *      http://stackoverflow.com/questions/29957180/android-multiple-screensize-support
 *      http://android-developers.blogspot.tw/2011/07/new-tools-for-managing-screen-sizes.html
 *      http://stackoverflow.com/questions/26521115/how-to-know-smallest-widthsw-of-android-device
 *
 *      values-sw320dp-land
 *      values-sw360dp-land
 *      values-sw480dp-land
 *      values-sw600dp-land
 *      values-sw700dp-land
 *      values-sw800dp-land
 *
 *      Nexus 4x
 *      Nexus 5x
 *      Nexus 6p (Priority: 2 for 手機測試)
 *      values-sw360dp-land
 *
 *      Nexus 7 (Priority: 4)
 *      values-sw600dp-land
 *
 *      Nexus 9 (Priority: 3)
 *      values-sw700dp-land
 *
 *      Nexus 10 (Priority: 1)
 *      values-sw800dp-land
 *
 *      Examples:
 *          https://github.com/commonsguy/cw-advandroid
 *
 *          http://blog.csdn.net/offbye/article/details/48658097
 *
 *          [Perfect Resource Image Size & DPI for any Android device]
 *          http://www.tivix.com/blog/perfect-resource-image-size-dpi-for-any-android-de/
 *
 *          [提供資源-說明 Android 如何配對 resource]
 *          https://developer.android.com/guide/topics/resources/providing-resources.html#ResourceTypes
 *
 * http://203.66.65.139/test/f_sample.png
 *
 * Handler
 *  http://givemepass-blog.logdown.com/posts/296606-how-to-use-a-handler
 *
 * online download apk
 *  http://apps.evozi.com/apk-downloader
 *
 *  http://droid-toolbox.com/tags/UI
 *
 *  context.getResources().getColor(R.color.colorGreen);
 */
public class Global extends Application {

    private static final String TAG = "Global";
    private static Context context;
    private static Cache cache;
    private static Config config;
    private static Human loginedUser;

    private static Human editedHuman;
    private static Human originEditedHuman;
    private static List<Human> humanList;
    private static int editedFingerResId;
    private static int editMode;

    public static Context getContext() {
        return context;
    }

    public static String getLoginedUserName()  {
        return loginedUser == null ? "" : loginedUser.name;
    }

    public static Human getLoginedUser()  {
        return loginedUser;
    }
    public static void setLoginedUser(Human newVal)  {
        loginedUser = newVal;
    }

    public static int getEditMode() {
        return editMode;
    }

    public static void setEditMode(int editMode) {
        Global.editMode = editMode;
    }

    public static Cache getCache()
    {
        if(cache == null)
        {
            cache = new Cache(Global.getContext());
        }
        return cache;
    }

    public static Config getConfig()
    {
        if(config == null)
        {
            config = new Config(Global.getContext());
        }
        return config;
    }

    public static Human getEditedHuman() {
        return editedHuman;
    }
    public static Human getOriginEditedHuman() {
        return originEditedHuman;
    }

    public static void setEditedHuman(Human editedHuman) {
        Global.editedHuman = editedHuman;
        Global.originEditedHuman = Cloner.deepClone(editedHuman);
    }

    public static int getEditedFingerResId() {
        return editedFingerResId;
    }

    public static void setEditedFingerResId(int editedFingerResId) {
        Global.editedFingerResId = editedFingerResId;
    }

    public static List<Human> getHumanList() {
        if(humanList == null)
        {
            humanList = new ArrayList<Human>();
        }
        return humanList;
    }

    public static void setHumanList(List<Human> humanList) {

        // 20160523 Norman, 原本邏輯
        // Global.humanList = humanList;

        // 20160523 Norman, 業主要求針對使用者進行排序
        List<Human> result = new ArrayList<Human>();

        if(humanList != null)
        {
            List<List<Human>> allHumans = new ArrayList<List<Human>>();

            //  在本機有指紋資料的管理者
            List<Human> humans_1 = new ArrayList<Human>();
            //  在本機無指紋資料的管理者
            List<Human> humans_2 = new ArrayList<Human>();
            //  一般使用者
            List<Human> humans_3 = new ArrayList<Human>();

            for(Human human:humanList)
            {
                if(human.isManager())
                {
                    if(human.hasDatFile())
                    {
                        humans_1.add(human);
                    }
                    else
                    {
                        humans_2.add(human);
                    }
                }
                else
                {
                    humans_3.add(human);
                }
            }

            allHumans.add(humans_1);
            allHumans.add(humans_2);
            allHumans.add(humans_3);

            // how to sort a list
            // http://www.ewdna.com/2008/10/list.html
            for(List<Human> humans:allHumans)
            {
                // Collections.sort(humans, humanComparator);
                Collections.sort(humans, humanComparatorByBindId);
            }

            for(List<Human> humans:allHumans)
                result.addAll(humans);

            setManagerIfExist(result);
        }

        Global.humanList = result;
    }

    /**
     * 20160620
     *  預設 第一位使用者為 manager
     */
    private static void setManagerIfExist(List<Human> humans)
    {
        if(humans.size() == 0)return;

        boolean found = false;
        for(Human human:humans)
        {
            if(human.isManager())
            {
                found = true;
                break;
            }
        }

        if(!found)
        {
            humans.get(0).is_manager = "first_human_as_manager";
        }
    }

    private static Comparator<Human> humanComparator = new Comparator<Human>() {
        public int compare(Human lhs, Human rhs)
        {
            // // 剛剛詢問了一下 當初定議會由server給order by 請確認有留下這個欄位 這是第一順位 第二順位是使用姓名排序
            //  http://stackoverflow.com/questions/4258700/collections-sort-with-multiple-fields
            int result = lhs.createdAt.compareTo(rhs.createdAt);
            return result != 0 ? result : lhs.name.compareTo(rhs.name);
        }
    };

    private static Comparator<Human> humanComparatorByBindId = new Comparator<Human>() {
        public int compare(Human lhs, Human rhs)
        {
            try
            {
                String lBindId = lhs.bind_id;
                String rBindId = rhs.bind_id;

                if(TextUtils.isEmpty(lBindId)) lBindId = "";
                if(TextUtils.isEmpty(rBindId)) rBindId = "";

                // 20160620
                // 依據工號排序
                return lBindId.compareTo(rBindId);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw e;
            }
        }
    };

    public static String getAppName() {
        return context.getString(R.string.app_name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        initIceNet(context, Global.getConfig().getBaseUrl());

        Global.registerEmailAlarm();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                e.printStackTrace(); // not all Android versions will print the stack trace automatically

                Global.getCache().createRunningLog(RunningLog.CATEGORY_DATA_MAINTAIN, e);

//                System.exit(1); // kill off the crashed app
            }
        });

        bootstrap();


    }

    // ----------------------------------------

    public static void startServices(Context context, Class<?> clazz)
    {
        if(!isMyServiceRunning(context, clazz))
        {
            context.startService(new Intent(context, clazz));
        }
    }

    private static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public static void initIceNet(Context context, String baseUrl) {

        IceNetConfig config = new IceNetConfig.Builder()
                .setBaseUrl(baseUrl)
                .setContext(context)
                .build();

        IceNet.init(config);


    }

    private void bootstrap()
    {
        boolean isFirstTimeActived = !PreferencesUtils.getBoolean(PreferencesKey.AP_DEVICE_CREATED);

        try
        {
            if(isFirstTimeActived)
            {
                new WsApDeviceR2(Global.getContext()).execute().promise().done(new DoneCallback<WsApDeviceR2.Response>() {
                    @Override
                    public void onDone(WsApDeviceR2.Response result) {

                        boolean hasData = result.result.success && result.data != null && !TextUtils.isEmpty(result.data.id);

                        if(hasData)
                        {
                            // 查到資料，更新

                            // 當App改版或使用一段時間後，推波的push_token、作業系統版本號的platform_type有可能會改變。所以在程式啟動時，使用更新設備資訊。
                            new WsApDeviceU(Global.getContext()).execute().done(new DoneCallback<WsApDeviceU.Response>() {
                                @Override
                                public void onDone(WsApDeviceU.Response result) {
                                    if (result.result.success)
                                        PreferencesUtils.setBoolean(PreferencesKey.AP_DEVICE_CREATED, true);
                                }
                            });
                        }
                        else
                        {
                            // 查無資料，新增

                            // 程式第一次被啟動時 call api (ap_device C)
                            new WsApDeviceC(Global.getContext()).execute().promise().done(new DoneCallback<WsApDeviceC.Response>() {
                                @Override
                                public void onDone(WsApDeviceC.Response result) {
                                    if(result.result.success)
                                        PreferencesUtils.setBoolean(PreferencesKey.AP_DEVICE_CREATED, true);
                                }
                            });
                        }
                    }
                });
            }
            else
            {
                // 當App改版或使用一段時間後，推波的push_token、作業系統版本號的platform_type有可能會改變。所以在程式啟動時，使用更新設備資訊。
                new WsApDeviceU(Global.getContext()).execute();
            }
        }
        catch (Exception ex)
        {
            Log.e(TAG, ex.getMessage());

        }
    }

    private static boolean debugEnabled = false;

    public static boolean isDebugEnabled()
    {
        return debugEnabled;
    }

    private static Activity currentActivity = null;

    public static Activity getActivity()
    {
        return currentActivity;
    }

    public static void setActivity(Activity activity)
    {
        currentActivity = activity;
    }

    public static void setFullscreen()
    {
        try
        {
            Activity currentActivity = getActivity();

            if(currentActivity != null && currentActivity instanceof MainActivity)
            {
                ((MainActivity)currentActivity).setFullscreen();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * adb shell dumpsys alarm
     *
     * 測試使用哪種方式比較穩定
     *
     * http://blog.csdn.net/u010142437/article/details/22078095
     * http://stackoverflow.com/questions/11681095/cancel-an-alarmmanager-pendingintent-in-another-pendingintent
     * http://blog.csdn.net/bingshushu/article/details/50433643
     * http://porterxie.blog.51cto.com/1787765/1436097
     */
    public static void registerEmailAlarm()
    {
        // 每天執行一次指定的Alarm, 發送 Service 請求
//        EmailService1.register(context);

        // 每天執行一次指定的Alarm, 發送 Broadcast 請求
//        EmailReceiver1.register(context);

        // 每秒執行偵測是否滿足發送Email的指定時間，如果滿足就發送Email
//        EmailService2.register(context);

        // 每秒執行一次指定的Alarm, 發送 Broadcast 請求，偵測是否滿足發送Email的指定時間，如果滿足就發送Email
        EmailReceiver.register(context);
    }
}
