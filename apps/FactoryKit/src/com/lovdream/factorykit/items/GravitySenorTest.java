
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
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.TestItemBase;
import com.swfp.utils.ProjectControlUtil;

public class GravitySenorTest extends TestItemBase{

	private int getDirection(int i, int j) {
		int i1;
		int k = Math.abs(i);
		int l = Math.abs(j);
		
		if (k > l) {
			if (i > 0)
				i1 = 2;
			else
				i1 = 4;
		} else {
			if (j > 0)
				i1 = 1;
			else
				i1 = 3;
		}
		
		if (m_rotation == 90) // goto _L2; else goto _L1
			i1 = (i1 + 1) % 4;
		else if (m_rotation == 270)
			i1 = (i1 + 3) % 4;

		if (i1 == 0)
			i1 = 4;
		
		return i1;

	}

	private void initAllControl(View v) {
		imageView[0] = (ImageView) v.findViewById(R.id.ms_arrow_down);
		imageView[1] = (ImageView) v.findViewById(R.id.ms_arrow_left);
		imageView[2] = (ImageView) v.findViewById(R.id.ms_arrow_up);
		imageView[3] = (ImageView) v.findViewById(R.id.ms_arrow_right);
		
		ms_tv_XYZ[0] = (TextView) v.findViewById(R.id.ms_tv_x);
		ms_tv_XYZ[1] = (TextView) v.findViewById(R.id.ms_tv_y);
		ms_tv_XYZ[2] = (TextView) v.findViewById(R.id.ms_tv_z);

		mCalibration = (Button) v.findViewById(R.id.calibration);
		String buildType = android.os.SystemProperties.get("ro.build.type");
		if (!"userdebug".equals(buildType)){
			mCalibration.setVisibility(View.GONE);
		}

		mCalibration.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						int errorCode = com.qualcomm.qti.sensors.core.sensortest.SensorUserCal.performUserCal((byte)0,(byte)0);
						Log.d(TAG,"errorCode = " + errorCode);
						if (errorCode == 0){
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(getActivity(),R.string.calibration_success,Toast.LENGTH_LONG).show();
								}
							});
						}else {
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(getActivity(),R.string.calibration_failed,Toast.LENGTH_LONG).show();
								}
							});
						}
					}
				}).start();
			}
		});

	}

	@Override
	public String getKey(){
		return "gsensor_test";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.gravity_test_mesg);
	}

	@Override
	public View getTestView(LayoutInflater inflater){
		imageView = new ImageView[4];
		ms_tv_XYZ = new TextView[3];
		View v = inflater.inflate(R.layout.gravity_sensor_test,null);
		initAllControl(v);

		if(mSensorManager != null){
			Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
			mSensorManager.registerListener(mListener,sensor,SensorManager.SENSOR_DELAY_NORMAL);
		}
		return v;
	}

	@Override
	public void onStartTest(){
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

	ImageView imageView[];
	int[] isTrue={0,0,0,0};
	private final SensorEventListener mListener = new SensListener();
	private SensorManager mSensorManager;
	private float mValues[];
	int m_nCurArrow;
	int m_rotation = 0;
	TextView ms_tv_XYZ[];
	Button bt_success;
	Button bt_fail;
	private Button mCalibration;
	private Handler mHandler = new Handler();
	
	boolean x = false;
	boolean y = false;
	boolean z = false;

	/*
	 * static float[] access$002(MotionSenorTest motionsenortest, float af[]) {
	 * motionsenortest.mValues = af; return af; }
	 */

	private class SensListener implements SensorEventListener {

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		public void onSensorChanged(SensorEvent event){
			/*if (x && y && z) {
				bt_success.setEnabled(true);
			}*/
			int j;
			int k;
			mValues = event.values;
			TextView textview = ms_tv_XYZ[0];
			StringBuilder stringbuilder = (new StringBuilder()).append("X = ");
			float f = mValues[0];
			if (f != -0.0) {
				x = true;
			}
			String s = stringbuilder.append(f).toString();
			textview.setText(s);
			TextView textview1 = ms_tv_XYZ[1];
			StringBuilder stringbuilder1 = (new StringBuilder()).append("Y = ");
			float f1 = mValues[1];
			if (f1 != -0.0) {
				y = true;
			}
			String s1 = stringbuilder1.append(f1).toString();
			textview1.setText(s1);
			TextView textview2 = ms_tv_XYZ[2];
			StringBuilder stringbuilder2 = (new StringBuilder()).append("Z = ");
			float f2 = mValues[2];
			if (f2 != -0.0) {
				z = true;
			}
			String s2 = stringbuilder2.append(f2).toString();
			textview2.setText(s2);
			j = (int) mValues[0];
			k = (int) mValues[1];

			int l = 1;
			switch (getDirection(j, k)) {
			case 1:
				l = 1;	
				break;
			case 2:
				l = 2;
				break;
			case 3:
				l = 3;
				break;
			case 4:
				l = 4;
				break;
			default:
				l = 1;
				break;
			}
			l--;

			if (l != m_nCurArrow) {
				imageView[m_nCurArrow].setVisibility(4);
				m_nCurArrow = l;
				imageView[l].setVisibility(0);
				if(isTrue[l]!=1){
					isTrue[l]=1;
				}
			} else {
				imageView[m_nCurArrow].setVisibility(0);
				if(isTrue[m_nCurArrow]!=1){
					isTrue[m_nCurArrow]=1;
				}
			}
            if (ProjectControlUtil.isGravitySenorHasTwoDirection()) {
                if (isTrue[0] == 1 && isTrue[1] == 1 && isTrue[2] == 1
                        && isTrue[3] == 1) {
                    postSuccess();
                }
            } else {
                if (isTrue[2] == 1 && isTrue[3] == 1) {
                    postSuccess();
                }
            }

			return;
		}

	}

}
