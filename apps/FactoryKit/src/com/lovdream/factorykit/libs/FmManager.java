package com.lovdream.factorykit.libs;

//import qcom.fmradio.FmConfig;
//import qcom.fmradio.FmReceiver;
//import qcom.fmradio.FmRxEvCallbacksAdaptor;
import android.content.Context;
import android.content.Intent;
import android.media.AudioSystem;
import android.os.Handler;
import android.util.Log;

public class FmManager {

//	public static FmReceiver mReceiver = null;
	private static final String RADIO_DEVICE = "/dev/radio0";
	public static final String ACTION_FM = "codeaurora.intent.action.FM";
	private static boolean mFmOn = false;
	static String TAG = "FM";
	private int frequency = 87500;
	Context mContext = null;
	Handler mHandler;

    static {
		System.loadLibrary("qcomfm_jni");
    }

	public FmManager(Context context, Handler handler) {

		mContext = context;
		mHandler = handler;
	}

	public int getFrequency() {
		return frequency;
	}

	public boolean openFM() {

		boolean ret = false;
		return ret;
	}

	public boolean closeFM() {

		boolean ret = false;

		return ret;
	}

	public boolean isFmOn() {

		return mFmOn;
	}

	public int searchUP() {

		return getFrequency();
	}

	public int searchDown() {

		return getFrequency();
	}

	private void loge(Object e) {

		if (e == null)
			return;
		Thread mThread = Thread.currentThread();
		StackTraceElement[] mStackTrace = mThread.getStackTrace();
		String mMethodName = mStackTrace[3].getMethodName();
		e = "[" + mMethodName + "] " + e;
		Log.e(TAG, e + "");
	}

	private void logd(Object s) {

		Thread mThread = Thread.currentThread();
		StackTraceElement[] mStackTrace = mThread.getStackTrace();
		String mMethodName = mStackTrace[3].getMethodName();

		s = "[" + mMethodName + "] " + s;
		Log.d(TAG, s + "");
	}

}
