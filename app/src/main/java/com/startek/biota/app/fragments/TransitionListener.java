package com.startek.biota.app.fragments;

import android.app.Fragment;

import com.startek.biota.app.hardware.FingerprintSensor;
import com.startek.biota.app.models.EasyCard;

public interface TransitionListener {

    void backToRoot();

    void back();

    void showFragment(Fragment f, String tag);

    EasyCard getReceivedEasyCard();

    FingerprintSensor getFingerprintSensor();
}
