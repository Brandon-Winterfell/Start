package com.huahua.start.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Administrator on 2017/2/25.
 * @function 下载不同状态接口回调
 */

public class UpdateProductService extends Service {

    /**
     * 常量
     */
    private static final int UPDATE_FLAG = 0X01;
    private static final int PRODUCT_FLAG = 0X02;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}





















