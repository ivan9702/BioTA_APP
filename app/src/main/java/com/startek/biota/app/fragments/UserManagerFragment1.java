package com.startek.biota.app.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import android.widget.ListView;
import android.widget.TextView;

import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.jobs.EmailJob;
import com.startek.biota.app.managers.HumanManager;
import com.startek.biota.app.models.Human;
import com.startek.biota.app.utils.DialogHelper;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.DoneCallback;
import org.jdeferred.Promise;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * UserManagerActivity1: 『使用者管理』
 * UserManagerActivity2: 『使用者管理-新增畫面』
 * UserManagerActivity3: 『使用者管理-資料驗證』
 * UserManagerActivity4: 『使用者管理-使用者編輯-指紋建檔』
 *
 * 『使用者管理』
 *
 * 1. 按鈕『返回』，轉跳至『設定畫面』
 * 2. 搜尋欄
 * 3. 按鈕『新增』，轉跳至『使用者管理-新增畫面』
 * 4. 對於單一使用者，允許操作(資料驗證，刪除，建檔)
 *     - 資料驗證: 進入『使用者管理-資料驗證』
 *     - 刪除: 刪除本地端與Server的使用者
 *     - 建檔: 點擊後，同步對應使用者資料(AddOrUpdate)
 *     PS:
 *     當「NFC 或 指紋資料」其中一個存在時，顯示：資料驗證，刪除；
 *     否則顯示：建檔
 */
public class UserManagerFragment1 extends BaseFragment implements View.OnClickListener {

    private List<ListItemData> data;

