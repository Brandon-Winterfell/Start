package com.huahua.start.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.huahua.constant.SDKConstant;
import com.huahua.core.AdParameters;
import com.huahua.start.R;
import com.huahua.start.activity.base.BaseActivity;
import com.huahua.start.db.SPManager;

/**
 * Created by Administrator on 2017/2/15.
 */

public class SettingActivity extends BaseActivity implements View.OnClickListener {

    /**
     * UI
     */
    private RelativeLayout mWifiLayout;
    private RelativeLayout mAlwayLayout;
    private RelativeLayout mNeverLayout;
    private CheckBox mWifiBox, mAlwayBox, mNeverBox;
    private ImageView mBackView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_layout);

        initView();
    }

    private void initView() {
        mBackView = (ImageView) findViewById(R.id.back_view);
        mWifiLayout = (RelativeLayout) findViewById(R.id.wifi_layout);
        mWifiBox = (CheckBox) findViewById(R.id.wifi_check_box);
        mAlwayLayout = (RelativeLayout) findViewById(R.id.alway_layout);
        mAlwayBox = (CheckBox) findViewById(R.id.alway_check_box);
        mNeverLayout = (RelativeLayout) findViewById(R.id.close_layout);
        mNeverBox = (CheckBox) findViewById(R.id.close_check_box);

        mBackView.setOnClickListener(this);
        mWifiLayout.setOnClickListener(this);
        mAlwayLayout.setOnClickListener(this);
        mNeverLayout.setOnClickListener(this);

        // 你有保存上次的偏好设置到SharedPreference里吗，没有的话那还不是每次都是那个
        int currentSetting = SPManager.getInstance().getInt(SPManager.VIDEO_PLAY_SETTING, 1);
        // 更新UI
        switch (currentSetting) {
            case 0:
                mAlwayBox.setBackgroundResource(R.drawable.setting_selected);
                mWifiBox.setBackgroundResource(0);
                mNeverBox.setBackgroundResource(0);
                break;
            case 1:
                mAlwayBox.setBackgroundResource(0);
                mWifiBox.setBackgroundResource(R.drawable.setting_selected);
                mNeverBox.setBackgroundResource(0);
                break;
            case 2:
                mAlwayBox.setBackgroundResource(0);
                mWifiBox.setBackgroundResource(0);
                mNeverBox.setBackgroundResource(R.drawable.setting_selected);
                break;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.alway_layout:
                // 有保存进SharedPreference了
                SPManager.getInstance().putInt(SPManager.VIDEO_PLAY_SETTING, 0);

                // 那每次打开应用的时候，AdParameters.setCurrentSetting这个值
                // TODO 没有看到在哪里初始化呀 （应该要从SharedPreference里读取出来的呀）
                // 而且AdParameters这个初始化也不关这个SettingActivity的事

                // 通知当前设置到视频播放SDK
                AdParameters.setCurrentSetting(SDKConstant.AutoPlaySetting.AUTO_PLAY_3G_4G_WIFI);

                // UI更新
                mAlwayBox.setBackgroundResource(R.drawable.setting_selected);
                mWifiBox.setBackgroundResource(0);
                mNeverBox.setBackgroundResource(0);
                break;
            case R.id.close_layout:
                SPManager.getInstance().putInt(SPManager.VIDEO_PLAY_SETTING, 2);
                AdParameters.setCurrentSetting(SDKConstant.AutoPlaySetting.AUTO_PLAY_NEVER);
                mAlwayBox.setBackgroundResource(0);
                mWifiBox.setBackgroundResource(0);
                mNeverBox.setBackgroundResource(R.drawable.setting_selected);
                break;
            case R.id.wifi_layout:
                SPManager.getInstance().putInt(SPManager.VIDEO_PLAY_SETTING, 1);
                AdParameters.setCurrentSetting(SDKConstant.AutoPlaySetting.AUTO_PLAY_ONLY_WIFI);
                mAlwayBox.setBackgroundResource(0);
                mWifiBox.setBackgroundResource(R.drawable.setting_selected);
                mNeverBox.setBackgroundResource(0);
                break;
            case R.id.back_view: // 返回按钮
                finish();
                break;

        }
    }
}






















