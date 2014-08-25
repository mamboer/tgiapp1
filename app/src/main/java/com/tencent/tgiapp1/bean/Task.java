package com.tencent.tgiapp1.bean;

import com.tencent.tgiapp1.activity.IActivity;

import java.util.Map;

/**
 * 任务类 获取不同信息
 *
 */
public class Task {
    private int taskId;// 任务编号
    @SuppressWarnings("rawtypes")
    private Map taskParam;// 任务参数
    //任务上下文
    private IActivity context;

    @SuppressWarnings("rawtypes")
    public Task(int taskId, Map taskParam,IActivity ct) {
        this.taskId = taskId;
        this.taskParam = taskParam;
        this.context = ct;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    @SuppressWarnings("rawtypes")
    public Map getTaskParam() {
        return taskParam;
    }

    @SuppressWarnings("rawtypes")
    public void setTaskParam(Map taskParam) {
        this.taskParam = taskParam;
    }

    public IActivity getContext() {
        return context;
    }

    public void setContext(IActivity context) {
        this.context = context;
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
