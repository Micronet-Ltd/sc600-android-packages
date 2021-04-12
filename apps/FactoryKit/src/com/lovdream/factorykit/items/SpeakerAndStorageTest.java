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

import com.lovdream.factorykit.services.SpeakerAndStorageService;

/**
 * Created by yangzhiming on 2017/6/20.
 */

public class SpeakerAndStorageTest extends TestItemBase{
    private static final String TAG = "SpeakerAndStorageTest";
    public static String TEST_FILE;
    private MyAsyncTask mMyAsyncTask;
    private boolean isCanPlay = true;
    private Handler mHandler = new Handler();
    @Override
    public String getKey() {
        return "speaker_storage_test";
    }

    @Override
    public String getTestMessage() {
        return getString(R.string.speaker_storage_msg);
    }

    @Override
    public void onStartTest() {
    	isCanPlay = true;
    }

    @Override
    public void onStopTest() {
    	// stopAnything();
    }

    private void stopAnything(){
    	isCanPlay =false;
    	  try {
              mService.stopMusic();
              if (mMyAsyncTask != null){
                  mMyAsyncTask.cancel(true);
                  mMyAsyncTask = null;
              }
              getActivity().unbindService(mServiceConnection);
          }catch (Exception e){
              e.printStackTrace();
          }
        
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
        stopAnything();
    }

    private ISpeakerAndStorageHelper mService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ISpeakerAndStorageHelper.Stub.asInterface(service);
            try {
                TEST_FILE = mService.getSDPath() + "/test.mp3";
                if (!mService.isSdMounted()){
                    showToast(R.string.sd_unmounted);
                }else {
                    if (mService.createTestFile(TEST_FILE)){
                        mService.copyTestToSd(TEST_FILE);
                    }else{
                        showToast(R.string.sd_read_write_error);
                        return;
                    }
                    mMyAsyncTask = new MyAsyncTask(getActivity());
                    mMyAsyncTask.execute();
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

    private Toast mToast;

    private void showToast(int resId){
        if (mToast == null){
            mToast = Toast.makeText(getActivity(),resId,Toast.LENGTH_LONG);
        }else {
            mToast.setText(resId);
            mToast.setDuration(Toast.LENGTH_LONG);
        }
        mToast.show();
    }

    class MyAsyncTask extends AsyncTask<Void,Integer,File>{
        private Context mContext;
        private ProgressDialog mProgressDialog;
        private Runnable mUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    publishProgress(mService.getCopyProgress(TEST_FILE));
                    mHandler.postDelayed(mUpdateRunnable,500);
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }
        };
        public MyAsyncTask(Context context){
            super();
            mContext = context;
        }

        @Override
        protected File doInBackground(Void... params) {
            try {
                mService.copyTestToSd(TEST_FILE);
            }catch (RemoteException e){
                e.printStackTrace();
            }
            return new File(TEST_FILE);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mProgressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMessage(getString(R.string.test_file_copying));
            try(BufferedInputStream bis = new BufferedInputStream(getResources().openRawResource(R.raw.test))){
                mProgressDialog.setMax(bis.available() / 1024);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mProgressDialog.show();
            mHandler.postDelayed(mUpdateRunnable,500);
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            mHandler.removeCallbacks(mUpdateRunnable);
            mProgressDialog.dismiss();
            enableSuccess(true);
            if (file == null){
                showToast(R.string.sd_read_write_error);
            }
            try {
                if(isCanPlay) mService.playMusic(TEST_FILE);
            }catch (RemoteException e){
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mProgressDialog.cancel();
        }
    }
}
