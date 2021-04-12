package com.lovdream.factorykit;

import java.util.ArrayList;

import android.app.Activity;
import android.util.Log;

import com.lovdream.factorykit.Config.TestItem;

public class Utils2 {
	
	private static Utils2 instance;
	private Utils2(){}
	public static Utils2 getInstance(){
		if(instance==null){
			instance = new Utils2();
		}
		return instance;
	}
	
	
	public int findIndex(String key,boolean isInPcba,Activity activity) {
		   FactoryKitApplication app = (FactoryKitApplication)activity
	                .getApplication();
	        Config config = app.getTestConfig();
		   ArrayList<TestItem> pcbaItems = config.getTestItems();
	        for (TestItem item : pcbaItems) {
	            if (item.key.equals(key)) {
	                if(isInPcba) return item.fm.pcbaFlag;
	                else return item.fm.testFlag;
	            }
	        }
	        return -1;
	    }
	
	public  static final int PCBA_1 = 1;
	public  static final int PCBA_2 = 2;
	public  static final int SINGLE = 3;
	public  static final int SMALLPCB = 4;
	public  static final int STRESS = 5;
	public  static final int RESULT = 6;
	public  static final int AUTO= 7;
	public  static final int SPCBA = 8;
	public  static final int MAIN_UI = 0;

	public  int currentTestMode = MAIN_UI;
	
	public boolean isPcba(){
		return currentTestMode==PCBA_1 || currentTestMode==PCBA_2 || currentTestMode==SPCBA;
	}
	
	
	public boolean isPcbaData(){
		return currentTestMode==PCBA_1  || currentTestMode==SPCBA;
	}
	
	public boolean isSingle(){
		return currentTestMode==SINGLE;
	}
	
	public boolean isBack(){
		return currentTestMode==PCBA_2;
	}
	public boolean isSmall(){
		return currentTestMode==SMALLPCB;
	}
	
	public boolean isAuto(){
		return currentTestMode==AUTO;
	}

}
