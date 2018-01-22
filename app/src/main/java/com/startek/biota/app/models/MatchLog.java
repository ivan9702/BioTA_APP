package com.startek.biota.app.models;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.startek.biota.app.utils.Converter;

import java.util.Date;

public class MatchLog {

    public static final String STimeFormat = Converter.DateTimeFormat.YYYYMMddHHmmssSSSZ;
    public static final String CATEGORY_ENROLL = "Ri";
    public static final String CATEGORY_VERIFY = "Rv";

    @DatabaseField(columnName = "id", generatedId = true, allowGeneratedIdInsert = true)
    private int id;

    @DatabaseField
    public String type;

    @DatabaseField
    public String minutiae;

    @DatabaseField
    public String pic;

    @DatabaseField
    public String humanId;

    @DatabaseField
    public String bind_id;

    @DatabaseField
    public String tagId;

    @DatabaseField(dataType = DataType.DATE_LONG)
    public Date STime;

    @DatabaseField
    public long CTime;

    @DatabaseField
    public int MScore;

    @DatabaseField
    public long MTime;

    @DatabaseField
    public boolean is_success;

    @DatabaseField
    public String client_action;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("MatchLog{");
        sb.append("id='").append(id).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", minutiae='").append(minutiae).append('\''); // 20160704  運行紀錄=>資料維護 詳細描述 拿掉minuatiae
        sb.append(", pic='").append(pic).append('\'');
        sb.append(", humanId='").append(humanId).append('\'');
        sb.append(", bind_id='").append(bind_id).append('\'');
        sb.append(", tagId='").append(tagId).append('\'');
        sb.append(", STime='").append(Converter.toString(STime, STimeFormat)).append('\'');
        sb.append(", CTime='").append(CTime).append('\'');
        sb.append(", MScore='").append(MScore).append('\'');
        sb.append(", MTime='").append(MTime).append('\'');
        sb.append(", is_success='").append(is_success).append('\'');
        sb.append(", client_action='").append(client_action).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
