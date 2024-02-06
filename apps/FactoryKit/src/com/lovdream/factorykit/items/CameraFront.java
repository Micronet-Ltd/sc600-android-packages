
package com.lovdream.factorykit.items;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;

public class CameraFront extends CameraBack{

	@Override
	public String getKey(){
		return "camera_test_front";
	}

	@Override
	public int getCameraId(){
		return Camera.CameraInfo.CAMERA_FACING_FRONT;
	}

	@Override
	public int getRotation(){
		/*int rotation = 270;
		String arg[] = getParameter("rotation");
		if(arg != null){
			try{
				rotation = Integer.valueOf(arg[0]);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		int screenRotation  = getActivity().getWindowManager().getDefaultDisplay()
				.getRotation()*90;
		CameraInfo cameraInfo = new CameraInfo();
		Camera.getCameraInfo(getCameraId(),cameraInfo);
		int rotation = (cameraInfo.orientation + screenRotation) %360;
		rotation = (360 - rotation) % 360;// compensate the mirror*/
		return 90;
	}

	@Override
	public boolean isFlashModeOn(){
		return false;
	}
	
	@Override
	public int getWidth(){
	    return 1280;
	}
    
	@Override
	public int getHeight(){
	    return 720;
	}
}
