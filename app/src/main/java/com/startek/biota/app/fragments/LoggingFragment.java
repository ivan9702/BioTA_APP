package com.startek.biota.app.fragments;

import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
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
import com.startek.biota.app.models.RunningLog;
import com.startek.biota.app.utils.Converter;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * 『運行記錄』
 *
 * 1. 按鈕『返回』，轉跳至『設定畫面』
 * 2. 搜尋欄
 * 3. 按鈕『人員進出』，ToggleButton，顯示/隱藏人員進出記錄
 * 4. 按鈕『資料維護』，ToggleButton，顯示/隱藏資料維護記錄
 * 5. 按鈕『主機同步』，ToggleButton，顯示/隱藏主機同步記錄
 * 6. 按鈕『門禁控制』，ToggleButton，顯示/隱藏門禁控制記錄
 */
public class LoggingFragment extends BaseFragment implements View.OnClickListener {

    private List<ListItemData> data;
    private boolean bUserInOut;
    private boolean bDataMaintain;
    private boolean bSyncServer;
    private boolean bAccessControl;

    private ImageView buttonBack;
    private EditText editTextSearch;
    private Button buttonClear;

    private Button buttonUserInOut;
    private Button buttonDataMaintain;
    private Button buttonSyncServer;
    private Button buttonAccessControl;

    private ListView listView;
    private ListViewAdapter listViewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_logging, container, false);

        buttonBack = (ImageView)v.findViewById(R.id.buttonBack);
        editTextSearch = (EditText)v.findViewById(R.id.editTextSearch);
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
        buttonClear = (Button)v.findViewById(R.id.buttonClear);

        buttonUserInOut = (Button)v.findViewById(R.id.buttonUserInOut);
        buttonDataMaintain = (Button)v.findViewById(R.id.buttonDataMaintain);
        buttonSyncServer = (Button)v.findViewById(R.id.buttonSyncServer);
        buttonAccessControl = (Button)v.findViewById(R.id.buttonAccessControl);

        listView = (ListView)v.findViewById(R.id.listView);
        listViewAdapter = new ListViewAdapter();
        listView.setAdapter(listViewAdapter);

        buttonBack.setOnClickListener( this );
        buttonClear.setOnClickListener( this );
        buttonUserInOut.setOnClickListener( this );
        buttonDataMaintain.setOnClickListener( this );
        buttonSyncServer.setOnClickListener( this );
        buttonAccessControl.setOnClickListener( this );

        bUserInOut = true;
        bDataMaintain = true;
        bSyncServer = true;
        bAccessControl = true;
        updateButtons();

        return v;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        updateListView(editTextSearch.getText().toString());
    }

    private int getCategories()
    {
        int categories = 0;
        if(bUserInOut) categories = categories | RunningLog.CATEGORY_USER_IN_OUT;
        if(bDataMaintain) categories = categories | RunningLog.CATEGORY_DATA_MAINTAIN;
        if(bSyncServer) categories = categories | RunningLog.CATEGORY_SYNC_SERVER;
        if(bAccessControl) categories = categories | RunningLog.CATEGORY_ACCESS_CONTROL;

        return categories;
    }

    private void updateListView(String filteredStr) {

        int categories = getCategories();

        List<RunningLog> logs = Global.getCache().queryRunningLogs(categories, filteredStr);

        data = new ArrayList<ListItemData>();
        for (RunningLog log : logs)
        {
            ListItemData delta = new ListItemData(log);
            data.add(delta);
        }

        listViewAdapter.setData(data);
    }

    private void deleteRunningLogs()
    {
        int categories = getCategories();
        String filteredStr = editTextSearch.getText().toString();
        int count = Global.getCache().deleteRunningLogs(categories, filteredStr);

        if(count != 0)
        {
            updateListView(filteredStr);
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

            case R.id.buttonClear:
            {
                // (1) 跳出確認視窗
                new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText(getString(R.string.warn_title_delete_runninglogs))
                        .setContentText(getString(R.string.warn_content_delete_runninglogs))
                        .setConfirmText(context.getString(R.string.yes))
                        .setCancelText(context.getString(R.string.no))
                        .showCancelButton(true)
                        .showContentText(true)
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismissWithAnimation();
                                deleteRunningLogs();
                            }
                        })
                        .show();
            }break;

            case R.id.buttonUserInOut:
            {
                bUserInOut = !bUserInOut;
                updateButtons();
                updateListView(editTextSearch.getText().toString());
            }break;

            case R.id.buttonDataMaintain:
            {
                bDataMaintain = !bDataMaintain;
                updateButtons();
                updateListView(editTextSearch.getText().toString());
            }break;


            case R.id.buttonSyncServer:
            {
                bSyncServer = !bSyncServer;
                updateButtons();
                updateListView(editTextSearch.getText().toString());
            }break;

            case R.id.buttonAccessControl:
            {
                bAccessControl = !bAccessControl;
                updateButtons();
                updateListView(editTextSearch.getText().toString());
            }break;
        }
    }

    private void updateButtons()
    {
        try
        {
            Drawable toggleOn = ResourcesCompat.getDrawable(getResources(), R.drawable.button_toggle_on, null);
            Drawable toggleOff = ResourcesCompat.getDrawable(getResources(),R.drawable.button_toggle_off, null);

            buttonUserInOut.setBackground(bUserInOut ? toggleOn : toggleOff);
            buttonDataMaintain.setBackground(bDataMaintain ? toggleOn : toggleOff);
            buttonSyncServer.setBackground(bSyncServer ? toggleOn : toggleOff);
            buttonAccessControl.setBackground(bAccessControl ? toggleOn : toggleOff);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Log.e(getLogTag(), ex.getMessage());
        }
    }

    class ListItemData
    {
        private RunningLog log;

        public ListItemData(RunningLog log)
        {
            this.log = log;
        }

        private String datetime;
        private String event;
        private String operator;
        private String description;
        private String result;

        public String getDatetime() {
            return Converter.toString(log.date, Converter.DateTimeFormat.YYYYMMddHHmmss);
        }

        public String getEvent() {
            return log.event;
        }

        public String getOperator() {
            return log.operator;
        }

        public String getDescription() {
            return log.description;
        }

        public String getResult() {
            return log.result;
        }

        public boolean isSuccess() {
            return log.success;
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
                convertView = inflater.inflate(R.layout.list_item_logging, parent, false);

                holder = new ViewHolder();
                holder.textViewDatetime = (TextView)convertView.findViewById(R.id.textViewDatetime);
                holder.textViewEvent = (TextView)convertView.findViewById(R.id.textViewEvent);
                holder.textViewOperator = (TextView)convertView.findViewById(R.id.textViewOperator);
                holder.textViewDescription = (TextView)convertView.findViewById(R.id.textViewDescription);
                holder.textViewResult = (TextView)convertView.findViewById(R.id.textViewResult);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ListItemData delta = data.get(position);

            holder.textViewDatetime.setText(delta.getDatetime());
            holder.textViewEvent.setText(delta.getEvent());
            holder.textViewOperator.setText(delta.getOperator());
            holder.textViewDescription.setText(delta.getDescription());
            holder.textViewResult.setText(delta.getResult());
            holder.textViewResult.setTextColor(context.getResources().getColor(delta.isSuccess() ? R.color.colorGreen : R.color.colorRed));

            return convertView;
        }

        class ViewHolder {
            private TextView textViewDatetime;
            private TextView textViewEvent;
            private TextView textViewOperator;
            private TextView textViewDescription;
            private TextView textViewResult;
        }
    }
}


