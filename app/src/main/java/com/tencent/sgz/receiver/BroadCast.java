package com.tencent.sgz.receiver;

import com.tencent.sgz.AppContext;
import com.tencent.sgz.R;
import com.tencent.sgz.activity.MainActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import in.xsin.common.MTAHelper;

/**
 * 通知信息广播接收器
 * @author lv (http://t.qq.com/badstyle)
 * @version 1.0
 * @created 2014-5-11
 */
public class BroadCast extends BroadcastReceiver {

    private final static int NOTIFICATION_ID = R.layout.home;

    public static String TAG = BroadCast.class.getName();

    private static int lastNoticeCount;

    @Override
    public void onReceive(Context context, Intent intent) {
        String ACTION_NAME = intent.getAction();

        //活动提醒
        assertRemindArticles(context,ACTION_NAME,intent);

    }

    void assertRemindArticles(Context context,String actionName,Intent intent){
        if(!context.getString(R.string.receiver_eventnotice).equals(actionName)) {
            return;
        }
        int activeCount = intent.getIntExtra("cnt", 0);//活动提醒数

        //通知栏显示
        this.notification(context, activeCount,"NOTICE_REMIND","您有 $ 条活动提醒快要到期啦，赶紧去看看！");
    }

    private void notification(Context context, int noticeCount,String key,String msgTpl){
        //创建 NotificationManager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String contentTitle = context.getString(R.string.app_name);
        String contentText = msgTpl.replace("$",noticeCount+"");
        int _lastNoticeCount;

        //判断是否发出通知信息
        if(noticeCount == 0)
        {
            notificationManager.cancelAll();
            lastNoticeCount = 0;
            return;
        }
        if(noticeCount == lastNoticeCount)
        {
            return;
        }

        _lastNoticeCount = lastNoticeCount;
        lastNoticeCount = noticeCount;

        //创建通知 Notification
        Notification notification = null;

        if(noticeCount > _lastNoticeCount)
        {
            String noticeTitle = msgTpl.replace("$",(noticeCount-_lastNoticeCount)+"");
            notification = new Notification(R.drawable.icon, noticeTitle, System.currentTimeMillis());
        }
        else
        {
            notification = new Notification();
        }

        //设置点击通知跳转
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(key, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //设置最新信息
        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

        //设置点击清除通知
        notification.flags = Notification.FLAG_AUTO_CANCEL;

        if(noticeCount > _lastNoticeCount)
        {
            //设置通知方式
            notification.defaults |= Notification.DEFAULT_LIGHTS;

            //设置通知音-根据app设置是否发出提示音
            if(((AppContext)context.getApplicationContext()).isAppSound())
                notification.sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notificationsound);

            //设置振动 <需要加上用户权限android.permission.VIBRATE>
            //notification.vibrate = new long[]{100, 250, 100, 500};
        }

        //发出通知
        notificationManager.notify(NOTIFICATION_ID, notification);

        MTAHelper.track(context, MTAHelper.TYPE.BROADCAST,TAG,"");
    }

}
