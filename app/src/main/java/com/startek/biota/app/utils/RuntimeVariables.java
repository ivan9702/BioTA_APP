package com.startek.biota.app.utils;

import android.content.Context;

import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;

import java.util.Calendar;

/**
 * Created by skt90u on 2016/4/18.
 */
public class RuntimeVariables
{
    private Context context;

    public RuntimeVariables()
    {
        this.context = Global.getContext();
    }

    public String appname()
    {
        return context.getString(R.string.app_name);
    }

    public String date(String format)
    {
        return Converter.toString(Calendar.getInstance().getTime(), format);
    }
}
