
package com.lovdream.factorykit.items;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.Utils;
import com.lovdream.factorykit.TestItemBase;

public class IRCutTest extends CameraBack{

	private static final String CONTRL_PATH = "/sys/class/leds/ir_cut/brightness";

	@Override
	public String getKey(){
		return "ircut_test";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.ircut_test_mesg);
	}

	@Override
	public int getCameraId(){
		return 2;
	}

	@Override
	public void onStartTest(){
		super.onStartTest();

		if(!Utils.writeFile(CONTRL_PATH,"255")){
			toast(R.string.ircut_test_error);
			postFail();
			return;
		}
	}

	@Override
	public void onStopTest(){
		super.onStopTest();
		Utils.writeFile(CONTRL_PATH,"0");
	}
}
