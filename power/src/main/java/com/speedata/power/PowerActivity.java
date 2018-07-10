package com.speedata.power;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.IOException;

/**
 * @author :Reginer in  2017/9/12 10:03.
 * 联系方式:QQ:282921012
 * 功能描述:上下电
 */
public class PowerActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = PowerActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power);
        findViewById(R.id.btn_power_on).setOnClickListener(this);
        findViewById(R.id.btn_power_on_params).setOnClickListener(this);
        findViewById(R.id.btn_go).setOnClickListener(this);
        findViewById(R.id.btn_power_off).setOnClickListener(this);
        findViewById(R.id.btn_power_off_params).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_power_on) {
            powerOn();
        }
        if (v.getId() == R.id.btn_power_on_params) {
            powerOnParams();
        }
        if (v.getId() == R.id.btn_go) {
            go();
        }
        if (v.getId() == R.id.btn_power_off) {
            powerOff();
        }
        if (v.getId() == R.id.btn_power_off_params) {
            powerOffParams();
        }
    }

    /**
     * 指定参数下电
     */
    private void powerOffParams() {
        try {
            FingerGpio power = new FingerGpio("sys/class/misc/mtgpio/pin");
            power.powerOffDevice(63,128);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 指定参数上电
     */
    private void powerOnParams() {
        try {
            Log.d(TAG, "准备上电: " + System.currentTimeMillis());
            FingerGpio power = new FingerGpio("sys/class/misc/mtgpio/pin");
            power.powerOnDevice(63,128);
            Log.d(TAG, "上电完成: " + System.currentTimeMillis());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 下电.
     */
    private void powerOff() {
        try {
            PowerUtils.powerFingerOff();
            Log.d(TAG, "下电成功");
        } catch (IOException e) {
            Log.e(TAG, " 下电失败:" + Log.getStackTraceString(e));
        }
    }

    /**
     * 跳转到操作.
     */
    private void go() {
        try {
            startActivity(new Intent("com.spd.FINGERPRINT"));
        } catch (Exception e) {
            Log.e(TAG, "Activity未注册，确认存在<action android:name=\"com.spd.FINGERPRINT\"/>");
        }
    }

    /**
     * 上电.
     */
    private void powerOn() {
        try {
            PowerUtils.powerFingerOn();
            Log.d(TAG, " 上电成功");
        } catch (Exception e) {
            Log.e(TAG, " 上电失败:" + Log.getStackTraceString(e));
        }
    }
}
