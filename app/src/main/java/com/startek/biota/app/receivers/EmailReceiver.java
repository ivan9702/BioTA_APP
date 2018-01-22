package com.startek.biota.app.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.startek.biota.app.global.Global;
import com.startek.biota.app.jobs.EmailJob;
import com.startek.biota.app.utils.Converter;

import java.util.Calendar;
import java.util.List;

/**
 *  發送 Email 方式(4)
 *      每秒執行一次指定的Alarm, 發送 Broadcast 請求
 *      偵測是否滿足發送Email的指定時間，如果滿足就發送Email
 */
public class EmailReceiver extends BroadcastReceiver
{
    private static final String TAG = "EmailReceiver";

    private static final int ONE_MINUTE = 60 * 1000;

    private static int getDelay(Calendar now)
    {
        try
        {
            int delay = ONE_MINUTE - (1000 * now.get(Calendar.SECOND) + now.get(Calendar.MILLISECOND));
            return delay;
        }
        catch (Exception e)
        {
            Log.e(TAG, String.format("getDelay, e = %s", e.getMessage()));
            return ONE_MINUTE;
        }
    }

    private static PendingIntent pendingIntent;

    public static void register(Context context)
    {
        Class<?> cls = EmailReceiver.class;

        try
        {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            if(pendingIntent != null)
            {
                pendingIntent.cancel();

                alarmManager.cancel(pendingIntent);
            }

            String identity = String.format("%s", TAG);

            int requestCode = Math.abs(identity.hashCode());

            // http://stackoverflow.com/questions/7496603/how-to-create-different-pendingintent-so-filterequals-return-false
            Intent intent = new Intent(context, cls);
            intent.setAction(identity);

            pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MILLISECOND, getDelay(calendar));

            int type = AlarmManager.RTC_WAKEUP;
            long triggerAtMillis = calendar.getTimeInMillis();
            long intervalMillis = ONE_MINUTE;
            PendingIntent operation = pendingIntent;
            alarmManager.setRepeating(type, triggerAtMillis, intervalMillis, operation);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        sendEmailIfAtTime(context);
    }

    private void sendEmailIfAtTime(Context context)
    {
        try
        {
            Calendar now = Calendar.getInstance();

            Log.d(TAG, String.format("sendEmailIfAtTime, now = %s", Converter.toString(now.getTime(), Converter.DateTimeFormat.YYYYMMddHHmmssSSSZ)));

            int hour = now.get(Calendar.HOUR_OF_DAY);
            int minute = now.get(Calendar.MINUTE);

            List<Calendar> calendars = Global.getConfig().getEmailTimes();
            for(Calendar calendar:calendars)
            {
                if(calendar.get(Calendar.HOUR_OF_DAY) == hour &&
                        calendar.get(Calendar.MINUTE) == minute)
                {
                    Log.d(TAG, "發送EMAIL");
                    new Thread(new EmailJob(context, EmailJob.ACTION_EMAILLOG)).start();
                }
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, String.format("發送EMAIL失敗，原因：%s", e.getMessage()));
        }
    }
}
