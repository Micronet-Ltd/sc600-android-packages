package com.lovdream.factorykit.items;

import android.view.View;
import com.lovdream.factorykit.R;
import com.lovdream.factorykit.Utils;
import com.lovdream.factorykit.TestItemBase;

public class CameraIrcutTest extends CameraBack{


	@Override
	public String getKey(){
		return "camera_ircut_test";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.camera_ircut_test_mesg);
	}

	@Override
	public int getCameraId(){
		return 2;
	}

	@Override
	public void onStartTest(){
		super.onStartTest();
	}

	@Override
	public void onStopTest(){
		super.onStopTest();
	}
	@Override
	public boolean isFlashModeOn(){
		return false;
	}

	@Override
	public void onClick(View v){

		super.onClick(v);
	}

}
