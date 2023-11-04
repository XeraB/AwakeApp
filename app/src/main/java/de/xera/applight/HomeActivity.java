package de.xera.applight;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.math.BigInteger;
import java.util.UUID;

@SuppressLint("MissingPermission")
public class HomeActivity extends AppCompatActivity {

    BluetoothGatt gatt;
    BluetoothGattService service;

    BluetoothDevice device;
    private Button setTimer;
    private Button startAlarm;
    private Button stopAlarm;
    private SwitchMaterial nightLight;
    private ChipGroup timerChipGroup;
    private Slider brightness;
    private Button sendForm;
    private TextView hour;
    private TextView minute;
    private TextView duration;
    private TextView volume;

    final UUID TIMER_SERVICE = UUID.fromString("19B10010-E8F2-537E-4F6C-D104768A1214");
    final UUID HOUR_CHAR = UUID.fromString("b7d06720-3cb7-40dc-94da-61b4af8a2759");
    final UUID MINUTE_CHAR = UUID.fromString("57dd48d8-ba50-4df4-ab6b-9e2349cac529");
    final UUID DURATION_CHAR = UUID.fromString("f246785d-5c35-4e77-be65-81d711fff24a");
    final UUID VOLUME_CHAR = UUID.fromString("eaefd17d-24cf-4021-afb7-06c7d9f221f9");
    final UUID ALARM_CHAR = UUID.fromString("33611222-e286-4835-b760-4adbcad8770b");
    final UUID NIGHT_CHAR = UUID.fromString("a805442b-63a8-4f7e-8f4e-59d0dcafba98");
    final UUID NIGHT_TIMER_CHAR = UUID.fromString("9dcdea3b-2a3c-4662-9eba-2e0bee9ffcf7");
    final UUID NIGHT_BRIGHT_CHAR = UUID.fromString("b34278fc-4756-45dc-b7d5-22a35412dea1");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        setTimer = findViewById(R.id.button_new_timer);
        startAlarm = findViewById(R.id.button_start_alarm);
        stopAlarm = findViewById(R.id.button_stop_alarm);
        nightLight = findViewById(R.id.switch_night_light);
        timerChipGroup = findViewById(R.id.chip_group_timer);
        brightness = findViewById(R.id.slider_brightness);

        device = (BluetoothDevice) getIntent().getExtras().get("device");
        gatt = device.connectGatt(this, false, bluetoothGattCallback);

        setTimer.setOnClickListener(view -> {
            setContentView(R.layout.form_timer);
            hour = findViewById(R.id.alarm_time_hour);
            minute = findViewById(R.id.alarm_time_min);
            duration = findViewById(R.id.alarm_duration);
            volume = findViewById(R.id.alarm_volume);
            sendForm = findViewById(R.id.button_send_form);
            sendForm.setOnClickListener(view1 -> {
                sendFormToService();
                setContentView(R.layout.activity_home);
            });
        });

