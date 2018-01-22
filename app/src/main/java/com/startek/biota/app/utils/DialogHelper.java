package com.startek.biota.app.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.devspark.appmsg.AppMsg;
import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by skt90u on 2016/3/20.
 */
public class DialogHelper {

    public static final int EXCEPTION_DURATION = 10000;

    private static final String TAG = "DialogHelper";

    // ----------------------------------------
    // AppMsg
    //
    // 參考資料
    //      http://johnkil.github.io/Android-AppMsg/
    //      http://gundumw100.iteye.com/blog/2005696
    // ----------------------------------------

    public static AppMsg alert(Activity context, CharSequence text)
    {
        return alert(context, text, 3000);
    }

    public static AppMsg alert(Activity context, CharSequence text, int duration)
    {
        AppMsg appMsg = AppMsg.makeText(context, text, new AppMsg.Style(duration, com.devspark.appmsg.R.color.alert));
        appMsg.setLayoutGravity(Gravity.BOTTOM);
        appMsg.show();
        return appMsg;
    }

    public static AppMsg confirm(Activity context, CharSequence text)
    {
        return confirm(context, text, 1000);
    }

    public static AppMsg confirm(Activity context, CharSequence text, int duration)
    {
        AppMsg appMsg = AppMsg.makeText(context, text, new AppMsg.Style(duration, com.devspark.appmsg.R.color.confirm));
        appMsg.setLayoutGravity(Gravity.BOTTOM);
        appMsg.show();
        return appMsg;
    }

    public static AppMsg info(Activity context, CharSequence text)
    {
        return info(context, text, 1000);
    }

    public static AppMsg info(Activity context, CharSequence text, int duration)
    {
        AppMsg appMsg = AppMsg.makeText(context, text, new AppMsg.Style(duration, com.devspark.appmsg.R.color.info));
        appMsg.setLayoutGravity(Gravity.BOTTOM);
        appMsg.show();
        return appMsg;
    }

    // ----------------------------------------
    // NetworkProgress
    // ----------------------------------------

    public static final int POSITION_BOTTOM = 2;
    public static final int POSITION_MIDDLE = 0;
    public static final int POSITION_TOP = 1;

    private static final Object lock;
    private static Dialog networkAlert;

    static {
        lock = new Object();
        networkAlert = null;
    }

    public static void openNetworkProgress(Activity context) {
        //openNetworkAlert(context, POSITION_BOTTOM);
        openNetworkAlert(context, POSITION_MIDDLE);
    }

    public static void closeNetworkProgress() {
        closeNetworkAlert();
    }

