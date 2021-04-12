
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
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.media.AudioRecord;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.AudioManager;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.TestItemBase;
import com.lovdream.factorykit.libs.VUMeter;
import com.lovdream.factorykit.libs.AudioLoopback;
import android.os.Vibrator;
import android.view.KeyEvent;

public class HeadsetTest extends TestItemBase implements VUMeter.Controller{

	private static final int DEFAULT_JUDGE_VOLUME = 2800;
	private static final int STATE_RECORDING = VUMeter.RECORDING_STATE;
	private static final int STATE_UNPLUG = -1;
         private boolean isTestKey=false;
         private boolean isTestHandset=false;

	AudioLoopback mAudio;
          VUMeter mVUMeter;
	int judgeVolume;
	TextView mStatusText;
	Context mContext;
	AudioManager mAm;

	private int mStatus;

	private BroadcastReceiver headsetReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context,Intent intent){
			if((mAm != null) && mAm.isWiredHeadsetOn()){
				startLoop();
			}else{
				stopLoop();
			}
		}
	};

	@Override
	public String getKey(){
		return "headset_test_nuno";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.headset_test_mesg);
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

		Log.d(TAG,"in headset test,judgeVolume:" + judgeVolume);

		mContext = getActivity();

		mAudio = AudioLoopback.getInstance(mContext);
		
		mAm = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
                   mVibrator = (Vibrator)getActivity().getSystemService(Context.VIBRATOR_SERVICE);

		if(mAm.isWiredHeadsetOn()){
			startLoop();
		} else {
                             mStatus = STATE_UNPLUG;
                   }
		updateState();

		mContext.registerReceiver(headsetReceiver,new IntentFilter(AudioManager.ACTION_HEADSET_PLUG));
	}

	@Override
	public void onStopTest(){
		stopLoop();
		mAudio.release();
		mContext.unregisterReceiver(headsetReceiver);
                    isTestKey=false;
                    isTestHandset=false;
	}
         TextView mKeyButton;

	@Override
	public View getTestView(LayoutInflater inflater){
		View v = inflater.inflate(R.layout.headset_sub_test,null);
                   mVUMeter = (VUMeter) v.findViewById(R.id.uvMeter);
		mVUMeter.setController(this);
		mStatusText = (TextView) v.findViewById(R.id.stateMessage);
		updateState();
                   
                   mKeyButton = v.findViewById(R.id.key_name_headset);
		return v;
	}

	private void startLoop(){
		mAudio.startLoopback();
		mStatus = STATE_RECORDING;
		updateState();
		resetAngle();
	}

	private void stopLoop(){
		mAudio.stopLoopback();
		mStatus = STATE_UNPLUG;
		updateState();
		resetAngle();
	}

	private void updateState(){
		if(mStatusText != null){
			mStatusText.setText(mStatus == STATE_UNPLUG ? R.string.headset_unplugged : R.string.recording);
		}
	}

	private void resetAngle(){
		if(mVUMeter != null){
			mVUMeter.resetAngle();
		}
	}

	@Override
	public int state(){
		return mStatus;
	}
	
	@Override
	public int getMaxAmplitude(){
		int value = 0;
		if(mAudio != null){
			value = mAudio.getMaxAmplitude();
		}

		if(value > judgeVolume){
                             isTestHandset=true;
                             if(isTestKey)
			    enableSuccess(true);
		}

		return value;
	}
    
    private Vibrator mVibrator;
        	public boolean onKeyUp(int keyCode, KeyEvent keyevent) {
		Log.d(TAG,"in key test,keyevent:" + keyevent);

		if(mVibrator != null){
			mVibrator.vibrate(200);
		}

		if(keyevent.getKeyCode()== KeyEvent.KEYCODE_HEADSETHOOK){
			mKeyButton.setVisibility(View.INVISIBLE);
                             isTestKey = true;
                             if (isTestHandset)
                                enableSuccess(true);
                             return true;
		}
                   
		return false;
	}
}
