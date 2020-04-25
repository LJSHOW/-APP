package com.example.test;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.test.Utils.SharePreUtil;
import com.example.test.adapter.LVDevicesAdapter;
import com.example.test.ui.DevicesControlActivity.DeviceAirActivity;
import com.example.test.ui.NetConfigActivity;
import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.api.GizWifiSDK;
import com.gizwits.gizwifisdk.enumration.GizEventType;
import com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.gizwifisdk.listener.GizWifiDeviceListener;
import com.gizwits.gizwifisdk.listener.GizWifiSDKListener;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private LVDevicesAdapter adapter;
    private List<GizWifiDevice> gizWifiDeviceList;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    //刷新的弹窗
    private QMUITipDialog refleshTipDialog;
    private QMUITipDialog mTipDialog;


    private Handler mHandler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what==105)
                adapter.notifyDataSetChanged();
        }
    };
    private String uid;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initSDK();
        initView();
    }

    private void initView() {
        QMUITopBar topBar=findViewById(R.id.topBar);
        topBar.setTitle("智能家居");
        //右边添加加号的图标
        topBar.addRightImageButton(R.mipmap.ic_add,R.id.topbar_right_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, NetConfigActivity.class));

            }
        });
        gizWifiDeviceList = new ArrayList<>();
        listView =findViewById(R.id.listView);
        adapter=new LVDevicesAdapter(this,gizWifiDeviceList);
        listView.setAdapter(adapter);

        //轻触的点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startControl(gizWifiDeviceList.get(position));

            }
        });

        //长按3秒的点击事件
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showLongDialogOnClick(gizWifiDeviceList.get(position));
                return true;
            }
        });

        getBoundDevices();
        mSwipeRefreshLayout =findViewById(R.id.swipeRefreshLayout);
        //设置下拉的颜色
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.white);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.app_color_theme_1,R.color.app_color_theme_2,
                R.color.app_color_theme_3,R.color.app_color_theme_4,R.color.app_color_theme_5
                ,R.color.app_color_theme_6,R.color.app_color_theme_7);
        //手动调用系统通知测量
        mSwipeRefreshLayout.measure(0,0);
        //打开页面就是下拉的状态
        mSwipeRefreshLayout.setRefreshing(true);
        //设置手动下拉的监听事件
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refleshTipDialog = new QMUITipDialog.Builder(MainActivity.this)
                        .setTipWord("正在刷新...")
                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                        .create();
                refleshTipDialog.show();
                //拿到SDK里面的设备
                mSwipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //如果拿到SDK里面的设备
                        if (GizWifiSDK.sharedInstance().getDeviceList().size()!=0){
                            gizWifiDeviceList.clear();
                            gizWifiDeviceList.addAll(GizWifiSDK.sharedInstance().getDeviceList());
                            adapter.notifyDataSetChanged();
                        }
                        refleshTipDialog.dismiss();
                        mSwipeRefreshLayout.setRefreshing(false);

                        ConnectivityManager connectivityManager =
                                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
                        //获取手机已经处于断开网络状态
                        if (info == null ||!info.isConnected()){
                            mTipDialog = new QMUITipDialog.Builder(MainActivity.this)
                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                    .setTipWord("获取失败，请检查网络").create();
                            mTipDialog.show();
                            listView.setVisibility(View.GONE);

                        }else {
                            listView.setVisibility(View.VISIBLE);
                            //显示另外的一个弹窗,如果获取到的设备为空的话
                            if (gizWifiDeviceList.size() == 0) {
                                mTipDialog = new QMUITipDialog.Builder(MainActivity.this)
                                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_NOTHING)
                                        .setTipWord("暂无设备").create();
                                mTipDialog.show();
                            } else {
                                mTipDialog = new QMUITipDialog.Builder(MainActivity.this)
                                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                                        .setTipWord("获取成功").create();
                                mTipDialog.show();

                            }
                        }
                        mSwipeRefreshLayout.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mTipDialog.dismiss();
                            }
                        },1500);
                    }
                },3000);
            }
        });
        //3s自动收回
        listView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);

            }
        },   3000);
    }
   //跳转控制，只有在线可控设备才能跳转
    private void startControl(GizWifiDevice device) {
        //判断设备是否在线
        if (device.getNetStatus() == GizWifiDeviceNetStatus.GizDeviceOffline)
            return;
        device.setListener(mWifiDeviceListener);
        device.setSubscribe("7a410b2a5fd54e5fa1e279acaff509f6",true);

    }

    private void showLongDialogOnClick(final GizWifiDevice device) {
        //显示弹窗
        String[] items = new String[]{"重命名","取消设备绑定"};
        new QMUIDialog.MenuDialogBuilder(this).addItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        showReNameDialag(device);
                        break;
                    case 1:
                        showDeleteDialog(device);
                        break;
                }
                dialog.dismiss();
            }
        }).show();
    }
