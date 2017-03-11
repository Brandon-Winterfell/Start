package com.huahua.start.service.update;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by Administrator on 2017/2/25.
 */

public class UpdateManager {

    private static UpdateManager manager;
    /**
     * 你虽然是线程池，但是也没有多线程下载，
     * 你个线程池肯定是只有一个线程的，这样子的还不如用单线程的线程池
     *
     * 对于很多个任务，就是多线程下载；
     * 但是对于某一个具体的任务，还是只有一个线程去执行下载。
     * 那下载app来说，并不是说很多个线程去分段下载那个app文件
     *
     * 这里就一个线程却使用线程池的原因：
     * 主要是使用线程池的对线程本身的生命周期的管理的一个优势
     * 减少僵尸线程的出现
     */
    private ThreadPoolExecutor threadPool;
    /**
     * 待执行的任务（其实就是一个Runnable）（谁去执行呀，线程呀）
     */
    private UpdateDownloadRequest downloadRequest;

    static {
        manager = new UpdateManager();
    }

    private UpdateManager() {
        threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    // 单例使用
    public static UpdateManager getInstance() {
        return manager;
    }

    public void startDownload(String downloadUrl, String localFilePath,
                              UpdateDownloadListener downloadListener) {
        if (downloadRequest != null && downloadRequest.isDownloading()) {
            return;
        }
        // 检查路径是否合法
        checkLocalFilePath(localFilePath);

        downloadRequest = new UpdateDownloadRequest(downloadUrl, localFilePath,
                downloadListener);
        // TODO
        Future<?> request = threadPool.submit(downloadRequest);
        new WeakReference<Future<?>>(request);
    }

    private void checkLocalFilePath(String localFilePath) {
        File path = new File(localFilePath.substring(0,
                localFilePath.lastIndexOf("/") + 1));
        File file = new File(localFilePath);
        // 目录不存在的话就创建出来
        if (!path.exists()) {
            path.mkdirs();
        }
        // 文件不存在就创建出来（存在的话，那是不是覆盖了呀）
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}




















