package com.startek.biota.app.network.webservices;

import android.content.Context;

import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.models.Human;

import java.util.List;

import labs.anton.icenet.Body;

/**
 * 刪除 特定使用者
 */
public class WsHumanU extends WebService {

    private Human human;

    public WsHumanU(Context context, Human human)
    {
        super(
            Global.getConfig().getWsHuman(),
            Response.class,
            context,
            R.string.error_webservice_WsHumanU);

        this.human = human;
    }

    protected void buildBody(Body.Builder builder)
    {
        builder.add("type", "U");
        builder.add("id", human.id);
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
