
package com.lovdream.factorykit.items;

import com.lovdream.factorykit.R;

public class WiFi5GTest extends WiFiTest{

	@Override
	public String getKey(){
		return "wifi_5g_test";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.wifi_5g_test_title);
	}

	@Override
	protected String getTestSSID(){
		return "lovdream5G";
	}
	
	protected boolean  isFrequencyRight(int frequency){
		return  frequency>5000;
	}

}
