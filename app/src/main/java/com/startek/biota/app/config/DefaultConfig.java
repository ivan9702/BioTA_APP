package com.startek.biota.app.config;

import com.google.gson.Gson;
import com.startek.biota.app.models.Setting;
import com.startek.biota.app.utils.AssetHelper;

/**
 * Created by skt90u on 2016/4/9.
 */
public class DefaultConfig {

    private static DefaultConfig instance;

    public static DefaultConfig getInstance()
    {
        if(instance == null)
        {
            String json = AssetHelper.readString("DefaultConfig.json");

            Gson gson = new Gson();

            instance = gson.fromJson(json,DefaultConfig.class);
        }

        return instance;
    }


    public Setting UseBiotaServer;
    public Setting HideApiError;

    public Setting BaseUrl;
    public Setting WsHuman;
    public Setting WsNfc;
    public Setting WsFingerprint;
    public Setting WsAnnouncement;
    public Setting WsReminder;
    public Setting WsApDevice;
    public Setting WsFingerprintDevice;
    public Setting WsMatchLog;

    public Setting EmailServer;
    public Setting EmailPort;
    public Setting EmailAccount;
    public Setting EmailPassword;
    public Setting EmailSSL;

    public Setting EmailTarget;
    public Setting EmailSubject;
    public Setting EmailBody;
    public Setting EmailTimes;
    public Setting EmailAttachFileFormat;

    public Setting NfcAlert;
    public Setting CreateManagerIfNotExist;
    public Setting MaxEmployeeIdLength;
    public Setting MaxNfcCount;
    public Setting VerifyTimeout;
    public Setting EnrollTimeout;
    public Setting VerifyScoreLimit;



}
