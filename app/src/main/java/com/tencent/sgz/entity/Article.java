package com.tencent.sgz.entity;

import java.io.Serializable;

/**
 * Created by levin on 6/18/14.
 */
public class Article implements Serializable {
    /*
    "id":"新闻id",
    "title":"新闻标题",
    "desc":"新闻描述",
    "cateName":"分类名称",
    "cateId":"分类ID",
    "commentCount":"评论数",//可选
    "voteCount":"赞数",
    "favCount":"收藏数",
    "author":"作者昵称",//可选，默认可以是游戏名称
    "authorid":"作者id",//可选
    "pubDate":"发布日期"//格式：2014-05-26 06:42:18
    "url":"新闻地址",
    "cover":"新闻封面图片",//可选
    "evtStartAt":"活动开始时间",//可选。如果分类是活动时必须，格式同pubDate
    "evtEndAt":"活动结束时间"//可选。如果分类是活动时必须，格式同pubDate
     */

    private String id;
    private String title;
    private String desc;
    private String cateName;
    private String cateId;
    private int commentCount;
    private int voteCount;
    private int favCount;
    private String author;
    private String authorid;
    private String pubDate;
    private String url;
    private String cover;
    private String evtStartAt;
    private String evtEndAt;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCateName() {
        return cateName;
    }

    public void setCateName(String cateName) {
        this.cateName = cateName;
    }

    public String getCateId() {
        return cateId;
    }

    public void setCateId(String cateId) {
        this.cateId = cateId;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public int getFavCount() {
        return favCount;
    }

    public void setFavCount(int favCount) {
        this.favCount = favCount;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorid() {
        return authorid;
    }

    public void setAuthorid(String authorid) {
        this.authorid = authorid;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getEvtStartAt() {
        return evtStartAt;
    }

    public void setEvtStartAt(String evtStartAt) {
        this.evtStartAt = evtStartAt;
    }

    public String getEvtEndAt() {
        return evtEndAt;
    }

    public void setEvtEndAt(String evtEndAt) {
        this.evtEndAt = evtEndAt;
    }
}
