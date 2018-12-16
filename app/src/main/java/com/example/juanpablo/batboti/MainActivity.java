package com.example.juanpablo.batboti;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    ProgressDialog mProgressDialog;
    private static final int REQUEST_ENABLE_BT = 1;

    BluetoothAdapter mBluetoothAdapter;
    Set<BluetoothDevice> pairedDevices;
    BluetoothSocket mbluetoothSocket;

    TextView bluetoothMessage;
    Button   btnManual;
    Button   btnAuto;
    Button   btnConnect;
    ListView bluetoothList;
    NavigationView nav;
    ArrayList mDeviceListBluetooth;

    ArrayAdapter<String> mArrayAdapter;

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String address = null;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bluetoothMessage = (TextView)findViewById(R.id.bluetoothStatus);
        btnManual = (Button)findViewById(R.id.btnLearn);
        btnAuto = (Button)findViewById(R.id.btnAuto);
        bluetoothList = (ListView)findViewById(R.id.bluetooths);
        nav = (NavigationView)findViewById(R.id.nav_view);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!mBluetoothAdapter.isEnabled()){
            setBluetoothState(1);
        }
        else if(mBluetoothAdapter == null)
            setBluetoothState(3);

        nav.setNavigationItemSelectedListener((NavigationView.OnNavigationItemSelectedListener) this);

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
                    bluetoothList.setVisibility(View.VISIBLE);
                    bluetoothList.setAdapter(mArrayAdapter);
                    bluetoothList.setOnItemClickListener(myTouchListener);
                }
            }

            else
                setBluetoothState(1);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item){
        int id = item.getItemId();
        switch(id){
            case R.id.calibrar:
                calibrarView(null);
                break;
        }
        return false;
    }

    public void calibrarView(View view){
        CalibrarActivity ca = new CalibrarActivity(mbluetoothSocket);
        Intent i = new Intent(this,ca.getClass());
        startActivity(i);
    }

    private AdapterView.OnItemClickListener myTouchListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String info = ((TextView) view).getText().toString();
            address = info.substring(info.length() - 17);
            try {
                doConnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };

    private void setBluetoothState(int c){
        switch(c){

            case 1:
              bluetoothMessage.setText("Descontectado");
              bluetoothMessage.setTextColor(Color.RED);
              btnManual.setEnabled(false);
              btnAuto.setEnabled(false);
              break;

            case 2:
              bluetoothMessage.setText("Conectado");
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

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException{
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    public void doConnect() throws IOException {

        try{
            BluetoothDevice dispositivo = mBluetoothAdapter.getRemoteDevice(address);//connects to the device's address and checks if it's available
            mbluetoothSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(BTMODULEUUID);//create a RFCOMM (SPP) connection
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            mbluetoothSocket.connect();//start connection
            bluetoothList.setVisibility(View.INVISIBLE);
            setBluetoothState(2);
        }
        catch(Exception e){
            Toast.makeText(this, "No se pudo realizar la conexión", Toast.LENGTH_SHORT).show();
        }

    }

    public void foward(View view){
        if(mbluetoothSocket!=null){
            try{
                mbluetoothSocket.getOutputStream().write(String.valueOf(1).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void toRight(View view){
        if(mbluetoothSocket!=null){
            try{
                mbluetoothSocket.getOutputStream().write(String.valueOf(2).getBytes());
            } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }

    public void toLeft(View view){
        if(mbluetoothSocket!=null){
            try{
                mbluetoothSocket.getOutputStream().write(String.valueOf(3).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void down(View view){
        if(mbluetoothSocket!=null){
            try{
                mbluetoothSocket.getOutputStream().write(String.valueOf(4).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
