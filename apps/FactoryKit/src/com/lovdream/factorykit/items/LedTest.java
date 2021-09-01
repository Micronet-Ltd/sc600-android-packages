package com.lovdream.factorykit.items;

import android.os.Handler;
import android.util.Log;
import android.graphics.Color;

import com.lovdream.factorykit.TestItemBase;
import com.lovdream.factorykit.R;
import com.swfp.utils.ServiceUtil;

import java.io.File;
import java.io.FileOutputStream;

import android.content.Context;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;

public class LedTest extends TestItemBase {

	private Handler mHandler = new Handler();
	private boolean mIsInTest;
	private Context mContext;
	Thread t;

	private int[] colors = { 0xffff0000, 0xff00ff00, 0xff0000ff };

	@Override
	public String getKey() {
		return "led_test";
	}

	@Override
	public String getTestMessage() {
		String[] msg = getParameter("msg");
		if ((msg != null) && (msg[0] != null)) {
			return msg[0];
		}
		return getString(R.string.two_color_led_test_mesg);
	}

	@Override
	public void onStartTest() {
		mContext = getActivity();
		mIsInTest = true;
		t = new Thread(mRunnable, "t1");
		t.start();
	}

	@Override
	public void onStopTest() {
        t.interrupt();
		mIsInTest = false;
		setColor(Color.BLACK);
		
	}

	private Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			while (mIsInTest) {
				for (int color : colors) {
				
                    if(mIsInTest){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setColor(color);
                        }
                    });
                    }
                    try {
                        Thread.sleep(1000);
                        } catch (Exception e) {
                        }
				}
			}
		}
	};

	private void setColor(int color) {
        LightsManager lm = new LightsManager(mContext);
        Light greenBlueLight = lm.getLight(LightsManager.LIGHT_ID_NOTIFICATIONS);
        Light redLight = lm.getLight(LightsManager.LIGHT_ID_BATTERY);
        if (color == Color.RED){
            greenBlueLight.setColor(0x00000000);
            redLight.setColor(color);
        } else {
            redLight.setColor(0x00000000);
            greenBlueLight.setColor(color);
        }

	}

}
