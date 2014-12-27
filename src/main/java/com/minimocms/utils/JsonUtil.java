package com.minimocms.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.minimocms.type.GenericContent;
import com.minimocms.type.GenericContentSerializer;

/**
 * Created by MattUpstairs on 27/12/2014.
 */
public class JsonUtil {
    static Gson gson;
    static {
        GsonBuilder b = new GsonBuilder();
        b.registerTypeAdapter(GenericContent.class,new GenericContentSerializer());

        gson = b.create();
    }

    public static String toJson(Object o) {
        return gson.toJson(o);
    }


    public static Gson gson(){
        return gson;
    }
}