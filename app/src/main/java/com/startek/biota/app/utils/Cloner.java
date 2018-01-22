package com.startek.biota.app.utils;

import com.google.gson.Gson;

/**
 * Created by skt90u on 2016/4/17.
 */
public class Cloner
{
    public static <T> T deepClone(T object)
    {
        if(object == null)return null;

        String jsonString = new Gson().toJson(object);

        T t = (T) new Gson().fromJson(jsonString, object.getClass());

        return t;
    }
}
