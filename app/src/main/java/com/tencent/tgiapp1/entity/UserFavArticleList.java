package com.tencent.tgiapp1.entity;

import java.util.ArrayList;

/**
 * Created by levin on 6/24/14.
 */
public class UserFavArticleList extends UserArticleList {
    public UserFavArticleList(String userId,ArrayList<Article> items){
        super(userId,items);
    }
    public UserFavArticleList(String userId){
        super(userId);
    }
    public UserFavArticleList(){
        super();
    }
}
