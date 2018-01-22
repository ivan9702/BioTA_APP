package com.startek.biota.app.utils;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

import com.startek.biota.app.global.Global;

/**
 * Created by skt90u on 2016/3/19.
 */
public class AssetHelper {

    public static String readString(String fileName) {
        String str = null;
        try {
            Context context = Global.getContext();
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            str = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return str;
    }
}
