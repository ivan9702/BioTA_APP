package com.startek.biota.app.managers;

import android.app.Activity;
import android.text.TextUtils;

import com.startek.biota.app.constant.PreferencesKey;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.hardware.FingerprintSensor;
import com.startek.biota.app.network.webservices.WsFingerprintDeviceC;
import com.startek.biota.app.network.webservices.WsFingerprintDeviceD;
import com.startek.biota.app.network.webservices.WsFingerprintDeviceR;
import com.startek.biota.app.network.webservices.WsFingerprintDeviceU;
import com.startek.biota.app.utils.PreferencesUtils;

import org.jdeferred.DoneCallback;

/**
 * Created by skt90u on 2016/6/16.
 */
public class FingerprintDeviceManager {

    private Activity context;

    public FingerprintDeviceManager(Activity context)
    {
        this.context = context;
    }

    public void create(final FingerprintSensor sensor)
    {
        if(sensor == null)return;

        if(!Global.getConfig().useBiotaServer())return;

        String deviceId = Integer.toString(sensor.getProductId());
        String companyId = Integer.toString(sensor.getVendorId());
        String version = sensor.getVersion();
        String speed = Integer.toString(sensor.getDeviceProtocol());
        String company = sensor.getManufacturerName();

        String address = sensor.getAddr();
        String product = sensor.getProductName();

        create(deviceId, companyId, version, speed, company,
                address, product);
    }

    /**
     * 沒辦法，為了測試這個 function，只好把 function 改成這樣
     */
    public void create(final String deviceId,
                       final String companyId,
                       final String version,
                       final String speed,
                       final String company,

                       final String address,
                       final String product)
    {
        if(!Global.getConfig().useBiotaServer())return;

        String origin_deviceId = PreferencesUtils.getString(PreferencesKey.FINGERPRINT_DEVICE_ID);
        if(!TextUtils.isEmpty(origin_deviceId))return;

        if(!TextUtils.isEmpty(deviceId))
        {
            // 如果已經在 SERVER 上面建立了，不要再次建立

            new WsFingerprintDeviceR(context, deviceId).execute().promise().done(new DoneCallback<WsFingerprintDeviceR.Response>() {
                @Override
                public void onDone(WsFingerprintDeviceR.Response result) {

                    boolean hasData = result.result.success && !TextUtils.isEmpty(result.data.device_id);

                    if(!hasData)
                    {
                        new WsFingerprintDeviceC(context,
                                deviceId, companyId, version, speed, company,
                                address, product).execute().promise().done(new DoneCallback<WsFingerprintDeviceC.Response>() {
                            @Override
                            public void onDone(WsFingerprintDeviceC.Response result) {
                                if(result.result.success)
                                {
                                    PreferencesUtils.setString(PreferencesKey.FINGERPRINT_DEVICE_ID, deviceId);
                                }
                            }
                        });
                    }
                    else
                    {
                        // 20160802 如果有資料就使用更新
                        new WsFingerprintDeviceU(context,
                                deviceId, companyId, version, speed, company,
                                address, product).execute().promise().done(new DoneCallback<WsFingerprintDeviceC.Response>() {
                            @Override
                            public void onDone(WsFingerprintDeviceC.Response result) {
                                if(result.result.success)
                                {
                                    PreferencesUtils.setString(PreferencesKey.FINGERPRINT_DEVICE_ID, deviceId);
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    public void delete()
    {
//        if(!Global.getConfig().useBiotaServer())return;
//
//        String deviceId = PreferencesUtils.getString(PreferencesKey.FINGERPRINT_DEVICE_ID);
//
//        if(TextUtils.isEmpty(deviceId))return;
//
//        new WsFingerprintDeviceD(context, deviceId).execute().done(new DoneCallback<WsFingerprintDeviceD.Response>() {
//            @Override
//            public void onDone(WsFingerprintDeviceD.Response result) {
//                if(result.result.success)
//                {
//                    PreferencesUtils.setString(PreferencesKey.FINGERPRINT_DEVICE_ID, "");
//                }
//            }
//        });

        // 20160802 Norman, Mander 說 取消刪除動作
        PreferencesUtils.setString(PreferencesKey.FINGERPRINT_DEVICE_ID, "");
    }
}
