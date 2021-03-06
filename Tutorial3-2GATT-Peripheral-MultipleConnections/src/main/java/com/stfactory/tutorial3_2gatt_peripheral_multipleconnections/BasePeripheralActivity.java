package com.stfactory.tutorial3_2gatt_peripheral_multipleconnections;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.stfactory.tutorial3_2gatt_peripheral_multipleconnections.constant.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BasePeripheralActivity extends BluetoothLEActivity {

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    protected BluetoothGattServer mBluetoothGattServer;

    private boolean mAdvertising = false;
    private boolean mConnected = false;

    protected Map<Integer, BluetoothDevice> connectedDevices = new HashMap<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBluetoothManager = getBluetoothManager();
        mBluetoothAdapter = getBluetoothAdapter();
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();

    }

    public void startGATTServer(BluetoothGattServerCallback callback) {
        mBluetoothGattServer = mBluetoothManager.openGattServer(this, callback);
    }


    public void startGATTServer(BluetoothGattServerCallback callback, List<BluetoothGattService> serviceList) {

        mBluetoothGattServer = mBluetoothManager.openGattServer(this, callback);

        if (serviceList != null && serviceList.size() > 0) {

            for (BluetoothGattService service : serviceList) {
                if (!mBluetoothGattServer.getServices().contains(service)) {
                    mBluetoothGattServer.addService(service);
                }
            }
        }
    }

    public void addGATTService(BluetoothGattService bluetoothGattService) {
        boolean isServiceAdded = mBluetoothGattServer.addService(bluetoothGattService);

        Toast.makeText(this, "BasePeripheralActivity addGATTService() added: " + isServiceAdded
                + "service: " + bluetoothGattService, Toast.LENGTH_SHORT).show();
    }


    public void closeServer() {
        if (mBluetoothGattServer != null) {
            mBluetoothGattServer.close();
        }
    }


    /**
     * Start advertising data with specified settings. There are 3 components are required for simple advertising.
     *
     * <li>
     * <b>AdvertiseSettings</b> to set features of advertising such as timout, power level and connectability
     * </li>
     *
     * <li>
     * <b>AdvertiseData</b> to be send to clients
     * </li>
     *
     * <li>
     * <b>AdvertiseCallback </b> to get events on {@link AdvertiseCallback#onStartFailure(int)}
     * or {@link AdvertiseCallback#onStartSuccess(AdvertiseSettings)} events
     * </li>
     */
    public void startAdvertising() {

        if (mBluetoothLeAdvertiser == null) {
            return;
        }

        // Advertising Settings
        AdvertiseSettings advertiseSettings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build();

        ParcelUuid pUuid = new ParcelUuid(Constants.UUID_ADVERTISE_SERVICE);
        ParcelUuid pUuidRx = new ParcelUuid(Constants.UUID_ADVERTISE_RESPONSE_DATA);

        // Data to be advertised
        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(true)
                .addServiceUuid(pUuid)
                .build();

        // TODO Test for sending Advertising data

        final byte[] toSendData = new byte[7];

        toSendData[0] = 6;
        toSendData[1] = 2;
        toSendData[2] = 2;
        toSendData[3] = 2;
        toSendData[4] = 2;
        toSendData[5] = 2;
        toSendData[6] = 6;


        AdvertiseData datatoSend = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .addServiceUuid(pUuid)
                .setIncludeTxPowerLevel(false)
                .addServiceData(pUuidRx, toSendData)
                .build();


        // Start advertising
        mBluetoothLeAdvertiser.startAdvertising(advertiseSettings, advertiseData, datatoSend, mAdvertiseCallback);

    }

    public void startAdvertising(AdvertiseSettings advertiseSettings, AdvertiseData advertiseData) {

        mBluetoothLeAdvertiser.startAdvertising(advertiseSettings, advertiseData, mAdvertiseCallback);

    }

    public void stopAdvertising() {
        setAdvertising(false);

        if (mBluetoothLeAdvertiser != null) {
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
            mAdvertising = false;
        }
    }

    public BluetoothGattServer getBluetoothGattServer() {
        return mBluetoothGattServer;
    }

    public boolean isAdvertising() {
        return mAdvertising;
    }

    public void setAdvertising(boolean advertising) {
        mAdvertising = advertising;
    }

    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            setAdvertising(true);
            onAdvertisingStartSuccess(settingsInEffect);

        }

        @Override
        public void onStartFailure(int errorCode) {
            setAdvertising(false);
            onAdvertisingStartFailure(errorCode);
        }
    };


    protected void setConnected(boolean connected) {
        mConnected = connected;
    }

    protected boolean isConnected() {
        return mConnected;
    }


    protected abstract void onAdvertisingStartSuccess(AdvertiseSettings settingsInEffect);

    protected abstract void onAdvertisingStartFailure(int errorCode);

}
