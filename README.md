# OkHttpManager
用过鸿洋大神封装的OkHttpUtil，封装的挺好，用起来挺方便，但我觉得太复杂了，而且有的地方不满足我的要求，用起来不是很得心应手，  

于是我自己写了个简单的，自己用的顺手。我自己写最大的一个原因是：服务器端返回的数据rs字段里面有的时候是个对象，有的时候是个字符串，  

有的时候是个数组。常规网上介绍的都只能满足返回对象一种情况。我的这个框架能接受三种类型的返回值。

我们服务器返回的数据类型如下：


返回字符串：

 {
  "isReview": false,
   "rs": {},
   "msg": "服务器异常",
   "code": 500
}


返回对象：
{

	 "rs": {
		 "new_msg": false,
		 "id": 1
	 },
	 "msg": "SUCCESS",
	 "code": 0
}

返回数组:

{

	 "rs": [
	   {
		  "bookId":1234
		  "bookName":"aaaa"
		  
	   },
	   {
	     "bookId":1235
		 "bookName":"bbbb"
	   },
	   {
		  "bookId":1236
		  "bookName":"cccc"
	   }
	 
	 ],
	 "msg": "SUCCESS",
	 "code": 0
}

使用方法：

    1.返回字符串：
	
	     HashMap params = new HashMap();
   
         OkHttpManager.getInstance().post(Constants.URL_BOOK_MARKET, params,
                new MyGenericCallback(String.class){
                 

                    @Override
                    public void onResponseString(String response) {
						
                    }
					
					@Override
                    public void onError(int errorCode, String errorMsg) {

                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        ptr_frame.refreshComplete();
                    }
                });
	

    1. 返回对象
	
	     HashMap params = new HashMap();
   
         OkHttpManager.getInstance().post(Constants.URL_BOOK_MARKET, params,
                new MyGenericCallback<WeikeDataBean>(WeikeDataBean.class) {
                 

                    @Override
                    public void onResponse(WeikeDataBean weikeDataBean) {
						
                    }
					
					@Override
                    public void onError(int errorCode, String errorMsg) {

                    }

                    @Override
                    public void onAfter() {
                        super.onAfter();
                        ptr_frame.refreshComplete();
                    }
                });
				
	2.返回数组
	
		HashMap params = new HashMap();

        params.put("type", "1");

        OkHttpManager.getInstance().post(Constants.URL_MY_STORE, params, new MyGenericCallback<Books>(Books.class) {

            @Override
            public void onResponseList(List<Books> list) {
                super.onResponseList(list);
                books.clear();
                if (mAdapter != null) {
                    if (list != null && list.size() > 0) {
                        listview.setVisibility(View.VISIBLE);
                        ptr_frame.setVisibility(View.VISIBLE);
                        books.clear();
                        books.addAll(list);
                    } else {
                        listview.setVisibility(View.GONE);
                        ptr_frame.setVisibility(View.GONE);
                        tv_empty.setVisibility(View.VISIBLE);
                    }
                    listDataSave.setDataList("my_store_weike_" + AccountManager.getZhijiaoUserInfo().id, list);
                    mAdapter.notifyDataSetChanged();
                }
            }
			
			  @Override
            public void onAfter() {
                super.onAfter();
                closeDialog();
                ptr_frame.refreshComplete();
            }

        });
		
	
    其他特点：
	
	1.输入参数可以是map，也可以使string，也可以是json，也可以是MultiPart文件上传。例如，下面这个”意见反馈“的应用场景是带文本和图片同时上传的：
	
	    private File file1;
		private File file2;
		private File file3;
	
        Map params = new HashMap();
        params.put("feedback", content);
        params.put("phone", phone);
        params.put("photo_one", file1);
        params.put("photo_two", file2);
        params.put("photo_three", file3);

        OkHttpManager.getInstance().postMultiPart(ZhijiaoConstants.ZHIJIAO_NOMAL_FEEDBACK, params, new MyGenericCallback<EmptySuccessResultBean>(EmptySuccessResultBean.class) {

            @Override
            public void onResponse(EmptySuccessResultBean successResultBean) {
                super.onResponse(successResultBean);
                ToastUtil.show("感谢亲故的反馈，内容已提交");
                finish();
            }
        });
		
		注意上面的EmptySuccessResultBean接收rs为 {}的空对象的
		
	
	2.我使用了网络上大神的Klog控件格式化打印json返回值，方便调试。compile ‘com.github.zhaokaiqiang.klog:library:0.0.1’
	
	3.添加公共参数和header
