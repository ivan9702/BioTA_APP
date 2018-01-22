package com.startek.biota.app.utils;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.startek.biota.app.global.Global;

import java.util.Map;



public class PreferencesUtils {

    public static final String OBJECTS_SAME_STRING = "";
    public static final String EVENT_PARAM_VALUE_NO = "0";
    public static final String EVENT_PARAM_VALUE_YES = "1";

    private static SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(Global.getContext());
    }

    public static String getString(String key) {
        String base64 = getSharedPreferences().getString(key, OBJECTS_SAME_STRING);
        if (TextUtils.isEmpty(base64)) {
            return null;
        }
        return EncodeHelper.decodeToString(base64);
    }

    public static void setString(String key, String value) {
        String base64 = null;
        if (value != null) {
            base64 = EncodeHelper.encodeFromString(value);
        }
        getSharedPreferences().edit().putString(key, base64).commit();
    }

    public static long getLong(String key) {
        String base64 = getSharedPreferences().getString(key, EVENT_PARAM_VALUE_NO);
        if (base64.equals(EVENT_PARAM_VALUE_NO)) {
            return 0;
        }
        return EncodeHelper.decodeToLong(base64);
    }

    public static void setLong(String key, long value) {
        getSharedPreferences().edit().putString(key, EncodeHelper.encodeFromLong(value)).commit();
    }

    public static int getInt(String key) {
        String base64 = getSharedPreferences().getString(key, EVENT_PARAM_VALUE_NO);
        if (base64.equals(EVENT_PARAM_VALUE_NO)) {
            return 0;
        }
        return EncodeHelper.decodeToInt(base64);
    }

    public static int getInt(String key, int defValue) {
        String defString = String.valueOf(defValue);
        String base64 = getSharedPreferences().getString(key, defString);
        return defString.equals(base64) ? defValue : EncodeHelper.decodeToInt(base64);
    }

    public static void setInt(String key, int value) {
        getSharedPreferences().edit().putString(key, EncodeHelper.encodeFromInt(value)).commit();
    }

    public static float getFloat(String key) {
        return EncodeHelper.decodeToFloat(getSharedPreferences().getString(key, EVENT_PARAM_VALUE_NO));
    }

    public static void setFloat(String key, Float value) {
        getSharedPreferences().edit().putString(key, EncodeHelper.encodeFromFloat(value.floatValue())).commit();
    }

    public static double getDouble(String key) {
        return EncodeHelper.decodeToFDouble(getSharedPreferences().getString(key, EVENT_PARAM_VALUE_NO));
    }

    public static void setDouble(String key, double value) {
        getSharedPreferences().edit().putString(key, EncodeHelper.encodeFromDouble(value)).commit();
    }

    public static boolean getBoolean(String key) {
        return getSharedPreferences().getBoolean(key, false);
    }

    public static void setBoolean(String key, boolean value) {
        getSharedPreferences().edit().putBoolean(key, value).commit();
    }

    public static boolean contains(String key) {
        return getSharedPreferences().contains(key);
    }

    public static void remove(String key) {
        Editor editor = getSharedPreferences().edit();
        editor.remove(key);
        editor.commit();
    }

    public static void resetAll() {
        SharedPreferences pref = getSharedPreferences();
        Editor editor = pref.edit();
        Map<String, ?> keys = pref.getAll();
        if (keys.size() > 0) {
            for (String key : keys.keySet()) {
                editor.remove(key);
            }
            editor.commit();
        }
    }


}
