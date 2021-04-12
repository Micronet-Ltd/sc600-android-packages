package com.lovdream.factorykit.items;

import android.os.Handler;
import android.view.View;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.content.Context;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.os.SystemProperties;
import android.util.Log;

import android.telephony.TelephonyManager;

import java.io.FileOutputStream;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.TestItemBase;
import com.swfp.utils.ProjectControlUtil;
import com.swfp.utils.ServiceUtil;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;

public class BackLedTest extends TestItemBase{


    private Context mContext;
	private boolean mIsInTest;
	Thread t;
    private int maxIrLedColor = 0xFFFFFFFF;
    private int minIrLedColor = 0x80808080;
    private int[] colors = { maxIrLedColor, minIrLedColor};

	@Override
	public String getKey(){
		return "back_led";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.back_led_mesg);
	}

	@Override
	public void onStartTest(){
        mContext = getActivity();
		mIsInTest = true;
		t = new Thread(mRunnable, "t1");
		t.start();
	}

	@Override
	public void onStopTest(){
        t.interrupt();
		mIsInTest = false;
		if(SystemProperties.getInt("hw.board.id", 0) >= 2){
            setColor(0);
		} else {
            setColor(0xFFFFFFFF);
		}
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
        Light irLed = lm.getLight(LightsManager.LIGHT_ID_BACKLIGHT);
        irLed.setColor(color);
	}
}
