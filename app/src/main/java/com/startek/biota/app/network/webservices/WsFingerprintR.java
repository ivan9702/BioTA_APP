package com.startek.biota.app.network.webservices;

import android.content.Context;

import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;

import java.util.List;

import labs.anton.icenet.Body;

/**
 * 刪除 特定使用者
 */
public class WsFingerprintR extends WebService {

    private String humanId;

    public WsFingerprintR(Context context, String humanId)
    {
        super(
            Global.getConfig().getWsFingerprint(),
            Response.class,
            context,
            R.string.error_webservice_WsFingerprintC);

        this.humanId = humanId;
    }

    protected void buildBody(Body.Builder builder)
    {
        builder.add("type", "R");
        builder.add("id", humanId);
    }

    public static class Response extends CommonResponse{
        public List<Finger> f;

        public class Finger
        {
            public String id;
            public String which;
            public String pic;
        }
    }

}
