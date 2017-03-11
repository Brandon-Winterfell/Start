package com.huahua.core.video;

import android.content.Intent;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.huahua.activity.AdBrowserActivity;
import com.huahua.core.AdContextInterface;
import com.huahua.module.monitor.AdValue;
import com.huahua.okhttp.HttpConstant;
import com.huahua.report.ReportManager;
import com.huahua.util.Utils;
import com.huahua.widget.CustomVideoView;

/**
 * Created by Administrator on 2017/2/24.
 * @function: 管理slot，与外界进行通信
 *
 * API层并不完成什么具体的实际功能，他只是对外暴露一些功能或者接口
 * 来告诉我们的调用者，我为你提供了什么功能，可以供你使用
 * 而具体的实现了，我们的API层会调用具体的功能模块去实现。
 * 外观模式
 * 没有实现任何功能，就是一个包装。
 *
 * 调用者不关心其他方法，假如直接用VideoAdSlot类（业务层）的话
 * 能看到很多方法的话，会让他很困惑的，那些方法干什么用的
 * 他只关心实现功能的方法是什么
 *
 * 其实就封多层而已，以前觉得这样会不会过度设计了 哈哈
 * 将不必要的隐藏了（封装思想），将需要用的暴露给外界
 *
 * 这叫SDK了
 */

public class VideoAdContext implements VideoAdSlot.AdSDKSlotListener {

    //the ad container
    private ViewGroup mParentView;

    private VideoAdSlot mAdSlot;
    private AdValue mInstance = null;
    //the listener to the app layer
    private AdContextInterface mListener;
    private CustomVideoView.ADFrameImageLoadListener mFrameLoadListener;

    /**
     *
     * @param parentView
     * @param instance 讲师不直接传入json对象而传入String基本数据类型结构，
     *                 是为了通用性更强，外界调用代价更小，他不用非得转化成我们的实体，我们解析即可
     *                 可是你还得解析了，网一解析出错了
     * @param frameLoadListener
     */
    public VideoAdContext(ViewGroup parentView,
                          String instance,
                          CustomVideoView.ADFrameImageLoadListener frameLoadListener) {
        mParentView = parentView;
        Gson gson = new Gson();
        // json字符串到实体对象的转化
        // 如果解析失败了 使用mInstance时有判断是否为空
        mInstance = gson.fromJson(instance, AdValue.class);
        // 这里要判断frameLoadListener是否为空吧 看你业务以及代码罗
        mFrameLoadListener = frameLoadListener;

        load();
    }

    /**
     * 创建Slot业务逻辑类，不调用则不会创建最底层的CustomVideoView
     * init the ad,不调用则不会创建videoview
     */
    private void load() {
        if (mInstance != null && mInstance.resource != null) {
            mAdSlot = new VideoAdSlot(mInstance, this, mFrameLoadListener);
            //发送解析成功事件
            sendAnalizeReport(HttpConstant.Params.ad_analize, HttpConstant.AD_DATA_SUCCESS);
        } else {
            mAdSlot = new VideoAdSlot(null, this, mFrameLoadListener); //创建空的slot,不响应任何事件
            if (mListener != null) {
                mListener.onAdFailed();
            }
            sendAnalizeReport(HttpConstant.Params.ad_analize, HttpConstant.AD_DATA_FAILED);
        }
    }

    /**
     * release the ad
     */
    private void destroy() {
        mAdSlot.destroy();
    }

    /**
     * 我觉得放到构造函数更直观
     * @param listener
     */
    public void setAdResultListener(AdContextInterface listener) {
        this.mListener = listener;
    }

    /**
     * 根据滑动距离来判断是否可以自动播放, 出现超过50%自动播放，离开超过50%,自动暂停
     *
     * 这是我们SDK对外提供的方法
     */
    public void updateAdInScrollView() {
        if (mAdSlot != null) {
            mAdSlot.updateAdInScrollView();
        }
    }

    /**
     * 下面这些呢就是回调事件的实现，这些方法也不需要外界知道
     * 将修饰符改为private 或者 protected，
     * 这样外界能点出来的方法就只有上面那个updateAdInScrollView了
     * 因为我们只提供了一个滑动暂停/播放API
     *
     * 不可以改为private了 报错
     * @return
     */

    @Override
    public ViewGroup getAdParent() {
        return mParentView;
    }

    @Override
    public void onAdVideoLoadSuccess() {
        if (mListener != null) {
            mListener.onAdSuccess();
        }
        sendAnalizeReport(HttpConstant.Params.ad_load, HttpConstant.AD_PLAY_SUCCESS);
    }

    @Override
    public void onAdVideoLoadFailed() {
        if (mListener != null) {
            mListener.onAdFailed();
        }
        sendAnalizeReport(HttpConstant.Params.ad_load, HttpConstant.AD_PLAY_FAILED);
    }

    @Override
    public void onAdVideoLoadComplete() {

    }

    @Override
    public void onClickVideo(String url) {
        if (mListener != null) {
            mListener.onClickVideo(url);
        } else {
            Intent intent = new Intent(mParentView.getContext(), AdBrowserActivity.class);
            intent.putExtra(AdBrowserActivity.KEY_URL, url);
            mParentView.getContext().startActivity(intent);
        }
    }

    private void sendAnalizeReport(HttpConstant.Params step, String result) {
        try {
            ReportManager.sendAdMonitor(
                    Utils.isPad(mParentView.getContext().getApplicationContext()),
                    mInstance == null ? "" : mInstance.resourceID,
                    (mInstance == null ? null : mInstance.adid),
                    Utils.getAppVersion(mParentView.getContext().getApplicationContext()),
                    step,
                    result);
        } catch (Exception e) {

        }
    }

}


























