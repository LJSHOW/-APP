package com.example.test.ui.DevicesControlActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.test.R;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;




public class DeviceAirActivity extends BaseDevicesControlActivity implements View.OnClickListener {


    private TextView mMTVCO2;
    private TextView mMTVCO;
    private TextView mMTVPM;
    private TextView mMTVTemper;
    private TextView mMTVHumidity;
    private Switch mSwitchfan;
    private Toast mCO2_AlarmDialog;
    private Toast mTemp_AlarmDialog;
    private Toast mHum_AlarmDialog;
    private Toast mCO_AlarmDialog;
    private Toast mPM_AlarmDialog;
    //临时的全局变量
    private float tempMQ7 =0 ;
    private int temptemperature = 0 ;
    private int tempHumidity =0;
    private int tempCO2_value = 0 ;
    private int tempPM2_5_value=0;
    private boolean temp_alarm= false;
    private boolean hum_alarm= false;
    private boolean CO_alarm= false;
    private boolean CO2_alarm= false;
    private boolean PM_alarm= false;
    private boolean tempSwitch = false;



    private static final Object KEY_TEMPERATURE = "MQ7";

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            if (msg.what==108){
                uidataUI();
            }
        }
    };




    private void uidataUI() {
        mMTVCO.setText(tempMQ7 + " ppm");
        mMTVTemper.setText(temptemperature + "°");
        mMTVHumidity.setText(tempHumidity + "%");
        mMTVCO2.setText(tempCO2_value + "ppm");
        mMTVPM.setText(tempPM2_5_value + "ppm");
        mSwitchfan.setChecked(tempSwitch);
    if (CO2_alarm == true) {

            Toast mCO2_AlarmDialog= Toast.makeText(this,"二氧化碳数据异常",Toast.LENGTH_LONG);
            mCO2_AlarmDialog.setGravity(Gravity.CENTER_HORIZONTAL,0,0);
            mCO2_AlarmDialog.show();
        }
        if (CO_alarm == true) {

            Toast mCO_AlarmDialog= Toast.makeText(this,"一氧化碳数据异常",Toast.LENGTH_LONG);
            mCO_AlarmDialog.setGravity(Gravity.CENTER_HORIZONTAL,0,0);
            mCO_AlarmDialog.show();
        }

        if (PM_alarm == true) {

            Toast mPM_AlarmDialog= Toast.makeText(this,"PM2.5数据异常",Toast.LENGTH_LONG);
            mPM_AlarmDialog.setGravity(Gravity.CENTER_HORIZONTAL,0,0);
            mPM_AlarmDialog.show();
        }
        if (temp_alarm == true) {

            Toast mTemp_AlarmDialog= Toast.makeText(this,"温度数据异常",Toast.LENGTH_LONG);
            mTemp_AlarmDialog.setGravity(Gravity.CENTER_HORIZONTAL,0,0);
            mTemp_AlarmDialog.show();
        }
        if (hum_alarm == true) {

            Toast mHum_AlarmDialog= Toast.makeText(this,"湿度数据异常",Toast.LENGTH_LONG);
            mHum_AlarmDialog.setGravity(Gravity.CENTER_HORIZONTAL,0,0);
            mHum_AlarmDialog.show();
        }else return;

}



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_air);
        initView();

    }


    private void bindViews() {


        mMTVCO2 = (TextView) findViewById(R.id.mTVCO2);
        mMTVCO = (TextView) findViewById(R.id.mTVCO);
        mMTVPM = (TextView) findViewById(R.id.mTVPM);
        mMTVTemper = (TextView) findViewById(R.id.mTVTemper);
        mMTVHumidity = (TextView) findViewById(R.id.mTVHumidity);
        mSwitchfan = (Switch) findViewById(R.id.Switchfan);

        //ui触碰事件
        mSwitchfan.setOnClickListener(this);



    }


    private void initView() {
        mTopBar =findViewById(R.id.topBar);
        //同步这个设备的名字
        String tempTittle = mDevice.getAlias().isEmpty()?mDevice.getProductName():mDevice.getAlias();
        mTopBar.setTitle(tempTittle);
        mTopBar.addLeftImageButton(R.mipmap.ic_back,R.id.topbar_right_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        bindViews();

    }

    @Override
    protected void receiveCloudData(GizWifiErrorCode result, ConcurrentHashMap<String, Object> dataMap) {
        super.receiveCloudData(result, dataMap);
        Log.e("LJ","DeviceAirActivity控制界面的下发数据："+dataMap);

        if (result==GizWifiErrorCode.GIZ_SDK_SUCCESS){
        //如果下发数据不为空
            if (dataMap!=null){

                parseReceiveData(dataMap);

            }

        }

    }

    private void parseReceiveData(ConcurrentHashMap<String, Object> dataMap) {
        if (dataMap.get("alerts")!=null){
            ConcurrentHashMap<String,Object> temperAlertsMap = (ConcurrentHashMap<String, Object>) dataMap.get("alerts");
            for (String alertsKey : temperAlertsMap.keySet() ){
                if (alertsKey.equals("MG811_alarm")){
                    CO2_alarm = (boolean) temperAlertsMap.get("MG811_alarm");
                }
                if (alertsKey.equals("temperature_alarm")){
                    temp_alarm = (boolean) temperAlertsMap.get("temperature_alarm");
                }
                if (alertsKey.equals("MQ_7_alarm")){
                    CO_alarm = (boolean) temperAlertsMap.get("MQ_7_alarm");
                }
                if (alertsKey.equals("humidity_alarm")){
                    hum_alarm = (boolean) temperAlertsMap.get("humidity_alarm");
                }
                if (alertsKey.equals("GP2Y1014AU_ALARM")){
                    PM_alarm = (boolean) temperAlertsMap.get("GP2Y1014AU_ALARM");
                }
                Log.e("LJ","CO2:"+CO2_alarm);
            }

        }


        if (dataMap.get("data") != null) {
            ConcurrentHashMap<String, Object> temperDataMap = (ConcurrentHashMap<String, Object>) dataMap.get("data");
            for (String dataKey : temperDataMap.keySet()) {

                //主要是通过我们在云端定义的标志点
                //温度
                if (dataKey.equals("MQ_7")) {
                    tempMQ7 = (int) temperDataMap.get("MQ_7");
                }
                if (dataKey.equals( "temperature")) {
                    temptemperature = (int) temperDataMap.get("temperature");
                }
                if (dataKey.equals( "humidity")) {
                    tempHumidity = (int) temperDataMap.get("humidity");
                }
                if (dataKey.equals( "MG811")) {
                    tempCO2_value = (int) temperDataMap.get("MG811");
                }
                if (dataKey.equals( "GP2Y1014AU")) {
                    tempPM2_5_value = (int) temperDataMap.get("GP2Y1014AU");
                }


            }
            mHandler.sendEmptyMessage(108);
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.Switchfan) {
        sendCommand("fan",mSwitchfan.isChecked());
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
