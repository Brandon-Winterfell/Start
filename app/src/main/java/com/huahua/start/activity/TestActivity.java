package com.huahua.start.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

import com.huahua.start.R;
import com.huahua.widget.CustomVideoView;

/**
 * Created by Administrator on 2017/2/15.
 */

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        LinearLayout contentLayout = (LinearLayout) findViewById(R.id.test_layout);

        CustomVideoView customVideoView =
                new CustomVideoView(this, contentLayout);
        customVideoView.setDataSource("http://fairee.vicp.net"
        + ":83/2016rm/0318/sanxing160318.mp4");

        contentLayout.addView(customVideoView);
    }
}


























