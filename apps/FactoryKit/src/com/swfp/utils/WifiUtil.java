package com.swfp.utils;
import java.util.List;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.Utils;
import com.swfp.model.WifiModel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

public class WifiUtil {
	Context mContext;
    WifiManager mWifiManager;
    WifiLock mWifiLock;
    IntentFilter mFilter;
	 List<ScanResult> wifiScanResult;
	final int SCAN_INTERVAL = 4000;
	final int OUT_TIME = 30000;
	static String TAG = "WiFi";
    boolean scanResultAvailabe = false;
    static String wifiInfos = "";
	
	private int connectedId = -1;
	
	WifiModel wifiModel;
	
	public WifiUtil(Context context,WifiModel wifiModel){
		mContext = context;
		this.wifiModel = wifiModel;
		
	}
	
	
	public WifiUtil start(){
		
		init();
		
		if (false == mWifiLock.isHeld())
			mWifiLock.acquire();
		
		ensureWifiEnabled();
		
		mCountDownTimer.start();

		registerListen();
		return this;
		
		
	}
	
	
	public void stop(){
		scanResultAvailabe = false;
		if (wifiScanResult != null && wifiScanResult.size() > 0) {
			loge("wifi scan success");
		}

		if(connectedId != -1){
			mWifiManager.forget(connectedId,null);
		}
                    enableWifi(false);

		Utils.enableWifi(mContext, false);
		try {
			mCountDownTimer.cancel();
			if (true == mWifiLock.isHeld())
				mWifiLock.release();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			mContext.unregisterReceiver(mReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void init(){
		mWifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		mWifiLock = mWifiManager.createWifiLock(
				WifiManager.WIFI_MODE_SCAN_ONLY, "WiFi");
		
		mFilter = new IntentFilter();
		mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
	}
	
	
	public void ensureWifiEnabled(){
		switch (mWifiManager.getWifiState()) {
		case WifiManager.WIFI_STATE_DISABLED:
			enableWifi(true);
			break;
		case WifiManager.WIFI_STATE_DISABLING:
                                  enableWifi(true);
			//fail(getString(R.string.wifi_is_closing));
			break;
		case WifiManager.WIFI_STATE_UNKNOWN:
			wifiModel.wifiBack.failed(mContext.getString(R.string.wifi_state_unknown));
			break;
		default:
			break;
	}
	}
	
	private void enableWifi(boolean enable) {

		if (mWifiManager != null)
			mWifiManager.setWifiEnabled(enable);
	}
	
	
	private void registerListen(){
		mContext.registerReceiver(mReceiver, mFilter);
	}
	
	
	CountDownTimer mCountDownTimer = new CountDownTimer(OUT_TIME, SCAN_INTERVAL){
		
		private int tickCount = 0;

		@Override
		public void onFinish() {

			logd("Timer Finish");
			if (wifiScanResult == null || wifiScanResult.size() == 0) {
				wifiModel.wifiBack.failed(mContext.getString(R.string.wifi_scan_null));
			}
			tickCount = 0;
		}

		@Override
		public void onTick(long arg0) {

			tickCount++;
			logd("Timer Tick");
			// At least conduct startScan() 3 times to ensure wifi's scan
			if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
				mWifiManager.startScan();
				// When screen is dim, SCAN_RESULTS_AVAILABLE_ACTION cannot be
				// got.
				// So get it actively
				if (tickCount >= 6 && !scanResultAvailabe) {
					wifiScanResult = mWifiManager.getScanResults();
					scanResultAvailabe = true;
					mHandler.sendEmptyMessage(0);
				}
			}

		}
	};
	

	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		public void onReceive(Context c, Intent intent) {
                            switch (mWifiManager.getWifiState()) {
                                case WifiManager.WIFI_STATE_DISABLED://11
                                    enableWifi(true);
                                    break;
                                case WifiManager.WIFI_STATE_DISABLING://0
                                    enableWifi(true);
                                    //fail(getString(R.string.wifi_is_closing));
                                    break;
                                case WifiManager.WIFI_STATE_UNKNOWN:
                                	wifiModel.wifiBack.failed(mContext.getString(R.string.wifi_state_unknown));
                                    break;
                                default:
                                    break;
                            }

			logd(intent.getAction() + "       ,state =  "+mWifiManager.getWifiState());
			if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent
					.getAction())) {

					wifiScanResult = mWifiManager.getScanResults();
					scanResultAvailabe = true;
					mHandler.sendEmptyMessage(0);

			}else if(WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())){
				NetworkInfo ni = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				if((ni != null) && !ni.isConnected()){
					return;
				}
				WifiInfo info = (WifiInfo)intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
				if((info == null)){
					 info = mWifiManager.getConnectionInfo();
				}
				logd(info);
				if((info != null) && getTestSSID().equals(formatSSID(info.getSSID()))){
					
					//这里增加判断hz是否是对的;
					if(!wifiModel.wifiBack.isFrequencyNotErr(info.getFrequency())){
						wifiModel.wifiBack.failed(mContext.getResources().getString(R.string.frequency_wrong));
						return;
					}

					String connection_success_msg =mContext.getString(R.string.connection_success,getTestSSID());
					wifiModel.wifiBack.toast(connection_success_msg);
					wifiModel.wifiBack.pass();
				}
			}
		}

	};
	
	public String  getTestSSID(){
		if(wifiModel!=null)
			return wifiModel.testSSID;
		return "";
		
	}
	
	
	
	Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {

			String s = mContext.getString(R.string.wifi_test_mesg) + "\n\n" + "AP List:\n";
			wifiInfos = "";
			if (wifiScanResult != null && wifiScanResult.size() > 0) {
				for (int i = 0; i < wifiScanResult.size(); i++) {
					logd(wifiScanResult.get(i));
					s += " " + i + ": " + wifiScanResult.get(i).SSID + "\n\n";
					wifiInfos += " " + i + ": "
							+ wifiScanResult.get(i).toString() + "\n\n";
					wifiModel.wifiBack.showMsg(s);
				}
				if(!connectTestAp()){
					String connection_fall_msg = mContext.getString(R.string.connection_fall,getTestSSID());
					wifiModel.wifiBack.failed(connection_fall_msg);
				}

			} else {
				//fail(getString(R.string.wifi_scan_null));
			}
		};
	};
	


	private void loge(Object e) {

		if (e == null)
			return;
		Thread mThread = Thread.currentThread();
		StackTraceElement[] mStackTrace = mThread.getStackTrace();
		String mMethodName = mStackTrace[3].getMethodName();
		e = "[" + mMethodName + "] " + e;
		Log.e(TAG, e + "");
	}

	private void logd(Object s) {

		Thread mThread = Thread.currentThread();
		StackTraceElement[] mStackTrace = mThread.getStackTrace();
		String mMethodName = mStackTrace[3].getMethodName();

		s = "[" + mMethodName + "] " + s;
		Log.d(TAG, s + "");
	}
	
	private String formatSSID(String ssid){
		if(ssid == null){
			return "";
		}
		return ssid.replace("\"","").trim();
	}
	
	private boolean connectTestAp(){
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear(); 
		config.allowedGroupCiphers.clear(); 
		config.allowedKeyManagement.clear(); 
		config.allowedPairwiseCiphers.clear(); 
		config.allowedProtocols.clear(); 
		config.SSID = "\"" + getTestSSID() + "\"";   
		config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE); 
		connectedId = mWifiManager.addNetwork(config);
		return mWifiManager.enableNetwork(connectedId, true); 
	}

}
