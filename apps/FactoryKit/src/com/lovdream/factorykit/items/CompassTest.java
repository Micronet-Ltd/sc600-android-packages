
package com.lovdream.factorykit.items;

import android.view.View;
import android.view.LayoutInflater;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.libs.CompassView;
import com.lovdream.factorykit.TestItemBase;

public class CompassTest extends TestItemBase implements CompassView.SensorChangeListener{

	@Override
	public String getKey(){
		return "compass";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.compass_test_mesg);
	}

	@Override
	public void onStartTest(){
	}

	@Override
	public void onStopTest(){
	}

	@Override
	public View getTestView(LayoutInflater inflater){
		View v = inflater.inflate(R.layout.compass_test,null);
		CompassView compass = (CompassView)v.findViewById(R.id.compass_view);
		compass.setSensorChangeListener(this);
		return v;
	}

	@Override
	public void updateState(boolean state){
		if(state){
			postSuccess();
		}
	}
}
