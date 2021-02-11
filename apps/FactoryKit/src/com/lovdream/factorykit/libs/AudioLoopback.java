
package com.lovdream.factorykit.libs;

import android.content.Context;
import android.media.AudioRecord;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.util.Log;

public class AudioLoopback extends Thread{

	private static final String TAG = "audio_loopback";
	
	private static final int SAMPLE_RATE = 8000;
	private static final int BUF_SIZE = 1024;

	private static AudioLoopback mInstance;
	private Context mContext;

	private AudioRecord mRecord;
	private AudioTrack mTrack;
	private byte[] mBuffer;
	private int mBufferSize;
	private int mMaxAmplitude;

	private boolean isLoop = false;
	private boolean exitFlag = true;

	private AudioLoopback(Context content){
		super();
		mContext = content;

		mBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);
		mBufferSize = Math.max(mBufferSize,AudioTrack.getMinBufferSize(SAMPLE_RATE,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT));
		mBufferSize = Math.max(mBufferSize, BUF_SIZE);
		Log.d(TAG,"mBufferSize:" + mBufferSize);
		mBuffer = new byte[mBufferSize];

		start();
	}

	public static AudioLoopback getInstance(Context context){
		if(mInstance == null){
			mInstance = new AudioLoopback(context);
		}

		return mInstance;
	}

	public void release(){
		stopLoopback();

		exitFlag = false;
		mInstance = null;
	}

	public void startLoopback(){
		if(isLoop){
			return;
		}
		
		/*
		AudioManager service = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
		service.setSpeakerphoneOn(true);
		*/

		mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,mBufferSize);
		mTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL,SAMPLE_RATE,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT,mBufferSize,AudioTrack.MODE_STREAM);
		mTrack.setPlaybackRate(SAMPLE_RATE);

		mRecord.startRecording();
		mTrack.play();

		synchronized(this){
			isLoop = true;
			this.notify();
		}
	}

	public void stopLoopback(){
		if(!isLoop){
			return;
		}

		synchronized(this){
			isLoop = false;
		}

		/*
		AudioManager service = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
		service.setSpeakerphoneOn(false);
		*/

		mRecord.stop();
		mRecord.release();
		mRecord = null;
		mTrack.stop();
		mTrack.release();
		mTrack = null;
	}

	@Override
	public void run(){
		while(exitFlag){
			synchronized(this){
				if(!isLoop){
					try{
						wait();
					}catch(Exception e){
						e.printStackTrace();
					}
				}

			}
			
			if((mRecord != null) && (mTrack != null)){
				int readSize = mRecord.read(mBuffer,0,mBufferSize);
				if(readSize > 0){
					trackMaxAmplitude(mBuffer);
					mTrack.write(mBuffer,0,readSize);
				}
			}

		}
	}

	public int getMaxAmplitude(){
		int value = mMaxAmplitude;
		mMaxAmplitude = 0;
		return value;
	}

	private void trackMaxAmplitude(byte[] buffer){
		int value = 0;
		for(int i = buffer.length - 1;i >= 0; --i){
			value += buffer[i] * buffer[i];
		}

		value = value / buffer.length;

		if(mMaxAmplitude < value){
			mMaxAmplitude = value;
		}
	}
}
