
package com.lovdream.factorykit.items;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.view.LayoutInflater;

import java.util.List;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.Utils;
import com.lovdream.factorykit.TestItemBase;


public class WiFiTest extends TestItemBase{

	private WifiLock mWifiLock;
	private WifiManager mWifiManager;
	private List<ScanResult> wifiScanResult;
	private TextView mTextView;
	final int SCAN_INTERVAL = 4000;
	final int OUT_TIME = 40000;
	IntentFilter mFilter = new IntentFilter();
	static String TAG = "WiFi";
	private boolean scanResultAvailabe = false;
	private static Context mContext = null;
	private int connectedId = -1;
	private boolean isSearchStarted = false;
	private int tickCount = 0;
	
	@Override
	public String getKey(){
		return "wifi_test";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.wifi_test_title);
	}

	@Override
	public void onStartTest(){
        isSearchStarted = false;

		mContext = getActivity();
		getService();

		/** Keep Wi-Fi awake */
		mWifiLock = mWifiManager.createWifiLock(
				WifiManager.WIFI_MODE_SCAN_ONLY, "WiFi");
		if (false == mWifiLock.isHeld())
			mWifiLock.acquire();

		switch (mWifiManager.getWifiState()) {
			case WifiManager.WIFI_STATE_DISABLED:
				enableWifi(true);
				break;
			case WifiManager.WIFI_STATE_DISABLING:
                                      enableWifi(true);
				//fail(getString(R.string.wifi_is_closing));
				break;
			case WifiManager.WIFI_STATE_UNKNOWN:
				fail(getString(R.string.wifi_state_unknown));
				break;
			default:
				break;
		}

		mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		tickCount = 0;
		mCountDownTimer.start();

		mContext.registerReceiver(mReceiver, mFilter);
	}

	@Override
	public void onStopTest(){
		mContext.unregisterReceiver(mReceiver);

		stopTest();
	}

	@Override
	public View getTestView(LayoutInflater inflater){
		View v = inflater.inflate(R.layout.wifi_test,null);
		bindView(v);
		return v;
	}


	public void stopTest() {
		scanResultAvailabe = false;
		// User may press back key while showing the AP list.
		if (wifiScanResult != null && wifiScanResult.size() > 0) {
			loge("wifi scan success");
		}

		if(connectedId != -1){
			mWifiManager.forget(connectedId,null);
		}
		wifiScanResult = null;
		mTextView.setText("");
        //enableWifi(false);

		//Utils.enableWifi(mContext, false);
		try {
			mCountDownTimer.cancel();
			if (true == mWifiLock.isHeld())
				mWifiLock.release();
		} catch (Exception e) {
			loge(e);
		}
	}

	private void enableWifi(boolean enable) {

		if (mWifiManager != null)
			mWifiManager.setWifiEnabled(enable);
	}

	void bindView(View v) {

		mTextView = (TextView) v.findViewById(R.id.wifi_hint);
		mTextView.setText(getString(R.string.wifi_test_mesg));
	}

	void getService() {

		mWifiManager = (WifiManager) getActivity()
				.getSystemService(Context.WIFI_SERVICE);
		
	}

	CountDownTimer mCountDownTimer = new CountDownTimer(OUT_TIME, SCAN_INTERVAL){
	
		//tickCount = 0;

		@Override
		public void onFinish() {

			logd("Timer Finish");
			if (wifiScanResult == null || wifiScanResult.size() == 0) {
				fail(getString(R.string.wifi_scan_null));
			}
			tickCount = 0;
		}

		@Override
		public void onTick(long arg0) {

			tickCount++;
			logd("Timer Tick " + tickCount);
			// At least conduct startScan() 3 times to ensure wifi's scan
			if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {

			if (!isSearchStarted){
                mWifiManager.startScan();
                isSearchStarted = true;
            }
				// When screen is dim, SCAN_RESULTS_AVAILABLE_ACTION cannot be
				// got.
				// So get it actively
				if (tickCount > 10 && !scanResultAvailabe && mWifiManager.getScanResults() != null) {
					wifiScanResult = mWifiManager.getScanResults();
					scanResultAvailabe = true;
					mHandler.sendEmptyMessage(0);
				}
			}

		}
	};

	static String wifiInfos = "";
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
					mTextView.setText(s);
				}
				if(!connectTestAp()){
					String connection_fall_msg = getString(R.string.connection_fall,getTestSSID());
					fail(connection_fall_msg);
				}

			} else {
				fail(getString(R.string.wifi_scan_null));
			}
		};
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
                                    fail(getString(R.string.wifi_state_unknown));
                                    break;
                                default:
                                    break;
                            }

			logd(intent.getAction() + "       ,state =  "+mWifiManager.getWifiState());
			if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent
					.getAction())) {
				if (!scanResultAvailabe && mWifiManager.getScanResults() != null) {
					wifiScanResult = mWifiManager.getScanResults();
					scanResultAvailabe = true;
					mHandler.sendEmptyMessage(0);
				}
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
					String connection_success_msg = getString(R.string.connection_success,getTestSSID());
					toast(connection_success_msg);
					pass(null);
				}
			}
		}

	};

	private String formatSSID(String ssid){
		if(ssid == null){
			return "";
		}
		return ssid.replace("\"","").trim();
	}

	protected String getTestSSID(){
		return "lovdream";
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
		mWifiManager.disconnect();
		connectedId = mWifiManager.addNetwork(config);
		boolean ret =  mWifiManager.enableNetwork(connectedId, true); 
		mWifiManager.reconnect();
		return ret;
		
	}

	void fail(Object msg) {

		loge(msg);
		toast(msg);
		postFail();
	}

	void pass(String msg) {

		//Utils.enableWifi(mContext, false);
		postSuccess();
	}

	public void toast(Object s) {

		if (s == null)
			return;
		Toast.makeText(mContext, s + "", Toast.LENGTH_SHORT).show();
	}

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
}
