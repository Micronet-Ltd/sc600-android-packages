package com.quectel.modemconfigtool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.app.Activity;
import android.content.Intent;

import android.os.SystemProperties;


public class AutoStartBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = "modemconfigtool-AutoStartBroadcastReceiver";
    private static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    String mBaseBandVersion = SystemProperties.get("gsm.version.baseband", "null");

    ModemConfig mModemConfig;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "AutoStartBroadcastReceiver onReceive, intent.getAction() = " + intent.getAction());
        boolean mLoadMbn = context.getResources().getBoolean(R.bool.config_mbns);
        boolean mSendAt = context.getResources().getBoolean(R.bool.config_at_commands);
        boolean mUiEnable = context.getResources().getBoolean(R.bool.ui_enable);

        mModemConfig = new ModemConfig(context);

        Log.d(TAG, "mBaseBandVersion = " + mBaseBandVersion + ", mLoadMbn = " + mLoadMbn + ", mSendAt = " + mSendAt + ", mUiEnable = " + mUiEnable);
        if (isRightVersion() && !mUiEnable && intent.getAction().equals(ACTION)) {
            if (mLoadMbn){
                Log.d(TAG, "start MbnUpdateService");
                mModemConfig.startMbnUpdateService();
            }

            if (mSendAt){
                Log.d(TAG, "startNvProcessThread");
                mModemConfig.startNvProcessThread();
            }
        }
    }

    boolean isRightVersion(){
        boolean isRightVersion;
        String mBaseBandVersion = SystemProperties.get("gsm.version.baseband", "invalid");
        //fix the issue that app may not get baseband version during the first boot up after re-flash the device.
        for (int i = 0; i< 10 && mBaseBandVersion == "invalid"; i++){
            try{
                Thread.sleep(2000);
            }catch(Exception e){
                e.printStackTrace();
            }
            mBaseBandVersion = SystemProperties.get("gsm.version.baseband", "invalid");
            Log.d(TAG, "mBaseBandVersion = " + mBaseBandVersion + ", i = " + i);
        }
        Log.d(TAG, "mBaseBandVersion = " + mBaseBandVersion);
        if (mBaseBandVersion != null){
            isRightVersion = mBaseBandVersion.contains("SC600YNA");
        } else {
            isRightVersion = true;
        }
        Log.d(TAG, "isRightVersion = " + isRightVersion);
        return isRightVersion;
    }

}

