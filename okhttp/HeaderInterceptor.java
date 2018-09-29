package com.unipus.http.okhttp;

import com.unipus.util.CommonUtil;

import org.videolan.vlc.MainApplication;

import java.io.IOException;

import cn.jpush.android.api.JPushInterface;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 添加统一请求header
 * Created by lx on 2016/8/25.
 */
public class HeaderInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
//        if (!chain.request().url().encodedPath().startsWith("/sso")) {
        Request request = chain.request()
                .newBuilder()
                .addHeader("registrationid", JPushInterface.getRegistrationID(MainApplication.getAppContext()))
                .addHeader("os", "android")
                .addHeader("appversion", CommonUtil.getVersionName())
                .build();
        return chain.proceed(request);
//        }
//        return chain.proceed(chain.request());
    }

}
