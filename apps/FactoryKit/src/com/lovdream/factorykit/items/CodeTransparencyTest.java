
package com.lovdream.factorykit.items;

import android.os.SystemProperties;

public class CodeTransparencyTest extends GPSTest{

	@Override
	public String getKey(){
		return "nmea_test";
	}

	@Override
	public void onStartTest(){
		SystemProperties.set("persist.sys.output.nmea","1");
		super.onStartTest();
	}

	@Override
	public void onStopTest(){
		SystemProperties.set("persist.sys.output.nmea","0");
		super.onStopTest();
	}
}
