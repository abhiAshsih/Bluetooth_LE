package com.example.bluetooth_le;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertisingSet;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private Button mAdvertiseButton, stopAdvertiseButton;
    private static final String TAG = "BLEApp";
    private BluetoothAdapter mBluetoothAdapter;
    public static final int REQUEST_ENABLE_BT = 1;
    private int STORAGE_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAdvertiseButton = (Button) findViewById(R.id.advertise_btn);
        stopAdvertiseButton = (Button) findViewById(R.id.advertise_stop_btn);

        mAdvertiseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (savedInstanceState == null)
                    mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE))
                            .getAdapter();
                // Is Bluetooth supported on this device?
                if (mBluetoothAdapter != null) {

                    // Is Bluetooth turned on?
                    if (mBluetoothAdapter.isEnabled()) {

                        // Are Bluetooth Advertisements supported on this device?
                        if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {

                            // Everything is supported and enabled, load the method
                            advertise();

                        } else {

                            // Bluetooth Advertisements are not supported.
                            Log.e(TAG, "R.string.bt_ads_not_supported");
                        }
                    } else {
                        Log.e(TAG, "Prompt user to turn on Bluetooth");
                        // Prompt user to turn on Bluetooth (logic continues in onActivityResult()).
                        //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                } else {

                    // Bluetooth is not supported.
                    Log.e(TAG, "R.string.bt_not_supported");
                }
            }
        });
        stopAdvertiseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                   stopAdv();
            }
        });


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void stopAdv() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AdvertisingSetCallback callback = new AdvertisingSetCallback() {
                @Override
                public void onAdvertisingSetStarted(AdvertisingSet advertisingSet, int txPower, int status) {
                    Log.i(TAG, "onAdvertisingSetStarted(): txPower:" + txPower + " , status: "
                            + status);
                    //currentAdvertisingSet = advertisingSet;
                }

                @Override
                public void onAdvertisingDataSet(AdvertisingSet advertisingSet, int status) {
                    Log.i(TAG, "onAdvertisingDataSet() :status:" + status);
                }

                @Override
                public void onScanResponseDataSet(AdvertisingSet advertisingSet, int status) {
                    Log.i(TAG, "onScanResponseDataSet(): status:" + status);
                }

                @Override
                public void onAdvertisingSetStopped(AdvertisingSet advertisingSet) {
                    Log.i(TAG, "onAdvertisingSetStopped():");
                }
            };
            BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            advertiser.stopAdvertisingSet(callback);
        }
    }


    private void advertise() {

        BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();
        AdvertisingSetParameters parameters = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            parameters = (new AdvertisingSetParameters.Builder())
                    .setLegacyMode(true) // True by default, but set here as a reminder.
                    .setConnectable(false)
                    .setInterval(AdvertisingSetParameters.INTERVAL_HIGH)
                    .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_MEDIUM)
                    .build();
        }


        String testData = "ADG";
        byte[] testData1 = testData.getBytes();

        String ssid = null;

        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo;

        wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
            ssid = wifiInfo.getSSID();
            Log.i(TAG,ssid);
        }


        //byte[] testData2=ssid.getBytes();
        ParcelUuid pUuid = new ParcelUuid(UUID.fromString("FDA0B2D9-2E47-48B3-BCFC-AFFD91E932CA"));

        AdvertiseData data = (new AdvertiseData.Builder())
                .addManufacturerData(1, ssid.getBytes(Charset.forName("UTF-8")))
                //.addServiceData( pUuid, ssid.getBytes(Charset.forName("UTF-8") ) )
                .setIncludeDeviceName(true)
                .build();
        //.addServiceData( pUuid, "Data".getBytes(Charset.forName("UTF-8") ) )

        //AdvertisingSetCallback callback = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AdvertisingSetCallback callback = new AdvertisingSetCallback() {
                @Override
                public void onAdvertisingSetStarted(AdvertisingSet advertisingSet, int txPower, int status) {
                    Log.i(TAG, "onAdvertisingSetStarted(): txPower:" + txPower + " , status: "
                            + status);
                    //currentAdvertisingSet = advertisingSet;
                }

                @Override
                public void onAdvertisingDataSet(AdvertisingSet advertisingSet, int status) {
                    Log.i(TAG, "onAdvertisingDataSet() :status:" + status);
                }

                @Override
                public void onScanResponseDataSet(AdvertisingSet advertisingSet, int status) {
                    Log.i(TAG, "onScanResponseDataSet(): status:" + status);
                }

                @Override
                public void onAdvertisingSetStopped(AdvertisingSet advertisingSet) {
                    Log.i(TAG, "onAdvertisingSetStopped():");
                }
            };

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_ADVERTISE,Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,Manifest.permission.BLUETOOTH_ADMIN}, STORAGE_PERMISSION_CODE);

            }else{
                boolean isNameChanged = BluetoothAdapter.getDefaultAdapter().setName("Byjus");
                advertiser.startAdvertisingSet(parameters, data, null, null, null, callback);
                Toast.makeText(MainActivity.this, "Data : " + data, Toast.LENGTH_LONG).show();
            }

            // When done with the advertising:
//            try
//            {
//                Thread.sleep(9000);
//            }
//            catch(InterruptedException ex)
//            {
//                Thread.currentThread().interrupt();
//            }

            //advertiser.stopAdvertisingSet(callback);

        }


    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0){
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "BLUETOOTH_ADVERTISE Permission GRANTED", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "BLUETOOTH_ADVERTISE Permission DENIED", Toast.LENGTH_SHORT).show();
                }
                if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "BLUETOOTH_CONNECT Permission GRANTED", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "BLUETOOTH_CONNECT Permission DENIED", Toast.LENGTH_SHORT).show();
                }
                if (grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "ACCESS_COARSE_LOCATION Permission GRANTED", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "ACCESS_COARSE_LOCATION Permission DENIED", Toast.LENGTH_SHORT).show();
                }
                if (grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "ACCESS_FINE_LOCATION Permission GRANTED", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "ACCESS_FINE_LOCATION Permission DENIED", Toast.LENGTH_SHORT).show();
                }
//                if (grantResults[4] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(this, "ACCESS_FINE_LOCATION Permission GRANTED", Toast.LENGTH_SHORT).show();
//                }
//                if (grantResults[5] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(this, "ACCESS_FINE_LOCATION Permission GRANTED", Toast.LENGTH_SHORT).show();
//                }
            }

        }
    }
}