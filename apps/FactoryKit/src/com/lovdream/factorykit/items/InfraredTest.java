package com.lovdream.factorykit.items;

import android.util.Log;

import com.lovdream.factorykit.TestItemBase;
import com.lovdream.factorykit.Utils;
import com.lovdream.factorykit.R;

public class InfraredTest extends TestItemBase{

	private static final String CONTRL_PATH = "/sys/class/leds/torch-light0/brightness";

	@Override
	public String getKey(){
		return "infrared_test";
	}

	@Override
	public String getTestMessage(){
        return getString(R.string.infrared_test_mesg);
	}

	@Override
	public void onStartTest(){
		if(!Utils.writeFile(CONTRL_PATH,"255")){
			toast(R.string.infrared_test_error);
			postFail();
			return;
		}
	}

	@Override
	public void onStopTest(){
		Utils.writeFile(CONTRL_PATH,"0");
	}
}
