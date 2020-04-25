package com.example.test.ui.DevicesControlActivity;

//获取数据
//当我们设备界面成功跳转后，要立刻同步设备状态信息，我们一打开控制界面就立刻弹窗显示“正在同步状态”
//当我们的控制界面打开，立刻要同步云端状态信息。{弹窗显示—>如果未能同步下来的话，就是无法控制的!退出控制界面。
/////////////////////////////////////////////////////////如果能过同步状态的话，那么我们就弹窗消失


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.test.MainActivity;
import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.gizwifisdk.listener.GizWifiDeviceListener;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseDevicesControlActivity extends AppCompatActivity {
    private QMUITipDialog mTipDialog;
    protected GizWifiDevice mDevice;
    protected QMUITopBar mTopBar;
    private NetWorkChangedReciever netWorkChangedReciever;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDevices();
        initNetBroadReciever();

    }

    private void initNetBroadReciever() {
        netWorkChangedReciever = new NetWorkChangedReciever();
        //此处表示拦截我们的安卓系统的网络状态改变
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkChangedReciever,intentFilter);


    }

    protected void sendCommand(String key, Object value) {

        if (value == null)
            return;

        ConcurrentHashMap<String, Object> dataMap = new ConcurrentHashMap<>();
        dataMap.put(key, value);
        mDevice.write(dataMap, 5);


    }
    protected void initDevices(){
        //我们拿到上个界面传来的一个设备对象
        mDevice=this.getIntent().getParcelableExtra("_device");
        //设置设备的云端回调结果监听
        mDevice.setListener(mListener);

        mTipDialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("同步状态中").
                        create();


        mTipDialog.show();
        //主动获取最新状态
        getstatus();

    }

    private void getstatus() {
        //如果当前的设备可控制，那么我们就获取最新状态
        if (mDevice.getNetStatus()==GizWifiDeviceNetStatus.GizDeviceControlled){
            mDevice.getNetStatus();
            mTipDialog.dismiss();

        }
    }

    protected void receiveCloudData(GizWifiErrorCode result,ConcurrentHashMap<String, Object> dataMap){
        if (result ==GizWifiErrorCode.GIZ_SDK_SUCCESS){
            mTipDialog.dismiss();
        }

    }
    private void updataNetStatus(GizWifiDevice device, GizWifiDeviceNetStatus netStatus){

        if (netStatus==GizWifiDeviceNetStatus.GizDeviceOffline) {
            if (mTipDialog.isShowing()) {
                Toast.makeText(this, "设备无法同步", Toast.LENGTH_SHORT);
                mTipDialog.dismiss();
                finish();//退出界面
            }
        }
    }



    private GizWifiDeviceListener mListener = new GizWifiDeviceListener(){

        //设备状态回调


        @Override
        public void didReceiveData(GizWifiErrorCode result, GizWifiDevice device, ConcurrentHashMap<String, Object> dataMap, int sn) {
            super.didReceiveData(result, device, dataMap, sn);
            receiveCloudData(result,dataMap);
            Log.e("LJ","控制界面的下发数据："+dataMap);

        }
        //设备状态回调，包括离线、在线回调
        //该回调主动上报设备的网络状态变化，当设备重上电、断电或可控时会触发该回调


        @Override
        public void didUpdateNetStatus(GizWifiDevice device, GizWifiDeviceNetStatus netStatus) {
            super.didUpdateNetStatus(device, netStatus);
            Log.e("LJ","控制界面的设备状态回调："+netStatus);
            updataNetStatus(device, netStatus);
        }
    };


    //内部类，获取手机网络状态发生改变的广播截取
    private class NetWorkChangedReciever extends BroadcastReceiver{
        //广播接收的内容
        @Override
        public void onReceive(Context context, Intent intent) {
          ConnectivityManager connectivityManager =
                  (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            //获取手机已经处于断开网络状态
            if (info == null ||!info.isConnected()){
                Log.e("clj","断网状态触发");
                finish();
            }
            if (info == null)
                return;

            //切换到我们的移动网络
            if (info.getType()==ConnectivityManager.TYPE_MOBILE){
                Log.e("clj","切换到移动网络");
                finish();
            }
            //切换到我们的wi-fi网络
            if (info.getType()==ConnectivityManager.TYPE_WIFI){
                Log.e("clj","切换到wi-fi网络");
            }
        }
                {



        }
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        //销毁广播
        unregisterReceiver(netWorkChangedReciever);
        //取消订阅云端消息
        mDevice.setListener(null);
        mDevice.setSubscribe("7a410b2a5fd54e5fa1e279acaff509f6",false);
    }
}
