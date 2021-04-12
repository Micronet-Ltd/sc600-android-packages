package com.lovdream.factorykit.items;

import android.content.Context;
import android.os.Handler;
import android.hardware.camera2.CameraManager;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.TestItemBase;

/**
 * Created by yangzhiming on 2017/6/16.
 */

public class FlashLedTest extends TestItemBase implements Runnable{

	private final int BLINK_INTERVAL = 800;
	private Handler mHandler = new Handler();
	private boolean lightEnabled = false;

	protected String getCameraId(){
		return "0";
	}

    @Override
    public String getKey() {
        return "flash_light";
    }

    @Override
    public String getTestMessage() {
        return getString(R.string.flash_led_msg);
    }

    @Override
    public void onStartTest() {
		mHandler.postDelayed(this,BLINK_INTERVAL);
    }

    @Override
    public void onStopTest() {
		mHandler.removeCallbacks(this);
		enableFlash(false);
    }

	@Override
	public void run(){
		lightEnabled = !lightEnabled;
		enableFlash(lightEnabled);
		mHandler.postDelayed(this,BLINK_INTERVAL);
	}

    private void enableFlash(boolean isEnable){
		CameraManager cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
		try{
			cameraManager.setTorchMode(getCameraId(),isEnable);
		}catch(Exception e){
			e.printStackTrace();
		}
    }
}
