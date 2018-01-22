package com.startek.biota.app.network.webservices;

import android.content.Context;

import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.utils.MobileStatus;

import java.util.List;

import labs.anton.icenet.Body;

/**
 * WebService: 指紋採樣程式執行設備 (ap_device)
 *  操作: Create
 */
public class WsApDeviceR1 extends WebService {

    private String apId;

    public WsApDeviceR1(Context context)
    {
        super(
            Global.getConfig().getWsApDevice(),
            Response.class,
            context,
            R.string.error_webservice_WsApDeviceR1);

        this.apId = MobileStatus.getDeviceId();
    }

    protected void buildBody(Body.Builder builder)
    {
        builder.add("type", "R1");
        builder.add("id", apId);
    }

    public class Response extends CommonResponse
    {
        public List<Delta> ap;

        public class Delta
        {
            public String id;
            public String device_type;
            public String platform_type;
            public List<String> human;
        }
    }
}
