package com.startek.biota.app.network.webservices;

import android.content.Context;

import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.models.Human;

import labs.anton.icenet.Body;

/**
 * 刪除 特定使用者
 */
public class WsHumanD extends WebService {

    private Human human;

    public WsHumanD(Context context, Human human)
    {
        super(
            Global.getConfig().getWsHuman(),
            Response.class,
            context,
            R.string.error_webservice_WsHumanD);

        this.human = human;
    }

    protected void buildBody(Body.Builder builder)
    {
        builder.add("type", "D");
        builder.add("id", human.id);
        builder.add("name", human.name);
//        builder.add("bind_id", human.bind_id);
    }

    public static class Response extends CommonResponse{}

}
