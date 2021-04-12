
package com.lovdream.factorykit.items;

import java.io.File;
import java.io.FileOutputStream;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.TestItemBase;

public class BackClipOtg extends TestItemBase{

	private static final String PATH = "/sys/class/ext_dev/function/sideswitch";

	@Override
	public String getKey(){
		return "back_clip_otg";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.back_clip_otg_mesg);
	}

	@Override
	public void onStartTest(){
		setBackClipOtgStatus(1);
	}

	@Override
	public void onStopTest(){
		setBackClipOtgStatus(0);
	}
	
	private void setBackClipOtgStatus(int status){
		File file = new File(PATH);
		if(!file.exists()){
			return;
		}
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);
			fos.write(String.valueOf(status).getBytes());
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
