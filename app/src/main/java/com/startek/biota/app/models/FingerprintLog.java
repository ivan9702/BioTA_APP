package com.startek.biota.app.models;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;

/**
 * 指紋機 - 掃描次數統計
 */
public class FingerprintLog {

    @DatabaseField(columnName = "id", generatedId = true, allowGeneratedIdInsert = true)
    private int id;

    @DatabaseField
    public int fingerprintId;

    // 開始時間 (datetime)
    @DatabaseField
    public String stime;

    // 本次特徵點 (byte[])
    @DatabaseField(dataType = DataType.BYTE_ARRAY)
    public byte[] minutiae;

    // 掃描完成秒數
    @DatabaseField
    public int ctime;

    // 比對分數
    @DatabaseField
    public int mscore;

    // 比對完成秒數
    @DatabaseField
    public int mtime;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FingerprintLog{");
        sb.append("id='").append(id).append('\'');
        sb.append(",fingerprintId='").append(fingerprintId).append('\'');
        sb.append(",stime='").append(stime).append('\'');
        sb.append(",ctime='").append(ctime).append('\'');
        sb.append(", mscore='").append(mscore).append('\'');
        sb.append(", mtime='").append(mtime).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
