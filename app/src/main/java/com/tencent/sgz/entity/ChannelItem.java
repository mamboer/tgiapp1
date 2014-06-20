package com.tencent.sgz.entity;

import com.tencent.sgz.common.EncryptUtils;

/**
 * Created by levin on 6/17/14.
 */
public class ChannelItem extends Entity {
    private String icon;
    private String action;

    public void setIcon(String ico){this.icon=ico;}
    public String getIcon(){return this.icon;}

    public void setAction(String act){this.action = act;}
    public String getAction(){return this.action;}

    public String getMD5(){
        return EncryptUtils.encodeMD5(this.action);
    }

}
