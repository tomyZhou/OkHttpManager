package com.unipus.http.okhttp;

import android.text.TextUtils;

import com.google.gson.JsonSyntaxException;
import com.unipus.common.Constant;
import com.unipus.util.GsonUtils;
import com.unipus.util.ToastUtil;
import com.unipus.util.LogUtils;
import com.unipus.common.AccountManager;
import com.unipus.util.JsonTools;

import org.json.JSONException;

import java.util.List;
import java.util.Map;

import okhttp3.Request;

public abstract class MyGenericCallback<T> extends MyCallback<T> {

    public MyGenericCallback(Class cLass) {
        this.cLass = cLass;
    }

    @Override
    public T parseNetworkResponse(String response) {
        try {
            String json = response;
            if (!TextUtils.isEmpty(json)) {
                Map<String, String> resultMap = JsonTools.toMap(json);
                if (resultMap != null && resultMap.containsKey("code")) {
                    String mCode = resultMap.get("code");
                    String message = resultMap.get("msg");
                    String rs = resultMap.get("rs");
                    Constant.isReview = resultMap.get("isReview");

                    if ("0".equals(mCode)) {
                        if (!TextUtils.isEmpty(rs)) {
                            T object = parseJson(rs);
                            if (object instanceof String) {
                                onResponse((String) object);
                            } else if (object instanceof List) {
                                onResponseList((List<T>) object);
                            } else {
                                onResponse(object);
                            }
                        } else {
                            onResponse(message);
                        }
                    } else if ("2".equals(mCode)) {
                        AccountManager.userLogout();
                    } else {
                        onError(Integer.valueOf(mCode), message);
                    }
                } else {
                    ToastUtil.show("返回数据错误");
                }
            } else {
                ToastUtil.show("返回数据错误");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 服务器返回的json数据的data部分解析成对象
     *
     * @param responseBody
     * @return
     */

    private T parseJson(String responseBody) {
        Object result = null;
        try {
            responseBody = responseBody.trim();
            if (responseBody.startsWith("{")) {
                result = GsonUtils.fromJson(responseBody, cLass);
            } else if (responseBody.startsWith("[")) {
                result = JsonTools.toListBeanNoKey(responseBody, cLass);
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            LogUtils.i("==e=====" + e.toString());
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return (T) result;
    }

    @Override
    public void onBefore(Request request) {
        super.onBefore(request);
    }

    @Override
    public void onAfter() {
        super.onAfter();
    }

    @Override
    public void onError(int errorCode, String errorMsg) {
        super.onError(errorCode, errorMsg);
        ToastUtil.show(errorMsg);
    }

    @Override
    public void onSuccess(String response) {
        super.onSuccess(response);
    }

    @Override
    public void onResponse(T response) {

    }

    @Override
    public void onResponse(String response) {

    }

    @Override
    public void onResponseList(List<T> reposne) {

    }
}