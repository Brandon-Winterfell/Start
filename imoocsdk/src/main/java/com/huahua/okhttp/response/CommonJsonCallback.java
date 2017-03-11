package com.huahua.okhttp.response;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.huahua.okhttp.exception.OkHttpException;
import com.huahua.okhttp.listener.DisposeDataHandle;
import com.huahua.okhttp.listener.DisposeDataListener;
import com.huahua.util.Logger;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/2/13.
 * @function 专门处理JSON的回调响应
 */

public class CommonJsonCallback implements Callback {

    // 与服务器返回的字段的一个对应关系
    protected final String RESULT_CODE = "ecode"; // 有返回则对于http请求来说是成功的，但还有可能是业务逻辑上的错误
    protected final int RESULT_CODE_VALUE = 0;
    protected final String ERROR_MSG = "emsg";
    protected final String EMPTY_MSG = "";

    // 自定义异常类型
    protected final int NETWORK_ERROR = -1; // the network relative error
    protected final int JSON_ERROR = -2; // the JSON relative error
    protected final int OTHER_ERROR = -3; // the unknow error

    private Handler mDeliveryHandler; // 进行消息的转发 将其它线程的数据转发到UI线程
    private DisposeDataListener mListener; // 回调
    private Class<?> mClass;

    // 构造方法
    public CommonJsonCallback(DisposeDataHandle handle) {
        this.mListener = handle.mListener;
        this.mClass = handle.mClass;
        this.mDeliveryHandler = new Handler(Looper.getMainLooper());
    }

    // 请求失败处理，将失败的原因告诉应用层
    // 将异常抛到应用层，应用层会根据不用的异常做不同的处理
    @Override
    public void onFailure(Call call, final IOException e) {
        Logger.debug("huahua", "CommonJsonCallback  onFailure >>>>");
        e.printStackTrace();

        /**
         * 此时还在非UI线程，因此要转发
         */
        mDeliveryHandler.post(new Runnable() {
            @Override
            public void run() {
                // NETWORK_ERROR 恒等于-1 感觉这么宽泛的
                mListener.onFailure(new OkHttpException(NETWORK_ERROR, e));
            }
        });
    }

    // 收到服务器的响应，真正的响应处理函数 为什么这里要将IOException抛出去
    @Override
    public void onResponse(Call call, Response response) throws IOException {
        Logger.debug("huahua", "CommonJsonCallback onResponse >>>>> ");

        final String result = response.body().string();

        Logger.debug("huahua",
                "CommonJsonCallback onResponse >>>>> response.body().string() " + result );

        mDeliveryHandler.post(new Runnable() {
            @Override
            public void run() {
                
                handleResponse(result);
                
            }
        });
    }

    /**
     * 处理服务器返回的响应数据
     * @param responseObj
     */
    private void handleResponse(Object responseObj) {

        if (responseObj == null || responseObj.toString().trim().equals("")) {
            // 这里还是认为是网络的异常 因为服务器还是没有给我返回真正的数据
            mListener.onFailure(new OkHttpException(NETWORK_ERROR, EMPTY_MSG));

            return;
        }

        try {
            // 这里是将数据转换成json对象
            JSONObject result = new JSONObject(responseObj.toString());

//            // 用gson转换的话，还需要一个bean类呀  不是用在这里 用在下面
//            Gson gson = new Gson();
//            JSONObject result = (JSONObject) gson.fromJson((String) responseObj, mClass);

            // TODO 网页右边的代码比这个写得好 根本没有判断是否有RESULT_CODE等 这是不是多余的
            // 开始尝试解析json
            if (result.has(RESULT_CODE)) {
                // 从json对象中取出我们的响应码，若为0，则是正确的响应。0是跟服务器商量好的
                if (result.getInt(RESULT_CODE) == RESULT_CODE_VALUE) {
                    if(mClass == null) {
                        // 不需要进行转换(不需要解析) 不做处理 直接返回数据
                        mListener.onSuccess(responseObj);
                    } else {
                        // 需要将json对象转化成实体对象
                        // 这里才用到gson，其实上面也可以用的吧
                        Gson gson = new Gson();
                        Object obj = gson.fromJson(responseObj.toString(), mClass);
                        // 判断是否转换成功了 不为空 表明正确的转为了实体对象
                        if (obj != null) {
                            mListener.onSuccess(obj);
                        } else {
                            // 返回的不是合法的json
                            mListener.onFailure(new OkHttpException(JSON_ERROR, EMPTY_MSG));
                        }
                    }
                } else {
                    // 服务器返回0表示正常，返回其他数字例如1表示不正常
                    // 将服务器返回给我们的异常回调到应用层去处理
                    mListener.onFailure(new OkHttpException(OTHER_ERROR, result.get(RESULT_CODE)));
                }
            } else  {
                // TODO 这里还有一个 如果没有那个字段了
            }

        } catch (Exception e) {
            // 每一步都有可能有问题
            mListener.onFailure(new OkHttpException(OTHER_ERROR, e.getMessage()));
        }
    }
}

























