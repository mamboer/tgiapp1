package com.tencent.sgz.bean;

import android.graphics.drawable.Drawable;

import com.tencent.sgz.common.EncryptUtils;

public class FavItem {

    private String icon;

    private String name;

    private String cateName;

    private String date;

    private String action;

    private String md5;

    public String getAction(){return action;}
    public void setAction(String action){
        this.action = action;
        md5 = EncryptUtils.encodeMD5(action);
    }

    public String getDate(){return date;}
    public void setDate(String date){this.date = date;}

    public String getCateName() {
        return cateName;
    }

    public void setCateName(String cName) {
        this.cateName = cName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getMd5() {
        return md5;
    }
}
