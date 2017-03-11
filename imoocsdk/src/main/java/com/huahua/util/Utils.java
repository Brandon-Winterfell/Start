package com.huahua.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.huahua.constant.SDKConstant;

import java.io.ByteArrayInputStream;

/**
 * Created by Administrator on 2017/2/14.
 */

public class Utils {

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale);
    }

    /**
     * wifi是否连接
     * @param context
     * @return
     */
    public static boolean isWifiConnected(Context context) {
        if (context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    //decide can autoplay the ad

    /**
     * 检测能否自动播放广告视频 用户的网络设置跟当前网络是否一致
     *
     * @param context
     * @param setting
     * @return
     */
    public static boolean canAutoPlay(Context context, SDKConstant.AutoPlaySetting setting) {
        boolean result = true;
        switch (setting) {
            case AUTO_PLAY_3G_4G_WIFI:
                result = true;
                break;
            case AUTO_PLAY_ONLY_WIFI:
                if (isWifiConnected(context)) {
                    result = true;
                } else {
                    result = false;
                }
                break;
            case AUTO_PLAY_NEVER:
                result = false;
                break;
        }
        return result;
    }

    /**
     * 获得view在屏幕中出现的百分比
     * @param pView
     * @return
     */
    public static int getVisiblePercent(View pView) {
        if (pView != null && pView.isShown()) {
            DisplayMetrics displayMetrics = pView.getContext().getResources().getDisplayMetrics();
            // 获取屏幕的宽
            int displayWidth = displayMetrics.widthPixels;

            Rect rect = new Rect();
            // 获取当前view在屏幕中出现的一个矩形
            pView.getGlobalVisibleRect(rect);
            if ((rect.top > 0) && (rect.left < displayWidth)) { // view真正出现在屏幕中
                double areaVisible = rect.width() * rect.height();
                double areaTotal = pView.getWidth() * pView.getHeight();
                return (int) ((areaVisible / areaTotal) * 100);
            } else {
                return -1;
            }
        }
        return -1;
    }

    public static boolean containString(String source, String destation) {

        if (source.equals("") || destation.equals("")) {
            return false;
        }

        if (source.contains(destation)) {
            return true;
        }
        return false;
    }

    public static BitmapDrawable decodeImage(String base64drawable) {
        byte[] rawImageData = Base64.decode(base64drawable, 0);
        return new BitmapDrawable(null, new ByteArrayInputStream(rawImageData));
    }

    public static DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            return displayMetrics;
        }
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }


    public static final String VIEW_INFO_EXTRA = "view_into_extra";
    public static final String PROPNAME_SCREENLOCATION_LEFT = "propname_sreenlocation_left";
    public static final String PROPNAME_SCREENLOCATION_TOP = "propname_sreenlocation_top";
    public static final String PROPNAME_WIDTH = "propname_width";
    public static final String PROPNAME_HEIGHT = "propname_height";

    /**
     * 获取view的屏幕属性
     *
     * @param view
     * @return
     */
    public static Bundle getViewProperty(View view) {
        Bundle bundle = new Bundle();
        int[] screenLocation = new int[2];
        view.getLocationOnScreen(screenLocation); //获取view在整个屏幕中的位置
        bundle.putInt(PROPNAME_SCREENLOCATION_LEFT, screenLocation[0]);
        bundle.putInt(PROPNAME_SCREENLOCATION_TOP, screenLocation[1]);
        bundle.putInt(PROPNAME_WIDTH, view.getWidth());
        bundle.putInt(PROPNAME_HEIGHT, view.getHeight());

        Log.e("Utils", "Left: " + screenLocation[0] + " Top: " + screenLocation[1]
                + " Width: " + view.getWidth() + " Height: " + view.getHeight());
        return bundle;
    }

    public static boolean isPad(Context context) {

        //如果能打电话那可能是平板或手机，再根据配置判断
        if (canTelephone(context)) {
            //能打电话可能是手机也可能是平板
            return (context.getResources().getConfiguration().
                    screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                    >= Configuration.SCREENLAYOUT_SIZE_LARGE;
        } else {
            return true; //不能打电话一定是平板
        }
    }

    private static boolean canTelephone(Context context) {
        TelephonyManager telephony = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        return (telephony.getPhoneType() == TelephonyManager.PHONE_TYPE_NONE) ? false : true;
    }

    /**
     * 获取对应应用的版本号
     *
     * @param context
     * @return
     */
    public static String getAppVersion(Context context) {
        String version = "1.0.0"; //默认1.0.0版本
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            version = info.versionName;
        } catch (Exception e) {
        }

        return version;
    }

    /**
     * 显示系统软件盘
     * 传入的View必须是EditText及其子类才可以强制显示出
     */
    public static void showSoftInputMethod(Context context, View v) {
        /* 隐藏软键盘 */
        InputMethodManager inputMethodManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_FORCED);
    }

    public static void hideSoftInputMethod(Context context, View v) {
        /* 隐藏软键盘 */
        InputMethodManager inputMethodManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

}


























