package com.startek.biota.app.network.webservices;

import android.content.Context;

import com.google.gson.Gson;
import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.utils.AssetHelper;

import org.jdeferred.Deferred;
import org.jdeferred.impl.DeferredObject;

import java.util.List;

import labs.anton.icenet.Body;

/**
 * 取得 Reminder
 */
public class WsReminderR extends WebService {

    public WsReminderR(Context context)
    {
        super(
            Global.getConfig().getWsReminder(),
            Response.class,
                context,
            R.string.error_webservice_WsReminderR);
    }

    protected void buildBody(Body.Builder builder){
        builder.add("type", "R");
        builder.add("style", "reminder");
        builder.remove("device_id");
        builder.remove("device_type");
        builder.remove("push_token");
    }

//    public class Response extends CommonResponse
//    {
//        public Data data;
//
//        public class Data
//        {
//            public List<Header> Header;
//            public List<List<String>> Body;
//
//            public class Header
//            {
//                public String column;
//                public int width;
//            }
//        }
//    }

    public class Response extends CommonResponse
    {
        public List<Delta> data;

        public class Delta
        {
            public String createdAt;
            public String updatedAt;
            public String subject_pk;
            public String subject;
            public String tricker;
            public String time;
            public String style;
        }
    }

    private static Response localData;

    public static Deferred getLocalData()
    {
        Deferred deferred = new DeferredObject();

        if(localData == null)
        {
            String json = AssetHelper.readString("LocalReminder.json");

            Gson gson = new Gson();

            localData = gson.fromJson(json, WsReminderR.Response.class);
        }

        deferred.resolve(localData);

        return deferred;
    }
}
