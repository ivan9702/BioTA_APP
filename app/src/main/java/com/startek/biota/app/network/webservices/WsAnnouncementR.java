package com.startek.biota.app.network.webservices;

import android.content.Context;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.utils.AssetHelper;
import com.startek.biota.app.utils.MobileStatus;

import org.jdeferred.Deferred;
import org.jdeferred.impl.DeferredObject;

import java.util.List;

import labs.anton.icenet.Body;
import labs.anton.icenet.RequestError;

/**
 * 取得 Announcement
 */
public class WsAnnouncementR extends WebService {

    public WsAnnouncementR(Context context)
    {
        super(
            Global.getConfig().getWsAnnouncement(),
            Response.class,
            context,
            R.string.error_webservice_WsAnnouncementR);
    }

    protected void buildBody(Body.Builder builder)
    {
        builder.add("type", "R");
        builder.add("style", "announcement");
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
            String json = AssetHelper.readString("LocalAnnouncement.json");

            Gson gson = new Gson();

            localData = gson.fromJson(json,WsAnnouncementR.Response.class);
        }

        deferred.resolve(localData);

        return deferred;
    }
}
