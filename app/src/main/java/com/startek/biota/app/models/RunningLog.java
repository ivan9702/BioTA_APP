package com.startek.biota.app.models;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.startek.biota.app.utils.Converter;
import com.startek.biota.app.utils.MyCsvWriter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 運行記錄
 *
 * 人員進入 - 登入成功，登入失敗
 * 使用者新增，刪除，修改紀錄
 * 主機同步紀錄
 */
public class RunningLog
{
    public static final int CATEGORY_USER_IN_OUT    = 1 << 0;
    public static final int CATEGORY_DATA_MAINTAIN  = 1 << 1;
    public static final int CATEGORY_SYNC_SERVER    = 1 << 2;
    public static final int CATEGORY_ACCESS_CONTROL = 1 << 3;

//    public static final int CATEGORY_LOGIN    = RunningLog.CATEGORY_USER_IN_OUT;
//    public static final int CATEGORY_SETTING    = RunningLog.CATEGORY_DATA_MAINTAIN;
//    public static final int CATEGORY_SYNC_DATA    = RunningLog.CATEGORY_DATA_MAINTAIN;
//    public static final int CATEGORY_USER_MANAGER    = RunningLog.CATEGORY_DATA_MAINTAIN;

    @DatabaseField(columnName = "id", generatedId = true, allowGeneratedIdInsert = true)
    private int id;

    // 類型
    @DatabaseField
    public int category;

    @DatabaseField(dataType = DataType.DATE_LONG)
    public Date date;

    @DatabaseField
    public String event;

    @DatabaseField
    public String operator;

    @DatabaseField
    public String description;

    @DatabaseField
    public String result;

    @DatabaseField
    public boolean success;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RunningLog{");
        sb.append("id='").append(id).append('\'');
        sb.append(", category='").append(category).append('\'');
        sb.append(", date='").append(Converter.toString(date, Converter.DateTimeFormat.YYYYMMddHHmmssSSSZ)).append('\'');
        sb.append(", event='").append(event).append('\'');
        sb.append(", operator='").append(operator).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", result='").append(result).append('\'');
        sb.append(", success='").append(success).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
