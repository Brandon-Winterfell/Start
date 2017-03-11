package com.huahua.start.view.fragment.home;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.huahua.okhttp.exception.OkHttpException;
import com.huahua.okhttp.listener.DisposeDataListener;
import com.huahua.start.R;
import com.huahua.start.activity.PhotoViewActivity;
import com.huahua.start.adapter.CourseAdapter;
import com.huahua.start.module.recommand.BaseRecommandModel;
import com.huahua.start.module.recommand.RecommandBodyValue;
import com.huahua.start.network.RequestCenter;
import com.huahua.start.view.fragment.BaseFragment;
import com.huahua.start.view.home.HomeHeaderLayout;
import com.huahua.start.zxing.app.CaptureActivity;
import com.huahua.util.Logger;

/**
 * Created by Administrator on 2017/2/13.
 */

public class HomeFragment extends BaseFragment
        implements View.OnClickListener, AdapterView.OnItemClickListener {

    /**
     * UI
     */
    private View mContentView;
    private ListView mListView;
    private TextView mQRCodeView;
    private TextView mCategoryView;
    private TextView mSearchView;
    private ImageView mLoadingView;
    /**
     * data
     */
    private CourseAdapter mAdapter;
    private BaseRecommandModel mRecommandData;

    private static final int REQUEST_QRCODE = 0x01;


    public HomeFragment() {
        // 我记得一定要写一个无参的构造方法的
    }

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity); // 命名 mHost
//        mContext = activity; // 命名不好 可以写在父类吧 老师的是写在onCreateView里
//    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.debug("huahua", "HomeFragment onCreate >>>>>> ");

        // 发送推荐产品请求
        requestRecommandData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        mContentView = inflater.inflate(R.layout.fragment_home_layout, container, false);
        initView();
        return  mContentView;
    }

    private void initView() {
        mQRCodeView = (TextView) mContentView.findViewById(R.id.qrcode_view);
        mQRCodeView.setOnClickListener(this);
        mCategoryView = (TextView) mContentView.findViewById(R.id.category_view);
        mCategoryView.setOnClickListener(this);
        mSearchView = (TextView) mContentView.findViewById(R.id.search_view);
        mSearchView.setOnClickListener(this);
        mListView = (ListView) mContentView.findViewById(R.id.list_view);
        mListView.setOnItemClickListener(this); // 看这里
        mLoadingView = (ImageView) mContentView.findViewById(R.id.loading_view);
        // 看这里
        AnimationDrawable anim = (AnimationDrawable) mLoadingView.getDrawable();
        anim.start();
    }

    // 发送推荐产品请求
    private void requestRecommandData() {
        Logger.debug("huahua", "发送推荐产品请求");
        // 这是一个匿名内部类了 小心内存泄漏
        // 不会发生数据回来 UI不见了吧的情况吧 因为Fragment的实例没有销毁 只是隐藏了
        RequestCenter.requestRecommandData(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                Logger.debug("huahua", "发送推荐产品请求onSuccess >>>> ");
                mRecommandData = (BaseRecommandModel) responseObj;
                Logger.debug("huahua", "发送推荐产品请求onSuccess 数据：" + mRecommandData.toString());

                // 更新UI
                showSuccessView();
            }

            @Override
            public void onFailure(Object reasonObj) {
                Logger.debug("huahua", "发送推荐产品请求onFailure >>>> ");
                Log.d("huahua", "发送推荐产品请求onFailure : 原因 >> " + reasonObj.toString());

                if (reasonObj instanceof OkHttpException) {
                    OkHttpException okHttpException = (OkHttpException) reasonObj;
                    Log.d("huahua", "发送推荐产品请求onFailure : 原因 >> " + okHttpException.toString());
                }

                // 显示请求失败View
                showErrorView(reasonObj);



            }
        });

    }

    /**
     * 显示请求成功UI
     */
    private void showSuccessView() {
        if (mRecommandData.data.list != null && mRecommandData.data.list.size() > 0) {
            mLoadingView.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            //为listview添加头 是一个自定义的view 组合的自定义view
            mListView.addHeaderView(new HomeHeaderLayout(mContext, mRecommandData.data.head));
            // ListView的适配器
            mAdapter = new CourseAdapter(mContext, mRecommandData.data.list);
            mListView.setAdapter(mAdapter);

            // 为ListView添加滑动事件监听
            mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    mAdapter.updateAdInScrollView();
                }
            });
        } else {
            showErrorView("fuck");
        }
    }

    private void showErrorView(Object reasonObj) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.qrcode_view :
                doOpenCamera();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // 要注意减去列表的头部
        RecommandBodyValue value = (RecommandBodyValue) mAdapter.getItem(
                position - mListView.getHeaderViewsCount());

        if (value.type != 0) { // type == 0是视频的item
            Intent intent = new Intent(mContext, PhotoViewActivity.class);
            intent.putStringArrayListExtra(PhotoViewActivity.PHOTO_LIST, value.url);
            startActivity(intent);
        }
    }

    /**
     * 去扫描二维码activity界面
     */
    public void doOpenCamera() {
        Intent intent = new Intent(mContext, CaptureActivity.class);
        startActivityForResult(intent, REQUEST_QRCODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_QRCODE :
                // 扫码处理逻辑
                if (resultCode == Activity.RESULT_OK) {
                    // 一般扫码返回的结果都是http或者是https超链接地址
                    // 然后一般是调用webview或者浏览器去打开地址
                    String code = data.getStringExtra("SCAN_RESULT");
                    // 判断是不是超链接地址
                    Toast.makeText(mContext, code, Toast.LENGTH_SHORT).show();
                    Logger.debug("huahua", "扫码结果：" + code);
                }
                break;
        }
    }
}




















