package com.startek.biota.app.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

import com.startek.biota.app.global.Global;

/**
 * 讀取門禁機(Android Tablet)資訊
 */
public class MobileStatus {

    private static final String TAG = "MobileStatus";


    // ----------------------------------------
    // 設備辨別資料
    // ----------------------------------------

    /**
     * Client端自行產生，設備唯一碼 UUID,如 390EEBE3­F1EB­4FD3­B1FA­D25365AFEDDB
     */
    public static String getDeviceId() {
        Context context = Global.getContext();
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    /**
     * 推播唯一碼
     */
    public static String getPushToken() {
        // 20160328 Norman, 此此階段尚未連接 push server, 只需要傳空值
        return "";
    }

    /**
     * e.g. HTC One (M8)
     * http://stackoverflow.com/questions/7071281/get-android-device-name
     */
    public static String getPlatformType()
    {
        return getDeviceName();
    }
    /**
     * 設備型態 = {ios / android}
     */
    public static String getDeviceType() {
        return "android";
    }

    // ----------------------------------------
    // 地理位置篩選資訊(全球衛星定位系統)，資料來源: 手機 GPS 信號
    // ----------------------------------------

    /**
     * 緯度
     */
    public static double getLatitude() {
        Location location = getLocation();
        return location == null ? 0 : location.getLatitude();
    }

    /**
     * 經度
     */
    public static double getLongitude() {
        Location location = getLocation();
        return location == null ? 0 : location.getLongitude();
    }

    // ----------------------------------------
    // 地理位置篩選資訊(Wifi定位)，資料來源: 手機偵測 Wifi 基地台
    // ----------------------------------------

    /**
     * Wifi 設備唯一碼
     */
    public String getWifiBSSID() {

        WifiInfo wifiInfo = getWifiInfo();

        return (wifiInfo != null && !TextUtils.isEmpty(wifiInfo.getBSSID()))
                ? wifiInfo.getBSSID()
                : null;
    }

    /**
     * Wifi 名稱
     */
    public String getWifiSSID() {

        WifiInfo wifiInfo = getWifiInfo();

        return (wifiInfo != null && !TextUtils.isEmpty(wifiInfo.getSSID()))
                ? wifiInfo.getSSID()
                : null;
    }

    /**
     * 信號強弱度 dBm,範圍 ­51 ~ ­113
     */
    public static int getWifiSignal() {

        Context context = Global.getContext();

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        int state = wifiManager.getWifiState();

        if(state == WifiManager.WIFI_STATE_ENABLED) {

            List<ScanResult> results = wifiManager.getScanResults();

            for (ScanResult result : results) {
                if(result.BSSID.equals(wifiManager.getConnectionInfo().getBSSID())) {
                    int level = WifiManager.calculateSignalLevel(wifiManager.getConnectionInfo().getRssi(),
                            result.level);
                    int difference = level * 100 / result.level;
                    int signalStrangth= 0;
                    if(difference >= 100)
                        signalStrangth = 4;
                    else if(difference >= 75)
                        signalStrangth = 3;
                    else if(difference >= 50)
                        signalStrangth = 2;
                    else if(difference >= 25)
                        signalStrangth = 1;
//                    tv.setText(tv.getText() + "\nDifference :" + difference + " signal state:" + signalStrangth);
                    return difference;
                }
            }
        }

        return -1;
    }



    // ----------------------------------------
    // 地理位置篩選資訊(基地台定位)，資料來源: 手機偵測信號基地台
    // ----------------------------------------

    /**
     * 電信商網路代碼,範圍 1 ~ 65533
     */
    public static int getCellsLac()
    {
        GsmCellLocation gsmCellLocation = getGsmCellLocation();

        return (gsmCellLocation != null)
                ? gsmCellLocation.getLac()
                : -1;
    }

    /**
     * 電信站點代碼,範圍 0 ~ 268435455
     */
    public static int getCellsCid()
    {
        GsmCellLocation gsmCellLocation = getGsmCellLocation();

        return (gsmCellLocation != null)
                ? gsmCellLocation.getCid()
                : -1;
    }

    /**
     * 信號強弱度 dBm,範圍 ­25 ~ ­137
     */
    public static String getCellsSignal() {
        return ""; // 未來會用到的時候再實做
    }

    /**
     * 信號型態:GSM, 2G, 1xRTT, 3G, LTE, 4G等
     *
     * http://androidforums.com/threads/how-to-tell-if-a-phone-will-work-on-t-mobile.706991/
     * http://stackoverflow.com/questions/9283765/how-to-determine-if-network-type-is-2g-3g-or-4g
     */
    public static String getCellsRadio() {

        String result = null;

        Context context = Global.getContext();

        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);

        int networkType = telephonyManager.getNetworkType();

        switch (networkType)
        {
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                result = "NETWORK_TYPE_UNKNOWN";
                break;
            case TelephonyManager.NETWORK_TYPE_GPRS:
                result = "NETWORK_TYPE_GPRS";
                break;
            case TelephonyManager.NETWORK_TYPE_EDGE:
                result = "NETWORK_TYPE_EDGE";
                break;
            case TelephonyManager.NETWORK_TYPE_UMTS:
                result = "NETWORK_TYPE_UMTS";
                break;
            case TelephonyManager.NETWORK_TYPE_CDMA:
                result = "NETWORK_TYPE_CDMA";
                break;

            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                result = "NETWORK_TYPE_EVDO_0";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                result = "NETWORK_TYPE_EVDO_A";
                break;
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                result = "NETWORK_TYPE_1xRTT";
                break;
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                result = "NETWORK_TYPE_HSDPA";
                break;
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                result = "NETWORK_TYPE_HSUPA";
                break;


            case TelephonyManager.NETWORK_TYPE_HSPA:
                result = "NETWORK_TYPE_HSPA";
                break;
            case TelephonyManager.NETWORK_TYPE_IDEN:
                result = "NETWORK_TYPE_IDEN";
                break;
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                result = "NETWORK_TYPE_EVDO_B";
                break;
            case TelephonyManager.NETWORK_TYPE_LTE:
                result = "NETWORK_TYPE_LTE";
                break;
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                result = "NETWORK_TYPE_EHRPD";
                break;


            case TelephonyManager.NETWORK_TYPE_HSPAP:
                result = "NETWORK_TYPE_HSPAP";
                break;
            case 16: //TelephonyManager.NETWORK_TYPE_GSM:
                result = "NETWORK_TYPE_GSM";
                break;
            case 17: //TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                result = "NETWORK_TYPE_TD_SCDMA";
                break;
            case 18: //TelephonyManager.NETWORK_TYPE_IWLAN:
                result = "NETWORK_TYPE_IWLAN";
                break;

            default:
                result = "NETWORK_TYPE_UNKNOWN";
                break;
        }

        return result;
    }

