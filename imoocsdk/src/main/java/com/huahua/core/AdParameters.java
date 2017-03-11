package com.huahua.core;

import com.huahua.constant.SDKConstant.AutoPlaySetting;

/**
 * @author: vision
 * @function: 广告SDK全局参数配置, 都用静态来保证统一性
 * @date: 16/7/1
 */
public final class AdParameters {

    //用来记录可自动播放的条件
    // 你默认每次都可以自动播放，我这次设置只wifi下播放
    // 然后第二次进来还是自动播放，这就不够好了
    // 应该放到SharedPreferences里，然后从里面取出来的
    private static AutoPlaySetting currentSetting = AutoPlaySetting.AUTO_PLAY_3G_4G_WIFI; //默认都可以自动播放

    public static void setCurrentSetting(AutoPlaySetting setting) {
        currentSetting = setting;
    }

    public static AutoPlaySetting getCurrentSetting() {
        return currentSetting;
    }

    /**
     * 获取sdk当前版本号
     */
    public static String getAdSDKVersion() {
        return "1.0.0";
    }
}
