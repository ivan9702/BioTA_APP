package com.startek.biota.app.managers;

import android.app.Activity;
import android.content.Context;

import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.models.DirtyData;
import com.startek.biota.app.models.Human;
import com.startek.biota.app.models.MatchLog;
import com.startek.biota.app.models.RunningLog;
import com.startek.biota.app.network.webservices.WsMatchLog;

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.impl.DeferredObject;

import java.util.Date;

import labs.anton.icenet.RequestError;

/**
 * MatchLog 相關操作
 */
public class MatchLogManager
{
    private Activity context;

    public MatchLogManager(Activity context)
    {
        this.context = context;
    }

    private String getString(int resId) {
        return context == null ? "" : context.getString(resId);
    }

    /**
     * 註冊指紋
     */
    public Deferred createMatchLogRi(
            String minutiae,
            String pic,
            Date STime,
            long CTime,
            int MScore,
            long MTime,
            boolean is_success,
            String client_action)
    {
//        MatchLog matchLog = new MatchLog();
//
//        matchLog.type = MatchLog.CATEGORY_ENROLL;
//        matchLog.minutiae = minutiae;
//        matchLog.pic = pic;
//        matchLog.humanId = null;
//        matchLog.bind_id = null;
//
//        matchLog.tagId = null;
//        matchLog.STime = STime;
//        matchLog.CTime = CTime;
//        matchLog.MScore = MScore;
//        matchLog.MTime = MTime;
//
//        matchLog.is_success = is_success;
//        matchLog.client_action = client_action;
//
//        return createMatchLog(matchLog);

        Deferred deferred = new DeferredObject();
        deferred.resolve("20160712 Android 端 不寫 Ri");
        return deferred;
    }

    /**
     * 驗證指紋
     */
    public Deferred createMatchLogRv(
            String minutiae,
            String pic,
            String humanId,
            String bind_id,
            String tagId,
            Date STime,
            long CTime,
            int MScore,
            long MTime,
            boolean is_success,
            String client_action)
    {
        MatchLog matchLog = new MatchLog();

        matchLog.type = MatchLog.CATEGORY_VERIFY;
        matchLog.minutiae = minutiae;
        matchLog.pic = pic;
        matchLog.humanId = humanId;
        matchLog.bind_id = bind_id;

        matchLog.tagId = tagId;
        matchLog.STime = STime;
        matchLog.CTime = CTime;
        matchLog.MScore = MScore;
        matchLog.MTime = MTime;

        matchLog.is_success = is_success;
        matchLog.client_action = client_action;

        return createMatchLog(matchLog);
    }

    private Deferred createMatchLog(final MatchLog matchLog)
    {
        final Deferred deferred = new DeferredObject();

        if(Global.getConfig().useBiotaServer())
        {
            createMatchLog_remote(matchLog).promise().done(new DoneCallback<String>() {
                @Override
                public void onDone(String result) {
                    deferred.resolve(result);
                }
            }).fail(new FailCallback<String>() {
                @Override
                public void onFail(String result) {
                    createMatchLog_local(matchLog).promise().done(new DoneCallback<String>() {
                        @Override
                        public void onDone(String result) {
                            deferred.resolve(result);
                        }
                    }).fail(new FailCallback<String>() {
                        @Override
                        public void onFail(String result) {
                            deferred.reject(result);
                        }
                    });
                }
            });
        }
        else {
            return createMatchLog_local(matchLog);
        }

        return deferred;
    }

