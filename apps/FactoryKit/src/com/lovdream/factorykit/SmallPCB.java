
package com.lovdream.factorykit;

import com.lovdream.factorykit.Config.TestItem;

public class SmallPCB extends SingleTest{

	@Override
	protected boolean itemFilter(TestItem item){
		return item.inSmallPCB;
	}

	@Override
	protected int getTitleResId(){
		return R.string.small_pcb;
	}

	@Override
	public void beforeTest(TestItemBase item){
		item.setSmallPCBTest(true);
	}

	@Override
	protected int getFlag(FlagModel fm){
		return mConfig.getSmallPCBFlag(fm.smallPcbFlag);
	}

	@Override
	public void clearFlag(){
		mConfig.clearSmallPCBFlag();
	}

	@Override
	public void onTestFinish(TestItemBase item){
		super.onTestFinish(item);
		item.setSmallPCBTest(false);
	}
}
