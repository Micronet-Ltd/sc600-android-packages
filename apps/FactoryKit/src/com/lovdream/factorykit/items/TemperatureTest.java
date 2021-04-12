package com.lovdream.factorykit.items;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lovdream.factorykit.TestItemBase;
import com.lovdream.factorykit.R;

/**
 * Created by yangzhiming on 2017/6/22.
 */

public class TemperatureTest extends TestItemBase {
    private final BatteryReceiver mBatteryReceiver = new BatteryReceiver();
    private TextView mTemperatureInfo;
    private Handler mHandler = new Handler();
    @Override
    public String getKey() {
        return "temperature_test";
    }

    @Override
    public String getTestMessage() {
        return getString(R.string.temperature_test_msg);
    }

    @Override
    public void onStartTest() {

    }

    @Override
    public void onStopTest() {

    }

    @Override
    public View getTestView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.temperature_test,null,false);
        mTemperatureInfo = (TextView) view.findViewById(R.id.temperature_info);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        getActivity().registerReceiver(mBatteryReceiver,filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mBatteryReceiver);
    }

    private class BatteryReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)){
                final int temperature = intent.getIntExtra("temperature", 0) / 10;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mTemperatureInfo.setText(getString(R.string.temperature,temperature));
                    }
                });
            }
        }
    }
}
