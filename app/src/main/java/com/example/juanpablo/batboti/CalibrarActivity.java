package com.example.juanpablo.batboti;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;


public class CalibrarActivity extends AppCompatActivity {

    private static BluetoothSocket mbluetoothSocket;

    TextView kp;
    TextView kd;
    TextView ki;
    TextView sp;

    private static SeekBar kpBar;
    SeekBar kdBar;
    SeekBar kiBar;
    SeekBar spBar;

    int MAX_KP = 100;
    int MIN_KP = 0;
    int MAX_KD = 2;
    int MIN_KD = 0;
    int MAX_KI = 200;
    int MIN_KI = 0;
    int MAX_SP = 360;
    int MIN_SP = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calibrate_menu);

        kpBarConfig();
        kdBarConfig();
        kiBarConfig();
        spBarConfig();
        mbluetoothSocket = myBluetoothSocket.getBSocket();

    }



    public void kpBarConfig(){
        kpBar = (SeekBar)findViewById(R.id.kp_bar);
        kp = (TextView)findViewById(R.id.kp_text);
        kp.setText(kpBar.getProgress()+"/100");

        kpBar.setOnSeekBarChangeListener(

                new SeekBar.OnSeekBarChangeListener() {
                    int progressValue;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        progressValue = progress;
                        kp.setText(progress+"/100");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        kp.setText(progressValue+"/100");
                        setKp();
                    }
                }
        );
    }

    public void kdBarConfig(){
        kdBar = (SeekBar)findViewById(R.id.kd_bar);
        kd = (TextView)findViewById(R.id.kd_text);
        kd.setText(kdBar.getProgress()+"/200");

        kdBar.setOnSeekBarChangeListener(

                new SeekBar.OnSeekBarChangeListener() {
                    int progressValue;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        progressValue = progress;
                        kd.setText(progress+"/200");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        kd.setText(progressValue+"/200");
                        setKd();
                    }
                }
        );
    }

    public void kiBarConfig(){
        kiBar = (SeekBar)findViewById(R.id.ki_bar);
        ki = (TextView)findViewById(R.id.ki_text);
        ki.setText(kiBar.getProgress()+"/200");

        kiBar.setOnSeekBarChangeListener(

                new SeekBar.OnSeekBarChangeListener() {
                    int progressValue;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        progressValue = progress;
                        ki.setText(progress+"/200");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        ki.setText(progressValue+"/200");
                        setKi();
                    }
                }
        );
    }

    public void spBarConfig(){
        spBar = (SeekBar)findViewById(R.id.sp_bar);
        sp = (TextView)findViewById(R.id.sp_text);
        sp.setText(spBar.getProgress()+"/360");

        spBar.setOnSeekBarChangeListener(

                new SeekBar.OnSeekBarChangeListener() {
                    int progressValue;
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        progressValue = progress;
                        sp.setText(progress+"/360");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        sp.setText(progressValue+"/360");
                        setSp();
                    }
                }
        );
    }

    private void setKp(){
        if(mbluetoothSocket!=null){
            try{
                byte[] aux1  = new byte[2];
                aux1[0] = (byte)5;
                aux1[1] = (byte)kpBar.getProgress();
                mbluetoothSocket.getOutputStream().write(aux1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setKd(){
        if(mbluetoothSocket!=null){
            try{
                byte[] aux1  = new byte[2];
                aux1[0] = (byte)6;
                aux1[1] = (byte)kdBar.getProgress();
                mbluetoothSocket.getOutputStream().write(aux1);
                mbluetoothSocket.getOutputStream().write(String.valueOf(kdBar.getProgress()).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setKi(){
        if(mbluetoothSocket!=null){
            try{
                byte[] aux1  = new byte[2];
                aux1[0] = (byte)7;
                aux1[1] = (byte)kiBar.getProgress();
                mbluetoothSocket.getOutputStream().write(aux1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setSp(){
        if(mbluetoothSocket!=null){
            try{
                byte[] aux1  = new byte[2];
                aux1[0] = (byte)8;
                aux1[1] = (byte)spBar.getProgress();
                mbluetoothSocket.getOutputStream().write(aux1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    @Override
    public void onResume(){
        super.onResume();
    }





}