    private Deferred createMatchLog_local(final MatchLog matchLog)
    {
        final Deferred deferred = new DeferredObject();

        try
        {
            Global.getCache().createMatchLog(matchLog);
            Global.getCache().createDirtyData(DirtyData.ACTION_CREATE, matchLog);

//            String result = String.format("建立本機 MatchLog(%s) 成功", matchLog.toString());
            String message = String.format("%s成功", matchLog.type);
            deferred.resolve(message);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();

            //String message = String.format("建立本機 MatchLog(%s) 失敗，%s", matchLog.toString(), ex.getMessage());
            String message = String.format("%s失敗", matchLog.type);
            deferred.reject(message);
        }

        deferred.promise()
        .done(new DoneCallback<String>() {
            @Override
            public void onDone(String result) {

                Global.getCache().createRunningLog(
                        getRunningLogCategory(matchLog),
                        //getString(R.string.runninglog_event_createMatchLog_local),
                        "Client端比對指紋資料記錄",
                        toOperator(matchLog.bind_id),
                        result,
                        getString(R.string.result_success),
                        true);
            }
        })
        .fail(new FailCallback<String>() {
            @Override
            public void onFail(String result) {
                Global.getCache().createRunningLog(
                        getRunningLogCategory(matchLog),
                        //getString(R.string.runninglog_event_createMatchLog_local),
                        "Client端比對指紋資料記錄",
                        toOperator(matchLog.bind_id),
                        result,
                        getString(R.string.result_failure),
                        false);
            }
        });

        return deferred;
    }

    private int getRunningLogCategory(MatchLog matchLog)
    {
        /*
        人員進出
            -指紋認證
        資料維護
            -人員資料,CUD
        主機同步
            -同步資料
            -發送email
            -指紋資料上傳
        門禁控制
         */
		 
		 return RunningLog.CATEGORY_SYNC_SERVER;
		 
        // if(matchLog.type.equals(MatchLog.CATEGORY_ENROLL))
            // return RunningLog.CATEGORY_DATA_MAINTAIN;
        // else
            // return RunningLog.CATEGORY_USER_IN_OUT;
    }

    public Deferred createMatchLog_remote(final MatchLog matchLog)
    {
        Context context = null; // 目前 createMatchLog_remote 會被用到 FingerprintSensor 裡面的 Thread 中

        final Deferred deferred = new DeferredObject();

        // "匯出字樣調整 建立matchlog改為 Client端比對指紋資料記錄 詳細描述改為Ri或Rv + 成功 or 失敗
        // 匯出失敗 不需要寫出原因"

        new WsMatchLog(context, matchLog).execute().promise().done(new DoneCallback<WsMatchLog.Response>() {
            @Override
            public void onDone(WsMatchLog.Response result) {
                //String message = String.format("建立遠端 MatchLog(%s) 成功", matchLog.toString());
                String message = String.format("%s成功", matchLog.type);
                deferred.resolve(message);
            }
        }).fail(new FailCallback<RequestError>() {
            @Override
            public void onFail(RequestError result) {
                String errorMessage = result.getMessage();
                //String message = String.format("建立遠端 MatchLog(%s) 失敗，%s", matchLog.toString(), errorMessage);
                String message = String.format("%s失敗", matchLog.type);
                deferred.reject(message);
            }
        });

        deferred.promise()
        .done(new DoneCallback<String>() {
            @Override
            public void onDone(String result) {
                Global.getCache().createRunningLog(
                        getRunningLogCategory(matchLog),
                        //getString(R.string.runninglog_event_createMatchLog_remote),
                        "Client端比對指紋資料記錄",
                        toOperator(matchLog.bind_id),
                        result,
                        getString(R.string.result_success),
                        true);
            }
        })
        .fail(new FailCallback<String>() {
            @Override
            public void onFail(String result) {
                Global.getCache().createRunningLog(
                        getRunningLogCategory(matchLog),
                        //getString(R.string.runninglog_event_createMatchLog_remote),
                        "Client端比對指紋資料記錄",
                        toOperator(matchLog.bind_id),
                        result,
                        getString(R.string.result_failure),
                        false);
            }
        });

        return deferred;
    }

    private String toOperator(String bind_id)
    {
        Human human = HumanManager.getHumanByBindId(bind_id);

        return human != null ? human.name : bind_id;
    }
}
