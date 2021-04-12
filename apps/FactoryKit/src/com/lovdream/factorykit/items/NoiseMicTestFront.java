
package com.lovdream.factorykit.items;

import com.lovdream.factorykit.R;

public class NoiseMicTestFront extends NoiseMicTest{

	@Override
	public String getKey(){
		return "noise_mic_front";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.noise_mic_front_test_mesg);
	}
}
