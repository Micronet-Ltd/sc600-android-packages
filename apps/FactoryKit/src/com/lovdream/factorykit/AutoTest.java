package com.lovdream.factorykit;

import android.os.Bundle;
import android.os.Handler;
import android.app.Fragment;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.view.View;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.LayoutInflater;
import android.widget.Toast;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.SystemProperties;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import com.lovdream.factorykit.Config.TestItem;

public class AutoTest extends Fragment implements TestItemBase.TestCallback{

	private static final String TAG = Main.TAG;

	private TestItemFactory mFactory;
	private ArrayList<TestItem> mItems;
	private int mCurrentIndex;
	
	private final int AUTO_TEST_TIME_INTERVAL = 300;
	private final int SMART_TAB_LTE = 0;
	private final int SMART_TAB_LOW_COST = 1;
	private final int SMART_CAM_BASIC = 2;
	private final int SMART_CAM_FULL = 6;
	private Handler mHandler = new Handler();
	private boolean quitTest;
    
    private String[] notRunOnDevType0 = {};
    private String[] notRunOnDevType1 = {};
    private String[] notRunOnDevType2 = {"flash_light", "distance_sensor", "noise_mic", "button_light", "headset_test_nuno", "sim_test", "cellular_data_test", "compass", "lcd_test", "tp_test"};
    private String[] notRunOnDevType3 = {"flash_light", "distance_sensor", "noise_mic", "button_light", "headset_test_nuno", "compass", "lcd_test", "tp_test"};
	private String[] notRunOnDevWithOneCam = {"camera_test_front", "back_led", "light_sensor"};
    private String[] notRunOnDevWithoutCam = {"camera_test_front", "back_led", "camera_test_back", "light_sensor", "nfc_test"};
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);

		FactoryKitApplication app = (FactoryKitApplication)getActivity().getApplication();
		mItems = new ArrayList<TestItem>();
		ArrayList<TestItem> allItems = app.getTestConfig().getTestItems();
		for(TestItem item : allItems){
			if(item.inAutoTest){
				mItems.add(item);
			}
		}
		mFactory = TestItemFactory.getInstance(getActivity());

		mCurrentIndex = 0;
		mHandler.postDelayed(mAutoTestRunnale, AUTO_TEST_TIME_INTERVAL);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		TextView tv = new TextView(getActivity());
		tv.setTextSize(24);
		tv.setText(R.string.auto_test_msg);
		tv.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		return tv;
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		activity.setTitle(R.string.auto_test);
		setHasOptionsMenu(true);
		quitTest = false;
		Log.d(TAG,"AutoTest onAttach");
	}

	@Override
	public void onDetach(){
		super.onDetach();
		getActivity().setTitle(R.string.app_name);
		Log.d(TAG,"AutoTest onDetach");
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu,MenuInflater inflater){
		menu.add(R.string.quit_auto_test);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		mHandler.removeCallbacks(mAutoTestRunnale);

		FragmentManager fm = getActivity().getFragmentManager();
		fm.popBackStack();

		quitTest = true;

		return true;
	}

	@Override
	public void onTestFinish(TestItemBase item){
		if(quitTest){
			FragmentManager fm = getActivity().getFragmentManager();
			fm.popBackStack();
			return;
		}
		mHandler.postDelayed(mAutoTestRunnale, AUTO_TEST_TIME_INTERVAL);
	}

	private Runnable mAutoTestRunnale = new Runnable(){
		@Override
		public void run(){
			if((mCurrentIndex >= 0) && (mCurrentIndex < mItems.size())){
				
				Log.d(TAG,"mAutoTestRunnale,mCurrentIndex:" + mCurrentIndex);
				TestItem item = (TestItem)mItems.get(mCurrentIndex++);
				if(!item.inAutoTest || !isNeededThisTest(item.key)){
					onTestFinish(null);
					return;
				}
				TestItemBase fragment = mFactory.createTestItem(getActivity(),item);

				if(fragment == null){
					Toast.makeText(getActivity(),R.string.no_item,Toast.LENGTH_SHORT).show();
					onTestFinish(fragment);
					return;
				}

				if(fragment.isAdded()){
					onTestFinish(fragment);
					return;
				}

				fragment.setTestCallback(AutoTest.this);
				fragment.setAutoTest(true);
				FragmentManager fm = getFragmentManager();
				if(fm == null){
					Log.e(TAG,"in mAutoTestRunnale,fm == null");
					return;
				}
				FragmentTransaction ft = fm.beginTransaction();
				ft.add(android.R.id.content,fragment,item.key);
				ft.addToBackStack(item.key);
				ft.commit();
				//getActivity().setTitle(item.displayName);
			}else{
				finishAndShowResult();
			}
		}
	};

	private void finishAndShowResult(){
		FragmentManager fm = getActivity().getFragmentManager();
		fm.popBackStack();

		Fragment fragment = Fragment.instantiate(getActivity(),AutoTestResult.class.getName());
		FragmentTransaction ft = fm.beginTransaction();
		ft.add(android.R.id.content,fragment);
		ft.addToBackStack("result");
		ft.commit();
	}
	
	private int getDeviceType(){
        int boardType = SystemProperties.getInt("hw.board.id", -1);
        
        if(boardType == -1){
            return -1;
        }
        switch (boardType){
            case 0:
                return SMART_TAB_LTE;
            case 1:
                return SMART_TAB_LOW_COST;
            case 2:
                return SMART_CAM_BASIC;
            case 6: 
                return SMART_CAM_FULL;
            default: 
                return -1;
        
        }
	}
	
	private String[] getNotNeededTests(int devType){
        switch (devType){
        case 0:
            return notRunOnDevType0;
        case 1:
            return notRunOnDevType1;
        case 2:
            if(Main.camera_count == 0)
                return appendArrays(notRunOnDevType2, notRunOnDevWithoutCam);
            else if (Main.camera_count == 1)
                return appendArrays(notRunOnDevType2, notRunOnDevWithOneCam);
            else return notRunOnDevType2;
        case 6:
            if(Main.camera_count == 0)
                return appendArrays(notRunOnDevType3, notRunOnDevWithoutCam);
            else if (Main.camera_count == 1)
                return appendArrays(notRunOnDevType3, notRunOnDevWithOneCam);
            else return notRunOnDevType3;    
        }
        return null;
	
	}
	
	private boolean isNeededThisTest (String key){
        int devType = getDeviceType();
        boolean needThisTest = true;
        String[] notNeededTests = getNotNeededTests(devType);
        for(int i = 0; i < notNeededTests.length; i++){
            if (key.equals(notNeededTests[i])){
                needThisTest = false;
            }
        }
        return needThisTest;
	}
	
	private <T> T[] appendArrays(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
