package com.lovdream.factorykit.items;

import android.os.Handler;
import android.util.Log;
import android.graphics.Color;

import com.lovdream.factorykit.TestItemBase;
import com.lovdream.factorykit.R;
import com.swfp.utils.ServiceUtil;

import java.io.File;
import java.io.FileReader;

import android.content.Context;
import android.os.SystemClock;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;

public class LedATest extends TestItemBase {

	private Handler mHandler = new Handler();
	private boolean mIsInTest;
	private Context mContext;
	Thread t;

	@Override
	public String getKey() {
		return "led_a_test";
	}

	@Override
	public String getTestMessage() {
		return getString(R.string.led_a_test_mesg);
	}

	@Override
	public void onStartTest() {
		mContext = getActivity();
		setColor(Color.WHITE);
		mIsInTest = true;
		t = new Thread(mRunnable, "t1");
		t.start();
	}

	@Override
	public void onStopTest() {
        t.interrupt();
		mIsInTest = false;
		setColor(0x30000000);
		
	}

	private Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			File flagFile = new File("/sdcard/Download/led.flg");
			while (mIsInTest) {
				SystemClock.sleep(1000L);
				if (flagFile.exists()){
					try (FileReader fileReader = new FileReader(flagFile)) {
						if (fileReader.read()=='1' && mIsInTest){
							flagFile.delete();
							getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								postSuccess();
							}});
							break;
						}
					} catch (Exception ignore) {}
				}
			}
		}
	};

	private void setColor(int color) {
         LightsManager lm = new LightsManager(mContext);
         Light redGreenBlueLight1 = lm.getLight(LightsManager.LIGHT_ID_NOTIFICATIONS);
         Light redLight0 = lm.getLight(LightsManager.LIGHT_ID_BATTERY);
         Light irLed = lm.getLight(LightsManager.LIGHT_ID_BACKLIGHT);
         if (color == Color.WHITE){
             redGreenBlueLight1.setColor(0x30000101);
             redLight0.setColor(0xffff0000);
             irLed.setColor(0xffffffff);
         } else {
             redLight0.setColor(0x00000000);
             redGreenBlueLight1.setColor(0x30000000);
             irLed.setColor(0x00000000);
         }

	}

}
