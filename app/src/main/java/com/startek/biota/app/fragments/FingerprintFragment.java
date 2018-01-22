package com.startek.biota.app.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.startek.biota.app.R;
import com.startek.biota.app.hardware.FingerprintSensor;
import com.startek.biota.app.utils.Converter;

/**
 * 『指紋機』
 *
 * 1. 按鈕『返回』，轉跳至『設定畫面』
 * 2. 顯示指紋機圖片
 * 3. 顯示指紋機相關資訊
 */
public class FingerprintFragment extends BaseFragment implements View.OnClickListener {

    private ImageView buttonBack;

    private TextView textViewDeviceId;
    private TextView textViewCompanyId;
    private TextView textViewVersion;
    private TextView textViewSpeed;
    private TextView textViewCompany;
    private TextView textViewAddress;
    private TextView textViewUpdatedtime;
    private TextView textViewProduct;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_fingerprint, container, false);

        textViewDeviceId = (TextView)v.findViewById(R.id.textViewDeviceId);
        textViewCompanyId = (TextView)v.findViewById(R.id.textViewCompanyId);
        textViewVersion = (TextView)v.findViewById(R.id.textViewVersion);
        textViewSpeed = (TextView)v.findViewById(R.id.textViewSpeed);
        textViewCompany = (TextView)v.findViewById(R.id.textViewCompany);
        textViewAddress = (TextView)v.findViewById(R.id.textViewAddress);
        textViewUpdatedtime = (TextView)v.findViewById(R.id.textViewUpdatedtime);
        textViewProduct = (TextView)v.findViewById( R.id.textViewProduct );

        buttonBack = (ImageView)v.findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener( this );
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        FingerprintSensor sensor = getFingerprintSensor();

        //String deviceId = sensor == null ? "" : Integer.toString(sensor.getProductId());
        //String companyId = sensor == null ? "" : Integer.toString(sensor.getVendorId());
        String deviceId = sensor == null ? "" : "0x"+Integer.toHexString(sensor.getProductId());
        String companyId = sensor == null ? "" : "0x"+Integer.toHexString(sensor.getVendorId());

        String version = sensor == null ? "" : sensor.getVersion();
        String speed = sensor == null ? "" : Integer.toString(sensor.getDeviceProtocol());
        String company = sensor == null ? "" : sensor.getManufacturerName();

        String address = sensor == null ? "" : sensor.getAddr();
        String updatedtime = sensor == null ? "" : Converter.toString(sensor.getLastUpdateTime());
        String product = sensor == null ? "" : sensor.getProductName();

        if(TextUtils.isEmpty(deviceId)) deviceId = "-";
        if(TextUtils.isEmpty(companyId)) companyId = "-";
        if(TextUtils.isEmpty(version)) version = "-";
        if(TextUtils.isEmpty(speed)) speed = "-";
        if(TextUtils.isEmpty(company)) company = "-";

        if(TextUtils.isEmpty(address)) address = "-";
        if(TextUtils.isEmpty(updatedtime)) updatedtime = "-";
        if(TextUtils.isEmpty(product)) product = "-";

        textViewDeviceId.setText(deviceId);
        textViewCompanyId.setText(companyId);
        textViewVersion.setText(version);
        textViewSpeed.setText(speed);
        textViewCompany.setText(company);

        textViewAddress.setText(address);
        textViewUpdatedtime.setText(updatedtime);
        textViewProduct.setText(product);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id)
        {
            case R.id.buttonBack: {
                onBackPressed();
            }break;
        }
    }
}
