package com.startek.biota.app.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * https://developer.android.com/guide/topics/resources/providing-resources.html#ResourceTypes
 *
 * Supporting Multiple Screens
 * https://developer.android.com/guide/practices/screens_support.html#overview
 *
 * http://android-developers.blogspot.tw/2011/07/new-tools-for-managing-screen-sizes.html
 */
public class DirectoryQualifierHelper
{
    private static final String TAG = "DirectoryQualifier";

    public static final int RESOURCE_ANIMATOR = 1 << 0;
    public static final int RESOURCE_ANIM = 1 << 1;
    public static final int RESOURCE_DRAWABLE = 1 << 2;
    public static final int RESOURCE_MIPMAP = 1 << 3;
    public static final int RESOURCE_LAYOUT = 1 << 4;
    public static final int RESOURCE_MENU = 1 << 5;
    public static final int RESOURCE_RAW = 1 << 6;
    public static final int RESOURCE_VALUES = 1 << 7;
    public static final int RESOURCE_XML = 1 << 8;

    private static HashMap<Integer, String> resourceMap;

    static {
        resourceMap = new HashMap<Integer, String>();
        resourceMap.put(RESOURCE_ANIMATOR, "animator");
        resourceMap.put(RESOURCE_ANIM, "anim");
        resourceMap.put(RESOURCE_DRAWABLE, "drawable");
        resourceMap.put(RESOURCE_MIPMAP, "mipmap");
        resourceMap.put(RESOURCE_LAYOUT, "layout");
        resourceMap.put(RESOURCE_MENU, "menu");
        resourceMap.put(RESOURCE_RAW, "raw");
        resourceMap.put(RESOURCE_VALUES, "values");
        resourceMap.put(RESOURCE_XML, "xml");
    }

    public static final int QUALIFIER_MCCMNC = 1 << 0;
    public static final int QUALIFIER_LANGUAGEREGIONCODE = 1 << 1;
    public static final int QUALIFIER_SMALLESTWIDTH = 1 << 2;
    public static final int QUALIFIER_AVAILABLETWIDTH = 1 << 3;
    public static final int QUALIFIER_AVAILABLETHEIGHT = 1 << 4;
    public static final int QUALIFIER_SCREENSIZE = 1 << 5;
    public static final int QUALIFIER_SCREENORIENTATION = 1 << 6;
    public static final int QUALIFIER_SCREENPIXELDENSITY = 1 << 7;
    public static final int QUALIFIER_APILEVEL = 1 << 8;

    private static HashMap<Integer, String> qualifierMap;

    static {
        qualifierMap = new HashMap<Integer, String>();
        qualifierMap.put(QUALIFIER_MCCMNC, "getMccMnc");
        qualifierMap.put(QUALIFIER_LANGUAGEREGIONCODE, "getLanguageRegionCode");
        qualifierMap.put(QUALIFIER_SMALLESTWIDTH, "getSmallestWidth");
        qualifierMap.put(QUALIFIER_AVAILABLETWIDTH, "getAvailabletWidth");
        qualifierMap.put(QUALIFIER_AVAILABLETHEIGHT, "getAvailabletHeight");
        qualifierMap.put(QUALIFIER_SCREENSIZE, "getScreenSize");
        qualifierMap.put(QUALIFIER_SCREENORIENTATION, "getScreenOrientation");
        qualifierMap.put(QUALIFIER_SCREENPIXELDENSITY, "getScreenPixelDensity");
        qualifierMap.put(QUALIFIER_APILEVEL, "getApiLevel");
    }

    private Context context;

