package com.startek.biota.app.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.startek.biota.app.R;
import com.startek.biota.app.enums.EditMode;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.managers.HumanManager;
import com.startek.biota.app.models.EasyCard;
import com.startek.biota.app.models.Fingerprint;
import com.startek.biota.app.models.Human;
import com.startek.biota.app.models.Nfc;
import com.startek.biota.app.utils.DialogHelper;
import com.startek.biota.app.utils.StrUtils;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * UserManagerActivity1: 『使用者管理』
 * UserManagerActivity2: 『使用者管理-新增畫面』
 * UserManagerActivity3: 『使用者管理-資料驗證』
 * UserManagerActivity4: 『使用者管理-使用者編輯-指紋建檔』
 *
 * 『使用者管理-新增畫面』
 *
 * 1. 按鈕『返回』，轉跳至『使用者管理』
 * 2. 使用者相關資料
 *     - 姓名
 *     - 工號（只允許數字）
 *     - 職稱
 *     - 生日（年月日）
 *     - 性別
 *     - 血型
 * 2. 指紋(拇指, 食指, ...)
 *     點擊後，轉跳至『使用者管理-使用者編輯-指紋建檔』
 *
 * 3. 按鈕『新增下一筆』
 *     a. 驗證輸入資料
 *     b. 儲存這筆資料(本地端)
 *     c. 清空所有輸入，已進行下一筆資料的新增
 * 4. 按鈕『儲存』
 *     a. 驗證輸入資料
 *     b. 儲存這筆資料(本地端)
 *     c. 轉跳至『使用者管理』
 * 5. 按鈕『刪除』，刪除 NFC 卡對應資料
 * 6. 門禁卡下方新增按鈕為「綁定新的 NFC 卡使用」，換句話說，一個人預設允許多張門禁卡，此設定參數請協助新增加到「設定畫面」中的「一人最多幾張 NFC 卡」，當參數處理。
 */
public class UserManagerFragment2 extends BaseFragment implements View.OnClickListener {

    private Human human;
    // ----------------------------------------

    private Button buttonNext;
    private Button buttonEdit;
    private Button buttonSave;
    private ImageView buttonBack;

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

    private Button lThumb;
    private Button rThumb;
    private Button lIndex;
    private Button rIndex;
    private Button lMiddle;
    private Button rMiddle;
    private Button lRing;
    private Button rRing;
    private Button lPinky;
    private Button rPinky;

