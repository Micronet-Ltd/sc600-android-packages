package com.lovdream.factorykit.items;

import android.util.Log;

import com.lovdream.factorykit.TestItemBase;
import com.lovdream.factorykit.Utils;
import com.lovdream.factorykit.R;

public class LaserTest extends TestItemBase{

	private static final String CONTRL_PATH = "/sys/class/leds/laser/brightness";

	@Override
	public String getKey(){
		return "laser_test";
	}

	@Override
	public String getTestMessage(){
        return getString(R.string.laser_test_mesg);
	}

	@Override
	public void onStartTest(){
		if(!Utils.writeFile(CONTRL_PATH,"255")){
			toast(R.string.laser_test_error);
			postFail();
			return;
		}
	}

	@Override
	public void onStopTest(){
		Utils.writeFile(CONTRL_PATH,"0");
	}
}
