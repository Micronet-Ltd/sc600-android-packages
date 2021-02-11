package com.lovdream.factorykit.items;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.lovdream.factorykit.TestItemBase;
import com.lovdream.factorykit.R;

/**
 * Created by yangzhiming on 2017/6/16.
 */

public class WakeUpTest extends TestItemBase{

    @Override
    public String getKey() {
        return "wake_up_test";
    }

    @Override
    public String getTestMessage() {
        return getString(R.string.sleep_wake_msg);
    }

    @Override
    public void onStartTest() {
        getActivity().registerReceiver(mPowerEventListener,mIntentFilter);
    }

    @Override
    public void onStopTest() {
		try{
			getActivity().unregisterReceiver(mPowerEventListener);
		}catch(Exception e){
			e.printStackTrace();
		}
    }

    private final BroadcastReceiver mPowerEventListener = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
				enableSuccess(true);
            }
        }
    };

    private final IntentFilter mIntentFilter =  new IntentFilter(Intent.ACTION_SCREEN_ON);

}
