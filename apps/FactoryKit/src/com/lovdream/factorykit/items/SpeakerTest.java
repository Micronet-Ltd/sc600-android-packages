package com.lovdream.factorykit.items;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.lovdream.factorykit.TestItemBase;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import com.lovdream.factorykit.R;
import com.lovdream.factorykit.services.ISpeakerAndStorageHelper;
import android.os.Environment;
import com.lovdream.factorykit.services.SpeakerAndStorageService;

/**
 * Created by yangzhiming on 2017/6/20.
 */

public class SpeakerTest extends TestItemBase{
    private static final String TAG = "SpeakerAndStorageTest";
    public static String TEST_FILE;
    //private MyAsyncTask mMyAsyncTask;
    private Handler mHandler = new Handler();
    @Override
    public String getKey() {
        return "speaker_test";
    }

    @Override
    public String getTestMessage() {
        return getString(R.string.speaker_storage_msg);
    }

    @Override
    public void onStartTest() {
    }

    @Override
    public void onStopTest() {
    }

    @Override
    public void onResume() {
        super.onResume();
        enableSuccess(false);
        Intent intent = new Intent(getActivity(), SpeakerAndStorageService.class);
        getActivity().bindService(intent,mServiceConnection,Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            mService.stopMusic();
        }catch (RemoteException e){
            e.printStackTrace();
        }
        getActivity().unbindService(mServiceConnection);
    }

    private ISpeakerAndStorageHelper mService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ISpeakerAndStorageHelper.Stub.asInterface(service);
            try {
                TEST_FILE =Environment.getExternalStorageDirectory().getAbsolutePath()+"/test.mp3";
                        mService.copyTestToSd(TEST_FILE);
		enableSuccess(true);
		try {
			mService.playMusic(TEST_FILE);
		    }catch (RemoteException e){
			e.printStackTrace();
		    }
            }catch (RemoteException e){
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };


}
