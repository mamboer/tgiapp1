package com.tencent.tgiapp1.bean;

import com.tencent.tgiapp1.common.EncryptUtils;

public class FavItem {

    private String icon;

    private String name;

    private String cateName;

    private String date;

    private String action;

    private String md5;

    private String startAt;

    private String endAt;

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

    public String getStartAt() {
        return startAt;
    }

    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public String getEndAt() {
        return endAt;
    }

    public void setEndAt(String endAt) {
        this.endAt = endAt;
    }
}
