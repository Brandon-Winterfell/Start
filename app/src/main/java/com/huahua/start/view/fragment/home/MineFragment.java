package com.huahua.start.view.fragment.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huahua.imageloader.ImageLoaderManager;
import com.huahua.okhttp.listener.DisposeDataListener;
import com.huahua.start.R;
import com.huahua.start.activity.LoginActivity;
import com.huahua.start.activity.SettingActivity;
import com.huahua.start.constant.Constant;
import com.huahua.start.manager.UserManager;
import com.huahua.start.network.RequestCenter;
import com.huahua.start.service.update.UpdateService;
import com.huahua.start.module.update.UpdateModel;
import com.huahua.start.sharesdk.ShareDialog;
import com.huahua.start.util.Util;
import com.huahua.start.view.CommonDialog;
import com.huahua.start.view.MyQrCodeDialog;
import com.huahua.start.view.fragment.BaseFragment;

import cn.sharesdk.framework.Platform;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Administrator on 2017/2/13.
 * @function: 个人信息Fragment
 */

public class MineFragment extends BaseFragment implements View.OnClickListener {

    /**
     * UI
     */
    private View mContentView;
    private RelativeLayout mLoginLayout;
    private CircleImageView mPhotoView;
    private TextView mLoginInfoView;
    private TextView mLoginView;
    private RelativeLayout mLoginedLayout;
    private TextView mUserNameView;
    private TextView mTickView;
    private TextView mVideoPlayerView;
    private TextView mShareView;
    private TextView mQrCodeView;
    private TextView mUpdateView;

    //自定义了一个广播接收器
    private LoginBroadcastReceiver receiver = new LoginBroadcastReceiver();

