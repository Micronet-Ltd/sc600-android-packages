
package com.lovdream.factorykit.items;

import android.view.View;
import android.content.Context;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.TestItemBase;
import com.swfp.utils.ServiceUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.os.SystemProperties;


public class PINTest extends TestItemBase{

        public static final byte[] LIGHTE_ON = { '1' };
        public static final byte[] LIGHTE_OFF = { '0' };
	@Override
	public String getKey(){
            return "sub_pin_test";
	}

	@Override
	public String getTestMessage(){
            return getActivity().getString(R.string.test_fourteenpin_prompt);
	}
    private Context mContext;
	private static final String[] BACK_CTRL_NODES = {
		"sys/class/ext_dev/function/power_en",
		"sys/class/ext_dev/function/pin10_en",
		"sys/class/ext_dev/function/pin11_en",
		"sys/class/ext_dev/function/uart_rx",
		"sys/class/ext_dev/function/uart_tx"
	};

	@Override
	public void onStartTest(){
                mContext = getActivity();
		for(String s : BACK_CTRL_NODES){
			ServiceUtil.getInstance().writeToFile(s, 1+"", mContext);
		}
            //setPinStatus(true);
	}

	@Override
	public void onStopTest(){
                    for(String s : BACK_CTRL_NODES){
                    	ServiceUtil.getInstance().writeToFile(s, 0+"", mContext);
			//writeValue(s,1);
		}
            //setPinStatus(false);
	}
	
	private void setPinStatus(boolean status) {
		
            String PATH_1 ="/sys/class/ext_dev/function/pin10_en";// "/sys/class/ext_dev/function/ext_dev_3v3_enable";
            String PATH_2 = "/sys/class/ext_dev/function/pin11_en";//"/sys/class/ext_dev/function/ext_dev_5v_enable";
            String PATH_3 = "/sys/class/ext_dev/function/power_en";
            String PATH_4 = "/sys/class/ext_dev/function/uart_rx";
            String PATH_5 = "/sys/class/ext_dev/function/uart_tx";


            changePinStatus(status, PATH_1);
            changePinStatus(status, PATH_2);
            changePinStatus(status, PATH_3);
            changePinStatus(status, PATH_4);
            changePinStatus(status, PATH_5);

    }
    private void changePinStatus(boolean status, String node) {
        try {
            byte[] ledData = status ? LIGHTE_ON : LIGHTE_OFF;
            FileOutputStream brightness = new FileOutputStream(node);
            brightness.write(ledData);
            brightness.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, e.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
