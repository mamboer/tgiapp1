package com.tencent.sgz;

import com.google.inject.AbstractModule;

/**
 * Created by levin on 5/19/14.
 */
public class RoboguiceIocModule extends AbstractModule {
    @Override
    protected void configure(){
        //some custome di logic
        //requestStaticInjection( Login.class );
    }
}
