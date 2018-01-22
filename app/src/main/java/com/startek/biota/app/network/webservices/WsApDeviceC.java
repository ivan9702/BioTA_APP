package com.startek.biota.app.network.webservices;

import android.content.Context;

import labs.anton.icenet.Body;

import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.utils.MobileStatus;

/**
 * WebService: 指紋採樣程式執行設備 (ap_device)
 *  操作: Create
 *
 * 20160616 check WsApDevice
 OK - WsApDeviceC
 OK - WsApDeviceR1
 OK - WsApDeviceR2
 OK - WsApDeviceU
 OK - WsApDeviceD
 */
public class WsApDeviceC extends WebService {

    private String apId;
    private String platformType;
    private double gpsLat;
    private double gpsLng;

    public WsApDeviceC(Context context)
    {
        super(
                Global.getConfig().getWsApDevice(),
                Response.class,
                context,
                R.string.error_webservice_WsApDeviceC);

        this.apId = MobileStatus.getDeviceId();
        this.platformType = MobileStatus.getPlatformType();
        this.gpsLat = MobileStatus.getLatitude();
        this.gpsLng = MobileStatus.getLongitude();
    }

    protected void buildBody(Body.Builder builder)
    {
        builder.add("type", "C");
        builder.add("id", apId);
        builder.add("platform_type", platformType);
        builder.add("lat", gpsLat);
        builder.add("lng", gpsLng);
    }

    public class Response extends CommonResponse {}
}
