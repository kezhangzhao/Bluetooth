package com.kzz.bluetooth.bt;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kzz.bluetooth.R;
import com.kzz.bluetoothlibrary.interfaces.BluetoothScanListener;
import com.kzz.bluetoothlibrary.interfaces.ConnectListener;
import com.kzz.bluetoothlibrary.interfaces.DeviceItemClickListener;
import com.kzz.bluetoothlibrary.interfaces.ReadCallback;
import com.kzz.bluetoothlibrary.interfaces.SendCallback;
import com.kzz.bluetoothlibrary.bt.BtClient;
import com.kzz.bluetoothlibrary.bt.BtDevAdapter;
import com.kzz.bluetoothlibrary.util.BtReceiver;

import java.io.File;


public class BtClientActivity extends Activity {

    private TextView mTips;
    private EditText mInputMsg;
    private EditText mInputFile;
    private TextView mLogs;
    private BtReceiver mBtReceiver;
    private BtDevAdapter mBtDevAdapter;
    private BtClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btclient);
        RecyclerView rv = findViewById(R.id.rv_bt);
        mTips = findViewById(R.id.tv_tips);
        mInputMsg = findViewById(R.id.input_msg);
        mInputFile = findViewById(R.id.input_file);
        mLogs = findViewById(R.id.tv_log);

        rv.setLayoutManager(new LinearLayoutManager(this));
        mBtDevAdapter = new BtDevAdapter();
        mBtDevAdapter.setOnItemClickListener(new DeviceItemClick());
        rv.setAdapter(mBtDevAdapter);

        mClient = new BtClient();
        mClient.setConnectListener(new MyConnectListener());////设置蓝牙连接监听
        mClient.setFilePath(Environment.getExternalStorageDirectory().getAbsolutePath() + "/bluetoothNew/");//设置蓝牙接收文件存放的路径
        mBtReceiver = new BtReceiver(this);//注册蓝牙广播
        mBtReceiver.setScanListener(new ScanListener());//设置蓝牙扫描监听
        BluetoothAdapter.getDefaultAdapter().startDiscovery();//开启蓝牙扫描
    }

    /**
     * 重新扫描
     *
     * @param view view
     */
    public void reScan(View view) {
        mBtDevAdapter.reScan();
    }

    /**
     * 发送文字数据
     *
     * @param view view
     */
    public void sendMsg(View view) {
        if (mClient.isConnected(null)) {
            String msg = mInputMsg.getText().toString();
            if (TextUtils.isEmpty(msg))
                Toast.makeText(this, "消息不能空", Toast.LENGTH_SHORT).show();
            else
                mClient.sendMsg(msg, new MySendCallback());
        } else
            Toast.makeText(this, "没有连接", Toast.LENGTH_SHORT).show();
    }

    /**
     * 发送文件数据
     *
     * @param view view
     */
    public void sendFile(View view) {
        if (mClient.isConnected(null)) {
            String filePath = mInputFile.getText().toString();
            if (TextUtils.isEmpty(filePath) || !new File(filePath).isFile())
                Toast.makeText(this, "文件无效", Toast.LENGTH_SHORT).show();
            else
                mClient.sendFile(filePath, new MySendCallback());
        } else
            Toast.makeText(this, "没有连接", Toast.LENGTH_SHORT).show();
    }

    /**
     * 发送数据回调
     */
    class MySendCallback implements SendCallback {

        @Override
        public void sending(String sendMsg) {
            String str = String.format("\n%s", "正在发送:" + sendMsg);
            mLogs.append(str);
        }

        @Override
        public void fail(String failReason) {
            failReason = String.format("\n%s", "发送失败:" + failReason);
            mLogs.append(failReason);
        }

        @Override
        public void complete(String sendMsg) {
            sendMsg = String.format("\n%s", "发送完成:" + sendMsg);
            mLogs.append(sendMsg);
        }
    }

    /**
     * 读取数据回调
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
            if (TextUtils.isEmpty(failReason))
                failReason = "连接断开";
            mTips.setText(failReason);
        }
    }

    class ScanListener implements BluetoothScanListener {

        @Override
        public void foundDev(BluetoothDevice dev) {
            mBtDevAdapter.add(dev);
        }
    }

    class DeviceItemClick implements DeviceItemClickListener {

        @Override
        public void onItemClick(BluetoothDevice dev) {
            if (mClient.isConnected(dev)) {
                Toast.makeText(BtClientActivity.this, "已经连接了", Toast.LENGTH_SHORT).show();
                return;
            }
            mClient.connect(dev, new MyReadCallback());
            Toast.makeText(BtClientActivity.this, "正在连接...", Toast.LENGTH_SHORT).show();
            mTips.setText("正在连接...");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBtReceiver);
        mClient.unConnectListener();
        mClient.close();
    }
}