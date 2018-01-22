package com.startek.biota.app.managers;

import android.app.Activity;
import android.text.TextUtils;

import com.startek.biota.app.R;
import com.startek.biota.app.enums.EditMode;
import com.startek.biota.app.fragments.TransitionListener;
import com.startek.biota.app.fragments.UserManagerFragment2;
import com.startek.biota.app.fragments.UserManagerFragment4;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.models.EasyCard;
import com.startek.biota.app.models.DirtyData;
import com.startek.biota.app.models.Fingerprint;
import com.startek.biota.app.models.Human;
import com.startek.biota.app.models.Nfc;
import com.startek.biota.app.models.RunningLog;
import com.startek.biota.app.models.Team;
import com.startek.biota.app.network.webservices.WsFingerprintC;
import com.startek.biota.app.network.webservices.WsFingerprintD;
import com.startek.biota.app.network.webservices.WsHumanC;
import com.startek.biota.app.network.webservices.WsHumanD;
import com.startek.biota.app.network.webservices.WsHumanR1;
import com.startek.biota.app.network.webservices.WsHumanR2;
import com.startek.biota.app.network.webservices.WsHumanU;
import com.startek.biota.app.network.webservices.WsNfcC;
import com.startek.biota.app.network.webservices.WsNfcD;
import com.startek.biota.app.utils.Converter;


import org.jdeferred.AlwaysCallback;
import org.jdeferred.Deferred;
import org.jdeferred.DeferredManager;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import com.startek.biota.app.network.webservices.SequenceDeferredManager;
import com.startek.fm210.tstlib;

import org.jdeferred.impl.DefaultDeferredManager;
import org.jdeferred.impl.DeferredObject;
import org.jdeferred.multiple.MultipleResults;
import org.jdeferred.multiple.OneReject;
import org.jdeferred.multiple.OneResult;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import labs.anton.icenet.RequestError;

/**
 * Human 相關操作
 *
 * 20160627 Norman, 使用 SequenceDeferredManager 取得 DefaultDeferredManager
 */
public class HumanManager
{
    private Activity context;

    public HumanManager(Activity context)
    {
        this.context = context;
    }

    private String getString(int resId) {
        return context == null ? "" : context.getString(resId);
    }

    // ----------------------------------------
    // getHumanByXXX
    // ----------------------------------------

    public static Human getHumanById(String humanId)
    {
        List<Human> humanList = Global.getHumanList();
        if(humanList == null)return null;

        for(Human human:humanList)
        {
            if(human.id.equals(humanId))
                return human;
        }

        return null;
    }

    public static Human getHumanByBindId(String bind_id)
    {
        List<Human> humanList = Global.getHumanList();
        if(humanList == null)return null;

        for(Human human:humanList)
        {
            if(human.bind_id.equals(bind_id))
                return human;
        }

        return null;
    }

    public static Nfc getNfcByEasyCard(EasyCard easyCard)
    {
        if(easyCard == null)return null;

        List<Human> humanList = Global.getHumanList();
        if(humanList == null)return null;

        for(Human human:humanList)
        {
            for(Nfc nfc:human.getNfcs())
            {
                if(nfc.tagId.equals(easyCard.getTagId()))
                    return nfc;
            }
        }

        return null;
    }

    public static Human getHumanByNfc(Nfc aNfc)
    {
        if(aNfc == null)return null;

        List<Human> humanList = Global.getHumanList();
        if(humanList == null)return null;

        for(Human human:humanList)
        {
            for(Nfc nfc:human.getNfcs())
            {
                if(nfc.tagId.equals(aNfc.tagId))
                    return human;
            }
        }

        return null;
    }

    // ----------------------------------------
    // Load Human
    // ----------------------------------------

