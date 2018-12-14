package com.example.juanpablo.batboti;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    ProgressDialog mProgressDialog;
    private static final int REQUEST_ENABLE_BT = 1;

    BluetoothAdapter mBluetoothAdapter;
    Set<BluetoothDevice> pairedDevices;

    TextView bluetoothMessage;
    Button   btnManual;
    Button   btnAuto;
    Button   btnConnect;
    ListView bluetoothList;

    ArrayList mDeviceListBluetooth;

    ArrayAdapter<String> mArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bluetoothMessage = (TextView)findViewById(R.id.bluetoothStatus);
        btnManual = (Button)findViewById(R.id.btnLearn);
        btnAuto = (Button)findViewById(R.id.btnAuto);
        bluetoothList = (ListView)findViewById(R.id.bluetooths);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!mBluetoothAdapter.isEnabled()){
            setBluetoothState(1);
        }
        else if(mBluetoothAdapter == null)
            setBluetoothState(3);

    }


    public void connectBluetooth(View view) {

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == REQUEST_ENABLE_BT){
            setBluetoothState(2);
            if(mBluetoothAdapter.isEnabled()){
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage("Buscando...");
                mArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    // Loop through paired devices
                    for (BluetoothDevice device : pairedDevices)
                        // Add the name and address to an array adapter to show in a ListView
                        mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    //bluetoothList.setVisibility(View.VISIBLE);
                    //bluetoothList.setAdapter(mArrayAdapter);
                }
            }

            else
                setBluetoothState(1);
        }
    }

    private void setBluetoothState(int c){
        switch(c){

            case 1:
              bluetoothMessage.setText("Descontectado");
              bluetoothMessage.setTextColor(Color.RED);
              btnManual.setEnabled(false);
              btnAuto.setEnabled(false);
              break;

            case 2:
              bluetoothMessage.setText("Buscando...");
              bluetoothMessage.setTextColor(Color.GREEN);
              btnManual.setEnabled(true);
              btnAuto.setEnabled(true);
              break;

            case 3:
                bluetoothMessage.setText("Dispositivo no soportado");
                bluetoothMessage.setTextColor(Color.RED);
                btnManual.setEnabled(false);
                btnAuto.setEnabled(false);
                break;
        }
    }


}
