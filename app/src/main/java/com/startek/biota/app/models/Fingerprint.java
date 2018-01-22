package com.startek.biota.app.models;

import android.content.Intent;
import android.text.TextUtils;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.startek.biota.app.utils.Converter;
import com.startek.biota.app.utils.StrUtils;

/**
 * 指紋資料
 */
public class Fingerprint {

    @DatabaseField(columnName = "id", generatedId = true, allowGeneratedIdInsert = true)
    public int id; // 從伺服器傳來的資料(無id欄位)，此欄位將設定為為 0

    @DatabaseField
    public String humanId;

    @DatabaseField
    public String minutiae; // base64(特徵點資料), // OLD COMMENT: 從伺服器傳來的資料(無id欄位)，此欄位將設定為為 null

    @DatabaseField
    public String f_id;

    @DatabaseField
    public String which;

    @DatabaseField
    public String pic;

    @Override
    public boolean equals(Object other)
    {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Fingerprint)) return false;

        Fingerprint lhs = this;
        Fingerprint rhs = (Fingerprint)other;

        return lhs.id == rhs.id &&
               StrUtils.equals(lhs.humanId, rhs.humanId) &&
               StrUtils.equals(lhs.minutiae, rhs.minutiae) &&
               StrUtils.equals(lhs.which, rhs.which) &&
               StrUtils.equals(lhs.pic, rhs.pic);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Fingerprint{");
        sb.append("id='").append(id).append('\'');
        sb.append(",humanId='").append(humanId).append('\'');
        sb.append(",minutiae='").append(minutiae).append('\'');
        sb.append(",which='").append(which).append('\'');
        sb.append(",pic='").append(pic).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public boolean match(int fingerBtnId)
    {
        String lhs = this.which;
        String rhs = Converter.fingerBtnIdToEnglish(fingerBtnId);
        return StrUtils.equals(lhs, rhs);
    }
}