    private ImageView buttonBack;
    private EditText editTextSearch;
    private Button buttonAdd;
    private ListView listView;
    private ListViewAdapter listViewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_manager1, container, false);

        buttonBack = (ImageView)v.findViewById(R.id.buttonBack);
        editTextSearch = (EditText)v.findViewById(R.id.editTextSearch);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                updateListView(s.toString());
            }
        });
        buttonAdd = (Button)v.findViewById(R.id.buttonAdd);

        listView = (ListView)v.findViewById(R.id.listView);
        listViewAdapter = new ListViewAdapter();
        listView.setAdapter(listViewAdapter);

        buttonBack.setOnClickListener(this);
        buttonAdd.setOnClickListener( this);

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
    private void loadData(){
        data = null;

        DialogHelper.openNetworkProgress(context);

        humanManager.loadHuman().promise().done(new DoneCallback<List<Human>>() {
            @Override
            public void onDone(List<Human> result) {
                String filteredStr = null;
                updateListView(filteredStr);
                humanManager.checkManagerCanLogin(Global.getHumanList());
            }
        }).always(new AlwaysCallback() {
            @Override
            public void onAlways(Promise.State state, Object resolved, Object rejected) {
                DialogHelper.closeNetworkProgress();
                Global.setFullscreen(); // 20160725  所有連線完成 皆呼叫native bar hide
            }
        });
    }

    private void updateListView(String filteredStr)
    {
        if (data == null)
        {
            data = new ArrayList<ListItemData>();
            for (Human human : Global.getHumanList()) {

                human.setLastAccessDate(Global.getCache().getLastAccessDate(human.bind_id));
                human.setLastAccessTime(Global.getCache().getLastAccessTime(human.bind_id));

                ListItemData delta = new ListItemData(human);
                data.add(delta);
            }
        }

        List<ListItemData> filteredData = data;

        if (!TextUtils.isEmpty(filteredStr))
        {
            filteredData = new ArrayList<ListItemData>();

            for (ListItemData delta : data)
            {
                if (-1 != delta.getName().toUpperCase().indexOf(filteredStr.toUpperCase()) ||
                        -1 != delta.getEmployeeId().toUpperCase().indexOf(filteredStr.toUpperCase()))
                {
                    filteredData.add(delta);
                }
            }
        }

        listViewAdapter.setData(filteredData);
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

            case R.id.buttonAdd:
            {
                createUser();
            }break;
        }
    }

    /**
     * 進入建立使用者功能
     */
    private void createUser()
    {
        humanManager.goToCreateUser();
    }

    /**
     * 進入編輯使用者功能
     */
    private void editUser(Human human)
    {
        humanManager.goToEditUser(human);

    }
    class ListItemData
    {
        private Human human;

        public ListItemData(Human human)
        {
            this.human = human;
        }

        public Human getHuman() {
            return human;
        }

        public String getFingerprintCount() {
            return Integer.toString(human.getFingerprints().size());
        }

        public String getNfcCount() {
            return Integer.toString(human.getNfcs().size());
        }

        public String getAccessTime() { return human.getLastAccessTime();}

        public String getAccessDate() { return human.getLastAccessDate();}

        public String getTitle() {
            return human.job;
        }

        public String getEmployeeId() {
            return human.bind_id;
        }

        public String getName() {
            return human.name;
        }

        public boolean needCreateFile()
        {
            return human.getFingerprints().size() == 0 || human.getNfcs().size() == 0;
        }
    }

    class ListViewAdapter extends BaseAdapter
    {
        private List<ListItemData> data;

        public void setData(List<ListItemData> data)
        {
            this.data = data;

            notifyDataSetChanged();
        }

        public final void deleteHuman(final ListItemData delta)
        {
            //if(Global.getConfig().useBiotaServer())
            if(delta.human.is_local)
            {
                // 20160705 Mander 說
                // 移除使用者流程 的部份不修改 但是需確認上傳完成才可以 移除 例如指紋上傳或nfc上傳都是失敗的 那就不能移除
                // 當然 如果nfc沒有資料 是可以刪除的
                // 但指紋就不能為無資料的狀況

                DialogHelper.alert(context, "此筆資料尚未上傳，無法刪除");
                return;
            }

            final Human human = delta.getHuman();

            DialogHelper.openNetworkProgress(context);

            humanManager.deleteHuman(human).promise().done(new DoneCallback<String>() {
                @Override
                public void onDone(String result) {
                    Global.getHumanList().remove(human);
                    data.remove(delta);
                    notifyDataSetChanged();
                    humanManager.checkManagerCanLogin(Global.getHumanList());
                }
            }).always(new AlwaysCallback() {
                @Override
                public void onAlways(Promise.State state, Object resolved, Object rejected) {
                    DialogHelper.closeNetworkProgress();
                    Global.setFullscreen(); // 20160725 Mander, 所有連線完成 皆呼叫native bar hide
                }
            });
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
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item_usermanager1, parent, false);

                holder = new ViewHolder();
                holder.textViewName = (TextView)convertView.findViewById(R.id.textViewName);
                holder.textViewEmployeeId = (TextView)convertView.findViewById(R.id.textViewEmployeeId);
                holder.textViewTitle = (TextView)convertView.findViewById(R.id.textViewTitle);
                holder.textViewAccessDate = (TextView)convertView.findViewById(R.id.textViewAccessDate);
                holder.textViewAccessTime = (TextView)convertView.findViewById(R.id.textViewAccessTime);
                holder.textViewNfcCount = (TextView) convertView.findViewById(R.id.textViewNfcCount);
                holder.textViewFingerprintCount = (TextView)convertView.findViewById(R.id.textViewFingerprintCount);

                holder.actionCreate = (Button)convertView.findViewById(R.id.actionCreate);
                holder.actionCreate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ListItemData delta = (ListItemData)v.getTag();
                        editUser(delta.getHuman());
                    }
                });

                holder.actionEdit = (Button)convertView.findViewById(R.id.actionEdit);
                holder.actionEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ListItemData delta = (ListItemData)v.getTag();
                        editUser(delta.getHuman());
                    }
                });

                holder.actionDelete = (Button)convertView.findViewById(R.id.actionDelete);
                holder.actionDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final ListItemData delta = (ListItemData)v.getTag();

                        final Human human = delta.getHuman();

                        // (1) 跳出確認視窗
                        new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText(getString(R.string.warn_title_delete_user))
                                .setContentText(String.format(getString(R.string.warn_content_delete_user), human.name))
                                .setConfirmText(context.getString(R.string.yes))
                                .setCancelText(context.getString(R.string.no))
                                .showCancelButton(true)
                                .showContentText(true)
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.dismissWithAnimation();
                                        deleteHuman(delta);
                                    }
                                })
                                .show();
                    }
                });

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ListItemData delta = data.get(position);

            int colorManagerAndHasDatFile = context.getResources().getColor(R.color.colorGreen);
            int colorHasDatFile = context.getResources().getColor(R.color.white);
            int colorNoDatFile = context.getResources().getColor(R.color.colorRed);

            boolean isManagerAndHasDatFile = delta.human.isManager() && delta.human.hasDatFile();
            boolean hasDatFile = delta.human.hasDatFile();

            holder.textViewName.setTextColor(isManagerAndHasDatFile
                    ? colorManagerAndHasDatFile
                    : hasDatFile
                    ? colorHasDatFile
                    : colorNoDatFile);
            holder.textViewName.setText(delta.getName());
            holder.textViewEmployeeId.setText(delta.getEmployeeId());
            holder.textViewTitle.setText(delta.getTitle());
            holder.textViewAccessDate.setText(delta.getAccessDate());
            holder.textViewAccessTime.setText(delta.getAccessTime());
            holder.textViewNfcCount.setText(delta.getNfcCount());
            holder.textViewFingerprintCount.setText(delta.getFingerprintCount());

            holder.actionEdit.setTag(delta);
            holder.actionDelete.setTag(delta);
            holder.actionCreate.setTag(delta);

            boolean needCreateFile = delta.needCreateFile();
            holder.actionEdit.setVisibility(needCreateFile ? View.GONE : View.VISIBLE);
            holder.actionDelete.setVisibility(needCreateFile ? View.GONE : View.VISIBLE);
            holder.actionCreate.setVisibility(needCreateFile ? View.VISIBLE : View.GONE);

            return convertView;
        }

        class ViewHolder {
            private TextView textViewName;
            private TextView textViewEmployeeId;
            private TextView textViewTitle;
            private TextView textViewAccessDate;
            private TextView textViewAccessTime;
            private TextView textViewNfcCount;
            private TextView textViewFingerprintCount;
            private Button actionEdit;
            private Button actionDelete;
            private Button actionCreate;
        }
    }


}

