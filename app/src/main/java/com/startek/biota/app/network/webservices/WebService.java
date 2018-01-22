package com.startek.biota.app.network.webservices;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.jdeferred.Deferred;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import labs.anton.icenet.Body;
import labs.anton.icenet.IceNet;
import labs.anton.icenet.RequestError;

import com.android.volley.VolleyError;
import com.startek.biota.app.R;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.utils.Converter;
import com.startek.biota.app.utils.DialogHelper;
import com.startek.biota.app.utils.MobileStatus;

/**
 * api document
 * https://docs.google.com/document/d/1g-FNhcmadihEaQUkK7V2ZMmS3UakK9f9PRzmNW0PMSU/edit?ts=5739318f&pref=2&pli=1#heading=h.5on6m3teq2hi
 */
public abstract class WebService {

    private static final String TAG = "WebService";

    private String url;
    private Class responseClass;

    private Context context;
    private int errorResId;
    private boolean hideApiError;

    public WebService(String url, Class responseClass, Context context, int errorResId)
    {
        this.url = url;
        this.responseClass = responseClass;
        this.context = context;
        this.errorResId = errorResId;
        this.hideApiError = Global.getConfig().hideApiError();
    }

    protected abstract void buildBody(Body.Builder builder);

    private Body getRequestBody() {

        Body.Builder builder = new Body.Builder();

        // 必要參數

        builder.add("device_id", MobileStatus.getDeviceId()); // 設備唯一碼, UUID,如390EEBE3­F1EB­4FD3­B1FA­D25365AFEDDB
        builder.add("push_token", MobileStatus.getPushToken()); // 推播唯一碼
        builder.add("device_type", MobileStatus.getDeviceType()); // 設備型態 = [ios | android]


        buildBody(builder);

        Body body = new Body(builder);

        return body;
    }

    private String getRequestTag()
    {
        String dateString = new SimpleDateFormat(Converter.DateTimeFormat.YYYYMMddHHmmssSSSZ).format(new Date());
        String requestTag = getClass().getSimpleName() + dateString;
        return requestTag;
    }

    private void alert(String message)
    {
        Log.e(TAG, message);

        if(context == null) return;

        if(context instanceof Activity)
        {
            Activity activity = (Activity)context;
            DialogHelper.alert(activity, message);
        }
    }

    protected String getString(int resId)
    {
        if(context == null) return "";

        return context.getString(resId);
    }

    public Deferred execute()
    {
        boolean networkEnabled = MobileStatus.isNetworkEnabled();
        if(!networkEnabled)
        {
            String errmsg = String.format(getString(errorResId), getString(R.string.error_network_unavailable));
            alert(errmsg);

            Deferred deferred = new DeferredObject();
            RequestError error = new RequestError(new VolleyError(new Exception(errmsg)));
            deferred.reject(error);

            return deferred;
        }

        Body body = getRequestBody();
        final String tag = getRequestTag();

        Deferred deferred = IceNet.connect()
                .createRequest()
                .post(body)
                .pathUrl(url)
                .fromJsonObject()
                .mappingInto(responseClass)
                .defer(tag);

        Promise promise = deferred.promise();
        promise.done(new DoneCallback() {
            public void onDone(Object result) {
                CommonResponse res = (CommonResponse)result;
                if(!res.result.success)
                {
                    String errmsg = String.format(getString(errorResId), res.result.message);
                    Log.e(tag, errmsg);

                    if(!hideApiError)
                        alert(errmsg);
                }
            }
        }).fail(new FailCallback() {
            public void onFail(Object rejection) {
                RequestError requestError = (RequestError)rejection;
                String errmsg = String.format(getString(errorResId), requestError.getMessage());
                Log.e(tag, errmsg);

                if(!hideApiError)
                    alert(errmsg);
            }
        });

        return deferred;
    }
}
