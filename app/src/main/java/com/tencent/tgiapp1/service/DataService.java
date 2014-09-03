package com.tencent.tgiapp1.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.os.Process;


/**
 * 实现业务调度的核心逻辑服务
 * 关于service请看文章：http://www.vogella.com/tutorials/AndroidServices/article.html
 */
public class DataService extends Service {
    private static String TAG = DataService.class.getName();

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    public DataService() {}
    @Override
    public void onCreate() {
        super.onCreate();
        /*
        http://developer.android.com/guide/components/services.html
        A service runs in the main thread of its hosting process—the service does not create its own thread and does not run in a separate process (unless you specify otherwise).
        This means that, if your service is going to do any CPU intensive work or blocking operations (such as MP3 playback or networking),
        you should create a new thread within the service to do that work.
        By using a separate thread, you will reduce the risk of Application Not Responding (ANR) errors
        and the application's main thread can remain dedicated to user interaction with your activities.
         */

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.what = intent.getIntExtra("taskId",-1);
        msg.arg1 = startId;
        msg.setData(intent.getExtras());

        mServiceHandler.sendMessage(msg);

        return START_STICKY;
    }

    /**
     * 退出服务
     */
    public static void stop(Context context) {
        // 关闭服务
        Intent it = new Intent(context,DataService.class);
        context.stopService(it);
    }

    /**
     * 启动一个服务。同一activity可以多次调用该方法
     * @param data 传给服务的数据
     */
    public static void execute(Context context,Bundle data){
        Intent it = new Intent(context,DataService.class);
        // potentially add data to the intent
        if (null!=data){
            it.putExtras(data);
        }
        context.startService(it);
    }

    // Handler that receives messages from the thread
    // 当前服务的子线程Handler,负责处理更新UI操作
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {

            DataTask.run(msg);
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
        }
    }


}

