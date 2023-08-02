
package com.lovdream.factorykit.items;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.MotionEvent;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.media.AudioRecord;
import android.media.AudioFormat;
import android.media.AudioSystem;
import android.media.AudioTrack;
import android.media.AudioManager;
import android.os.Environment;

import java.io.File;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.TestItemBase;

public class HeadsetTestAsync extends TestItemBase implements View.OnTouchListener,OnCompletionListener{

	Context mContext;
	AudioManager mAudioManager;
	View mTalkButton;

	private String mPath;
	MediaRecorder mRecorder;
	MediaPlayer mPlayer;

	private void deleteTmpFile(){
		if (mPath==null) return;
		File f = new File(mPath);
		if(f.exists()){
			f.delete();
		}
	}

	private void startRecord(){
		if(mRecorder == null){
			return;
		}
		deleteTmpFile();
		try{
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mRecorder.setOutputFile(mPath);
			mRecorder.prepare();
			mRecorder.start();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private void stopRecord(){
		if(mRecorder == null){
			return;
		}

		try{
			mRecorder.stop();
		}catch(Exception e){
			toast(R.string.record_short);
			deleteTmpFile();
		}
		mRecorder.reset();
	}

	private void startPlay(){
		if((mPath == null) || (!new File(mPath).exists())){
			return;
		}

		mPlayer.reset();
		try{
			mPlayer.setDataSource(mPath);
			mPlayer.prepare();
		}catch(Exception e){
			e.printStackTrace();
			toast(R.string.fail_to_play);
		}
		mPlayer.start();
	}

	private void stopPlay(){
		if((mPlayer == null) || !mPlayer.isPlaying()){
			return;
		}

		mPlayer.stop();
	}
	
	@Override
	public void onCompletion(MediaPlayer mPlayer){
		deleteTmpFile();
		enableSuccess(true);
	}

	private BroadcastReceiver headsetReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context,Intent intent){
			boolean isEnabled = false;
			if((mAudioManager != null) && mAudioManager.isWiredHeadsetOn()){
				isEnabled = true;
			}

			if(mTalkButton != null){
				//mTalkButton.setEnabled(isEnabled);
			}
		}
	};

	@Override
	public String getKey(){
		return "mic_loop";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.headset_test_async_mesg);
	}

	@Override
	public void onStartTest(){
		mContext = getActivity();
		mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
		mPath = getPathToStorage(); //Environment.getExternalStorageDirectory().getAbsolutePath()+ "/test.3gp";
		if (mPath==null) {
			if (mTalkButton!=null) mTalkButton.setEnabled(false);
			enableSuccess(false);
			toast(R.string.sd_problem);
		}

		mRecorder = new MediaRecorder();
		
		mPlayer = new MediaPlayer();
		mPlayer.setOnCompletionListener(this);

		mContext.registerReceiver(headsetReceiver,new IntentFilter(AudioManager.ACTION_HEADSET_PLUG));
		setMaxVolum();
	}

	@Override
	public void onStopTest(){

		mRecorder.release();
		mRecorder = null;
		stopPlay();
		mPlayer.release();
		mPlayer = null;
		deleteTmpFile();
		mContext.unregisterReceiver(headsetReceiver);
	}

	@Override
	public View getTestView(LayoutInflater inflater){
		View v = inflater.inflate(R.layout.headset_test_async,null);
		mTalkButton = v.findViewById(R.id.push_to_talk);
		mTalkButton.setEnabled(true);
		mTalkButton.setOnTouchListener(this);
		return v;
	}

	@Override
	public boolean onTouch(View v,MotionEvent event){
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			stopPlay();
			startRecord();
		}else if(event.getAction() == MotionEvent.ACTION_UP){
			stopRecord();
			startPlay();
		}
		return false;
	}
	public void setMaxVolum() {
		float ratio = 1f;

		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
				(int) (ratio * mAudioManager
						.getStreamMaxVolume(AudioManager.STREAM_MUSIC)), 0);
		mAudioManager
				.setStreamVolume(
						AudioManager.STREAM_VOICE_CALL,
						(int) (ratio * mAudioManager
								.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)),
						0);
		mAudioManager.setStreamVolume(AudioManager.STREAM_RING,
				(int) (ratio * mAudioManager
						.getStreamMaxVolume(AudioManager.STREAM_RING)), 0);
		mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,
				(int) (ratio * mAudioManager
						.getStreamMaxVolume(AudioManager.STREAM_SYSTEM)), 0);

	}
	
	private String getPathToStorage() {
        File[] externalDirs = getActivity().getExternalFilesDirs(null);
        File inner = null,sd = null;
        String internalRoot = Environment.getExternalStorageDirectory().getAbsolutePath().toLowerCase();
        for (File file : externalDirs) {
            if (file == null) continue;
            if (file.getPath().toLowerCase().startsWith(internalRoot)) {
                inner=file;
            } else {
                sd=file;
            }
        }
        if (sd!=null) sd.mkdirs();
        return sd==null?null:sd.getAbsolutePath()+"/test.3gp";
    }
}
