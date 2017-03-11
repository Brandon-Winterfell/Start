package com.huahua.okhttp.listener;

/**
 * Created by Administrator on 2017/2/13.
 */

public interface DisposeDataListener {

    /**
     * 请求成功回调事件处理
     * @param responseObj
     */
    public void onSuccess(Object responseObj);

    /**
     * 请求失败回调事件处理
     * @param reasonObj
     */
    public void onFailure(Object reasonObj);

}





























