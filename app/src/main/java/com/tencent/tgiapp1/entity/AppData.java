package com.tencent.tgiapp1.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
        favArticles = new UserFavArticleList();
        remindArticles = new UserRemindArticleList();
        xgNotices = new ArrayList<XGNotification>();
    }

    private int errCode;
    private String errMsg;

    private ArticleList articles;
    private ArticleList notices;
    private ArticleList slides;
    private MiscData misc;
    private UserFavArticleList favArticles;
    private UserRemindArticleList remindArticles;

    private List<XGNotification> xgNotices;

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

    public UserFavArticleList getFavArticles() {
        return favArticles;
    }

    public void setFavArticles(UserFavArticleList favArticles) {
        this.favArticles = favArticles;
    }

    /**
     * 收藏数据中是否存在指定数据
     * @param item
     * @return
     */
    public boolean hasFavArticle(Article item){
        return hasFavItem(item.getMD5());
    }

    /**
     * 收藏数据中是否存在指定数据
     * @param md5id
     * @return
     */
    public boolean hasFavItem(String md5id){
        boolean has = false;
        for(Article item0:favArticles.getItems()){
            if(item0.getMD5().equals(md5id)){
                has = true;
                break;
            }
        }
        return has;
    }

    /**
     * 提醒数据中是否存在指定数据
     * @param md5id
     * @return
     */
    public boolean hasRemindItem(String md5id){
        boolean has = false;
        for(Article item0:remindArticles.getItems()){
            if(item0.getMD5().equals(md5id)){
                has = true;
                break;
            }
        }
        return has;
    }

    public UserRemindArticleList getRemindArticles() {
        return remindArticles;
    }

    public void setRemindArticles(UserRemindArticleList remindArticles) {
        this.remindArticles = remindArticles;
    }

    public List<XGNotification> getXgNotices() {
        return xgNotices;
    }

    public void setXgNotices(List<XGNotification> xgNotices) {
        this.xgNotices = xgNotices;
    }
}
