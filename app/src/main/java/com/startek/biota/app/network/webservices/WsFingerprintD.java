package com.startek.biota.app.network.webservices;

import android.content.Context;

import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.models.Fingerprint;
import com.startek.biota.app.models.Human;

import java.util.List;

import labs.anton.icenet.Body;

/**
 * 刪除 特定使用者
 *
 */
public class WsFingerprintD extends WebService {

    private Human human;
    private Fingerprint fingerprint;


    public WsFingerprintD(Context context, Human human, Fingerprint fingerprint)
    {
        super(
                Global.getConfig().getWsFingerprint(),
                Response.class,
                context,
                R.string.error_webservice_WsFingerprintD);

        this.human = human;
        this.fingerprint = fingerprint;
    }

    public WsFingerprintD(Context context)
    {
        super(
                Global.getConfig().getWsFingerprint(),
                Response.class,
                context,
                R.string.error_webservice_WsFingerprintD);

        List<Human> humans = Global.getHumanList();

        for(Human aHuman:humans)
        {
            if(aHuman.getFingerprints().size() != 0)
            {
                this.human = aHuman;
                this.fingerprint = aHuman.getFingerprints().get(0);
                break;
            }
        }

        String s = "";
    }

    protected void buildBody(Body.Builder builder)
    {
        builder.add("type", "D");
        builder.add("id", human.id);
        builder.add("f_id", fingerprint.f_id);
		
		// 20160714 Norman, 針對 刪除使用者無法移除指紋，測試又是以下的問題所引起
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
