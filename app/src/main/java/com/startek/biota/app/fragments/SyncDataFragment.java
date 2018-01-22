package com.startek.biota.app.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.models.DirtyData;
import com.startek.biota.app.models.Human;
import com.startek.biota.app.models.MatchLog;
import com.startek.biota.app.models.RunningLog;
import com.startek.biota.app.network.webservices.SequenceDeferredManager;
import com.startek.biota.app.utils.Converter;
import com.startek.biota.app.utils.DialogHelper;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.Deferred;
import org.jdeferred.DeferredManager;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DefaultDeferredManager;
import org.jdeferred.impl.DeferredObject;
import org.jdeferred.multiple.MultipleResults;
import org.jdeferred.multiple.OneReject;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * 『手動同步資料』
 *
 * 1. 需要同步以下資料
 *     a. 使用者資料
 *         - 姓名
 *         - 工號
 *         - 職稱
 *         - 生日
 *         - 性別
 *         - 血型
 *     b. 卡號資料
 *     c. 指紋資料
 *  d. 運行紀錄(取得各點狀態)
 *  e. 系統設定資料(遠端設定參數)
 * 2. 沒有特殊需求的畫面
 *     可用 tableview 方式呈現「還未與 server 同步的資料清單」，當按下「同步按鈕」時，重新把這些資料送到 Server 上去。
 * 3. 資料同步時，依循「寫入本地資料→呼叫API→刪除本地資料」原則，避免資料遺失。
 *
 *
 // - Global.getCache().createDirtyData(DirtyData.ACTION_INSERT, human); -> only handle localdata
 // - Global.getCache().createDirtyData(DirtyData.ACTION_UPDATE, human); -> only handle localdata
 // - Global.getCache().createDirtyData(DirtyData.ACTION_DELETE, human); -> only handle remotedata
 // 手動同步
 //  - human (ACTION_INSERT)
 //      檢查資料庫是否有對應 human,
 //      如果有就
 //          a. 上傳(Create) human
 //          b. 上傳(Create) human 相關 nfc
 //          c. 上傳(Create) human 相關 fingerprint
 //          如果上傳成功，就清除此筆 DirtyData
 //      如果沒有(此筆資料已經從更新動作上傳到SERVER)就
 //          a. 直接清除此筆 DirtyData
 */
public class SyncDataFragment extends BaseFragment implements View.OnClickListener {

    private List<ListItemData> data;

    private ImageView buttonBack;
    private Button buttonReload;
    private Button buttonSync;

