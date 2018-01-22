package com.startek.biota.app.network.webservices;

import android.content.Context;

import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;

import java.util.List;

import labs.anton.icenet.Body;

/**
 * 刪除 特定使用者
 */
public class WsNfcR extends WebService {

    private String humanId;

    public WsNfcR(Context context, String humanId)
    {
        super(
            Global.getConfig().getWsNfc(),
            Response.class,
            context,
            R.string.error_webservice_WsNfcR);

        this.humanId = humanId;
    }

    protected void buildBody(Body.Builder builder)
    {
        builder.add("type", "R");
        builder.add("id", humanId);
    }

    public static class Response extends CommonResponse{
        public List<String> nfc;
    }

}
