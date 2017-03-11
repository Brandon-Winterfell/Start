package com.huahua.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.huahua.okhttp.R;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * Created by Administrator on 2017/2/13.
 * @function 初始化UniversalImageLoader，并用来加载网络图片
 */

public class ImageLoaderManager {

    private static final int THREAD_COUNT = 4; // 表示UIL最多可以有多少条线程
    private static final int PRIORITY = 2; // 表示图片加载线程的优先级
    private static final int DISK_CACHE_SIZE = 50 * 1024; // 表示磁盘缓存容量
    private static final int CONNECTION_TIME_OUT = 5 * 1000; // 连接的超时时间
    private static final int READ_TIME_OUT = 30 * 1000; // 读取的超时时间

    private static ImageLoader sImageLoader;
    private static ImageLoaderManager sInstance = null;

    public static ImageLoaderManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (ImageLoaderManager.class) {
                if (sInstance == null) {
                    sInstance = new ImageLoaderManager(context);
                }
            }
        }

        return sInstance;
    }

    /**
     * 单例模式的私有构造方法
     * @param context
     */
    private ImageLoaderManager(Context context) {
        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(context)
                .threadPoolSize(THREAD_COUNT) // 配置图片下载线程的最大数量
                .threadPriority(Thread.NORM_PRIORITY - PRIORITY) // 降级
                .denyCacheImageMultipleSizesInMemory() // 禁止在内存中缓存不同尺寸的图片
                .memoryCache(new WeakMemoryCache())  // 使用弱引用内存缓存
                .diskCacheSize(DISK_CACHE_SIZE) // 分配硬盘缓存大小
                .diskCacheFileNameGenerator(new Md5FileNameGenerator()) // 使用MD5命名文件 安全
                .tasksProcessingOrder(QueueProcessingType.LIFO) //图片下载顺序
                .defaultDisplayImageOptions(getDefaultOPtions()) // 默认的图片加载Options
                .imageDownloader(new BaseImageDownloader(context,
                        CONNECTION_TIME_OUT, READ_TIME_OUT)) // 设置图片下载器
                .writeDebugLogs() // debug模式下会输出日志
                .build();

        ImageLoader.getInstance().init(configuration);
        sImageLoader = ImageLoader.getInstance();
    }

    /**
     * 实现默认的Options
     * @return
     */
    private DisplayImageOptions getDefaultOPtions() {

        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.xadsdk_img_error) // 在我们图片地址为空的时候加载这张图片
                .showImageOnFail(R.drawable.xadsdk_img_error) // 图片下载失败的时候显示的图片
                .cacheInMemory(true) // 设置图片可以缓存在内存
                .cacheOnDisk(true) // 设置图片可以缓存到硬盘
                .bitmapConfig(Bitmap.Config.RGB_565) // 图片解码类型 降低了图片色彩 减少了内存的使用
                .decodingOptions(new BitmapFactory.Options()) // 图片解码配置 系统自带的配置
                .build();

        return options;
    }

    public void displayImage(ImageView imageView, String url) {
        displayImage(imageView, url, null);
    }

    public void displayImage(ImageView imageView, String url, ImageLoadingListener listener) {
        displayImage(imageView, url, null, listener);
    }

    /**
     * 加载图片API
     *
     * @param imageView
     * @param url
     * @param options
     * @param listener
     */
    public void displayImage(ImageView imageView,
                             String url,
                             DisplayImageOptions options,
                             ImageLoadingListener listener) {
        if (sImageLoader != null) {
            sImageLoader.displayImage(url, imageView, options, listener);
        } // else等于null的话，是不是应该要抛出个exception
    }

}
































