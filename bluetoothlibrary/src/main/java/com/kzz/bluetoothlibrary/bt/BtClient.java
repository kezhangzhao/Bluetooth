package com.kzz.bluetoothlibrary.bt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.kzz.bluetoothlibrary.interfaces.ReadCallback;
import com.kzz.bluetoothlibrary.util.Util;


/**
 * 客户端，与服务端建立长连接
 */
public class BtClient extends BtBase {

    /**
     * 与远端设备建立长连接
     *
     * @param dev 远端设备
     */
    public void connect(BluetoothDevice dev, final ReadCallback readCallback) {
        close();
        try {
//             final BluetoothSocket socket = dev.createRfcommSocketToServiceRecord(SPP_UUID); //加密传输，Android系统强制配对，弹窗显示配对码
            final BluetoothSocket socket = dev.createInsecureRfcommSocketToServiceRecord(SPP_UUID); //明文传输(不安全)，无需配对
            // 开启子线程
            Util.EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    loopRead(socket,readCallback); //循环读取
                }
            });
        } catch (Throwable e) {
            close();
        }
    }
}