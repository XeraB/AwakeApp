package de.xera.applight;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.UUID;

import de.xera.applight.databinding.ActivityHomeBinding;

@SuppressLint("MissingPermission")
public class HomeActivity extends AppCompatActivity {

    BluetoothGatt gatt;
    BluetoothGattService service;

    BluetoothDevice device;
    private Button setTimer;
    private Button sendForm;
    private TextView time;
    private TextView duration;
    private TextView volume;

    final UUID TIMER_SERVICE = UUID.fromString("19B10010-E8F2-537E-4F6C-D104768A1214");
    final UUID TIME_CHAR = UUID.fromString("b7d06720-3cb7-40dc-94da-61b4af8a2759");
    final UUID DURATION_CHAR = UUID.fromString("f246785d-5c35-4e77-be65-81d711fff24a");
    final UUID VOLUME_CHAR = UUID.fromString("eaefd17d-24cf-4021-afb7-06c7d9f221f9");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setTimer = findViewById(R.id.button_new_timer);

        device = (BluetoothDevice) getIntent().getExtras().get("device");
        gatt = device.connectGatt(this, false, bluetoothGattCallback);

        setTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.form_timer);
                time = findViewById(R.id.alarm_time);
                duration = findViewById(R.id.alarm_duration);
                volume = findViewById(R.id.alarm_volume);
                sendForm = findViewById(R.id.button_send_form);
                sendForm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendFormToService();

                        setContentView(R.layout.activity_home);
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gatt.disconnect();
        gatt.close();
    }

    private void sendFormToService() {
        Log.d("Form", "Sending form ...");
        BluetoothGattCharacteristic timeCharacteristic = service.getCharacteristic(TIME_CHAR);
        BluetoothGattCharacteristic durationCharacteristic = service.getCharacteristic(DURATION_CHAR);
        BluetoothGattCharacteristic volumeCharacteristic = service.getCharacteristic(VOLUME_CHAR);

        BigInteger bigIntTime = BigInteger.valueOf(Integer.parseInt(time.getText().toString()));
        byte[] timeArray = bigIntTime.toByteArray();
        BigInteger bigIntDuration = BigInteger.valueOf(Integer.parseInt(duration.getText().toString()));
        byte[] durationArray = bigIntDuration.toByteArray();
        BigInteger bigInt = BigInteger.valueOf(Integer.parseInt(volume.getText().toString()));
        byte[] volumeArray = bigInt.toByteArray();

        Log.d("Form", "Time: " + bigIntTime);
        Log.d("Form", "Duration: " + bigIntDuration);
        Log.d("Form", "Volume: " + Arrays.toString(volumeArray));

        timeCharacteristic.setValue(timeArray);
        durationCharacteristic.setValue(durationArray);
        volumeCharacteristic.setValue(volumeArray);

        Log.d("Form", "Time: " + Arrays.toString(timeCharacteristic.getValue()));
        Log.d("Form", "Duration: " + Arrays.toString(durationCharacteristic.getValue()));
        Log.d("Form", "Volume: " + Arrays.toString(volumeCharacteristic.getValue()));

        Boolean status = gatt.writeCharacteristic(timeCharacteristic);
        Log.d("Gatt", String.valueOf(status));
        try{
            Thread.sleep(100);
        } catch(InterruptedException e){
            e.printStackTrace();
        }
        status = gatt.writeCharacteristic(durationCharacteristic);
        Log.d("Gatt", String.valueOf(status));
        try{
            Thread.sleep(100);
        } catch(InterruptedException e){
            e.printStackTrace();
        }
        status = gatt.writeCharacteristic(volumeCharacteristic);
        Log.d("Gatt", String.valueOf(status));
        Log.d("Form", "Form sent");
    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("ConnectionState", "Connected");
                //Toast.makeText(HomeActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("ConnectionState", "Disconnected");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            service = gatt.getService(TIMER_SERVICE);
            if ( service != null) {
                Log.d("BLEService", "Timer service found.");
                Log.d("Characteristics", service.getCharacteristics().toString());

                //Toast.makeText(HomeActivity.this, "Connected", Toast.LENGTH_SHORT).show();

            } else {
                Log.e("BLEService", "Could not find Timer service");
            }
        }
    };
}