        startAlarm.setOnClickListener(view -> sendAlarmValue(1));
        stopAlarm.setOnClickListener(view -> sendAlarmValue(0));
        nightLight.setOnCheckedChangeListener((buttonView, isChecked) -> {

            Log.d("ChipGroup", "Selected: " + timerChipGroup.getCheckedChipId());
            Chip chip = timerChipGroup.findViewById(timerChipGroup.getCheckedChipId());
            int time;
            switch (chip.getText().toString()) {
                case "10 min":
                    time = 10;
                    break;
                case "20 min":
                    time = 20;
                    break;
                case "30 min":
                    time = 30;
                    break;
                case "60 min":
                    time = 60;
                    break;
                default:
                    time = 1;
            }
            if (isChecked) {
                sendNightLightValue(1, time);
            } else {
                sendNightLightValue(0, 0);
            }
        });
        brightness.addOnChangeListener(((slider, value, fromUser) -> {
            Log.d("Slider", "Selected: " + value);
            sendNightLightBrightness((int) value);
        }));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gatt.disconnect();
        gatt.close();
    }

    private void sendFormToService() {
        Log.d("Form", "Sending form ...");
        BluetoothGattCharacteristic hourCharacteristic = service.getCharacteristic(HOUR_CHAR);
        BluetoothGattCharacteristic minuteCharacteristic = service.getCharacteristic(MINUTE_CHAR);
        BluetoothGattCharacteristic durationCharacteristic = service.getCharacteristic(DURATION_CHAR);
        BluetoothGattCharacteristic volumeCharacteristic = service.getCharacteristic(VOLUME_CHAR);

        BigInteger bigIntHour = BigInteger.valueOf(Integer.parseInt(hour.getText().toString()));
        byte[] hourArray = bigIntHour.toByteArray();
        BigInteger bigIntMin = BigInteger.valueOf(Integer.parseInt(minute.getText().toString()));
        byte[] minArray = bigIntMin.toByteArray();
        BigInteger bigIntDuration = BigInteger.valueOf(Integer.parseInt(duration.getText().toString()));
        byte[] durationArray = bigIntDuration.toByteArray();
        BigInteger bigInt = BigInteger.valueOf(Integer.parseInt(volume.getText().toString()));
        byte[] volumeArray = bigInt.toByteArray();

        hourCharacteristic.setValue(hourArray);
        minuteCharacteristic.setValue(minArray);
        durationCharacteristic.setValue(durationArray);
        volumeCharacteristic.setValue(volumeArray);

        Boolean status = gatt.writeCharacteristic(hourCharacteristic);
        Log.d("Gatt", String.valueOf(status));
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        status = gatt.writeCharacteristic(minuteCharacteristic);
        Log.d("Gatt", String.valueOf(status));
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        status = gatt.writeCharacteristic(durationCharacteristic);
        Log.d("Gatt", String.valueOf(status));
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        status = gatt.writeCharacteristic(volumeCharacteristic);
        Log.d("Gatt", String.valueOf(status));
        Log.d("Form", "Form sent");
    }

    private void sendAlarmValue(int value) {
        BluetoothGattCharacteristic alarmCharacteristic = service.getCharacteristic(ALARM_CHAR);

        BigInteger bigInt = BigInteger.valueOf(value);
        byte[] alarmArray = bigInt.toByteArray();

        alarmCharacteristic.setValue(alarmArray);
        Boolean status = gatt.writeCharacteristic(alarmCharacteristic);
        Log.d("Gatt", String.valueOf(status));
    }

    private void sendNightLightValue(int value, int time) {
        BluetoothGattCharacteristic nightCharacteristic = service.getCharacteristic(NIGHT_CHAR);
        BluetoothGattCharacteristic nightTimerCharacteristic = service.getCharacteristic(NIGHT_TIMER_CHAR);

        BigInteger bigInt = BigInteger.valueOf(value);
        byte[] valueArray = bigInt.toByteArray();
        BigInteger bigInt2 = BigInteger.valueOf(time);
        byte[] timeArray = bigInt2.toByteArray();

        nightCharacteristic.setValue(valueArray);
        nightTimerCharacteristic.setValue(timeArray);
        Boolean status = gatt.writeCharacteristic(nightTimerCharacteristic);
        Log.d("Gatt", String.valueOf(status));
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        status = gatt.writeCharacteristic(nightCharacteristic);
        Log.d("Gatt", String.valueOf(status));
    }

    private void sendNightLightBrightness(int value) {
        BluetoothGattCharacteristic nightBrightCharacteristic = service.getCharacteristic(NIGHT_BRIGHT_CHAR);

        BigInteger bigInt = BigInteger.valueOf(value);
        byte[] valueArray = bigInt.toByteArray();

        nightBrightCharacteristic.setValue(valueArray);
        Boolean status = gatt.writeCharacteristic(nightBrightCharacteristic);
        Log.d("Gatt", String.valueOf(status));
    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("ConnectionState", "Connected");
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("ConnectionState", "Disconnected");
                finish();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            service = gatt.getService(TIMER_SERVICE);
            if (service != null) {
                Log.d("BLEService", "Timer service found.");
                Log.d("Characteristics", service.getCharacteristics().toString());
            } else {
                Log.e("BLEService", "Could not find Timer service");
            }
        }
    };
}