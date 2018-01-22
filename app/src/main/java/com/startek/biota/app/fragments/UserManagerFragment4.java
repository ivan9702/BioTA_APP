package com.startek.biota.app.fragments;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.hardware.FingerprintSensor;
import com.startek.biota.app.managers.FileManager;
import com.startek.biota.app.models.Fingerprint;
import com.startek.biota.app.models.Human;
import com.startek.biota.app.models.RunningLog;
import com.startek.biota.app.utils.Converter;
import com.startek.biota.app.utils.DialogHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * UserManagerActivity1: 『使用者管理』
 * UserManagerActivity2: 『使用者管理-新增畫面』
 * UserManagerActivity3: 『使用者管理-資料驗證』
 * UserManagerActivity4: 『使用者管理-使用者編輯-指紋建檔』
 *
 * 『使用者管理-使用者編輯-指紋建檔』
 *
 * 1. 按鈕『返回』，轉跳至前一頁
 * 2. 按鈕『儲存』，儲存所有變更
 * 3: 姓名, 工號, 職稱, 生日, 性別, 血型不可編輯
 * 4: 指紋建檔，每一支手指必須建立三筆資料
 * 5: 以下操作，會進入此功能頁
 *     「新增」或「編輯」時，按下「指紋圈圈」後的子畫面功能頁。
 *
 * Loading Indicator
 * http://stackoverflow.com/questions/5442183/using-the-animated-circle-in-an-imageview-while-loading-stuff
 *
 * (1) 每次掃描指紋機，都要三次才會 enroll 成功
 * (2) 示意圖顯示需要三個指紋圖，這是否意味，每個指紋需要三個dat檔
 *
 * 在【新增指紋資料】或【編輯指紋資料】，在儲存資料時，
 只要在手機端，儲存3各bitmap加上1各dat檔
 不需要上傳任何資訊到SERVER

 http://cosmochen.pixnet.net/blog/post/54819536-如何自動調控android-imageview的大小
 */
public class UserManagerFragment4 extends BaseFragment implements View.OnClickListener {

    private int fingerBtnId;
    private Human human;
    private boolean isScaning;

    public static final int maxScanTimes = 3;
    private String[] bmpFilename = new String[maxScanTimes];
    private String datFilename;
    private String minutiae; // base64(特徵點資料)

    // ----------------------------------------

    private ImageView buttonBack;
    private Button buttonSacn;
    private Button buttonSave;

    private TableRow rowName;
    private TextView textViewName;
    private TableRow rowEmployeeId;
    private TextView textViewEmployeeId;
    private TableRow rowTitle;
    private TextView textViewTitle;
    private TableRow rowBirthday;
    private TextView textViewBirthday;
    private TableRow rowGender;
    private TextView textViewGender;
    private TableRow rowBloodType;
    private TextView textViewBloodType;

    private TextView labelWhich;
    private List<RelativeLayout> progressBarLayouts;
    private List<ProgressBar> progressBars;
    private List<ImageView> imageViews;

