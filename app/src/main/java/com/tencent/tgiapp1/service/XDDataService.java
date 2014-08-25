package com.tencent.tgiapp1.service;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.tencent.tgiapp1.AppContext;
import com.tencent.tgiapp1.AppDataProvider;
import com.tencent.tgiapp1.AppManager;
import com.tencent.tgiapp1.activity.IActivity;
import com.tencent.tgiapp1.bean.Task;
import com.tencent.tgiapp1.entity.AppData;

import java.util.ArrayList;

/**
 * 实现业务调度的核心逻辑服务
 * 关于service请看文章：http://www.vogella.com/tutorials/AndroidServices/article.html
 */
public class XDDataService extends Service implements Runnable {
    private static String TAG = XDDataService.class.getName();

    public XDDataService() {
    }
    // 所以任务
    public static ArrayList<Task> allTasks = new ArrayList<Task>();
    // 循环控制变量
    private boolean isrun = true;

    /**
     * 在集合里，通过name获取Activity对象
     *
     * @param name
     * @return Activity
     */
    public static Activity getActivityByName(String name) {
        return AppManager.getActivityByName(name);
    }

    /**
     * 新建任务
     *
     * @param task
     */
    public static void addTask(Task task) {
        // 添加一个任务
        allTasks.add(task);
    }

    /**
     * 启动线程
     */
    @Override
    public void run() {
        while (isrun) {
            Task lastTask = null;
            if (allTasks.size() > 0) {
                synchronized (allTasks) {
                    // 获取任务
                    lastTask = allTasks.get(0);
                    // 执行任务

                    doTask(lastTask);
                }
                return;
            }
            // 如果没有任务，则等待2000ms，继续获取任务
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 很据任务ID，执行该任务
     *
     * @param task
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void doTask(Task task) {

        Message msg = new Message();
        Log.e(TAG, "doTask-->" + "任务编号： " + task.getTaskId());
        msg.what = task.getTaskId();

        try {
            switch (task.getTaskId()) {

                case Task.SN.INIT:

                    //数据初始化
                    AppData data = AppDataProvider.getAppDataSync(AppContext.Instance, false);
                    msg.obj = data;

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
            msg.arg1 = -1;
            msg.obj = e;
            e.printStackTrace();
        }
        handler.sendMessage(msg);
        allTasks.remove(task);// 执行完任务，则移出该任务
    }

    // 当前服务的子线程Handler,负责处理更新UI操作
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.i(TAG, "UI 更新编号：" + msg.what);
            switch (msg.what) {

                case Task.SN.INIT:

                    //刷新UI
                    IActivity ia = (IActivity) getActivityByName("AppStart");
                    ia.refresh(msg.arg1,msg.obj);

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
        }

    };

    @Override
    public void onCreate() {
        super.onCreate();
        isrun = true;
        new Thread(this).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isrun = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        return Service.START_NOT_STICKY;
    }

    /**
     * 退出服务
     */
    public static void stop(Context context) {
        // 关闭服务
        Intent it = new Intent(context,XDDataService.class);
        context.stopService(it);
    }

    /**
     * 启动服务。在AppStart中调用
     */
    public static void start(Context context,Bundle data){
        Intent it = new Intent(context,XDDataService.class);
        // potentially add data to the intent
        if (null!=data){
            it.putExtras(data);
        }
        context.startService(it);
    }
}

