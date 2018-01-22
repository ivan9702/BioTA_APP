package com.startek.biota.app.models;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.startek.biota.app.utils.Converter;
import com.startek.biota.app.utils.MyCsvWriter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EmailLog implements MyCsvWriter.CsvLine
{
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

    @Override
    public List<String> getCsvEntries() {
        // 2016-04-16 20:04:11,verify,0327,server_action,true
        List<String> result = new ArrayList<String>();

        result.add(Converter.toString(date, Converter.DateTimeFormat.YYYYMMddHHmmss));
        result.add(event);
        result.add(operator);
        result.add(description);
        result.add(Boolean.toString(success));

        return result;
    }
}
