

package com.lovdream.factorykit;

import android.content.Context;
import android.util.Log;
import dalvik.system.DexFile;

import java.util.HashMap;
import java.util.Enumeration;

public class TestItemFactory{

	private static final String TAG = Main.TAG;

	private final static String PACKAGE_NAME = "com.lovdream.factorykit.items";

	private static TestItemFactory mInstance;

	private HashMap<String,TestItemBase> mClasses = new HashMap<String,TestItemBase>();

	private void loadClasses(Context context){

		DexFile df = null;

		try{
			df = new DexFile(context.getPackageCodePath());
		}catch(Exception e){
			e.printStackTrace();
			Log.e(TAG,"can not create dex file");
			return;
		}

		Enumeration<String> enu = df.entries();
		while(enu.hasMoreElements()){
			String className = (String)enu.nextElement();
			if(className.contains(PACKAGE_NAME) &&!className.contains("$")){
				Log.i(TAG,"found class:" + className);
				String key = null;
				Object obj = null;

				try{
					obj = Class.forName(className).newInstance();
				}catch(Exception e){
					e.printStackTrace();
					continue;
				}

				if((obj == null) || !(obj instanceof TestItemBase)){
					Log.e(TAG,className + " can not create instance,or it's not TestItemBase subclass");
					continue;
				}
				
				key = ((TestItemBase)obj).getKey();

				if((key != null) && (!"".equals(key))){
					mClasses.put(key,(TestItemBase)obj);
				}
			}
		}
	}

	private TestItemFactory(){
	}

	private TestItemFactory(Context context){
		loadClasses(context);
	}

	public TestItemBase createTestItem(Context context,Config.TestItem testItem){
		TestItemBase item = mClasses.get(testItem.key);

		if(item != null){
			item.init(context,testItem.parameter,testItem.isAutoJudge,testItem.flagIndex,testItem.fm);
		}

		return item;
	}

	public static TestItemFactory getInstance(Context context){
		if(mInstance == null){
			mInstance = new TestItemFactory(context);
		}
		return mInstance;
	}
}
