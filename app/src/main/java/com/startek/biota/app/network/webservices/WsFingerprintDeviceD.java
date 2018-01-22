package com.startek.biota.app.network.webservices;

import android.content.Context;

import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.hardware.FingerprintSensor;
import com.startek.biota.app.utils.MobileStatus;

import labs.anton.icenet.Body;

/**
 * WebService: 指紋採樣程式執行設備 (ap_device)
 *  操作: Create
 */
public class WsFingerprintDeviceD extends WebService {

    private String apId;
    private String deviceId;

    public WsFingerprintDeviceD(Context context, String deviceId)
    {
        super(
            Global.getConfig().getWsFingerprintDevice(),
            Response.class,
            context,
            R.string.error_webservice_WsFingerprintDeviceD);

        this.apId = MobileStatus.getDeviceId();
        this.deviceId = deviceId; // 必須手動傳入，因為移除裝置就不能用 FingerprintSensor.findFirst 找到對應 sensor
    }

    protected void buildBody(Body.Builder builder)
    {
        builder.add("type", "D");
        builder.add("id", apId);
        builder.add("device_id", deviceId);
    }

    public class Response extends CommonResponse
    {
    }
}
