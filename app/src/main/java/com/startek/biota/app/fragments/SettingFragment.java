package com.startek.biota.app.fragments;

import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.models.RunningLog;
import com.startek.biota.app.models.Setting;
import com.startek.biota.app.utils.DialogHelper;
import com.startek.biota.app.utils.StrUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * 『系統設定』
 * <p/>
 * 1. 按鈕『返回』，轉跳至『設定畫面』
 * 2. 搜尋欄
 * 3. 按鈕『儲存』，儲存所有變更
 * 4. 按鈕『主機同步』，ToggleButton，顯示/隱藏主機同步參數
 * 5. 按鈕『門禁控制』，ToggleButton，顯示/隱藏門禁控制參數
 * 6. 按鈕『打卡設定』，ToggleButton，顯示/隱藏打卡設定參數
 * 7. 參數操作
 * - 編輯參數
 * - 前一次參數
 * - 還原預設值
 */
public class SettingFragment extends BaseFragment implements View.OnClickListener {

    private List<ListItemData> data;
    private boolean bSyncServer;
    private boolean bAccessControl;
    private boolean bUserInOut;

    private ImageView buttonBack;
    private EditText editTextSearch;
    private Button buttonSave;

    private Button buttonSyncServer;
    private Button buttonAccessControl;
    private Button buttonUserInOut;

