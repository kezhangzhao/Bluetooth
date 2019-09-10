package com.kzz.bluetoothlibrary.interfaces;

/**
 * author : zhangzhao.ke
 * time   : 2019/09/10
 * desc   : 接收(读取)数据回调
 */

public interface ReadCallback {

    void reading(String filePath);

    void fail(String failReason);

    void complete(String sendMsg);
}
