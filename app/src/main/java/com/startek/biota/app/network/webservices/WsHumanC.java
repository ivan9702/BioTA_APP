package com.startek.biota.app.network.webservices;

import android.content.Context;
import android.util.Log;

import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.models.Human;

import java.util.List;

import labs.anton.icenet.Body;

/**
 * 刪除 特定使用者
 *
 * 20160616 check WsHuman
 OK - WsHumanC
 OK - WsHumanR1
 XX - WsHumanR2
     // 20160616 檢查的版本少了以下欄位

     public String dept;
     public String is_manager;
     public List<Team> team;

     public class Team
     {
     public int id;
     public boolean arrived;
     }
 OK - WsHumanU
 XX - WsHumanD
    // 20160616 檢查的版本，認為只需要 humanId 就可以執行刪除動作
 */
public class WsHumanC extends WebService {

    private Human human;

    public WsHumanC(Context context, Human human)
    {
        super(
            Global.getConfig().getWsHuman(),
            Response.class,
            context,
            R.string.error_webservice_WsHumanC);

        this.human = human;

        Log.d("WsHumanC", human.name);
    }

    protected void buildBody(Body.Builder builder)
    {
        builder.add("type", "C");
        builder.add("birthday", human.birthday);
        builder.add("gender", human.gender);
        builder.add("bloodtype", human.bloodtype);
        builder.add("job", human.job);

        builder.add("name", human.name);
        builder.add("bind_id", human.bind_id);
        builder.add("is_manager", human.is_manager);
    }

    public static class Response extends CommonResponse
    {
        public Data data;

        public class Data
        {
            public String id;
            public String name;
            public List<String> nfc;
            public List<Finger> f;

            public class Finger
            {
                public String id;
                public String which;
                public String pic;
            }
        }
    }

}
