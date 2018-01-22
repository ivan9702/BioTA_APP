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
public class WsApDeviceU extends WebService {

    private String apId;
    private String platformType;
    private double gpsLat;
    private double gpsLng;

    public WsApDeviceU(Context context)
    {
        super(
            Global.getConfig().getWsApDevice(),
            Response.class,
            context,
            R.string.error_webservice_WsApDeviceU);

        this.apId = MobileStatus.getDeviceId();
        this.platformType = MobileStatus.getPlatformType();
    }

    protected void buildBody(Body.Builder builder)
    {
        builder.add("type", "U");
        builder.add("id", apId);
        builder.add("platform_type", platformType);
        builder.add("lat", gpsLat);
        builder.add("lng", gpsLng);
    }

    public class Response extends CommonResponse {}
}
