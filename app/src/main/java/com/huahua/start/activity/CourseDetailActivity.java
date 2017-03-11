package com.huahua.start.activity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huahua.okhttp.listener.DisposeDataListener;
import com.huahua.start.R;
import com.huahua.start.activity.base.BaseActivity;
import com.huahua.start.adapter.CourseCommentAdapter;
import com.huahua.start.manager.UserManager;
import com.huahua.start.module.course.BaseCourseModel;
import com.huahua.start.module.course.CourseCommentValue;
import com.huahua.start.module.user.User;
import com.huahua.start.network.RequestCenter;
import com.huahua.start.util.Util;
import com.huahua.start.view.course.CourseDetailFooterView;
import com.huahua.start.view.course.CourseDetailHeaderView;
import com.huahua.util.Utils;

/**
 * Created by Administrator on 2017/2/28.
 *
 *@function: 课程详情Activity, 展示课程详情,这个页面要用signalTop模式
 */

public class CourseDetailActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    // 主要就是通过course id来发送请求，请求课程详情
    public static String COURSE_ID = "courseID";

    /**
     * UI
     */
    private ImageView mBackView;
    private ListView mListView;
    private ImageView mLoadingView;
    private RelativeLayout mBottomLayout;
    private ImageView mJianPanView;
    private EditText mInputEditView;
    private TextView mSendView;
    private CourseDetailHeaderView headerView;
    private CourseDetailFooterView footerView;
    private CourseCommentAdapter mAdapter;
    /**
     * Data
     */
    private String mCourseID;
    private BaseCourseModel mData;
    private String tempHint = "";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail_layout);

        initData();
        initView();
        requestDeatil();
    }

    // 使用singletop singletask模式下，传入最新的intent。复用activity时走的生命周期回调
    // oncreate()方法只会在activity第一次创建的时候回调
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // 为当前activity设置最新的intent，页面中使用getIntent获取到的intent就是最新的intent了

        initData();
        initView();
        requestDeatil();
    }

    //初始化数据
    private void initData() {
        Intent intent = getIntent();
        mCourseID = intent.getStringExtra(COURSE_ID);
    }

    //初始化数据
    private void initView() {
        mBackView = (ImageView) findViewById(R.id.back_view);
        mBackView.setOnClickListener(this);

        mListView = (ListView) findViewById(R.id.comment_list_view);
        mListView.setOnItemClickListener(this);
        mListView.setVisibility(View.GONE);

        mLoadingView = (ImageView) findViewById(R.id.loading_view);
        mLoadingView.setVisibility(View.VISIBLE);
        // 还有个动画 停止动画的时机 数据回来的时候隐藏掉
        AnimationDrawable anim = (AnimationDrawable) mLoadingView.getDrawable();
        anim.start();

        mBottomLayout = (RelativeLayout) findViewById(R.id.bottom_layout);
        mJianPanView = (ImageView) findViewById(R.id.jianpan_view);
        mJianPanView.setOnClickListener(this);
        mInputEditView = (EditText) findViewById(R.id.comment_edit_view);
        mSendView = (TextView) findViewById(R.id.send_view);
        mSendView.setOnClickListener(this);
        mBottomLayout.setVisibility(View.GONE);

        intoEmptyState();
    }

    private void requestDeatil() {

        RequestCenter.requestCourseDetail(mCourseID, new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                mData = (BaseCourseModel) responseObj;
                updateUI();
            }

            @Override
            public void onFailure(Object reasonObj) {
                // 失败了没有提示 bad
            }
        });
    }

    /**
     * 根据数据填充UI
     */
    private void updateUI() {
        // 隐藏动画那个view，这样隐藏动画呀，只是隐藏了那个view，动画没有停止呀
        mLoadingView.setVisibility(View.GONE);
        // 设置ListView列表项
        mListView.setVisibility(View.VISIBLE);
        mAdapter = new CourseCommentAdapter(this, mData.data.body);
        mListView.setAdapter(mAdapter);
        // 更新列表项的头
        // 这个判断主要是为了防止headerView多次添加
        if (headerView != null) {
            mListView.removeHeaderView(headerView);
        }
        headerView = new CourseDetailHeaderView(this, mData.data.head);
        mListView.addHeaderView(headerView);
        // 更新列表项的尾
        if (footerView != null) {
            mListView.removeFooterView(footerView);
        }
        footerView = new CourseDetailFooterView(this, mData.data.footer);
        mListView.addFooterView(footerView);

        mBottomLayout.setVisibility(View.VISIBLE);
    }

    public void intoEmptyState() {
        tempHint = "";
        mInputEditView.setText("");
        mInputEditView.setHint(getString(R.string.input_comment));
        Utils.hideSoftInputMethod(this, mInputEditView);
    }

    /**
     * EditText进入编辑状态
     */
    private void intoEditState(String hint) {
        mInputEditView.requestFocus();
        mInputEditView.setHint(hint);
        Utils.showSoftInputMethod(this, mInputEditView);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_view:
                finish();
                break;
            /**
             * 评论没有通过网络发送出去呀，因为没有后台的原因？
             */
            case R.id.send_view:
                String comment = mInputEditView.getText().toString().trim();
                if (UserManager.getInstance().hasLogined()) {
                    if (!TextUtils.isEmpty(comment)) {
                        // 只是添加到了 adapter中，ListView显示出来
                        mAdapter.addComment(assembleCommentValue(comment));
                        intoEmptyState();
                    }
                } else {
                    startActivity(new Intent(this, LoginActivity.class));
                }

                break;
            case R.id.jianpan_view:
                mInputEditView.requestFocus();
                Utils.showSoftInputMethod(this, mInputEditView);
                break;
        }
    }

    /**
     * 组装CommentValue对象
     *
     * @return
     */
    private CourseCommentValue assembleCommentValue(String comment) {
        User user = UserManager.getInstance().getUser();
        CourseCommentValue value = new CourseCommentValue();
        value.name = user.data.name;
        value.logo = user.data.photoUrl;
        value.userId = user.data.userId;
        value.type = 1;
        value.text = tempHint + comment;
        return value;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int cursor = position - mListView.getHeaderViewsCount();
        if (cursor >= 0 && cursor < mAdapter.getCommentCount()) {
            if (UserManager.getInstance().hasLogined()) {
                CourseCommentValue value = (CourseCommentValue) mAdapter.getItem(
                        position - mListView.getHeaderViewsCount());
                if (value.userId.equals(UserManager.getInstance().getUser().data.userId)) {
                    //自己的评论不能回复
                    intoEmptyState();
                    Toast.makeText(this, "不能回复自己!", Toast.LENGTH_SHORT).show();
                } else {
                    //不是自己的评论，可以回复
                    tempHint = getString(R.string.comment_hint_head).concat(value.name).
                            concat(getString(R.string.comment_hint_footer));
                    intoEditState(tempHint);
                    // 没有做什么东西呀
                }
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
        }
    }
}






















