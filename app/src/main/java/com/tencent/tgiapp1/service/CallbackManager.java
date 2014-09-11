package com.tencent.tgiapp1.service;

import android.os.Handler;
import android.os.Message;

import java.util.HashMap;

/**
 * Created by levin on 9/11/14.
 */
public class CallbackManager {
    private final static HashMap<String,Handler> callbacks = new HashMap<String, Handler>();

    /**
     * 添加一个回调
     * @param key
     * @param cbk
     */
    public static void add(String key,Handler cbk){
        callbacks.put(key,cbk);
    }

    /**
     * 移除某个回调函数
     * @param key
     */
    public static void remove(String key){
        callbacks.remove(key);
    }

    /**
     * 是否含有指定的回调函数
     * @param key
     * @return
     */
    public static boolean has(String key){
        boolean r = callbacks.containsKey(key);
        return r;
    }

    /**
     * 获取指定的回调函数
     * @param key
     * @return
     */
    public static Handler get(String key){
        if(!has(key)){
            return null;
        }
        return callbacks.get(key);
    }

    /**
     * 执行指定的回调，并销毁该回调
     * @param key
     * @param msg
     */
    public static void run(String key,Message msg){
        Handler handler = get(key);
        if(handler==null){
            return;
        }
        Message msg1 = new Message();
        msg1.copyFrom(msg);
        handler.sendMessage(msg1);
        remove(key);
    }
}
