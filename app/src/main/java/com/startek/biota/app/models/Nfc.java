package com.startek.biota.app.models;

import android.text.TextUtils;

import com.j256.ormlite.field.DatabaseField;
import com.startek.biota.app.utils.StrUtils;

public class Nfc {

    @DatabaseField(columnName = "id", generatedId = true, allowGeneratedIdInsert = true)
    public int id; // 從伺服器傳來的資料(無id欄位)，此欄位將設定為為 0

    @DatabaseField
    public String humanId;

    @DatabaseField
    public String tagId;

    @Override
    public boolean equals(Object other)
    {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Nfc)) return false;

        Nfc lhs = this;
        Nfc rhs = (Nfc)other;

        return lhs.id == rhs.id &&
               StrUtils.equals(lhs.humanId, rhs.humanId) &&
               StrUtils.equals(lhs.tagId, rhs.tagId);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Nfc{");
        sb.append("id='").append(id).append('\'');
        sb.append(",humanId='").append(humanId).append('\'');
        sb.append(", tagId='").append(tagId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
