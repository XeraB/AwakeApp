package de.xera.applight;

import android.annotation.SuppressLint;
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

@SuppressLint("MissingPermission")
public class LeDeviceListAdapter extends RecyclerView.Adapter<LeDeviceListAdapter.ViewHolder> {

    private final List<BluetoothDevice> localDataSet;
    private final Context context;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        Button connectButton;

        ViewHolder(View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.device_name);
            connectButton = itemView.findViewById(R.id.connectButton);
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
        holder.connectButton.setOnClickListener(button ->
                ((DeviceScanActivity) context).connectDevice(localDataSet.get(position))
        );
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    public void addDevice(BluetoothDevice device) {
        //Check if device is not empty or if it already exists in the list
        if (device.getName() == null) {
            return;
        }
        if (localDataSet.contains(device)) {
            return;
        }
        localDataSet.add(device);
        notifyDataSetChanged();
    }
}
