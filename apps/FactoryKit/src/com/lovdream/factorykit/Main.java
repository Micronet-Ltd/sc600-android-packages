
package com.lovdream.factorykit;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.content.Context;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.StatusBarManager;
import android.app.ActivityManager;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.os.SystemProperties;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;

import com.lovdream.factorykit.Utils;
import com.swfp.utils.ProjectControlUtil;
import com.swfp.utils.SaveDataModel;
import com.swfp.utils.TestDataUtil;

import java.util.List;

import com.lovdream.factorykit.items.SystemVersionTest;

import android.preference.PreferenceScreen;
import android.content.res.Configuration;
import android.widget.TextView;
import android.widget.ListView;
import android.view.View;
import android.preference.Preference;

public class Main extends PreferenceActivity {

	public static final String TAG = "factorykit";

	Handler mHandler = new Handler();
	private StatusBarManager mSbManager;
	private boolean mIsLocationProviderEnabled;
	private boolean mIsWifiEnable;
	private boolean mIsBluetoothEnable;
	private boolean mIsNfcEnable;
    public static boolean isBatteryFull = false;
    public static int currentTemp = 0;
    public static String resultString = "";
    public static int currentVoltage = 0;
    public static int camera_count = 2;
    public static boolean full_auto=false;
    Preference testResult;
    Preference pcbaTest;
    Preference smallPcb;
    Preference citVersionInfo;
    private int counter = 0;

	@Override
	protected void onCreate(Bundle bundle){
		super.onCreate(bundle);
		
		turnOffIrLed();

		if (ActivityManager.isUserAMonkey()) {
			Log.e(TAG, "user is a monkey");
			finish();
			return;
		}

		try {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "e.getMessage()--->"+e.getMessage());
		}

		
		FactoryKitApplication app = (FactoryKitApplication) getApplication();
		if (app.getTestConfig().getTestItems().size() <= 0) {
			Toast.makeText(this, R.string.load_config_error, Toast.LENGTH_SHORT)
					.show();
			finish();
		}

		mSbManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);

		addPreferencesFromResource(R.xml.main_list);
				
		PreferenceScreen mainScreen = (PreferenceScreen) findPreference("main");
		testResult = findPreference("test_result");
		pcbaTest = findPreference("pcba_test");
		smallPcb = findPreference("small_pcb");
		citVersionInfo = findPreference("cit_version_info");
		
        ListView v = getListView();
        TextView returnPrefs = new TextView(this);
        returnPrefs.setText("");
        returnPrefs.setHeight(1000);
        returnPrefs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(counter == 1){
                    mainScreen.addPreference(testResult);
                    mainScreen.addPreference(pcbaTest);
                    mainScreen.addPreference(smallPcb);
                    mainScreen.addPreference(citVersionInfo);
                    returnPrefs.setHeight(0);
                } else
                    counter++;
            }
        });
		v.addFooterView(returnPrefs);
		
		// Remove preferences
        mainScreen.removePreference(testResult);
		mainScreen.removePreference(pcbaTest);
		mainScreen.removePreference(smallPcb);
		mainScreen.removePreference(citVersionInfo);
		
		int type = (Integer.reverse(SystemProperties.getInt("hw.board.id", -1))>>>29);
		if(type == 0 || type == 1) {
            mainScreen.removePreference(findPreference("auto_test_no_cam"));
            mainScreen.removePreference(findPreference("auto_test_one_cam"));
		}

		if (!ProjectControlUtil.isC802) {
			mainScreen.removePreference(findPreference("test_usb"));
		}
		if (SystemProperties.getBoolean(Utils.PROP_DEBUG_ABLE, false)) {
			Utils.createShortcut(this, Main.class);
			SystemProperties.set(Utils.PROP_DEBUG_ABLE, String.valueOf(false));
		}
		if (SystemProperties.getBoolean(CrashHandler.CRASH_PROP, false)) {
			Toast.makeText(this,
					getString(R.string.crash_mesg, CrashHandler.TRACE_FILE),
					Toast.LENGTH_LONG).show();
			SystemProperties
					.set(CrashHandler.CRASH_PROP, String.valueOf(false));
		}
		mIsLocationProviderEnabled = Utils.isLocationProviderEnabled(this);// bug
																			// 15688
		mIsBluetoothEnable = Utils.isBluetoothEnable(this);
		mIsNfcEnable = Utils.isNfcEnable(this);
		mIsWifiEnable = Utils.isWifiEnable(this);
		// To save test time, enable some devices first
		Utils.enableWifi(this, true);
		Utils.enableBluetooth(true);
		Utils.enableGps(this, true);
		Utils.enableNfc(this, false);
		// Utils.enableCharging(true);
	}
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (!mIsWifiEnable) Utils.enableWifi(this, false);
		if (!mIsBluetoothEnable) Utils.enableBluetooth(false);
		Utils.enableGps(this, mIsLocationProviderEnabled);//bug 15688 15689
		Utils.enableNfc(this, mIsNfcEnable);
	}

	@Override
	public void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		Fragment intentHandler = getFragmentManager().findFragmentById(android.R.id.content);
		if(intentHandler instanceof FragmentNewIntentHandler){
			((FragmentNewIntentHandler)intentHandler).onNewIntent(intent);
		}
		String type = intent.getStringExtra("test_type");
		Fragment fragment = null;
		boolean sdMounted = Utils.isSdMounted(this);
		boolean simReady = Utils.isSimReady();
		if("single".equals(type)){
			Utils2.getInstance().currentTestMode=Utils2.SINGLE;
			fragment = Fragment.instantiate(this,SingleTest.class.getName());
        }else if("full_auto".equals(type) || "auto".equals(type) || "auto_no_cam".equals(type) || "auto_one_cam".equals(type)){
            if((Integer.reverse(SystemProperties.getInt("hw.board.id", -1))>>>29) == 2){
                 if(!sdMounted){
                        showWarningDialog(0);
                        return;
                }
            } else {
                if(!sdMounted && !simReady){
                    showWarningDialog(2);
                    return;
                } else if(!sdMounted){
                    showWarningDialog(0);
                    return;
                } else if(!simReady){
                    showWarningDialog(1);
                    return;
                }
            }
            
            if("auto_no_cam".equals(type)){
                camera_count = 0;
            } else if("auto_one_cam".equals(type)){
                camera_count = 1;
            } else if("full_auto".equals(type)){
                full_auto=true;
            }
            
			fragment = Fragment.instantiate(this,AutoTest.class.getName());
		}else if("pcba".equals(type)){
			Utils2.getInstance().currentTestMode=Utils2.PCBA_1;
			fragment = Fragment.instantiate(this,PCBATest.class.getName());
			//((PCBATest)fragment).setPcba_1(true);
		}else if("spcba".equals(type)){
			Utils2.getInstance().currentTestMode=Utils2.SPCBA;
			fragment = Fragment.instantiate(this,PCBATest.class.getName());
			//((PCBATest)fragment).setPcba_1(true);
		}else if("pcba2".equals(type)){
			Utils2.getInstance().currentTestMode=Utils2.PCBA_2;
			fragment = Fragment.instantiate(this,BackTest.class.getName());
			//((PCBATest)fragment).setPcba_1(false);
		}else if("small".equals(type)){
			Utils2.getInstance().currentTestMode=Utils2.SMALLPCB;
			fragment = Fragment.instantiate(this,SmallPCB.class.getName());
		}else if("result".equals(type)){
			Utils2.getInstance().currentTestMode=Utils2.RESULT;
			fragment = Fragment.instantiate(this,TestResult.class.getName());
		}else if("usb".equals(type)){
			Utils2.getInstance().currentTestMode=Utils2.STRESS;
			fragment = Fragment.instantiate(this,UsbTest.class.getName());
		}else if("version".equals(type)){
			fragment = Fragment.instantiate(this,SystemVersionTest.class.getName());
		}

		if(fragment == null){
			return;
		}
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.add(android.R.id.content,fragment,type);
		ft.addToBackStack(type);
		ft.commit();
	}

	private void showWarningDialog(int messageType){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (messageType){
            case 0:
                builder.setMessage(R.string.no_sd_card);
                break;
            case 1:
                builder.setMessage(R.string.no_sim_card);
                break;
            case 2:
                builder.setMessage(R.string.no_sd_and_sim_card);
                break;
        }
		builder.setPositiveButton(android.R.string.ok,null);
		builder.show();
	}
	
	@Override
	public boolean onKeyDown(int keyCode,KeyEvent event){
		Fragment fragment = getFragmentManager().findFragmentById(android.R.id.content);
		if((fragment instanceof FragmentKeyHandler) && ((FragmentKeyHandler)fragment).onKeyDown(keyCode,event)){
			return true;
		}
		return keyCode == KeyEvent.KEYCODE_MENU ? true : super.onKeyDown(keyCode,event);
	}

	@Override
	public boolean onKeyUp(int keyCode,KeyEvent event){
		Fragment fragment = getFragmentManager().findFragmentById(android.R.id.content);
		if((fragment instanceof FragmentKeyHandler) && ((FragmentKeyHandler)fragment).onKeyUp(keyCode,event)){
			return true;
		}
		return keyCode == KeyEvent.KEYCODE_MENU ? true : super.onKeyUp(keyCode,event);
	}

	
	//9.0之后此方法不可重写,否则导致界面崩溃,具体原因有待研究;
	/*@Override
		public void onAttachedToWindow() {
			try {
				getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
			} catch (Exception e) {
				e.printStackTrace();
				Log.d(TAG, "e.onAttachedToWindow()--->"+e.getMessage());
			}
			super.onAttachedToWindow();
		}*/

	@Override
	public void onResume(){
		super.onResume();
		ProjectControlUtil.comeCit(true);
		mSbManager.disable(StatusBarManager.DISABLE_EXPAND);
	}

	@Override
	public void onPause(){
		super.onPause();
		ProjectControlUtil.comeCit(false);
		mSbManager.disable(StatusBarManager.DISABLE_NONE);
	}

	private long exitTime;

	@Override
	public void onBackPressed(){

		if(getFragmentManager().popBackStackImmediate()){
			return;
		}
		if((System.currentTimeMillis() - exitTime) < 1000){
			super.onBackPressed();
		}else{
			exitTime = System.currentTimeMillis();
			Toast.makeText(this,R.string.double_back_msg,Toast.LENGTH_SHORT).show();
		}
	}
	
	private void turnOffIrLed() {
		LightsManager lm = new LightsManager(this);
		Light irLed = lm.getLight(LightsManager.LIGHT_ID_BACKLIGHT);
		irLed.setColor(0x00000000);
	}
}
