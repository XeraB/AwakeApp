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
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.math.BigInteger;
import java.util.Calendar;
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
    private TextView duration;
    private TextView volume;
    private NumberPicker hourPicker;
    private NumberPicker minPicker;

    final UUID TIMER_SERVICE = UUID.fromString("19B10010-E8F2-537E-4F6C-D104768A1214");
    final UUID TS_CHAR = UUID.fromString("5f954252-fd83-46e2-abaf-8fafcce5f6a3");
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
            hourPicker = findViewById(R.id.picker_hour);
            hourPicker.setMinValue(0);
            hourPicker.setMaxValue(23);
            hourPicker.setValue(6);
            hourPicker.setTextSize(90);
            hourPicker.setFormatter(i -> String.format("%02d", i));
            minPicker = findViewById(R.id.picker_minute);
            minPicker.setMinValue(0);
            minPicker.setMaxValue(59);
            minPicker.setTextSize(90);
            minPicker.setFormatter(i -> String.format("%02d", i));
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
        BluetoothGattCharacteristic durationCharacteristic = service.getCharacteristic(DURATION_CHAR);
        BluetoothGattCharacteristic volumeCharacteristic = service.getCharacteristic(VOLUME_CHAR);
        BluetoothGattCharacteristic tsCharacteristic = service.getCharacteristic(TS_CHAR);

        BigInteger bigIntHour = BigInteger.valueOf(hourPicker.getValue());
        BigInteger bigIntMin = BigInteger.valueOf(minPicker.getValue());
        BigInteger bigIntDuration = BigInteger.valueOf(Integer.parseInt(duration.getText().toString()));
        byte[] durationArray = bigIntDuration.toByteArray();
        BigInteger bigInt = BigInteger.valueOf(Integer.parseInt(volume.getText().toString()));
        byte[] volumeArray = bigInt.toByteArray();

        Calendar rightNow = Calendar.getInstance();
        Log.d("Form", "Actual Time: " + rightNow.getTime());
        Calendar alarm = (Calendar) rightNow.clone();
        alarm.set(Calendar.HOUR_OF_DAY, bigIntHour.intValue());
        alarm.set(Calendar.MINUTE, bigIntMin.intValue());
        Log.d("Form", "Clone Time: " + alarm.getTime());
        // Liegt der Alarm vor der Aktuellen Zeit, wird der Alarm für den nächsten Tag gesetzt.
        if (alarm.before(rightNow)) {
            // Add One Day
            alarm.add(Calendar.DAY_OF_MONTH, 1);
            Log.d("Adjust", "Alarm Time: " + alarm.getTime().toString());
        }
        Log.d("Form", "Alarm Time Millis: " + alarm.getTimeInMillis());
        long ts = alarm.getTimeInMillis() / 1000;
        Log.d("Form", "Alarm Time Seconds: " + ts);

        BigInteger bigIntTs = BigInteger.valueOf((int) ts);
        Log.d("Form", "Alarm Time Int: " + bigIntTs);
        byte[] tsArray = bigIntTs.toByteArray();

        byte[] back = new byte[4];
        back[0] = tsArray[3];
        back[1] = tsArray[2];
        back[2] = tsArray[1];
        back[3] = tsArray[0];

        durationCharacteristic.setValue(durationArray);
        volumeCharacteristic.setValue(volumeArray);
        tsCharacteristic.setValue(back);

        Boolean status = gatt.writeCharacteristic(tsCharacteristic);
        Log.d("Gatt", "Status ts " + String.valueOf(status));
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        status = gatt.writeCharacteristic(durationCharacteristic);
        Log.d("Gatt", "Status duration " + String.valueOf(status));
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        status = gatt.writeCharacteristic(volumeCharacteristic);
        Log.d("Gatt", "Status volume " + String.valueOf(status));
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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