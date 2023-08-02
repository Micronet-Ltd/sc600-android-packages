
package com.lovdream.factorykit;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Xml;
import android.util.Log;

import com.lovdream.util.SystemUtil;
import com.swfp.utils.TestDataUtil;

import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class Config{

	private static final String TAG = Main.TAG;
	private static final String CONFIG_PATH = "/system/etc/cit.xml";
	
	
	public static final int DATA_PCBA = 0;
	public static final int DATA_SIGNLE = 1;
	public static final int DATA_SMALL = 2;
	public static final int DATA_BACK = 3;

	
	
	public static final int SMALL_PCB_FLAG_START = 10;
	public static final int SMALL_PCB_FLAG_END = 19;
	private static int smallPcbaIndex = SMALL_PCB_FLAG_START;
	
	private static final int USB_FLAG_START = 20;
	private static final int USB_FLAG_END = 23;
	private static int usbIndex = USB_FLAG_START;
	
	private static final int PCBA_FLAG_START = 24;
	private static final int PCBA_FLAG_END = 69;
	private static int pcbaIndex = PCBA_FLAG_START;
	
	private static final int SMALL_BACK_FLAG_START = 70;
	private static final int SMALL_BACK_FLAG_END = 75;
	private static int backIndex = SMALL_BACK_FLAG_START;

	public static final int TEST_FLAG_START = 76;
	public static final int TEST_FLAG_MAX = 128;
	private static int testFlagIndex = TEST_FLAG_START;

	public static final int TEST_FLAG_PASS = 1;
	public static final int TEST_FLAG_FAIL = 0;
	public static final int TEST_FLAG_NO_TEST = 2;

	private byte[] mTestFlag;

	private ArrayList<TestItem> allItems = new ArrayList<TestItem>();

	public static class TestItem{
		public String key;
		public boolean isAutoJudge;
		public String displayName;
		public String parameter;
		public int flagIndex;
		public int resultIndex;
		public FlagModel fm;
		public boolean inAutoTest;
		public boolean inPCBATest;
		public boolean inSPCBATest;
		public boolean inBackTest;
		public boolean inSmallPCB;
        public boolean inUSBTest;
        public boolean visibility= false;

		public TestItem(Context context,AttributeSet attr){

			key = attr.getAttributeValue(null,"key");
			isAutoJudge = attr.getAttributeBooleanValue(null,"isAutoJudge",false);
			displayName = attr.getAttributeValue(null,"displayName");
			parameter = attr.getAttributeValue(null,"parameter");
			inAutoTest = attr.getAttributeBooleanValue(null,"inAutoTest",false);
			inPCBATest = attr.getAttributeBooleanValue(null,"inPCBATest",false);
			inSPCBATest = attr.getAttributeBooleanValue(null,"inSPCBATest",false);
			inBackTest = attr.getAttributeBooleanValue(null,"inBackTest",false);
			inSmallPCB = attr.getAttributeBooleanValue(null,"inSmallPCB",false);
            inUSBTest = attr.getAttributeBooleanValue(null,"inUSBTest",false);
			flagIndex = FlagIndex.getIndex(key);
			resultIndex = attr.getAttributeIntValue(null, "resultIndex", Integer.MAX_VALUE);
			visibility = attr.getAttributeBooleanValue(null,"visibility",true);
			//add by xxf;涉及到名字需要改变的都在这里做
			String newKeyName = DisplayNameUtil.getInstance().getNewKeyName(key,context);
			if(newKeyName==null){
				int resId = context.getResources().getIdentifier(key,"string",context.getPackageName());
				if (resId != 0){
					String name = context.getResources().getString(resId);
					if (!TextUtils.isEmpty(name)){
						displayName = name;
					}
				}
			}else{
				displayName = newKeyName;
			}
			//add by xxf;涉及到名字需要改变的都在这里做
			
			//add by xxf
			fm = FlagIndex.getFlagModel(key);
			fm.displayName = key;
			if(fm==null){
				fm =  new FlagModel(key,flagIndex,FlagIndex.DEFAULT_INDEX,FlagIndex.DEFAULT_INDEX,FlagIndex.DEFAULT_INDEX);
				fm.usbIndex = FlagIndex.DEFAULT_INDEX;
				fm.backFlag=FlagIndex.DEFAULT_INDEX;
			}
			if(inSmallPCB){
				fm.smallPcbFlag = smallPcbaIndex++;
			}
			if(inPCBATest){
				fm.pcbaFlag = pcbaIndex++;
			}
			if(inBackTest){
				fm.backFlag = backIndex++;
			}
			if(inUSBTest){
				fm.usbIndex = usbIndex++;
			}
			fm.testFlag = testFlagIndex++;
			//add by xxf
			StringBuilder sb = new StringBuilder();
			sb.append("key="+fm.key+"  ");
			sb.append("index=" + fm.index+"  ");
			sb.append("pcbaFlag=" + fm.pcbaFlag+"  ");
			sb.append("smallPcbFlag=" + fm.smallPcbFlag+"  ");
			sb.append("testFlag=" + fm.testFlag+"  ");
			sb.append("inUSBTest=" + fm.usbIndex+"  ");
			Log.d(Main.TAG, sb.toString());
			
			
		}

		public TestItem(String key,String displayName,boolean isPcba){
			FlagModel fmTemp = new FlagModel(key, -1, -1, -1, -1);
			if(isPcba){
				fmTemp.pcbaFlag=pcbaIndex++;
			}
			fm = fmTemp;
			this.key = key;
			this.displayName = displayName;
		}

		@Override
		public String toString(){
			return "key:" + key + " isAutoJudge:" + isAutoJudge + " displayName:" + displayName + " parameter:" + parameter + " flagIndex:" + flagIndex;
		}
	}

	private Context mContext;

	private static Config mInstance;

	private Config(){
	} 

	private Config(Context context){
		mContext = context;

		mTestFlag = NvUtil.getNvFactoryData3IByte();
	}

	public static Config getInstance(Context context){
		if(mInstance == null){
			mInstance = new Config(context);
		}
		return mInstance;
	}

	public ArrayList<TestItem> getTestItems(){
		return allItems;
	}

	public void setAutoTestFt(boolean state){
		mTestFlag[Utils.FLAG_INDEX_AUTO] = (byte)(state ? 'P' : 'F');
		SystemUtil.setNvFactoryData3IByte(mTestFlag);
	}

	public void setPCBAFt(boolean state){
		mTestFlag[Utils.FLAG_INDEX_PCBA] = (byte)(state ? 'P' : 'F');
		SystemUtil.setNvFactoryData3IByte(mTestFlag);
	}
	public void setUSBFt(boolean state){
		mTestFlag[Utils.FLAG_INDEX_USB] = (byte)(state ? 'P' : 'F');
		SystemUtil.setNvFactoryData3IByte(mTestFlag);
	}

	public int get4GftStatus(){

		byte flag = mTestFlag[Utils.FLAG_INDEX_4G_FT];

		if('P' == flag){
			return TEST_FLAG_PASS;
		}else if('F' == flag){
			return TEST_FLAG_FAIL;
		}else{
			return TEST_FLAG_NO_TEST;
		}
	}

	public void clearTestFlag(){
		for(int i = TEST_FLAG_START;i < TEST_FLAG_MAX;i++){
			mTestFlag[i] = 0;
		}
		SystemUtil.setNvFactoryData3IByte(mTestFlag);
		TestDataUtil.getTestDataUtil().readSingleFromNv(allItems, mContext,true);
	}
	public void clearSmallPCBFlag(){
		for(int i = SMALL_PCB_FLAG_START;i < SMALL_PCB_FLAG_END;i++){
			mTestFlag[i] = 0;
		}
		SystemUtil.setNvFactoryData3IByte(mTestFlag);
		TestDataUtil.getTestDataUtil().readSmallFromNv(allItems, mContext,true);
	}
	public void clearPCBAFlag(){
		for(int i = PCBA_FLAG_START;i < PCBA_FLAG_END;i++){
			mTestFlag[i] = 0;
		}
		SystemUtil.setNvFactoryData3IByte(mTestFlag);
		TestDataUtil.getTestDataUtil().readPcbaFromNv(allItems, mContext,true);
	}
	public void clearPCBAFlag(ArrayList<TestItem> allItems){
		for (int i = 0; i < allItems.size(); i++) {
			TestItem item = allItems.get(i);
			if(item==null) continue;
			mTestFlag[item.fm.pcbaFlag]=0;
		}
		SystemUtil.setNvFactoryData3IByte(mTestFlag);
		TestDataUtil.getTestDataUtil().readPcbaFromNv(allItems, mContext,true);
	}
	
	public void clearBackFlag(ArrayList<TestItem> backItems){
	for (int i = 0; i < allItems.size(); i++) {
			TestItem item = allItems.get(i);
			if(item==null) continue;
			if(item.fm.backFlag!=-1)mTestFlag[item.fm.backFlag]=0;
		}
	TestDataUtil.getTestDataUtil().readBacklFromNv(allItems, mContext,true);
	}
	
	public void clearUSBFlag(){
		for(int i = USB_FLAG_START;i <=USB_FLAG_END;i++){
			mTestFlag[i] = 0;
		}
		SystemUtil.setNvFactoryData3IByte(mTestFlag);
	}

	
	public void saveUSBFlag(FlagModel fm,boolean state){
		int realIndex = fm.usbIndex;
		if((realIndex < USB_FLAG_START) || (realIndex > USB_FLAG_END)){
			Log.e(TAG,"saveUSBFlag,invalid index:" + fm.index,new RuntimeException());
			return;
		}
		saveTestFlagInner(realIndex,state);
	}
	
	public void saveSmallPCBFlag(FlagModel fm,boolean state){
		int realIndex = fm.smallPcbFlag;
		if((realIndex < SMALL_PCB_FLAG_START) || (realIndex > SMALL_PCB_FLAG_END)){
			Log.e(TAG,"saveSmallPCBFlag,invalid index:" + fm.index,new RuntimeException());
			return;
		}
		saveTestFlagInner(realIndex,state);
	}

	public void savePCBAFlag(int realIndex,boolean state){
		if((realIndex < PCBA_FLAG_START) || (realIndex > PCBA_FLAG_END)){
			Log.e(TAG,"savePCBAFlag,invalid index:" + realIndex,new RuntimeException());
			return;
		}
		saveTestFlagInner(realIndex,state);
		FlagModel fm = null;
		for (TestItem item : allItems) {
            if (item.fm.pcbaFlag==realIndex) {
            	fm = item.fm;
            }
        }
		TestDataUtil.getTestDataUtil().backDataForUser(mContext,fm,state,true);
	}
	
	public void savePCBAFlag(FlagModel fm,boolean state){
		int realIndex = fm.pcbaFlag;
		if((realIndex < PCBA_FLAG_START) || (realIndex > PCBA_FLAG_END)){
			Log.e(TAG,"savePCBAFlag,invalid index:" + fm.index,new RuntimeException());
			return;
		}
		saveTestFlagInner(realIndex,state);
	}

	
	public void saveBackFlag(int realIndex,boolean state){
		if((realIndex < SMALL_BACK_FLAG_START) || (realIndex > SMALL_BACK_FLAG_END)){
			Log.e(TAG,"savePCBAFlag,invalid index:" + realIndex,new RuntimeException());
			return;
		}
		saveTestFlagInner(realIndex,state);
	}
	
	public void saveBackFlag(FlagModel fm,boolean state){
		int realIndex = fm.backFlag;
		if((realIndex < SMALL_BACK_FLAG_START) || (realIndex > SMALL_BACK_FLAG_END)){
			Log.e(TAG,"savePCBAFlag,invalid index:" + fm.index,new RuntimeException());
			return;
		}
		saveTestFlagInner(realIndex,state);
	}
	
	public int getBackFlag(int realIndex){
		if((realIndex < SMALL_BACK_FLAG_START) || (realIndex > SMALL_BACK_FLAG_END)){
			Log.e(TAG,"getPCBAFlag,invalid index:" + realIndex,new RuntimeException());
			return -1;
		}
		return getTestFlagInner(realIndex);
	}

	public void saveTestFlag(FlagModel fm,boolean state){
		int realIndex = fm.testFlag;
		if((realIndex < TEST_FLAG_START) || (realIndex >= TEST_FLAG_MAX)){
			Log.e(TAG,"saveTestFlag,invalid index:" + fm.index,new RuntimeException());
			return;
		}
		saveTestFlagInner(realIndex,state);
	}

	public void saveTestFlagInner(int index,boolean state){
		mTestFlag[index] = (byte)(state ? 'P' : 'F');
		SystemUtil.setNvFactoryData3IByte(mTestFlag);
	}

	
	
	public int getSmallPCBFlag(int realIndex){
		if((realIndex < SMALL_PCB_FLAG_START) || (realIndex > SMALL_PCB_FLAG_END)){
			Log.e(TAG,"getSmallPCBFlag,invalid index:" + realIndex,new RuntimeException());
			return -1;
		}
		return getTestFlagInner(realIndex);
	}

	public int getPCBAFlag(int realIndex){
		if((realIndex < PCBA_FLAG_START) || (realIndex > PCBA_FLAG_END)){
			Log.e(TAG,"getPCBAFlag,invalid index:" + realIndex,new RuntimeException());
			return -1;
		}
		return getTestFlagInner(realIndex);
	}
	
	public int getUSBFlag(int index){
		if((index < USB_FLAG_START) || (index > USB_FLAG_END)){
			return -1;
		}
		return getTestFlagInner(index);
	}

	public int getTestFlag(int realIndex){
		if((realIndex < TEST_FLAG_START) || (realIndex >= TEST_FLAG_MAX)){
			Log.e(TAG,"getTestFlag,invalid index:" + realIndex,new RuntimeException());
			return -1;
		}
		return getTestFlagInner(realIndex);
	}

	public int getTestFlagInner(int index){
		if((index < SMALL_PCB_FLAG_START) || (index >= TEST_FLAG_MAX)){
			Log.e(TAG,"getTestFlagInner,invalid index:" + index);
			return -1;
		}

		byte flag = mTestFlag[index];

		if('P' == flag){
			return TEST_FLAG_PASS;
		}else if('F' == flag){
			return TEST_FLAG_FAIL;
		}else{
			return TEST_FLAG_NO_TEST;
		}
	}

	public int loadConfig(){

		File configFile = new File(CONFIG_PATH);
		if(!configFile.exists()){
			Log.e(TAG,"cit config file doesnot exist");
			return -1;
		}

		XmlPullParser parser = null;
		try{
			parser = XmlPullParserFactory.newInstance().newPullParser();
		}catch(Exception e){
			e.printStackTrace();
			Log.e(TAG,"can not create xml parser");
			return -2;
		}

		FileInputStream in = null;
		try{
			in = new FileInputStream(configFile);
		}catch(Exception e){
			e.printStackTrace();
			Log.e(TAG,"can not read config file");
			return -2;
		}

		try{
			parser.setInput(in,null);
		}catch(Exception e){
			e.printStackTrace();
			Log.e(TAG,"can not set parser input");
			try{
				in.close();
			}catch(Exception e2){
				e2.printStackTrace();
			}
			return -2;
		}
		
		AttributeSet attrs = Xml.asAttributeSet(parser);

		int type;

		try{
			while((type = parser.next()) != XmlPullParser.START_TAG &&
					type != XmlPullParser.END_DOCUMENT){
				// do nothing
				// just look for the root node
			}

			final int depth = parser.getDepth();
			while(((type = parser.next()) != XmlPullParser.END_TAG ||
						parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT){
				if(type != XmlPullParser.START_TAG){
					continue;
				}
				final String name = parser.getName();
				try {
					if(TestItem.class.getCanonicalName().equals(name)){
						TestItem item = new TestItem(mContext,attrs);
						boolean isAdd = item.visibility;
						if(isAdd) allItems.add(item);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}

		try{
			in.close();
		}catch(Exception e){
			e.printStackTrace();
		}

		return allItems.size();

	}
}
