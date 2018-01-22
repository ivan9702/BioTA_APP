package com.startek.biota.app.network.webservices;

import android.content.Context;

import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.models.Nfc;

import labs.anton.icenet.Body;

/**
 * 刪除 特定使用者
 *
 OK - WsNfcC
 OK - WsNfcR
 OK - WsNfcD
 */
public class WsNfcD extends WebService {

    private Nfc nfc;

    public WsNfcD(Context context, Nfc nfc)
    {
        super(
            Global.getConfig().getWsNfc(),
            Response.class,
            context,
            R.string.error_webservice_WsNfcD);

        this.nfc = nfc;
    }

    protected void buildBody(Body.Builder builder)
    {
        builder.add("type", "D");
        builder.add("id", nfc.humanId);
        builder.add("nfc", nfc.tagId);
    }

    public static class Response extends CommonResponse{}

}
