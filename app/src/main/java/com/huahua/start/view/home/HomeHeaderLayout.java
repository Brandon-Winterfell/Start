package com.huahua.start.view.home;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huahua.imageloader.ImageLoaderManager;
import com.huahua.start.R;
import com.huahua.start.adapter.PhotoPagerAdapter;
import com.huahua.start.module.recommand.RecommandFooterValue;
import com.huahua.start.module.recommand.RecommandHeadValue;
import com.huahua.start.view.viewpagerindictor.CirclePageIndicator;

import cn.trinea.android.view.autoscrollviewpager.AutoScrollViewPager;

/**
 * Created by Administrator on 2017/2/14.
 */

public class HomeHeaderLayout extends RelativeLayout {

    private Context mContext;

    /**
     * UI
     */
    // 容器
    private RelativeLayout mRootView;
    // 自动滑动ViewPager
    private AutoScrollViewPager mViewPager;
    // 滚动指示器
    private CirclePageIndicator mPagerIndictor;
    private TextView mHotView;
    // 适配器
    private PhotoPagerAdapter mAdapter;
    // ImageView的个数
    private ImageView[] mImageViews = new ImageView[4];
    private LinearLayout mFootLayout;
    /**
     * Data
     */
    private RecommandHeadValue mHeaderValue;
    private ImageLoaderManager mImageLoader;

    /**
     * 直接将数据传进来
     * @param context
     * @param headerValue
     */
    public HomeHeaderLayout(Context context, RecommandHeadValue headerValue) {
        this(context, null, headerValue);
    }

    public HomeHeaderLayout(Context context, AttributeSet attrs, RecommandHeadValue headerValue) {
        super(context, attrs);
        mContext = context;
        mHeaderValue = headerValue;
        mImageLoader = ImageLoaderManager.getInstance(mContext);
        initView();
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mRootView = (RelativeLayout) inflater.inflate(R.layout.listview_home_head_layout, this);

        mViewPager = (AutoScrollViewPager) mRootView.findViewById(R.id.pager);
        mPagerIndictor = (CirclePageIndicator) mRootView.findViewById(R.id.pager_indictor_view);
        mHotView = (TextView) mRootView.findViewById(R.id.zuixing_view);
        mImageViews[0] = (ImageView) mRootView.findViewById(R.id.head_image_one);
        mImageViews[1] = (ImageView) mRootView.findViewById(R.id.head_image_two);
        mImageViews[2] = (ImageView) mRootView.findViewById(R.id.head_image_three);
        mImageViews[3] = (ImageView) mRootView.findViewById(R.id.head_image_four);
        mFootLayout = (LinearLayout) mRootView.findViewById(R.id.content_layout);

        mAdapter = new PhotoPagerAdapter(mContext, mHeaderValue.ads, true);
        mViewPager.setAdapter(mAdapter);
        mViewPager.startAutoScroll(3000);
        mPagerIndictor.setViewPager(mViewPager);

        for (int i = 0; i < mImageViews.length; i++) {
            mImageLoader.displayImage(mImageViews[i], mHeaderValue.middle.get(i));
        }

        for (RecommandFooterValue value : mHeaderValue.footer) {
            mFootLayout.addView(createItem(value));
        }
        mHotView.setText(mContext.getString(R.string.today_zuixing));

    }

    private HomeBottomItem createItem(RecommandFooterValue value) {
        HomeBottomItem item = new HomeBottomItem(mContext, value);
        return item;
    }

}

























