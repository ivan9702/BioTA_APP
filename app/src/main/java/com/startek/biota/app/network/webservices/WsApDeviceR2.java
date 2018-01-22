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
public class WsApDeviceR2 extends WebService {

    private String apId;

    public WsApDeviceR2(Context context)
    {
        super(
            Global.getConfig().getWsApDevice(),
            Response.class,
            context,
            R.string.error_webservice_WsApDeviceR2);

        this.apId = MobileStatus.getDeviceId();
    }

    protected void buildBody(Body.Builder builder)
    {
        builder.add("type", "R2");
        builder.add("id", apId);

        builder.remove("device_type"); // 不可填寫參數:device_type // 20160705 Norman
        builder.remove("device_id");
        builder.remove("device_type");
    }

    // API 文件上的結構
//    public class Response extends CommonResponse
//    {
//        public String id;
//        public String device_type;
//        public String platform_type;
//        public String push_token;
//    }

    // 實際傳回的結構
    public class Response extends CommonResponse
    {
        public Data data;

        public class Data
        {
            public String id;
            public String device_type;
            public String platform_type;
            public String push_token;
        }

        /*
        {
            "data": {
                "createdAt": "2016-06-27T13:24:12.443Z",
                "updatedAt": "2016-07-04T12:37:41.062Z",
                "ds_uuid": "577128fc82b4103c529e6f2c",
                "id": "355098063161082",
                "device_type": "android",
                "platform_type": "Sony D6653",
                "push_token": ""
            },
            "result": {
                "success": true,
                "message": ""
            },
            "client": {
                "resp": ""
            }
        }
         */

    }
}
