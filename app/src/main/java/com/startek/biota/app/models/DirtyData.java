package com.startek.biota.app.models;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.startek.biota.app.utils.Converter;

import java.util.Date;

/**
 * Created by skt90u on 2016/4/2.
 */
public class DirtyData {

    public static final int ACTION_CREATE = 0;
    public static final int ACTION_UPDATE = 1;
    public static final int ACTION_DELETE = 2;

    public static final int STATE_PENDING = 0;
    public static final int STATE_IN_PROCESSED = 1;
    public static final int STATE_SUCCESS = 2;
    public static final int STATE_FAILURE = 3;

    @DatabaseField(columnName = "id", generatedId = true, allowGeneratedIdInsert = true)
    private int id;

    @DatabaseField
    public String className;

    @DatabaseField
    public int action;

    @DatabaseField
    public String json;

    @DatabaseField
    public String result;

    @DatabaseField
    public int state;

    @DatabaseField(dataType = DataType.DATE_LONG)
    public Date date;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DirtyData{");
        sb.append(String.format(" id=%d,", id));
        sb.append(String.format(" className=%s,", className));
        sb.append(String.format(" action=%s,", actionToString(action)));
        sb.append(String.format(" json=%s", json));
        sb.append(String.format(" result=%s,", result));
        sb.append(String.format(" state=%s,", stateToString(state)));
        sb.append(String.format(" date=%s", Converter.toString(date, Converter.DateTimeFormat.YYYYMMddHHmmssSSSZ)));
        sb.append("}");
        return sb.toString();
    }

    public static String actionToString(int action)
    {
        return action == ACTION_CREATE ? "INSERT" :
               action == ACTION_UPDATE ? "UPDATE" :
               action == ACTION_DELETE ? "DELETE" : String.format("UNKNOWN(%d)", action);
    }

    public static String stateToString(int state)
    {
        return state == STATE_PENDING ? "PENDING" :
               state == STATE_IN_PROCESSED ? "IN_PROCESSED" :
               state == STATE_SUCCESS ? "SUCCESS" :
               state == STATE_FAILURE ? "FAILURE" : String.format("UNKNOWN(%d)", state);
    }
}
