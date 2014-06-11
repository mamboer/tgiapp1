package com.tencent.sgz.bean;

import android.graphics.drawable.Drawable;

public class FavItem {

    private Drawable icon;

    private String name;

    private String cateName;

    private String date;

    private String action;

    public String getAction(){return action;}
    public void setAction(String action){this.action = action;}

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

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }
}
