package de.xera.applight;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;

@SuppressLint("MissingPermission")
public class DeviceScanActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LeDeviceListAdapter adapter;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private LocationManager locationManager;
    private static final long SCAN_PERIOD = 3000;
    String[] permissions;
    private Button searchDevices;
    SharedPreferences sharedPref;
    private boolean scanning = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private static final String LAST_BLE_DEVICE_ADDRESS = "lastBleDeviceAddress";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkForPermissions();

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        connectToLastBleDevice();

        setContentView(R.layout.activity_device_scan);
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        searchDevices = findViewById(R.id.searchDevicesButton);
        searchDevices.setOnClickListener(view -> {
            if (!bluetoothAdapter.isEnabled()) {
                Toast.makeText(DeviceScanActivity.this, "Bluetooth ist deaktiviert", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!locationManager.isLocationEnabled()) {
                Toast.makeText(DeviceScanActivity.this, "Standort ist deaktiviert", Toast.LENGTH_SHORT).show();
                return;
            }
            scanLeDevice();
        });

        recyclerView = findViewById(R.id.rv_bluetooth_devices);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llm);
        adapter = new LeDeviceListAdapter(this);
        recyclerView.setAdapter(adapter);
    }


    private void connectToLastBleDevice() throws NullPointerException{
        String lastDeviceAddress = sharedPref.getString(LAST_BLE_DEVICE_ADDRESS, "none");
        Log.d("LastBLEDevice", "Device: "+ lastDeviceAddress);
        if (!lastDeviceAddress.equals("none") && BluetoothAdapter.checkBluetoothAddress(lastDeviceAddress)) {
            try {
                BluetoothDevice lastBluetoothDevice = bluetoothAdapter.getRemoteDevice(lastDeviceAddress);
                Log.d("LastBLEDevice", ""+ lastBluetoothDevice.toString());
                if (lastBluetoothDevice != null) {
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    intent.putExtra("device", lastBluetoothDevice);
                    startActivity(intent);
                }
            } catch (Exception e) {
                Log.d("LastBLEDevice", e.toString());
            }
        }
    }

    private void scanLeDevice() {
        if (!scanning) {
            // Stops scanning after a predefined scan period.
            handler.postDelayed(() -> {
                scanning = false;
                Log.d("DevicesScanActivity", "Scan stopped.");
                bluetoothLeScanner.stopScan(leScanCallback);
            }, SCAN_PERIOD);

            scanning = true;
            Log.d("DevicesScanActivity", "Starting scan ...");
            bluetoothLeScanner.startScan(leScanCallback);
        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    // Device scan callback.
    private final ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    Log.d("DevicesScanActivity", "Device " + result.getDevice().getName());
                    super.onScanResult(callbackType, result);
                    if (result.getDevice().getName() != null) {
                        if (result.getDevice().getName().contains("AWAKE")) {
                            adapter.addDevice(result.getDevice());
                        }
                    }
                }
            };


    public void connectDevice(BluetoothDevice bluetoothDevice) {
        String address = bluetoothDevice.getAddress();
        if(address != null) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(LAST_BLE_DEVICE_ADDRESS, address);
            editor.apply();
        }
        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        intent.putExtra("device", bluetoothDevice);
        startActivity(intent);
    }

    private void checkForPermissions() {
        permissions = new String[]{Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        Log.d("OnCreate()", "Permissions check:");
        if (ContextCompat.checkSelfPermission(DeviceScanActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.d("OnCreate()", "Permissions missing BLUETOOTH_CONNECT");
            ActivityCompat.requestPermissions(this, permissions, 1);
        } else if (ContextCompat.checkSelfPermission(DeviceScanActivity.this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.d("OnCreate()", "Permissions missing BLUETOOTH_SCAN");
            ActivityCompat.requestPermissions(this, permissions, 1);
        } else if (ContextCompat.checkSelfPermission(DeviceScanActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("OnCreate()", "Permissions missing ACCESS_COARSE_LOCATION");
            ActivityCompat.requestPermissions(this, permissions, 1);
        } else if (ContextCompat.checkSelfPermission(DeviceScanActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("OnCreate()", "Permissions missing ACCESS_FINE_LOCATION");
            ActivityCompat.requestPermissions(this, permissions, 1);
        } else {
            Log.d("Permissions", "Permissions granted.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("Permissions", "Permissions: " + Arrays.toString(grantResults));
    }
}