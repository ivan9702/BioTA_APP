package com.startek.biota.app.utils;

import android.content.Context;

import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by skt90u on 2016/4/24.
 */
public class FingerInfo {
    private FingerInfo(
            int fingerBtnId,
            String chinese,
            String which)
    {
        this.fingerBtnId = fingerBtnId;
        this.chinese = chinese;
        this.which = which;
    }

    public final int fingerBtnId;
    public final String chinese;
    public final String which;

    private static List<FingerInfo> fingerInfos;

    private static List<FingerInfo> getFingerInfos()
    {
        if(fingerInfos == null)
        {
            fingerInfos = new ArrayList<FingerInfo>();

            Context context = Global.getContext();

            fingerInfos.add(new FingerInfo(R.id.l_thumb, context.getString(R.string.buttonFinger1L), "l-thumb"));
            fingerInfos.add(new FingerInfo(R.id.r_thumb, context.getString(R.string.buttonFinger1R), "r-thumb"));

            fingerInfos.add(new FingerInfo(R.id.l_index, context.getString(R.string.buttonFinger2L), "l-index"));
            fingerInfos.add(new FingerInfo(R.id.r_index, context.getString(R.string.buttonFinger2R), "r-index"));

            fingerInfos.add(new FingerInfo(R.id.l_middle, context.getString(R.string.buttonFinger3L), "l-middle"));
            fingerInfos.add(new FingerInfo(R.id.r_middle, context.getString(R.string.buttonFinger3R), "r-middle"));

            fingerInfos.add(new FingerInfo(R.id.l_ring, context.getString(R.string.buttonFinger4L), "l-ring"));
            fingerInfos.add(new FingerInfo(R.id.r_ring, context.getString(R.string.buttonFinger4R), "r-ring"));

            fingerInfos.add(new FingerInfo(R.id.l_pinky, context.getString(R.string.buttonFinger5L), "l-pinky"));
            fingerInfos.add(new FingerInfo(R.id.r_pinky, context.getString(R.string.buttonFinger5R), "r-pinky"));
        }

        return fingerInfos;
    }

    public static FingerInfo getByFingerBtnId(int fingerBtnId)
    {
        List<FingerInfo> fingerInfos = getFingerInfos();

        for(FingerInfo fingerInfo:fingerInfos)
        {
            if(fingerInfo.fingerBtnId == fingerBtnId)
                return fingerInfo;
        }

        return null;
    }

    public static FingerInfo getByWhich(String which)
    {
        List<FingerInfo> fingerInfos = getFingerInfos();

        for(FingerInfo fingerInfo:fingerInfos)
        {
            if(StrUtils.equals(fingerInfo.which, which))
                return fingerInfo;
        }

        return null;
    }
}
