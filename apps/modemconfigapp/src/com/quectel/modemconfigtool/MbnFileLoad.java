/**
 * Copyright (c) 2014-2015 Qualcomm Technologies, Inc.  All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 **/

package com.quectel.modemconfigtool;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.Messenger;
import android.content.ServiceConnection;
import android.app.Service;
import android.content.ComponentName;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.os.IPowerManager;
import android.os.ServiceManager;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import android.widget.Toast;
import android.os.Environment;

import com.qualcomm.qti.modemtestmode.MbnFileLoadService;
import com.qualcomm.qti.modemtestmode.MbnMetaInfo;


public class MbnFileLoad extends Activity {
	private final String TAG = "modemconfigtool-MbnFileLoad";

	private Messenger mServiceMessenger;
	private Messenger mLocalMessenger;

	private Context mContext;
	TextView tvShowMessage;
	Button bnStart, bnQuery,atSend;
	int offset;

	NvManager mNvManager;

	Map<String, Integer> mbnMap = new HashMap<String, Integer>();

	private static final int DIRECTION_IN = 0X10;
	private static final int DIRECTION_OUT = DIRECTION_IN + 1;


	private SharedPreferences mSharedPreferences;
	private final static String LOAD_KEY = "com.qualcomm.qti.MbnManager.mbnloaded";

