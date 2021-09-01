package com.lovdream.factorykit.items;

import com.lovdream.factorykit.items.ChargingTest;

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
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.Utils;
import com.lovdream.factorykit.TestItemBase;

/**
 * Created by yangzhiming on 2017/7/5.
 */

public class BackChargeTest extends TestItemBase {
	private static final int BATTERY_PLUGGED_NONE = 0;
	private static final String CURRENT = "/sys/class/power_supply/battery/current_now";
	private static final String GPIO_NODE = "/sys/class/ext_dev/function/pogo_irq";
	private static final String ISCHARGING = "/sys/class/power_supply/battery/status";
	private String mInfo = "";
	private int node_state;
	private TextView mInfoView;
	private IntentFilter mFilter;
	private boolean disconnected;
	private final static String usbACTION = "android.hardware.usb.action.USB_STATE";
	private static int a = 0;
	
	boolean isCanFinish = false;
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if ("Charging".equals(getBatteryInfo(ISCHARGING))) {
				disconnected = false;
			} else {
				disconnected = true;
			}
			if (!disconnected) {
				updateButton();
			}
			updateInfo(intent);
			updateInfoView();
		}
	};

	@Override
	public String getKey() {
		return "back_charge";
	}
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		isCanFinish=true;
	}

	@Override
	public String getTestMessage() {
		return getActivity().getString(R.string.charging_test_mesg);
	}

	@Override
	public void onResume() {
		super.onResume();
		enableSuccess(true);
	}

	@Override
	public void onStartTest() {
		if ("Charging".equals(getBatteryInfo(ISCHARGING))) {
			disconnected = false;
		} else {
			disconnected = true;
		}
		isFlag = 0;
		Context context = getActivity();
		mFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent intent = context.registerReceiver(null, mFilter);
		updateInfo(intent);
		mFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
		mFilter.addAction(usbACTION);
		context.registerReceiver(mReceiver, mFilter);
		
		
		final Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (isCanFinish) {
					timer.cancel();
					cancel();
				}
				Log.d("xxfkkk", "state--->" + getBatteryInfo(GPIO_NODE));
				try {
					if ("1".equals(getBatteryInfo(GPIO_NODE))) {
						enableSuccess(true);
						isCanFinish = true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 100, 1500);

	}

	private int isFlag = 0;

	private void updateInfo(Intent intent) {
		Context context = getActivity();
		if (intent == null) {
			mInfo = context.getString(R.string.charging_no_info);//无法获取;
			enableSuccess(false);
			return;
		}
		//mCurrent = 0;
		mInfo = "";
		if (disconnected) {
			mInfo = context.getString(R.string.charging_state_none);//断开
			enableSuccess(false);
			return;
		}
		mInfo += context.getString(R.string.charging_state_charging) + "\n";

		try {
			node_state =Integer.valueOf( getBatteryInfo(GPIO_NODE));
		} catch (Exception e) {
			node_state = 0;
			e.printStackTrace();
		}
		
		mInfo += context.getString(node_state==1?R.string.back_charge_yes:R.string.back_charge_no);
		mInfo+="\n";
		if(node_state==0){
			enableSuccess(false);
		}else if(node_state==1 && !disconnected ){
			enableSuccess(true);
		}
	}

	private void updateInfoView() {
		if (mInfoView != null) {
			mInfoView.setText(mInfo);
		}
	}

	private String getPluggedTypeString(int type) {
		if (type == BatteryManager.BATTERY_PLUGGED_AC) {
			return getActivity().getString(R.string.charging_state_ac);
		} else if (type == BatteryManager.BATTERY_PLUGGED_USB) {
			return getActivity().getString(R.string.charging_state_usb);
		} else if (type == BatteryManager.BATTERY_PLUGGED_WIRELESS) {
			return getActivity().getString(R.string.charging_state_wirless);
		}
		return getActivity().getString(R.string.charging_state_unknown);
	}

	public void updateButton() {
		postSuccess();
	}

	@Override
	public void onStopTest() {
		try {
			getActivity().unregisterReceiver(mReceiver);
		} catch (Exception e) {
			// ignore
		}
	}

	@Override
	public View getTestView(LayoutInflater inflater) {
		View v = inflater.inflate(R.layout.test_mesg_view, null);
		mInfoView = (TextView) v.findViewById(R.id.test_mesg_view);
		return v;
	}

	private int calcCurrent(String current) {
		int ret = 0;

		if ((current == null) || ("".equals(current))) {
			return ret;
		}

		try {
			ret = Integer.valueOf(current) / 1000;
		} catch (Exception e) {
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
