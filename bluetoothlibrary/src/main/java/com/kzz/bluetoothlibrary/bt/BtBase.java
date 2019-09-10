package com.kzz.bluetoothlibrary.bt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import com.kzz.bluetoothlibrary.Config;
import com.kzz.bluetoothlibrary.interfaces.ConnectListener;
import com.kzz.bluetoothlibrary.interfaces.ReadCallback;
import com.kzz.bluetoothlibrary.interfaces.SendCallback;
import com.kzz.bluetoothlibrary.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.UUID;


/**
 * 客户端和服务端的基类，用于管理socket长连接
 */
public class BtBase {
    static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static  String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/bluetooth/";
    private static final int FLAG_MSG = 0;  //消息标记
    private static final int FLAG_FILE = 1; //文件标记

    private BluetoothSocket mSocket;
    private DataOutputStream mOut;

    private ConnectListener connectListener;
    private boolean isRead;
    private boolean isSending;

    /**
     * 设置蓝牙连接监听
     * @param connectListener 蓝牙连接监听
     */
    public void setConnectListener(ConnectListener connectListener) {
        this.connectListener = connectListener;
    }

    /**
     * 设置文件接收存放的路径
     * @param filePath 路径
     */
    public void setFilePath(String filePath) {
        FILE_PATH = filePath;
    }
    public String getFilePath() {
        return FILE_PATH;
    }


    /**
     * 循环读取对方数据(若没有数据，则阻塞等待)
     */
    void loopRead(BluetoothSocket socket, ReadCallback readCallback) {
        mSocket = socket;
        try {
            if (!mSocket.isConnected())
                mSocket.connect();
            connectNotifyUI(Config.CONNECTED, mSocket.getRemoteDevice());
            mOut = new DataOutputStream(mSocket.getOutputStream());
            DataInputStream in = new DataInputStream(mSocket.getInputStream());
            isRead = true;
            while (isRead) { //死循环读取
                switch (in.readInt()) {
                    case FLAG_MSG: //读取短消息
                        readNotifyUI(Config.READ_ING, "address: " + mSocket.getRemoteDevice().getAddress()
                                + " name: " + mSocket.getRemoteDevice().getName(), readCallback);
                        String msg = in.readUTF();
//                        notifyUI(Listener.MSG, "接收短消息：" + msg);
                        readNotifyUI(Config.READ_COMPLETE, msg, readCallback);
                        break;
                    case FLAG_FILE: //读取文件
                        Util.mkdirs(FILE_PATH);
                        String fileName = in.readUTF(); //文件名
                        long fileLen = in.readLong(); //文件长度
                        // 读取文件内容
                        long len = 0;
                        int r;
                        byte[] b = new byte[4 * 1024];
                        FileOutputStream out = new FileOutputStream(FILE_PATH + fileName);
//                        notifyUI(Listener.MSG, "正在接收文件(" + fileName + "),请稍后...");
                        readNotifyUI(Config.READ_ING, FILE_PATH + fileName, readCallback);
                        while ((r = in.read(b)) != -1) {
                            out.write(b, 0, r);
                            len += r;
                            if (len >= fileLen)
                                break;
                        }
//                        notifyUI(Listener.MSG, "文件接收完成(存放在:" + FILE_PATH + ")");
                        readNotifyUI(Config.READ_COMPLETE, FILE_PATH + fileName, readCallback);
                        break;
                }
            }
        } catch (Throwable e) {
            close();
        }
    }

    /**
     * 发送短消息
     */
    public void sendMsg(String msg, SendCallback sendCallback) {
        if (checkSend(sendCallback)) return;
        isSending = true;
        sendNotifyUI(Config.SEND_ING, msg, sendCallback);
        try {
            mOut.writeInt(FLAG_MSG); //消息标记
            mOut.writeUTF(msg);
            mOut.flush();
//            notifyUI(Listener.MSG, "发送短消息：" + msg);
            sendNotifyUI(Config.SEND_COMPLETE, msg, sendCallback);
        } catch (Throwable e) {
            close();
            sendNotifyUI(Config.SEND_FAIL, e.toString(), sendCallback);
        }
        isSending = false;
    }