    private static void openNetworkAlert(Activity context, int position) {

        synchronized (lock) {
            if (networkAlert != null) return;

            networkAlert = new Dialog(context);

            networkAlert.getWindow().requestFeature(POSITION_TOP);

            networkAlert.getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    258);

            networkAlert.setContentView(R.layout.fragment_dialog_network_progress);

            networkAlert.getWindow().setBackgroundDrawable(new ColorDrawable(POSITION_MIDDLE));

            WindowManager.LayoutParams lp = networkAlert.getWindow().getAttributes();
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            switch (position) {
                case POSITION_TOP /*1*/:
                    lp.y -= metrics.heightPixels / 4;
                    break;
                case POSITION_BOTTOM /*2*/:
                    lp.y += metrics.heightPixels / 4;
                    break;
            }
            networkAlert.getWindow().setAttributes(lp);

            networkAlert.setCancelable(false);

            networkAlert.show();
        }
    }

    private static void closeNetworkAlert() {
        synchronized (lock) {
            if (networkAlert != null) {
                new DismissAction(networkAlert).run();
                networkAlert = null;
            }
            //OnClickStopper.unlock();
        }
    }



    private static class DismissAction implements Runnable {
        private final String TAG = "DismissAction";
        private Dialog dialog;

        public DismissAction(Dialog dialog) {
            this.dialog = dialog;
        }

        public void run() {
            if (Looper.getMainLooper() != Looper.myLooper()) {
                new Handler(Looper.getMainLooper()).post(this);
                return;
            }
            try {
                if (this.dialog.isShowing() || (this.dialog.getWindow() != null && this.dialog.getWindow().isActive())) {
                    this.dialog.dismiss();
                } else {
                    Log.d(TAG, "DismissAction#run Not showing dialog.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ----------------------------------------
    // item editor
    // ----------------------------------------

    public static interface OkClickListener
    {
        public void onClick(String result);
    }

    public static AlertDialog.Builder showTextEditor(Context context, String title, final TextView textView)
    {
        return showTextEditor(
                context,
                title,
                textView.getText().toString(),
                new OkClickListener()
                {
                    @Override
                    public void onClick(String result) {
                        textView.setText(result);
                    }
                },
                false,
                -1);
    }

    public static AlertDialog.Builder showTextEditor(
            Context context,
            String title,
            final TextView textView,
            boolean numberOnly,
            int maxLength)
    {
        return showTextEditor(
                context,
                title,
                textView.getText().toString(),
                new OkClickListener()
                {
                    @Override
                    public void onClick(String result) {
                        textView.setText(result);
                    }
                },
                numberOnly,
                maxLength);
    }

    /**
     * http://androidbiancheng.blogspot.tw/2011/05/alertdialogbuilder-edittext.html
     * http://stackoverflow.com/questions/11519214/set-edittext-using-only-0123456789-programmatically-in-android
     * http://stackoverflow.com/questions/3285412/limit-text-length-of-edittext-in-android
     */
    public static AlertDialog.Builder showTextEditor(
            Context context,
            String title,
            String defaultValue,
            final OkClickListener okClickListener,
            boolean numberOnly,
            int maxLength)
    {
        boolean autoPopSoftInput = true; // 是否在 dialog 建立後就自動彈出鍵盤

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);

        final EditText editText = new EditText(context);
        editText.setText(defaultValue);

        if(numberOnly)
            //editText.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            editText.setRawInputType(InputType.TYPE_CLASS_NUMBER);

        if(maxLength > 0)
        {
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        }

        builder.setView(editText);

        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Global.setFullscreen();
                if (okClickListener != null) {
                    okClickListener.onClick(editText.getText().toString());
                }
            }
        });

        builder.setNegativeButton(context.getString(R.string.canel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Global.setFullscreen();
            }
        });

        final AlertDialog dialog = builder.show();

        // http://stackoverflow.com/questions/3455235/when-using-alertdialog-builder-with-edittext-the-soft-keyboard-doesnt-pop
        // http://stackoverflow.com/questions/2403632/android-show-soft-keyboard-automatically-when-focus-is-on-an-edittext
        // http://stackoverflow.com/questions/8537518/the-method-getwindow-is-undefined-for-the-type-alertdialog-builder
//        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus) {
//                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//                }
//            }
//        });

        // http://stackoverflow.com/questions/12997273/alertdialog-with-edittext-open-soft-keyboard-automatically-with-focus-on-editte
        // 建立或編輯使用者姓名,工號,職稱(或其他輸入項目的)鍵盤未被自動升起
        if(autoPopSoftInput)
        {
            SoftInputHelper.show((Activity)context);
        }


        return builder;
    }

    public static AlertDialog.Builder showSingleChooser(Context context, String title, final TextView textView, final List<String> options)
    {
        return showSingleChooser(
                context,
                title,
                textView.getText().toString(),
                new OkClickListener() {
                    @Override
                    public void onClick(String result) {
                        textView.setText(result);
                    }
                },
                options);
    }

    /**
     * http://stackoverflow.com/questions/8605301/alertdialog-with-selector
     */
    public static AlertDialog.Builder showSingleChooser(
            Context context,
            String title,
            String defaultValue,
            final OkClickListener okClickListener,
            final List<String> options)
    {
        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        CharSequence items[] = Converter.toCharSequence(options);

        String text = defaultValue;

        int checkedItem = 0; // default value
        for(int i=0; i<options.size(); i++)
        {
            if(options.get(i).equals(text))
            {
                checkedItem = i;
                break;
            }
        }

        if(!TextUtils.isEmpty(title)) adb.setTitle(title);

        adb.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface d, int n) {
                d.dismiss();
                Global.setFullscreen();

                if (okClickListener != null) {
                    okClickListener.onClick(options.get(n));
                }
            }

        });

        adb.show();

        return adb;
    }

    public static AlertDialog.Builder showMultiChooser(
            Context context,
            String title,
            String defaultValue,
            final OkClickListener okClickListener,
            final List<String> options)
    {
        final CharSequence items[] = Converter.toCharSequence(options);
        final boolean[] checkedItems = new boolean[options.size()];

        List<String> values = new ArrayList<String>(Arrays.asList(defaultValue.split(",", -1)));

        for(int i=0; i<items.length; i++)
        {
            String item = items[i].toString();
            checkedItems[i] = values.contains(item);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMultiChoiceItems(items, checkedItems,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedItemId,
                                        boolean isSelected) {
                        checkedItems[selectedItemId] = isSelected;
                    }
                })
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        Global.setFullscreen();

                        if (okClickListener != null) {
                            List<String> chooses = new ArrayList<String>();

                            for (int i = 0; i < checkedItems.length; i++) {
                                if (checkedItems[i])
                                    chooses.add(items[i].toString());
                            }

                            String result = TextUtils.join(",", chooses);

                            okClickListener.onClick(result);
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Global.setFullscreen();
                    }
                });

        builder.show();

        return builder;
    }

    public static DatePickerDialog showDatePicker(Context context, String title, final TextView textView)
    {
        return showDatePicker(
                context,
                title,
                textView.getText().toString(),
                new OkClickListener() {
                    @Override
                    public void onClick(String result) {
                        textView.setText(result);
                    }
                });
    }

    public static DatePickerDialog showDatePicker(
            Context context,
            String title,
            String defaultValue,
            final OkClickListener okClickListener)
    {
        String strDate = defaultValue;

        Calendar birthday = TextUtils.isEmpty(strDate)
                ? Calendar.getInstance()
                : Converter.toCalendar(strDate);

        int year = birthday.get(Calendar.YEAR);
        int month = birthday.get(Calendar.MONTH);
        int day = birthday.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Global.setFullscreen();

                if(okClickListener != null)
                {
                    Calendar calendar = new GregorianCalendar(year, monthOfYear, dayOfMonth);
                    okClickListener.onClick(Converter.toString(calendar));
                }

            }
        }, year, month, day);

        if(!TextUtils.isEmpty(title)) datePickerDialog.setTitle(title);

        datePickerDialog.show();

        return datePickerDialog;
    }

    public static TimePickerDialog showTimePicker(
            Context context,
            String title,
            String defaultValue,
            final OkClickListener okClickListener)
    {
        String strDate = defaultValue;

        Calendar calendar = TextUtils.isEmpty(strDate)
                ? Calendar.getInstance()
                : Converter.toCalendar(strDate);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        int hourOfDay = 0;
        int minute = 0;
        try
        {
            String[] tokens = defaultValue.split(":");
            hourOfDay = tokens.length > 0 ? Integer.parseInt(tokens[0]) : 0;
            minute = tokens.length > 1 ? Integer.parseInt(tokens[1]) : 0;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Log.e(TAG, ex.getMessage());
        }
        boolean is24HourView = true;

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                context,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Global.setFullscreen();

                        if(okClickListener != null)
                        {
                            String format = "HH:mm";
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(Calendar.MILLISECOND, 0);
                            calendar.set(Calendar.SECOND, 0);
                            calendar.set(Calendar.MINUTE, minute);
                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            okClickListener.onClick(Converter.toString(calendar.getTime(), format));
                        }
                    }
                },
                hourOfDay,
                minute,
                is24HourView);

        if(!TextUtils.isEmpty(title)) timePickerDialog.setTitle(title);

        timePickerDialog.show();

        return timePickerDialog;
    }
}
