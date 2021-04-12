
package com.lovdream.factorykit;

import com.swfp.utils.TestDataUtil;

import android.app.Application;

public class FactoryKitApplication extends Application{

	private Config mConfig;
	private TestItemFactory mTestItemFactory;

	@Override
	public void onCreate(){
			super.onCreate();
			
			Thread.setDefaultUncaughtExceptionHandler(CrashHandler.getInstance());
			
			mTestItemFactory = TestItemFactory.getInstance(this);
			mConfig = Config.getInstance(this);
			mConfig.loadConfig();
			
			//每次进来测试前,我们需要初始化map 数据;
			if (TestDataUtil.getTestDataUtil().isFirstBoot()) {
				TestDataUtil.getTestDataUtil().readDataFromNv(this);
			} else {
				TestDataUtil.getTestDataUtil().readDataFromFile();
			}
	}

	public TestItemFactory getTestItemFactory(){
		return mTestItemFactory;
	}

	public Config getTestConfig(){
		return mConfig;
	}
}
