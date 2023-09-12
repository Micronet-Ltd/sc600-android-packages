
package com.lovdream.factorykit;

import android.os.Bundle;
import android.os.SystemClock;
import android.app.Dialog;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.Context;
import android.view.View;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.MotionEvent;
import android.view.LayoutInflater;
import android.widget.Toast;
import android.widget.Button;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.util.Log;
import android.os.SystemProperties;
import android.content.DialogInterface;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.swfp.utils.SaveDataModel;
import com.swfp.utils.TestDataUtil;

public abstract class TestItemBase extends Fragment implements Button.OnClickListener,FragmentKeyHandler,FragmentNewIntentHandler,View.OnTouchListener,DialogInterface.OnDismissListener{

	public interface TestCallback{
		public void onTestFinish(TestItemBase item);
	}

	public static final String TAG = Main.TAG;
	private boolean isFinished = false;

	public abstract String getKey();
	public abstract String getTestMessage();
	public abstract void onStartTest();
	public abstract void onStopTest();
	

	private HashMap<String,String[]> mParameterMap;
	private Button mSuccessBt;
	private Button mFailBt;
	private TextView msgTv ;
	private Dialog mDialog;

	private String mParameter;
	private boolean mIsAutoJuge;
	private boolean mIsAutoTest;
	private boolean mIsPCBATest;
	private boolean mIsBackTest; 
    private boolean mIsUSBTest;
	private boolean mIsSmallPCB;
	private TestCallback mCallback;
	private int mFlagIndex;
	protected boolean mCameraFailClicked=false;
	
	//add by xxf
	private FlagModel fm;
	//add by xxf

	public void init(Context context,String parameter,boolean isAutoJuge,int flagIndex,FlagModel fm){
		mParameter = parameter;
		mIsAutoJuge = isAutoJuge;
		mFlagIndex = flagIndex;
		this.fm =fm;
	}

	public View getTestView(LayoutInflater inflater){
		/* implements in subclass */
		return null;
	}

	protected boolean isPCBATest(){
		return mIsPCBATest;
	}
	
	protected boolean isBackTest(){
		return mIsBackTest;
	}

	public void setPCBATest(boolean isPCBATest){
		mIsPCBATest = isPCBATest;
	}
	public void setBackTest(boolean isBackTest){
		mIsBackTest = isBackTest;
	}
	
	public void setUSBTest(boolean isUSBTest){
		mIsUSBTest = isUSBTest;
	}

	public void setSmallPCBTest(boolean isSmallPCB){
		mIsSmallPCB = isSmallPCB;
	}

	public void setAutoTest(boolean isAutoTest){
		mIsAutoTest = isAutoTest;
	}

