package com.tencent.sgz.entity;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by levin on 6/18/14.
 */
public class ArticleList implements Serializable {

    /*
    "catalog":1, //新闻分类id
    "newsCount":0,//新闻总数 （可选）
    "prevPageId":"上一页ID",
    "nextPageId":"下一页ID"
     */
    private int catalog;
    private int newsCount;
    private String prevPageId;
    private String nextPageId;

    private ArrayList<Article> items;

    public ArticleList(){
        items = new ArrayList<Article>();
    }

    public int getCatalog() {
        return catalog;
    }

    public void setCatalog(int catalog) {
        this.catalog = catalog;
    }

    public int getNewsCount() {
        return newsCount;
    }

    public void setNewsCount(int newsCount) {
        this.newsCount = newsCount;
    }

    public String getPrevPageId() {
        return prevPageId;
    }

    public void setPrevPageId(String prevPageId) {
        this.prevPageId = prevPageId;
    }

    public String getNextPageId() {
        return nextPageId;
    }

    public void setNextPageId(String nextPageId) {
        this.nextPageId = nextPageId;
    }

    public ArrayList<Article> getItems() {
        return items;
    }

    public void setItems(ArrayList<Article> items) {
        this.items = items;
    }
}