    // ----------------------------------------

    private static Location getLocation()
    {
        Context context = Global.getContext();

        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

//        if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)  &&
//            PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION))
//        {
//            Log.e(TAG, "getLocation lost necessary permission");
//            return null;
//        }

        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        return location;
    }

    private static NetworkInfo getActiveNetworkInfo()
    {
        Context context = Global.getContext();
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo;
    }

    public static boolean isNetworkEnabled() {
        NetworkInfo activeNetworkInfo = getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private static boolean is3gActivated() {
        NetworkInfo activeNetworkInfo = getActiveNetworkInfo();
        return (activeNetworkInfo != null && activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    private static boolean isWifiActivated() {
        NetworkInfo activeNetworkInfo = getActiveNetworkInfo();
        return (activeNetworkInfo != null && activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI);
    }

    private static WifiInfo getWifiInfo()
    {
        if(!isWifiActivated()) return null;

        Context context = Global.getContext();

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        WifiInfo connectionInfo = wifiManager.getConnectionInfo();

        return connectionInfo;
    }

    private static GsmCellLocation getGsmCellLocation()
    {
        Context context = Global.getContext();

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        android.telephony.CellLocation location = telephonyManager.getCellLocation();

        return (location instanceof GsmCellLocation)
                ? (GsmCellLocation) location
                : null;
    }

    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;
        String phrase = "";
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase += Character.toUpperCase(c);
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase += c;
        }
        return phrase;
    }
}
