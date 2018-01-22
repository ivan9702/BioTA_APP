package com.startek.biota.app.models;

import com.j256.ormlite.field.DatabaseField;

/**
 * 用來表現資料用，不用再來儲存資料
 */
public class Team {

    @DatabaseField
    public String humanId;

    @DatabaseField
    public int memberId;

    @DatabaseField
    public boolean arrived;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Team{");
        sb.append("humanId='").append(humanId).append('\'');
        sb.append(", memberId='").append(memberId).append('\'');
        sb.append(", arrived='").append(arrived).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
