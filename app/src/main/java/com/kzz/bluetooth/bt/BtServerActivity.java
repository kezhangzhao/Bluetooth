package com.kzz.bluetooth.bt;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kzz.bluetooth.R;
import com.kzz.bluetoothlibrary.interfaces.ConnectListener;
import com.kzz.bluetoothlibrary.interfaces.ReadCallback;
import com.kzz.bluetoothlibrary.interfaces.SendCallback;
import com.kzz.bluetoothlibrary.bt.BtServer;

import java.io.File;


public class BtServerActivity extends Activity {
    private TextView mTips;
    private EditText mInputMsg;
    private EditText mInputFile;
    private TextView mLogs;
    private BtServer mServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btserver);
        mTips = findViewById(R.id.tv_tips);
        mInputMsg = findViewById(R.id.input_msg);
        mInputFile = findViewById(R.id.input_file);
        mLogs = findViewById(R.id.tv_log);

        mServer = new BtServer(new MyReadCallback());//设置蓝牙读取数据监听
        mServer.setConnectListener(new MyConnectListener());//设置蓝牙连接监听
        mServer.setFilePath( Environment.getExternalStorageDirectory().getAbsolutePath() + "/bluetoothNew/");//设置蓝牙接收文件存放的路径
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mServer.unConnectListener();
        mServer.close();
    }

    /**
     * 发送文字
     * @param view view
     */
    public void sendMsg(View view) {
        if (mServer.isConnected(null)) {
            String msg = mInputMsg.getText().toString();
            if (TextUtils.isEmpty(msg))
                Toast.makeText(this, "消息不能空", Toast.LENGTH_SHORT).show();
            else
                mServer.sendMsg(msg, new MySendCallback());
        } else
            Toast.makeText(this, "没有连接", Toast.LENGTH_SHORT).show();
    }

    /**
     * 发送文件
     * @param view view
     */
    public void sendFile(View view) {
        if (mServer.isConnected(null)) {
            String filePath = mInputFile.getText().toString();
            if (TextUtils.isEmpty(filePath) || !new File(filePath).isFile())
                Toast.makeText(this, "文件无效", Toast.LENGTH_SHORT).show();
            else
                mServer.sendFile(filePath, new MySendCallback());
        } else
            Toast.makeText(this, "没有连接", Toast.LENGTH_SHORT).show();
    }

    /**
     * 数据发送回调
     */
    public class MySendCallback implements SendCallback {

        @Override
        public void sending(String sendMsg) {
            sendMsg = String.format("\n%s", "正在发送：" + sendMsg);
            mLogs.append(sendMsg);
        }

        @Override
        public void fail(String failReason) {
            failReason = String.format("\n%s", "发送失败：" + failReason);
            mLogs.append(failReason);
        }

        @Override
        public void complete(String sendMsg) {
            sendMsg = String.format("\n%s", "发送完成：" + sendMsg);
            mLogs.append(sendMsg);
        }
    }

    /**
     * 读取消息回调
     */
    public class MyReadCallback implements ReadCallback {

        @Override
        public void reading(String filePath) {
            filePath = String.format("\n%s", "正在接收：" + filePath);
            mLogs.append(filePath);
        }

        @Override
        public void fail(String failReason) {
            failReason = String.format("\n%s", "接收失败：" + failReason);
            mLogs.append(failReason);
        }

        @Override
        public void complete(String sendMsg) {
            sendMsg = String.format("\n%s", "接收完成：" + sendMsg);
            mLogs.append(sendMsg);
        }
    }

    /**
     * 蓝牙连接监听
     */
    public class MyConnectListener implements ConnectListener {

        @Override
        public void connected(BluetoothDevice device) {
            String str = String.format("与%s(%s)连接成功", device.getName(), device.getAddress());
            mTips.setText(str);
        }

        @Override
        public void disConnected(String failReason) {
            mServer.listen(new MyReadCallback());
            if (TextUtils.isEmpty(failReason))
                failReason = "连接断开,正在重新监听...";
            mTips.setText(failReason);
        }
    }
}