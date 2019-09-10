package com.kzz.bluetoothlibrary;

/**
 * author : zhangzhao.ke
 * time   : 2019/09/10
 * desc   : 配置
 */

public class Config {
    //发送数据状态的回调码
    public static final int SEND_FAIL = -1;
    public static final int SEND_ING = 0;
    public static final int SEND_COMPLETE = 1;
    //读取数据状态的回调码
    public static final int READ_FAIL = -1;
    public static final int READ_ING = 0;
    public static final int READ_COMPLETE = 1;
    //连接监听状态码
    public static final int DISCONNECTED = 0;
    public static final int CONNECTED = 1;
}
