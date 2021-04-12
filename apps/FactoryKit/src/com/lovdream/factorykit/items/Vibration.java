
package com.lovdream.factorykit.items;

import android.view.View;
import android.os.Vibrator;
import android.content.Context;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.TestItemBase;

public class Vibration extends TestItemBase{

	Vibrator mVibrator;

	private final long VIBRATOR_ON_TIME = 1000;
	private final long VIBRATOR_OFF_TIME = 500;
	long[] pattern = { VIBRATOR_OFF_TIME, VIBRATOR_ON_TIME };

	@Override
	public String getKey(){
		return "vibrator_test";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.vibrator_test_mesg);
	}

	@Override
	public void onStartTest(){
		mVibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
		mVibrator.vibrate(pattern, 0);
	}

	@Override
	public void onStopTest(){
		if(mVibrator != null){
			mVibrator.cancel();
		}
	}

}
