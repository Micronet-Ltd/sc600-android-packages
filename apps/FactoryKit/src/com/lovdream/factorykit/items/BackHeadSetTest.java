package com.lovdream.factorykit.items;

import java.io.FileOutputStream;

import com.lovdream.factorykit.MusicService;
import com.lovdream.factorykit.R;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;

import com.lovdream.factorykit.TestItemBase;
import com.swfp.utils.ProjectControlUtil;

public class BackHeadSetTest extends TestItemBase {
	


	private Context mContext;
	
	
	MusicService musicService;
	ServiceConnection conn;
    AudioManager mAudioManager;

	@Override
	public String getKey() {
		return "back_headset";
	}

	@Override
	public String getTestMessage() {
		return getActivity().getString(R.string.back_headset_mesg);
	}

	@Override
	public void onStartTest() {
		

	}
	
	
	@Override
	public void onStop() {
		super.onStop();
		stopPlay();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mContext =getActivity();
		mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
		startPlay();
	}
	
	private class MusicConnector implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            musicService = ((MusicService.MyMusicBinder) iBinder).getService();

        }
        //不成功绑定时调用
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicService = null;
            Log.i("xxfggg", "binding is fail");
        }
    }
	
	
	
	private void startPlay(){
	    Intent intent = new Intent();
        intent.setClass(mContext, MusicService.class);
        if(conn==null) conn= new MusicConnector();
        mContext.bindService(intent, conn, Context.BIND_AUTO_CREATE);
	}
	
	private void stopPlay(){
		 try {
			 mContext.unbindService(conn);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onStopTest() {
		
	}
}
