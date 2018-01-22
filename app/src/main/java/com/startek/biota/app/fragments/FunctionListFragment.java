package com.startek.biota.app.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;

import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.hardware.FingerprintSensor;
import com.startek.biota.app.jobs.EmailJob;
import com.startek.biota.app.managers.FingerprintDeviceManager;
import com.startek.biota.app.network.webservices.WsAnnouncementR;
import com.startek.biota.app.network.webservices.WsFingerprintD;
import com.startek.biota.app.network.webservices.WsReminderR;
import com.startek.biota.app.utils.DialogHelper;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.Promise;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * 『設定畫面』
 *
 * 1. 按鈕『返回』，轉跳至『首頁-使用者登入』
 * 2. 按鈕『使用者管理』
 * 3. 按鈕『手動同步資料』
 * 4. 按鈕『運行記錄』
 * 5. 按鈕『系統設定』
 * 6. 按鈕『指紋機』
 * 7. 按鈕『門禁機』
 * 8: 等候畫面 系統發現沒有使用者(資料庫中沒有任何使用者資料)時,直接進入 [設定]畫面
 * 9. 只有管理者才能進入這個畫面
 * 10. 在 User 中 「管理者註記」ds_is_manager 欄位在紀錄時，需遵循以下邏輯：
 *      - 非空白時為管理者，空白時為使用者。
 *      - 內容為「記錄將該使用者提升為管理者」的操作者。
 *      - 第一個安裝的使用者 = installer
 */
public class FunctionListFragment extends BaseFragment implements View.OnClickListener {

    private ImageView buttonBack;
    private Button buttonUserManager;
    private Button buttonSyncData;
    private Button buttonLogging;
    private Button buttonSetting;
    private Button buttonFingerprint;
    private Button buttonDevice;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_function_list, container, false);

        buttonBack = (ImageView)v.findViewById(R.id.buttonBack);
        buttonUserManager = (Button)v.findViewById(R.id.buttonUserManager);
        buttonSyncData = (Button)v.findViewById(R.id.buttonSyncData);
        buttonLogging = (Button)v.findViewById(R.id.buttonLogging);
        buttonSetting = (Button)v.findViewById(R.id.buttonSetting);
        buttonFingerprint = (Button)v.findViewById(R.id.buttonFingerprint);
        buttonDevice = (Button)v.findViewById(R.id.buttonDevice);

        buttonBack.setOnClickListener( this );
        buttonUserManager.setOnClickListener(this);
        buttonSyncData.setOnClickListener(this);
        buttonLogging.setOnClickListener(this);
        buttonSetting.setOnClickListener(this);
        buttonFingerprint.setOnClickListener(this);
        buttonDevice.setOnClickListener(this);

        v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // Ensure you call it only once :
                v.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                // Here you can get the size :)

                // 動態調整圖片大小
                resizeDrawables(buttonUserManager, 0.35f);
                resizeDrawables(buttonSyncData, 0.35f);
                resizeDrawables(buttonLogging, 0.35f);
                resizeDrawables(buttonSetting, 0.35f);
                resizeDrawables(buttonFingerprint, 0.35f);
                resizeDrawables(buttonDevice, 0.35f);
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    /**
     * 載入使用者資料
     *  localHuman: 本機使用者資料
     *  remoteHuman: 遠端使用者資料
     */
    private void loadData()
    {
        DialogHelper.openNetworkProgress(context);

        humanManager.loadHuman().promise()
        .always(new AlwaysCallback() {
            @Override
            public void onAlways(Promise.State state, Object resolved, Object rejected) {
                DialogHelper.closeNetworkProgress();
                Global.setFullscreen(); // 20160725  所有連線完成 皆呼叫native bar hide
            }
        });
    }

    /**
     * 檢查本地端是否有資料需要同步到 SERVER
     *  提示使用者本機仍有資料尚未上傳到 SERVER
     */
    private boolean needSyncData()
    {
        return Global.getCache().queryDirtyData().size() != 0;
    }

    /**
     * 執行『返回』動作，回到上一頁(等待登入畫面)，需要清除目前登入者
     */
    private void logout()
    {
        Global.setLoginedUser(null);
        super.onBackPressed();
    }

    /**
     * 動態改變 Drawable 大小
     *
     * http://stackoverflow.com/questions/4502605/how-to-programatically-set-drawableleft-on-android-button
     * http://www.reader8.cn/jiaocheng/20130326/1391945.html
     */
    private void resizeDrawables(Button button, float scale)
    {
        int buttonWidth = button.getWidth();
        int buttonHeight = button.getHeight();
//        int buttonWidth = button.getMeasuredWidth();
//        int buttonHeight = button.getMeasuredHeight();

        // Left, top, right, bottom drawables.
        Drawable[] drawables = button.getCompoundDrawables();

        // get left drawable.

        int paddingTop = -1;

        for(Drawable drawable:drawables)
        {
            if(drawable != null)
            {
                float newDrawableWidth = (float)buttonWidth * scale;
                float newDrawableHeight = (newDrawableWidth/(float)drawable.getBounds().width()) * drawable.getBounds().height();
//                int w = drawable.getBounds().width();
//                int h = drawable.getBounds().height();
                int width = (int)newDrawableWidth;
                int height = (int)newDrawableHeight;
                drawable = new ScaleDrawable(drawable, 0, width, height).getDrawable();
                drawable.setBounds(0, 0, width, height);

                paddingTop = (buttonHeight - height)/2; // 置中
            }
        }

        button.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);

        // http://stackoverflow.com/questions/9685658/add-padding-on-view-programmatically
