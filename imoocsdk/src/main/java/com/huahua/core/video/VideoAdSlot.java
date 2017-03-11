package com.huahua.core.video;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.huahua.activity.AdBrowserActivity;
import com.huahua.constant.SDKConstant;
import com.huahua.core.AdParameters;
import com.huahua.module.monitor.AdValue;
import com.huahua.report.ReportManager;
import com.huahua.util.Utils;
import com.huahua.widget.CustomVideoView;
import com.huahua.widget.VideoFullDialog;

/**
 * Created by Administrator on 2017/2/15.
 * 实现自定义播放器的    因为要接口回调 这一层（业务逻辑层）要调用播放器的方法
 *
 * @function: 视频广告业务逻辑层
 */

public class VideoAdSlot implements CustomVideoView.ADVideoPlayerListener {

    private Context mContext;
    /**
     * UI
     */
    private CustomVideoView mVideoView;
    // CustomVideoView所在的父容器
    private ViewGroup mParentView;

    /**
     * Data
     */
    private AdValue mXAdInstance; // 视频实体类
    private AdSDKSlotListener mSlotListener; // 接口回调
    private boolean canPause = false; //是否可自动暂停标志位
    private int lastArea = 0; //防止将要滑入滑出时播放器的状态改变

    /**
     * 构造方法
     * @param adInstance
     * @param slotLitener
     * @param frameLoadListener
     */
    public VideoAdSlot(AdValue adInstance, AdSDKSlotListener slotLitener, CustomVideoView.ADFrameImageLoadListener frameLoadListener) {
        mXAdInstance = adInstance;
        mSlotListener = slotLitener;
        mParentView = slotLitener.getAdParent();
        mContext = mParentView.getContext();
        initVideoView(frameLoadListener);
    }

    private void initVideoView(CustomVideoView.ADFrameImageLoadListener frameImageLoadListener) {
        mVideoView = new CustomVideoView(mContext, mParentView);
        if (mXAdInstance != null) {
            mVideoView.setDataSource(mXAdInstance.resource);
            mVideoView.setFrameURI(mXAdInstance.thumb);
            mVideoView.setFrameLoadListener(frameImageLoadListener);
            mVideoView.setListener(this);
        }

        RelativeLayout paddingView = new RelativeLayout(mContext);
        paddingView.setBackgroundColor(mContext.getResources().getColor(android.R.color.black));
        paddingView.setLayoutParams(mVideoView.getLayoutParams());
        mParentView.addView(paddingView);
        mParentView.addView(mVideoView);
    }

    private boolean isPlaying() {
        if (mVideoView != null) {
            return mVideoView.isPlaying();
        }

        return false;
    }

    private boolean isRealPause() {
        if (mVideoView != null) {
            return mVideoView.isRealPause();
        }

        return false;
    }

    private boolean isComplete() {
        if (mVideoView != null) {
            return mVideoView.isComplete();
        }

        return false;
    }

