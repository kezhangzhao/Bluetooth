package com.kzz.bluetoothlibrary.interfaces;

import android.bluetooth.BluetoothDevice;

/**
 * author : zhangzhao.ke
 * time   : 2019/09/10
 * desc   : 连接监听
 */

public interface ConnectListener {
    void connected(BluetoothDevice device);
    void disConnected(String failReason);
}
