
package com.lovdream.factorykit.items;

import com.lovdream.factorykit.R;

public class SubMicTest extends HandsetTest{

	private static final String SUB_MIC_SWITCH = "/sys/class/fm2018/function/fm2018_sub_mic_switch";

	public String getKey(){
		return "sub_mic";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.sub_mic_test_mesg);
	}

	@Override
	public String getMicSwitchFile(){
		return SUB_MIC_SWITCH;
	}
}
