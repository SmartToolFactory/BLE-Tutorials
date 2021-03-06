package com.example.tutorial3_1gatt_connect;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public abstract class BluetoothScanActivity extends BluetoothLEActivity {


    // Stops scanning after 100 seconds.
    private static final long SCAN_PERIOD = 100_000;

    protected boolean mScanning = false;
    private Handler mHandler;

    private BluetoothLeScanner bluetoothLeScanner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        // Request for location permission
        if (!isLocationPermissionGranted()) {
            requestLocationPermission();
        }

        // !!! Important: Returns NULL if Bluetooth is NOT enabled
        bluetoothLeScanner = getBluetoothAdapter().getBluetoothLeScanner();

        Toast.makeText(this, "BluetoothScanActivity isBluetoothEnabled: " + isBluetoothEnabled() + ", bluetoothLeScanner: " + bluetoothLeScanner, Toast.LENGTH_SHORT).show();

    }


    /**
     * Advanced scanning method for Android 21+
     *
     * @param enable
     */
    public void scanBTDevice(boolean enable) {
        if (enable) {
            mScanning = true;
            startBTScan();

            stopBTDeviceScanAfterAPeriod();

        } else {
            mScanning = false;
            if (bluetoothLeScanner != null) {
                bluetoothLeScanner.stopScan(mScanCallback);
            }

        }

        invalidateOptionsMenu();

    }

    public void startBTScan() {

        List<ScanFilter> scanFilters = new ArrayList<>();

        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build();

        if (bluetoothLeScanner == null) {
            bluetoothLeScanner = getBluetoothAdapter().getBluetoothLeScanner();
        }

        bluetoothLeScanner.startScan(scanFilters, scanSettings, mScanCallback);

    }

    protected void stopBTDeviceScanAfterAPeriod() {
        // Stops scanning after a pre-defined scan period.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScanning = false;
                if (bluetoothLeScanner != null) {
                    bluetoothLeScanner.stopScan(mScanCallback);
                }
                invalidateOptionsMenu();
            }
        }, SCAN_PERIOD);
    }

    /**
     * ScanCallback for advanced BT LE scan. It requires Android 21+
     */
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            onBLEScanResult(callbackType, result);
            System.out.println("ScanCallback onScanResult() result: " + result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            System.out.println("ScanCallback onBLEBatchScanResults() results: " + results);
            onBLEBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            mScanning = false;
            System.out.println("ScanCallback onBLEScanFailed() errorCode: " + errorCode);
            onBLEScanFailed(errorCode);
        }
    };

    public abstract void onBLEScanResult(int callbackType, ScanResult result);

    public abstract void onBLEBatchScanResults(List<ScanResult> results);

    public abstract void onBLEScanFailed(int errorCode);

}
