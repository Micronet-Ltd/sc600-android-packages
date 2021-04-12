
package com.lovdream.factorykit.items;

import android.view.View;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.os.SystemProperties;
import android.util.Log;
import android.telephony.TelephonyManager;
import com.swfp.utils.SimUtil;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.TestItemBase;
import com.lovdream.factorykit.Utils;
import java.io.IOException;

public class CellularDataTest extends TestItemBase{

	private TextView mTv;
	private boolean isDataTransferred = false;
	private boolean mIsInTest = false;
	TextView tv;
	private WifiManager mWifiManager;
	private int counter = 0;


	@Override
	public String getKey(){
		return "cellular_data_test";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.cellular_data_test_mesg);
	}

	@Override
	public void onStartTest(){
        mIsInTest = true;
        mWifiManager = (WifiManager) getActivity()
				.getSystemService(Context.WIFI_SERVICE);
        Utils.enableWifi(getActivity(), false);
        Thread t = new Thread(mRunnable, "t1");
		t.start();
	}

	@Override
	public void onStopTest(){
        Utils.enableWifi(getActivity(), true);
	}
	
	public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 google.com");
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(ipProcess.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(ipProcess.getErrorStream()));
            // read the output from the command
            String s = null;
            while ((s = stdInput.readLine()) != null) {
            Log.d("Cellular Data Test: Ping output: ", s);
            }
            //read any errors from the attempted command
            while ((s = stdError.readLine()) != null) {
                 Log.d("Cellular Data Test: Ping error: ", s);
            }
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        }
        catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }
	
	@Override
	public View getTestView(LayoutInflater inflater){
		View v = inflater.inflate(R.layout.test_mesg_view,null);
		tv = (TextView)v.findViewById(R.id.test_mesg_view);
		tv.setText("Sending Cellular data ... ");
		return v;
	}
	
	private Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
            while(mIsInTest){
                
                if(mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED){
                    mIsInTest = false;
                    while(counter < 3 && isDataTransferred == false){
                        counter++;
                        isDataTransferred = isOnline();
                    }
                    
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            
                            if(isDataTransferred){
                                postSuccess();
                            }else{
                                postFail();
                            }
                            tv.setText("Cellular data transferred: " + isDataTransferred);
                            enableSuccess(isDataTransferred);
                        }
                    });
                  
                }
           } 
		}
	};

}