//        if(paddingTop != -1) button.setPadding(0, paddingTop, 0,0);
        button.setPadding(0, (int)((float)buttonHeight/3), 0,0); // 暫時先設定為 1/3 高度
    }

    @Override
    public void onBackPressed() {

        if(!humanManager.checkManagerCanLogin(Global.getHumanList()))
            return;

        if(needSyncData())
        {
            new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(getString(R.string.warn_title_need_sync_data))
                    .setContentText(getString(R.string.warn_content_no_need_sync_data))
                    .setConfirmText(context.getString(R.string.yes))
                    .setCancelText(context.getString(R.string.no))
                    .showCancelButton(true)
                    .showContentText(true)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();

                            logout();
                        }
                    })
                    .show();

            return;
        }

        logout();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.buttonBack: {
                onBackPressed();
            }
            break;

            case R.id.buttonUserManager: {
                showFragment(new UserManagerFragment1(), "UserManagerFragment1");
            }
            break;

            case R.id.buttonLogging: {
                showFragment(new LoggingFragment(), "LoggingFragment");
            }
            break;

            case R.id.buttonSetting: {
                showFragment(new SettingFragment(), "SettingFragment");
            }
            break;

            case R.id.buttonSyncData: {
                showFragment(new SyncDataFragment(), "SyncDataFragment");
            }
            break;

            case R.id.buttonFingerprint: {
                showFragment(new FingerprintFragment(), "FingerprintFragment");
            }
            break;

            case R.id.buttonDevice: {
                // TODO: JUST FOR TEST
//                Log.d(getLogTag(), "發送EMAIL");
//                new Thread(new EmailJob(getActivity(), EmailJob.ACTION_INTERNALLOG)).start();
//                new Thread(new EmailJob(context, EmailJob.ACTION_EMAILLOG)).start();

//                new WsAnnouncementR(context).execute();
//                new WsReminderR(context).execute();

//                new WsFingerprintD(context).execute();

//                new FingerprintDeviceManager(context).create(FingerprintSensor.findFirst(context));

//                new FingerprintDeviceManager(context).create(
//                        "deviceId",
//                        "companyId",
//                        "version",
//                        "speed",
//                        "company",
//                        "address",
//                        "product");
            }
            break;
        }
    }
}
