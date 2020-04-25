package com.example.test.ui;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.test.MainActivity;
import com.example.test.R;

import java.util.ArrayList;
import java.util.List;

//闪屏页
public class SplashActivity extends AppCompatActivity {
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        if (msg.what==107){
                startActivity(new Intent(SplashActivity.this,MainActivity.class));
            }
        finish();

        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        checkAndroidPermission();

    }
    private void checkAndroidPermission(){
        //如果当前手机系统是安卓6.0或者以上的版本需要进行动态授权
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            requestRunPermission(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.READ_PHONE_STATE});

        }else {
            mHandler.sendEmptyMessageDelayed(107,2500);

        }
    }

    private void requestRunPermission(String[] strings) {
        int status = 0;
        for (String permission:strings) {
            //检查当前权限是否已经授权
            if (ContextCompat.checkSelfPermission(this,permission)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,strings,108);
            }else {
                status++;
            }
        }
        if (status==5){
            mHandler.sendEmptyMessageDelayed(107,2500);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 108:
                if (grantResults.length>0){
                    //用来存储被拒绝的权限
                    List<String> deniedPermission = new ArrayList<>();
                    for (int i=0;i<grantResults.length;i++){
                        int grantPermission = grantResults[i];
                        String permission = permissions[i];
                        if (grantPermission!=PackageManager.PERMISSION_GRANTED){
                            deniedPermission.add(permission);
                        }
                    }
                    if (deniedPermission.isEmpty()){
                        //权限全部通过
                        mHandler.sendEmptyMessage(107);
                    }else {
                        Toast.makeText(this,"您拒绝了部分权限，需要您手动去开启",Toast.LENGTH_SHORT).show();
                        mHandler.sendEmptyMessageDelayed(107,2500);

                    }

                }

                break;
        }

    }
}
