package com.huahua.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.huahua.constant.SDKConstant;
import com.huahua.core.AdParameters;
import com.huahua.okhttp.R;
import com.huahua.util.Logger;
import com.huahua.util.Utils;

/**
 * Created by Administrator on 2017/2/15.
 * @fuction 负责视频(广告)播放，暂停以及各类事件的触发
 * load方法是整个播放器的播放入口 onSurfaceTextureAvailable方法去调用
 *
 * 讲师继承自RelativeLayout，可以继承自任何一个ViewGroup，讲师是习惯而已，
 * 主要是一个ViewGroup能添加进其他父容器即可
 *
 * 继承需要的Listener
 *
 * 所以现在看来也就是这些接口的回调而已
 *
 * 在onSurfaceTextureAvailable 就是说TextureView准备好了就可以调用load()方法了
 */

public class CustomVideoView extends RelativeLayout implements View.OnClickListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener,
        TextureView.SurfaceTextureListener, MediaPlayer.OnInfoListener {

    /**
     * Constant
     */
    private static final String TAG = "MraidVideoView";
    // 事件类型 handler中使用
    private static final int TIME_MSG = 0x01;
    // 时间间隔 每隔一秒执行一次handler的handle message
    private static final int TIME_INVAL = 1000;
    // 当前播放器状态的标识
    private static final int STATE_ERROR = -1; // 出错
    private static final int STATE_IDLE = 0; // 空闲
    private static final int STATE_PLAYING = 1; // 正在播放
    private static final int STATE_PAUSING = 2; // 暂停
    // 加载失败 重试机制 重试3次 提高视频加载成功率
    private static final int LOAD_TOTAL_COUNT = 3;

    /**
     * UI
     */
    // videoview要添加到哪个父容器中
    private ViewGroup mParentContainer;
    // 当前布局的relativelayout
    private RelativeLayout mPlayerView;
    // 显示帧数据上去的那个view
    private TextureView mVideoView;
    // 功能按钮
    private Button mMiniPlayBtn;
    private ImageView mFullBtn;
    private ImageView mLoadingBar;
    private ImageView mFrameView;
    // 音频播放器 是否静音
    private AudioManager audioManager;
    // 真正显示帧数据的类 最终显示帧数据的类
    private Surface videoSurface;

    /**
     * Data
     */
    // 要加载的视频地址
    private String mUrl;
    private String mFrameURI;
    // 是否静音
    private boolean isMute;
    // 宽是屏幕的宽度 高是16:9算出来的高度
    private int mScreenWidth, mDestationHeight;


    /**
     * Status状态保护
     */
    private boolean canPlay = true;
    private boolean mIsRealPause;
    private boolean mIsComplete;
    private int mCurrentCount;
    // 标记处于哪个状态 默认是IDLE状态(空闲状态)
    private int playerState = STATE_IDLE;

    // 播放器的核心类
    private MediaPlayer mediaPlayer;
    // 事件监听回调 通知外界发生了什么事件 由外界监听并处理响相应的逻辑
    // 自己定义的内部接口
    private ADVideoPlayerListener listener;
    // 接收屏幕是否锁屏的广播
    private ScreenEventReceiver mScreenReceiver;
    // 每隔TIME_INVAL（1秒）时间发送TIME_MSG事件执行一下listener.onBufferUpdate
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIME_MSG:
                    if (isPlaying()) {
                        //还可以在这里更新progressbar
                        //LogUtils.i(TAG, "TIME_MSG");
                        if (listener != null) {
                            listener.onBufferUpdate(getCurrentPosition());
                            sendEmptyMessageDelayed(TIME_MSG, TIME_INVAL);
                        }
                        Logger.error(TAG, "handlMessage listener >>>> null");
                    }
                    break;
            }
        }
    };



    private ADFrameImageLoadListener mFrameLoadListener;

    /**
     * 构造方法 --- >>>>> 初始化工作
     * @param context
     * @param parentContainer videoview要添加的父容器
     */
    public CustomVideoView(Context context, ViewGroup parentContainer) {
        super(context); // 调用父类的一个参数的构造方法
        mParentContainer = parentContainer;
        audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        initData();
        initView();
        // 注册广播接收器
        registerBroadcastReceiver();
    }

    // 获取 计算 屏幕的宽和默认视频播放器的高 宽是屏幕的宽 高是屏幕的16分之9
    private void initData() {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mDestationHeight = (int) (mScreenWidth * SDKConstant.VIDEO_HEIGHT_PERCENT);
    }

    /**
     * 主要是findviewbyid工作，自定义xml布局文件的控件
     */
    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(this.getContext());
        mPlayerView = (RelativeLayout) inflater.inflate(R.layout.xadsdk_video_player, this);

        mVideoView = (TextureView) mPlayerView.findViewById(R.id.xadsdk_player_video_textureView);
        mVideoView.setOnClickListener(this);
        mVideoView.setKeepScreenOn(true);
        mVideoView.setSurfaceTextureListener(this);

        initSmallLayoutMode(); //init the small mode
    }

    // 小模式状态
    private void initSmallLayoutMode() {
        LayoutParams params = new LayoutParams(mScreenWidth, mDestationHeight);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mPlayerView.setLayoutParams(params);

        mMiniPlayBtn = (Button) mPlayerView.findViewById(R.id.xadsdk_small_play_btn);
        mFullBtn = (ImageView) mPlayerView.findViewById(R.id.xadsdk_to_full_view);
        mLoadingBar = (ImageView) mPlayerView.findViewById(R.id.loading_bar);
        mFrameView = (ImageView) mPlayerView.findViewById(R.id.framing_view);
        mMiniPlayBtn.setOnClickListener(this);
        mFullBtn.setOnClickListener(this);
    }

    public void isShowFullBtn(boolean isShow) {
        mFullBtn.setImageResource(isShow ? R.drawable.xadsdk_ad_mini : R.drawable.xadsdk_ad_mini_null);
        mFullBtn.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public boolean isRealPause() {
        return mIsRealPause;
    }

    public boolean isComplete() {
        return mIsComplete;
    }

    // 所有view都有的生命状态方法 view中的方法 在view的显示发生改变时，回调此方法
    // 显示 到 不显示 ； 不显示 到 显示 状态的改变时候 会回调这个方法
    // 回到桌面
    // 可见 <--> 不可见  实现播放或者暂停的逻辑
    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        Logger.error(TAG, "onVisibilityChanged >>>>  " + visibility);
        super.onVisibilityChanged(changedView, visibility);

        if (visibility == VISIBLE && playerState == STATE_PAUSING) {
            // 决定是否播放
            if (isRealPause() || isComplete()) {
                // 表明播放器进入了真正的暂停状态
                pause();
            } else {
                decideCanPlay();
            }
        } else {
            pause();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        Logger.info(TAG, "onDetachedFromWindow >>>>  " );
        super.onDetachedFromWindow();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
        // 只要触摸事件传到视频播放器中表示消耗掉事件
        // 主要是用于防止与父容器产生冲突
    }

    /**
     * true is no voice
     *
     * @param mute
     */
    public void mute(boolean mute) {
        Logger.debug(TAG, "mute");
        isMute = mute;
        if (mediaPlayer != null && this.audioManager != null) {
            float volume = isMute ? 0.0f : 1.0f;
            mediaPlayer.setVolume(volume, volume);
        }
    }

    public boolean isPlaying() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            return true;
        }
        return false;
    }

    public boolean isFrameHidden() {
        return mFrameView.getVisibility() == View.VISIBLE ? false : true;
    }

    public void setIsComplete(boolean isComplete) {
        mIsComplete = isComplete;
    }

    public void setIsRealPause(boolean isRealPause) {
        this.mIsRealPause = isRealPause;
    }

    // 点击播放 或者 点击全屏  的事件
    @Override
    public void onClick(View v) {
        if (v == this.mMiniPlayBtn) {
            if (this.playerState == STATE_PAUSING) {
                if (Utils.getVisiblePercent(mParentContainer)
                        > SDKConstant.VIDEO_SCREEN_PERCENT) {
                    resume();
                    this.listener.onClickPlay();
                }
            } else {
                load();
            }
        } else if (v == this.mFullBtn) {
            this.listener.onClickFullScreenBtn();
        } else if (v == mVideoView) {
            this.listener.onClickVideo();
        }
    }

    // 播放器播放完成后回调此方法
    @Override
    public void onCompletion(MediaPlayer mp) {

        // handler发送事件通知外界 当前播放器播放完成了
        if (listener != null) {
            listener.onAdVideoLoadComplete();
        }

        // 回到初始状态
        playBack();

        // 改变状态标识
        setIsComplete(true);
        // 两种暂停状态 一种是画出屏幕后 回来的话会继续自动播放
        // 另一种是播放完了 真正进入暂停状态 不会再自动播放了
        setIsRealPause(true);
    }

    // 播放器产生异常时回调
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {

        Logger.error(TAG, "do error:" + what);
        // 设置error状态
        this.playerState = STATE_ERROR;

        mediaPlayer = mp;
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        }

        if (mCurrentCount >= LOAD_TOTAL_COUNT) {
            showPauseView(false);
            if (this.listener != null) {
                listener.onAdVideoLoadFailed();
            }
        }
        this.stop();//去重新load  stop方法里还有个判断是否重试次数够了 所以不一定会重新加载
        return true;

        // 返回true，android系统认为你处理了这个异常，不会再帮你处理了
        // 返回false 默认，它认为你的MediaPlayer没有处理这个异常事件，它会进行默认处理
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return true;
    }

    /**
     * load()是入口方法，成功加载好的话然后就进入这里
     * 表示视频已经准备好了，可以调用播放状态了
     * MediaPlayer通知我们，播放器处于就绪状态 这时候调用onStart去播放，才会有效果
     * 异步回调嘛
     *
     * 就是去执行播放逻辑
     * @param mp
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        Logger.info(TAG, "onPrepared");
        showPlayView();
        mediaPlayer = mp;
        if (mediaPlayer != null) {
            mediaPlayer.setOnBufferingUpdateListener(this);
            mCurrentCount = 0;
            if (listener != null) {
                listener.onAdVideoLoadSuccess();
            }
            //满足自动播放条件（2个条件），则直接播放
            // 播放器能见 并且能看见百分之五十 ；
            // 当前网络与用户的设置是否一致
            if (Utils.canAutoPlay(getContext(),
                    AdParameters.getCurrentSetting()) &&
                    Utils.getVisiblePercent(mParentContainer) > SDKConstant.VIDEO_SCREEN_PERCENT) {
                setCurrentPlayState(STATE_PAUSING);

                resume();
            } else {
                setCurrentPlayState(STATE_PLAYING);
                pause();
            }
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    public void setDataSource(String url) {
        this.mUrl = url;
    }

    public void setFrameURI(String url) {
        mFrameURI = url;
    }

    /**
     * 整个视频播放器的入口方法
     * 加载视频url
     */
    public void load() {
        if (this.playerState != STATE_IDLE) {
            return;
        }
        Logger.debug(TAG, "do play url = " + this.mUrl);

        showLoadingView();

        try {
            setCurrentPlayState(STATE_IDLE);
            checkMediaPlayer();
            mute(true);

            mediaPlayer.setDataSource(this.mUrl);
            mediaPlayer.prepareAsync(); //开始异步加载
            // 异步加载成功后，会调用 播放器的onPrepared()回调方法
        } catch (Exception e) {
            Logger.error(TAG, e.getMessage());
            stop(); //error以后重新调用stop加载
        }
    }

    /**
     * 暂停视频
     */
    public void pause() {
        // 如果不是正在play，直接返回
        if (this.playerState != STATE_PLAYING) {
            return;
        }

        Logger.debug(TAG, "do pause");
        // 设置状态
        setCurrentPlayState(STATE_PAUSING);
        if (isPlaying()) {
            // 真正的完成暂停
            mediaPlayer.pause();
            if (!this.canPlay) {
                this.mediaPlayer.seekTo(0);
            }
        }
        // 显示暂停相关的UI
        this.showPauseView(false);
        // 移除事件发送 只有在播放状态下才发送事件 其他状态都移除掉事件发送
        mHandler.removeCallbacksAndMessages(null);
    }


    //全屏不显示暂停状态,后续可以整合，不必单独出一个方法
    public void pauseForFullScreen() {
        if (playerState != STATE_PLAYING) {
            return;
        }
        Logger.debug(TAG, "do full pause");
        setCurrentPlayState(STATE_PAUSING);
        if (isPlaying()) {
            mediaPlayer.pause();
            if (!this.canPlay) {
                mediaPlayer.seekTo(0);
            }
        }
        mHandler.removeCallbacksAndMessages(null);
    }

    public int getCurrentPosition() {
        if (this.mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    // 跳到指定点播放视频
    public void seekAndResume(int position) {
        if (mediaPlayer != null) {
            showPauseView(true);
            entryResumeState();
            mediaPlayer.seekTo(position);
            mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mp) {
                    Logger.info(TAG, "do seek and resume");
                    mediaPlayer.start();
                    mHandler.sendEmptyMessage(TIME_MSG);
                }
            });
        }
    }

    // 跳到指定点暂停视频
    public void seekAndPause(int position) {
        if (this.playerState != STATE_PLAYING) {
            return;
        }
        showPauseView(false);
        setCurrentPlayState(STATE_PAUSING);
        if (isPlaying()) {
            mediaPlayer.seekTo(position);
            mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mp) {
                    Logger.info(TAG, "do seek and pause");
                    mediaPlayer.pause();
                    mHandler.removeCallbacksAndMessages(null);
                }
            });
        }
    }

    /**
     * 恢复视频播放
     * 然后就是两种情况 一种是顺利播放完成视频 onCompletion回调
     * 另一种是发生了错误 onError回调
     */
    public void resume() {
        if (this.playerState != STATE_PAUSING) {
            return;
        }
        Logger.debug(TAG, "do resume");
        if (!isPlaying()) {
            entryResumeState(); // 进入播放状态时的状态更新
            mediaPlayer.setOnSeekCompleteListener(null);
            mediaPlayer.start(); // 真正开始播放
            mHandler.sendEmptyMessage(TIME_MSG); // handler发送事件
            showPauseView(true);
        } else {
            showPauseView(false);
        }
    }

    /**
     * 进入播放状态时的状态更新
     */
    private void entryResumeState() {
        canPlay = true;
        setCurrentPlayState(STATE_PLAYING);
        setIsRealPause(false);
        setIsComplete(false);
    }

    private void setCurrentPlayState(int state) {
        playerState = state;
    }

    // 播放完成后回到初始状态
    // 播放完成后不把播放器销毁 而是把播放流跳转到0 处于暂停状态
    public void playBack() {
        Logger.debug(TAG, " do playBack");
        // 设置为暂停状态
        setCurrentPlayState(STATE_PAUSING);
        // 移除事件发送
        mHandler.removeCallbacksAndMessages(null);
        // 重置mediaPlayer
        if (mediaPlayer != null) {
            mediaPlayer.setOnSeekCompleteListener(null);
            // 跳到第0秒 播放流回到零点
            mediaPlayer.seekTo(0);
            // 暂停状态
            mediaPlayer.pause();
        }

        // UI显示暂停状态相关的view
        this.showPauseView(false);
    }

    /**
     * 停止状态 -- >> prepare
     */
    public void stop() {
        Logger.debug(TAG, " do stop");
        if (this.mediaPlayer != null) {
            this.mediaPlayer.reset();
            this.mediaPlayer.setOnSeekCompleteListener(null);
            this.mediaPlayer.stop();
            this.mediaPlayer.release();
            this.mediaPlayer = null;
        }
        // 移除事件发送
        mHandler.removeCallbacksAndMessages(null);
        // 设置当前播放器状态 空闲状态
        setCurrentPlayState(STATE_IDLE);
        // 重试 LOAD_TOTAL_COUNT 次数
        if (mCurrentCount < LOAD_TOTAL_COUNT) { //满足重新加载的条件
            mCurrentCount += 1;
            load();
        } else {
            showPauseView(false); //显示暂停状态
        }
    }

    /**
     * 销毁当前的自定义view（videoview） 也会销毁事件监听
     * 相当于activity的onDestroy方法
     */
    public void destroy() {
        Logger.debug(TAG, " do destroy");
        if (this.mediaPlayer != null) {
            this.mediaPlayer.setOnSeekCompleteListener(null);
            this.mediaPlayer.stop();
            this.mediaPlayer.release();
            this.mediaPlayer = null;
        }
        setCurrentPlayState(STATE_IDLE);
        mCurrentCount = 0;
        setIsComplete(false);
        setIsRealPause(false);
        unRegisterBroadcastReceiver();
        mHandler.removeCallbacksAndMessages(null); //release all message and runnable
        showPauseView(false); //除了播放和loading外其余任何状态都显示pause
    }

    /**
     * 添加回调
     * 就是调用外界传进来的listener实例的方法
     * @param listener
     */
    public void setListener(ADVideoPlayerListener listener) {
        this.listener = listener;
    }

    public void setFrameLoadListener(ADFrameImageLoadListener frameLoadListener) {
        this.mFrameLoadListener = frameLoadListener;
    }

    private synchronized void checkMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = createMediaPlayer(); //每次都重新创建一个新的播放器
        }
    }

    private MediaPlayer createMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.reset();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        if (videoSurface != null && videoSurface.isValid()) {
            mediaPlayer.setSurface(videoSurface);
        } else {
            stop();
        }
        return mediaPlayer;
    }

    private void showPauseView(boolean show) {
        mFullBtn.setVisibility(show ? View.VISIBLE : View.GONE);
        mMiniPlayBtn.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoadingBar.clearAnimation();
        mLoadingBar.setVisibility(View.GONE);
        if (!show) {
            mFrameView.setVisibility(View.VISIBLE);
            loadFrameImage();
        } else {
            mFrameView.setVisibility(View.GONE);
        }
    }

    /**
     * 正在加载的动画提示用户
     */
    private void showLoadingView() {
        mFullBtn.setVisibility(View.GONE);
        mLoadingBar.setVisibility(View.VISIBLE);
        AnimationDrawable anim = (AnimationDrawable) mLoadingBar.getBackground();
        anim.start();
        mMiniPlayBtn.setVisibility(View.GONE);
        mFrameView.setVisibility(View.GONE);
        loadFrameImage();
    }

    /**
     * 异步加载定帧图
     */
    private void loadFrameImage() {
        if (mFrameLoadListener != null) {
            mFrameLoadListener.onStartFrameLoad(mFrameURI, new ImageLoaderListener() {
                @Override
                public void onLoadingComplete(Bitmap loadedImage) {
                    if (loadedImage != null) {
                        mFrameView.setScaleType(ImageView.ScaleType.FIT_XY);
                        mFrameView.setImageBitmap(loadedImage);
                    } else {
                        mFrameView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        mFrameView.setImageResource(R.drawable.xadsdk_img_error);
                    }
                }
            });
        }
    }

    private void showPlayView() {
        mLoadingBar.clearAnimation();
        mLoadingBar.setVisibility(View.GONE);
        mMiniPlayBtn.setVisibility(View.GONE);
        mFrameView.setVisibility(View.GONE);
    }

    /**
     * 表明TextureView处于就绪状态
     * 只有TextureView准备好了之后，才可以加载帧数据去显示
     * 否则一片黑屏
     * @param surface
     * @param width
     * @param height
     */
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Logger.debug(TAG, "onSurfaceTextureAvailable >>>>> ");

        // 创建预览surface view
        videoSurface = new Surface(surface);

        checkMediaPlayer();
        mediaPlayer.setSurface(videoSurface);

        /**
         * 在这里调用load()方法，为什么不在构造方法里直接调用呀
         * 因为只有TextureView准备好了之后，才可以加载帧数据去显示
         * 否则一片黑屏
         */
        load();

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Logger.debug(TAG, "onSurfaceTextureSizeChanged >>>>> ");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Logger.debug(TAG, "onSurfaceTextureDestroyed >>>>> ");
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // 缓存的更新
        Logger.debug(TAG, "onSurfaceTextureUpdated >>>>> ");
    }

    /**
     * 注册广播监听
     */
    private void registerBroadcastReceiver() {
        if (mScreenReceiver == null) {
            mScreenReceiver = new ScreenEventReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_USER_PRESENT);
            getContext().registerReceiver(mScreenReceiver, filter);
        }
    }

    /**
     * 取消广播监听
     */
    private void unRegisterBroadcastReceiver() {
        if (mScreenReceiver != null) {
            getContext().unregisterReceiver(mScreenReceiver);
        }
    }

    private void decideCanPlay() {
        if ((Utils.canAutoPlay(getContext(), AdParameters.getCurrentSetting())
                && Utils.getVisiblePercent(mParentContainer) > SDKConstant.VIDEO_SCREEN_PERCENT)) {
            //来回切换页面时，只有 >50,且满足自动播放条件才自动播放
            resume();
        } else {
            setCurrentPlayState(STATE_PLAYING);
            pause();
        }

//        if (Utils.canAutoPlay(getContext(),
//                AdParameters.getCurrentSetting()) &&
//                Utils.getVisiblePercent(mParentContainer) > SDKConstant.VIDEO_SCREEN_PERCENT) {
//            setCurrentPlayState(STATE_PAUSING);
//
//            resume();
//        } else {
//            setCurrentPlayState(STATE_PLAYING);
//            pause();
//        }
    }

    /**
     * 监听锁屏事件的广播接收器
     *
     * 不是还应该有一个判断吗，这个app是否在前台
     * 在后台的话，那就不能播放了呀
     *
     * oh，里面有个decideCanPlay()判断，恰巧有个条件是
     * 视频播放器是否有50%可见 解决了上面那个疑问
     */
    private class ScreenEventReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //主动锁屏时 pause, 主动解锁屏幕时，resume
            switch (intent.getAction()) {
                case Intent.ACTION_USER_PRESENT:
                    // 解锁
                    if (playerState == STATE_PAUSING) {
                        if (mIsRealPause) {
                            //手动点的暂停，回来后还暂停
                            pause();
                        } else {
                            decideCanPlay();
                        }
                    }
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    // 用户锁屏的时候 暂停播放器播放
                    if (playerState == STATE_PLAYING) {
                        pause();
                    }
                    break;
            }
        }
    }


    /**
     * 接口回调作用
     * 供slot层来实现具体点击逻辑,具体逻辑还会变，
     * 如果对UI的点击没有具体监测的话可以不回调
     */
    public interface ADVideoPlayerListener {

        // 视频播放器放到了第几秒
        public void onBufferUpdate(int time);

        // 跳转全屏播放的事件监听
        public void onClickFullScreenBtn();

        // 点击视频区域的一个事件
        public void onClickVideo();

        public void onClickBackBtn();

        public void onClickPlay();

        // 加载成功事件
        public void onAdVideoLoadSuccess();

        // 加载失败事件
        public void onAdVideoLoadFailed();

        // 视频播放完的监听
        public void onAdVideoLoadComplete();
    }

    public interface ADFrameImageLoadListener {

        void onStartFrameLoad(String url, ImageLoaderListener listener);
    }

    public interface ImageLoaderListener {
        /**
         * 如果图片下载不成功，传null
         *
         * @param loadedImage
         */
        void onLoadingComplete(Bitmap loadedImage);
    }


}



























