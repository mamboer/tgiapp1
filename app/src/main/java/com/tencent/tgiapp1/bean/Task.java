package com.tencent.tgiapp1.bean;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.tencent.tgiapp1.AppContext;
import com.tencent.tgiapp1.AppDataProvider;
import com.tencent.tgiapp1.AppManager;
import com.tencent.tgiapp1.activity.IActivity;
import com.tencent.tgiapp1.entity.AppData;

import java.util.Map;

/**
 * 任务类 获取不同信息
 *
 */
public class Task {

    private final static String TAG = Task.class.getName();

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

        //errCode
        params.arg2 = 0;

        Log.e(TAG, "Task.run --> " + "任务编号： " + taskId);

        try {
            switch (taskId) {

                case Task.SN.INIT:

                    //数据初始化
                    AppData data = AppDataProvider.getAppDataSync(AppContext.Instance, false);
                    params.obj = data;

                    break;

                case Task.SN.GET_ARTICLE:

                    //TODO:获取新闻数据

                    break;
                case Task.SN.GET_NOTICE:

                    //TODO:获取公告数据

                    break;

                case Task.SN.GET_SLIDE:

                    //TODO:获取图片轮播

                    break;

                case Task.SN.GET_MANUAL:

                    //TODO:获取攻略

                    break;
                case Task.SN.GET_EXT:
                    //TODO:获取高玩心得
                    break;
                case Task.SN.GET_TESTING:
                    //TODO:获取评测
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
        IActivity ia = (IActivity) AppManager.getActivityByName(activityName);
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
         * 初始化
         */
        public static final int INIT = 100;
    }
}
