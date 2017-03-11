package com.huahua.start.network;

import com.huahua.okhttp.CommonOkhttpClient;
import com.huahua.okhttp.listener.DisposeDataHandle;
import com.huahua.okhttp.listener.DisposeDataListener;
import com.huahua.okhttp.request.CommonRequest;
import com.huahua.okhttp.request.RequestParams;
import com.huahua.start.module.course.BaseCourseModel;
import com.huahua.start.module.recommand.BaseRecommandModel;
import com.huahua.start.module.update.UpdateModel;
import com.huahua.start.module.user.User;

/**
 * Created by Administrator on 2017/2/14.
 * 应用层面 业务层对请求的封装
 * 所有的网络请求都在这里
 */

public class RequestCenter {

    /**
     * 根据参数发送所有get请求 异步的
     * @param url
     * @param params
     * @param listener
     * @param clazz
     */
    public static void sendGetRequest(String url,
                                      RequestParams params,
                                      DisposeDataListener listener,
                                      Class<?> clazz) {
        // 调用get方法
        CommonOkhttpClient.get(CommonRequest.createGetRequest(url, params),
                new DisposeDataHandle(listener, clazz));
    }

    /**
     * 根据参数发送所有的post请求 异步的
     * @param url
     * @param params
     * @param listener
     * @param clazz
     */
    public static void sendPostRequest(String url,
                                       RequestParams params,
                                       DisposeDataListener listener,
                                       Class<?> clazz) {
        // 调用post方法
        CommonOkhttpClient.post(CommonRequest.createPostRequest(url, params),
                new DisposeDataHandle(listener, clazz));
    }


    public static void requestRecommandData(DisposeDataListener listener) {
        RequestCenter.sendGetRequest(HttpConstants.HOME_RECOMMAND, null, listener, BaseRecommandModel.class);
    }

    /**
     * 应用版本号请求
     *
     * @param listener
     */
    public static void checkVersion(DisposeDataListener listener) {
        RequestCenter.sendGetRequest(HttpConstants.CHECK_UPDATE, null, listener, UpdateModel.class);
    }

    /**
     * 用户登陆请求
     *
     * @param listener
     * @param userName
     * @param passwd
     */
    public static void login(String userName, String passwd, DisposeDataListener listener) {

        RequestParams params = new RequestParams();
        params.put("mb", userName);
        params.put("pwd", passwd);
        RequestCenter.sendGetRequest(HttpConstants.LOGIN, params, listener, User.class);
    }

    /**
     * 请求课程详情
     *
     * @param listener
     */
    public static void requestCourseDetail(String courseId, DisposeDataListener listener) {
        RequestParams params = new RequestParams();
        params.put("courseId", courseId);
        RequestCenter.sendGetRequest(HttpConstants.COURSE_DETAIL, params, listener, BaseCourseModel.class);
    }

}






























