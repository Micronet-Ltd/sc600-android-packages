
package com.lovdream.factorykit.items;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.Main;
import com.lovdream.factorykit.Utils;
import com.lovdream.factorykit.TestItemBase;

public class NFCTest extends TestItemBase{

	@Override
	public String getKey(){
		return "nfc_test";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.nfc_test_mesg);
	}

	@Override
	public void onStartTest(){

        mContext = getActivity();
        mNfcAdapter = NfcAdapter.getDefaultAdapter(mContext);

        if (mNfcAdapter == null) {
            toast(getString(R.string.nfc_not_available));
            fail(null);
            return;
        }
        if (!mNfcAdapter.isEnabled()) {
            logd("To enable NFC");
            //toast(getString(R.string.enabling));
            if (thread.getState() == Thread.State.NEW) {
                thread.start();
            }
        }

        mPendingIntent = PendingIntent.getActivity(mContext, 0,
                new Intent(mContext, getActivity().getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter intentFilter = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        try {
            intentFilter.addDataType("*/*");
        } catch (MalformedMimeTypeException e) {
            e.printStackTrace();
        }
        mIntentFilters = new IntentFilter[] { intentFilter };
        mTechLists = new String[][] { new String[] { NfcF.class.getName() },
                new String[] { NfcA.class.getName() }, new String[] { NfcB.class.getName() },
                new String[] { NfcV.class.getName() }, new String[] { IsoDep.class.getName() },
                new String[] { IsoDep.class.getName() },
                new String[] { NdefFormatable.class.getName() },
                new String[] { MifareClassic.class.getName() },
                new String[] { MifareUltralight.class.getName() } };

        // broadcast
        mIntentFilter = new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);
        mIntentFilter.addAction(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);

        mContext.registerReceiver(mBroadcastReceiver, mIntentFilter);
        mNfcAdapter.enableForegroundDispatch(getActivity(), mPendingIntent, mIntentFilters, mTechLists);
	}

	@Override
	public void onStopTest(){

		try{
			//maybe it already has been unregister
        	mContext.unregisterReceiver(mBroadcastReceiver);
		}catch(Exception e){
			//ignore
		}
		if(getActivity().isResumed()){
        	mNfcAdapter.disableForegroundDispatch(getActivity());
		}
		mCount = 0;
	}

	@Override
	public View getTestView(LayoutInflater inflater){
		View v = inflater.inflate(R.layout.test_mesg_view,null);
		mTextView = (TextView)v.findViewById(R.id.test_mesg_view);
		return v;
	}

    String TAG = Main.TAG;

    TextView mTextView;
    private Context mContext;
    private NfcAdapter mNfcAdapter = null;
    private final int ENABLE_RETRY = 4;

    PendingIntent mPendingIntent;
    IntentFilter[] mIntentFilters;
    IntentFilter mIntentFilter;
    String[][] mTechLists;
	private int mCount = 0;

	@Override
    public void onNewIntent(Intent intent) {

        String action = intent.getAction();
        logd(action);
        if ("android.nfc.action.TECH_DISCOVERED".equals(action)) {
            // [4, 59, -99, 106, 7, 41, -128]
            byte[] id = intent.getByteArrayExtra("android.nfc.extra.ID");
            String hexString = Utils.byteArrayToHexArray(id);
            logd(hexString);
			mCount++;
            showMessage("TAG: " + hexString + " X " + mCount);
            if (hexString.length() > 0)
                quitActionTimer.start();
        }
    };

	private void showMessage(String msg){
		if(mTextView != null){
			mTextView.setText(msg);
		}
	}

    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            int i = 0;
            while (i < ENABLE_RETRY) {

                logd("NfcEnabled=" + mNfcAdapter.isEnabled() + " try=" + i++);
                if (!mNfcAdapter.isEnabled())
                    mNfcAdapter.enable();
                else
                    break;
                SystemClock.sleep(2000);
            }
        }
    };

    Thread thread = new Thread(runnable);

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            logd(action);
            mNfcAdapter = NfcAdapter.getDefaultAdapter(mContext);

            if (NfcAdapter.ACTION_ADAPTER_STATE_CHANGED.equals(action)) {

                int state = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, 0);
                switch (state) {
                case NfcAdapter.STATE_ON:
                    showMessage(getString(R.string.scanning));
                    break;
                case NfcAdapter.STATE_OFF:
                    showMessage(getString(R.string.off));
                    break;
                case NfcAdapter.STATE_TURNING_OFF:
                    showMessage(getString(R.string.turning_off));
                    break;
                case NfcAdapter.STATE_TURNING_ON:
                    showMessage(getString(R.string.turning_on));
                    break;
                default:
                    break;
                }
            }
        }
    };

    private final int QUIT_DELAY_TIME = 1000;

    CountDownTimer quitActionTimer = new CountDownTimer(QUIT_DELAY_TIME, QUIT_DELAY_TIME) {

        @Override
        public void onTick(long arg0) {
        }

        @Override
        public void onFinish() {
            pass();
        }
    };

    void fail(Object msg) {
        loge(msg);
		postFail();
    }

    void pass() {
		postSuccess();
    }

    public void toast(Object s) {

        if (s == null)
            return;
        Toast.makeText(mContext, s + "", Toast.LENGTH_SHORT).show();
    }

    private void logd(Object s) {

        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();

        s = "[" + mMethodName + "] " + s;
        Log.d(TAG, s + "");
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
}
