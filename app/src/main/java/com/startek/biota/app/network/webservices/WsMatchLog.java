package com.startek.biota.app.network.webservices;

import android.content.Context;
import android.text.TextUtils;

import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.models.Human;
import com.startek.biota.app.models.MatchLog;
import com.startek.biota.app.models.Nfc;
import com.startek.biota.app.utils.Converter;

import labs.anton.icenet.Body;

/**
 * Client 端比對指紋資料記錄
 *  Rv 為讀取單一指紋比對成功或失敗結果、Ri 為使用指紋尋找特定使用者
 */
public class WsMatchLog extends WebService {

    MatchLog matchLog;

    public WsMatchLog(Context context, MatchLog matchLog)
    {
        super(
            Global.getConfig().getWsMatchLog(),
            Response.class,
            context,
            matchLog.type.equals(MatchLog.CATEGORY_ENROLL) ? R.string.error_webservice_WsMatchLogRi : R.string.error_webservice_WsMatchLogRv);

        this.matchLog = matchLog;
    }

    protected void buildBody(Body.Builder builder)
    {


        if(matchLog.type.equals(MatchLog.CATEGORY_ENROLL))
        {
            // Ri	http://52.192.93.236:1337/api/Comparison_client?type=Ri&minutiae=xxx&STime=2012&CTime=sss&MScore=111&is_success=true&client_action=true&MTime=2012

            builder.add("type", matchLog.type);

            builder.add("minutiae", matchLog.minutiae);
            builder.add("pic", matchLog.pic);

            builder.add("STime", Converter.toString(matchLog.STime, MatchLog.STimeFormat));
            builder.add("CTime", matchLog.CTime);
            builder.add("MScore", matchLog.MScore);
            builder.add("MTime", matchLog.MTime);

            builder.add("is_success", matchLog.is_success ? "true" : "false");
            builder.add("client_action", matchLog.client_action);
        }
        else
        {
            // Rv	http://52.192.93.236:1337/api/Comparison_client?type=Rv&minutiae=xxx&STime=2012&CTime=sss&MScore=111&is_success=true&client_action=true&MTime=2012&id=57725dd57685ce8c10495442&bind_id=aaa

            builder.add("type", matchLog.type);

            builder.add("minutiae", matchLog.minutiae);
            builder.add("pic", matchLog.pic);

            builder.add("id", matchLog.humanId);
            builder.add("bind_id", matchLog.bind_id);
            builder.add("nfc", !TextUtils.isEmpty(matchLog.tagId) ? matchLog.tagId : "");

            builder.add("STime", Converter.toString(matchLog.STime, Converter.DateTimeFormat.YYYYMMddHHmmssSSSZ));
            builder.add("CTime", matchLog.CTime);
            builder.add("MScore", matchLog.MScore);
            builder.add("MTime", matchLog.MTime);

            builder.add("is_success", matchLog.is_success ? "true" : "false");
            builder.add("client_action", matchLog.client_action);
        }
    }

    public static class Response extends CommonResponse{
        public String recordtime;
        public String server_action;
    }

}
