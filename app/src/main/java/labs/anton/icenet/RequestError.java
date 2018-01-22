package labs.anton.icenet;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.startek.biota.app.utils.Converter;

import org.apache.http.util.ExceptionUtils;

import java.util.Map;

/**
 * Created by anton on 10/15/14.
 */
public final class RequestError {

    // https://en.wikipedia.org/wiki/List_of_HTTP_status_codes
    // 20160318 Norman, 處理伺服器沒有開啟的問題
    public final static int REQUEST_RESPONSE_EXCEPTION = -1;

    public final static int REQUEST_RESPONSE_OK = 200;
    public final static int REQUEST_RESPONSE_CREATED = 201;
    public final static int REQUEST_RESPONSE_ACCEPTED = 202;
    public final static int REQUEST_RESPONSE_NO_CONTENT = 204;
    public final static int REQUEST_RESPONSE_BAD_REQUEST = 400;
    public final static int REQUEST_RESPONSE_UNAUTHORIZED = 401;
    public final static int REQUEST_RESPONSE_FORBIDDEN = 403;
    public final static int REQUEST_RESPONSE_PAYMENT_REQUIRED = 402;
    public final static int REQUEST_RESPONSE_NOT_FOUND = 404;
    public final static int REQUEST_RESPONSE_GONE = 410;
    public final static int REQUEST_RESPONSE_UNPROCESSABLE_ENTITY = 422;
    public final static int REQUEST_RESPONSE_INTERNAL_SERVER_ERROR = 500;
    public final static int REQUEST_RESPONSE_SERVICE_UNAVAILABLE = 503;
    public final static int REQUEST_RESPONSE_MULTIPLE_DEVICE = 429;
    public final static int REQUEST_RESPONSE_NOT_PERMITTED = 301;
    public final static int REQUEST_RESPONSE_RESET_PASSWORD_SUCCESS = 204;

    public String getMessage()
    {
        switch (this.errorCode)
        {
            case REQUEST_RESPONSE_OK: return "REQUEST_RESPONSE_OK";
            case REQUEST_RESPONSE_CREATED: return "REQUEST_RESPONSE_CREATED";
            case REQUEST_RESPONSE_ACCEPTED: return "REQUEST_RESPONSE_ACCEPTED";
            case REQUEST_RESPONSE_NO_CONTENT: return "REQUEST_RESPONSE_NO_CONTENT";
            case REQUEST_RESPONSE_BAD_REQUEST: return "REQUEST_RESPONSE_BAD_REQUEST";

            case REQUEST_RESPONSE_UNAUTHORIZED: return "REQUEST_RESPONSE_UNAUTHORIZED";
            case REQUEST_RESPONSE_FORBIDDEN: return "REQUEST_RESPONSE_FORBIDDEN";
            case REQUEST_RESPONSE_PAYMENT_REQUIRED: return "REQUEST_RESPONSE_PAYMENT_REQUIRED";
            case REQUEST_RESPONSE_NOT_FOUND: return "REQUEST_RESPONSE_NOT_FOUND";
            case REQUEST_RESPONSE_GONE: return "REQUEST_RESPONSE_GONE";

            case REQUEST_RESPONSE_UNPROCESSABLE_ENTITY: return "REQUEST_RESPONSE_UNPROCESSABLE_ENTITY";
            case REQUEST_RESPONSE_INTERNAL_SERVER_ERROR: return "REQUEST_RESPONSE_INTERNAL_SERVER_ERROR";
            case REQUEST_RESPONSE_SERVICE_UNAVAILABLE: return "REQUEST_RESPONSE_SERVICE_UNAVAILABLE";
            case REQUEST_RESPONSE_MULTIPLE_DEVICE: return "REQUEST_RESPONSE_MULTIPLE_DEVICE";
            case REQUEST_RESPONSE_NOT_PERMITTED: return "REQUEST_RESPONSE_NOT_PERMITTED";

            //case REQUEST_RESPONSE_RESET_PASSWORD_SUCCESS: return "REQUEST_RESPONSE_RESET_PASSWORD_SUCCESS";

            case REQUEST_RESPONSE_EXCEPTION:
            default:
            {
                return Converter.getMessage(volleyError);
            }
        }

    }

    final int errorCode;
    final Map<String,String> headers;
    private VolleyError volleyError;

//    RequestError(NetworkResponse response) {
//        this.errorCode = response.statusCode;
//        this.headers = response.headers;
//    }

    // 20160318 Norman, 處理伺服器沒有開啟的問題
    public RequestError(VolleyError volleyError) {

        this.volleyError = volleyError;

        NetworkResponse response = this.volleyError.networkResponse;
        if (response != null)
        {
            this.errorCode = response.statusCode;
            this.headers = response.headers;
        }
        else {
            this.errorCode = REQUEST_RESPONSE_EXCEPTION;
            this.headers = null;
        }
    }

    // 20160318 Norman, 處理伺服器沒有開啟的問題
    // if errorCode is REQUEST_RESPONSE_EXCEPTION
    // use getError to know what happen
    public VolleyError getError() { return volleyError;}

    public int getErrorCode() {
        return errorCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }


}