    public Deferred loadHuman()
    {
        final Deferred deferred = new DeferredObject();
        final List<Human> humanList = new ArrayList<Human>();

        List<Human> localHuman = Global.getCache().queryHuman();
        humanList.addAll(localHuman);


        loadHuman_remote().promise().done(new DoneCallback<List<Human>>() {
            @Override
            public void onDone(List<Human> remoteHuman)
            {
                humanList.addAll(remoteHuman);

                Global.setHumanList(humanList);
                deferred.resolve(Global.getHumanList());
            }
        }).fail(new FailCallback<RequestError>() {
            @Override
            public void onFail(RequestError result) {

                Global.setHumanList(humanList);
                deferred.resolve(Global.getHumanList());
            }
        });

        return deferred;
    }

    private Deferred loadHuman_remote() {
        final Deferred deferred = new DeferredObject();

        if (Global.getConfig().useBiotaServer()) {
            new WsHumanR1(context).execute().promise()
                    .done(new DoneCallback<WsHumanR1.Response>() {
                        @Override
                        public void onDone(final WsHumanR1.Response result1) {

                            if (!result1.result.success) {
                                deferred.resolve(new ArrayList<Human>()); // 發生錯誤，回傳空陣列
                                return;
                            }

                            DeferredManager dm = new DefaultDeferredManager();

                            List<Promise> promises = new ArrayList<Promise>();

                            // 20160629 Norman, fix empty array bug
                            if(result1.data.size() == 0)
                            {
                                deferred.resolve(new ArrayList<Human>());
                                return;
                            }

                            for (WsHumanR1.Response.Delta delta : result1.data) {
                                promises.add(new WsHumanR2(context, delta.id).execute().promise());
                            }

                            dm.when(Converter.toArray(promises))
                                    .done(new DoneCallback<MultipleResults>()
                                    {
                                        @Override
                                        public void onDone(MultipleResults result2)
                                        {
                                            try
                                            {
                                                List<Human> remoteHuman = new ArrayList<Human>();
                                                List<String> datPaths = new ArrayList<String>();

                                                for (int i = 0; i < result2.size(); i++)
                                                {
                                                    OneResult result3 = result2.get(i);

                                                    String id = result1.data.get(i).id;

                                                    WsHumanR2.Response res = (WsHumanR2.Response) result3.getResult();

                                                    // 發生錯誤，忽略此比資料
                                                    if (!res.result.success) continue;

                                                    WsHumanR2.Response.Data r2Data = res.data;

                                                    WsHumanR1.Response.Delta r1Data = getDelta(result1, id);

                                                    if (r1Data == null) continue;

                                                    Human human = new Human();

                                                    human.id = r1Data.id;
                                                    human.name = r2Data.name;
                                                    human.bind_id = r2Data.bind_id;
                                                    human.job = r2Data.job;
                                                    //human.birthday = r2Data.birthday;
                                                    human.birthday = Converter.toString(Converter.toDate(r2Data.birthday, Converter.DateTimeFormat.ServerDate), Converter.DateTimeFormat.YYYYMMdd);
                                                    human.createdAt = Converter.toDate(r2Data.birthday, Converter.DateTimeFormat.ServerDate);
                                                    human.gender = r2Data.gender;
                                                    human.bloodtype = r2Data.bloodtype;
                                                    human.dept = r2Data.dept;
                                                    human.is_manager = r2Data.is_manager;
                                                    human.is_local = false;
                                                    human.createdAt = Converter.toDate(r2Data.createdAt, Converter.DateTimeFormat.ServerDate);
                                                    human.updatedAt = Converter.toDate(r2Data.updatedAt, Converter.DateTimeFormat.ServerDate);

                                                    List<Team> teams = new ArrayList<Team>();
                                                    if(r2Data.team != null)
                                                    {
                                                        for (final WsHumanR2.Response.Data.Team team : r2Data.team) {
                                                            Team t = new Team();
                                                            t.humanId = human.id;
                                                            t.memberId = team.id;
                                                            t.arrived = team.arrived;
                                                            teams.add(t);
                                                        }
                                                    }

                                                    human.getTeams().clear();
                                                    human.getTeams().addAll(teams);

                                                    List<Fingerprint> fingerprints = new ArrayList<Fingerprint>();
                                                    for (final WsHumanR1.Response.Delta.Finger finger : r1Data.f) {
                                                        Fingerprint f = new Fingerprint();
                                                        f.id = 0; // 從伺服器傳來的資料(無id欄位)，此欄位將設定為為 0
                                                        f.humanId = human.id;
                                                        // TODO: 下個階段(從伺服器驗證指紋的階段) 需要此欄位
                                                        f.minutiae = finger.minutiae; // 20160713  線上載下來特徵碼 產生dat檔給紅色使用者 使用
                                                        f.which = finger.which;
                                                        f.pic = finger.pic;
                                                        f.f_id = finger.id; // 20160711 Norman, 回傳指紋資料主鍵值(使用在刪除指紋資料)
                                                        fingerprints.add(f);

                                                        createDatIfNoExist(human, f);
                                                    }
                                                    human.getFingerprints().clear();
                                                    human.getFingerprints().addAll(fingerprints);

                                                    List<Nfc> nfcs = new ArrayList<Nfc>();
                                                    //for (final String nfcTagId : r1Data.nfc)
                                                    for (final WsHumanR1.Response.Delta.Nfc nfc : r1Data.nfc)
                                                    {
                                                        Nfc n = new Nfc();
                                                        n.id = 0; // 從伺服器傳來的資料(無id欄位)，此欄位將設定為為 0
                                                        n.humanId = human.id;
                                                        n.tagId = nfc.id;
                                                        nfcs.add(n);
                                                    }
                                                    human.getNfcs().clear();
                                                    human.getNfcs().addAll(nfcs);

                                                    if(human.bind_id == null)human.bind_id = "";

                                                    remoteHuman.add(human);

                                                } // for (int i = 0; i < result2.size(); i++)

                                                // TODO: 檢查遠端 remoteHuman 對應 dat 檔案是否存在此檯手機中，如果不存在，就下載
                                                // https://android-arsenal.com/details/1/2131
                                                // https://android-arsenal.com/details/1/2814
                                                // https://android-arsenal.com/details/1/2959

                                                deferred.resolve(remoteHuman);
                                            }
                                            catch (Exception e)
                                            {
                                                e.printStackTrace();
                                                throw e;
                                            }

                                        }
                                    })
                                    .fail(new FailCallback<OneReject>() {
                                        @Override
                                        public void onFail(OneReject result) {
                                            deferred.reject((RequestError) result.getReject());
                                        }
                                    });
                }
            })
            .fail(new FailCallback<RequestError>() {
                @Override
                public void onFail(RequestError error) {
                    deferred.reject(error);
                }
            });

        }
        else
        {
            deferred.resolve(new ArrayList<Human>()); // 不使用 SERVER，回傳空陣列
        }

        return deferred;
    }

