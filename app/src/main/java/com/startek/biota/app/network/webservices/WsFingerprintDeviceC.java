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
 *
 * 20160616 check WsFingerprintDevice
 OK - WsFingerprintDeviceC
 OK - WsFingerprintDeviceR
 OK - WsFingerprintDeviceU
 OK - WsFingerprintDeviceD
 */
public class WsFingerprintDeviceC extends WebService {

    private String apId;

    private String deviceId;
    private String companyId;
    private String version;
    private String speed;
    private String company;
    private String address;
    private String product;

    public WsFingerprintDeviceC(Context context,
                                String deviceId,
                                String companyId,
                                String version,
                                String speed,
                                String company,

                                String address,
                                String product)
    {
        super(
                Global.getConfig().getWsFingerprintDevice(),
                Response.class,
                context,
                R.string.error_webservice_WsFingerprintDeviceC);

        this.deviceId = deviceId;
        this.companyId = companyId;
        this.version = version;
        this.speed = speed;
        this.company = company;
        this.address = address;
        this.product = product;

        this.apId = MobileStatus.getDeviceId();
    }

    protected void buildBody(Body.Builder builder)
    {
        builder.add("type", "C");
        builder.add("id", apId);
        builder.add("device_id", deviceId);
        builder.add("co_id", companyId);
        builder.add("ver", version);

        builder.add("speed", speed);
        builder.add("company", company);
        builder.add("addr", address);
        builder.add("product", product);
    }

    public class Response extends CommonResponse {}
}
