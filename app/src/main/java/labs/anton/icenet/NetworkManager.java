package labs.anton.icenet;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.startek.biota.app.global.Global;
import com.startek.biota.app.models.RunningLog;

import org.jdeferred.Deferred;
import org.jdeferred.impl.DeferredObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by anton on 10/9/14.
 */
public final class NetworkManager {
    private static final String TAG = NetworkManager.class.getSimpleName();

    public enum RESULT {
        JSONOBJECT,
        JSONARRAY,
        STRING
    }

    private final String baseUrl;
    private final labs.anton.icenet.NetworkHelper networkHelper;
    private final String pathUrl;
    private final int method;
    private final TypeToken<?> classTarget;
    private final RESULT resultType;
    private final HashMap<String, Object> bodyRequest;
    private final HashMap<String, String> headers;
    private final boolean debugEnabled;
    private final boolean debugEnabledInEmailLog;

    public NetworkManager(Builder builder) {
        this.baseUrl = builder.baseUrl;
        this.networkHelper = NetworkHelper.getInstance(builder.context);
        this.pathUrl = builder.pathUrl;
        this.method = builder.method;
        this.classTarget = builder.targetType;
        this.resultType = builder.resultType;
        this.bodyRequest = builder.bodyRequest;
        this.headers = builder.headers;
        this.debugEnabled = true;
        this.debugEnabledInEmailLog = false;
    }

    private String getUrlConnection(String pathUrl) {
        StringBuilder builder = new StringBuilder();
        builder.append(baseUrl)
                .append(pathUrl);

        return builder.toString();
    }

    private JSONObject createBodyRequest(HashMap<String, Object> bodyRequest) {
        return bodyRequest == null ? null : new JSONObject(bodyRequest);
    }

