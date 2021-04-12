
package com.lovdream.factorykit.items;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioSystem;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.Utils;
import com.lovdream.factorykit.TestItemBase;
import com.lovdream.factorykit.libs.VUMeter;
import com.lovdream.factorykit.libs.AudioLoopback;

public class HandsetTest extends TestItemBase implements VUMeter.Controller{

	private static final String MAIN_MIC_SWITCH = "/sys/class/fm2018/function/ fm2018_main_mic_switch";

	private static final int DEFAULT_JUDGE_VOLUME = 2800;
	private static final int STATE_RECORDING = VUMeter.RECORDING_STATE;

	Context mContext;
	AudioManager mAudioManager;
	AudioLoopback mAudio;
    VUMeter mVUMeter;
	int judgeVolume;
	int state = -1;
	boolean useSpeaker = false;

	BroadcastReceiver headsetReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent){
			if(Intent.ACTION_HEADSET_PLUG.equals(intent.getAction())){
				if(intent.getIntExtra("state", 0) == 0){
					startLoop();
					mContext.unregisterReceiver(this);
				}
			}
		}
	};

	@Override
	public String getKey(){
		return "handset_loopback";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.handset_test_mesg);
	}

	@Override
	public void onStartTest(){

		String arg[] = getParameter("volume");
		if(arg != null){
			try{
				judgeVolume = Integer.valueOf(arg[0]);
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		if(judgeVolume == 0){
			judgeVolume = DEFAULT_JUDGE_VOLUME;
		}

		arg = getParameter("useSpeaker");
		if(arg != null){
			try{
				useSpeaker = Boolean.valueOf(arg[0]);
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		Log.d(TAG,"in handset test,judgeVolume:" + judgeVolume + " useSpeaker:" + useSpeaker);

		mContext = getActivity();

		mAudio = AudioLoopback.getInstance(mContext);
		mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);

		setAudio();

		if(!mAudioManager.isWiredHeadsetOn()){
			startLoop();
		}else{
			mContext.registerReceiver(headsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
			toast(R.string.headset_remove);
		}
	}

	@Override
	public void onStopTest(){
		stopLoop();
		mAudio.release();
		
		mAudioManager.setMode(AudioManager.MODE_NORMAL);
		AudioSystem.setForceUse(AudioSystem.FOR_COMMUNICATION,AudioSystem.FORCE_NONE);

		if((new File(MAIN_MIC_SWITCH)).exists()){
			Utils.writeFile(MAIN_MIC_SWITCH,"1");
		}
	}

	protected String getMicSwitchFile(){
		return MAIN_MIC_SWITCH;
	}

	@Override
	public View getTestView(LayoutInflater inflater){
		View v = inflater.inflate(R.layout.handset_test,null);
        mVUMeter = (VUMeter) v.findViewById(R.id.uvMeter);
		mVUMeter.setController(this);

		//ensure to collect some voice
		enableSuccess(false);

		return v;
	}

	private void resetAngle(){
		if(mVUMeter != null){
			mVUMeter.resetAngle();
		}
	}

	@Override
	public int state(){
		return state;
	}

	@Override
	public int getMaxAmplitude(){
		int value = 0;
		if(mAudio != null){
			value = mAudio.getMaxAmplitude();
		}

		if(value > judgeVolume){
			enableSuccess(true);
		}

		return value;
	}

	private void startLoop(){
		mAudio.startLoopback();
		state = STATE_RECORDING;
		resetAngle();
	}

	private void stopLoop(){
		mAudio.stopLoopback();
		resetAngle();
		state = -1;
	}

	public void setAudio() {

		mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
		mAudioManager.setSpeakerphoneOn(useSpeaker);
		AudioSystem.setForceUse(AudioSystem.FOR_COMMUNICATION,
				AudioSystem.FORCE_HEADPHONES);

		float ratio = 0.2f;

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
}
