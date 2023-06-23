package de.xera.applight;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Arrays;

public class DeviceScanActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LeDeviceListAdapter adapter;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private static final long SCAN_PERIOD = 10000;
    String[] permissions;

    private Button searchDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan);
        permissions = new String[]{Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        Log.d("OnCreate()", "Permissions check:");
        if (ActivityCompat.checkSelfPermission(DeviceScanActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d("OnCreate()", "Permissions missing BLUETOOTH_CONNECT");
            ActivityCompat.requestPermissions(this, permissions, 1);
        } else if (ActivityCompat.checkSelfPermission(DeviceScanActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.d("OnCreate()", "Permissions missing BLUETOOTH_SCAN");
            ActivityCompat.requestPermissions(this, permissions, 1);
        } else if (ActivityCompat.checkSelfPermission(DeviceScanActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("OnCreate()", "Permissions missing ACCESS_COARSE_LOCATION");
            ActivityCompat.requestPermissions(this, permissions, 1);
        } else if (ActivityCompat.checkSelfPermission(DeviceScanActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("OnCreate()", "Permissions missing ACCESS_FINE_LOCATION");
            ActivityCompat.requestPermissions(this, permissions, 1);
        } else {
            Log.d("Permissions", "Permissions granted.");
        }

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        searchDevices = (Button) findViewById(R.id.searchDevicesButton);

        searchDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!bluetoothAdapter.isEnabled()) {
                    Toast.makeText(DeviceScanActivity.this, "Bluetooth ist deaktiviert", Toast.LENGTH_SHORT).show();
                } else {
                    scanLeDevice();
                }
            }
        });

        recyclerView = findViewById(R.id.rv_bluetooth_devices);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);
        adapter = new LeDeviceListAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("Permissions", "Permissions: " + Arrays.toString(grantResults));
    }

    private boolean scanning = false;
    private Handler handler = new Handler();


    private void scanLeDevice() {
        if (!scanning) {
            // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    Log.d("DevicesScanActivity","Scan stopped.");
                    bluetoothLeScanner.stopScan(leScanCallback);
                }
            }, SCAN_PERIOD);

            scanning = true;
            Log.d("DevicesScanActivity","Starting scan ...");
            bluetoothLeScanner.startScan(leScanCallback);
        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    // Device scan callback.
    private ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    Log.d("DevicesScanActivity","Device " + result.getDevice().getName());
                    super.onScanResult(callbackType, result);
                    adapter.addDevice(result.getDevice());
                }
            };


    public void connectDevice(BluetoothDevice bluetoothDevice) {
    }
}