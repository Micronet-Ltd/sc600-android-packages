
package com.lovdream.factorykit.items;

import android.view.KeyEvent;
import android.content.Context;
import android.hardware.input.InputManager;

import com.lovdream.factorykit.R;

public class HeadsetKeyTest extends KeyTest{

	private static final String HEADSET_KEY_DEV_NAME = "msm8953-sku3-tasha-snd-card Button Jack";

	InputManager mIm;

	@Override
	public String getKey(){
		return "headset_key_test";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.headset_key_test_mesg);
	}

	@Override
	public void onStartTest(){
		mIm = (InputManager)getActivity().getSystemService(Context.INPUT_SERVICE);
		super.onStartTest();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		if(mIm == null){
			return true;
		}

		if(!HEADSET_KEY_DEV_NAME.equals(mIm.getInputDevice(event.getDeviceId()).getName())){
			return true;
		}
		
		return super.onKeyUp(keyCode,event);
	}
}
