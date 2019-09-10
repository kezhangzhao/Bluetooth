package com.kzz.bluetoothlibrary.interfaces;

import android.bluetooth.BluetoothDevice;

/**
 * author : zhangzhao.ke
 * time   : 2019/09/10
 * desc   : 蓝牙设备列表的item点击事件
 */

public interface DeviceItemClickListener {
    void onItemClick(BluetoothDevice dev);
}
