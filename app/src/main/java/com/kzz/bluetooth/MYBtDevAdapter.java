package com.kzz.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kzz.bluetoothlibrary.bt.BtDevAdapter;

/**
 * author : zhangzhao.ke
 * time   : 2019/09/09
 * desc   : 这个是蓝牙扫码列表的adapter继承重新item的UI
 */

public class MYBtDevAdapter extends BtDevAdapter {

    @NonNull
    @Override
    public MyView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dev_spread, parent, false);
        return new MYBtDevAdapter.MyView(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        BluetoothDevice dev = mDevices.get(position);
        String name = dev.getName();
        String address = dev.getAddress();
        int bondState = dev.getBondState();
        MyView vh = (MyView) holder;
        vh.name.setText(name == null ? "" : name);
        vh.address.setText(String.format("%s (%s)", address, bondState == 10 ? "未配对" : "配对"));
    }

    class MyView extends VH {
        final TextView name;
        final TextView address;

        MyView(final View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            name = itemView.findViewById(R.id.tv_name);
            address = itemView.findViewById(R.id.tv_address);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            Log.d(TAG, "MYBtDevAdapter onClick, getAdapterPosition=" + pos);
        }
    }
}