//解除绑定设备
    private void showDeleteDialog(final GizWifiDevice device) {
        new QMUIDialog.MessageDialogBuilder(this)
                .setTitle("您可以解绑远程设备").setMessage("确定解除绑定？")
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();

                    }
                }).addAction("删除", new QMUIDialogAction.ActionListener() {
            @Override
            public void onClick(QMUIDialog dialog, int index) {
                GizWifiSDK.sharedInstance().unbindDevice(uid,token,device.getDid());
                dialog.dismiss();

            }
        }).show();
    }

    //重命名的操作
    private void showReNameDialag(final GizWifiDevice device) {
        final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(this);
        builder.setTitle("重命名操作").setInputType(InputType.TYPE_CLASS_TEXT)
                .setPlaceholder("再次输入新名字")
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("确定", new QMUIDialogAction.ActionListener() {
            @Override
            public void onClick(QMUIDialog dialog, int index) {
                String newName = builder.getEditText().getText().toString().trim();
                //判断是否输入为空
                if (newName.isEmpty()) {
                    Toast.makeText(MainActivity.this, "输入为空，修改失败！", Toast.LENGTH_SHORT);
                }else{
                    device.setListener(mWifiDeviceListener);
                    device.setCustomInfo(null,newName);
                }
                dialog.dismiss();
            }
        }).show();


    }

    private void getBoundDevices() {
        uid = SharePreUtil.getString(this,"_uid",null);
        token = SharePreUtil.getString(this,"_token",null);

        if (uid !=null&& token !=null)
        GizWifiSDK.sharedInstance().getBoundDevices(uid, token);
    }


    private void initSDK(){
        // 设置 SDK 监听
        GizWifiSDK.sharedInstance().setListener(mListener);
// 设置 AppInfo
        ConcurrentHashMap<String, String> appInfo =  new ConcurrentHashMap<>();
        appInfo.put("appId", "6fb27af9f89141fb8970f0f38af7fc6b");

                appInfo.put("appSecret", "c8098687acbd4dbcb51e1d534c95bfe8");
// 设置要过滤的设备 productKey 列表。不过滤则直接传 null
        List<ConcurrentHashMap<String, String>> productInfo = new ArrayList<>();
        ConcurrentHashMap<String, String> product =  new ConcurrentHashMap<>();
        product.put("productKey", "dce8e45f53ef4b9d91d3a55ac5795085");
        product.put("productSecret", "7a410b2a5fd54e5fa1e279acaff509f6");
        productInfo.add(product);
        GizWifiSDK.sharedInstance().startWithAppInfo(this, appInfo, productInfo, null, false);

    }


    private GizWifiSDKListener mListener = new GizWifiSDKListener() {
        @Override
        public void didUnbindDevice(GizWifiErrorCode result, String did) {
            if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                 // 解绑成功
                Toast.makeText(MainActivity.this,"解绑成功",Toast.LENGTH_SHORT).show();
            } else {
                 // 解绑失败
                Toast.makeText(MainActivity.this,"解绑失败" + result,Toast.LENGTH_SHORT).show();
            }
        }
        //        @Override
