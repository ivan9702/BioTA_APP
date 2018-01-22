package com.startek.biota.app.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.startek.biota.app.R;
import com.startek.biota.app.database.Cache;
import com.startek.biota.app.database.SQLiteOrmWithCipherHelper;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.network.webservices.CommonResponse;
import com.startek.biota.app.network.webservices.WsAnnouncementR;
import com.startek.biota.app.network.webservices.WsReminderR;

import org.jdeferred.Promise;
import org.jdeferred.multiple.MultipleResults;
import org.jdeferred.multiple.OneResult;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by skt90u on 2016/3/19.
 */
public class Converter {

    public class DateTimeFormat
    {
        public static final String YYYYMMdd = "yyyy-MM-dd";
        public static final String YYYYMMdd_E = "yyyy/MM/dd E";
        public static final String HHmmss = "HH:mm:ss";
        public static final String HHmm = "HH:mm";
        public static final String YYYYMMddHHmmss= "yyyy-MM-dd HH:mm:ss";
        public static final String YYYYMMddHHmmssSSSZ = "yyyy-MM-dd HH:mm:ss.SSSZ";
        public static final String DEFAULT = YYYYMMdd;
        public static final String FileName = "yyyyMMddHHmmss";
        public static final String ServerDate = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    }

    private static final String TAG = "Converter";

    public static int[] toIntArray(String str)
    {
        String[] tokens = str.split(",");

        int result[] = new int[tokens.length];

        for(int i=0, len = tokens.length; i<len; i++)
        {
            result[i] = Integer.valueOf(tokens[i]);
        }

        return result;
    }

    public static String toString(int[] tokens)
    {
        String result = "";
        for(int i=0, len = tokens.length; i<len; i++)
        {
            result += String.format("%d", tokens[i]);
            if(i != len-1)
            {
                result += ",";
            }
        }
        return result;
    }



    public static Date toDate(String dateString) {
        return toDate(dateString, DateTimeFormat.DEFAULT);
    }

    public static CharSequence[] toCharSequence(List<String> list)
    {
        return list.toArray(new CharSequence[list.size()]);
    }

    public static Calendar toCalendar(String dateString, String format)
    {
        Date date = toDate(dateString, format);

        if(date != null)
        {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return cal;
        }

        return null;
    }

    public static Calendar toCalendar(String dateString)
    {
        return toCalendar(dateString, DateTimeFormat.DEFAULT);
    }

    public static String toString(Calendar calendar) {
        return toString(calendar.getTime());
    }

    public static String toString(Date date) {
        return toString(date, DateTimeFormat.DEFAULT);
    }

    public static String getErrors(MultipleResults results)
    {
        boolean hasError = false;

        List<String> errors = new ArrayList<String>();

        for(int i=0; i<results.size(); i++)
        {
            CommonResponse commonResponse = (CommonResponse)results.get(i).getResult();

            if(!commonResponse.result.success)
            {
                hasError = true;

                errors.add(commonResponse.result.message);
            }
        }

        //return TextUtils.join(",", errors);

        if(hasError)
        {
            String result = TextUtils.join(",", errors);

            if(TextUtils.isEmpty(result))
            {
                result = "未指定錯誤";
            }

            return result;
        }
        else
        {
            return "";
        }
    }

    public static Date toDate(String dateString,String format) {

        try
        {
            if(dateString==null) return null;

            ParsePosition pos = new ParsePosition(0);

            SimpleDateFormat simpledateformat = new SimpleDateFormat(format);

            return simpledateformat.parse(dateString, pos);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();

            Log.e(TAG, String.format("Faile to convert string to date from string(%s) with format(%s", dateString, format));

            return null;
        }
    }

    public static String toString(Date date,String format) {

//        if(date==null) return null;
        if(date==null) return "";

        SimpleDateFormat simpledateformat = new SimpleDateFormat(format);

        return simpledateformat.format(date);
    }

    public static String fingerBtnIdToChinese(int fingerBtnId)
    {
        FingerInfo fingerInfo = FingerInfo.getByFingerBtnId(fingerBtnId);

        return fingerInfo == null ? "" : fingerInfo.chinese;
    }

    public static int englishToFingerId(String which)
    {
        int buttonId = englishToFingerBtnId(which);

        // 右手大拇指起算 左手小指結束 (0~9)

        switch (buttonId)
        {
            case R.id.r_thumb: return 0;
            case R.id.r_index: return 1;
            case R.id.r_middle: return 2;
            case R.id.r_ring: return 3;
            case R.id.r_pinky: return 4;

            case R.id.l_thumb: return 5;
            case R.id.l_index: return 6;
            case R.id.l_middle: return 7;
            case R.id.l_ring: return 8;
            case R.id.l_pinky: return 9;
        }

        return -1;
    }

    public static int englishToFingerBtnId(String which)
    {
        FingerInfo fingerInfo = FingerInfo.getByWhich(which);

        return fingerInfo == null ? -1 : fingerInfo.fingerBtnId;
    }

    public static String fingerBtnIdToEnglish(int fingerBtnId)
    {
        FingerInfo fingerInfo = FingerInfo.getByFingerBtnId(fingerBtnId);

        return fingerInfo == null ? "" : fingerInfo.which;
    }

    public static Promise[] toArray(List<Promise> promises)
    {
        return (Promise[]) promises.toArray(new Promise[promises.size()]);
    }

    public static String getSimpleName(String className)
    {
        Class clazz = getClass(className);

        return clazz == null ? "" : clazz.getSimpleName();
    }

    public static Class getClass(String className)
    {
        // return Class.forName(dirtyData.className).getSimpleName();

        List<Class> classList = Cache.getTableList();

        for(Class clazz:classList)
        {
            if(clazz.toString().equals(className))
                return clazz;
        }

        return null;
    }

    /**
     * https://coderwall.com/p/ab5qha/convert-json-string-to-pretty-print-java-gson
     */
    public static String toPrettyFormat(String jsonString)
    {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(jsonString).getAsJsonObject();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }

    public static String methodToString(int method)
    {
        switch (method)
        {
            case Request.Method.DEPRECATED_GET_OR_POST: return "DEPRECATED_GET_OR_POST";

            case Request.Method.GET: return "GET";
            case Request.Method.POST: return "POST";
            case Request.Method.PUT: return "PUT";
            case Request.Method.DELETE: return "DELETE";
            case Request.Method.HEAD: return "HEAD";

            case Request.Method.OPTIONS: return "OPTIONS";
            case Request.Method.TRACE: return "TRACE";
            case Request.Method.PATCH: return "PATCH";
        }

       return String.format("UNKNOWN(%d)", method);
    }

    public static String encodeBase64String(byte[] bytes)
    {
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static byte[] decodeBase64String(String base64String)
    {
        return Base64.decode(base64String, Base64.DEFAULT);
    }

    public static String getMessage(Throwable throwable)
    {
        Throwable cause = throwable;

        while(cause.getCause() != null) {
            cause = cause.getCause();
        }

        String result = cause.getMessage();

        if(TextUtils.isEmpty(result))
        {
            result = cause.getClass().getSimpleName();
        }

        return result;
    }

    public static String getStackTrace(Throwable throwable)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString(); // stack trace as a string
    }
}
