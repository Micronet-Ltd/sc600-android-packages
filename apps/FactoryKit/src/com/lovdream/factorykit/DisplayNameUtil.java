package com.lovdream.factorykit;

import android.content.Context;
import com.lovdream.factorykit.R;
import com.swfp.utils.ProjectControlUtil;

public class DisplayNameUtil {
	
	private DisplayNameUtil(){}
	private static DisplayNameUtil mDisplayNameUtil;
	public static DisplayNameUtil getInstance(){
		if(mDisplayNameUtil==null) mDisplayNameUtil = new DisplayNameUtil();
		return mDisplayNameUtil;
	}
	
	public String getNewKeyName(String key,Context context){
		if("led_test".equals(key)){
			if(ProjectControlUtil.isC801){
				return context.getResources().getString(R.string.led_test3);
			}else{
				return null;
			}
		}
		return null;
	}
}
