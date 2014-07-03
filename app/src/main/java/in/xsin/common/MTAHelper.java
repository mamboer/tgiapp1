package in.xsin.common;

import android.content.Context;

import com.tencent.stat.StatService;

import java.util.Properties;

/**
 * Created by levin on 7/3/14.
 */
public class MTAHelper {

    public static class TYPE{
        final public static String CLICK = "CLICK";
        final public static String REMOTE_NETWORK = "NETWORK";
        final public static String BROADCAST = "BROADCAST";
        final public static String FRAGMENT = "FRAGMENT";
        final public static String LOGIN = "LOGIN";
    }

    public static void trackClick(Context context,String className,String viewName){
        track(context,TYPE.CLICK,className,viewName);

    }

    public static void track(Context context,String trackType,String className,String viewName){
        Properties pro = new Properties();
        pro.setProperty("className",className);
        pro.setProperty("viewName",className+"."+viewName);
        StatService.trackCustomKVEvent(context, trackType, pro);
    }

    public static void trackLogin(Context ctx,boolean isOk,Properties pro){
        StatService.trackCustomEvent(ctx, TYPE.LOGIN, String.valueOf(isOk));

        if(null!=pro){
            if(isOk){
                StatService.trackCustomKVEvent(ctx, TYPE.LOGIN+".OnLoginOK", pro);
            }else {
                StatService.trackCustomKVEvent(ctx, TYPE.LOGIN+".OnLoginError", pro);
            }
        }
    }

}