    public MineFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        /**
         * 注册我们自定义的广播
         */
        registerBroadcast();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_mine_layout, null, false);
        initView();
        return mContentView;
    }


    private void initView() {
        mLoginLayout = (RelativeLayout) mContentView.findViewById(R.id.login_layout);
        mLoginLayout.setOnClickListener(this);
        mLoginedLayout = (RelativeLayout) mContentView.findViewById(R.id.logined_layout);
        mLoginedLayout.setOnClickListener(this);

        mPhotoView = (CircleImageView) mContentView.findViewById(R.id.photo_view);
        mPhotoView.setOnClickListener(this);
        mLoginView = (TextView) mContentView.findViewById(R.id.login_view);
        mLoginView.setOnClickListener(this);
        mVideoPlayerView = (TextView) mContentView.findViewById(R.id.video_setting_view);
        mVideoPlayerView.setOnClickListener(this);
        mShareView = (TextView) mContentView.findViewById(R.id.share_imooc_view);
        mShareView.setOnClickListener(this);
        mQrCodeView = (TextView) mContentView.findViewById(R.id.my_qrcode_view);
        mQrCodeView.setOnClickListener(this);
        mLoginInfoView = (TextView) mContentView.findViewById(R.id.login_info_view);
        mUserNameView = (TextView) mContentView.findViewById(R.id.username_view);
        mTickView = (TextView) mContentView.findViewById(R.id.tick_view);

        mUpdateView = (TextView) mContentView.findViewById(R.id.update_view);
        mUpdateView.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 根据用户信息更新我们的fragment
        if (UserManager.getInstance().hasLogined()) {
            if (mLoginedLayout.getVisibility() == View.GONE) {
                mLoginLayout.setVisibility(View.GONE);
                mLoginedLayout.setVisibility(View.VISIBLE);

                mUserNameView.setText(UserManager.getInstance().getUser().data.name);
                mTickView.setText(UserManager.getInstance().getUser().data.tick);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBroadcast();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.share_imooc_view:
                //分享Imooc课网址
                shareFriend();
                break;
            case R.id.login_layout:
            case R.id.login_view:
                //未登陆，则跳轉到登陸页面
                if (!UserManager.getInstance().hasLogined()) {
                    toLogin();
                }
                break;
            case R.id.my_qrcode_view:
                if (!UserManager.getInstance().hasLogined()) {
                    //未登陆，去登陆。
                    toLogin();
                } else {
                    //已登陆根据用户ID生成二维码显示
                    MyQrCodeDialog dialog = new MyQrCodeDialog(mContext);
                    dialog.show();
                }
                break;
            case R.id.video_setting_view:
                mContext.startActivity(new Intent(mContext, SettingActivity.class));
                break;
            case R.id.update_view:
                // 权限处理 小于6.0的就不用动态申请权限
                if (hasPermission(Constant.WRITE_READ_EXTERNAL_PERMISSION)) {
                    // 拥有了此权限，那么直接执行业务逻辑
                    checkVersion();
                } else {
                    // 还没有对应权限
                    requestPermission(Constant.WRITE_READ_EXTERNAL_CODE, Constant.WRITE_READ_EXTERNAL_PERMISSION);
                }
                break;
        }
    }

    @Override
    public void doWriteSDCard() {
        checkVersion();
    }

    /**
     * 去登陆页面
     */
    private void toLogin() {
        Intent intent = new Intent(mContext, LoginActivity.class);
        mContext.startActivity(intent);
    }

    /**
     * 分享慕课网给好友
     */
    private void shareFriend() {
        ShareDialog dialog = new ShareDialog(mContext, false);
        // 添加数据
        dialog.setShareType(Platform.SHARE_IMAGE);
        dialog.setShareTitle("慕课网");
        dialog.setShareTitleUrl("http://www.imooc.com");
        dialog.setShareText("慕课网");
        dialog.setShareSite("imooc");
        dialog.setShareSiteUrl("http://www.imooc.com");
        dialog.setImagePhoto(Environment.getExternalStorageDirectory() + "/test2.jpg");

        dialog.show();
    }

    /**
     * 注册广播（我们自定义的广播）
     */
    private void registerBroadcast() {

        IntentFilter filter =
                new IntentFilter(LoginActivity.LOGIN_ACTION);
        LocalBroadcastManager.getInstance(mContext)
                .registerReceiver(receiver, filter);
    }

    /**
     * 解绑注册的广播
     */
    private void unregisterBroadcast() {
        LocalBroadcastManager.getInstance(mContext)
                .unregisterReceiver(receiver);
    }

    //发送版本检查更新请求
    private void checkVersion() {
        RequestCenter.checkVersion(new DisposeDataListener() {
            @Override
            public void onSuccess(Object responseObj) {
                // 拿到服务器响应
                final UpdateModel updateModel = (UpdateModel) responseObj;

                // 判断本地程序版本号与服务器返回版本号大小
                if (Util.getVersionCode(mContext) < updateModel.data.currentVersion) {
                    //说明有新版本,开始下载
                    CommonDialog dialog = new CommonDialog(mContext, getString(R.string.update_new_version),
                            getString(R.string.update_title), getString(R.string.update_install),
                            getString(R.string.cancel), new CommonDialog.DialogClickListener() {
                        @Override
                        public void onDialogClick() {
                            /**
                             * 启动service去下载apk文件
                             */
                            Intent intent = new Intent(mContext, UpdateService.class);
                            mContext.startService(intent);
                        }
                    });
                    dialog.show();
                } else {
                    //弹出一个toast提示当前已经是最新版本等处理
                    Toast.makeText(mContext, "当前版本已经是最新版本~", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Object reasonObj) {
                Toast.makeText(mContext, "检查更新失败~", Toast.LENGTH_SHORT).show();
            }
        });
    }

//
    /**
     * 接收mina发送来的消息，并更新UI
     *
     * 自定义广播接收器，用来处理我们的登录广播
     */
    private class LoginBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UserManager.getInstance().hasLogined()) {
                //更新我们的fragment
                // (隐藏我们未登录的布局，显示我们登录的布局，并且将用户名显示在textview上)
                if (mLoginedLayout.getVisibility() == View.GONE) {
                    mLoginLayout.setVisibility(View.GONE);
                    mLoginedLayout.setVisibility(View.VISIBLE);

                    mUserNameView.setText(UserManager.getInstance().getUser().data.name);
                    mTickView.setText(UserManager.getInstance().getUser().data.tick);

                    ImageLoaderManager.getInstance(mContext).displayImage(mPhotoView, UserManager.getInstance().getUser().data.photoUrl);
                }
            }
        }
    }

}























