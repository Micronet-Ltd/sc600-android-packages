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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.util.Log;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.Utils;
import com.lovdream.factorykit.TestItemBase;
import com.swfp.utils.ServiceUtil;

public class IgnitionTest extends TestItemBase {

    public static final String dockAction = "android.intent.action.DOCK_EVENT";
    private static final int IGNITION_ON = 2;
    private static final String TAG = "IgnitionTest";
    private int dockState = -1;
	
	private TextView mInfoView;
	private Context mContext;
	
	
	@Override
	public String getKey() {
		return "ignition_test";
	}

	@Override
	public String getTestMessage() {
		return getActivity().getString(R.string.ignition_test_mesg);
	}

	@Override
	public void onStartTest() {
		mContext =  getActivity();
        registerBroadCastReceiver();
	}
	
	@Override
	public void onStopTest() {
		unRegisterBroadCastReceiver();
    }

	@Override
	public View getTestView(LayoutInflater inflater) {
		View v = inflater.inflate(R.layout.test_mesg_view, null);
		mInfoView = (TextView) v.findViewById(R.id.test_mesg_view);
		updateInfo(getIgnState());
		return v;
	}
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
            goAsync();
            if(intent.getAction().equals(dockAction)){
                dockState = intent.getIntExtra(Intent.EXTRA_DOCK_STATE, -1);
                updateInfo(getIgnState());
			}
		}
	};	
	
	private void updateInfo(boolean isIgnitionOn) {
	
		StringBuilder sb = new StringBuilder();
            if(isIgnitionOn){
                sb.append("test pass");
                enableSuccess(true);
            }else {
                sb.append(mContext.getString(R.string.turn_on_ignition_mesg));
			}
		
		if (mInfoView != null) 
			mInfoView.setText(sb.toString());
        }
		
    
	private void registerBroadCastReceiver(){
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(dockAction);
		mContext.registerReceiver(mReceiver, mFilter);
	}
	
	private void unRegisterBroadCastReceiver(){
		try {
			getActivity().unregisterReceiver(mReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	 private boolean getIgnState() {
        if(dockState == Intent.EXTRA_DOCK_STATE_CAR)
            return true;
        else 
            return false;
    }
    
}
