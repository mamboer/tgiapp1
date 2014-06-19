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

    //TODO:移到配置文件中
    public static class URL{
        final public static String ARTICLE = "http://ttxd.qq.com/webplat/info/news_version3/7367/8248/8277/8278/m6746/list_1.shtml";//新闻数据
        final public static String NOTICE = "http://ttxd.qq.com/webplat/info/news_version3/7367/8248/8277/8279/m6748/list_1.shtml";//公告数据
        final public static String MISC = "http://ttxd.qq.com/webplat/info/news_version3/7367/8248/8277/8280/m6749/list_1.shtml";//杂项数据
        final public static String SLIDE = "http://ttxd.qq.com/webplat/info/news_version3/7367/8248/8277/8340/m6810/list_1.shtml";//图片轮播
        final public static String DEFAULT_SLIDE_IMG="http://ossweb-img.qq.com/upload/webplat/info/tgideas/201406/1402931095_1436653066_785_imageAddr.jpg";
        final public static String MANUAL ="http://ttxd.qq.com/webplat/info/news_version3/7367/8248/8277/8339/8341/m6808/list_1.shtml";//玩法攻略
        final public static String TESTING="http://ttxd.qq.com/webplat/info/news_version3/7367/8248/8277/8339/8342/m6808/list_1.shtml";//评测
        final public static String EXP = "http://ttxd.qq.com/webplat/info/news_version3/7367/8248/8277/8339/8343/m6808/list_1.shtml";//高玩心得
    }

    public static String getRemoteData(AppContext appContext,String url,boolean isRefresh) throws AppException{
        String key = EncryptUtils.encodeMD5(url);

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
                int flagIdx = 0;
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