    private ListView listView;
    private ListViewAdapter listViewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_setting, container, false);

        buttonBack = (ImageView)v.findViewById(R.id.buttonBack);
        editTextSearch = (EditText) v.findViewById(R.id.editTextSearch);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateListView(s.toString());
            }
        });
        buttonSave = (Button)v.findViewById(R.id.buttonSave);

        buttonSyncServer = (Button) v.findViewById(R.id.buttonSyncServer);
        buttonAccessControl = (Button) v.findViewById(R.id.buttonAccessControl);
        buttonUserInOut = (Button) v.findViewById(R.id.buttonUserInOut);

        listView = (ListView) v.findViewById(R.id.listView);
        listViewAdapter = new ListViewAdapter();
        listView.setAdapter(listViewAdapter);

        bSyncServer = true;
        bAccessControl = true;
        bUserInOut = true;

        buttonBack.setOnClickListener( this );
        buttonSave.setOnClickListener( this );
        buttonSyncServer.setOnClickListener( this );
        buttonAccessControl.setOnClickListener( this );
        buttonUserInOut.setOnClickListener( this );

        updateButtons();

        return v;
    }

    private void saveSetting() {
        boolean needReload = false;

        for (ListItemData delta : data) {
            Setting setting = delta.setting;

            Setting lastestSetting = Global.getCache().getLastestSetting(setting.signature);

            if (lastestSetting.value.trim().equals(setting.value.trim())) continue;
            ;

            needReload = true;

            try {
                Global.getCache().saveSetting(lastestSetting, setting.value);

                Global.getCache().createRunningLog(
                        RunningLog.CATEGORY_DATA_MAINTAIN,
                        getString(R.string.runninglog_event_savesetting),
                        Global.getLoginedUserName(),
                        String.format(getString(R.string.runninglog_event_savesetting_success), setting.description, lastestSetting.value, setting.value),
                        getString(R.string.result_success),
                        true);

                // if email time changed
                if (StrUtils.equals(Global.getConfig().getDefaultConfig().EmailTimes.signature, setting.signature)) {
                    Global.registerEmailAlarm();
                }
                // 使用ScheduleService 取代 AlarmReceiver

                // if base url changed
                if (StrUtils.equals(Global.getConfig().getDefaultConfig().BaseUrl.signature, setting.signature)) {
                    Global.initIceNet(Global.getContext(), Global.getConfig().getBaseUrl());
                }

            } catch (Exception ex) {
                ex.printStackTrace();

                Global.getCache().createRunningLog(
                        RunningLog.CATEGORY_DATA_MAINTAIN,
                        getString(R.string.runninglog_event_deleteHuman_local),
                        Global.getLoginedUserName(),
                        String.format(getString(R.string.runninglog_event_savesetting_failure), setting.description, lastestSetting.value, setting.value, ex.getMessage()),
                        getString(R.string.result_failure),
                        false);

                needReload = false;
            }

        }

        if (needReload) {
            data = null;
            updateListView(editTextSearch.getText().toString());
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.buttonBack: {
                onBackPressed();
            }
            break;

            case R.id.buttonSave: {
// (1) 跳出確認視窗
                new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText(getString(R.string.warn_title_save_setting))
                        .setContentText(getString(R.string.warn_content_save_setting))
                        .setConfirmText(context.getString(R.string.yes))
                        .setCancelText(context.getString(R.string.no))
                        .showCancelButton(true)
                        .showContentText(true)
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismissWithAnimation();
                                saveSetting();
                            }
                        })
                        .show();
            }
            break;

            case R.id.buttonSyncServer: {
                bSyncServer = !bSyncServer;
                updateButtons();
                updateListView(editTextSearch.getText().toString());
            }
            break;

            case R.id.buttonAccessControl: {
                bAccessControl = !bAccessControl;
                updateButtons();
                updateListView(editTextSearch.getText().toString());
            }
            break;

            case R.id.buttonUserInOut: {
                bUserInOut = !bUserInOut;
                updateButtons();
                updateListView(editTextSearch.getText().toString());
            }
            break;
        }
    }

    private void updateButtons() {
        try {
            Drawable toggleOn = ResourcesCompat.getDrawable(getResources(), R.drawable.button_toggle_on, null);
            Drawable toggleOff = ResourcesCompat.getDrawable(getResources(), R.drawable.button_toggle_off, null);

            buttonSyncServer.setBackground(bSyncServer ? toggleOn : toggleOff);
            buttonAccessControl.setBackground(bAccessControl ? toggleOn : toggleOff);
            buttonUserInOut.setBackground(bUserInOut ? toggleOn : toggleOff);
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(getLogTag(), ex.getMessage());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateListView(editTextSearch.getText().toString());
    }

    private int getCategories() {
        int categories = 0;
        if (bSyncServer) categories = categories | Setting.CATEGORY_SYNC_SERVER;
        if (bAccessControl) categories = categories | Setting.CATEGORY_ACCESS_CONTROL;
        if (bUserInOut) categories = categories | Setting.CATEGORY_USER_IN_OUT;

        return categories;
    }

    private void updateListView(String filteredStr) {

        int categories = getCategories();

        if (data == null) {
            List<Setting> settings = Global.getCache().querySettings();

            data = new ArrayList<ListItemData>();
            for (Setting setting : settings) {
                setting.previousSetting = Global.getCache().getPreviousSetting(setting);
                setting.defaultSetting = Global.getCache().getFirstSetting(setting.signature);

                ListItemData delta = new ListItemData(setting);
                data.add(delta);
            }
        }

        List<ListItemData> filteredData = new ArrayList<ListItemData>();

        for (ListItemData delta : data) {
            int flag = delta.setting.category;

            if ((categories & flag) != flag) continue;

            if (!TextUtils.isEmpty(filteredStr) &&
                    ((-1 == delta.getDescription().toUpperCase().indexOf(filteredStr.toUpperCase())) &&
                            (-1 == delta.getValue().toUpperCase().indexOf(filteredStr.toUpperCase()))
                    )) continue;

            filteredData.add(delta);
        }

        listViewAdapter.setData(filteredData);
    }

    class ListItemData {
        private Setting setting;

        public ListItemData(Setting setting) {
            this.setting = setting;
        }

        private String parameter;
        private String previousParameter;

        public String getDescription() {
            return setting.description;
        }

        public String getValue() {
            return setting.value;
        }

        public String getPreviousValue() {
            return setting.previousSetting == null ? "" : setting.previousSetting.value;
        }
    }

    class ListViewAdapter extends BaseAdapter {
        private List<ListItemData> rawData;

        public void setData(List<ListItemData> rawData) {
            this.rawData = rawData;
            notifyDataSetChanged();
        }

        public ListViewAdapter() {
            this.rawData = new ArrayList<ListItemData>();
        }

        @Override
        public int getCount() {
            return rawData.size();
        }

        @Override
        public Object getItem(int position) {
            return rawData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item_setting, parent, false);

                holder = new ViewHolder();

                holder.row = (LinearLayout) convertView.findViewById(R.id.row);

                holder.textViewSetting = (TextView) convertView.findViewById(R.id.textViewSetting);
                holder.textViewParameter = (TextView) convertView.findViewById(R.id.textViewParameter);
                holder.textViewPreviousParameter = (TextView) convertView.findViewById(R.id.textViewPreviousParameter);
                holder.actionPreviousParameter = (Button) convertView.findViewById(R.id.actionPreviousParameter);
                holder.actionDefaultParameter = (Button) convertView.findViewById(R.id.actionDefaultParameter);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final ListItemData delta = rawData.get(position);

            holder.textViewSetting.setText(delta.getDescription());
            holder.textViewParameter.setText(delta.getValue());
            holder.textViewPreviousParameter.setText(delta.getPreviousValue());

            holder.row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final Setting setting = delta.setting;

                    int editorType = setting.editorType;

                    switch (editorType) {
                        case Setting.EDITOR_EDITTEXT: {
                            String title = getString(R.string.editor_title_prefix) + setting.description;
                            String defaultValue = setting.value;
                            DialogHelper.OkClickListener okClickListener = new DialogHelper.OkClickListener() {
                                @Override
                                public void onClick(String result) {
                                    setting.value = result;
                                    notifyDataSetChanged();
                                }
                            };
                            boolean numberOnly = false;
                            int maxLength = -1;
                            DialogHelper.showTextEditor(context, title, defaultValue, okClickListener, numberOnly, maxLength);
                        }
                        break;

                        case Setting.EDITOR_SINGLECHOOSER: {
                            String title = getString(R.string.editor_title_prefix) + setting.description;
                            String defaultValue = setting.value;
                            DialogHelper.OkClickListener okClickListener = new DialogHelper.OkClickListener() {
                                @Override
                                public void onClick(String result) {
                                    setting.value = result;
                                    notifyDataSetChanged();
                                }
                            };

                            List<String> options = !TextUtils.isEmpty(setting.editorValues)
                                    ? new ArrayList<String>(Arrays.asList(setting.editorValues.split(",", -1)))
                                    : new ArrayList<String>();

                            DialogHelper.showSingleChooser(
                                    context,
                                    title,
                                    defaultValue,
                                    okClickListener,
                                    options);
                        }
                        break;

                        case Setting.EDITOR_TIMEPICKER: {
                            String title = getString(R.string.editor_title_prefix) + setting.description;
                            String defaultValue = setting.value;
                            DialogHelper.OkClickListener okClickListener = new DialogHelper.OkClickListener() {
                                @Override
                                public void onClick(String result) {
                                    setting.value = result;
                                    notifyDataSetChanged();
                                }
                            };

                            DialogHelper.showTimePicker(
                                    context,
                                    title,
                                    defaultValue,
                                    okClickListener);
                        }
                        break;

                        case Setting.EDITOR_DATEPICKER: {
                            String title = getString(R.string.editor_title_prefix) + setting.description;
                            String defaultValue = setting.value;
                            DialogHelper.OkClickListener okClickListener = new DialogHelper.OkClickListener() {
                                @Override
                                public void onClick(String result) {
                                    setting.value = result;
                                    notifyDataSetChanged();
                                }
                            };

                            DialogHelper.showDatePicker(
                                    context,
                                    title,
                                    defaultValue,
                                    okClickListener);
                        }
                        break;

                        case Setting.EDITOR_NUMBER: {
                            String title = getString(R.string.editor_title_prefix) + setting.description;
                            String defaultValue = setting.value;
                            DialogHelper.OkClickListener okClickListener = new DialogHelper.OkClickListener() {
                                @Override
                                public void onClick(String result) {
                                    setting.value = result;
                                    notifyDataSetChanged();
                                }
                            };
                            boolean numberOnly = true;
                            int maxLength = -1;
                            DialogHelper.showTextEditor(context, title, defaultValue, okClickListener, numberOnly, maxLength);
                        }
                        break;

                        case Setting.EDITOR_MULTICHOOSER: {
                            String title = getString(R.string.editor_title_prefix) + setting.description;
                            String defaultValue = setting.value;
                            DialogHelper.OkClickListener okClickListener = new DialogHelper.OkClickListener() {
                                @Override
                                public void onClick(String result) {
                                    setting.value = result;
                                    notifyDataSetChanged();
                                }
                            };

                            List<String> options = !TextUtils.isEmpty(setting.editorValues)
                                    ? new ArrayList<String>(Arrays.asList(setting.editorValues.split(",", -1)))
                                    : new ArrayList<String>();

                            DialogHelper.showMultiChooser(
                                    context,
                                    title,
                                    defaultValue,
                                    okClickListener,
                                    options);
                        }
                        break;
                    }
                }
            });

            holder.actionPreviousParameter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Setting setting = delta.setting;
                    if (setting.previousSetting != null) {
                        setting.value = setting.previousSetting.value;
                        setting.version = setting.version - 1;
                        setting.previousSetting = Global.getCache().getPreviousSetting(setting.previousSetting);
                        notifyDataSetChanged();
                    }
                }
            });

            holder.actionDefaultParameter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Setting setting = delta.setting;
                    if (setting.defaultSetting != null) {
                        setting.value = setting.defaultSetting.value;
                        notifyDataSetChanged();
                    }
                }
            });

            boolean enablePreviousParameter = delta.setting.previousSetting != null;
            boolean enableDefaultParameter = delta.setting.defaultSetting != null && !delta.setting.defaultSetting.value.equals(delta.setting.value);

            holder.actionPreviousParameter.setEnabled(enablePreviousParameter);
            holder.actionDefaultParameter.setEnabled(enableDefaultParameter);

            // 改成沒有按鈕的樣式，看不出 enable | disable 的分別，因此如果 disable 就隱藏起來
            holder.actionPreviousParameter.setVisibility(enablePreviousParameter ? View.VISIBLE : View.GONE);
            holder.actionDefaultParameter.setVisibility(enableDefaultParameter ? View.VISIBLE : View.GONE);

            return convertView;
        }

        class ViewHolder {
            private LinearLayout row;
            private TextView textViewSetting;
            private TextView textViewParameter;
            private TextView textViewPreviousParameter;
            private Button actionPreviousParameter;
            private Button actionDefaultParameter;
        }


    }
}