	private int mAtCommandsId = R.array.at_commands_for_mbns_config;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mbn_file_load);
		mContext = this;
		Log.d(TAG, "klein----------onCreate");
		tvShowMessage = (TextView) findViewById(R.id.show_message);
		bnStart = (Button)findViewById(R.id.start);
		bnQuery = (Button)findViewById(R.id.query);
		tvShowMessage.setMovementMethod(ScrollingMovementMethod.getInstance());
		mNvManager = new NvManager(mContext);

		mSharedPreferences = getSharedPreferences(LOAD_KEY, MODE_PRIVATE);

		Log.d(TAG, "ismbnloaded : " + mSharedPreferences.getInt("isloaded",-1));

		if (mSharedPreferences.getInt("isloaded",-1) == 1){
			Log.d(TAG, "mbn is loaded, exit");
			//onDestroy();
		}


		if(mLocalMessenger == null) {
			mLocalMessenger = new Messenger(new MyHandler());
		}

		//MbnAppGlobals.getInstance().getMbnConfig(0);

		startMbnManagerService();

		bnStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				try{
					clearAllTempMbnFiles();
				}catch (Exception e){
					e.printStackTrace();
				}

				getMbnFiles();

				try{
					Set<Map.Entry<String, Integer>> set = mbnMap.entrySet();
					for (Map.Entry<String, Integer> me : set) {
						String key = me.getKey();
						Integer value = me.getValue();
						Log.d(TAG, "copy mbn files to sdcard : " + "sdcard/" + key + ".mbn");
						copyMbnToSD(value, "sdcard/" + key + ".mbn");
					}
				}catch (Exception e){
					e.printStackTrace();
				}

				startLoadMbn();
				bnStart.setEnabled(false);
			}
		});

		bnQuery.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getTheMbnConfigure();
			}
		});

	}


	void loadMbns(){
		Log.d(TAG, "loadMbns");
		try{
			clearAllTempMbnFiles();
		}catch (Exception e){
			e.printStackTrace();
		}

		getMbnFiles();

		try{
			Set<Map.Entry<String, Integer>> set = mbnMap.entrySet();
			for (Map.Entry<String, Integer> me : set) {
				String key = me.getKey();
				Integer value = me.getValue();
				copyMbnToSD(value, "sdcard/" + key + ".mbn");
			}

		}catch (Exception e){
			e.printStackTrace();
		}

		startLoadMbn();
		bnStart.setEnabled(false);

	}

	void getMbnFiles(){
		Log.d(TAG, "getMbnFiles");
		String[] stringArray = getResources().getStringArray(R.array.mbns);
		for (int i = 0; i < stringArray.length; i++) {
			mbnMap.put(stringArray[i], getResources().getIdentifier(stringArray[i],"raw",getPackageName()));
		}

		Set<Map.Entry<String, Integer>> set = mbnMap.entrySet();
		for (Map.Entry<String, Integer> me : set) {
			String key = me.getKey();
			Integer value = me.getValue();
			Log.d(TAG, "key = " + key + ", " + "value = " + value);
		}
	}

	private void startMbnManagerService() {
		Log.d(TAG, "startMbnManagerService");
		Intent intent = new Intent(mContext, com.qualcomm.qti.modemtestmode.MbnFileLoadService.class);

        /*Intent serviceIntent = new Intent();
        serviceIntent.setComponent(new ComponentName("com.qualcomm.qti.modemtestmode","com.qualcomm.qti.modemtestmode.MbnFileLoadService"));
        serviceIntent.setPackage("com.qualcomm.qti.modemtestmode");
        serviceIntent.setAction("com.qualcomm.qti.modemtestmode.MbnFileLoadService");*/

		bindService(intent, conn, Service.BIND_AUTO_CREATE);
	}

	private void getTheMbnConfigure() {
		Log.d(TAG, "getTheMbnConfigure");
		printMessage(DIRECTION_OUT, "CMD_GET_MBN_CONFIGURATION");
		sendCommandMessage(MbnFileLoadService.CMD_GET_MBN_CONFIGURATION);
	}
	private void startLoadMbn() {
		Log.d(TAG, "startLoadMbn");
		printMessage(DIRECTION_OUT, "CMD_LOAD_MBN");
		sendCommandMessage(MbnFileLoadService.CMD_LOAD_MBN);
	}


	private class MyHandler  extends Handler{
		public void handleMessage(Message msg){
			switch(msg.what) {
				case MbnFileLoadService.TYPE_DEFAULT:
					printMessage(DIRECTION_IN, msg.obj.toString());
					break;
				case MbnFileLoadService.TYPE_MBN_LOAD_SUCCESS:
					printMessage(DIRECTION_IN, msg.obj.toString());
					bnStart.setEnabled(true);
					try{
						clearTempMbnFiles();
						if (mNvManager.startWrite(mAtCommandsId) == Common.EVENT_WRITE_SUCCESS){
							Log.d(TAG, "Success: AT command for mbn config set successfully");
							printMessage(DIRECTION_IN, "AT command for mbn config set successfully");
						}
					}catch (Exception e){
						e.printStackTrace();
					}

					//After load success, it will sync the mbns
					// automatically.
					//SharedPreferences.Editor editor = mSharedPreferences.edit();
					//editor.putInt("isloaded",1);
					//editor.commit();
					//reboot();


					break;
				case MbnFileLoadService.TYPE_CONNECTION_ESTABLISHED:
					printMessage(DIRECTION_IN, msg.obj.toString());
					break;
				case MbnFileLoadService.TYPE_MBN_CONFIGURATION:
					ArrayList<MbnMetaInfo> mbnlist = (ArrayList<MbnMetaInfo>) msg.obj;
					if (mbnlist != null) {
						for (int i = 0; i < mbnlist.size(); i++) {
							MbnMetaInfo mbninfo = mbnlist.get(i);
							Log.d(TAG, "mbninfo.getMetaInfo() = " + mbninfo.getMetaInfo()
									+ ", mbninfo.getQcVersion = " + MbnFileLoadService.bytesToHex(mbninfo.getQcVersion()));
							printMessage(DIRECTION_IN, mbninfo.getMetaInfo()
									+ " : " + MbnFileLoadService.bytesToHex(mbninfo.getQcVersion()));
						}
					}
					break;
				default:
					Log.d(TAG,"Unexpected event:" + msg.what);
					break;
			}
		}
	}

	private ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.d(TAG, "onServiceConnected");
			mServiceMessenger = new Messenger(service);

			Message message = Message.obtain();
			message.what = MbnFileLoadService.CMD_TRY_TO_EASTABLISH_CONNECTION;

			message.replyTo = mLocalMessenger;

			try {
				mServiceMessenger.send(message);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}
	};

	public void sendCommandMessage(int command){
		Message message = new Message();
		message.what = command;
		try {
			mServiceMessenger.send(message);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void copyMbnToSD(int originalFileId, String strOutFileName) throws IOException
	{
		InputStream myInput = getResources().openRawResource(originalFileId);;
		OutputStream myOutput = new FileOutputStream(strOutFileName);
		byte[] buffer = new byte[1024];
		int length = myInput.read(buffer);
		while(length > 0)
		{
			myOutput.write(buffer, 0, length);
			length = myInput.read(buffer);
		}

		myOutput.flush();
		myInput.close();
		myOutput.close();
	}

	private void clearTempMbnFiles() throws IOException
	{

		Set<Map.Entry<String, Integer>> set = mbnMap.entrySet();
		for (Map.Entry<String, Integer> me : set) {
			String key = me.getKey();
			Integer value = me.getValue();
			File file = new File("sdcard/" + key + ".mbn");
			Log.d(TAG, "clear file.name = " + file.getName());

			if(file.exists()){
				file.delete();
			}
		}

	}

	private void clearAllTempMbnFiles() throws IOException
	{
		File currentParent;
		File[] currentFiles;

		File root = Environment.getExternalStorageDirectory().getAbsoluteFile();

		if (!root.exists()) {
			Toast.makeText(this, "please check if the sdcard exist !", Toast.LENGTH_LONG).show();
		}
		if (!root.canRead()){
			Toast.makeText(this, "please approve the read perssion of sdcard !", Toast.LENGTH_LONG).show();
		}

		currentFiles = root.listFiles();
		for (int i = 0; i < currentFiles.length; i++){
			if (currentFiles[i].getName().endsWith("mbn")){
				Log.d(TAG, "clear remnant mbn file : " + currentFiles[i].getName());
				currentFiles[i].delete();
			}
		}
	}


	private void printMessage(int direction, String msg){
		if (direction == DIRECTION_IN){
			tvShowMessage.append("< " + msg + "\n");
		} else if (direction == DIRECTION_OUT){
			tvShowMessage.append("> " + msg + "\n");
		}

		offset=tvShowMessage.getLineCount()*tvShowMessage.getLineHeight();
		if(offset>tvShowMessage.getHeight()){
			tvShowMessage.scrollTo(0,offset-tvShowMessage.getHeight());
		}
	}

   // this method will toggle a dialog
   public void rebootWithDialog() {
   	    Log.d(TAG,"reboot device to sync mbn configuration");
		try {
		    IPowerManager pm = IPowerManager.Stub.asInterface(ServiceManager
				.getService(Context.POWER_SERVICE));
		    pm.reboot(true, null, false);
		} catch (RemoteException e) {
		    Log.e(TAG, "PowerManager service died!", e);
		    return;
		}
    }

	public void reboot() {
		Log.d(TAG,"reboot device to sync mbn configuration");
		Intent intent2 = new Intent(Intent.ACTION_REBOOT);
		intent2.putExtra("nowait", 1);
		intent2.putExtra("interval", 1);
		intent2.putExtra("window", 0);
		sendBroadcast(intent2);
	}

	/*public boolean syncMbns() {
		Log.d(TAG,"syncMbns");

		Log.d(TAG, "send first sync command");
		String syncresult = modemtool.sendAtCommand(NvConstants.REQUEST_SEND_AT_COMMAND,
				"at+qmbncfg=\"autosel\",1");

		Log.d(TAG, "syncresult = " + syncresult);

		if (syncresult.contains("OK")){
			Log.d(TAG, "sync success");
			return true;
		} else {
			Log.d(TAG, "sync fail");
			return false;
		}
	}*/


	/*public boolean BackupMbns() {
		Log.d(TAG,"BackupMbns");
		String backupresult = modemtool.sendAtCommand(NvConstants.REQUEST_SEND_AT_COMMAND,
				"AT+QPRTPARA=1");
		Log.d(TAG, "backupresult = " + backupresult);
		if (backupresult.contains("OK")){
			Log.d(TAG, "back up success");
			return true;
		} else {
			Log.d(TAG, "back up fail");
			return false;
		}
	}*/

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(conn);
	}
}
