
package com.lovdream.factorykit.items;

import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.LayoutInflater;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.Main;
import com.lovdream.factorykit.TestItemBase;

public class LCDTest extends TestItemBase implements View.OnTouchListener{

	@Override
	public String getKey(){
		return "lcd_test";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.lcd_test_mesg);
	}

	@Override
	public void onStartTest(){

		mContext = getActivity();

		LayoutInflater inflater = LayoutInflater.from(mContext);

		mView = inflater.inflate(R.layout.lcd_test,null);
		//mView.setOnTouchListener(this);
		showFullscreenOverlay(mView,true);

		mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(
				PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "BackLight");

		imgSeq = 0;
		setBackgroud(0);
		wakeLock();
		start();
	}

	@Override
	public void onStopTest(){
		wakeUnlock();
		dismissOverlay();
		mHandler.removeCallbacks(mRunnable);
	}


	private Handler mHandler = new Handler();
	private int imgSeq = 0;
	private boolean ifLocked = false;
	private PowerManager.WakeLock mWakeLock;
	private PowerManager mPowerManager;
	private View mView;

	private static final String TAG = Main.TAG;
	private static Context mContext;

	private int[] mTestImg = { R.drawable.lcm_red, R.drawable.lcm_green,
			R.drawable.lcm_blue, R.drawable.lcm_black,R.drawable.lcm_white };

	public void start() {

		mHandler.removeCallbacks(mRunnable);
		mHandler.postDelayed(mRunnable, 0);
	}

	private Runnable mRunnable = new Runnable() {

		public void run() {

			if (imgSeq < mTestImg.length) {
				setBackgroud(imgSeq);
				imgSeq++;
			}else{
				imgSeq = 0;
				dismissOverlay();
			}
			mHandler.postDelayed(mRunnable, 1600);
		}
	};

	private void setBackgroud(int index) {
		if (index >= mTestImg.length)
			return;

		try {
			mView.setBackgroundDrawable(mContext.getDrawable(mTestImg[index]));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onTouch(View v,MotionEvent event) {

		final int mAction = event.getAction();
		if ((mAction == MotionEvent.ACTION_UP)) {
			if (imgSeq < mTestImg.length) {
				setBackgroud(imgSeq);
				imgSeq++;
			}
		}
		if (imgSeq >= mTestImg.length) {
			imgSeq = 0;
			dismissOverlay();
		} else
			setBackgroud(imgSeq);
		return true;
	}

	private void wakeLock() {

		if (!ifLocked) {
			ifLocked = true;
			mWakeLock.acquire();
		}
	}

	private void wakeUnlock() {

		if (ifLocked) {
			mWakeLock.release();
			ifLocked = false;
		}
	}

}
