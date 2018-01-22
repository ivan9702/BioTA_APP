package com.startek.biota.app.utils;

import android.text.TextUtils;

/**
 * Created by skt90u on 2016/4/24.
 */
public class StrUtils {

    public static String removeBreakLine(String text)
    {
        // http://stackoverflow.com/questions/2163045/how-to-remove-line-breaks-from-a-file-in-java

        // trim NewLine
        // https://en.wikipedia.org/wiki/Newline
        
        return text.replaceAll("\\r|\\n", "");
    }

    public static boolean equals(String lhs, String rhs)
    {
        return (TextUtils.isEmpty(lhs) && TextUtils.isEmpty(rhs))
            ? true
            : TextUtils.equals(lhs, rhs);
    }
}
