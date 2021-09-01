package com.swfp.utils;
import android.content.Context;
import android.os.IBinder;
import android.os.ServiceManager;

import com.lovdream.ILovdreamDevice;
import com.lovdream.LovdreamDeviceManager;
public class ServiceUtil {	
	private static  ServiceUtil util;
	
	private static ILovdreamDevice sService;
	
	private ServiceUtil (){}
	
	public static  ServiceUtil getInstance(){
		if(util==null) util = new ServiceUtil();
		return util;
	}
	
	   private  static ILovdreamDevice getService()
	    {
	        if (sService != null) {
	            return sService;
	        }
	        IBinder b = ServiceManager.getService(Context.LOVDREAMDEVICES_SERVICE);
	        sService = ILovdreamDevice.Stub.asInterface(b);
	        return sService;
	    }
	
	
	private  LovdreamDeviceManager getLdm(Context context){
		return LovdreamDeviceManager.getInstance(context);
	}
	
	
	public String readFromFile(String path){
		try {
			return getService().readToFile(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public void writeToFile(String path,String flag,Context context){
		try {
			getLdm(context).writeToFile(path, flag);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void setThreeLightColor(int color,Context context){
		try {
			getLdm(context).setThreeLightColor(color);
			//getLdm(context).writeToFile(String.valueOf(0),String.valueOf(color));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	  public void setButtonBackLight(boolean light,Context context){
		  try {
				getLdm(context).setButtonBackLight(light);
			  //getLdm(context).writeToFile(String.valueOf(1),String.valueOf(light));
			} catch (Exception e) {
				e.printStackTrace();
			}
	  }
	
	public void runProcess(String runProcess,Context context){
		try {
			getLdm(context).runProcess(runProcess);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void startPlayFm(Context context){
		try {
			getService().startPlayFm();
			//getService().readToFile("0");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void stopPlayFm(Context context){
		try {
			getService().stopPlayFm();
			//getService().readToFile("1");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
