package com.huahua.okhttp.request;

import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Request;

/**
 * Created by Administrator on 2017/2/13.
 * @function 接收请求参数，为我们生成Request对象
 *
 * 类似一个工具类 这里因为参数少 没必要用构建者new对象 用了个static类型的方法 不用new对象
 * 其实用作单例的对象更好 或者构建者的话 用对象更好
 */

public class CommonRequest {

    /**
     *
     * @param url
     * @param requestParams
     * @return 通过传入的参数返回一个Post类型的请求 返回一个创建好的Request对象
     */
    public static Request createPostRequest(String url, RequestParams requestParams) {
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        // 往里面放参数
        if (requestParams != null) {
            // 遍历map
            for (Map.Entry<String, String> entry : requestParams.urlParams.entrySet()) {
                // 将请求参数遍历添加到我们的请求构建类中。
                formBodyBuilder.add(entry.getKey(), entry.getValue());
            }
        }

        // 通过请求构建类（FormBody.Builder）的build方法获取到真正的请求体对象
        FormBody formBody = formBodyBuilder.build();

        // 也是用构建者模式
        return new Request.Builder()
                .url(url)
                .post(formBody) // post方法所以有body
                .build();
    }

    /**
     *
     * @param url
     * @param requestParams
     * @return 通过传入的参数返回一个Get类型的请求
     */
    public static Request createGetRequest(String url, RequestParams requestParams) {
        // 字符串的拼接 StringBuilder效率更高
        StringBuilder urlBuilder = new StringBuilder(url).append("?");
        if (requestParams != null) {
            // 遍历map
            for (Map.Entry<String, String> entry : requestParams.urlParams.entrySet()) {
                // 将请求参数遍历添加到我们的请求构建类中。
                urlBuilder.append(entry.getKey()).append("=")
                        .append(entry.getValue()).append("&");
            }
        }

        // 去掉最后多余的那个“&”符号 或者说那个问号（requestParams为空的情况下）
        return new Request.Builder().url(urlBuilder.substring(0, urlBuilder.length() - 1))
                .get().build();
    }


    /**
     * @param url
     * @param params
     * @return
     */
    public static Request createMonitorRequest(String url, RequestParams params) {
        StringBuilder urlBuilder = new StringBuilder(url).append("&");
        if (params != null && params.hasParams()) {
            for (Map.Entry<String, String> entry : params.urlParams.entrySet()) {
                urlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        return new Request.Builder().url(urlBuilder.substring(0, urlBuilder.length() - 1)).get().build();
    }

}
























