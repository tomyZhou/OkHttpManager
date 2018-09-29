package com.unipus.http.okhttp;

import java.util.List;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public abstract class MyCallback<T> {
    public Class cLass;

    /**
     * UI Thread
     *
     * @param request
     */
    public void onBefore(Request request) {
    }

    /**
     * UI Thread
     *
     * @param
     */
    public void onAfter() {
    }


    /**
     * Thread Pool Thread
     *
     * @param response
     */
    public abstract T parseNetworkResponse(String response);

    public void onError(int errorCode, String errorMsg) {

    }

    public void onSuccess(String response) {
        parseNetworkResponse(response);
    }

    public abstract void onResponse(T response);               //"rs":{"name":"aa","age":18}

    public abstract void onResponse(String response);    //"rs":"操作成功"

    public abstract void onResponseList(List<T> reposne);      //"rs":[{"id":11,"image":"xxx.jpg"},{"id":2,"image":"xxx.jpg"}]


}