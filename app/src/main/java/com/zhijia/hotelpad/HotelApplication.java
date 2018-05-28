package com.zhijia.hotelpad;

import android.app.Application;
import android.content.Context;

import com.taobao.sophix.SophixManager;

/**
 * Created by rxnh8 on 2018/5/26.
 */

public class HotelApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SophixManager.getInstance().queryAndLoadNewPatch();
    }
}
