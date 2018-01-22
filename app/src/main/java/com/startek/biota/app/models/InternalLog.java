package com.startek.biota.app.models;

import android.app.Activity;
import android.util.Log;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.utils.Converter;
import com.startek.biota.app.utils.MyCsvWriter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by skt90u on 2016/6/8.
 */
public class InternalLog implements MyCsvWriter.CsvLine {

    @DatabaseField(columnName = "id", generatedId = true, allowGeneratedIdInsert = true)
    private int id;

    @DatabaseField(dataType = DataType.DATE_LONG)
    public Date date;

    @DatabaseField
    public String activity;

    @DatabaseField
    public String tag;

    @DatabaseField
    public String function;

    @DatabaseField
    public String message;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RunningLog{");
        sb.append("id='").append(id).append('\'');
        sb.append(", date='").append(Converter.toString(date, Converter.DateTimeFormat.YYYYMMddHHmmssSSSZ)).append('\'');
        sb.append(", activity='").append(activity).append('\'');
        sb.append(", tag='").append(tag).append('\'');
        sb.append(", function='").append(function).append('\'');
        sb.append(", message='").append(message).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public List<String> getCsvEntries() {
        // 2016-04-16 20:04:11,verify,0327,server_action,true
        List<String> result = new ArrayList<String>();

        result.add(Converter.toString(date, Converter.DateTimeFormat.YYYYMMddHHmmss));
        result.add(activity);
        result.add(tag);
        result.add(function);
        result.add(message);

        return result;
    }

    public static int d(Activity activity, String tag, String function, String message)
    {
        InternalLog internalLog = new InternalLog();

        internalLog.date = Calendar.getInstance().getTime();
        internalLog.activity = activity.getClass().getSimpleName();
        internalLog.tag = tag;
        internalLog.function = function;
        internalLog.message = message;
        Global.getCache().createInternalLog(internalLog);

        return Log.d(tag, internalLog.toString());
    }
}
