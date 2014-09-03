package com.tencent.tgiapp1.service;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.tencent.tgiapp1.AppContext;
import com.tencent.tgiapp1.AppDataProvider;
import com.tencent.tgiapp1.AppManager;
import com.tencent.tgiapp1.api.ApiClient;
import com.tencent.tgiapp1.entity.AppData;
import com.tencent.tgiapp1.entity.ArticleList;

/**
 * 任务类 获取不同信息
 *
 */
public class DataTask {

    private final static String TAG = DataTask.class.getName();

    private final static Handler taskHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            done(msg);
        }
    };

    /**
     * 执行指定的任务
     * @param params
     */
    public static void run(Message params){

        int taskId = params.what;
        int serviceId = params.arg1;
        Bundle data = params.getData();

        //errCode
        params.arg2 = 0;

        Log.e(TAG, "Task.run --> " + "任务编号： " + taskId);

        try {
            switch (taskId) {

                case DataTask.SN.INIT:

                    //数据初始化
                    AppData data0 = AppDataProvider.getAppDataSync(AppContext.Instance, false);
                    params.obj = data0;

                    break;

                case SN.Get_AppData:
                    //应用数据更新
                    AppData data1 = AppDataProvider.getAppDataSync(AppContext.Instance, true);
                    params.obj = data1;
                    break;

                case DataTask.SN.GET_ARTICLE:

                    ArticleList data2 = AppDataProvider.getArticleDataSync(AppContext.Instance,data.getString("url"), true);
                    params.obj = data2;

                    break;
                case DataTask.SN.GET_NOTICE:

                    //TODO:获取公告数据

                    break;

                case DataTask.SN.GET_SLIDE:

                    //TODO:获取图片轮播

                    break;

                case DataTask.SN.GET_MANUAL:

                    //TODO:获取攻略

                    break;
                case DataTask.SN.GET_EXT:
                    //TODO:获取高玩心得
                    break;
                case DataTask.SN.GET_TESTING:
                    //TODO:获取评测
                    break;
                case SN.DownloadImg:
                    Bitmap obj = ApiClient.getAndSaveImageSync(AppContext.Instance,data.getString("url"));
                    params.obj = obj;
                    break;
            }

        } catch (Exception e) {
            params.arg2 = -1;
            params.obj = e;
            e.printStackTrace();
        }

        Message msg = new Message();
        msg.copyFrom(params);

        taskHandler.sendMessage(msg);
    }

    /**
     * 执行指定任务后的回调处理函数
     * @param params
     */
    private static void done( Message params){
        int taskId = params.what;
        Bundle data = params.getData();
        String activityName = data.getString("activity");

        Log.e(TAG, "Task.done --> " + "任务编号： " + taskId);

        //刷新UI
        IUpdatableUI ia = (IUpdatableUI) AppManager.getActivityByName(activityName);
        ia.refresh(taskId,params);

    }

    /**
     * 任务序号
     */
    public static class SN{
        /**
         * 获取新闻
         */
        public static final int GET_ARTICLE = 0;
        /**
         * 获取公告
         */
        public static final int GET_NOTICE = 1;
        /**
         * 获取图片轮播
         */
        public static final int GET_SLIDE = 2;
        /**
         * 获取玩法攻略
         */
        public static final int GET_MANUAL = 3;
        /**
         * 获取评测
         */
        public static final int GET_TESTING = 4;
        /**
         * 获取高玩心得
         */
        public static final int GET_EXT = 5;
        /**
         * 获取应用数据
         */
        public static final int Get_AppData = 6;
        /**
         * 下载图片
         */
        public static final int DownloadImg = 7;
        /**
         * 初始化
         */
        public static final int INIT = 100;

    }
}
