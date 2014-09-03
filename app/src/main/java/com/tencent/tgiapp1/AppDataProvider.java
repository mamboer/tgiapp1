package com.tencent.tgiapp1;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.tencent.tgiapp1.bean.SearchList;
import com.tencent.tgiapp1.common.EncryptUtils;
import com.tencent.tgiapp1.common.HttpUtil;
import com.tencent.tgiapp1.common.StringUtils;
import com.tencent.tgiapp1.common.UIHelper;
import com.tencent.tgiapp1.entity.AppData;
import com.tencent.tgiapp1.entity.Article;
import com.tencent.tgiapp1.entity.ArticleList;
import com.tencent.tgiapp1.entity.ChannelGroup;
import com.tencent.tgiapp1.entity.ChannelItem;
import com.tencent.tgiapp1.entity.MiscData;
import com.tencent.tgiapp1.entity.UserFavArticleList;
import com.tencent.tgiapp1.entity.UserRemindArticleList;

import org.apache.http.util.EncodingUtils;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by levin on 6/18/14.
 */
public class AppDataProvider {

    //TODO:移到配置文件中
    public static class URL{
        final public static String ARTICLE = "http://ttxd.qq.com/webplat/info/news_version3/7367/8248/8277/8278/m6746/list_1.shtml";//新闻数据
        final public static String NOTICE = "http://ttxd.qq.com/webplat/info/news_version3/7367/8248/8277/8279/m6748/list_1.shtml";//公告数据
        final public static String MISC = "http://ttxd.qq.com/webplat/info/news_version3/7367/8248/8277/8280/m6749/list_1.shtml";//杂项数据
        final public static String SLIDE = "http://ttxd.qq.com/webplat/info/news_version3/7367/8248/8277/8340/m6810/list_1.shtml";//图片轮播
        final public static String DEFAULT_SLIDE_IMG="http://ossweb-img.qq.com/upload/webplat/info/ttxd/201408/1408451188_1436653066_17125_imageAddr.jpg";
        final public static String MANUAL ="http://ttxd.qq.com/webplat/info/news_version3/7367/8248/8277/8339/8341/m6808/list_1.shtml";//玩法攻略
        final public static String TESTING="http://ttxd.qq.com/webplat/info/news_version3/7367/8248/8277/8339/8342/m6808/list_1.shtml";//评测
        final public static String EXP = "http://ttxd.qq.com/webplat/info/news_version3/7367/8248/8277/8339/8343/m6808/list_1.shtml";//高玩心得
    }

    public static class CONSTS{
        final public static String FAV_ChANNELGROUP = "FavChannelGroup";
        final public static String FAV_ARTICLE = "FavArticleList";
        final public static String REMIND_ARTICLE = "REMIND_ARTICLELIST";
        final public static String ERROR="ERROR";
        final public static String ENCODING_UTF8="UTF-8";
    }

    public static String assertUrl(AppContext ct,String url){
        if(url.indexOf("http://")==0 || url.indexOf("https://")==0){
            return url;
        }
        return (ct.getString(R.string.app_datahost)+url);
    }

