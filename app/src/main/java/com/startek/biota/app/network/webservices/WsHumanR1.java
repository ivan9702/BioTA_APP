package com.startek.biota.app.network.webservices;

import android.content.Context;

import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;

import java.util.List;

import labs.anton.icenet.Body;

/**
 * 取得 所有使用者資料
 */
public class WsHumanR1 extends WebService {

    public WsHumanR1(Context context)
    {
        super(
            Global.getConfig().getWsHuman(),
            Response.class,
            context,
            R.string.error_webservice_WsHumanR1);
    }

    protected void buildBody(Body.Builder builder)
    {
        builder.add("type", "R1");
//        builder.remove("device_id");
        builder.remove("device_type");
        builder.remove("push_token");
    }

    public class Response extends CommonResponse
    {
        public List<Delta> data;

        public class Delta
        {
            public String id;
            public String name;
            public List<Nfc> nfc;
            public List<Finger> f;

            public class Finger
            {
                public String id; // HumanR1，需要傳送 f_id，在刪除指紋資料時會使用到
                public String which;
                public String pic;

                public String minutiae; // 20160714 Norman, 給 API 傳送特徵值，用以讓手機端產生 dat 檔案
            }

            public class Nfc
            {
                public String id;
            }
        }

    }
}
