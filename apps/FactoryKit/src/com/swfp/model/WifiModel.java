package com.swfp.model;

public class WifiModel {
	
	public WifiBack wifiBack;
	public String testSSID;
	public WifiModel(WifiBack mWifiBack ){
		this.wifiBack= mWifiBack;
	}
	
	
	
	
	
	public interface WifiBack{
		void pass();
		void failed(String msg);
		boolean isFrequencyNotErr(int fre);
		void showMsg(String msg);
		void toast(String msg);
	}
	
	
	

}
