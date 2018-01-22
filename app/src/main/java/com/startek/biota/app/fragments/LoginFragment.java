package com.startek.biota.app.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.hardware.FingerprintSensor;
import com.startek.biota.app.jobs.EmailJob;
import com.startek.biota.app.managers.HumanManager;
import com.startek.biota.app.models.EasyCard;
import com.startek.biota.app.models.Human;
import com.startek.biota.app.models.Nfc;
import com.startek.biota.app.models.RunningLog;
import com.startek.biota.app.models.Team;
import com.startek.biota.app.network.webservices.WsAnnouncementR;
import com.startek.biota.app.network.webservices.WsReminderR;
import com.startek.biota.app.utils.Converter;
import com.startek.biota.app.utils.DialogHelper;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.DeferredManager;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DefaultDeferredManager;
import org.jdeferred.multiple.MultipleResults;
import org.jdeferred.multiple.OneResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class LoginFragment extends BaseFragment implements View.OnClickListener {

    //-------------------------------

    private WsAnnouncementR.Response remoteAnnouncement;
    private WsReminderR.Response remoteReminder;

    private FingerprintSensor.VerifyThread verifyThread;

//    private TimeoutTimer timeoutTimer;

    //-------------------------------

    private TextView textViewDate;
    private TextView textViewTime;
    private Button buttonNextUser;
    private ImageView buttonSetting;

    private Button keyboard0;
    private Button keyboard1;
    private Button keyboard2;
    private Button keyboard3;
    private Button keyboard4;
    private Button keyboard5;
    private Button keyboard6;
    private Button keyboard7;
    private Button keyboard8;
    private Button keyboard9;
    private Button keyboardC;
    private Button keyboardBack;


    private TextView textViewEmployeeId;

    private RelativeLayout progressBarLayout;
    private TextView textViewFingerprint;
    private ProgressBar progressBar;
    private ImageView imageViewFingerprint;

    private LinearLayout linearLayoutUserProfiler1;
    private TextView textViewNameAndDept;

    private LinearLayout linearLayoutUserProfiler2;
    private LinearLayout linearLayoutTeamStatusContainer;

    private LinearLayout linearLayoutReminder;
    private ListView listViewReminder;
    private ListViewAdapter listViewAdapterReminder;

    private LinearLayout linearLayoutAnnouncement;
    private ListView listViewAnnouncement;
    private ListViewAdapter listViewAdapterAnnouncement;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        textViewDate = (TextView)v.findViewById(R.id.textViewDate);
        textViewTime = (TextView)v.findViewById(R.id.textViewTime);
        buttonNextUser = (Button)v.findViewById(R.id.buttonNextUser);
        buttonSetting = (ImageView)v.findViewById(R.id.buttonSetting);

        keyboard0 = (Button)v.findViewById(R.id.keyboard0);
        keyboard1 = (Button)v.findViewById(R.id.keyboard1);
        keyboard2 = (Button)v.findViewById(R.id.keyboard2);
        keyboard3 = (Button)v.findViewById(R.id.keyboard3);
        keyboard4 = (Button)v.findViewById(R.id.keyboard4);
        keyboard5 = (Button)v.findViewById(R.id.keyboard5);
        keyboard6 = (Button)v.findViewById(R.id.keyboard6);
        keyboard7 = (Button)v.findViewById(R.id.keyboard7);
        keyboard8 = (Button)v.findViewById(R.id.keyboard8);
        keyboard9 = (Button)v.findViewById(R.id.keyboard9);
        keyboardC = (Button)v.findViewById(R.id.keyboardC);
        keyboardBack = (Button)v.findViewById(R.id.keyboardBack);

        buttonNextUser.setOnClickListener(this);
        buttonSetting.setOnClickListener(this);
        keyboard0.setOnClickListener(this);
        keyboard1.setOnClickListener(this);
        keyboard2.setOnClickListener(this);
        keyboard3.setOnClickListener(this);
        keyboard4.setOnClickListener(this);
        keyboard5.setOnClickListener(this);
        keyboard6.setOnClickListener(this);
        keyboard7.setOnClickListener(this);
        keyboard8.setOnClickListener(this);
        keyboard9.setOnClickListener(this);
        keyboardC.setOnClickListener(this);
        keyboardBack.setOnClickListener(this);

        textViewEmployeeId = (TextView)v.findViewById(R.id.textViewEmployeeId);

        progressBarLayout = (RelativeLayout)v.findViewById(R.id.progressBarLayout);
        textViewFingerprint = (TextView)v.findViewById(R.id.textViewFingerprint);
        progressBar = (ProgressBar)v.findViewById(R.id.progressBar);
        imageViewFingerprint = (ImageView)v.findViewById(R.id.imageViewFingerprint);

        linearLayoutUserProfiler1 = (LinearLayout)v.findViewById(R.id.linearLayoutUserProfiler1);
        textViewNameAndDept = (TextView)v.findViewById(R.id.textViewNameAndDept);

        linearLayoutUserProfiler2 = (LinearLayout)v.findViewById(R.id.linearLayoutUserProfiler2);
        linearLayoutTeamStatusContainer = (LinearLayout)v.findViewById(R.id.linearLayoutTeamStatusContainer);

        linearLayoutReminder = (LinearLayout)v.findViewById(R.id.linearLayoutReminder);
        listViewReminder = (ListView)v.findViewById(R.id.listViewReminder);
        listViewAdapterReminder = new ListViewAdapter();
        listViewReminder.setAdapter(listViewAdapterReminder);

        linearLayoutAnnouncement = (LinearLayout)v.findViewById(R.id.linearLayoutAnnouncement);
        listViewAnnouncement = (ListView)v.findViewById(R.id.listViewAnnouncement);
        listViewAdapterAnnouncement = new ListViewAdapter();
        listViewAnnouncement.setAdapter(listViewAdapterAnnouncement);

        // ----------------------------------------
        // 無登入者的畫面
        // ----------------------------------------

        // 上方
        Date now = new Date();
        textViewDate.setText(Converter.toString(now, Converter.DateTimeFormat.YYYYMMdd_E));
        textViewTime.setText(Converter.toString(now, Converter.DateTimeFormat.HHmmss));
        buttonNextUser.setVisibility(View.GONE);
        buttonSetting.setVisibility(View.GONE);

        // 左側
        textViewEmployeeId.setText("");

        updateFinger(FINGERPRINT_SHOW_NONE);

        // 右側
        textViewNameAndDept.setText("");
        linearLayoutUserProfiler2.setVisibility(View.GONE);
        linearLayoutReminder.setVisibility(View.GONE);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        clockTimer.start();

        if(!isEasyCardReceived())
        {
            loadData(new DoneCallback() {
                @Override
                public void onDone(Object result) {
                    checkHuman();
                }
            });
        }
    }

    private void superOnBackPressed()
    {
        super.onBackPressed();
    }

    @Override
    public void onBackPressed()
    {
        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.warn_title_exit))
                .setContentText(getString(R.string.warn_content_exit))
                .setConfirmText(context.getString(R.string.yes))
                .setCancelText(context.getString(R.string.no))
                .showCancelButton(true)
                .showContentText(true)
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        superOnBackPressed(); // 使用 finish 會出錯，請使用以下方式
                        //System.exit(0);
                    }
                })
                .show();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        clockTimer.cancel();
    }

    private boolean humanListLoaded = false; // 修正外部觸發 NFC 導致無窮回圈

    @Override
    public void onEasyCardReceived(final EasyCard easyCard)
    {
        super.onEasyCardReceived(easyCard);

        if(Global.getHumanList().size() == 0 && humanListLoaded == false)
        {
            // 修正外部觸發 NFC
            loadData(new DoneCallback() {
                @Override
                public void onDone(Object result) {
                    humanListLoaded = true;
                    onEasyCardReceived(easyCard);
                    humanListLoaded = false; // reset flag
                }
            });
            return;
        }

        // 20160429 Norman, 將判斷移到這裡進行用以嘗試解決 【找不到對應使用者資訊】
        boolean hasManagerCanLogin = humanManager.hasManagerCanLogin(Global.getHumanList());
        if(!hasManagerCanLogin)
        {
            Log.d(getLogTag(), "此手機尚無管理者可進入設定畫面");

            Global.setLoginedUser(null);
            showFragment(new FunctionListFragment(), "FunctionListFragment");
            return;
        }

        if(Global.getLoginedUser() != null)
        {
            DialogHelper.alert(context, getString(R.string.press_next_user));
            return; // 必須使用『下一個』按鈕，才可繼續按
        }

        Nfc nfc = HumanManager.getNfcByEasyCard(easyCard);
        if(nfc == null)
        {
            DialogHelper.alert(context, getString(R.string.no_human_found));
            return;
        }

        Human human = HumanManager.getHumanByNfc(nfc);
        if(human == null)
        {
            DialogHelper.alert(context, getString(R.string.no_human_found));
            return;
        }

        String bind_id = human.bind_id;

        // 避開重複偵測相同 tagId
        if(textViewEmployeeId.getText().equals(bind_id))return;

        if(isVerifying())
        {
            DialogHelper.alert(context, getString(R.string.wait_for_verify_complete));
            return;
        }

        textViewEmployeeId.setText(bind_id);

        verify(human, nfc);
    }

    // ----------------------------------------
    // loadData
    // ----------------------------------------

    /**
     * 載入相關資料
     *  1. 來自『手機端』的使用者
     *  2. 來自『伺服器』的使用者
     *  3. Announcement
     *  4. Reminder
     */
    private void loadData(final DoneCallback doneCallback)
    {
        remoteAnnouncement = null;
        remoteReminder = null;

        if(Global.getConfig().useBiotaServer())
        {
            DialogHelper.openNetworkProgress(context);

            DeferredManager dm = new DefaultDeferredManager();
            dm.when(humanManager.loadHuman().promise(),
                    new WsAnnouncementR(context).execute().promise(),
                    new WsReminderR(context).execute().promise())
                    .done(new DoneCallback<MultipleResults>() {
                        @Override
                        public void onDone(MultipleResults result) {
                            OneResult oneResult1 = result.get(1);
                            OneResult oneResult2 = result.get(2);

                            WsAnnouncementR.Response res1 = (WsAnnouncementR.Response) oneResult1.getResult();
                            WsReminderR.Response res2 = (WsReminderR.Response) oneResult2.getResult();

                            // if (res1.result.success) // TODO: 通知 Light 這個回傳的 success 應該為 true
                            remoteAnnouncement = (WsAnnouncementR.Response) oneResult1.getResult();

                            // if (res2.result.success) // TODO: 通知 Light 這個回傳的 success 應該為 true
                            remoteReminder = (WsReminderR.Response) oneResult2.getResult();

                            // checkHuman();
                            if (doneCallback != null) doneCallback.onDone(Global.getHumanList());
                        }
                    }) // .done(new DoneCallback<MultipleResults>() {
                    .fail(new FailCallback() {
                        @Override
                        public void onFail(Object result) {
                            // checkHuman();
                            if (doneCallback != null) doneCallback.onDone(Global.getHumanList());
                        }
                    })
                    .always(new AlwaysCallback() {
                        @Override
                        public void onAlways(Promise.State state, Object resolved, Object rejected) {
                            DialogHelper.closeNetworkProgress();
                            Global.setFullscreen(); // 20160725, 所有連線完成 皆呼叫native bar hide
                        }
                    });
        }
        else
        {
//            humanManager.loadHuman().promise().done(new DoneCallback() {
//                @Override
//                public void onDone(Object result) {
//                    // checkHuman();
//                    if(doneCallback != null) doneCallback.onDone(Global.getHumanList());
//                }
//            });

            DeferredManager dm = new DefaultDeferredManager();
            dm.when(humanManager.loadHuman().promise(),
                    WsAnnouncementR.getLocalData(),
                    WsReminderR.getLocalData())
                    .done(new DoneCallback<MultipleResults>() {

            @Override
            public void onDone(MultipleResults result) {
                OneResult oneResult1 = result.get(1);
                OneResult oneResult2 = result.get(2);

                WsAnnouncementR.Response res1 = (WsAnnouncementR.Response) oneResult1.getResult();
                WsReminderR.Response res2 = (WsReminderR.Response) oneResult2.getResult();

                // if (res1.result.success) // TODO: 通知 Light 這個回傳的 success 應該為 true
                remoteAnnouncement = (WsAnnouncementR.Response) oneResult1.getResult();

                // if (res2.result.success) // TODO: 通知 Light 這個回傳的 success 應該為 true
                remoteReminder = (WsReminderR.Response) oneResult2.getResult();

                // checkHuman();
                if (doneCallback != null) doneCallback.onDone(Global.getHumanList());
            }
        });
        }
    }

    /**
     * 檢查是否有安裝使用者
     *  如果沒有的話，則直接進入設定畫面，進行初次安裝作業。
     *  如果有的話，更新畫面
     */
    private void checkHuman()
    {
        boolean hasManagerCanLogin = humanManager.hasManagerCanLogin(Global.getHumanList());

        if(!hasManagerCanLogin)
        {
            Log.d(getLogTag(), "此手機尚無管理者可進入設定畫面");

            Global.setLoginedUser(null);
            showFragment(new FunctionListFragment(), "FunctionListFragment");
            return;
        }

        updateUi(null);
    }

    private void updateUi(Human human)
    {
        Global.setLoginedUser(human);

        if(human == null)
        {
            // --------------------
            // 沒有登入者
            // --------------------

            // 上方
            buttonNextUser.setVisibility(View.GONE);
            buttonSetting.setVisibility(View.GONE);

            // 左側
            textViewEmployeeId.setText("");

            updateFinger(FINGERPRINT_SHOW_NONE);

            // 右側
            textViewNameAndDept.setText("");
            linearLayoutUserProfiler2.setVisibility(View.GONE);
            linearLayoutReminder.setVisibility(View.GONE);

            loadAnnouncements();

//            buttonNextUser.setVisibility(View.VISIBLE); // TODO: 測試用，未來請移除
//            buttonSetting.setVisibility(View.VISIBLE); // TODO: 測試用，未來請移除
        }
        else
        {
            // --------------------
            // 有登入者
            // --------------------

            // 上方
            buttonNextUser.setVisibility(View.VISIBLE);
            buttonSetting.setVisibility(human.isManager() ? View.VISIBLE : View.GONE);

            // 左側
            textViewEmployeeId.setText(human.bind_id);

            // 右側
            textViewNameAndDept.setText(String.format("%s / %s", human.name, human.dept));

            linearLayoutUserProfiler2.setVisibility(View.VISIBLE);
            loadTeamStatus(human);

            linearLayoutReminder.setVisibility(View.VISIBLE);

            loadReminders();
            loadAnnouncements();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id)
        {
            case R.id.buttonNextUser:
            case R.id.buttonSetting:
            {
                onButtonClick(v);
            }break;

            default:
            {
                onKeyboardClick(v);
            }break;
        }
    }

    public void onButtonClick(View view)
    {
        int id = view.getId();

        switch (id)
        {
            case R.id.buttonNextUser:
            {
                updateUi(null);
            }break;

            case R.id.buttonSetting:
            {
                showFragment(new FunctionListFragment(), "FunctionListFragment");
            }break;
        }
    }

    public void onKeyboardClick(View view)
    {
        if(Global.getLoginedUser() != null)
        {
            DialogHelper.alert(context, getString(R.string.press_next_user));
            return; // 必須使用『下一個』按鈕，才可繼續按
        }

        int id = view.getId();

        switch (id)
        {
            case R.id.keyboard0:
            {
                if(!addCharacter("0"))return;
            }break;

            case R.id.keyboard1:
            {
                if(!addCharacter("1"))return;
            }break;

            case R.id.keyboard2:
            {
                if(!addCharacter("2"))return;
            }break;

            case R.id.keyboard3:
            {
                if(!addCharacter("3"))return;
            }break;

            case R.id.keyboard4:
            {
                if(!addCharacter("4"))return;
            }break;

            case R.id.keyboard5:
            {
                if(!addCharacter("5"))return;
            }break;

            case R.id.keyboard6:
            {
                if(!addCharacter("6"))return;
            }break;

            case R.id.keyboard7:
            {
                if(!addCharacter("7"))return;
            }break;

            case R.id.keyboard8:
            {
                if(!addCharacter("8"))return;
            }break;

            case R.id.keyboard9:
            {
                if(!addCharacter("9"))return;
            }break;

            case R.id.keyboardC:
            {
                // TODO: JUST FOR TEST
//                Log.d(getLogTag(), "發送EMAIL");
//                new Thread(new EmailJob(context, EmailJob.ACTION_INTERNALLOG)).start();

                textViewEmployeeId.setText("");

                if(isVerifying())
                    stopVerify();

                return;
            }

            case R.id.keyboardBack:
            {
                String origin = textViewEmployeeId.getText().toString();
                if(origin.length() != 0)
                {
                    textViewEmployeeId.setText(origin.substring(0, origin.length()-1));

                    if(isVerifying())
                        stopVerify();

                    return;
                }
            }
        }

        String employeeId = textViewEmployeeId.getText().toString();
        if(Global.getConfig().getMaxEmployeeIdLength() == employeeId.length())
        {
            Human human = HumanManager.getHumanByBindId(employeeId);

            if(human != null)
            {
                verify(human, null);
            }
            else
            {
                DialogHelper.alert(context, getString(R.string.no_human_found));
            }
        }
    }

    private boolean addCharacter(String c)
    {
        final int allowLength = Global.getConfig().getMaxEmployeeIdLength();

        String origin = textViewEmployeeId.getText().toString();
        if(origin.length() < allowLength)
        {
            textViewEmployeeId.setText(origin + c);
            return true;
        }

        return false;
    }

    /**
     * 載入 TeamStatus 資訊
     *
     * reference
     *  http://sawchenko.net/blog/android/2013/09/01/Setting-Layout-Programmatically/
     */
    private void loadTeamStatus(Human human)
    {
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        linearLayoutTeamStatusContainer.removeAllViews();

        for(Team team:human.getTeams())
        {
            Human member = HumanManager.getHumanById(Integer.toString(team.memberId)); // TODO: memberId型別(int)應該與 humanId(String) 一致才對

            String name = "-"; // 找不到對應使用者

            if(member != null)
            {
                name = member.name;
            }
            else
            {
                name = Integer.toString(team.memberId); // 20160515, 顯示 id 代表沒有找到對應使用者
            }

            View v = vi.inflate(R.layout.team_status, null);

            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            v.setLayoutParams(layoutParams2);

            TextView textView = (TextView) v.findViewById(R.id.textViewTeamStatus);

            textView.setText(name);

            textView.setTextColor(context.getResources().getColor(team.arrived ? R.color.colorTextAttendence : R.color.colorTextAbsence));
            textView.setBackgroundColor(context.getResources().getColor(team.arrived ? R.color.colorBackgroundAttendence : R.color.colorBackgroundAbsence));
//            textView.setVisibility(!team.arrived ? View.INVISIBLE : View.VISIBLE);

            linearLayoutTeamStatusContainer.addView(v);
        }

    }

    private void loadAnnouncements()
    {
        WsAnnouncementR.Response res = remoteAnnouncement;

        List<ListItemData> data = new ArrayList<ListItemData>();

        if(res != null)
        {
            for(WsAnnouncementR.Response.Delta aData:res.data)
            {
                ListItemData delta = new ListItemData();
                delta.setItem1(aData.subject);
                delta.setItem2(aData.tricker);
                delta.setItem3(aData.time);
                data.add(delta);
            }
        }

        listViewAdapterAnnouncement.setData(data);
    }

    private void loadReminders()
    {
        WsReminderR.Response res = remoteReminder;

        List<ListItemData> data = new ArrayList<ListItemData>();

        if(res != null)
        {
            for(WsReminderR.Response.Delta aData:res.data)
            {
                ListItemData delta = new ListItemData();
                delta.setItem1(aData.subject);
                delta.setItem2(aData.tricker);
                delta.setItem3(aData.time);
                data.add(delta);
            }
        }

        listViewAdapterReminder.setData(data);
    }

    // ----------------------------------------
    // Verification
    // ----------------------------------------

    /**
     * 是否正在進行驗證動作
     */
    private boolean isVerifying()
    {
        return (verifyThread != null);
    }

    /**
     * 停止驗證動作
     */
    private boolean stopVerify()
    {
        if(verifyThread != null)
        {
            Handler handler = verifyThread.getHandler();

            Message m = handler.obtainMessage(FingerprintSensor.EVENT_STOP_IMMEDIATELY, -1, -1, null);

            boolean result = handler.sendMessage(m);

            resetVerifyThread();

            return result;
        }

        return false;
    }

    /**
     * 取得目前驗證的使用者
     */
    private Human getVerifiedHuman()
    {
        if(verifyThread == null)return null;

        return verifyThread.getHuman();
    }

    /**
     * 執行驗證動作
     */
    private boolean verify(Human human, Nfc nfc)
    {
        FingerprintSensor sensor = getFingerprintSensor();

        if(sensor == null)
        {
            DialogHelper.alert(context, getString(R.string.no_fingerprint_sensor));
            return false;
        }

        if(verifyThread != null)
        {
            DialogHelper.alert(context, getString(R.string.sensor_in_used));
            return false;
        }

//        if(timeoutTimer != null)
//        {
//            timeoutTimer.cancel();
//            timeoutTimer = null;
//        }
//
//        timeoutTimer = new TimeoutTimer(1000 * timeoutSeconds);
//        timeoutTimer.start();

        int timeoutSeconds = Global.getConfig().getVerifyTimeout();
        String client_action = getString(R.string.client_action_verify);
        verifyThread = sensor.verify(verifyHandler, human, nfc, timeoutSeconds, client_action);

        return true;
    }

    private void resetVerifyThread()
    {
//        if(timeoutTimer != null)
//        {
//            timeoutTimer.cancel();
//            timeoutTimer = null;
//        }

        verifyThread = null;
    }

    private Handler verifyHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            try
            {
                int action = msg.what;
//                String message = (String)msg.obj;

                switch (action)
                {
                    case FingerprintSensor.EVENT_STOP_IMMEDIATELY:
                    {
                        updateFinger(FINGERPRINT_SHOW_NONE);
                        resetVerifyThread();
                    }break;

                    case FingerprintSensor.EVENT_CAPTURE_TIMEOUT:
                    {
                        logMe((String)msg.obj);
                        updateFinger(FINGERPRINT_SHOW_NONE);
                        updateUi(null);
                        resetVerifyThread();
                    }break;

                    case FingerprintSensor.EVENT_EXCEPTION:
                    {
                        logMe((Exception)msg.obj);
                        updateFinger(FINGERPRINT_SHOW_NONE);
                        updateUi(null);
                        resetVerifyThread();
                    }break;

                    case FingerprintSensor.EVENT_VERIFY_ACTION_FINGER_PRESS:
                    {
                        logMe((String)msg.obj);
                        updateFinger(FINGERPRINT_SHOW_PROGRESSBAR);
                    }break;

                    case FingerprintSensor.EVENT_VERIFY_ACTION_SHOW_IMAGE:
                    {
                        ImageView imageView = imageViewFingerprint;
                        setImageView(imageView, (Bitmap) msg.obj);
                        updateFinger(FINGERPRINT_SHOW_IMAGEVIEW);
                    }break;

                    case FingerprintSensor.EVENT_VERIFY_ACTION_SAVE_IMAGE: {
                        ImageView imageView = imageViewFingerprint;
                        String filepath = (String)msg.obj;
                        setImageView(imageView, filepath);
                        updateFinger(FINGERPRINT_SHOW_IMAGEVIEW);
                    }break;

                    case FingerprintSensor.EVENT_VERIFY_ACTION_VERIFY_START:
                    {
                        logMe((String)msg.obj);
                        updateFinger(FINGERPRINT_SHOW_IMAGEVIEW);
                    }break;

                    case FingerprintSensor.EVENT_VERIFY_RESULT_SUCCESS:
                    {
                        Human human = verifyThread.getHuman();

                        Global.getCache().createRunningLog(
                                RunningLog.CATEGORY_USER_IN_OUT,
                                getString(R.string.runninglog_event_verification),
                                human.name,
                                getString(R.string.runninglog_description_verification),
                                getString(R.string.result_success),
                                true);

                        logMe((String)msg.obj);
                        updateFinger(FINGERPRINT_SHOW_IMAGEVIEW);
                        updateUi(human);
                        resetVerifyThread();
                    }break;

                    case FingerprintSensor.EVENT_VERIFY_RESULT_FAILURE:
                    {
                        Human human = verifyThread.getHuman();

                        Global.getCache().createRunningLog(
                                RunningLog.CATEGORY_USER_IN_OUT,
                                getString(R.string.runninglog_event_verification),
                                human.name,
                                getString(R.string.runninglog_description_verification),
                                getString(R.string.result_failure),
                                true);

                        logMe(new Exception((String)msg.obj));
                        updateFinger(FINGERPRINT_SHOW_NONE);
                        updateUi(null);
                        resetVerifyThread();
                    }break;
                }
                super.handleMessage(msg);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();

                Global.getCache().createRunningLog(RunningLog.CATEGORY_USER_IN_OUT, ex);

                String error = Converter.getStackTrace(ex);
//                String error = Converter.getMessage(ex);
                DialogHelper.alert(context, error, DialogHelper.EXCEPTION_DURATION);
            }
        }
    };

    private void logMe(final Object o)
    {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if ((o instanceof String) == false && (o instanceof Exception) == false) return;

                boolean isError = false;
                String message = "";

                if (o instanceof Exception) {
                    isError = true;
                    message = ((Exception) o).getMessage();
                }

                if (o instanceof String) {
                    isError = false;
                    message = (String) o;
                }

                if (isError) {
                    DialogHelper.alert(context, message, 1000);
                } else {
                    DialogHelper.info(context, message, 500);
                }
            }
        });
    }

    private void setImageView(ImageView imageView, String filepath)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(filepath, options);
        imageView.setImageBitmap(bitmap);
    }

    private void setImageView(ImageView imageView, Bitmap bitmap)
    {
        if(bitmap != null)
        {
            imageView.setImageBitmap(bitmap);
            bitmap = null;
            System.gc();
        }
    }

    // ----------------------------------------
    // updateFinger
    // ----------------------------------------

    private static final int FINGERPRINT_SHOW_NONE = 0;
    private static final int FINGERPRINT_SHOW_IMAGEVIEW = 1;
    private static final int FINGERPRINT_SHOW_PROGRESSBAR = 2;

    private void updateFinger(int state)
    {
        switch(state)
        {
            case FINGERPRINT_SHOW_NONE:
            {
                progressBarLayout.setVisibility(View.VISIBLE);
                textViewFingerprint.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                imageViewFingerprint.setVisibility(View.GONE);
            }break;

            case FINGERPRINT_SHOW_IMAGEVIEW:
            {
                progressBarLayout.setVisibility(View.GONE);
                textViewFingerprint.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                imageViewFingerprint.setVisibility(View.VISIBLE);
            }break;

            case FINGERPRINT_SHOW_PROGRESSBAR:
            {
                progressBarLayout.setVisibility(View.VISIBLE);
                textViewFingerprint.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                imageViewFingerprint.setVisibility(View.GONE);
            }break;
        }
    }

    // ----------------------------------------
    // CountDownTimer
    // ----------------------------------------

    class TimeoutTimer extends CountDownTimer
    {
        public TimeoutTimer(long millisInFuture)
        {
            super(millisInFuture, 1000);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            int seconds = (int)(millisUntilFinished/1000);
//            textViewFingerprint.setText(String.format(getString(R.string.textViewFingerprintWithCountDown), seconds));
        }

        @Override
        public void onFinish() {
            stopVerify();
        }
    }

    // ----------------------------------------
    // CountDownTimer
    // ----------------------------------------



    private final CountDownTimer clockTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {

        public void onTick(long millisUntilFinished) {
            Date now = new Date();
            textViewDate.setText(Converter.toString(now, Converter.DateTimeFormat.YYYYMMdd_E));
            textViewTime.setText(Converter.toString(now, Converter.DateTimeFormat.HHmmss));
        }

        public void onFinish() {
            this.start(); // infinite loop
        }
    };

    // ----------------------------------------
    // ListItemData & ListViewAdapter
    // ----------------------------------------

    class ListItemData
    {
        private String item1;
        private String item2;
        private String item3;

        public String getItem1() {
            return item1;
        }

        public void setItem1(String item1) {
            this.item1 = item1;
        }

        public String getItem2() {
            return item2;
        }

        public void setItem2(String item2) {
            this.item2 = item2;
        }

        public String getItem3() {
            return item3;
        }

        public void setItem3(String item3) {
            this.item3 = item3;
        }
    }

    class ListViewAdapter extends BaseAdapter
    {
        private List<ListItemData> data;
        private int layoutId;

        public void setData(List<ListItemData> data)
        {
            this.data = data;

            notifyDataSetChanged();
        }

        public ListViewAdapter()
        {
            this.data = new ArrayList<ListItemData>();
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            try
            {
                final ViewHolder holder;

                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.list_item_login, parent, false);

                    holder = new ViewHolder();
                    holder.textViewItem1 = (TextView) convertView.findViewById(R.id.textViewItem1);
                    holder.textViewItem2 = (TextView) convertView.findViewById(R.id.textViewItem2);
                    holder.textViewItem3 = (TextView) convertView.findViewById(R.id.textViewItem3);

                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                ListItemData delta = data.get(position);

                holder.textViewItem1.setText(delta.getItem1());
                holder.textViewItem2.setText(delta.getItem2());
                holder.textViewItem3.setText(delta.getItem3());

//                int iColor = ContextCompat.getColor(context, position % 2 == 0 ? R.color.colorRow1 : R.color.colorRow2);
//                holder.textViewItem1.setBackgroundColor(iColor);
//                holder.textViewItem2.setBackgroundColor(iColor);
//                holder.textViewItem3.setBackgroundColor(iColor);

                return convertView;
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                return null;
            }
        }

        class ViewHolder {

            private TextView textViewItem1;
            private TextView textViewItem2;
            private TextView textViewItem3;

            public TextView getTextViewItem1() {
                return textViewItem1;
            }

            public TextView getTextViewItem2() {
                return textViewItem2;
            }

            public TextView getTextViewItem3() {
                return textViewItem3;
            }
        }
    }

}

