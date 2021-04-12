
package com.lovdream.factorykit.items;

import com.lovdream.factorykit.R;

public class MediaMicTest extends HandsetTest{

	private static final String MEDIA_MIC_SWITCH = "/sys/class/fm2018/function/fm2018_media_mic_switch";

	public String getKey(){
		return "media_mic";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.media_mic_test_mesg);
	}

	@Override
	public String getMicSwitchFile(){
		return MEDIA_MIC_SWITCH;
	}
}