    private TextView textViewAddNfc;
    private LinearLayout nfcContainer;
    private Button buttonAdd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_manager2, container, false);

        buttonNext = (Button)v.findViewById(R.id.buttonNext);
        buttonEdit = (Button)v.findViewById(R.id.buttonEdit);
        buttonSave = (Button)v.findViewById(R.id.buttonSave);
        buttonBack = (ImageView)v.findViewById(R.id.buttonBack);

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

        rowName.setOnClickListener(rowClickListener);
        rowEmployeeId.setOnClickListener(rowClickListener);
        rowTitle.setOnClickListener(rowClickListener);
        rowBirthday.setOnClickListener(rowClickListener);
        rowGender.setOnClickListener(rowClickListener);
        rowBloodType.setOnClickListener(rowClickListener);

        lThumb = (Button)v.findViewById(R.id.l_thumb);
        rThumb = (Button)v.findViewById(R.id.r_thumb);
        lIndex = (Button)v.findViewById(R.id.l_index);
        rIndex = (Button)v.findViewById(R.id.r_index);
        lMiddle = (Button)v.findViewById(R.id.l_middle);
        rMiddle = (Button)v.findViewById(R.id.r_middle);
        lRing = (Button)v.findViewById(R.id.l_ring);
        rRing = (Button)v.findViewById(R.id.r_ring);
        lPinky = (Button)v.findViewById(R.id.l_pinky);
        rPinky = (Button)v.findViewById(R.id.r_pinky);

        textViewAddNfc = (TextView)v.findViewById(R.id.textViewAddNfc);
        nfcContainer = (LinearLayout)v.findViewById(R.id.nfcContainer);
        buttonAdd = (Button)v.findViewById(R.id.buttonAdd);

        buttonBack.setOnClickListener(this);
        buttonNext.setOnClickListener(this);
        buttonEdit.setOnClickListener(this);
        buttonSave.setOnClickListener(this);

        buttonAdd.setOnClickListener(this);

        lThumb.setOnClickListener(this);
        rThumb.setOnClickListener(this);
        lIndex.setOnClickListener(this);
        rIndex.setOnClickListener(this);
        lMiddle.setOnClickListener(this);
        rMiddle.setOnClickListener(this);
        lRing.setOnClickListener(this);
        rRing.setOnClickListener(this);
        lPinky.setOnClickListener(this);
        rPinky.setOnClickListener(this);

        human = Global.getEditedHuman();

        return v;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if(!isEasyCardReceived())
        {
            reload();
        }
    }

    @Override
    public void onEasyCardReceived(final EasyCard easyCard)
    {
        super.onEasyCardReceived(easyCard);

        if(isReadonly())
        {
            DialogHelper.alert(context, getString(R.string.readonly_cannot_insert_card));
            return;
        }

        if(!isEasyCardAvailable(easyCard))return;

        buttonAdd.setVisibility(View.VISIBLE);
        buttonAdd.setTag(easyCard.getTagId());
    }

    private void superOnBackPressed()
    {
        super.onBackPressed();
    }

    @Override
    public void onBackPressed()
    {
        String title = getString(Global.getEditMode() == EditMode.INSERT
                ? R.string.warn_title_ignore_create_user
                : R.string.warn_title_ignore_update_user);

        if (isDirty()) {
            // (1) 跳出確認視窗
            new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(title)
                    .setContentText(getString(R.string.warn_content_ignore_create_user))
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

    /**
     * 更新畫面上使用者相關資料
     */
    private void reload()
    {
        updateNavigationButtons();

        textViewName.setText(human.name);
        textViewEmployeeId.setText(human.bind_id);
        textViewTitle.setText(human.job);
        textViewBirthday.setText(human.birthday);
        textViewGender.setText(human.gender);
        textViewBloodType.setText(human.bloodtype);

        updateFingerprintButtons();

        updateNfcButtons();
    }

    private void updateNavigationButtons()
    {
        switch (Global.getEditMode())
        {
            case EditMode.INSERT:
            {
                buttonNext.setVisibility(View.VISIBLE);
                buttonEdit.setVisibility(View.GONE);
                buttonSave.setVisibility(View.VISIBLE);
            }break;

            case EditMode.UPDATE:
            {
                buttonNext.setVisibility(View.GONE);
                buttonEdit.setVisibility(View.GONE);
                buttonSave.setVisibility(View.VISIBLE);
            }break;

            case EditMode.READONLY:
            {
                buttonNext.setVisibility(View.GONE);
                buttonEdit.setVisibility(View.VISIBLE);
                buttonSave.setVisibility(View.GONE);
            }break;
        }
    }

    /**
     * 更新畫面上使用者對應的指紋資料
     */
    private void updateFingerprintButtons()
    {
        List<Button> buttons = new ArrayList<Button>();
        buttons.add(lThumb);
        buttons.add(rThumb);
        buttons.add(lIndex);
        buttons.add(rIndex);
        buttons.add(lMiddle);
        buttons.add(rMiddle);
        buttons.add(lRing);
        buttons.add(rRing);
        buttons.add(lPinky);
        buttons.add(rPinky);

        for(Button button:buttons)
        {
            Fingerprint fingerprint = human.getFingerprint(button.getId());
            boolean hasFingerprintData = fingerprint != null;
            int iColor = context.getResources().getColor(hasFingerprintData ? R.color.colorWhite : R.color.colorRed);
            button.setTextColor(iColor);
        }
    }

    /**
     * 更新畫面上使用者擁有的悠遊卡
     */
    public void updateNfcButtons()
    {
        List<Nfc> nfcs = human.getNfcs();

        nfcContainer.removeAllViews();

        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        textViewAddNfc.setVisibility(isReadonly() ? View.GONE : View.VISIBLE);

        for(Nfc nfc:nfcs)
        {
            View v = vi.inflate(R.layout.nfc, null);

            TextView textView = (TextView) v.findViewById(R.id.textViewTagId);
            Button button = (Button)v.findViewById(R.id.buttonDelete);

            textView.setText(nfc.tagId);
            button.setTag(nfc);
            button.setOnClickListener(deleteTagListener);

            button.setVisibility(isReadonly() ? View.INVISIBLE : View.VISIBLE);

            nfcContainer.addView(v);
        }
    }

    /**
     * 將使用者輸入寫入 Human 物件
     */
    private void uiDataToHuman()
    {
        human.name = textViewName.getText().toString();
        human.bind_id = textViewEmployeeId.getText().toString();
        human.job = textViewTitle.getText().toString();
        human.birthday = textViewBirthday.getText().toString();
        human.gender = textViewGender.getText().toString();
        human.bloodtype = textViewBloodType.getText().toString();
    }

    /**
     * 處理刪除悠遊卡動作
     */
    private View.OnClickListener deleteTagListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            final Nfc nfc = (Nfc)v.getTag();

            // (1) 跳出確認視窗
            new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(getString(R.string.warn_title_delete_nfc))
                    .setContentText(String.format(getString(R.string.warn_content_delete_nfc), nfc.tagId))
                    .setConfirmText(context.getString(R.string.yes))
                    .setCancelText(context.getString(R.string.no))
                    .showCancelButton(true)
                    .showContentText(true)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener()
                    {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            human.getNfcs().remove(nfc);
                            updateNfcButtons();
                        }
                    })
                    .show();
        }
    };

    /**
     * 處理使用者基本資料的編輯動作
     */
    private View.OnClickListener rowClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {

            if(isReadonly())
            {
                DialogHelper.alert(context, getString(R.string.readonly_cannot_edit_profile));
                return;
            }

            int id = v.getId();

            switch (id)
            {
                case R.id.rowName:
                {
                    DialogHelper.showTextEditor(
                            context,
                            getString(R.string.editor_title_prefix) + getString(R.string.labelName),
                            textViewName
                    );
                }break;

                case R.id.rowEmployeeId:
                {
                    DialogHelper.showTextEditor(
                            context,
                            getString(R.string.editor_title_prefix) + getString(R.string.labelEmployeeIdC),
                            textViewEmployeeId,
                            true,
                            Global.getConfig().getMaxEmployeeIdLength()
                    );
                }break;

                case R.id.rowTitle:
                {
                    DialogHelper.showTextEditor(
                            context,
                            getString(R.string.editor_title_prefix) + getString(R.string.labelTitle),
                            textViewTitle
                    );
                }break;

                case R.id.rowBirthday:
                {
                    DialogHelper.showDatePicker(
                            context,
                            getString(R.string.editor_title_prefix) + getString(R.string.labelBirthday),
                            textViewBirthday
                    );
                }break;

                case R.id.rowGender:
                {
                    List<String> options = new ArrayList<String>();
                    options.add(getString(R.string.male));
                    options.add(getString(R.string.female));

                    DialogHelper.showSingleChooser(
                            context,
                            getString(R.string.editor_title_prefix) + getString(R.string.labelGender),
                            textViewGender,
                            options
                    );
                }break;

                case R.id.rowBloodType:
                {
                    List<String> options = new ArrayList<String>();
                    options.add("A");
                    options.add("B");
                    options.add("AB");
                    options.add("O");

                    DialogHelper.showSingleChooser(
                            context,
                            getString(R.string.editor_title_prefix) + getString(R.string.labelBloodType),
                            textViewBloodType,
                            options
                    );
                }break;
            }
        }
    };

    /**
     * 檢查是否已經有輸入資料
     */
    private boolean isDirty()
    {
        uiDataToHuman();

        return !human.equals(Global.getOriginEditedHuman());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id)
        {
            case R.id.buttonBack:
            {
                onBackPressed();
            }break;

            case R.id.buttonNext:
            {
                DialogHelper.openNetworkProgress(context);

                save().promise().done(new DoneCallback<String>() {
                    @Override
                    public void onDone(String result) {
                        DialogHelper.info(context, result);
                        human = Human.CreateNewUser("");
                        Global.setEditedHuman(human);
                        reload();
                    }
                }).fail(new FailCallback<String>() {
                    @Override
                    public void onFail(String result) {
                        DialogHelper.alert(context, result);
                    }
                }).always(new AlwaysCallback() {
                    @Override
                    public void onAlways(Promise.State state, Object resolved, Object rejected) {
                        DialogHelper.closeNetworkProgress();
                        Global.setFullscreen(); // 20160725, 所有連線完成 皆呼叫native bar hide
                    }
                });
            }break;

            case R.id.buttonEdit:
            {
                Global.setEditMode(EditMode.UPDATE);
                reload();
            }break;

            case R.id.buttonSave:
            {
                DialogHelper.openNetworkProgress(context);

                save().promise().done(new DoneCallback<String>() {
                    @Override
                    public void onDone(String result) {
                        DialogHelper.info(context, result);
                        superOnBackPressed();
                    }
                }).fail(new FailCallback<String>() {
                    @Override
                    public void onFail(String result) {
                        DialogHelper.alert(context, result);
                    }
                }).always(new AlwaysCallback() {
                    @Override
                    public void onAlways(Promise.State state, Object resolved, Object rejected) {
                        DialogHelper.closeNetworkProgress();
                    }
                });
            }break;

            case R.id.buttonAdd:
            {
                Nfc n = new Nfc();
                n.id = 0; // 0 代表新增資料
                n.humanId = human.id;
                n.tagId = (String)v.getTag();
                human.getNfcs().add(n);

                updateNfcButtons();

                buttonAdd.setVisibility(View.GONE);
                buttonAdd.setTag(null);
            }break;

            // ----------------------------------------
            // 拇指按鈕
            // ----------------------------------------

            case R.id.l_thumb:
            case R.id.l_index:
            case R.id.l_middle:
            case R.id.l_ring:
            case R.id.l_pinky:
            case R.id.r_thumb:
            case R.id.r_index:
            case R.id.r_middle:
            case R.id.r_ring:
            case R.id.r_pinky:
            {
                if(isReadonly())
                {
                    DialogHelper.alert(context, getString(R.string.readonly_cannot_edit_fingerprint));
                    return;
                }


                String errors = getInputErrors(CHECK_INPUT);

                // 使用者資料輸入有誤
                if(!TextUtils.isEmpty(errors))
                {
                    DialogHelper.alert(context, errors);
                    return;
                }

//                uiDataToHuman(); // getInputErrors already sync data to human
                Global.setEditedFingerResId(id);

                showFragment(new UserManagerFragment4(), "UserManagerFragment4");
            }break;
        }
    }

    /**
     * 判斷這張悠遊卡是否可被新增
     * (1) 有沒有使用過(被別人或自己)
     * (2) 是否正在感應(把悠遊卡放在裝置上過久)
     */
    private boolean isEasyCardAvailable(EasyCard easyCard) {

        String buttonTagId = (String)buttonAdd.getTag();
        String easyCardTagId = easyCard.getTagId();

        if(TextUtils.isEmpty(buttonTagId)) buttonTagId = "";
        if(TextUtils.isEmpty(easyCardTagId)) easyCardTagId = "";

        // 正在感應
        if(StrUtils.equals(buttonTagId, easyCardTagId))
            return false;

        if(human.getNfcs().size() >= Global.getConfig().getMaxNfcCount())
        {
            // 可註冊悠遊卡已達最大張數(%d)
            DialogHelper.alert(context, String.format(getString(R.string.error_nfc_count_limit), Global.getConfig().getMaxNfcCount()));

            return false;
        }

        for(Nfc nfc:human.getNfcs())
        {
            if(StrUtils.equals(nfc.tagId, easyCardTagId))
            {
                // 您已經註冊此張悠遊卡(%s)
                DialogHelper.alert(context, String.format(getString(R.string.error_nfc_has_been_used_byself), easyCard.getTagId()));

                return false;
            }
        }

        List<Human> humanList = Global.getHumanList();

        if(humanList == null) return true; // 理論上不應該發生這種狀況

        for(Human aHuman:humanList)
        {
            for(Nfc nfc:aHuman.getNfcs())
            {
                if(StrUtils.equals(nfc.tagId, easyCardTagId))
                {
                    if(!StrUtils.equals(aHuman.bind_id, human.bind_id))
                    {
                        // 悠遊卡(%s)已經被使用者(%s)註冊
                        DialogHelper.alert(context, String.format(getString(R.string.error_nfc_has_been_used_other), easyCard.getTagId(), aHuman.name));
                    }

                    return false;
                }
            }
        }

        return true;
    }

    private int CHECK_INPUT = 1 << 0;
    private int CHECK_FINGERPRINT = 1 << 1;
    private int CHECK_NFC = 1 << 2;

    private int CHECK_ALL = CHECK_INPUT | CHECK_FINGERPRINT | CHECK_NFC;

    private boolean containOption(int options, int flag)
    {
        return (options & flag) == flag;
    }

    /**
     * 檢驗使用者輸入
     */
    private String getInputErrors(int options)
    {
        List<String> errors = new ArrayList<String>();

        uiDataToHuman();

        if(containOption(options, CHECK_INPUT))
        {
            // (1) 以下欄位都不能為空
            // 【姓名】
            // 【工號】
            // 【職稱】
            // 【生日】
            // 【性別】
            // 【血型】
            if(TextUtils.isEmpty(human.name))
                errors.add(String.format(getString(R.string.error_input_required), getString(R.string.labelName)));

            if(TextUtils.isEmpty(human.bind_id))
                errors.add(String.format(getString(R.string.error_input_required), getString(R.string.labelEmployeeIdC)));

            if(human.bind_id.length() != Global.getConfig().getMaxEmployeeIdLength() || !isNumeric(human.bind_id))
                errors.add(String.format(getString(R.string.error_employeeid_limit), getString(R.string.labelEmployeeIdC), Global.getConfig().getMaxEmployeeIdLength()));

            if(TextUtils.isEmpty(human.job))
                errors.add(String.format(getString(R.string.error_input_required), getString(R.string.labelTitle)));

            if(TextUtils.isEmpty(human.birthday))
                errors.add(String.format(getString(R.string.error_input_required), getString(R.string.labelBirthday)));

            if(TextUtils.isEmpty(human.gender))
                errors.add(String.format(getString(R.string.error_input_required), getString(R.string.labelGender)));

            if(TextUtils.isEmpty(human.bloodtype))
                errors.add(String.format(getString(R.string.error_input_required), getString(R.string.labelBloodType)));

            List<Human> humanList = Global.getHumanList();

            for(Human aHuman:humanList)
            {
                if(StrUtils.equals(aHuman.bind_id, human.bind_id) &&
                        !StrUtils.equals(aHuman.id, human.id))
                {
                    // 輸入工號%s已被使用者%s所使用
                    errors.add(String.format(getString(R.string.error_duplicate_bind_id), human.bind_id, aHuman.name));
                }
            }
        } // if(containOption(options, CHECK_INPUT))

        if(containOption(options, CHECK_FINGERPRINT))
        {
            // (2) 指紋資料數量可以為零
            //     (例外狀況：若使用者身分為【安裝者】，至少必須新增一筆指紋資料)

            if(human.isManager())
            {
                if(human.getFingerprints().size() == 0)
                {
                    errors.add(getString(R.string.error_no_fingerprint));
                }
            }
        } // if(containOption(options, CHECK_FINGERPRINT))

        if(errors.size() != 0)
        {
            String errmsg = "";
            for(int i=0; i<errors.size(); i++)
            {
                errmsg += String.format("%d. %s\n", i+1, errors.get(i));
            }

            return errmsg;
        }

        return null;
    }

    private boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    /**
     * 儲存
     */
    private Deferred save()
    {
        String errors = getInputErrors(CHECK_ALL);

        // 使用者資料輸入有誤
        if(!TextUtils.isEmpty(errors))
        {
            Deferred deferred = new DeferredObject();

            deferred.reject(errors);

            return deferred;
        }

        switch (Global.getEditMode())
        {
            case EditMode.INSERT:
            {
                return humanManager.createHuman(human);
            }

            case EditMode.UPDATE:
            {
                return humanManager.updateHuman(human);
            }

            default:
            {
                Deferred deferred = new DeferredObject();

                deferred.reject(String.format("未知的編輯模式(%d)", Global.getEditMode()));

                return deferred;
            }
        }
    }

    private boolean isReadonly()
    {
        return Global.getEditMode() == EditMode.READONLY;
    }
}
