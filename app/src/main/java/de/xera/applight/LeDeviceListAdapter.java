package de.xera.applight;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LeDeviceListAdapter extends RecyclerView.Adapter<LeDeviceListAdapter.ViewHolder> {

    private List<BluetoothDevice> localDataSet;
    private Context context;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        Button connectButton;

        ViewHolder(View itemView) {
            super(itemView);
            deviceName = (TextView)itemView.findViewById(R.id.device_name);
            connectButton = (Button) itemView.findViewById(R.id.connectButton);
        }
    }

    public LeDeviceListAdapter(Context context) {
        this.localDataSet = new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public LeDeviceListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recycler_view_device, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeDeviceListAdapter.ViewHolder holder, int position) {
        holder.deviceName.setText(localDataSet.get(position).getName());
        holder.connectButton.setOnClickListener((button) -> {
            ((DeviceScanActivity) context).connectDevice(localDataSet.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    public void addDevice(BluetoothDevice device) {
        // Check if Device already exists
        if(!localDataSet.contains(device) || device.getName() != "null") {
            localDataSet.add(device);
            notifyDataSetChanged();
        }
    }
}
