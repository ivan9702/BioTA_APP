package com.startek.biota.app.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.startek.biota.app.global.Global;
import com.startek.biota.app.hardware.FingerprintSensor;
import com.startek.biota.app.hardware.NfcReader;
import com.startek.biota.app.managers.HumanManager;
import com.startek.biota.app.managers.MatchLogManager;
import com.startek.biota.app.models.EasyCard;
import com.startek.biota.app.utils.Converter;
import com.startek.biota.app.utils.DirectoryQualifierHelper;
import com.startek.biota.app.utils.PermissionHelper;
import com.startek.biota.app.utils.SoftInputHelper;

/**
 * Created by skt90u on 2016/6/10.
 */
public class BaseFragment extends Fragment {

    /** Transition Listener */
    private TransitionListener mListener;

    protected Activity context;
    protected HumanManager humanManager;
    protected MatchLogManager matchLogManager;
    private PermissionHelper permissionHelper;
    private DirectoryQualifierHelper directoryQualifierHelper;

    public String getLogTag()
    {
        return getClass().getSimpleName();
    }

    public void onBackPressed() {
        back();
    }

    protected boolean isEasyCardReceived()
    {
        if (mListener != null) {
            return mListener.getReceivedEasyCard() != null;
        }

        return false;
    }

    public FingerprintSensor getFingerprintSensor()
    {
        if (mListener != null) {
            return mListener.getFingerprintSensor();
        }

        return null;
    }

    public void onEasyCardReceived(final EasyCard easyCard)
    {
        Log.d(getLogTag(), String.format("onEasyCardReceived, read easy card (readTime = %s, tagId = %s)",
                Converter.toString(easyCard.getReadTime(), Converter.DateTimeFormat.YYYYMMddHHmmss),
                easyCard.getTagId()));
    }

    public void onUsbChanged(boolean attached)
    {
        Log.d(getLogTag(), String.format("onUsbChanged = %s", attached ? "attached" : "detached"));
    }

    protected void backToRoot() {
        if (mListener != null) {
            mListener.backToRoot();
        }
    }

    protected void back() {
        if (mListener != null) {
            mListener.back();
        }
    }

    protected void showFragment(Fragment f, String tag) {
        if (mListener != null) {
            mListener.showFragment(f, tag);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(getLogTag(), "onAttach");

        if (activity instanceof TransitionListener) {
            mListener = (TransitionListener) activity;
        }

        context = activity;
        humanManager = new HumanManager(context);
        matchLogManager = new MatchLogManager(context);
        permissionHelper = new PermissionHelper(context);
        directoryQualifierHelper = new DirectoryQualifierHelper(context);

//         檢查目前使用的 resource file
//        Log.d(TAG, directoryQualifierHelper.get(
//                DirectoryQualifierHelper.RESOURCE_VALUES,
//
//                DirectoryQualifierHelper.QUALIFIER_MCCMNC |
//                DirectoryQualifierHelper.QUALIFIER_LANGUAGEREGIONCODE |
//                DirectoryQualifierHelper.QUALIFIER_SMALLESTWIDTH |
//                DirectoryQualifierHelper.QUALIFIER_AVAILABLETWIDTH |
//                DirectoryQualifierHelper.QUALIFIER_AVAILABLETHEIGHT |
//                DirectoryQualifierHelper.QUALIFIER_SCREENSIZE |
//                DirectoryQualifierHelper.QUALIFIER_SCREENORIENTATION |
//                DirectoryQualifierHelper.QUALIFIER_SCREENPIXELDENSITY |
//                DirectoryQualifierHelper.QUALIFIER_APILEVEL
//        ));
//        permissionHelper.checkRequestedPermissions();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(getLogTag(), "onCreate");
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(getLogTag(), "onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(getLogTag(), "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(getLogTag(), "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(getLogTag(), "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(getLogTag(), "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(getLogTag(), "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(getLogTag(), "onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(getLogTag(), "onDetach");
    }
}
