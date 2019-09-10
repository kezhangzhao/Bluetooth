package com.kzz.bluetoothlibrary.interfaces;

import android.bluetooth.BluetoothDevice;

/**
 * author : zhangzhao.ke
 * time   : 2019/09/10
 * desc   : 蓝牙扫描监听
 */

public interface BluetoothScanListener {
    void foundDev(BluetoothDevice dev);
}
