package com.huahua.start.activity.base;

import android.support.annotation.ColorRes;
import android.support.v7.app.AppCompatActivity;

import com.huahua.start.util.StatusBarUtil;

/**
 * Created by Administrator on 2017/2/14.
 * @function: 所有Activity的基类，用来处理一些公共事件，如：数据统计
 */

public class BaseActivity  extends AppCompatActivity {

    /**
     * 改变状态栏颜色
     *
     * @param color
     */
    public void changeStatusBarColor(@ColorRes int color) {
        StatusBarUtil.setStatusBarColor(this, color);
    }

}



























