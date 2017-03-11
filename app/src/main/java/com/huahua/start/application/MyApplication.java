package com.huahua.start.application;

import android.app.Application;
import android.util.Log;

import com.huahua.start.sharesdk.ShareManager;
import com.huahua.start.zxing.decode.Intents;
import com.huahua.util.Logger;
import com.umeng.analytics.MobclickAgent;

import cn.jpush.android.api.JPushInterface;
import cn.sharesdk.framework.ShareSDK;

/**
 * Created by Administrator on 2017/2/13.
 */

public class MyApplication extends Application {

    private static MyApplication sApplication = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.debug("huahua", "MyApplication onCreate >>>>>> ");
        sApplication = this;

        initShareSDK();
        initJPush();
        initUMeng();
    }

    /**
     * 初始化ShareSDK
     */
    public void initShareSDK() {
        ShareManager.initSDK(this);
    }

    /**
     * 初始化极光推送
     */
    public void initJPush() {
        JPushInterface.setDebugMode(true);
        JPushInterface.init(this);
    }

    public void initUMeng() {
        MobclickAgent.setDebugMode(true); // debug模式
        MobclickAgent.openActivityDurationTrack(false); // 不需要跟踪activity的执行轨迹
    }

    public static MyApplication getInstance() {
        return sApplication;
    }
}






