    /**
     * 发送文件
     */
    public void sendFile(final String filePath, final SendCallback sendCallback) {
        if (checkSend(sendCallback)) return;
        isSending = true;
        Util.EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {
//                    notifyUI(Listener.MSG, "正在发送文件(" + filePath + "),请稍后...");
                    sendNotifyUI(Config.SEND_ING, filePath, sendCallback);
                    FileInputStream in = new FileInputStream(filePath);
                    File file = new File(filePath);
                    mOut.writeInt(FLAG_FILE); //文件标记
                    mOut.writeUTF(file.getName()); //文件名
                    mOut.writeLong(file.length()); //文件长度
                    int r;
                    byte[] b = new byte[4 * 1024];
                    while ((r = in.read(b)) != -1)
                        mOut.write(b, 0, r);
                    mOut.flush();
//                    notifyUI(Listener.MSG, "文件发送完成.");
                    sendNotifyUI(Config.SEND_COMPLETE, filePath, sendCallback);
                } catch (Throwable e) {
                    close();
                    sendNotifyUI(Config.SEND_FAIL, filePath, sendCallback);
                }
                isSending = false;
            }
        });
    }

    /**
     * 释放监听引用(例如释放对Activity引用，避免内存泄漏)
     */
    public void unConnectListener() {
        connectListener = null;
    }

    /**
     * 关闭Socket连接
     */
    public void close() {
        try {
            isRead = false;
            mSocket.close();
            connectNotifyUI(Config.DISCONNECTED, null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 当前设备与指定设备是否连接
     */
    public boolean isConnected(BluetoothDevice dev) {
        boolean connected = (mSocket != null && mSocket.isConnected());
        if (dev == null)
            return connected;
        return connected && mSocket.getRemoteDevice().equals(dev);
    }

    // ============================================通知UI===========================================================
    private boolean checkSend(SendCallback sendCallback) {
        if (isSending) {
//            Toast.makeText(activity, "正在发送其它数据,请稍后再发...", Toast.LENGTH_SHORT).show();
            sendNotifyUI(Config.SEND_FAIL, "正在发送其它数据,请稍后再发...", sendCallback);
            return true;
        }
        return false;
    }

    /**
     * 蓝牙连接监听
     *
     * @param state  状态码
     * @param device 蓝牙设备
     */
    private void connectNotifyUI(final int state, final BluetoothDevice device) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (connectListener != null) {
                    try {
                        switch (state) {
                            case Config.CONNECTED:
                                connectListener.connected(device);
                                break;
                            case Config.DISCONNECTED:
                                connectListener.disConnected(null);
                                break;
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        connectListener.disConnected(e.toString());
                    }
                }
            }
        });
    }

    /**
     * 发送数据回调转换到主线程
     *
     * @param state        状态码
     * @param string       msg
     * @param sendCallback 回调
     */
    private void sendNotifyUI(final int state, final String string, final SendCallback sendCallback) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (sendCallback != null) {
                    try {
                        switch (state) {
                            case Config.SEND_ING:
                                sendCallback.sending(string);
                                break;
                            case Config.SEND_COMPLETE:
                                sendCallback.complete(string);
                                break;
                            default:
                                sendCallback.fail(string);
                                break;
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        sendCallback.fail(e.toString());
                    }
                }
            }
        });
    }

    /**
     * 读取数据回调转换到主线程
     *
     * @param state        状态码
     * @param string       msg
     * @param readCallback 回调
     */
    private void readNotifyUI(final int state, final String string, final ReadCallback readCallback) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (readCallback != null) {
                    try {
                        switch (state) {
                            case Config.READ_ING:
                                readCallback.reading(string);
                                break;
                            case Config.READ_COMPLETE:
                                readCallback.complete(string);
                                break;
                            default:
                                readCallback.fail(string);
                                break;
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        readCallback.fail(e.toString());
                    }
                }
            }
        });
    }
}
