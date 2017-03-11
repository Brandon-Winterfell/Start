package com.huahua.updatedownload.update;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Created by Administrator on 2017/2/25.
 * @function 真正的负责处理文件的下载和线程间的通信
 * 是一个Runnable
 */

public class UpdateDownloadRequest implements Runnable {

    private String mDownloadUrl;
    private String mLocalFilePath;
    private UpdateDownloadListener mDownloadListener;

    private boolean mIsDownloading = false; // 下载标志位 是否正在下载
    private long mCurrentLength;

    private DownloadResponseHandler mDownloadHandler;

    public UpdateDownloadRequest(
            String downloadUrl,
            String localFilePath,
            UpdateDownloadListener downloadListener) {
        mDownloadUrl = downloadUrl;
        mLocalFilePath = localFilePath;
        mDownloadListener = downloadListener;
    }

    @Override
    public void run() {

    }

    /**
     * 用来真正的去下载文件，并发送消息和回调的接口
     */
    public class DownloadResponseHandler {
        protected static final  int SUCCESS_MESSAGE = 0;
        protected static final  int FAILURE_MESSAGE = 1;
        protected static final  int START_MESSAGE = 2;
        protected static final  int FINISH_MESSAGE = 3;
        protected static final  int NETWORK_OFF = 4;
        protected static final  int PROGRESS_CHANGED = 5;

        private int mCompleteSize = 0;
        private int mProgress = 0;

        private Handler mHandler; // 真正的完成线程间的通信

        public DownloadResponseHandler() {
            // handler的创建
            mHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    handleSelfMessage(msg);
                }
            };
        }

        private void handleSelfMessage(Message msg) {

        }

        /**
         * 定义了一堆方法
         * 用来发送不同的消息对象
         */
        protected void sendFinishMessage() {

        }

        protected void sendProgressChangedMessage(int progress) {

        }

//        protected void sendFailureMessage(Fa)

        /**
         * 发消息最终都是调用这个方法
         * @param msg
         */
        protected void sendMessage(Message msg) {
            if (mHandler != null) {
                mHandler.sendMessage(msg);
            } else {
                handleSelfMessage(msg);
            }
        }
    }

}
















