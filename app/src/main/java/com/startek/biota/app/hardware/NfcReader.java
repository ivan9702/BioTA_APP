package com.startek.biota.app.hardware;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.util.Log;

import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.models.EasyCard;
import com.startek.biota.app.utils.DialogHelper;
import com.startek.biota.app.utils.StrUtils;

/**
 * NFC相關操作
 *
 * http://code.tutsplus.com/tutorials/reading-nfc-tags-with-android--mobile-17278
 */
public class NfcReader {

    private static final String TAG = "NfcReader";

    private Activity mActivity;
    private boolean showAlert;
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;

    public static final String[] actions = new String[]{
            NfcAdapter.ACTION_TAG_DISCOVERED,
            // NfcAdapter.ACTION_NDEF_DISCOVERED,
            // NfcAdapter.ACTION_TECH_DISCOVERED,
    };

    // http://terryyamg.blogspot.tw/2014/11/android-nfc-tag.html
    // list of NFC technologies detected:
//    private static final String[][] techList = new String[][] { new String[] {
//            NfcA.class.getName(),
//            NfcB.class.getName(),
//            NfcF.class.getName(),
//            NfcV.class.getName(),
//            IsoDep.class.getName(),
//            MifareClassic.class.getName(), MifareUltralight.class.getName(),
//            Ndef.class.getName()
//    }};

    public static final String[][] techList = null; // accept any format

    public NfcReader(Activity activity)
    {
        mActivity = activity;
        showAlert = Global.getConfig().getNfcAlert();
        mAdapter = NfcAdapter.getDefaultAdapter(mActivity);

        if (mAdapter == null)
        {
            String msg = mActivity.getString(R.string.nfc_not_support);
            Log.e(TAG, msg);

            if(showAlert)
                DialogHelper.alert(mActivity, msg);

            return;
        }

        mPendingIntent = PendingIntent.getActivity(mActivity, 0,
                new Intent(mActivity, mActivity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    public void attach()
    {
        if (mAdapter == null) return;

        if (!mAdapter.isEnabled())
        {
            String msg = mActivity.getString(R.string.nfc_is_disable);
            Log.e(TAG, msg);

            if(showAlert)
                DialogHelper.alert(mActivity, msg);

            return;
        }

        IntentFilter filter = new IntentFilter();
        for(String action:actions) filter.addAction(action);
        mAdapter.enableForegroundDispatch(mActivity, mPendingIntent, new IntentFilter[]{filter}, techList);
//        mAdapter.enableForegroundDispatch(mActivity, mPendingIntent, null, null);

        Log.d(TAG, "enableForegroundDispatch");
    }

    public void detach()
    {
        if (mAdapter == null) return;

        mAdapter.disableForegroundDispatch(mActivity);

        Log.d(TAG, "disableForegroundDispatch");
    }

    public EasyCard parse(Intent intent) {

        EasyCard easyCard = null;

        String intentAction = intent.getAction();
        Log.d(TAG, "intentAction = " + intentAction);

        boolean matched = false;
        for(String action:actions)
        {
            if(StrUtils.equals(action, intentAction))
            {
                matched = true;
                break;
            }
        }

        if(matched)
        {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if(tag!= null)
            {
                easyCard = new EasyCard(tag);
            }
        }

        return easyCard;
    }

//    public boolean isSupported()
//    {
//        return mAdapter != null;
//    }
//
//    public boolean isEnabled()
//    {
//        return mAdapter != null && mAdapter.isEnabled();
//    }
}
