
/* copy from lovdream.cit*/
package com.lovdream.factorykit.items;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.KeyEvent;
import android.view.LayoutInflater;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.TestItemBase;

public class SARSenorTest extends TestItemBase{

	@Override
	public String getKey(){
		return "sarsensor_test";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.sarsensor_test_mesg);
	}

	@Override
	public View getTestView(LayoutInflater inflater){
		View v = inflater.inflate(R.layout.sar_sensor_test,null);
		mSarSensorText = (TextView)v.findViewById(R.id.sar_sensor_text);

		if(mSensorManager != null){
			Sensor sensor = mSensorManager.getDefaultSensor(33171015);
			mSensorManager.registerListener(mListener,sensor,SensorManager.SENSOR_DELAY_NORMAL);
		}
		return v;
	}

	@Override
	public void onStartTest(){
		bSuccess = false;
		for(int i = 0;i < isTrue.length;i++){
			isTrue[i] = 0;
		}
		mSensorManager = (SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
	}

	@Override
	public void onStopTest(){
		if(mSensorManager != null){
			mSensorManager.unregisterListener(mListener);
		}
	}

	private final SensorEventListener mListener = new SensListener();
	private SensorManager mSensorManager;
	private float mValues[];
	Button bt_success;
	Button bt_fail;
	TextView mSarSensorText;
	int[] isTrue={0,0};
	boolean bSuccess = false;

	/*
	 * static float[] access$002(MotionSenorTest motionsenortest, float af[]) {
	 * motionsenortest.mValues = af; return af; }
	 */

	private class SensListener implements SensorEventListener {

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		public void onSensorChanged(SensorEvent event){
			mValues = event.values;
			StringBuilder stringbuilder = (new StringBuilder()).append("X = ");
			float f = mValues[0];
			String s = stringbuilder.append(f).toString();
			mSarSensorText.setText(s);

			if(1 == f){
				if(1!=isTrue[1]){
					isTrue[1] = 1;
				}
			}else if(0 == f){
				if(1!=isTrue[0]){
					isTrue[0] = 1;
				}
			}
			// add by hudayu for bug 0017734
			if((isTrue[0]==1 || isTrue[1] == 1)&&!bSuccess){
				bSuccess = true;
				postSuccess();
			}
			return;
		}
	}
}
