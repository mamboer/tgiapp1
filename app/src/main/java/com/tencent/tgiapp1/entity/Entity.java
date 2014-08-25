package com.tencent.tgiapp1.entity;

import java.io.Serializable;

/**
 * Created by levin on 6/17/14.
 */
public class Entity implements Serializable {

    private String id;
    private String name;

    public void setId(String id){this.id = id;}
    public String getId(){return this.id;}

    public void setName(String name){this.name = name;}
    public String getName(){return this.name;}
}
