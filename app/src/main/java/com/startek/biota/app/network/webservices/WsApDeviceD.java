package com.startek.biota.app.network.webservices;

import android.content.Context;

import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.utils.MobileStatus;

import labs.anton.icenet.Body;

/**
 * WebService: 指紋採樣程式執行設備 (ap_device)
 *  操作: Create
 */
public class WsApDeviceD extends WebService {

    private String apId;
    private String platformType;

    public WsApDeviceD(Context context)
    {
        super(
            Global.getConfig().getWsApDevice(),
            Response.class,
            context,
            R.string.error_webservice_WsApDeviceD);

        this.apId = MobileStatus.getDeviceId();
        this.platformType = MobileStatus.getPlatformType();
    }

    protected void buildBody(Body.Builder builder)
    {
        builder.add("type", "D");
        builder.add("id", apId);
        builder.add("platform_type", platformType);
    }

    public class Response extends CommonResponse {}
}
