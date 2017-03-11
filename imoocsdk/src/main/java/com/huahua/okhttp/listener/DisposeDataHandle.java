package com.huahua.okhttp.listener;

/**
 * Created by Administrator on 2017/2/13.
 */

public class DisposeDataHandle {

    public DisposeDataListener mListener = null;
    public Class<?> mClass = null;
    public String mSource = null;

    public DisposeDataHandle(DisposeDataListener listener) {
        mListener = listener;
    }

    public DisposeDataHandle(DisposeDataListener listener, Class<?> clazz) {
        mListener = listener;
        mClass = clazz;
    }

    public DisposeDataHandle(DisposeDataListener listener, String source) {
        mListener = listener;
        mSource = source;
    }


}



























