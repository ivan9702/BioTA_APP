package com.startek.biota.app.network.webservices;

import java.util.List;

/**
 * WebService必定有回傳參數
 */
public class CommonResponse {

    public Result result;
    public List<String> resp;

    public class Result
    {
        public boolean success;
        public String message;
    }
}
