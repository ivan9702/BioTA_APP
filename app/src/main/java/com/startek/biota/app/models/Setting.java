package com.startek.biota.app.models;

import com.j256.ormlite.field.DatabaseField;

public class Setting {

    public static final int CATEGORY_USER_IN_OUT    = 1 << 0;
    public static final int CATEGORY_DATA_MAINTAIN  = 1 << 1;
    public static final int CATEGORY_SYNC_SERVER    = 1 << 2;
    public static final int CATEGORY_ACCESS_CONTROL = 1 << 3;
    public static final int CATEGORY_ALL            = CATEGORY_USER_IN_OUT | CATEGORY_DATA_MAINTAIN | CATEGORY_SYNC_SERVER | CATEGORY_ACCESS_CONTROL;

    public static final int EDITOR_EDITTEXT      = 1 << 0;
    public static final int EDITOR_TIMEPICKER    = 1 << 1;
    public static final int EDITOR_SINGLECHOOSER = 1 << 2;
    public static final int EDITOR_DATEPICKER    = 1 << 3;
    public static final int EDITOR_NUMBER        = 1 << 4;
    public static final int EDITOR_MULTICHOOSER  = 1 << 5;

    @DatabaseField(columnName = "id", generatedId = true, allowGeneratedIdInsert = true)
    private int id;

    @DatabaseField
    public int order;

    @DatabaseField
    public int category;

    @DatabaseField
    public String signature;

    @DatabaseField
    public String description;

    @DatabaseField
    public String value;

    @DatabaseField
    public int version;

    @DatabaseField
    public int editorType;

    @DatabaseField
    public String editorValues;

    public Setting previousSetting;
    public Setting defaultSetting;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Setting{");
        sb.append("id='").append(id).append('\'');
        sb.append(",category='").append(category).append('\'');
        sb.append(",signature='").append(signature).append('\'');
        sb.append(",description='").append(description).append('\'');
        sb.append(",value='").append(value).append('\'');
        sb.append(",version='").append(version).append('\'');
        sb.append(",editorType='").append(editorType).append('\'');
        sb.append(",editorValues='").append(editorValues).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
