package com.startek.biota.app.network.webservices;

import android.content.Context;

import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;

import java.util.List;

import labs.anton.icenet.Body;

/**
 * 取得 特定使用者資料
 */
public class WsHumanR2 extends WebService {

    private String humanId;

    public WsHumanR2(Context context, String humanId)
    {
        super(
            // http://203.66.65.139/test/human.php?id=1
            Global.getConfig().getWsHuman() + "?id=" + humanId,
            Response.class,
            context,
            R.string.error_webservice_WsHumanR2);

        this.humanId = humanId;
    }

    protected void buildBody(Body.Builder builder)
    {
        builder.add("type", "R2");
        builder.add("id", humanId);
        builder.remove("device_id");
        builder.remove("push_token");
        builder.remove("device_type");
    }

    public class Response extends CommonResponse
    {
        public Data data;

        public class Data
        {
            // 以下三個欄位是新增的
            public String id;
            public String updatedAt;
            public String createdAt;

            public String birthday;
            public String gender;
            public String bloodtype;
            public String job;
            public String name;
            public String bind_id;

            // 20160616 檢查的 api 文件，少了以下欄位

            public String dept;
            public String is_manager;
            public List<Team> team;

            public class Team
            {
                public int id;
                public boolean arrived;
            }
        }
    }
}
