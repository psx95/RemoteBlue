package com.psx.projectcontrol;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by user on 03-07-2016.
 */
public class BluetoothDevicesAdapter extends RecyclerView.Adapter<BluetoothDevicesAdapter.MyViewHolder> {

    int count = 0;
    List<BluetoothDevice> bonded_devices;
    @Override
    public BluetoothDevicesAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_row_list,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(BluetoothDevicesAdapter.MyViewHolder holder, int position) {
        BluetoothDevice bluetoothDevice = bonded_devices.get(position);
        if (bluetoothDevice!=null) {
            if (bluetoothDevice.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.COMPUTER_LAPTOP) {
                holder.name.setText(bluetoothDevice.getName());
                holder.device_type.setImageResource(R.drawable.laptop);
                count += 1;
            } else {
                holder.name.setText(bluetoothDevice.getName());
                holder.device_type.setImageResource(R.drawable.smartphone);
            }
        }
    }

    @Override
    public int getItemCount() {
        return bonded_devices.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView device_type;
        public MyViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.device_name);
            device_type = (ImageView) itemView.findViewById(R.id.device_type);

        }
    }

    //public constructor
    public BluetoothDevicesAdapter(List<BluetoothDevice> bonded_devices){
        this.bonded_devices = bonded_devices;
    }

}
