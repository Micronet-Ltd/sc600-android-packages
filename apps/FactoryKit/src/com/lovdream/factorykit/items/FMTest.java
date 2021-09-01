package com.lovdream.factorykit.items;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioSystem;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import android.os.Handler;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.Main;
import com.lovdream.factorykit.TestItemBase;
import com.lovdream.factorykit.libs.FmManager;

public class FMTest extends TestItemBase implements Runnable{

	@Override
	public String getKey(){
		return "fm_test";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.fm_test_mesg);
	}

	@Override
	public void onStartTest(){
		mContext = getActivity();
		init();
		setAudio();

		if (!mAudioManager.isWiredHeadsetOn()) {
			enableSuccess(false);
			showWarningDialog(getString(R.string.fm_insert_headset));
		}
		if (!mRunning){
			mHandler.postDelayed(this,0);
		}
	}

	@Override
	public void onStopTest(){
		closeFM();
	}
	
	@Override
	public View getTestView(LayoutInflater inflater){
		View v = inflater.inflate(R.layout.fm_test,null);
		bindView(v);
		return v;
	}

	static String TAG = Main.TAG;
	Button searchButton;
	TextView mTextView;

	AudioManager mAudioManager = null;
	FmManager mFmManager = null;
	Context mContext = null;
	boolean forceHeadset = false;
	boolean mRunning = false;

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				mTextView.setText(new Float(mFmManager.getFrequency() / 1000f)
						.toString() + "MHZ");
				break;

			default:
				break;
			}
		};
	};

	public void closeFM() {

		mFmManager.closeFM();
		mRunning = false;
		if (forceHeadset) {
			AudioSystem.setDeviceConnectionState(
					AudioSystem.DEVICE_OUT_WIRED_HEADSET,
					AudioSystem.DEVICE_STATE_UNAVAILABLE, "","");
			AudioSystem.setForceUse(AudioSystem.FOR_MEDIA,
					AudioSystem.FORCE_NONE);
		}
	}

	void init() {

		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		mFmManager = new FmManager(mContext, mHandler);
	}

	void bindView(View v) {

		searchButton = (Button) v.findViewById(R.id.fm_search);

		mTextView = (TextView) v.findViewById(R.id.fm_frequency);
		mTextView.setText("87.5" + "MHZ");

		searchButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				if (mAudioManager.isWiredHeadsetOn()) {
					enableSuccess(true);
					mFmManager.searchUP();
				} else {
					enableSuccess(false);
					showWarningDialog(getString(R.string.fm_insert_headset));
				}
			}
		});
	}

	public void setAudio() {

		mAudioManager.setMode(AudioManager.MODE_NORMAL);
		// Force headset's check
		if (forceHeadset) {
			AudioSystem.setDeviceConnectionState(
					AudioSystem.DEVICE_OUT_WIRED_HEADSET,
					AudioSystem.DEVICE_STATE_AVAILABLE, "","");
			AudioSystem.setForceUse(AudioSystem.FOR_MEDIA,
					AudioSystem.FORCE_WIRED_ACCESSORY);
			mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC,
					AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
		}
		float ratio = 0.8f;

		mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM,
				(int) (ratio * mAudioManager
						.getStreamMaxVolume(AudioManager.STREAM_ALARM)), 0);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
				(int) (ratio * mAudioManager
						.getStreamMaxVolume(AudioManager.STREAM_MUSIC)), 0);
		mAudioManager
				.setStreamVolume(
						AudioManager.STREAM_VOICE_CALL,
						(int) (ratio * mAudioManager
								.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)),
						0);
		mAudioManager.setStreamVolume(AudioManager.STREAM_DTMF,
				(int) (ratio * mAudioManager
						.getStreamMaxVolume(AudioManager.STREAM_DTMF)), 0);
		mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,
				(int) (ratio * mAudioManager
						.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)),
				0);
		mAudioManager.setStreamVolume(AudioManager.STREAM_RING,
				(int) (ratio * mAudioManager
						.getStreamMaxVolume(AudioManager.STREAM_RING)), 0);
		mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,
				(int) (ratio * mAudioManager
						.getStreamMaxVolume(AudioManager.STREAM_SYSTEM)), 0);
	}

	@Override
	public void run() {
		mRunning = true;
		mFmManager.openFM();
	};

	void showWarningDialog(String title) {

		new AlertDialog.Builder(mContext)
				.setTitle(title)
				.setPositiveButton(getString(android.R.string.ok),
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {

							}
						}).setCancelable(false).show();

	}
}
