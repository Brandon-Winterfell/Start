package com.huahua.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.huahua.activity.AdBrowserActivity;
import com.huahua.constant.SDKConstant;
import com.huahua.core.video.VideoAdSlot;
import com.huahua.module.monitor.AdValue;
import com.huahua.okhttp.R;
import com.huahua.report.ReportManager;
import com.huahua.util.Logger;
import com.huahua.util.Utils;

/**
 * Created by Administrator on 2017/2/15.
 * 就是自定义一个Dialog
 * @fucntion 全屏显示视频
 * 全屏显示的Dialog 播放器将放进这个Dialog里面 所以整个视频看起来就是全屏播放
 */

public class VideoFullDialog extends Dialog implements CustomVideoView.ADVideoPlayerListener {

    private static final String TAG = VideoFullDialog.class.getSimpleName();

    /**
     * UI
     */
    private CustomVideoView mVideoView; // 视频播放器
    private RelativeLayout mRootView; //
    private ViewGroup mParentView; // 视频播放器所要放入的父容器
    private ImageView mBackButton; // 返回按钮

    /**
     * Data
     */
    private AdValue mXAdInstance; // 视频信息的实体对象
    private int mPosition; // 从小屏到全屏时视频的播放位置
    private FullToSmallListener mListener; // 与ADVideoPlayerListener接口进行通讯接口回调
    private boolean isFirst = true; // 解决适配时的一个bug
    private Context mContext;
    //动画要执行的平移值
    private int deltaY;
    private VideoAdSlot.AdSDKSlotListener mSlotListener;
    private Bundle mStartBundle;
    private Bundle mEndBundle; //用于Dialog出入场动画


    // 构造函数
    public VideoFullDialog(Context context,
                           CustomVideoView mraidView,
                           AdValue instance,
                           int position) {
        super(context, R.style.dialog_full_screen); // 通过style设置dialog为全屏

        mContext = context;
        mXAdInstance = instance;
        mPosition = position;
        mVideoView = mraidView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.xadsdk_dialog_video_layout);
        // 初始化Dialog中的控件
        initVideoView();
    }

    public void setViewBundle(Bundle bundle) {
        mStartBundle = bundle;
    }

    /**
     * 注入事件监听类
     *
     * 三种方式注入 第一种set方法 第二种构造函数传入 第三种注解库注入
     * @param listener
     */
    public void setListener(FullToSmallListener listener) {
        mListener = listener;
    }

    public void setSlotListener(VideoAdSlot.AdSDKSlotListener slotListener) {
        this.mSlotListener = slotListener;
    }

    /**
     * 初始化Dialog中的控件
     */
    private void initVideoView() {
        mParentView = (RelativeLayout) findViewById(R.id.content_layout);
        mBackButton = (ImageView) findViewById(R.id.xadsdk_player_close_btn);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击返回 TODO 按home键了
                onClickBackBtn();
            }
        });
        mRootView = (RelativeLayout) findViewById(R.id.root_view);
        mRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickVideo();
            }
        });
        mRootView.setVisibility(View.INVISIBLE);

        // 设置事件监听为这个对话框
        mVideoView.setListener(this);
        // 是否静音 false->不静音
        mVideoView.mute(false);
        // 添加到父容器中
        mParentView.addView(mVideoView);
        mParentView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mParentView.getViewTreeObserver().removeOnPreDrawListener(this);
                prepareScene();
                runEnterAnimation();
                return true;
            }
        });
    }

    /**
     * 焦点状态改变时的回调  决定播放/暂停的时机 important
     * @param hasFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Logger.info(TAG, "VideoFullDialog >>> onWindowFocusChanged");

        mVideoView.isShowFullBtn(false); //防止第一次，有些手机仍显示全屏按钮
        if (!hasFocus) { // 未获得焦点时逻辑
            // 保存播放位置 以便从该点续播
            mPosition = mVideoView.getCurrentPosition();
            // 暂停状态
            mVideoView.pauseForFullScreen();
        } else { // 取得焦点时的逻辑
            // 表明，我们的dialog是首次创建且首次获得焦点
            if (isFirst) { //为了适配某些手机不执行seekandresume中的播放方法
                // 恢复播放 续播
                mVideoView.seekAndResume(mPosition);
            } else {
                /**
                 * 要对话框取得焦点时才能够播放
                 * 所以不能直接在构造函数里执行播放操作
                 */
                mVideoView.resume(); // 恢复视频播放
            }
        }
        isFirst = false;
    }

    /**
     * Dialog销毁的时候调用
     */
    @Override
    public void dismiss() {
        Logger.error(TAG, "VideoFullDialog >>> dismiss");
        mParentView.removeView(mVideoView);

        super.dismiss();
    }

    @Override
    public void onBackPressed() {
        onClickBackBtn();
        //super.onBackPressed(); 禁止掉返回键本身的关闭功能,转为自己的关闭效果
    }

    @Override
    public void onClickFullScreenBtn() {
        onClickVideo();
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

    // 全屏返回 关闭按钮点击事件
    @Override
    public void onClickBackBtn() {
        runExitAnimator();
    }

    //准备动画所需数据
    private void prepareScene() {
        mEndBundle = Utils.getViewProperty(mVideoView);
        /**
         * 将desationview移到originalview位置处
         */
        deltaY = (mStartBundle.getInt(Utils.PROPNAME_SCREENLOCATION_TOP)
                - mEndBundle.getInt(Utils.PROPNAME_SCREENLOCATION_TOP));
        mVideoView.setTranslationY(deltaY);
    }

    //准备入场动画
    private void runEnterAnimation() {
        mVideoView.animate()
                .setDuration(200)
                .setInterpolator(new LinearInterpolator())
                .translationY(0)
                .withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        mRootView.setVisibility(View.VISIBLE);
                    }
                })
                .start();
    }

    //准备出场动画
    private void runExitAnimator() {
        mVideoView.animate()
                .setDuration(200)
                .setInterpolator(new LinearInterpolator())
                .translationY(deltaY)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        dismiss(); // 对话框dismiss掉

                        try {
                            ReportManager.exitfullScreenReport(mXAdInstance.event.exitFull.content, mVideoView.getCurrentPosition()
                                    / SDKConstant.MILLION_UNIT);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (mListener != null) {
                            // 传入当前播放位置 然后在小屏状态下续播
                            mListener.getCurrentPlayPosition(mVideoView.getCurrentPosition());
                        }
                    }
                }).start();
    }

    @Override
    public void onAdVideoLoadSuccess() {
        if (mVideoView != null) {
            mVideoView.resume();
        }
    }

    @Override
    public void onAdVideoLoadFailed() {

    }

    /**
     * 与小屏播放时的处理不一样，单独处理
     */
    @Override
    public void onAdVideoLoadComplete() {
        try {
            int position = mVideoView.getDuration() / SDKConstant.MILLION_UNIT;
            ReportManager.sueReport(mXAdInstance.endMonitor, true, position);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 销毁掉dialog
        dismiss();
        // 通知业务逻辑层
        if (mListener != null) {
            mListener.playComplete();
        }
    }

    @Override
    public void onBufferUpdate(int time) {
        try {
            if (mXAdInstance != null) {
                ReportManager.suReport(mXAdInstance.middleMonitor, time / SDKConstant.MILLION_UNIT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClickPlay() {

    }

    /**
     * 与业务逻辑层（VideoAdSlot）进行通信
     */
    public interface FullToSmallListener {
        // 全屏播放中点击关闭按钮或者back键时回调
        void getCurrentPlayPosition(int position);

        void playComplete();//全屏播放结束时回调
    }
}



























