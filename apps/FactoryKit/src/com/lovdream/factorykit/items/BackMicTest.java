package com.lovdream.factorykit.items;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;






import com.lovdream.factorykit.MusicService;
import com.lovdream.factorykit.R;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lovdream.factorykit.TestItemBase;
import com.swfp.utils.ProjectControlUtil;

public class BackMicTest extends NoiseMicTest {

	private static final String GPIO_NODE = "/sys/class/ext_dev/function/pogo_irq";
	boolean isCanFinish = false;
	
	Context mContext;
	
	


	@Override
	public String getKey() {
		return "back_mic";
	}

	@Override
	public String getTestMessage() {
		return getActivity().getString(R.string.back_mic_mesg);
	}
	
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		isCanFinish =true;
	}

	@Override
	public void onStartTest() {
		super.onStartTest();
		final Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (isCanFinish) {
					timer.cancel();
					cancel();
				}
				Log.d("xxfkkk", "state--->" + getMicInfo(GPIO_NODE));
				try {
					if ("1".equals(getMicInfo(GPIO_NODE))) {
						enableSuccess(true);
						isCanFinish = true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 100, 1500);

	}

	@Override
	public void onResume() {
		super.onResume();
		enableSuccess(false);
		mContext =getActivity();
	}
	



	private String getMicInfo(String path) {
		File mFile;
		FileReader mFileReader;
		mFile = new File(path);

		try {
			mFileReader = new FileReader(mFile);
			char data[] = new char[128];
			int charCount;
			String status[] = null;
			try {
				charCount = mFileReader.read(data);
				status = new String(data, 0, charCount).trim().split("\n");
				return status[0];
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}
