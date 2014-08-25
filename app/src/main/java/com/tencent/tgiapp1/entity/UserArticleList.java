package com.tencent.tgiapp1.entity;

import com.tencent.tgiapp1.AppContext;
import com.tencent.tgiapp1.R;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by levin on 6/24/14.
 */
public class UserArticleList implements Serializable {
    private String userId;
    private ArrayList<Article> items;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ArrayList<Article> getItems() {
        return items;
    }

    public void setItems(ArrayList<Article> items) {
        this.items = items;
    }

    public UserArticleList(String userId,ArrayList<Article> items){
        this.userId = userId;
        this.items = items;
    }
    public UserArticleList(String userId){
        this(userId,new ArrayList<Article>());
    }
    public UserArticleList(){
        this(AppContext.Instance.getResources().getString(R.string.app_id));
    }

    public void addItem(Article item,boolean append){
        if(!existsItem(item)){
            if(append){
                this.items.add(item);
            }else{
                items.add(0,item);
            }
        }
    }

    /**
     * 添加或删除记录
     * @param item
     * @return true添加记录｜false删除记录
     */
    public boolean toogleItem(Article item){
        Article item1 = getItemByMd5(item.getMD5());
        boolean isExists = (null!=item1);
        if(isExists){
            this.items.remove(item1);
        }else{
            this.items.add(0,item);
        }
        return isExists;
    }

    /**
     * 增加查看次数
     * @param item
     * @param diffCount
     */
    public void increaseViewCount(Article item,int diffCount){
        Article item1 = getItemByMd5(item.getMD5());
        boolean isExists = (null!=item1);
        if(!isExists) {
            return;
        }
        item1.setViewCount(item1.getViewCount()+diffCount);
    }

    /**
     * 记录是否存在
     * @param item
     * @return
     */
    public boolean existsItem(Article item){

        Article item1 = getItemByMd5(item.getMD5());

        return (null!=item1);
    }

    /**
     * 根据MD5获取记录
     * @param md5
     * @return
     */
    public Article getItemByMd5(String md5){
        Article item = null;
        for(Article it:items){
            if(it.getMD5().equals(md5)){
                item = it;
                break;
            }
        }
        return item;
    }

    /**
     * 根据MD5获取记录
     * @param md5
     * @return
     */
    public void removeItemByMd5(String md5){
        Article item = getItemByMd5(md5);
        if(null!=item){
            this.items.remove(item);
        }
    }

}
