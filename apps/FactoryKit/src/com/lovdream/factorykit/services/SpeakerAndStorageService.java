package com.lovdream.factorykit.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;

import com.lovdream.factorykit.services.ISpeakerAndStorageHelper;
import com.lovdream.factorykit.R;

/**
 * Created by yangzhiming on 2017/6/21.
 */

public class SpeakerAndStorageService extends Service {
    private final IBinder mBinder = new SpeakerAndStorageHelperImpl(this);
    private StorageManager mStorageManager;
    private static final String TAG = "SpeakerStorageService";
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public boolean createTestFile(String path){
        Log.d(TAG,"createTestFile : " + path);
        boolean isCreated = false;
        File file = new File(path);
        if (!file.exists()){
            try {
                isCreated = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            isCreated = true;
        }
        return isCreated;
    }

    public boolean isSdMounted(){
        if (mStorageManager == null){
            mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        }
        StorageVolume[] storageVolumes = mStorageManager.getVolumeList();
        return storageVolumes.length > 1 && storageVolumes[1].getState().equals(Environment.MEDIA_MOUNTED);
    }
    public String getSDPath(){
        if (isSdMounted()){
            StorageVolume[] storageVolumes = mStorageManager.getVolumeList();
            return storageVolumes[1].getPath();
        }
        return null;
    }

    public boolean copyTestToSd(String toPath){
        Log.d(TAG,"copyTestToSd : " + toPath);
        boolean isSucceed = false;
        File file = new File(toPath);
        try (BufferedInputStream bis = new BufferedInputStream(getResources().openRawResource(R.raw.test));
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))){
            byte[] b = new byte[1024];
            while (bis.read(b) != -1){
                bos.write(b);
            }
            isSucceed = true;
        } catch (IOException e) {
            e.printStackTrace();
            isSucceed = false;
        }
        Log.d(TAG,"copyTestToSd : " + "done");
        return isSucceed;
    }

    File mTestFile;
    public int getCopyProgress(String path){
        if (mTestFile == null){
            mTestFile = new File(path);
        }
        int progress = (int) (mTestFile.length() / 1024);
        return progress;
    }

    private MediaPlayer mMediaPlayer;
    public void playMusic(String path){
        Log.d(TAG,"playMusic : " + path);
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void stopMusic(){
        if (mMediaPlayer != null){
            mMediaPlayer.stop();
			mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }




    static class SpeakerAndStorageHelperImpl extends ISpeakerAndStorageHelper.Stub{
        private SoftReference<SpeakerAndStorageService> mService;
        SpeakerAndStorageHelperImpl(SpeakerAndStorageService service){
            mService = new SoftReference<SpeakerAndStorageService>(service);
        }
        public void playMusic(String path){
            mService.get().playMusic(path);
        }
        public void stopMusic(){
            mService.get().stopMusic();
        }
        public boolean isSdMounted(){
            return mService.get().isSdMounted();
        }
        public boolean copyTestToSd(String toPath){
            return mService.get().copyTestToSd(toPath);
        }
        public boolean createTestFile(String path){
                return mService.get().createTestFile(path);
        }
        public int getCopyProgress(String path){
            return mService.get().getCopyProgress(path);
        }
        public String getSDPath(){
            return mService.get().getSDPath();
        }
    }



}