    private void fromJsonObject(final HashMap<String, String> headers, HashMap<String, Object> bodyRequest, String requestTag, final RequestCallback requestCallback) {

        if(debugEnabled)
        {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("method", methodToString(method));
                jsonObject.put("url", getUrlConnection(pathUrl));
                jsonObject.put("body", createBodyRequest(bodyRequest));
                Log.d(TAG, String.format("onRequest: %s", toPrettyFormat(jsonObject.toString())));

                if(debugEnabledInEmailLog)
                {
                    Global.getCache().createEmailLog(
                            RunningLog.CATEGORY_SYNC_SERVER,
                            "WebService.onRequest",
                            Global.getLoginedUserName(),
                            toPrettyFormat(jsonObject.toString()),
                            "success",
                            true);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        JSONObject jsonRequest = createBodyRequest(bodyRequest);

        JsonObjectRequest request = new JsonObjectRequest(method, getUrlConnection(pathUrl), createBodyRequest(bodyRequest), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                try
                {
                    if(debugEnabled)
                    {
                        Log.d(TAG, String.format("onResponse: %s", toPrettyFormat(jsonObject.toString())));

                        if(debugEnabledInEmailLog)
                        {
                            Global.getCache().createEmailLog(
                                    RunningLog.CATEGORY_SYNC_SERVER,
                                    "WebService.onResponse",
                                    Global.getLoginedUserName(),
                                    toPrettyFormat(jsonObject.toString()),
                                    "success",
                                    true);
                        }
                    }

                    Object t = new Gson().fromJson(jsonObject.toString(), classTarget.getType());
                    if (requestCallback != null)
                        requestCallback.onRequestSuccess(t);
                }
                catch (Exception ex)
                {
                    // 20160402 Norman, 避免 Server 傳送內容與這邊設定的對應類別無法對應，導致程式出錯
                    VolleyError volleyError = new VolleyError(ex);
                    if (requestCallback != null) {
                        requestCallback.onRequestError(new RequestError(volleyError));
                    }
                }

            }
        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                if (requestCallback != null) {
//                    NetworkResponse response = error.networkResponse;
//                    if (response != null)
//                        requestCallback.onRequestError(new RequestError(response));
//                }
//            }
            // 20160318 Norman, 處理伺服器沒有開啟的問題
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (requestCallback != null) {
                    requestCallback.onRequestError(new RequestError(volleyError));
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers != null ? headers : super.getHeaders();
            }
        };

        networkHelper.addToRequestQueue(request, requestTag);
    }

    private void fromJsonArray(final Map<String, String> headers, String requestTag, final RequestCallback requestCallback) {
        JsonArrayRequest request = new JsonArrayRequest(getUrlConnection(pathUrl), new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray jsonArray) {
                Object t = new Gson().fromJson(jsonArray.toString(), classTarget.getType());
                if (requestCallback != null)
                    requestCallback.onRequestSuccess(t);
            }
        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                if (requestCallback != null) {
//                    NetworkResponse response = error.networkResponse;
//                    if (response != null)
//                        requestCallback.onRequestError(new RequestError(response));
//                }
//            }
            // 20160318 Norman, 處理伺服器沒有開啟的問題
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (requestCallback != null) {
                    requestCallback.onRequestError(new RequestError(volleyError));
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers != null ? headers : super.getHeaders();
            }
        };

        networkHelper.addToRequestQueue(request, requestTag);
    }

    private void fromString(final Map<String, String> headers, String requestTag, final RequestCallback requestCallback) {
        StringRequest request = new StringRequest(getUrlConnection(pathUrl), new Response.Listener<String>(){
            @Override
            public void onResponse(String s) {
                requestCallback.onRequestSuccess(s);
            }
        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                if (requestCallback != null) {
//                    NetworkResponse response = error.networkResponse;
//                    if (response != null)
//                        requestCallback.onRequestError(new RequestError(response));
//                }
//            }
            // 20160318 Norman, 處理伺服器沒有開啟的問題
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (requestCallback != null) {
                    requestCallback.onRequestError(new RequestError(volleyError));
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers != null ? headers : super.getHeaders();
            }
        };

        networkHelper.addToRequestQueue(request, requestTag);
    }

    public Deferred defer(String requestTag)
    {
        final Deferred deferred = new DeferredObject();

        execute(requestTag, new RequestCallback() {
            @Override
            public void onRequestSuccess(Object postResponse) {

                deferred.resolve(postResponse);
            }

            @Override
            public void onRequestError(RequestError error) {

                deferred.reject(error);
            }
        });

        return deferred;
    }

    public void execute(String requestTag, RequestCallback callback) {
        if (resultType == null) {
            throw new IllegalArgumentException("result type must not be null.");
        }

        if (classTarget == null) {
            throw new IllegalArgumentException("class target must not be null.");
        }

        if (pathUrl == null) {
            throw new IllegalArgumentException("path url must not be null.");
        }

        switch (resultType) {
            case JSONARRAY:
                fromJsonArray(headers, requestTag, callback);
                break;
            case JSONOBJECT:
                if (method == Request.Method.POST)
                    if (bodyRequest == null)
                        throw new IllegalArgumentException("body request must not be null.");

                fromJsonObject(headers, bodyRequest, requestTag, callback);
                break;
            case STRING:
                fromString(headers, requestTag, callback);
                break;
            default:
                throw new IllegalArgumentException("response type not found");
        }
    }

    public static class Builder implements INetworkManagerBuilder {
        private String baseUrl;
        private Context context;
        private String pathUrl;
        private int method;
        private RESULT resultType;
        private TypeToken<?> targetType;
        private HashMap<String, Object> bodyRequest;
        private HashMap<String, String> headers;

        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        public Builder setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder setMethod(int method) {
            this.method = method;
            return this;
        }

        public Builder setBodyRequest(@NonNull HashMap<String, Object> bodyRequest) {
            this.bodyRequest = bodyRequest;
            return this;
        }

        public Builder setHeaders(@NonNull HashMap<String, String> headers) {
            this.headers = headers;
            return this;
        }

        @Override
        public INetworkManagerBuilder pathUrl(@NonNull String pathUrl) {
            this.pathUrl = pathUrl;
            return this;
        }

        @Override
        public INetworkManagerBuilder fromJsonObject() {
            this.resultType = RESULT.JSONOBJECT;
            return this;
        }

        @Override
        public INetworkManagerBuilder fromJsonArray() {
            this.resultType = RESULT.JSONARRAY;
            return this;
        }

        @Override
        public NetworkManager fromString() {
            this.resultType = RESULT.STRING;
            this.targetType = TypeToken.get(String.class);
            return new NetworkManager(this);
        }

        @Override
        public NetworkManager mappingInto(@NonNull Class classTarget) {
            this.targetType = TypeToken.get(classTarget);
            return new NetworkManager(this);
        }

        @Override
        public NetworkManager mappingInto(@NonNull TypeToken typeToken) {
            this.targetType = typeToken;
            return new NetworkManager(this);
        }
    }

    public static interface INetworkManagerBuilder {
        /**
         * @param pathUrl
         * @return
         */
        public INetworkManagerBuilder pathUrl(@NonNull String pathUrl);

        public INetworkManagerBuilder fromJsonObject();

        public INetworkManagerBuilder fromJsonArray();

        public NetworkManager fromString();

        public NetworkManager mappingInto(@NonNull Class classTarget);

        public NetworkManager mappingInto(@NonNull TypeToken typeToken);
    }

    //----------------------------------------
    // 20160610 Norman, 用來除錯
    //----------------------------------------

    private String methodToString(int method)
    {
        switch (method)
        {
            case Request.Method.DEPRECATED_GET_OR_POST: return "DEPRECATED_GET_OR_POST";

            case Request.Method.GET: return "GET";
            case Request.Method.POST: return "POST";
            case Request.Method.PUT: return "PUT";
            case Request.Method.DELETE: return "DELETE";
            case Request.Method.HEAD: return "HEAD";

            case Request.Method.OPTIONS: return "OPTIONS";
            case Request.Method.TRACE: return "TRACE";
            case Request.Method.PATCH: return "PATCH";
        }

        return String.format("UNKNOWN(%d)", method);
    }

    private String toPrettyFormat(String jsonString)
    {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(jsonString).getAsJsonObject();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = gson.toJson(json);

        return prettyJson;
    }
}
