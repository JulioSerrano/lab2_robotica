package com.example.juanpablo.batboti;

import android.bluetooth.BluetoothSocket;

public class myBluetoothSocket {

    private static BluetoothSocket mBluetoothSocket;

    public static void setBSocket(BluetoothSocket bs){
        myBluetoothSocket.mBluetoothSocket = bs;
    }

    public static BluetoothSocket getBSocket(){
        return myBluetoothSocket.mBluetoothSocket;
    }
}
