package com.unipus.http.okhttp;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.socks.library.KLog;
import com.unipus.common.AccountManager;
import com.unipus.util.CommonUtil;

import org.videolan.vlc.MainApplication;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by zhougang on 2018/9/13.
 */

public class OkHttpManager {

    /**
     * 网络访问要求singleton
     */
    private static OkHttpManager instance;

    // 必须要用的okhttpclient实例,在构造器中实例化保证单一实例
    private OkHttpClient mOkHttpClient;

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private Handler mHandler;

    private OkHttpManager() {
        /**
         * okHttp3中超时方法移植到Builder中
         */

        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:"
                        + Arrays.toString(trustManagers));
            }
            X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{trustManager}, null);
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();


            mOkHttpClient = (new OkHttpClient()).newBuilder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(new HeaderInterceptor())
                    .build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        mHandler = new Handler(Looper.getMainLooper());

    }

    public static OkHttpManager getInstance() {
        if (instance == null) {
            synchronized (OkHttpManager.class) {
                if (instance == null) {
                    instance = new OkHttpManager();
                }
            }
        }

        return instance;
    }

    public void initBuilder() {

    }

    /**
     * 对外提供的Get方法访问
     *
     * @param url
     * @param callBack
     */
    public void get(String url, MyCallback callBack) {
        /**
         * 通过url和GET方式构建Request
         */
        KLog.i(url);
        Request request = bulidRequestForGet(url);
        /**
         * 请求网络的逻辑
         */
        requestNetWork(request, callBack);
    }

    /**
     * GET方式构建Request
     *
     * @param url
     * @return
     */
    private Request bulidRequestForGet(String url) {

        return new Request.Builder()
                .url(url)
                .get()
                .build();
    }


    public void postMultiPart(String url, Map<String, String> parms, MyCallback callBack) {
        /**
         * 通过url和POST方式构建Request
         */
        KLog.i(url);
        Request request = bulidRequestForPostByForm(url, parms, true);
        /**
         * 请求网络的逻辑
         */
        requestNetWork(request, callBack);
    }

    /**
     * 对外提供的Post方法访问
     *
     * @param url
     * @param parms:   提交内容为表单数据
     * @param callBack
     */
    public void post(String url, Map parms, MyCallback callBack) {
        /**
         * 通过url和POST方式构建Request
         */
        KLog.i(url);
        Request request = bulidRequestForPostByForm(url, parms, false);
        /**
         * 请求网络的逻辑
         */
        requestNetWork(request, callBack);

    }

    /**
     * POST方式构建Request {Form}
     *
     * @param url
     * @param parms
     * @return
     */
    private Request bulidRequestForPostByForm(String url, Map<String, String> parms, boolean isMultiPart) {

        RequestBody body = null;

        if (isMultiPart) {
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            builder = initBuilder(parms, builder);
            body = builder.build();
        } else {
            FormBody.Builder builder = new FormBody.Builder();
            builder = initBuilder(parms, builder);
            body = builder.build();
        }
        return new Request.Builder()
                .url(url)
                .post(body)
                .build();
    }

    private MultipartBody.Builder initBuilder(Map<String, String> parms, MultipartBody.Builder builder) {
        if (AccountManager.getZhijiaoUserInfo() != null) {
            builder.addFormDataPart("oauth_token", AccountManager.getZhijiaoUserInfo().oauth_token);
            builder.addFormDataPart("openid", AccountManager.getZhijiaoUserInfo().openid);
        }

        String imei = "";
        if (!TextUtils.isEmpty(MainApplication.getIMEI())) {
            imei = MainApplication.getIMEI();
        } else {
            imei = "unkown";
        }
        builder.addFormDataPart("device_id", imei);
        builder.addFormDataPart("udid", CommonUtil.getUUID());
        builder.addFormDataPart("os", "android");
        builder.addFormDataPart("brand", Build.MODEL);
        builder.addFormDataPart("os_version", Build.VERSION.RELEASE);
        builder.addFormDataPart("app_version", CommonUtil.getVersionName());
        builder.addFormDataPart("lng", "0.0");
        builder.addFormDataPart("lat", "0.0");

        if (parms != null) {
            for (Map.Entry entry : parms.entrySet()) {
                if (entry.getValue() instanceof String) {
                    builder.addFormDataPart((String) entry.getKey(), (String) entry.getValue());
                } else if (entry.getValue() instanceof File) {
                    File file = (File) entry.getValue();
                    builder.addFormDataPart((String) entry.getKey(), file.getName(), RequestBody.create(null, file));
                }

            }

        }

        return builder;
    }

    private FormBody.Builder initBuilder(Map<String, String> parms, FormBody.Builder builder) {
//        if (MainApplication.getUser() != null) {
//            if (MainApplication.getUser().getOauth_token() != null) {
//                builder.add("oauth_token", MainApplication.getUser().getOauth_token());
//            }
//            if (MainApplication.getUser().getOpenid() != null) {
//                builder.add("openid", MainApplication.getUser().getOpenid());
//            }
//        }

        if (AccountManager.getZhijiaoUserInfo() != null) {
            builder.add("oauth_token", AccountManager.getZhijiaoUserInfo().oauth_token);
            builder.add("openid", AccountManager.getZhijiaoUserInfo().openid);
        }

        String imei = "";
        if (!TextUtils.isEmpty(MainApplication.getIMEI())) {
            imei = MainApplication.getIMEI();
        } else {
            imei = "unkown";
        }
        builder.add("device_id", imei);
        builder.add("udid", CommonUtil.getUUID());
        builder.add("os", "android");
        builder.add("brand", Build.MODEL);
        builder.add("os_version", Build.VERSION.RELEASE);
        builder.add("app_version", CommonUtil.getVersionName());
        builder.add("lng", "0.0");
        builder.add("lat", "0.0");

        if (parms != null) {
            for (Map.Entry entry : parms.entrySet()) {
                if (entry.getValue() instanceof String) {
                    builder.add((String) entry.getKey(), (String) entry.getValue());
                }
            }

        }

        return builder;
    }


    /**
     * 对外提供的Post方法访问
     *
     * @param url
     * @param json:    提交内容为json数据
     * @param callBack
     */
    public void post(String url, String json, MyCallback callBack) {
        /**
         * 通过url和POST方式构建Request
         */
        Request request = bulidRequestForPostByJson(url, json);


        /**
         * 请求网络的逻辑
         */
        requestNetWork(request, callBack);

    }

    /**
     * POST方式构建Request {json}
     *
     * @param url
     * @param json
     * @return
     */
    private Request bulidRequestForPostByJson(String url, String json) {
        RequestBody body = RequestBody.create(JSON, json);
        KLog.i(url);
        return new Request.Builder()
                .url(url)
                .post(body)
                .build();
    }

    private void requestNetWork(final Request request, final MyCallback callBack) {

        /**
         * 处理连网逻辑，此处只处理异步操作enqueue
         */
        callBack.onBefore(request);

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("okHttpError", "请求出错" + e.getMessage());
                    }
                });

            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {

                if (response.isSuccessful()) {
                    final String json = response.body().string();
                    KLog.json(json);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callBack.onSuccess(json);
                            callBack.onAfter();
                        }
                    });
                } else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callBack.onError(-1, "请求发生错误" + response.body());
                            callBack.onAfter();
                        }
                    });
                }
                response.body().close();
            }
        });
    }


}
