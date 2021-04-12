package com.lovdream.factorykit;

import com.swfp.utils.TestDataUtil;

import android.graphics.Color;

public class FlagModel {
	public String key;
	public int index;
	public int smallPcbFlag =-1;//10-19
	public int pcbaFlag =-1;//20-69
	public int testFlag =-1;//70-128
	public int usbIndex=-1;
	public int backFlag=-1;
	public String displayName;
	
	public FlagModel(String key,int index, int smallPcbFlag,int pcbaFlag,int testFlag){
		this.index=index;
		this.key = key;
		this.smallPcbFlag = smallPcbFlag;
		this.pcbaFlag = pcbaFlag;
		this.testFlag = testFlag;
	}
	
	public String getTestModel(){
		if(Utils2.getInstance().isPcbaData()) return TestDataUtil.PCBA;
		if(Utils2.getInstance().isSmall()) return TestDataUtil.SMALLP;
		if(Utils2.getInstance().isSingle()) return TestDataUtil.SINGLE;
		if(Utils2.getInstance().isBack()) return TestDataUtil.BACK;
		return null;
	}
	
	public String getResult(Config mConfig,int mode){
		int flag = -2;
		switch (mode) {
		case Config.DATA_PCBA:
			flag =  mConfig.getPCBAFlag(pcbaFlag);
			break;
		case Config.DATA_SIGNLE:
			flag =  mConfig.getSmallPCBFlag(smallPcbFlag);
			break;
		case Config.DATA_SMALL:
			flag =  mConfig.getTestFlag(testFlag);
		break;
		
		case Config.DATA_BACK:
			flag =  mConfig.getBackFlag(backFlag);
		break;

		default:
			break;
		}
		return getResult(flag);
	}
	
	private String getResult(int flag){
		if (flag == Config.TEST_FLAG_PASS) {
			return TestDataUtil.PASS;
         }else if (flag == Config.TEST_FLAG_FAIL) {
        	 return TestDataUtil.FAILURE;
         }else{
        	 return TestDataUtil.NOTEST;
         }		
	}
}
