package com.startek.biota.app.models;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Parcelable;
import android.util.Log;

import com.startek.biota.app.utils.StrUtils;

import java.util.Date;

/**
 * 悠遊卡可取得的資訊
 */
public class EasyCard {

    private Date readTime;

    public Date getReadTime() {
        return readTime;
    }

    private String tagId;

    public String getTagId() {
        return tagId;
    }

    public EasyCard(Tag tag)
    {
        this.readTime = new Date();
        this.tagId = getHex(tag.getId());
    }

    private String getHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b).toUpperCase());
        }
        return sb.toString();
    }


}