    public String get(int resource, int qualifiers) {

        List<String> tokens = new ArrayList<String>();

        // get directory name

        String directoryName = "";

        for (Map.Entry<Integer,String> entry:resourceMap.entrySet()) {
            if(entry.getKey() == resource)
            {
                directoryName = entry.getValue();
                break;
            }
        }

        if(TextUtils.isEmpty(directoryName))
            Log.e(TAG, String.format("ResourceNotFound(%d)", resource));
        else
            tokens.add(directoryName);

        List<Integer> qualifierKeys = new ArrayList<Integer>(qualifierMap.keySet());
        Collections.sort(qualifierKeys);

        for (Integer qualifierKey:qualifierKeys) {
            int key = qualifierKey;
            String methodName = qualifierMap.get(qualifierKey);
            if((qualifiers & key) == key)
            {
                java.lang.reflect.Method method = null;
                String value = null;

                try
                {
                    method = this.getClass().getMethod(methodName);

                    if(method == null)continue;

                    try {
                        value = (String) method.invoke(this);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException ex) {
                        ex.printStackTrace();
                    }

                    if(TextUtils.isEmpty(value))continue;

                    tokens.add(value);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        String result = TextUtils.join("-", tokens);

        return result;
    }

    public DirectoryQualifierHelper(Context context)
    {
        this.context = context;
    }

    /**
     * 行動裝置國家/地區代碼 (MobileCountryCode)
     * 行動裝置網路代碼 (MobileNetworkCode)
     */
    public String getMccMnc()
    {
        int mcc = context.getResources().getConfiguration().mcc;
        int mnc = context.getResources().getConfiguration().mnc;
        return String.format("mnc%d-mcc%d", mnc, mcc);
    }

    /**
     * 語言和區域
     */
    public String getLanguageRegionCode()
    {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        // TODO: 目前不知道如何使用 Locale 取得區域
        return String.format("%s", language);
    }

    /**
     * 螢幕的「最小寬度」
     */
    public String getSmallestWidth()
    {
        int smallestScreenWidthDp = context.getResources().getConfiguration().smallestScreenWidthDp;

        // sw<N>dp, e.g. sw320dp
        return String.format("sw%ddp", smallestScreenWidthDp);
    }

    /**
     * 螢幕的「可用寬度」
     */
    public String getAvailabletWidth()
    {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        MeasureHelper measureHelper = new MeasureHelper(context);
        float widthWithDp = measureHelper.convertPixelsToDp(width);
        float heightWithDp = measureHelper.convertPixelsToDp(height);

        // w<N>dp, e.g. w720dp
        return String.format("w%ddp", (int)widthWithDp);
    }

    /**
     * 螢幕的「可用高度」
     */
    public String getAvailabletHeight()
    {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        MeasureHelper measureHelper = new MeasureHelper(context);
        float widthWithDp = measureHelper.convertPixelsToDp(width);
        float heightWithDp = measureHelper.convertPixelsToDp(height);

        // h<N>dp, e.g. h720dp
        return String.format("h%ddp", (int)heightWithDp);
    }

    /**
     * 螢幕大小
     */
    public String getScreenSize()
    {
        int sizeMask = context.getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;

        /*
            small：
                與低密度 QVGA 螢幕大小相似的螢幕 。小螢幕的最低版面配置大小約 320x426 dp 單位。 範例包含 QVGA 低密度和 VGA 高密度。
            normal：
                與中密度 HVGA 螢幕大小相似的螢幕。 一般螢幕的最低版面配置大小約 320x470 dp 單位。 這類螢幕的範例有 WQVGA 低密度、HVGA 中密度、WVGA高密度。
            large：
                與中密度 VGA 螢幕大小相似的螢幕。 大螢幕的最低版面配置大小約 480x640 dp 單位。 範例包含 VGA 和 WVGA 中密度螢幕。
            xlarge：
                比傳統中密度 HVGA 螢幕大很多的螢幕。 超大螢幕的最低版面配置大小約 720x960 dp 單位。 在大多數情況下，使用超大螢幕的裝置由於尺寸過大無法放入口袋，因此最有可能是平板電腦樣式的裝置。 已新增至 API 級別 9。
         */

        switch (sizeMask)
        {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return "small";
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return "normal";
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                return "large";
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                return "xlarge";

            default:
                return "";
        }
    }

    /**
     * 螢幕方向
     */
    public String getScreenOrientation()
    {
        int orientation = context.getResources().getConfiguration().orientation;

        switch (orientation)
        {
            case Configuration.ORIENTATION_LANDSCAPE:
                return "land"; // 裝置的方向為橫向 (水平)
            case Configuration.ORIENTATION_PORTRAIT:
                return "port"; // 裝置的方向為直向 (垂直)

            default:
                return "";
        }
    }

    /**
     * 螢幕像素密度
     */
    public String getScreenPixelDensity()
    {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        switch(metrics.densityDpi){
            case DisplayMetrics.DENSITY_LOW:
                return "ldpi";
            case DisplayMetrics.DENSITY_MEDIUM:
                return "mdpi";
            case DisplayMetrics.DENSITY_HIGH:
                return "hdpi";
            case DisplayMetrics.DENSITY_XHIGH:
                return "xhdpi";
            case DisplayMetrics.DENSITY_XXHIGH:
                return "xxhdpi";
            case DisplayMetrics.DENSITY_XXXHIGH:
                return "xxhdpi";
            case DisplayMetrics.DENSITY_TV:
                return "tvdpi";
            default:
                return "";
        }
    }



    /**
     * 平台版本 (API 級別)
     *
     * e.g. v3, v4, v7
     *
     * http://stackoverflow.com/questions/3423754/retrieving-android-api-version-programmatically
     * https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels
     */
    public String getApiLevel() {
        // https://developer.android.com/guide/topics/manifest/uses-sdk-element.html#ApiLevels
        return String.format("v%d", Build.VERSION.SDK_INT);
    }
}
