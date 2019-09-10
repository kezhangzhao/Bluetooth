package com.kzz.bluetoothlibrary.bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kzz.bluetoothlibrary.R;
import com.kzz.bluetoothlibrary.interfaces.DeviceItemClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class BtDevAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected static final String TAG = BtDevAdapter.class.getSimpleName();
    protected final List<BluetoothDevice> mDevices = new ArrayList<>();
    protected DeviceItemClickListener onItemClickListener;

    public BtDevAdapter() {
        addBound();
    }

    protected void addBound() {
        Set<BluetoothDevice> bondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (bondedDevices != null)
            mDevices.addAll(bondedDevices);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dev, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        BluetoothDevice dev = mDevices.get(position);
        String name = dev.getName();
        String address = dev.getAddress();
        int bondState = dev.getBondState();
        VH vh = (VH) holder;
        vh.name.setText(name == null ? "" : name);
        vh.address.setText(String.format("%s (%s)", address, bondState == 10 ? "未配对" : "配对"));
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    public void setOnItemClickListener(DeviceItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void add(BluetoothDevice dev) {
        if (mDevices.contains(dev))
            return;
        mDevices.add(dev);
        notifyDataSetChanged();
    }

    public void reScan() {
        mDevices.clear();
        addBound();
        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
        if (!bt.isDiscovering())
            bt.startDiscovery();
        notifyDataSetChanged();
    }

    protected class VH extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView name;
        final TextView address;

        protected VH(final View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            name = itemView.findViewById(R.id.name);
            address = itemView.findViewById(R.id.address);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            Log.d(TAG, "onClick, getAdapterPosition=" + pos);
            if (pos >= 0 && pos < mDevices.size() && onItemClickListener != null)
                onItemClickListener.onItemClick(mDevices.get(pos));
        }
    }

}
