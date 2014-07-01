package com.tencent.sgz.entity;

import com.tencent.sgz.AppContext;
import com.tencent.sgz.R;

import java.io.Serializable;
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
