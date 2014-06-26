package com.tencent.sgz.entity;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by levin on 6/17/14.
 */
public class MiscData implements Serializable {

    private ArrayList<CateItem> cates;
    private ArrayList<ChannelGroup> channels;
    private ToolboxData toolbox;

    public MiscData(){
        cates = new ArrayList<CateItem>();
        channels = new ArrayList<ChannelGroup>();
        toolbox = new ToolboxData();
    }

    public void setCates(ArrayList<CateItem> items) {this.cates = items;}
    public ArrayList<CateItem> getCates(){return this.cates;}

    public void setKeywords(ArrayList<ChannelGroup> items){this.channels = items;}
    public ArrayList<ChannelGroup> getKeywords(){return this.channels;}

    public void setToolbox(ToolboxData item){this.toolbox = item;}
    public ToolboxData getToolbox(){return this.toolbox;}
}