    /**
     * 从assets目录获取指定文件
     * @param context
     * @param filePath
     * @return
     */
    public static String getAssetFile(AppContext context,String filePath,String encoding){
        String result = "";

        try {

            InputStream in = context.getResources().getAssets().open(filePath);
            //获取文件的字节数
            int lenght = in.available();
            //创建byte数组
            byte[]  buffer = new byte[lenght];
            //将文件中的数据读到byte数组中
            in.read(buffer);
            result = EncodingUtils.getString(buffer, encoding);

            in.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String getAssetFileNameByUrl(String url){
        String r = "data/";
        if(url.equalsIgnoreCase(URL.ARTICLE)){
            //新闻
            r+="article";
        }else if(url.equalsIgnoreCase(URL.NOTICE)){
            //公告
            r+="notice";
        }else if(url.equalsIgnoreCase(URL.EXP)){
            r+="exp";
        }else if(url.equalsIgnoreCase(URL.MANUAL)){
            r+="manual";
        }else if(url.equalsIgnoreCase(URL.MISC)){
            r+="misc";
        }else if(url.equalsIgnoreCase(URL.SLIDE)){
            r+="slide";
        }else if(url.equalsIgnoreCase(URL.TESTING)){
            r+="testing";
        }else{
            r=null;
        }
        if(null!=r){
            r+=".shtml";
        }

        return r;
    }

    public static String getRemoteData(AppContext appContext,String url,boolean isRefresh) throws AppException{
        String key = EncryptUtils.encodeMD5(url);

        try{
            String data = "";
            if(appContext.isNetworkConnected() && (!appContext.isReadDataCache(key) || isRefresh)) {
                data = HttpUtil.get(url);
                if(data.lastIndexOf(CONSTS.ERROR)>=0){
                    //http请求时错误
                    throw AppException.network(new Exception(data));
                }else{
                    //缓存
                    appContext.saveObject(data,key);
                }

            } else {
                data = (String)appContext.readObject(key);
                //没有缓存也没有网络，从assets目录中读取
                if(data == null){
                    url = getAssetFileNameByUrl(url);
                    if(null!=url){
                        data = getAssetFile(appContext,url,CONSTS.ENCODING_UTF8);
                    }else{
                        data = "";
                    }
                }
            }
            return data;

        }catch(Exception e){
            if(e instanceof AppException)
                throw (AppException)e;
            throw AppException.network(e);
        }
    }

    /**
     * 获取指定新闻列表的数据
     * @param context
     * @param url
     * @param handler
     * @param isRefresh
     */
    public static void getArticleData(final AppContext context,final String url,final Handler handler,final boolean isRefresh){

        new Thread(){
            public void run() {

                ArticleList data = new ArticleList();
                Message msg = new Message();
                try {
                    data = getArticleDataSync(context,url,isRefresh);
                    msg.obj = data;

                } catch (Exception e) {
                    data = null;
                    e.printStackTrace();
                    msg.arg2 = 1;
                    msg.obj = e;
                }
                handler.sendMessage(msg);

            }
        }.start();

    }

    /**
     * 获取指定新闻列表的数据
     * @param context
     * @param url
     * @param isRefresh
     */
    public static ArticleList getArticleDataSync(final AppContext context,final String url,final boolean isRefresh)  throws Exception{

        String data0 = "";
        int flagIdx = 0;
        ArticleList data = new ArticleList();
        try {
            Gson gson = new Gson();
            data0 = getRemoteData(context,assertUrl(context,url),isRefresh);
            flagIdx = data0.lastIndexOf("<!--");
            if(flagIdx>0) {
                data0 = data0.substring(0, flagIdx);
                data = gson.fromJson(data0, ArticleList.class);
                data.getItems().remove(0);
            }else{
                throw new Exception("网络数据连失败或数据格式有误!");
            }

        } catch (AppException e) {
            data = null;
            e.printStackTrace();
            throw e;
        }
        return data;

    }

    /**
     * 获取杂项的数据
     * @param context
     * @param handler
     * @param isRefresh
     */
    public static void getMiscData(final AppContext context,final Handler handler,final boolean isRefresh){

        new Thread(){
            public void run() {
                String data0 = "";
                int flagIdx = 0;
                MiscData data = new MiscData();
                Bundle bundle = new Bundle();
                try {
                    Gson gson = new Gson();
                    data0 = getRemoteData(context,assertUrl(context,URL.MISC),isRefresh);
                    flagIdx = data0.lastIndexOf("<!--");
                    if(flagIdx>0) {
                        data0 = data0.substring(0, flagIdx);
                        data = gson.fromJson(data0, MiscData.class);
                        bundle.putInt("errCode",0);
                        bundle.putString("errMsg",null);
                    }else{
                        bundle.putInt("errCode",2);
                        bundle.putString("errMsg","网络数据连失败或数据格式有误！");
                    }

                } catch (AppException e) {
                    data = null;
                    e.printStackTrace();
                    bundle.putInt("errCode",1);
                    bundle.putString("errMsg",e.getMessage());
                }

                Message msg = new Message();
                bundle.putSerializable("data",data);

                msg.setData(bundle);
                handler.sendMessage(msg);

            }
        }.start();

    }

    /**
     * 同步的方式获取应用数据
     * @param context
     * @param isRefresh
     * @return
     */
    public static AppData getAppDataSync(final AppContext context,final boolean isRefresh)  throws Exception{
        AppData datas = new AppData();
        String data0 = "";
        int flagIdx = 0;
        final String uid = context.getLoginOpenId();
        try {
            Gson gson = new Gson();
            //杂项数据
            data0 = getRemoteData(context,URL.MISC,isRefresh);
            flagIdx = data0.lastIndexOf("<!--");
            if(flagIdx>0) {
                data0 = data0.substring(0, flagIdx);
                datas.setMisc(gson.fromJson(data0, MiscData.class));
            }

            //新闻数据
            data0 = getRemoteData(context,URL.ARTICLE,isRefresh);
            flagIdx = data0.lastIndexOf("<!--");
            if(flagIdx>0) {
                data0 = data0.substring(0, flagIdx);
                datas.setArticles(gson.fromJson(data0, ArticleList.class));
                //第一条数据是为了生成json数据伪造的不可用
                datas.getArticles().getItems().remove(0);
            }

            //公告数据
            data0 = getRemoteData(context,URL.NOTICE,isRefresh);
            flagIdx = data0.lastIndexOf("<!--");
            if(flagIdx>0) {
                data0 = data0.substring(0, flagIdx);
                datas.setNotices(gson.fromJson(data0, ArticleList.class));
                //第一条数据是为了生成json数据伪造的不可用
                datas.getNotices().getItems().remove(0);
            }

            //图片轮播数据
            data0 = getRemoteData(context,URL.SLIDE,isRefresh);
            flagIdx = data0.lastIndexOf("<!--");
            if(flagIdx>0) {
                data0 = data0.substring(0, flagIdx);
                datas.setSlides(gson.fromJson(data0, ArticleList.class));
                //第一条数据是为了生成json数据伪造的不可用
                datas.getSlides().getItems().remove(0);
            }

            //收藏数据
            UserFavArticleList favData = getFavArticlesSync(context,uid,false);
            datas.setFavArticles(favData);

            //提醒数据
            UserRemindArticleList reminderData = UserRemindArticleList.getRemindArticlesSync(context,uid,false);
            datas.setRemindArticles(reminderData);

        }catch (Exception e){
            e.printStackTrace();
            datas.setErrCode(1);
            datas.setErrMsg(e.getMessage());
            throw e;
        }

        return datas;
    }

    /**
     * 获取应用数据
     * @param context
     * @param handler
     * @throws AppException
     */
    public static void getAppData(final AppContext context,final Handler handler, final boolean isRefresh){

        new Thread(){
            public void run() {

                Bundle data = new Bundle();
                AppData datas = new AppData();

                try {
                    datas = getAppDataSync(context,isRefresh);

                    data.putInt("errCode",0);
                    data.putString("errMsg",null);

                }catch (Exception e){
                    datas.setErrCode(1);
                    datas.setErrMsg(e.getMessage());
                    data.putInt("errCode",1);
                    data.putString("errMsg",e.getMessage());
                }

                Message msg = new Message();
                data.putSerializable("data",datas);
                msg.setData(data);
                handler.sendMessage(msg);

            }
        }.start();
    }

    /**
     * 获取猜你喜欢频道数据
     * @return
     */
    public static ChannelGroup getFavChannelGroup(final AppContext context,boolean reset){
        String key = EncryptUtils.encodeMD5(CONSTS.FAV_ChANNELGROUP);
        ChannelGroup data = new ChannelGroup();
        try{


            ArrayList<ChannelGroup> items = null;
            ArrayList<ChannelItem> citems = new ArrayList<ChannelItem>();
            int max = context.getResources().getInteger(R.integer.channelfav_max);
            if((!context.isReadDataCache(key) || reset)) {
                items = context.getData().getMisc().getChannels();
                if(items.size()>0){
                    data = items.get(0);
                    max = Math.min(max, data.getItems().size());
                    citems.addAll(data.getItems().subList(0,max));
                    data.setItems(citems);
                    data.setGroup(context.getResources().getString(R.string.channelfav_name));
                    context.saveObject(data,key);
                }

            } else {
                data = (ChannelGroup)context.readObject(key);
                if(data == null)
                    data = new ChannelGroup();
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return data;
    }

    /**
     * 以同步的方式获取新闻收藏数据
     * @param context
     * @param reset
     * @param uid 用户滴openID
     * @return
     */
    public static UserFavArticleList getFavArticlesSync(final AppContext context,final String uid,final boolean reset) throws Exception{
        String uid1 = getOperationId(context,uid);
        String key = EncryptUtils.encodeMD5(uid1+"_"+CONSTS.FAV_ARTICLE);
        UserFavArticleList data = new UserFavArticleList(uid1);
        try{

            if(!(!context.isReadDataCache(key) || reset)) {
                data = (UserFavArticleList)context.readObject(key);
                if(data == null)
                    data = new UserFavArticleList(uid1);
            }


        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }

        return data;
    }
    /**
     * 获取收藏的新闻数据
     * @return
     */
    public static void getFavArticles(final AppContext context,final Handler handler,final String uid,final boolean reset){



        new Thread(){
            public void run() {

                Bundle bundle = new Bundle();
                UserFavArticleList data = null;
                try{

                    data = getFavArticlesSync(context,uid,reset);

                    //更新AppData中的数据
                    context.getData().setFavArticles(data);

                    bundle.putInt("errCode",0);
                    bundle.putString("errMsg",null);


                }catch(Exception e){
                    e.printStackTrace();
                    bundle.putInt("errCode",1);
                    bundle.putString("errMsg",e.getMessage());
                }

                Message msg = new Message();
                bundle.putSerializable("data",data);
                msg.setData(bundle);

                handler.sendMessage(msg);

            }
        }.start();

    }

    /**
     * 添加猜你喜欢频道 //TODO:逻辑优化，加点击计数器，然后根据计数排序
     * @param context
     * @param item
     * @param handler
     */
    public static void addFavChannelItem(final AppContext context,final ChannelItem item,final Handler handler){

        new Thread(){
            public void run() {
                String key = EncryptUtils.encodeMD5(CONSTS.FAV_ChANNELGROUP);
                ChannelGroup data = getFavChannelGroup(context,false);
                Bundle bundle = new Bundle();
                try{

                    int max = context.getResources().getInteger(R.integer.channelfav_max);
                    ArrayList<ChannelItem> citems0 = new ArrayList<ChannelItem>();
                    ArrayList<ChannelItem> citems1 = new ArrayList<ChannelItem>();
                    String uid0 = null;
                    String uid1 = item.getMD5()+item.getName();

                    //移除已经存在
                    for(ChannelItem item0:data.getItems()){
                        uid0 = item0.getMD5()+item0.getName();

                        if(uid0.equals(uid1)){
                            continue;
                        }
                        citems0.add(item0);
                    }

                    //添加新项
                    citems0.add(0,item);

                    max = Math.min(max,citems0.size());

                    citems1.addAll(citems0.subList(0,max));

                    data.setItems(citems1);
                    context.saveObject(data,key);

                    bundle.putInt("errCode",0);
                    bundle.putString("errMsg",null);


                }catch(Exception e){
                    e.printStackTrace();
                    data= new ChannelGroup();
                    bundle.putInt("errCode",1);
                    bundle.putString("errMsg",e.getMessage());
                }

                Message msg = new Message();
                bundle.putSerializable("data",data);
                msg.setData(bundle);

                handler.sendMessage(msg);

            }
        }.start();

    }

    private static ArrayList<Article> getLocalArticleData(final AppContext context,String url){
        //read from cache
        String data0 = "";
        url = assertUrl(context,url);
        String key = EncryptUtils.encodeMD5(url);
        ArrayList<Article> data = new ArrayList<Article>();
        int flagIdx = 0;
        Gson gson = new Gson();
        ArticleList data1 = null;

        if(!context.isReadDataCache(key)){
            return data;
        }
        data0 =(String) context.readObject(key);
        flagIdx = data0.lastIndexOf("<!--");
        if(flagIdx>0) {
            data0 = data0.substring(0, flagIdx);
            data1 = gson.fromJson(data0, ArticleList.class);
            data1.getItems().remove(0);
            data.addAll(data1.getItems());
        }



        if(null!=data1&&null!=data1.getNextPageId()&&!data1.getNextPageId().equals("")){
            data.addAll(getLocalArticleData(context,data1.getNextPageId()));
        }

        return data;
    }

    /**
     * 搜索本地数据
     * @param context
     * @param handler
     */
    public static void searchLocalData(final AppContext context,final String term,final Handler handler){

        new Thread(){
            public void run() {
                String data0 = "";
                int flagIdx = 0;
                Bundle bundle = new Bundle();
                SearchList data = new SearchList();
                data.setPageSize(1);

                SearchList.Result res = null;

                ArrayList<Article> data1= new ArrayList<Article>();

                try {

                    data1 = getLocalArticleData(context,URL.ARTICLE);

                    for(Article item:data1){
                        if(!(item.getTitle().indexOf(term)>-1||item.getDesc().indexOf(term)>-1||item.getCateName().indexOf(term)>-1)){
                            continue;
                        }
                        res = new SearchList.Result();
                        res.setType(1);//1新闻
                        res.setUrl(item.getUrl());
                        res.setPubDate(item.getEvtStartAt());
                        res.setTitle(item.getTitle());
                        res.setImg(item.getCover());
                        res.setDesc(item.getDesc());
                        res.setCateName(item.getCateName());
                        res.setStartAt(item.getEvtStartAt());
                        res.setEndAt(item.getEvtEndAt());
                        data.getResultlist().add(res);
                    }

                    bundle.putInt("errCode",0);
                    bundle.putString("errMsg",null);
                } catch (Exception e) {
                    e.printStackTrace();
                    bundle.putInt("errCode",1);
                    bundle.putString("errMsg",e.getMessage());
                }

                Message msg = new Message();
                bundle.putSerializable("data",data);

                msg.setData(bundle);
                handler.sendMessage(msg);

            }
        }.start();

    }

    /**
     * 添加删除收藏新闻.注意：如果已添加过则移除
     * @param context
     * @param item
     * @param handler
     */
    public static void toggleFavArticle(final AppContext context, final Article item, final String uid, final Handler handler){


        final Handler onDataGot = new Handler(){

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                Bundle data = msg.getData();
                int errCode = data.getInt("errCode");
                String errMsg = data.getString("errMsg");

                if(errMsg!=null){
                    UIHelper.ToastMessage(context,errMsg);
                    return;
                }

                final UserFavArticleList listData = (UserFavArticleList)data.getSerializable("data");


                new Thread(){
                    public void run() {
                        String uid1 = getOperationId(context,uid);
                        String key = EncryptUtils.encodeMD5(uid1+"_"+CONSTS.FAV_ARTICLE);
                        boolean isRemoved = false;
                        Bundle bundle = new Bundle();
                        try{

                            isRemoved = listData.toogleItem(item);


                            context.saveObject(listData,key);
                            //更新内存缓存数据
                            context.getData().setFavArticles(listData);

                            bundle.putInt("errCode",0);
                            bundle.putString("errMsg",null);


                        }catch(Exception e){
                            e.printStackTrace();
                            bundle.putInt("errCode",1);
                            bundle.putString("errMsg",e.getMessage());
                        }

                        Message msg = new Message();
                        bundle.putSerializable("data",listData);
                        bundle.putBoolean("isRemoved",isRemoved);
                        msg.setData(bundle);

                        handler.sendMessage(msg);

                    }
                }.start();

            }
        };

        getFavArticles(context,onDataGot,uid,false);

    }

    /**
     * 删除收藏的新闻.
     * @param context
     * @param itemId
     * @param handler
     */
    public static void removeFavArticle(final AppContext context,final String itemId,final String uid,final Handler handler){


        final Handler onDataGot = new Handler(){

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                Bundle data = msg.getData();
                int errCode = data.getInt("errCode");
                String errMsg = data.getString("errMsg");

                if(errMsg!=null){
                    UIHelper.ToastMessage(context,errMsg);
                    return;
                }

                final UserFavArticleList listData = (UserFavArticleList)data.getSerializable("data");

                //TODO:实际上下面会发起服务器端请求，所以放到新到线程中处理
                new Thread(){
                    public void run() {
                        String uid1 = getOperationId(context,uid);
                        String key = EncryptUtils.encodeMD5(uid1+"_"+CONSTS.FAV_ARTICLE);

                        Bundle bundle = new Bundle();
                        try{


                            listData.removeItemByMd5(itemId);

                            context.saveObject(listData,key);
                            //更新内存缓存数据
                            context.getData().setFavArticles(listData);

                            bundle.putInt("errCode",0);
                            bundle.putString("errMsg",null);


                        }catch(Exception e){
                            e.printStackTrace();
                            bundle.putInt("errCode",1);
                            bundle.putString("errMsg",e.getMessage());
                        }

                        Message msg = new Message();
                        bundle.putSerializable("data",listData);
                        msg.setData(bundle);

                        handler.sendMessage(msg);

                    }
                }.start();

            }
        };

        getFavArticles(context,onDataGot,uid,false);

    }

    /**
     * 获取操作ID
     * @param uid 用户ID，如果时空则使用app_id
     * @return
     */
    public static String getOperationId(final AppContext context,final String uid){

        if(StringUtils.isEmpty(uid)){
            return context.getResources().getString(R.string.app_id);
        }
        return uid;
    }

}