    private boolean createDatIfNoExist(Human human, Fingerprint fingerprint)
    {
        try
        {
//            if(true)return true; // 若取消註解此行，代表『不要從 SERVER 下載指紋資料』，用來測試系統設定功能

            if(TextUtils.isEmpty(fingerprint.minutiae))return false;

            int fingerBtnId = Converter.englishToFingerBtnId(fingerprint.which);
            String datPath = FileManager.getDatPath(human, fingerBtnId);

            if(FileManager.exists(datPath))return true;

            byte[] minu_code2 = Converter.decodeBase64String(fingerprint.minutiae);

            tstlib adapter = new tstlib(context);

            adapter.FP_SaveISOminutia(minu_code2, datPath);

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    private WsHumanR1.Response.Delta getDelta(WsHumanR1.Response res, String id)
    {
        for(WsHumanR1.Response.Delta delta:res.data)
        {
            if(delta.id.equals(id))
                return delta;
        }
        return null;
    }

    // ----------------------------------------
    // Create Human
    // ----------------------------------------

    public Deferred createHuman(final Human human)
    {
        if(Global.getConfig().useBiotaServer())
        {
            final Deferred deferred = new DeferredObject();

            createHuman_remote(human).promise().done(new DoneCallback<String>() {
                @Override
                public void onDone(String result) {
                    deferred.resolve(result);
                }
            }).fail(new FailCallback<String>() {
                @Override
                public void onFail(String result) {
                    createHuman_local(human).promise().done(new DoneCallback<String>() {
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

            return deferred;
        }
        else
        {
            return createHuman_local(human);
        }
    }

    public Deferred createHuman_local(final Human human)
    {
        final Deferred deferred = new DeferredObject();

        try
        {
            Global.getCache().createHuman(human);
            Global.getCache().createDirtyData(DirtyData.ACTION_CREATE, human);

            deferred.resolve(String.format(getString(R.string.createHuman_local_success), human.name));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();

            deferred.reject(String.format(getString(R.string.createHuman_local_failure), human.name, ex.getMessage()));
        }

        deferred.promise()
        .done(new DoneCallback<String>() {
            @Override
            public void onDone(String result) {
                Global.getCache().createRunningLog(
                        RunningLog.CATEGORY_DATA_MAINTAIN,
                        getString(R.string.runninglog_event_createHuman_local),
                        Global.getLoginedUserName(),
                        result,
                        getString(R.string.result_success),
                        true);
            }
        })
        .fail(new FailCallback<String>() {
            @Override
            public void onFail(String result) {
                Global.getCache().createRunningLog(
                        RunningLog.CATEGORY_DATA_MAINTAIN,
                        getString(R.string.runninglog_event_createHuman_local),
                        Global.getLoginedUserName(),
                        result,
                        getString(R.string.result_failure),
                        false);
            }
        });

        return deferred;
    }

    public Deferred createHuman_remote(final Human human)
    {
        final Deferred deferred = new DeferredObject();

        new WsHumanC(context, human).execute().promise().done(new DoneCallback<WsHumanC.Response>() {
            @Override
            public void onDone(WsHumanC.Response result) {

                if (!result.result.success) {
                    deferred.reject(String.format(getString(R.string.createHuman_remote_failure1), human.name, result.result.message));
                    return;
                }

                String id = result.data.id;
                human.id = id;

                List<Promise> promises = new ArrayList<Promise>();

                // create nfc
                for (Nfc nfc : human.getNfcs()) {
                    nfc.humanId = id;
                    promises.add(new WsNfcC(context, nfc).execute().promise());
                }

                for (Fingerprint fingerprint : human.getFingerprints()) {
                    fingerprint.humanId = id;
                    promises.add(new WsFingerprintC(context, human, fingerprint).execute().promise());
                }

                DeferredManager dm = new SequenceDeferredManager();

                dm.when(Converter.toArray(promises)).done(new DoneCallback<MultipleResults>()
                {
                    @Override
                    public void onDone(MultipleResults result1) {
                        String errors = Converter.getErrors(result1);

                        if(!TextUtils.isEmpty(errors))
                        {
                            deferred.reject(String.format(getString(R.string.createHuman_remote_failure2), human.name, errors));

                            deleteHuman_remote(human, false); // 建立失敗，刪除剛剛建立的使用者
                        }
                        else
                        {
                            deferred.resolve(String.format(getString(R.string.createHuman_remote_success), human.name));
                        }
                    }
                })
                .fail(new FailCallback<OneReject>()
                {
                    @Override
                    public void onFail(OneReject result)
                    {
                        String errorMessage = ((RequestError) result.getReject()).getMessage();
                        deferred.reject(String.format(getString(R.string.createHuman_remote_failure2), human.name, errorMessage));

                        deleteHuman_remote(human, false); // 建立失敗，刪除剛剛建立的使用者
                    }
                })
                .always(new AlwaysCallback<MultipleResults, OneReject>()
                {
                    @Override
                    public void onAlways(Promise.State state, MultipleResults resolved, OneReject rejected) {
                    }
                });

            }
        }).fail(new FailCallback<RequestError>() {
            @Override
            public void onFail(RequestError result) {
                String errorMessage = result.getMessage();
                deferred.reject(String.format(getString(R.string.createHuman_remote_failure1), human.name, errorMessage));
            }
        });

        deferred.promise()
        .done(new DoneCallback<String>() {
            @Override
            public void onDone(String result) {
                Global.getCache().createRunningLog(
                        RunningLog.CATEGORY_DATA_MAINTAIN,
                        getString(R.string.runninglog_event_createHuman_remote),
                        Global.getLoginedUserName(),
                        result,
                        getString(R.string.result_success),
                        true);
            }
        })
        .fail(new FailCallback<String>() {
            @Override
            public void onFail(String result) {
            Global.getCache().createRunningLog(
                    RunningLog.CATEGORY_DATA_MAINTAIN,
                    getString(R.string.runninglog_event_createHuman_remote),
                    Global.getLoginedUserName(),
                    result,
                    getString(R.string.result_failure),
                    false);
            }
        });

        return deferred;
    }

    // ----------------------------------------
    // Update Human
    // ----------------------------------------

    public Deferred updateHuman(final Human human)
    {
        if(Global.getConfig().useBiotaServer())
        {
            final Deferred deferred = new DeferredObject();

            updateHuman_remote(human).promise()
            .done(new DoneCallback<String>() {
                @Override
                public void onDone(String result) {
                    deferred.resolve(result);
                }
            }).fail(new FailCallback<String>() {
                @Override
                public void onFail(String result) {

                    if(!human.is_local)
                    {
                        deferred.reject(result);
                        return;
                    }

                    updateHuman_local(human).promise().done(new DoneCallback<String>() {
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

            return deferred;
        }
        else
        {
            return updateHuman_local(human);
        }
    }

    public Deferred updateHuman_local(final Human human) {

        final Deferred deferred = new DeferredObject();

        try
        {
            Global.getCache().updateHuman(human);
            Global.getCache().createDirtyData(DirtyData.ACTION_UPDATE, human);

            deferred.resolve(String.format(getString(R.string.updateHuman_local_success), human.name));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            deferred.reject(String.format(getString(R.string.updateHuman_local_failure), human.name, ex.getMessage()));
        }

        deferred.promise()
        .done(new DoneCallback<String>() {
            @Override
            public void onDone(String result) {
                Global.getCache().createRunningLog(
                        RunningLog.CATEGORY_DATA_MAINTAIN,
                        getString(R.string.runninglog_event_updateHuman_local),
                        Global.getLoginedUserName(),
                        result,
                        getString(R.string.result_success),
                        true);
            }
        })
        .fail(new FailCallback<String>() {
            @Override
            public void onFail(String result) {
                Global.getCache().createRunningLog(
                        RunningLog.CATEGORY_DATA_MAINTAIN,
                        getString(R.string.runninglog_event_updateHuman_local),
                        Global.getLoginedUserName(),
                        result,
                        getString(R.string.result_failure),
                        false);
            }
        });

        return deferred;
    }

    public Deferred updateHuman_remote(final Human human) {

        if(human.is_local)
            return createHuman_remote(human);

        final Deferred deferred = new DeferredObject();

        List<Promise> promises = new ArrayList<Promise>();

        promises.add(new WsHumanU(context, human).execute().promise());

        for(Nfc nfc:human.getOriginalNfcs())
        {
            promises.add(new WsNfcD(context, nfc).execute().promise());
        }

        for(Nfc nfc:human.getNfcs())
        {
            promises.add(new WsNfcC(context, nfc).execute().promise());
        }

        for(Fingerprint fingerprint:human.getOriginalFingerprints())
        {
            promises.add(new WsFingerprintD(context, human, fingerprint).execute().promise());
        }

        for(Fingerprint fingerprint:human.getFingerprints())
        {
            promises.add(new WsFingerprintC(context, human, fingerprint).execute().promise());
        }

        DeferredManager dm = new SequenceDeferredManager();

        dm.when(Converter.toArray(promises)).done(new DoneCallback<MultipleResults>() {
            @Override
            public void onDone(MultipleResults multipleResults) {

                String errors = Converter.getErrors(multipleResults);
                if(!TextUtils.isEmpty(errors))
                {
                    deferred.reject(String.format(getString(R.string.updateHuman_remote_failure), human.name, errors));
                }
                else
                {
                    deferred.resolve(String.format(getString(R.string.updateHuman_remote_success), human.name));
                }
            }
        })
                .fail(new FailCallback<OneReject>() {
                    @Override
                    public void onFail(OneReject result) {
                        String errorMessage = ((RequestError) result.getReject()).getMessage();
                        deferred.reject(String.format(getString(R.string.updateHuman_remote_failure), human.name, errorMessage));
            }
        });

        deferred.promise()
        .done(new DoneCallback<String>() {
            @Override
            public void onDone(String result) {
                Global.getCache().createRunningLog(
                        RunningLog.CATEGORY_DATA_MAINTAIN,
                        getString(R.string.runninglog_event_updateHuman_remote),
                        Global.getLoginedUserName(),
                        result,
                        getString(R.string.result_success),
                        true);

                boolean cleanup = false;
                deleteHuman_local(human, cleanup);
            }
        })
        .fail(new FailCallback<String>() {
            @Override
            public void onFail(String result) {
                Global.getCache().createRunningLog(
                        RunningLog.CATEGORY_DATA_MAINTAIN,
                        getString(R.string.runninglog_event_updateHuman_remote),
                        Global.getLoginedUserName(),
                        result,
                        getString(R.string.result_failure),
                        false);
            }
        });

        return deferred;
    }

    // ----------------------------------------
    // Delete Human
    // ----------------------------------------

    public Deferred deleteHuman(final Human human)
    {
        final boolean cleanup = true;

        Deferred deferred = human.is_local
                ? deleteHuman_local(human, cleanup)
                : deleteHuman_remote(human, cleanup);

        return deferred;
    }

    public Deferred deleteHuman_local(final Human human, final boolean cleanup) {

        final Deferred deferred = new DeferredObject();

        try
        {
            Global.getCache().deleteHuman(human);
            Global.getCache().deleteDirtyData(human);

            deferred.resolve(String.format(getString(R.string.deleteHuman_local_success), human.name));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();

            deferred.reject(String.format(getString(R.string.deleteHuman_local_failure), human.name, ex.getMessage()));
        }

        deferred.promise()
        .done(new DoneCallback<String>() {
            @Override
            public void onDone(String result) {
                Global.getCache().createRunningLog(
                        RunningLog.CATEGORY_DATA_MAINTAIN,
                        getString(R.string.runninglog_event_deleteHuman_local),
                        Global.getLoginedUserName(),
                        result,
                        getString(R.string.result_success),
                        true);

                clearFiles(human, cleanup);
            }
        })
        .fail(new FailCallback<String>() {
            @Override
            public void onFail(String result) {
                Global.getCache().createRunningLog(
                        RunningLog.CATEGORY_DATA_MAINTAIN,
                        getString(R.string.runninglog_event_deleteHuman_local),
                        Global.getLoginedUserName(),
                        result,
                        getString(R.string.result_failure),
                        false);
            }
        });

        return deferred;
    }

    public Deferred deleteHuman_remote(final Human human, final boolean cleanup) {

        final Deferred deferred = new DeferredObject();

        List<Promise> promises = new ArrayList<Promise>();

        for(Nfc nfc:human.getNfcs())
        {
            promises.add(new WsNfcD(context, nfc).execute().promise());
        }

        for(Fingerprint fingerprint:human.getFingerprints())
        {
            promises.add(new WsFingerprintD(context, human, fingerprint).execute().promise());
        }

        promises.add(new WsHumanD(context, human).execute().promise());

        DeferredManager dm = new SequenceDeferredManager();

        dm.when(Converter.toArray(promises)).done(new DoneCallback<MultipleResults>() {
            @Override
            public void onDone(MultipleResults multipleResults) {
                String errors = Converter.getErrors(multipleResults);
                if(!TextUtils.isEmpty(errors))
                {
                    deferred.reject(String.format(getString(R.string.deleteHuman_remote_failure), human.name, errors));
                }
                else
                {
                    deferred.resolve(String.format(getString(R.string.deleteHuman_remote_success), human.name));
                }
            }
        })
        .fail(new FailCallback<OneReject>() {
            @Override
            public void onFail(OneReject result) {
                String errorMessage = ((RequestError) result.getReject()).getMessage();
                deferred.reject(String.format(getString(R.string.deleteHuman_remote_failure), human.name, errorMessage));
            }
        });

        deferred.promise()
        .done(new DoneCallback<String>() {
            @Override
            public void onDone(String result) {


                Global.getCache().createRunningLog(
                        RunningLog.CATEGORY_DATA_MAINTAIN,
                        getString(R.string.runninglog_event_deleteHuman_remote),
                        Global.getLoginedUserName(),
                        result,
                        getString(R.string.result_success),
                        true);

                clearFiles(human, cleanup);
            }
        })
        .fail(new FailCallback<String>() {
            @Override
            public void onFail(String result) {
                Global.getCache().createRunningLog(
                        RunningLog.CATEGORY_DATA_MAINTAIN,
                        getString(R.string.runninglog_event_deleteHuman_remote),
                        Global.getLoginedUserName(),
                        result,
                        getString(R.string.result_failure),
                        false);
            }
        });

        return deferred;
    }

    /**
     * 移除使用者在手機的相關檔案
     *  20160427 Norman, 不確定刪除是否正確
     */
    private void clearFiles(Human human, boolean cleanup)
    {
        if(!cleanup)return;

        List<String> filepaths = new ArrayList<String>();

        filepaths.addAll(human.getDatPaths());

        for(Fingerprint fingerprint:human.getFingerprints())
        {
            for(int scanTime=0; scanTime< UserManagerFragment4.maxScanTimes; scanTime++)
            {
                String filepath = FileManager.getBmpPath(human, Converter.englishToFingerBtnId(fingerprint.which), scanTime);

                if(FileManager.exists(filepath))
                    filepaths.add(filepath);
            }
        }

        for(String filepath:filepaths)
            FileManager.delete(filepath);
    }

    /**
     * 檢查是否有任何使用者可登入(有指紋資料)這台手機，並且是管理者
     */
    public boolean hasManagerCanLogin(List<Human> humanList)
    {
        for(Human human:humanList)
        {
            if(human.hasDatFile() && human.isManager())
                return true;
        }

        return false;
    }

    public boolean checkManagerCanLogin(List<Human> humanList, SweetAlertDialog.OnSweetClickListener listener)
    {
        boolean hasManagerCanLogin = hasManagerCanLogin(humanList);
        if(!hasManagerCanLogin)
        {
            SweetAlertDialog dialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText(getString(R.string.warn_title_no_manager_data))
                    .setContentText(getString(R.string.warn_content_no_manager_data))
                    .setConfirmText(context.getString(R.string.ok))
                    .showCancelButton(false)
                    .showContentText(true);

            if(listener != null)
            {
                dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismissWithAnimation();
                        goToCreateUser();
                    }
                });
            }

            dialog.show();
        }

        return hasManagerCanLogin;
    }

    public boolean checkManagerCanLogin(List<Human> humanList)
    {
        boolean createManagerIfNotExist = Global.getConfig().getCreateManagerIfNotExist();

        return checkManagerCanLogin(humanList, createManagerIfNotExist
                ? new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog)
                    {
                        sweetAlertDialog.dismissWithAnimation();
                        goToCreateUser();
                    }
                }
                : null
        );
    }

    public void goToCreateUser()
    {
        boolean hasManagerCanLogin = hasManagerCanLogin(Global.getHumanList());
        Human human = Human.CreateNewUser(!hasManagerCanLogin ? Human.INSTALLER : "");
        Global.setEditedHuman(human);
        Global.setEditMode(EditMode.INSERT);
        if(context instanceof TransitionListener)
        {
            ((TransitionListener)context).showFragment(new UserManagerFragment2(),"UserManagerFragment2");
        }
    }

    public void goToEditUser(Human human)
    {
        human.saveOriginalNfcs();
        human.saveOriginalFingerprints();
        Global.setEditedHuman(human);
        Global.setEditMode(EditMode.READONLY);
        if(context instanceof TransitionListener)
        {
            ((TransitionListener)context).showFragment(new UserManagerFragment2(),"UserManagerFragment2");
        }
    }
}