    private void pauseVideo(boolean isAuto) {
        if (mVideoView != null) {
            if (isAuto) {
                // 发自动暂停监测
                if (!isRealPause() && isPlaying()) {
                    try {
                        ReportManager.pauseVideoReport(mXAdInstance.event.pause.content, getPosition());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            mVideoView.seekAndPause(0);
        }
    }

    // resume the video
    private void resumeVideo() {
        if (mVideoView != null) {
            mVideoView.resume();
            if (isPlaying()) {
                sendSUSReport(true); // 发自动播放监测
            }
        }
    }

    /**
     * 实现滑入播放，滑出暂停功能
     * 自己是无法知道外界是否滑动的
     * 如果调用方想用这个功能，他需要在ListView或者GridView中调用
     */
    public void updateAdInScrollView() {
        // 当前parentview在当前屏幕的百分比
        int currentArea = Utils.getVisiblePercent(mParentView);

        //小于0表示未出现在屏幕上，不做任何处理
        if (currentArea <= 0) {
            return;
        }
        //刚要滑入和滑出时，异常状态的处理
        if (Math.abs(currentArea - lastArea) >= 100) {
            return;
        }

        // 滑动没有超过屏幕50%的时候
        if (currentArea < SDKConstant.VIDEO_SCREEN_PERCENT) {
            //进入自动暂停状态
            if (canPause) {
                pauseVideo(true);
                canPause = false; // 滑动事件过滤
            }
            lastArea = 0;
            mVideoView.setIsComplete(false); // 滑动出50%后标记为从头开始播
            mVideoView.setIsRealPause(false); //以前叫setPauseButtonClick()
            return;
        }

        // 当视频进入真正的暂停状态时
        if (isRealPause() || isComplete()) {
            //进入手动暂停或者播放结束，播放结束和不满足自动播放条件都作为手动暂停
            pauseVideo(false);
            canPause = false;
            return;
        }

        //满足自动播放条件(用户设置的)或者用户主动点击播放，开始播放
        if (Utils.canAutoPlay(mContext, AdParameters.getCurrentSetting())
                || isPlaying()) {
            lastArea = currentArea;
            // 真正地去播放视频
            resumeVideo();
            canPause = true;
            mVideoView.setIsRealPause(false);
        } else {
            // 不满足用户条件设置
            pauseVideo(false);
            mVideoView.setIsRealPause(true); //不能自动播放则设置为手动暂停效果
        }
    }

    public void destroy() {
        mVideoView.destroy();
        mVideoView = null;
        mContext = null;
        mXAdInstance = null;
    }

    // 实现play层接口
    /**
     * 实现从小屏到全屏播放功能接口
     */
    @Override
    public void onClickFullScreenBtn() {

        try {
            ReportManager.fullScreenReport(mXAdInstance.event.full.content, getPosition());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //获取videoview在当前界面的属性
        Bundle bundle = Utils.getViewProperty(mParentView);

        // 将播放器从View树中移除
        mParentView.removeView(mVideoView);
        // 创建全屏播放dialog
        VideoFullDialog dialog = new VideoFullDialog(
                mContext, mVideoView, mXAdInstance, mVideoView.getCurrentPosition()
        );

        // 设置dialog
        // 添加监听
        dialog.setListener(new VideoFullDialog.FullToSmallListener() {
            @Override
            public void getCurrentPlayPosition(int position) {
                // 在全屏视频播放的时候点击了返回
                // 全屏 == >> 小屏
                backToSmallMode(position);
            }

            @Override
            public void playComplete() {
                // 在全屏视频播放完成后
                bigPlayComplete();
            }
        });
        dialog.setViewBundle(bundle); //为Dialog设置播放器数据Bundle对象
        dialog.setSlotListener(mSlotListener);
        // 将dialog显示出来
        dialog.show();

    }

    /**
     * 在全屏视频播放的时候点击了返回按钮的事件回调
     * @param position
     */
    private void backToSmallMode(int position) {
        // 一个view只能有一个parent 所以进行判断
        if (mVideoView.getParent() == null) {
            mParentView.addView(mVideoView);
        }

        mVideoView.setTranslationY(0); //防止动画导致偏离父容器
        // 显示全屏按钮
        mVideoView.isShowFullBtn(true);
        // 小屏的时候静音播放
        mVideoView.mute(true);
        // 为业务逻辑层重新设置监听（覆盖事件监听）
        mVideoView.setListener(this);
        // 播放器跳到指定位置并进行播放
        mVideoView.seekAndResume(position);
        canPause = true; // 标为可自动暂停
    }

    /**
     * 全屏播放结束时的事件回调
     */
    private void bigPlayComplete() {
        // 一个view只能有一个parent 所以进行判断
        if (mVideoView.getParent() == null) {
            mParentView.addView(mVideoView);
        }

        mVideoView.setTranslationY(0); //防止动画导致偏离父容器
        mVideoView.isShowFullBtn(true);
        mVideoView.mute(true);
        mVideoView.setListener(this);
        mVideoView.seekAndPause(0);
        canPause = false;
    }

    @Override
    public void onClickVideo() {
        String desationUrl = mXAdInstance.clickUrl;

        if (mSlotListener != null) {
            if (mVideoView.isFrameHidden() && !TextUtils.isEmpty(desationUrl)) {
                mSlotListener.onClickVideo(desationUrl);
                try {
                    ReportManager.pauseVideoReport(mXAdInstance.clickMonitor, mVideoView.getCurrentPosition()
                            / SDKConstant.MILLION_UNIT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            //走默认样式
            if (mVideoView.isFrameHidden() && !TextUtils.isEmpty(desationUrl)) {
                // 跳转到webview页面
                Intent intent = new Intent(mContext, AdBrowserActivity.class);
                intent.putExtra(AdBrowserActivity.KEY_URL, mXAdInstance.clickUrl);
                mContext.startActivity(intent);
                try {
                    ReportManager.pauseVideoReport(mXAdInstance.clickMonitor, mVideoView.getCurrentPosition()
                            / SDKConstant.MILLION_UNIT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onClickBackBtn() {

    }

    @Override
    public void onClickPlay() {
        sendSUSReport(false);
    }

    @Override
    public void onAdVideoLoadSuccess() {
        // 通知最外层视频加载成功了
        // 这里的接口回调一共有两层 一层是customview传到这里的videoadslot
        // 另一层是 这里 传到外面API层
        if (mSlotListener != null) {
            mSlotListener.onAdVideoLoadSuccess();
        }
    }

    @Override
    public void onAdVideoLoadFailed() {
        if (mSlotListener != null) {
            mSlotListener.onAdVideoLoadFailed();
        }

        //加载失败全部回到初始状态
        canPause = false;
    }

    @Override
    public void onAdVideoLoadComplete() {
        try {
            // 发送一个（sue检测）网络请求
            ReportManager.sueReport(mXAdInstance.endMonitor, false, getDuration());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mSlotListener != null) {
            mSlotListener.onAdVideoLoadComplete();
        }
        mVideoView.setIsRealPause(true);
    }

    @Override
    public void onBufferUpdate(int time) {
        try {
            // 这里是发送一个su类型的监测的网络请求
            ReportManager.suReport(mXAdInstance.middleMonitor, time / SDKConstant.MILLION_UNIT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 获取播放到哪个位置
    private int getPosition() {
        return mVideoView.getCurrentPosition() / SDKConstant.MILLION_UNIT;
    }

    // 获取这个视频总共有多长时间
    private int getDuration() {
        return mVideoView.getDuration() / SDKConstant.MILLION_UNIT;
    }

    /**
     * 发送视频开始播放监测
     *
     * @param isAuto
     */
    private void sendSUSReport(boolean isAuto) {
        try {
            ReportManager.susReport(mXAdInstance.startMonitor, isAuto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //传递消息到appcontext层   外面的api层
    public interface AdSDKSlotListener {

        public ViewGroup getAdParent();

        public void onAdVideoLoadSuccess();

        public void onAdVideoLoadFailed();

        public void onAdVideoLoadComplete();

        public void onClickVideo(String url);
    }
}
