	public void setTestCallback(TestCallback callback){
		mCallback = callback;
	}
	
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		if(fm!=null){
			StringBuilder sb = new StringBuilder();
			sb.append("index=" + fm.index+"  ");
			sb.append("pcbaFlag=" + fm.pcbaFlag+"  ");
			sb.append("smallPcbFlag=" + fm.smallPcbFlag+"  ");
			sb.append("testFlag=" + fm.testFlag+"  ");
			Log.d(TAG,sb.toString());
		}
		Log.d(TAG,getKey() + " startTest");
		onStartTest();
		isFinished = false;
	}

	@Override
	public void onDetach(){
		super.onDetach();
		Log.d(TAG,getKey() + " stopTest");
		onStopTest();
		if(mCallback != null){
			mCallback.onTestFinish(this);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
		View v = inflater.inflate(R.layout.test_item_base,container,false);

		mSuccessBt = (Button)v.findViewById(R.id.button_success);
		mSuccessBt.setOnClickListener(this);
		mFailBt = (Button)v.findViewById(R.id.button_fail);
		mFailBt.setOnClickListener(this);
		if(!mIsAutoJuge){
			mSuccessBt.setEnabled(true);
		}

		msgTv = (TextView)v.findViewById(R.id.test_message);
		msgTv.setText(getTestMessage());

		View testView = getTestView(inflater);
		if(testView != null){
			FrameLayout content = (FrameLayout)v.findViewById(R.id.container);
			content.addView(testView);
		}

		v.findViewById(R.id.item_base_root).setOnTouchListener(this);
		return v;
	}

	
	protected void changeShowMsg(String msg,boolean isAppend){
		if(msgTv!=null){
			if(isAppend) msgTv.setText(msgTv.getText().toString()+msg);
			else msgTv.setText(msg);
		}
	}
	@Override
	public void onClick(View v){
		switch(v.getId()){
			case R.id.button_success:
				onSuccessClick();
				break;
			case R.id.button_fail:
				onFailClick();
				break;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event){
		/* intercept event,avoid nether view to hand it*/
		return true;
	}

	public boolean isAutoJuge(){
		return mIsAutoJuge;
	}

	public String[] getParameter(String argKey){

		if((mParameterMap != null) && (mParameterMap.size() > 0)){
			return mParameterMap.get(argKey);
		}

		if(mParameter == null){
			return null;
		}

		mParameterMap = new HashMap<String,String[]>();

		String splitArgs[] = mParameter.split(";");
		for(String arg : splitArgs){
			String splitArg[] = arg.split(":");
			if((splitArg != null) && (splitArg.length > 1) && !isEmptyString(splitArg[0])){
				mParameterMap.put(splitArg[0],splitArg[1].split(","));
			}
		}

		return mParameterMap.get(argKey);
	}

	public void toast(int resId){
		Toast.makeText(getActivity(),resId,Toast.LENGTH_SHORT).show();
	}

	public void showFullscreenOverlay(View v,boolean cancelable){
		dismissOverlay();
		mDialog = new Dialog(getActivity(),R.style.Dialog_Fullscreen);
		mDialog.setContentView(v);
		mDialog.setCancelable(cancelable);
		mDialog.setOnDismissListener(this);
		mDialog.show();
		SystemProperties.set(Utils.PROP_FULL_SCREEN,String.valueOf(true));
	}

	public void dismissOverlay(){
		if(mDialog != null){
			mDialog.dismiss();
			mDialog = null;
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog){
		SystemProperties.set(Utils.PROP_FULL_SCREEN,String.valueOf(false));
	}

	public boolean isEmptyString(String str){
		return (str == null) || "".equals(str);
	}

	public void hideButtons(){
		if(mSuccessBt != null){
			mSuccessBt.setVisibility(View.GONE);
		}
		if(mFailBt != null){
			mFailBt.setVisibility(View.GONE);
		}
	}

	public void enableSuccess(boolean enabled){
		if(mSuccessBt != null){
			boolean isEnabled =  mSuccessBt.isEnabled();
			if(isEnabled !=enabled )
			mSuccessBt.setEnabled(enabled);
		}
	}

	public void postSuccess(){
		saveTestResult(true);
		if(mSuccessBt != null){
			mSuccessBt.setEnabled(true);
		}
		if (mIsAutoTest || getKey().equals("light_sensor")
				|| getKey().equals("barometer_test")
				|| getKey().equals("camera_test_back")
				|| getKey().equals("camera_test_front")
				||getKey().equals("nfc_test")
				|| getKey().equals("gsensor_test")) {
			finish();
		}
	}

	public void postFail(){
		saveTestResult(false);
		if(mIsAutoTest || mCameraFailClicked){
			mCameraFailClicked=false;
			finish();
		}
	}

	public void onSuccessClick(){
		saveTestResult(true);
		finish();
	}

	public void onFailClick(){
		try {
			saveTestResult(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		finish();
	}

	private void finish(){
		if(isFinished){
			return;//prevent repeat popback
		}
		Log.d(TAG,getKey() + " finish()");
		FragmentManager fm = getFragmentManager();
		if(fm != null){
			fm.popBackStack();
			isFinished = true;
		}
	}

	private void saveTestResult(boolean result){
		if(mIsPCBATest){
			Config.getInstance(getActivity()).savePCBAFlag(fm,result);
		}else if(mIsSmallPCB){
			Config.getInstance(getActivity()).saveSmallPCBFlag(fm,result);
		}else if(mIsBackTest){
			Config.getInstance(getActivity()).saveBackFlag(fm, result);
		}else if(mIsUSBTest){
			Config.getInstance(getActivity()).saveUSBFlag(fm, result);
		}else{
			Config.getInstance(getActivity()).saveTestFlag(fm,result);
		}		
		TestDataUtil.getTestDataUtil().backDataForUser(getActivity(),fm,result,true);
	}
	

	@Override
	public boolean onKeyDown(int keyCode,KeyEvent event){
		return false;
	}

	@Override
	public boolean onKeyUp(int keyCode,KeyEvent event){
		return false;
	}

	@Override
	public void onNewIntent(Intent intent){
	}
}