    private ListView listView;
    private ListViewAdapter listViewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sync_data, container, false);

        buttonBack = (ImageView)v.findViewById(R.id.buttonBack);
        buttonReload = (Button)v.findViewById(R.id.buttonReload);
        buttonSync = (Button)v.findViewById(R.id.buttonSync);

        listView = (ListView)v.findViewById(R.id.listView);
        listViewAdapter = new ListViewAdapter();
        listView.setAdapter(listViewAdapter);

        buttonReload.setVisibility(View.GONE);
        buttonSync.setVisibility(View.GONE);

        buttonBack.setOnClickListener(this);
        buttonReload.setOnClickListener( this );
        buttonSync.setOnClickListener(this);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        reload();
    }

    private void reload()
    {
        List<DirtyData> dirtyDataList = Global.getCache().queryDirtyData();

        data = new ArrayList<ListItemData>();
        for (DirtyData rawData : dirtyDataList)
        {
            ListItemData delta = new ListItemData(rawData);
            data.add(delta);
        }

        listViewAdapter.setData(data);

        buttonReload.setVisibility(View.GONE);
        buttonSync.setVisibility(dirtyDataList.size() > 0 ? View.VISIBLE : View.GONE);

        if(dirtyDataList.size() == 0)
        {
            new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(getString(R.string.info_title_no_need_sync_data))
                    .setContentText(getString(R.string.info_content_no_need_sync_data))
                    .showContentText(true)
                    .setConfirmText(context.getString(R.string.ok))
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismissWithAnimation();
                            onBackPressed();
                        }
                    })
                    .show();
        }
    }

    @Override
    public void onClick(View view)
    {
        int id = view.getId();

        switch (id)
        {
            case R.id.buttonBack:
            {
                onBackPressed();
            }break;

            case R.id.buttonReload:
            {
                reload();
            }break;

            case R.id.buttonSync:
            {
                syncData();
            }break;
        }
    }

    private Class getClass(String className)
    {
        List<Class> classList = new ArrayList<>();

        classList.add(Human.class);
        classList.add(MatchLog.class);

        for(Class clazz:classList)
        {
            if(clazz.toString().equals(className))
                return clazz;
        }

        return null;
    }

    private Deferred getTask(ListItemData delta)
    {
        Deferred deferred = null;

        final DirtyData dirtyData = delta.dirtyData;

        Class classTarget = getClass(delta.dirtyData.className);
        if(classTarget == null)
        {
            deferred = new DeferredObject();
            deferred.reject(String.format("尚未定義 %s 在 getClass 中的對應類別", delta.dirtyData.className));
        }
        else
        {
            if(classTarget.equals(Human.class))
            {
                final Human human = new Gson().fromJson(dirtyData.json, Human.class);

                switch (dirtyData.action)
                {
                    case DirtyData.ACTION_CREATE:
                        deferred = humanManager.createHuman_remote(human);
                        break;

                    case DirtyData.ACTION_UPDATE:
                        deferred = humanManager.updateHuman_remote(human);
                        break;

                    case DirtyData.ACTION_DELETE:
                        boolean cleanup = true;
                        deferred = humanManager.deleteHuman_remote(human, cleanup);
                        break;
                }

                deferred.promise().done(new DoneCallback<String>() {
                    @Override
                    public void onDone(String result) {
                        try {
                            if (Global.getCache().deleteHuman(human) > 0)
                            {
                                Global.getCache().createRunningLog(
                                        RunningLog.CATEGORY_SYNC_SERVER,
                                        getString(R.string.runninglog_event_deleteHuman_local),
                                        Global.getLoginedUserName(),
                                        String.format(getString(R.string.deleteHuman_local_success), human.name),
                                        getString(R.string.result_success),
                                        true);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();

                            Global.getCache().createRunningLog(
                                    RunningLog.CATEGORY_SYNC_SERVER,
                                    getString(R.string.runninglog_event_deleteHuman_local),
                                    Global.getLoginedUserName(),
                                    String.format(getString(R.string.deleteHuman_local_failure), human.name, ex.getMessage()),
                                    getString(R.string.result_failure),
                                    false);

                            DialogHelper.alert(context, ex.getMessage());
                        }
                    }
                });
            }

            if(classTarget.equals(MatchLog.class))
            {
                final MatchLog matchLog = new Gson().fromJson(dirtyData.json, MatchLog.class);

                switch (dirtyData.action)
                {
                    case DirtyData.ACTION_CREATE:
                        deferred = matchLogManager.createMatchLog_remote(matchLog);
                        break;
                }

                deferred.promise().done(new DoneCallback<String>() {
                    @Override
                    public void onDone(String result) {


                    }
                });
            }
        }

        if(deferred == null) {
            deferred = new DeferredObject();
            deferred.reject(String.format("無對應處理方式(%s)", dirtyData.toString()));
        }

        final int category = RunningLog.CATEGORY_SYNC_SERVER;
        final String event = String.format(getString(R.string.runninglog_event_syncdata), actionToString(dirtyData.action), Converter.getSimpleName(dirtyData.className));
        final String operator = Global.getLoginedUserName();

        deferred.promise().done(new DoneCallback<String>() {
            @Override
            public void onDone(String result) {
                try {
                    dirtyData.result = result;
                    dirtyData.state = DirtyData.STATE_SUCCESS;

                    Global.getCache().createRunningLog(
                            category,
                            event,
                            operator,
                            result,
                            getString(R.string.result_success),
                            true);

                    Global.getCache().updateDirtyData(dirtyData);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    DialogHelper.alert(context, ex.getMessage());
                }
            }
        }).fail(new FailCallback<String>() {
            @Override
            public void onFail(String result) {
                dirtyData.result = result;
                dirtyData.state = DirtyData.STATE_FAILURE;

                Global.getCache().createRunningLog(
                        category,
                        event,
                        operator,
                        result,
                        getString(R.string.result_failure),
                        false);
            }
        });

        return deferred;
    }

    private void syncData()
    {
        List<Promise> promises = new ArrayList<Promise>();

        for(final ListItemData delta:data)
        {
            Deferred deferred = getTask(delta);

            promises.add(deferred.promise());
        }

        if(promises.size() != 0)
        {
            buttonSync.setVisibility(View.GONE);

            DialogHelper.openNetworkProgress(context);

            DeferredManager dm = new SequenceDeferredManager();

            dm.when(Converter.toArray(promises)).always(new AlwaysCallback<MultipleResults, OneReject>() {
                @Override
                public void onAlways(Promise.State state, MultipleResults resolved, OneReject rejected) {
                    DialogHelper.closeNetworkProgress();
                    listViewAdapter.notifyDataSetChanged();
                    buttonReload.setVisibility(View.VISIBLE);
                    Global.setFullscreen(); // 20160725, 所有連線完成 皆呼叫native bar hide
                }
            });
        }
    }

    private void syncData_CustomSequence()
    {
        List<Promise> promises = new ArrayList<Promise>();

        for(final ListItemData delta:data)
        {
            Deferred deferred = getTask(delta);

            promises.add(deferred.promise());
        }

        if(promises.size() != 0)
        {
            // http://stackoverflow.com/questions/13651243/how-do-i-chain-a-sequence-of-deferred-functions-in-jquery-1-8-x
            syncData(0, promises); // 一個執行完，才會執行下一個
        }
    }

    private void syncData(final int idx, final List<Promise> promises)
    {
        if(idx < 0 || idx > promises.size() -1) return; // out of range

        final Promise promise = promises.get(idx);

//        http://stackoverflow.com/questions/13651243/how-do-i-chain-a-sequence-of-deferred-functions-in-jquery-1-8-x

        if(idx == 0)
        {
            buttonSync.setVisibility(View.GONE);
            DialogHelper.openNetworkProgress(context);
        }

        promise.done(new DoneCallback<String>() {
            @Override
            public void onDone(String result) {
                if(idx == promises.size() -1) {
                    // 全部成功
                    DialogHelper.closeNetworkProgress();
                    listViewAdapter.notifyDataSetChanged();
                    buttonReload.setVisibility(View.VISIBLE);
                }
                else {
                    syncData(idx + 1, promises);
                }

            }
        }).fail(new FailCallback<String>() {
            @Override
            public void onFail(String result) {
                // 有其中一個失敗
                DialogHelper.closeNetworkProgress();
                listViewAdapter.notifyDataSetChanged();
                buttonReload.setVisibility(View.VISIBLE);
            }
        });
    }

    private String actionToString(int action)
    {
        switch (action)
        {
            case DirtyData.ACTION_CREATE:
                return getString(R.string.actionCreate1);
            case DirtyData.ACTION_UPDATE:
                return getString(R.string.actionUpdate);
            case DirtyData.ACTION_DELETE:
                return getString(R.string.actionDelete);
            default:
                return String.format("UNKNOWN(%d)", action);
        }
    }

    class ListItemData
    {
        private DirtyData dirtyData;

        public ListItemData(DirtyData dirtyData)
        {
            this.dirtyData = dirtyData;
        }

        public String getAction() {
            return actionToString(dirtyData.action);
        }

        public String getClassName()
        {
            return Converter.getSimpleName(dirtyData.className);
        }

        public String getJson() {
            return dirtyData.json;
        }

        public int getState() {return dirtyData.state;}
    }

    class ListViewAdapter extends BaseAdapter
    {
        private List<ListItemData> data;

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

            final ViewHolder holder;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item_sync_data, parent, false);

                holder = new ViewHolder();
                holder.textViewAction = (TextView)convertView.findViewById(R.id.textViewAction);
                holder.textViewClassName = (TextView)convertView.findViewById(R.id.textViewClassName);
                holder.textViewJson = (TextView)convertView.findViewById(R.id.textViewJson);
                holder.textViewState = (TextView)convertView.findViewById(R.id.textViewState);
                holder.progressBarState = (ProgressBar)convertView.findViewById(R.id.progressBarState);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ListItemData delta = data.get(position);

            holder.textViewAction.setText(delta.getAction());
            holder.textViewClassName.setText(delta.getClassName());
            holder.textViewJson.setText(Converter.toPrettyFormat(delta.getJson()));

            updateState(holder, delta);

            return convertView;
        }

        private void updateState(ViewHolder holder, ListItemData delta)
        {
            TextView textViewState = holder.textViewState;
            ProgressBar progressBarState = holder.progressBarState;

            int iColorDefault = context.getResources().getColor(android.R.color.black);
            int iColorFailure = context.getResources().getColor(R.color.colorRed);
            int iColorSuccess = context.getResources().getColor(R.color.colorGreen);

            switch (delta.getState())
            {
                case DirtyData.STATE_PENDING:
                {
                    textViewState.setText("-");
                    textViewState.setTextColor(iColorDefault);
                    textViewState.setVisibility(View.VISIBLE);
                    progressBarState.setVisibility(View.GONE);
                }break;

                case DirtyData.STATE_IN_PROCESSED:
                {
                    textViewState.setVisibility(View.GONE);
                    progressBarState.setVisibility(View.VISIBLE);
                }break;

                case DirtyData.STATE_SUCCESS:
                {
                    textViewState.setText(getString(R.string.result_success));
                    textViewState.setTextColor(iColorSuccess);
                    textViewState.setVisibility(View.VISIBLE);
                    progressBarState.setVisibility(View.GONE);
                }break;

                case DirtyData.STATE_FAILURE:
                {
                    textViewState.setText(getString(R.string.result_failure));
                    textViewState.setTextColor(iColorFailure);
                    textViewState.setVisibility(View.VISIBLE);
                    progressBarState.setVisibility(View.GONE);
                }break;

            }
        }

        class ViewHolder {

            private TextView textViewAction;
            private TextView textViewClassName;
            private TextView textViewJson;
            private TextView textViewState;
            private ProgressBar progressBarState;
        }
    }
}