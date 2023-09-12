package com.lovdream.factorykit;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.app.Fragment;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.SystemProperties;
import android.os.PowerManager;
import android.view.View;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Toast;
import android.widget.ScrollView;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

import com.android.internal.util.ArrayUtils;
import com.lovdream.factorykit.Config.TestItem;
import com.swfp.utils.ServiceUtil;
import android.os.BatteryManager;


public class AutoTestResult extends Fragment{

	private static final String TAG = Main.TAG;
	private static final String CURRENT = "/sys/class/power_supply/bms/current_now";
	private static final String BATTERY_TYPE = "/sys/class/power_supply/bms/battery_type";
    StringBuilder data;
    public static final String PASS = "Pass,";
    public static final String FAIL = "Fail,";
    public static final String NULL = "NA,";
    FactoryKitApplication app;
    Config config;
    ArrayList<TestItem> mItems;
    private static final int IGNITION_ON = 2;
    private int dockState = -1;
    private Context mContext;
    TextView tv;
    String view;
    String results;
    boolean valuesReceived = false;
    
	private String buildTestResult(){
	
		app = (FactoryKitApplication)getActivity().getApplication();
		config = app.getTestConfig();
		mItems = config.getTestItems();
		
		saveResultsToCsv();
		
		String failItems = "";
		for(TestItem item : mItems){
			//xxf
			Log.d("xxfjjj", "=============================");
			Log.d("xxfjjj", "item---->"+item.key);
			Log.d("xxfjjj", "item.fm.testFlag---->"+(item.fm.testFlag));
			Log.d("xxfjjj", "=============================");
			if(item.inAutoTest && (config.getTestFlag(item.fm.testFlag) == Config.TEST_FLAG_FAIL)){
				failItems += item.displayName + "\n";
			}
		}

		if("".equals(failItems)){
			config.setAutoTestFt(true);
			return "Automatic test of the device\nAll tests passed!";
		}else{
			config.setAutoTestFt(false);
			return "Automatic test of the device\nSome test items failed：\n\n" + failItems;
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		registerBroadCastReceiver();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ScrollView sv = new ScrollView(getActivity());
		tv = new TextView(getActivity());
		tv.setTextSize(24);
		results = "Device type: " + getDevType() + "\n" + "Number of cameras on device: " + Main.camera_count + "\n\n" + buildTestResult();
		view = results + Main.resultString + "\n\nPlease run batch script, and after that turn off the Ignition. \n Device will shutdown.\n\n\n\n\n\n";
		tv.setText(view);
		tv.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		mContext =  getActivity();
		sv.addView(tv);
		return sv;
	}
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
            goAsync();
            if(intent.getAction().equals(Intent.ACTION_DOCK_EVENT)){
                dockState = intent.getIntExtra(Intent.EXTRA_DOCK_STATE, -1);
                if(dockState != Intent.EXTRA_DOCK_STATE_CAR){
                    PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
                    pm.shutdown(false,null,false);
                }
			}
		}
	};
	
	private void registerBroadCastReceiver(){
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(Intent.ACTION_DOCK_EVENT);
		mContext.registerReceiver(mReceiver, mFilter);
	}
		
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		activity.setTitle(R.string.test_result);
	}

	@Override
	public void onDetach(){
		super.onDetach();
		getActivity().setTitle(R.string.app_name);
	}
	
	private void saveResultsToCsv() {
        data = getPhoneData(new StringBuilder());
        String filename = "/sdcard/test_results.csv";
        BufferedWriter bufferedWriter = null;
        try {
            File file = new File(filename);
            file.delete();
            bufferedWriter = new BufferedWriter(new FileWriter(file));
            //String dataWithoutImeiTest = removeWordFromData(data.toString(), 3);
            //String dataWithoutSmallBatteryIndex = removeWordFromData(dataWithoutImeiTest.toString(), 9);
            //bufferedWriter.write(resultToSha256(dataWithoutSmallBatteryIndex) + ",");
            bufferedWriter.write(data.substring(0, data.length()));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private StringBuilder getPhoneData(StringBuilder results) {
        WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        TelephonyManager telephonyManager = (TelephonyManager) getActivity().getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        results.append(getProductType() + ",");
        results.append(Build.getSerial() + ",");
        results.append(telephonyManager.getImei() + ",");
        String deviceId=telephonyManager.getImei();
        if (deviceId==null || !isValidImei(deviceId)) {
            Log.e(TAG,"Bad IMEI: "+deviceId);
            results.append(FAIL);
        } else {
            Log.i(TAG,"IMEI: "+deviceId);
            results.append(PASS);
        }
        results.append("cccc,");
        results.append(getBuildVersion(Build.DISPLAY) + ",");
        results.append("cccc,");
        results.append(wInfo.getMacAddress() + ",");
//        results.append(getCurrent() + ",");
//        results.append(Main.currentVoltage + ",");
//        results.append(isSmallBattery() + ",");
//        results.append(Main.camera_count + ",");

	TestItem[] sorted = mItems.toArray(new TestItem[0]);
	if (Main.full_auto){
		Arrays.sort(sorted,new Comparator<TestItem>(){
			@Override
			public int compare(TestItem o1, TestItem o2){
				return o1.resultIndex==o2.resultIndex?0:o1.resultIndex>o2.resultIndex?1:-1;
			}
		});
	}
        for(TestItem item : sorted){
            if(item.inAutoTest){
		if (Main.full_auto && item.resultIndex==Integer.MAX_VALUE) continue;
                Log.e("Aitam", item.resultIndex+" itemName: "+item.key+", Result:"+config.getTestFlag(item.fm.testFlag));
                if(config.getTestFlag(item.fm.testFlag) == Config.TEST_FLAG_FAIL){
                    results.append(FAIL);
                } else if(config.getTestFlag(item.fm.testFlag) == Config.TEST_FLAG_PASS) {
                    results.append(PASS);
                } else {
                    results.append(NULL);
                }
            }
        }
        String sim = telephonyManager.getSimSerialNumber();
        if (sim.length() > 19) sim = sim.substring(0,19);
        results.append(sim);

        return results;

    }
    
    private String getBuildVersion(String fullVersion){
        String temp = fullVersion.substring(0, Build.DISPLAY.lastIndexOf("_"));
        return temp.substring(temp.indexOf(Build.MODEL));
    }
    
    private String getMcuVersion(){
        return SystemProperties.get("hw.build.version.mcu", "unknown");
    }
    
    private String getProductType(){
        String productType = "";
        int type = SystemProperties.getInt("hw.board.id", -1);
        switch (type){
            case 2:
            productType = "SC200-MINIMAL";
            break;
            
            case 3:
            productType = "SC200-MID";
            break;
            
            case 4:
            productType = "SC200-FULL-BAT";
            break;
            
            case 5:
            productType = "SC200-FULL-NOBAT";
            break;
            
            case 6:
            productType = "SC200-FULL-NOBAT-CANBUS";
            break;
        }
        return productType;
    }
    
    private String getDevType(){
        return getProductType();        
    }
    
    private int getCurrent() {
		int mCurrent = 0;
		try {
			mCurrent=Integer.valueOf(ServiceUtil.getInstance().readFromFile(CURRENT));
			mCurrent=Math.abs(mCurrent);
		} catch (Exception e) {
			mCurrent =0;
			e.printStackTrace();
		}
	
		return  mCurrent;
	}
	
	private int isSmallBattery() {
		String type = "";
		try {
			type=ServiceUtil.getInstance().readFromFile(BATTERY_TYPE);
		} catch (Exception e) {
			e.printStackTrace();
		}
        if(type.equals("c801_fullymax_fb382030xl_4v2_135mah_20k"))
            return  1;
		else 
            return 0;
	}
	
    private String resultToSha256 (String res){
        String noDelimiters = res.replaceAll(",", "");
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] encodedhash = digest.digest(noDelimiters.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encodedhash);
    }
    
    private static String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    private boolean isValidImei(String s){
        long n = Long.parseLong(s);
        int l = s.length();
        if(s == null || !s.matches("\\d+") || l!=15){ // If IMEI is null or length is not 15 then IMEI is Invalid
            return false;
        }else {
            int d = 0, sum = 0;
            for(int i=15; i>=1; i--){
                d = (int)(n%10);
                if(i%2 == 0){
                    d = 2*d; // Doubling every alternate digit
                }
                sum = sum + sumDig(d); // Finding sum of the digits
                n = n/10;
            }
             
            if(sum%10==0 && sum!=0){
                return true;
            } else 
                return false;
        }
    }
    
    int sumDig(int n){
        int a = 0;
        while(n>0){
            a = a + n%10;
            n = n/10;
        }
        return a;
    }
    
    private String removeWordFromData(String data, int index){
        String[] items = data.split("\\s*,\\s*");
        for (String i : items){
        }
        items[index] = "";
        
        for (String i : items){
        }
        return String.join(",", items);
    }
    
}
