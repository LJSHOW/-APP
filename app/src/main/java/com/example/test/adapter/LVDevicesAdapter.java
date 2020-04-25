package com.example.test.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.test.R;
import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus;

import java.util.List;

/**
 *
 */

public class LVDevicesAdapter extends BaseAdapter {

    private Context mContext;
    private List<GizWifiDevice>  gizWifiDeviceList;
    private LayoutInflater mLayoutInflater;

    public LVDevicesAdapter(Context mContext, List<GizWifiDevice> gizWifiDeviceList) {
        this.mContext = mContext;
        this.gizWifiDeviceList = gizWifiDeviceList;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return gizWifiDeviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return gizWifiDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHodlerListView viewHodlerListView =null;
        View convertView1;
        GizWifiDevice device = gizWifiDeviceList.get(position);
        if (convertView==null){

            convertView1 = mLayoutInflater.inflate(R.layout.item_list_view_devices,null);
            viewHodlerListView = new ViewHodlerListView();
            //绑定控件
            viewHodlerListView.mTvName = convertView1.findViewById(R.id.tvDeviceName);
            viewHodlerListView.mTvStatus = convertView1.findViewById(R.id.tvStatus);
            viewHodlerListView.mIvDeviceIcon = convertView1.findViewById(R.id.ivDeviceIcon);
            viewHodlerListView.mIvNext = convertView1.findViewById(R.id.ivNext);
            convertView1.setTag(viewHodlerListView);
        }else {

            convertView1 = convertView;
            viewHodlerListView = (ViewHodlerListView) convertView1.getTag();
        }

        //设置名字。如果用户已经设置了该设备的别名，那么就优先显示别名
        if (device.getAlias().isEmpty()){
            viewHodlerListView.mTvName.setText(device.getProductName());
        }else {
            viewHodlerListView.mTvName.setText(device.getAlias());
        }
        //设置状态
        if (device.getNetStatus() == GizWifiDeviceNetStatus.GizDeviceOffline){

            viewHodlerListView.mTvStatus.setText("离线");
            viewHodlerListView.mTvStatus.setTextColor(mContext.getResources().getColor(R.color.app_color_description));
            viewHodlerListView.mIvNext.setVisibility(View.VISIBLE);
        }else {
            //如果设备不为离线状态，那么进一步的剖析他的远程状态
            if (device.isLAN()){
                viewHodlerListView.mTvStatus.setText("本地在线");
            }else {
                viewHodlerListView.mTvStatus.setText("远程在线");
            }
            viewHodlerListView.mTvStatus.setTextColor(mContext.getResources().getColor(R.color.black));
            viewHodlerListView.mIvNext.setVisibility(View.VISIBLE);//把箭头显示出来
        }


        return convertView1;
    }

    private class ViewHodlerListView{
        //设备图标,箭头
        ImageView mIvDeviceIcon,mIvNext;
        //设备名字和设备状态
        TextView mTvName,mTvStatus;


    }
}
