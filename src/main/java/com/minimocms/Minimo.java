package com.minimocms;

import com.minimocms.data.DataStoreInterface;
import com.minimocms.data.mongodb.MongoDataStoreImpl;
import com.minimocms.type.GenericContent;
import com.minimocms.type.MoImageItem;
import com.minimocms.type.MoPage;
import com.minimocms.type.MoUser;
import com.minimocms.utils.FormUtil;
import com.minimocms.web.Routes;
import spark.Request;
import spark.utils.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Minimo {

    public DataStoreInterface store;
    String siteName;
    static boolean restAuthentication=true;


    public static void main(String args[]){
        if(args.length==0){
            System.err.println("Error, please specify site name as the first argument");
            System.err.println("E.g. java -jar minimocms.jar MySite");
        } else {
            String siteName = args[0];
            Minimo.init(siteName, new MongoDataStoreImpl(siteName));
        }
    }


    public static void restAuthentication(boolean auth){
        restAuthentication=auth;
    }

    public static boolean restAuthentication(){
        return restAuthentication;
    }

    static Minimo minimo = new Minimo();
    public static Minimo instance(){
        return minimo;
    }

    public static MoPage page(String name){
        return store().page(name);
    }

    public static MoPage page(Request req,String name){
        return store().page(req, name);
    }

    public static void persistUser(MoUser user){
        store().persistUser(user);
    }

    public static void persistPages(){
        store().persistPages();
    }

    public static void persistPages(Request req){
        store().persistPages(req);
    }

    public static Collection<MoPage> pages(){
        return store().pages().values();
    }
    public static Collection<MoPage> pages(Request req){
        return store().pages(req).values();
    }

    public static MoUser user(String username){
        return store().user(username);
    }
    public static List<MoUser> users(){
        return store().users();
    }

    public static byte[] file(String id){
        return store().file(id);
    }

    public static void rollbackPages(){
        store().rollbackPages();
    }

    public static void rollbackPages(Request req){
        store().rollbackPages(req);
    }
//
//    public static void init(String siteName){
//        init(siteName,new MongoStore(siteName));
//    }

    public static void init(String siteName, DataStoreInterface store){
        instance().store = store;
        instance().siteName=siteName;
        instance().rollbackPages();
        new Routes().init();
    }

    public static void initClientOnly(String siteName, DataStoreInterface store){
        instance().store = store;
        instance().siteName=siteName;
        instance().rollbackPages();
    }

    public static GenericContent findById(MoPage page, List<String> ids){
        GenericContent c = page.getChildById(ids.get(0));
        ids.remove(0);
        for (String id : ids) {
            if (c.existsChildById(id))
                c = c.getChildById(id);
            else {
                throw new IllegalStateException("Something went wrong in creating data, stuck at id:" + c.id());
            }
        }
        return c;
    }

    public static GenericContent findById(MoPage page, String path){
        List<String> ids = new ArrayList<>(Arrays.asList(path.substring(1).split("/")));
        return findById(page,ids);
    }

    public static boolean save(String name, Request req) {
        try{
            MoPage page = page(req,name);

            FormUtil form = new FormUtil(req);
            form.parseFormInputs();

//        System.out.println("\n\nListing qparams");
//        form.queryParams().forEach(p -> {
//            System.out.println("param:" + p + "=" + form.queryParam(p));
//        });
//        System.out.println("Listing files");
//
//        form.files().forEach(p -> {
//            FileItem f = form.file(p);
//            System.out.println("file:" +p+" "+ f.getFieldName() + " " + f.getContentType() + " " + f.getName());
//        });

            form.queryParams().stream().filter(p->p.startsWith("/")).forEach(p -> {
                String value = form.queryParam(p);
                GenericContent c = findById(page,p);
                c.setValue(value);
            });

            form.queryParams().stream().filter(p->p.startsWith("deleted:")).forEach(p->{
                String value = form.queryParam(p);
                if(value.equals("true")) {
                    p = p.substring("deleted:".length());
                    List<String> ids = new ArrayList<>(Arrays.asList(p.substring(1).split("/")));
                    String toDelete = ids.get(ids.size()-1);
                    ids.remove(ids.size()-1);
                    GenericContent c = findById(page,ids);
                    c.removeChildById(toDelete);
                }
            });

            form.queryParams().stream().filter(p->p.startsWith("img:")).forEach(p->{
                GenericContent c = findById(page,p.substring("img:".length()));
                MoImageItem f = (MoImageItem)c;
                if(form.files().contains(p.substring("img:".length()))) {
                    try {
                        f.file(IOUtils.toByteArray(form.file(p.substring("img:".length())).getInputStream()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            form.queryParams().stream().filter(p->p.startsWith("name:")).forEach(p -> {
                GenericContent c = findById(page,p.substring("name:".length()));
                String value = form.queryParam(p);
                c.name(value);
            });

            String url = form.queryParam("url");
            if(url!=null&&url.equals("")==false)
                page.url(url);

            store().persistPages(req);
            return true;
        } catch(Exception e){
            e.printStackTrace();
            return false;
        }

    }


    public static DataStoreInterface store() {
        return instance().store;
    }

    public static List<String> files(Request req) {
        return store().fileIds();
    }
}
