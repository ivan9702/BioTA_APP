package com.startek.biota.app.models;


import com.j256.ormlite.field.DatabaseField;

/**
 * Created by skt90u on 2016/4/5.
 */
public class NfcLog {

    @DatabaseField(columnName = "id", generatedId = true, allowGeneratedIdInsert = true)
    private int id;

    @DatabaseField
    public String humanId;

    // 卡號
    @DatabaseField
    public String tagId;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NfcLog{");
        sb.append("id='").append(id).append('\'');
        sb.append(",humanId='").append(humanId).append('\'');
        sb.append(", tagId='").append(tagId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
