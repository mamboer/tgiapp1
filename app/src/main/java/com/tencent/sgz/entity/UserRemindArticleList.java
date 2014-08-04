package com.tencent.sgz.entity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.tencent.sgz.AppContext;
import com.tencent.sgz.AppDataProvider;
import com.tencent.sgz.R;
import com.tencent.sgz.common.EncryptUtils;
import com.tencent.sgz.common.StringUtils;
import com.tencent.sgz.common.UIHelper;

import java.util.ArrayList;

/**
 * 提醒
 * Created by levin on 7/1/14.
 */
public class UserRemindArticleList extends UserArticleList {


    public UserRemindArticleList(String userId,ArrayList<Article> items){
        super(userId,items);
    }
    public UserRemindArticleList(String userId){
        super(userId);
    }
    public UserRemindArticleList(){
        super();
    }

    /**
     * 以同步的方式获取新闻提醒数据
     * @param context
     * @param reset
     * @param uid
     * @return
     */
    public static UserRemindArticleList getRemindArticlesSync(final AppContext context,final String uid,final boolean reset) throws Exception{
        String uid1 = AppDataProvider.getOperationId(context, uid);
        String key = EncryptUtils.encodeMD5(uid1 + "_" + AppDataProvider.CONSTS.REMIND_ARTICLE);
        UserRemindArticleList data = new UserRemindArticleList(uid1);
        try{

            if(!(!context.isReadDataCache(key) || reset)) {
                data = (UserRemindArticleList)context.readObject(key);
                if(data == null)
                    data = new UserRemindArticleList(uid1);
            }


        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }

        return data;
    }
    /**
     * 获取提醒的新闻数据
     * @return
     */
    public static void getRemindArticles(final AppContext context,final Handler handler,final String uid,final boolean reset){



        new Thread(){
            public void run() {

                Bundle bundle = new Bundle();
                UserRemindArticleList data = null;
                try{

                    data = getRemindArticlesSync(context, uid, reset);

                    //更新AppData中的数据
                    context.getData().setRemindArticles(data);

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
     * 添加删除新闻提醒.注意：如果已添加过则移除
     * @param context
     * @param item
     * @param handler
     */
    public static void toggleRemindArticle(final AppContext context, final Article item, final String uid, final Handler handler){


        final Handler onDataGot = new Handler(){

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                Bundle data = msg.getData();
                int errCode = data.getInt("errCode");
                String errMsg = data.getString("errMsg");

                if(errMsg!=null){
                    UIHelper.ToastMessage(context, errMsg);
                    return;
                }

                final UserRemindArticleList listData = (UserRemindArticleList)data.getSerializable("data");


                new Thread(){
                    public void run() {
                        String uid1 = AppDataProvider.getOperationId(context, uid);
                        String key = EncryptUtils.encodeMD5(uid1+"_"+ AppDataProvider.CONSTS.REMIND_ARTICLE);
                        boolean isRemoved = false;
                        Bundle bundle = new Bundle();
                        try{

                            isRemoved = listData.toogleItem(item);


                            context.saveObject(listData,key);
                            //更新内存缓存数据
                            context.getData().setRemindArticles(listData);

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

        getRemindArticles(context, onDataGot, uid, false);

    }

    /**
     * 增加新闻的阅读次数
     * @param context
     * @param item
     * @param handler
     */
    public static void updateArticleViewCount(final AppContext context, final Article item, final String uid,final int diffCount, final Handler handler){


        final Handler onDataGot = new Handler(){

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                Bundle data = msg.getData();
                int errCode = data.getInt("errCode");
                String errMsg = data.getString("errMsg");

                if(errMsg!=null){
                    UIHelper.ToastMessage(context, errMsg);
                    return;
                }

                final UserRemindArticleList listData = (UserRemindArticleList)data.getSerializable("data");


                new Thread(){
                    public void run() {
                        String uid1 = AppDataProvider.getOperationId(context, uid);
                        String key = EncryptUtils.encodeMD5(uid1+"_"+ AppDataProvider.CONSTS.REMIND_ARTICLE);
                        Bundle bundle = new Bundle();
                        try{

                            listData.increaseViewCount(item,diffCount);

                            context.saveObject(listData,key);
                            //更新内存缓存数据
                            context.getData().setRemindArticles(listData);

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

        getRemindArticles(context, onDataGot, uid, false);

    }

    /**
     * 删除新闻提醒.
     * @param context
     * @param itemId
     * @param handler
     */
    public static void removeRemindArticle(final AppContext context,final String itemId,final String uid,final Handler handler){


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

                final UserRemindArticleList listData = (UserRemindArticleList)data.getSerializable("data");

                //TODO:实际上下面会发起服务器端请求，所以放到新到线程中处理
                new Thread(){
                    public void run() {
                        String uid1 = AppDataProvider.getOperationId(context, uid);
                        String key = EncryptUtils.encodeMD5(uid1+"_"+ AppDataProvider.CONSTS.REMIND_ARTICLE);

                        Bundle bundle = new Bundle();
                        try{


                            listData.removeItemByMd5(itemId);

                            context.saveObject(listData,key);
                            //更新内存缓存数据
                            context.getData().setRemindArticles(listData);

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

        getRemindArticles(context,onDataGot,uid,false);

    }

    /**
     * 获取快过期的本地活动提醒
     * @return
     */
    public static ArrayList<Article> getDueArticleList(AppContext context){
        int dueDay = context.getResources().getInteger(R.integer.reminder_ahead_day);
        ArrayList<Article> items = new ArrayList<Article>();

        ArrayList<Article> items0 = context.getData().getRemindArticles().getItems();

        for(Article item:items0){
            if(StringUtils.isLargerThanTodayButLessThan(item.getEvtEndAt(),dueDay)){
                items.add(item);
            }
        }

        return items;
    }

    /**
     * 获取快开始的本地活动提醒
     * @return
     */
    public static ArrayList<Article> getReadyToBeginArticleList(AppContext context){
        int dueDay = context.getResources().getInteger(R.integer.reminder_readytobegin_day);
        ArrayList<Article> items = new ArrayList<Article>();

        ArrayList<Article> items0 = context.getData().getRemindArticles().getItems();

        for(Article item:items0){
            if(StringUtils.isLessThanTodayAndLessThan(item.getEvtStartAt(),dueDay)){
                items.add(item);
            }
        }

        return items;
    }

    /**
     * 发活动提醒广播
     */
    public static void sendBroadCast(AppContext context){
        //快结束提醒
        ArrayList<Article> items = getDueArticleList(context);

        if(items.size()>0){
            Intent intent = new Intent(context.getString(R.string.receiver_eventnotice));
            intent.putExtra("cnt", items.size());
            intent.putExtra("type",0);
            context.sendBroadcast(intent);
        }

        //快开始提醒
        items = getReadyToBeginArticleList(context);

        if(items.size()>0){
            Intent intent = new Intent(context.getString(R.string.receiver_eventnotice));
            intent.putExtra("cnt", items.size());
            intent.putExtra("type",1);
            context.sendBroadcast(intent);
        }

    }
}
