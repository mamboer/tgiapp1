package com.tencent.sgz.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class XDDataService extends Service {

    private static String TAG = XDDataService.class.getName();

    public XDDataService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