//        public void didBindDevice(GizWifiErrorCode result, String did) {
//            super.didBindDevice(result, did);
//            if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
//                // 绑定成功
//                Toast.makeText(MainActivity.this,"绑定成功了",Toast.LENGTH_SHORT).show();
//            } else {
//                // 绑定失败
//                Toast.makeText(MainActivity.this,"绑定失败",Toast.LENGTH_SHORT).show();
//
//            }
//        }

        @Override
        public void didNotifyEvent(GizEventType eventType, Object eventSource, GizWifiErrorCode eventID, String eventMessage) {
            super.didNotifyEvent(eventType, eventSource, eventID, eventMessage);
            Log.e("TEST", "didNotifyEvent" + eventType.toString());
            //如果我们的SDK初始化成功，就匿名登入
            //匿名登入，匿名方式登入，不需要注册用户账号
            if (eventType == GizEventType.GizEventSDK) {
                GizWifiSDK.sharedInstance().userLoginAnonymous();
            }
        }

        @Override
        public void didUserLogin(GizWifiErrorCode result, String uid, String token) {
            super.didUserLogin(result, uid, token);
            if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                // 登录成功
                Log.e("LJ","登录成功");
                Log.e("LJ","uid:"+uid);
                Log.e("LJ","token:"+token);
                SharePreUtil.putString(MainActivity.this,"_uid",uid);
                SharePreUtil.putString(MainActivity.this,"_token",token);
            } else {
                // 登录失败
                Log.e("LJ","登录失败");
            }
        }

        /**
         *
         * @param result
         * @param deviceList 已经在局域网发现的设备，包括未绑定的设备
         */

        @Override
        public void didDiscovered(GizWifiErrorCode result, List<GizWifiDevice> deviceList) {
            super.didDiscovered(result, deviceList);
            Log.e("LJ","已经绑定的deviceList"+deviceList);
            //每次拿到数据都要清空设备集合
            gizWifiDeviceList.clear();
            gizWifiDeviceList.addAll(deviceList);
            for (int i = 0; i <deviceList.size() ; i++) {
                //判断此设备是否已经绑定
                if (!deviceList.get(i).isBind()){
                    starBindDevice(deviceList.get(i));
                }
                
            }
            mHandler.sendEmptyMessage(105);
        }
    };

    private void starBindDevice(GizWifiDevice device) {
        if (token!=null&&uid!=null)
           GizWifiSDK.sharedInstance().bindRemoteDevice(uid,token,device.getMacAddress(),
                   "dce8e45f53ef4b9d91d3a55ac5795085",
                   "7a410b2a5fd54e5fa1e279acaff509f6");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //保证每次打开页面都能正常的回调SDK监听
        GizWifiSDK.sharedInstance().setListener(mListener);

    }

    private GizWifiDeviceListener mWifiDeviceListener = new GizWifiDeviceListener(){
       //设备订阅或解除订阅的回调
        @Override
        public void didSetSubscribe(GizWifiErrorCode result, GizWifiDevice device, boolean isSubscribed) {
            super.didSetSubscribe(result, device, isSubscribed);
            Log.e("LJ","订阅结果" + result);
            Log.e("LJ","订阅设备" + device);
            //如果成功的订阅回调则可以跳转
            if (result==GizWifiErrorCode.GIZ_SDK_SUCCESS){
                Intent intent = new Intent(MainActivity.this, DeviceAirActivity.class);
                intent.putExtra("_device",device);
                startActivity(intent);

            }
        }

        @Override
        public void didSetCustomInfo(GizWifiErrorCode result, GizWifiDevice device) {
            super.didSetCustomInfo(result, device);
            if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
                // 修改成功
                if (GizWifiSDK.sharedInstance().getDeviceList().size()!=0){

                    gizWifiDeviceList.clear();
                    gizWifiDeviceList.addAll(GizWifiSDK.sharedInstance().getDeviceList());
                    adapter.notifyDataSetChanged();
                    Toast.makeText(MainActivity.this, "修改成功", Toast.LENGTH_SHORT);
                }
            } else {
                Toast.makeText(MainActivity.this, "修改失败！", Toast.LENGTH_SHORT);
                // 修改失败
            }
        }
    };
}


