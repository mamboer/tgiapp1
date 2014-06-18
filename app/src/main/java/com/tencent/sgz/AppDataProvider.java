package com.tencent.sgz;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.tencent.sgz.common.EncryptUtils;
import com.tencent.sgz.common.HttpUtil;
import com.tencent.sgz.entity.AppData;
import com.tencent.sgz.entity.ArticleList;
import com.tencent.sgz.entity.MiscData;

/**
 * Created by levin on 6/18/14.
 */
public class AppDataProvider {

    static class URL{
        final static String ARTICLE = "http://ttxd.qq.com/webplat/info/news_version3/7367/8248/8277/8278/m6746/list_1.shtml";
        final static String NOTICE = "http://ttxd.qq.com/webplat/info/news_version3/7367/8248/8277/8279/m6748/list_1.shtml";
        final static String MISC = "http://ttxd.qq.com/webplat/info/news_version3/7367/8248/8277/8280/m6749/list_1.shtml";
    }

    public static String getRemoteData(AppContext appContext,String url,boolean isRefresh) throws AppException{
        String key = EncryptUtils.encodeMD5(url);
        Log.e("LV URL MD5加密"+url,key);
        try{
            String data = "";
            if(appContext.isNetworkConnected() && (!appContext.isReadDataCache(key) || isRefresh)) {
                data = HttpUtil.get(url);
            } else {
                data = (String)appContext.readObject(key);
                if(data == null)
                    data = "";
            }
            return data;

        }catch(Exception e){
            if(e instanceof AppException)
                throw (AppException)e;
            throw AppException.network(e);
        }
    }

    /**
     * 获取应用数据
     * @param context
     * @param handler
     * @throws AppException
     */
    public static void getAppData(final AppContext context,final Handler handler, final boolean isRefresh){

        final AppData datas = new AppData();

        new Thread(){
            public void run() {
                String data0 = "";
                try {
                    //杂项数据
                    data0 = getRemoteData(context,URL.MISC,isRefresh);
                    data0 = data0.substring(0,data0.lastIndexOf("<!--"));
                    Gson gson = new Gson();
                    datas.setMisc(gson.fromJson(data0,MiscData.class));

                    //新闻数据
                    data0 = getRemoteData(context,URL.ARTICLE,isRefresh);
                    data0 = data0.substring(0,data0.lastIndexOf("<!--"));
                    datas.setArticles(gson.fromJson(data0, ArticleList.class));
                    //第一条数据是为了生成json数据伪造的不可用
                    datas.getArticles().getItems().remove(0);

                    //公告数据
                    data0 = getRemoteData(context,URL.NOTICE,isRefresh);
                    data0 = data0.substring(0,data0.lastIndexOf("<!--"));
                    datas.setNotices(gson.fromJson(data0, ArticleList.class));
                    //第一条数据是为了生成json数据伪造的不可用
                    datas.getNotices().getItems().remove(0);


                } catch (AppException e) {
                    datas.setErrCode(1);
                    datas.setErrMsg(e.getMessage());
                    e.printStackTrace();

                }

                context.setData(datas);

                Message msg = new Message();
                Bundle data = new Bundle();
                //data.putSerializable("data",datas);
                data.putInt("errCode",datas.getErrCode());
                data.putString("errMsg",datas.getErrMsg());
                msg.setData(data);
                handler.sendMessage(msg);

            }
        }.start();
    }

}
