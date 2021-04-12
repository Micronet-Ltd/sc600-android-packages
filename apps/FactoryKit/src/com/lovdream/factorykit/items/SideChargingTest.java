
package com.lovdream.factorykit.items;

import android.view.View;
import android.os.Vibrator;
import android.os.BatteryManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.content.BroadcastReceiver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import android.util.Log;
import com.lovdream.factorykit.R;
import com.lovdream.factorykit.Utils;
import com.lovdream.factorykit.TestItemBase;

public class SideChargingTest extends TestItemBase{

    private static final int BATTERY_PLUGGED_NONE = 0;
	private static final String CURRENT = "/sys/class/power_supply/battery/current_now";
    private static final String ISCHARGING ="/sys/class/power_supply/battery/status";//"/sys/class/power_supply/battery/current_now";
    private static final String CURRENT_MAX= "/sys/class/power_supply/battery/current_max";
    // private static final int SIDE_CHARGING_CURRENT_MAX = 1500;
	private String mInfo = "";
	private int mCurrent;
	private int mCurrentMax;
	private TextView mInfoView;
	private IntentFilter mFilter;
	private boolean disconnected;
    private final static String usbACTION ="android.hardware.usb.action.USB_STATE";
    private static int a =0;
	private BroadcastReceiver mReceiver = new BroadcastReceiver(){
		@Override
         public void onReceive(Context context, Intent intent) {
                            // final int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                            // BatteryManager.BATTERY_STATUS_UNKNOWN);
                            // mCurrentMax = calcCurrent(getBatteryInfo(CURRENT_MAX));
                            if("Charging".equals(getBatteryInfo(ISCHARGING))){
                                disconnected =false;
                            } else {
                                disconnected =true;
                            }

                            if(!disconnected
                            // && mCurrentMax == SIDE_CHARGING_CURRENT_MAX
                                ) {
                                updateButton();
                             }
                            updateInfo(intent);
                            updateInfoView();
                    }
         };

	@Override
	public String getKey(){
		return "side_charging_test";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.charging_test_mesg);
	}

	@Override
	public void onStartTest(){
	    if("Charging".equals(getBatteryInfo(ISCHARGING))){
            disconnected =false;
        } else {
            disconnected =true;
        }
        isFlag = 0;
		Context context = getActivity();
		mFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent intent = context.registerReceiver(null,mFilter);
		mCurrentMax = calcCurrent(getBatteryInfo(CURRENT_MAX));
		updateInfo(intent);
        mFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        mFilter.addAction(usbACTION);
		context.registerReceiver(mReceiver,mFilter);

	}
	private int isFlag=0;
	private void updateInfo(Intent intent){
		Context context = getActivity();
		if(intent == null){
			mInfo = context.getString(R.string.charging_no_info);
			enableSuccess(false);
			return;
		}

        mCurrent = 0;
        mInfo = "";
        // if(!disconnected
        //  // && mCurrentMax == SIDE_CHARGING_CURRENT_MAX
        //  ) {//connect
        //     if(isFlag ==0)
        //         isFlag = 1;
        // } else {//disconnect
        //     if("Charging".equals(getBatteryInfo(ISCHARGING))){ } else {
        //         if(isFlag ==1)
        //         isFlag =2;
        //         mInfo = context.getString(R.string.charging_state_none);
        //         enableSuccess(false);
        //         return;
        //     }
        // }
        if(disconnected){
            mInfo = context.getString(R.string.charging_state_none);
            enableSuccess(false);
            return;
        }
        mInfo += context.getString(R.string.side_charging_state)+ "\n";
        mCurrent = calcCurrent(getBatteryInfo(CURRENT));
        mInfo += context.getString(R.string.charging_current_label,mCurrent) + "\n";
    }
    private void updateInfoView(){
        if(mInfoView != null){
            mInfoView.setText(mInfo);
        }
    }

    public void updateButton(){
        postSuccess();
    }
	@Override
	public void onStopTest(){
		try{
			getActivity().unregisterReceiver(mReceiver);
		}catch(Exception e){
			//ignore
		}
	}

	@Override
	public View getTestView(LayoutInflater inflater){
		View v = inflater.inflate(R.layout.test_mesg_view,null);
		mInfoView = (TextView)v.findViewById(R.id.test_mesg_view);
		return v;
	}

	private int calcCurrent(String current){
		int ret = 0;

		if((current == null) || ("".equals(current))){
			return ret;
		}

		try{
			ret = Integer.valueOf(current) / 1000;
		}catch(Exception e){
			e.printStackTrace();
		}

		return Math.abs(ret);
	}

    private String getBatteryInfo(String path) {

        File mFile;
        FileReader mFileReader;
        mFile = new File(path);

        try {
            mFileReader = new FileReader(mFile);
            char data[] = new char[128];
            int charCount;
            String status[] = null;
            try {
                charCount = mFileReader.read(data);
                status = new String(data, 0, charCount).trim().split("\n");
                return status[0];
            } catch (IOException e) {
				e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
			e.printStackTrace();
        }
        return null;
    }
}
