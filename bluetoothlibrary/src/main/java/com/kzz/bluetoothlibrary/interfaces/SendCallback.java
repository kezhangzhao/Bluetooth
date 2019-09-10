package com.kzz.bluetoothlibrary.interfaces;

/**
 * author : zhangzhao.ke
 * time   : 2019/09/10
 * desc   : 发送数据回调
 */

public interface SendCallback {
    void sending(String sendMsg);

    void fail(String failReason);

    void complete(String sendMsg);
}
