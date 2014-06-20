package com.tencent.sgz.entity;

import java.io.Serializable;

/**
 * Created by levin on 6/18/14.
 */
public class AppData implements Serializable {

    public AppData(){
        errCode = 0;
        articles = new ArticleList();
        notices = new ArticleList();
        slides = new ArticleList();
        misc = new MiscData();
    }

    private int errCode;
    private String errMsg;

    private ArticleList articles;
    private ArticleList notices;
    private ArticleList slides;
    private MiscData misc;

    public ArticleList getArticles() {
        return articles;
    }

    public void setArticles(ArticleList articles) {
        this.articles = articles;
    }

    /**
     * 获取公告信息
     * @return
     */
    public ArticleList getNotices() {
        return notices;
    }

    public void setNotices(ArticleList notices) {
        this.notices = notices;
    }

    public MiscData getMisc() {
        return misc;
    }

    public void setMisc(MiscData misc) {
        this.misc = misc;
    }

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    /**
     * 获取图片轮播数据
     * @return
     */
    public ArticleList getSlides() {
        return slides;
    }

    /**
     * 设置图片轮播数据
     * @param slides
     */
    public void setSlides(ArticleList slides) {
        this.slides = slides;
    }

    public void appendArticles(ArticleList items){
        this.articles.getItems().addAll(items.getItems());
    }

}
