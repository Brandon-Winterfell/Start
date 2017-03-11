package com.huahua.start.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.huahua.imageloader.ImageLoaderManager;
import com.huahua.start.activity.CourseDetailActivity;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by Administrator on 2017/2/14.
 */

public class PhotoPagerAdapter extends PagerAdapter {

    private Context mContext;

    private boolean mIsMatch;
    // 数据源
    private ArrayList<String> mData;
    // 图片下载管理器
    private ImageLoaderManager mLoader;

    public PhotoPagerAdapter(Context context, ArrayList<String> list, boolean isMatch) {
        mContext = context;
        mData = list;
        mIsMatch = isMatch;
        mLoader = ImageLoaderManager.getInstance(mContext);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    // 构建ViewPager的每一项
    @Override
    public View instantiateItem(ViewGroup container, int position) {
        ImageView photoView;
        if (mIsMatch) {
            // 构建ImageView
            photoView = new ImageView(mContext);
            photoView.setScaleType(ImageView.ScaleType.FIT_XY);
            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext,
                            CourseDetailActivity.class);
                    mContext.startActivity(intent);
                }
            });
        } else {
            // PhotoView
            photoView = new PhotoView(mContext);
        }

        // 下载图片
        // 使用图片加载组件为PhotoView加载图片
        mLoader.displayImage(photoView, mData.get(position));
        // 添加到容器内
        container.addView(
                photoView,
                ViewGroup.LayoutParams.MATCH_PARENT, // 宽 高
                ViewGroup.LayoutParams.MATCH_PARENT
        );

        return photoView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

}