    private TextView textViewMessage;
    private boolean hasEnrolled;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_manager4, container, false);

        buttonBack = (ImageView)v.findViewById(R.id.buttonBack);
        buttonSacn = (Button)v.findViewById(R.id.buttonSacn);
        buttonSave = (Button)v.findViewById(R.id.buttonSave);

        rowName = (TableRow)v.findViewById(R.id.rowName);
        textViewName = (TextView)v.findViewById(R.id.textViewName);
        rowEmployeeId = (TableRow)v.findViewById(R.id.rowEmployeeId);
        textViewEmployeeId = (TextView)v.findViewById(R.id.textViewEmployeeId);
        rowTitle = (TableRow)v.findViewById(R.id.rowTitle);
        textViewTitle = (TextView)v.findViewById(R.id.textViewTitle);
        rowBirthday = (TableRow)v.findViewById(R.id.rowBirthday);
        textViewBirthday = (TextView)v.findViewById(R.id.textViewBirthday);
        rowGender = (TableRow)v.findViewById(R.id.rowGender);
        textViewGender = (TextView)v.findViewById(R.id.textViewGender);
        rowBloodType = (TableRow) v.findViewById(R.id.rowBloodType);
        textViewBloodType = (TextView)v.findViewById(R.id.textViewBloodType);

        labelWhich = (TextView)v.findViewById(R.id.labelWhich);

        progressBarLayouts = new ArrayList<RelativeLayout>();
        progressBarLayouts.add((RelativeLayout)v.findViewById(R.id.progressBarLayout0));
        progressBarLayouts.add((RelativeLayout)v.findViewById(R.id.progressBarLayout1));
        progressBarLayouts.add((RelativeLayout)v.findViewById(R.id.progressBarLayout2));

        progressBars = new ArrayList<ProgressBar>();
        progressBars.add((ProgressBar)v.findViewById(R.id.progressBar0));
        progressBars.add((ProgressBar)v.findViewById(R.id.progressBar1));
        progressBars.add((ProgressBar)v.findViewById(R.id.progressBar2));

        imageViews = new ArrayList<ImageView>();
        imageViews.add((ImageView)v.findViewById(R.id.imageViewFingerprint0));
        imageViews.add((ImageView)v.findViewById(R.id.imageViewFingerprint1));
        imageViews.add((ImageView)v.findViewById(R.id.imageViewFingerprint2));

        textViewMessage = (TextView)v.findViewById( R.id.textViewMessage );

        fingerBtnId = Global.getEditedFingerResId();
        human = Global.getEditedHuman();
        hasEnrolled = false;

        buttonSave.setVisibility(View.GONE);

        textViewName.setText(human.name);
        textViewEmployeeId.setText(human.bind_id);
        textViewTitle.setText(human.job);
        textViewBirthday.setText(human.birthday);
        textViewGender.setText(human.gender);
        textViewBloodType.setText(human.bloodtype);

        labelWhich.setText(Converter.fingerBtnIdToChinese(fingerBtnId));

        buttonBack.setOnClickListener(this);
        buttonSacn.setOnClickListener(this);
        buttonSave.setOnClickListener(this);

        initImages();

        return v;
    }

    private void test()
    {
        updateFinger(0, SHOW_PROGRESSBAR);
        updateFinger(1, SHOW_IMAGEVIEW);
        updateFinger(2, SHOW_IMAGEVIEW);

        String path = FileManager.getEnrollBmpPath(fingerBtnId, 1);
        ImageView imageView = imageViews.get(1);
        setImageView(imageView, path);

        path = "http://203.66.65.139/test/f_sample.png";
        imageView = imageViews.get(2);
        Glide.with(context).load(path).into(imageView);
    }

    private static final int SHOW_NONE = 0;
    private static final int SHOW_IMAGEVIEW = 1;
    private static final int SHOW_PROGRESSBAR = 2;

    private void updateFinger(int scanTime, int onlyWhat)
    {
        RelativeLayout progressBarLayout = progressBarLayouts.get(scanTime);
        ProgressBar progressBar = progressBars.get(scanTime);
        ImageView imageView = imageViews.get(scanTime);

        switch (onlyWhat)
        {
            case SHOW_NONE:
            {
                progressBarLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
            }break;

            case SHOW_IMAGEVIEW:
            {
                progressBarLayout.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
            }break;

            case SHOW_PROGRESSBAR:
            {
                progressBarLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
            }break;
        }
    }

    private void initImages()
    {
        updateFinger(0, SHOW_NONE);
        updateFinger(1, SHOW_NONE);
        updateFinger(2, SHOW_NONE);

        Fingerprint fingerprint = human.getFingerprint(fingerBtnId);
        if(fingerprint != null)
        {
            for(int scanTime = 0; scanTime<maxScanTimes; scanTime++)
            {
                ImageView imageView = imageViews.get(scanTime);

                String filepath = FileManager.getBmpPath(human, fingerBtnId, scanTime);

                if(FileManager.exists(filepath))
                {
                    updateFinger(scanTime, SHOW_IMAGEVIEW);

                    setImageView(imageView, filepath);
                }
                else
                {
                    if(!TextUtils.isEmpty(fingerprint.pic))
                    {
                        final int finalScanTime = scanTime;

                        // ImageView 必須設定為 VISIBLE，否則無法觸發 listener
                        imageView.setVisibility(View.VISIBLE);

                        Glide.with(context)
                                .load(fingerprint.pic)
                                .listener(new RequestListener<String, GlideDrawable>() {
                                    @Override
                                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                        updateFinger(finalScanTime, SHOW_NONE);
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                        updateFinger(finalScanTime, SHOW_IMAGEVIEW);
                                        return false;
                                    }
                                })
                                .into(imageView);
                    }
                }
            }
        }
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

    private Handler enrollHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            try
            {
                int action = msg.what;
                int scanTime = msg.arg1;
                int arg2 = -1; // useless

                switch (action)
                {
                    case FingerprintSensor.EVENT_EXCEPTION:
                    {
                        logMe((Exception)msg.obj);
                        buttonSacn.setEnabled(true);
                        buttonSave.setVisibility(View.GONE);
                        isScaning = false;
                    }break;

                    case FingerprintSensor.EVENT_STOP_IMMEDIATELY:
                    {
                        updateFinger(0, SHOW_NONE);
                        updateFinger(1, SHOW_NONE);
                        updateFinger(2, SHOW_NONE);
                        buttonSacn.setEnabled(true);
                        buttonSave.setVisibility(View.GONE);
                        isScaning = false;
                    }break;

                    case FingerprintSensor.EVENT_CAPTURE_TIMEOUT:
                    {
                        DialogHelper.alert(context, (String)msg.obj);

                        updateFinger(0, SHOW_NONE);
                        updateFinger(1, SHOW_NONE);
                        updateFinger(2, SHOW_NONE);
                        buttonSacn.setEnabled(true);
                        buttonSave.setVisibility(View.GONE);
                        isScaning = false;
                    }break;

                    case FingerprintSensor.EVENT_ENROLL_ACTION_FINGER_PRESS:
                    {
                        logMe((String)msg.obj);
                        updateFinger(scanTime, SHOW_PROGRESSBAR);
                    }break;

                    case FingerprintSensor.EVENT_ENROLL_ACTION_SHOW_IMAGE:
                    {
                        ImageView imageView = imageViews.get(scanTime);
                        setImageView(imageView, (Bitmap) msg.obj);
                        updateFinger(scanTime, SHOW_IMAGEVIEW);
                    }break;

                    case FingerprintSensor.EVENT_ENROLL_ACTION_SAVE_IMAGE:
                    {
                        bmpFilename[scanTime] = (String)msg.obj;
                        ImageView imageView = imageViews.get(scanTime);

                        String filepath = bmpFilename[scanTime];
                        setImageView(imageView, filepath);
                        updateFinger(scanTime, SHOW_IMAGEVIEW);
                    }break;

                    case FingerprintSensor.EVENT_ENROLL_ACTION_FINGER_REMOVE:
                    {
                        logMe((String)msg.obj);
                    }break;

                    case FingerprintSensor.EVENT_ENROLL_ACTION_SAVE_MINUTIAE:
                    {
                        minutiae = (String)msg.obj;
                    }break;

                    case FingerprintSensor.EVENT_ENROLL_ACTION_SAVE_DAT:
                    {
                        datFilename = (String)msg.obj;
                    }break;

                    case FingerprintSensor.EVENT_ENROLL_RESULT_SUCCESS:
                    {
                        logMe((String)msg.obj);
                        buttonSacn.setEnabled(true);
                        buttonSave.setVisibility(View.VISIBLE);
                        isScaning = false;
                        hasEnrolled = true;
                    }break;

                    case FingerprintSensor.EVENT_ENROLL_RESULT_FAILURE:
                    {
                        logMe(new Exception((String)msg.obj));
                        buttonSacn.setEnabled(true);
                        buttonSave.setVisibility(View.GONE);
                        isScaning = false;
                    }break;
                }
                super.handleMessage(msg);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();

                Global.getCache().createRunningLog(RunningLog.CATEGORY_DATA_MAINTAIN, ex);

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

                textViewMessage.setText(message);
//                int iColor = context.getResources().getColor(isError ? R.color.colorRed : R.color.colorGreen);
                int iColor = context.getResources().getColor(isError ? R.color.colorRed : R.color.colorWhite);
                textViewMessage.setTextColor(iColor);
            }
        });
    }

    private void superOnBackPressed()
    {
        super.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if(isScaning)
        {
            DialogHelper.alert(context, getString(R.string.sensor_enrolling));
            return;
        }

        if(hasEnrolled)
        {
            // (1) 跳出確認視窗
            new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(getString(R.string.warn_title_ignore_create_fingerprint))
                    .setContentText(getString(R.string.warn_content_ignore_create_fingerprint))
                    .setConfirmText(context.getString(R.string.yes))
                    .setCancelText(context.getString(R.string.no))
                    .showCancelButton(true)
                    .showContentText(true)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            superOnBackPressed();
                        }
                    })
                    .show();

            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        try
        {
            int id = v.getId();

            switch (id) {
                case R.id.buttonBack: {
                    onBackPressed();
                }break;

                case R.id.buttonSacn: {
                    scan();
                }break;

                case R.id.buttonSave:
                {
                    save();
                }break;
            }
        } catch (IOException e) {
            e.printStackTrace();
            DialogHelper.alert(context, e.getMessage());
        }
    }

    private void scan()
    {
        FingerprintSensor sensor = getFingerprintSensor();

        if(sensor == null)
        {
            DialogHelper.alert(context, getString(R.string.no_fingerprint_sensor));
            return;
        }

        buttonSacn.setEnabled(false);
        buttonSave.setVisibility(View.GONE);

        isScaning = true;
        hasEnrolled = false;

        int timeoutSeconds = Global.getConfig().getEnrollTimeout();
        String client_action = getString(R.string.client_action_identify);
        sensor.enroll(enrollHandler, fingerBtnId, maxScanTimes, timeoutSeconds, client_action);
    }

    private void save() throws IOException {

        for(int scanTime = 0; scanTime<maxScanTimes; scanTime++)
            saveBmpFile(scanTime);

        saveDatFile();

        Fingerprint fingerprint = human.getFingerprint(fingerBtnId);
        if(fingerprint == null)
        {
            fingerprint = new Fingerprint();

            fingerprint.humanId = human.id;
            fingerprint.minutiae = minutiae;
            fingerprint.which = Converter.fingerBtnIdToEnglish(fingerBtnId);
            fingerprint.pic = FileManager.getBmpPath(human, fingerBtnId, 0);

            human.getFingerprints().add(fingerprint);
        }
        else
        {
            fingerprint.humanId = human.id;
            fingerprint.minutiae = minutiae;
            fingerprint.which = Converter.fingerBtnIdToEnglish(fingerBtnId);
            fingerprint.pic = FileManager.getBmpPath(human, fingerBtnId, 0);
        }

        superOnBackPressed();
    }

    private void saveBmpFile(int scanTime) throws IOException {
        String srcFilepath = bmpFilename[scanTime];
        String dstFilepath = FileManager.getBmpPath(human, fingerBtnId, scanTime);
        FileManager.copy(srcFilepath, dstFilepath);
    }

    private void saveDatFile() throws IOException {
        String srcFilepath = datFilename;
        String dstFilepath = FileManager.getDatPath(human, fingerBtnId);
        FileManager.copy(srcFilepath, dstFilepath);
    }

}