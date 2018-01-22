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
public class WsFingerprintDeviceR extends WebService {

    private String apId;
    private String device_id;

    public WsFingerprintDeviceR(Context context, String device_id)
    {
        super(
            Global.getConfig().getWsFingerprintDevice(),
            Response.class,
            context,
            R.string.error_webservice_WsFingerprintDeviceR);

        this.apId = MobileStatus.getDeviceId();
        this.device_id = device_id;
    }

    protected void buildBody(Body.Builder builder)
    {
        builder.add("type", "R");
        builder.add("id", apId);
        builder.add("device_id", device_id);
    }

    // 20160729 之前的 Response
//    public class Response extends CommonResponse
//    {
//        public String device_id;
//        public String co_id;
//        public String ver;
//        public String speed;
//        public String company;
//
//        public String addr;
//        public String product;
//    }

    // 20160729 之後的 Response
    public class Response extends CommonResponse
    {
        public Data data;

        public class Data
        {
            public String product;
            public String id;
            public String updatedAt;
            public String speed;
            public String createdAt;

            public String company;
            public String addr;
            public String device_id;
            public String ver;
            public String co_id;
            public String ds_unique;
        }
    }
}
