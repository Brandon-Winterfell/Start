package com.huahua.start.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;
import android.widget.TextView;

import com.huahua.start.R;
import com.huahua.start.activity.base.BaseActivity;
import com.huahua.start.adapter.PhotoPagerAdapter;
import com.huahua.util.Utils;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/2/28.
 * @function 显示产品大图页面
 */

public class PhotoViewActivity extends BaseActivity {
    public static final String PHOTO_LIST = "photo_list";

    /**
     * UI
     */
    private ViewPager mPager;
    private TextView mIndictorView;
    private ImageView mShareView;
    /**
     * Data
     */
    private PhotoPagerAdapter mAdapter;
    // 图片数组(图片的地址)
    private ArrayList<String> mPhotoLists;
    private int mLenght;
    private int currentPos;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view_layout);

        initData();
        initView();
    }

    /**
     * 初始化要显示的图片地址列表
     */
    private void initData() {
        // 这个页面负责显示，数据（图片地址）从其他页面带过来
        Intent intent = getIntent();
        mPhotoLists = intent.getStringArrayListExtra(PHOTO_LIST);
        mLenght = mPhotoLists.size();
    }

    private void initView() {
        mIndictorView = (TextView) findViewById(R.id.indictor_view);
        mIndictorView.setText("1/" + mLenght);

        mShareView = (ImageView) findViewById(R.id.share_view);
        //mShareView.setOnClickListener(this);

        mPager = (ViewPager) findViewById(R.id.photo_pager);
        mAdapter = new PhotoPagerAdapter(this, mPhotoLists, false);
        mPager.setPageMargin(Utils.dip2px(this, 30));
        mPager.setAdapter(mAdapter);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mIndictorView.setText(String.valueOf((position + 1)).concat("/").
                        concat(String.valueOf(mLenght)));
                currentPos = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        // 隐藏软键盘
        Utils.hideSoftInputMethod(this, mIndictorView);
    }

}





















