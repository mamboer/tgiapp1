package com.tencent.tgiapp1.entity;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by levin on 6/17/14.
 */
public class ToolboxData implements Serializable {
    private ArrayList<ActionItem> tools;
    private ArrayList<ActionItem> apps;

    public ToolboxData(){
        tools = new ArrayList<ActionItem>();
        apps = new ArrayList<ActionItem>();
    }

    public void setTools(ArrayList<ActionItem> items){
        this.tools = items;
    }
    public ArrayList<ActionItem> getTools(){return this.tools;}

    public void setApps(ArrayList<ActionItem> items){
        this.apps = items;
    }
    public ArrayList<ActionItem> getApps(){return this.apps;}
}
