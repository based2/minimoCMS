package com.minimocms.test;

import com.google.gson.Gson;
import com.minimocms.Minimo;
import com.minimocms.data.mongodb.MongoDataStoreImpl;
import com.minimocms.test.web.TestData;
import com.minimocms.test.web.TestStructure;

import static com.minimocms.Minimo.pages;

/**
 * Created by MattUpstairs on 27/12/2014.
 */
public class WebTest {

    public static void main(String args[]){
        Minimo.init("web-test",new MongoDataStoreImpl("web-test"));
        TestStructure.create();
        TestData.create();
        Minimo.persist();
        System.out.println(new Gson().toJson(pages()));

    }


}
