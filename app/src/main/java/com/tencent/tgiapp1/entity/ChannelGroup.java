package com.tencent.tgiapp1.entity;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by levin on 6/17/14.
 */
public class ChannelGroup implements Serializable {
    private String group;

    public void setGroup(String g){this.group = g;}
    public String getGroup(){return this.group;}

    private ArrayList<ChannelItem> items;
    public void setItems(ArrayList<ChannelItem> items){
        this.items = items;
    }
    public ArrayList<ChannelItem> getItems(){return this.items;}

    public ChannelGroup(){
        this.items = new ArrayList<ChannelItem>();
    }

}
