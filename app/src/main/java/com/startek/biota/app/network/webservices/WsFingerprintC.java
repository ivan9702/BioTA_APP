package com.startek.biota.app.network.webservices;

import android.content.Context;

import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.models.Fingerprint;
import com.startek.biota.app.models.Human;
import com.startek.biota.app.utils.Converter;

import java.util.List;

import labs.anton.icenet.Body;

/**
 * 刪除 特定使用者
 *
 * 20160616 check WsFingerprint
 OK - WsFingerprintC
 OK - WsFingerprintD
 */
public class WsFingerprintC extends WebService {

    private Human human;
    private Fingerprint fingerprint;

    public WsFingerprintC(Context context, Human human, Fingerprint fingerprint)
    {
        super(
            Global.getConfig().getWsFingerprint(),
            Response.class,
            context,
            R.string.error_webservice_WsFingerprintC);

        this.human = human;
        this.fingerprint = fingerprint;
    }

    protected void buildBody(Body.Builder builder)
    {
        builder.add("type", "C");
        builder.add("id", fingerprint.humanId);
        builder.add("bind_id", human.bind_id);
        //builder.add("f_id", Converter.englishToFingerId(fingerprint.which)); // 右手大拇指起算 左手小指結束 (0~9)
        builder.add("which", fingerprint.which);

        builder.add("pic", fingerprint.pic);
        builder.add("minutiae", fingerprint.minutiae);
    }

//    public class Response extends CommonResponse
//    {
//        public Data data;
//
//        public class Data
//        {
//            public String birthday;
//            public String gender;
//            public String bloodtype;
//            public String job;
//            public String name;
//            public String bind_id;
//
//            // 20160616 檢查的 api 文件，少了以下欄位
//
//            public String dept;
//            public String is_manager;
//            public List<Team> team;
//
//            public class Team
//            {
//                public int id;
//                public boolean arrived;
//            }
//        }
//    }

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
                public String id;
                public String which;
                public String pic;
            }

            public class Nfc
            {
                public String id;
            }
        }

    }

}
