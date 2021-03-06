package com.minimocms.test;

import com.google.gson.Gson;
import com.minimocms.Minimo;
import com.minimocms.data.mongodb.MongoDataStoreImpl;
import com.minimocms.test.web.TestData;

import static com.minimocms.Minimo.pages;

public class WebTest {

    public static void main(String args[]){
        Minimo.init("web-test",new MongoDataStoreImpl("web-test"));
        if(pages().size()==0){
            TestData.create();
        }
        Minimo.persistPages();
        System.out.println(new Gson().toJson(pages()));

    }


}
