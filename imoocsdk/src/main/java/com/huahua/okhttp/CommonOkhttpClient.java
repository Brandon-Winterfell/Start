package com.huahua.okhttp;

import com.huahua.okhttp.https.HttpsUtils;
import com.huahua.okhttp.listener.DisposeDataHandle;
import com.huahua.okhttp.response.CommonJsonCallback;
import com.huahua.util.Logger;

import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by Administrator on 2017/2/13.
 * @function 请求的发送，请求参数的配置，HTTPS的支持。
 *
 * 请求头参数或者请求参数在request里处理的 怎么我没看到
 */

public class CommonOkhttpClient {

    // 读超时 写超时 一般统一的指定一个时间
    // 不会分开详细写每个的超时时间
    private static final int TIME_OUT = 30; // 超时参数

    private static OkHttpClient sOkHttpClient;

    // 为client配置参数 对builder对象做处理
    static {

        OkHttpClient.Builder sBuilder = new OkHttpClient.Builder();

        sBuilder.connectTimeout(TIME_OUT, TimeUnit.SECONDS); // 连接超时时间
        sBuilder.readTimeout(TIME_OUT, TimeUnit.SECONDS); // 读超时时间
        sBuilder.writeTimeout(TIME_OUT, TimeUnit.SECONDS); // 写超时时间

        sBuilder.followRedirects(true); // 允许重定向 默认是true

        // https支持 两步
        // 信任所有类型的证书 包括自定义的证书以及官方生成的证书

        // 主机名认证  支持所有的HTTPS请求
        sBuilder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                // 无论任何主机都return true。
                return true;
            }
        });

        // sslSocketFactory
        sBuilder.sslSocketFactory(HttpsUtils.initSSLSocketFactory());

        // 生成client对象 builder的build方法
        sOkHttpClient = sBuilder.build();
    }

    /**
     * 发送具体的http/https请求
     * @param request
     * @param callback
     * @return Call实例 使用Call实例可以在onDestroy方法中取消请求
     */
    public static Call sendRequest(Request request, CommonJsonCallback callback) {

        Call call = sOkHttpClient.newCall(request);
        call.enqueue(callback);  // 这是异步的请求 TODO 同步的呢

        return call;
    }

    public static Call get(Request request, DisposeDataHandle handle) {
        Logger.debug("huahua", "CommonOkhttpClient get方法 >>>>>>  ");
        Call call = sOkHttpClient.newCall(request);
        call.enqueue(new CommonJsonCallback(handle));
        return call;
    }

    public static Call post(Request request, DisposeDataHandle handle) {
        Logger.debug("huahua", "CommonOkhttpClient post方法 >>>>>>  ");
        Call call = sOkHttpClient.newCall(request);
        call.enqueue(new CommonJsonCallback(handle));
        return call;
    }
}





















