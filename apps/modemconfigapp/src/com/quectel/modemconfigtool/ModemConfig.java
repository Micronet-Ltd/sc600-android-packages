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
import com.quectel.modemtool.ModemTool;
import static com.quectel.modemtool.NvConstants.REQUEST_SEND_AT_COMMAND;

public class ModemConfig {
	public static final String TAG = "modemconfigtool-ModemConfig";

	Context mContext;
	NvManager mNvManager;
    private int checkVersionId,checkATTmccmnc;
    private ModemTool mModemTool;

	public ModemConfig(Context context) {
		mContext = context;

		mNvManager = new NvManager(context);
        checkVersionId = R.string.at_command_version;
        checkATTmccmnc = R.string.at_command_mccmnc;
	}

	void startMbnUpdateService(){
		Intent intent_mls = new Intent(mContext, com.quectel.modemconfigtool.MbnUpdateService.class);
		mContext.startService(intent_mls);
	}


	void startNvProcessThread(){
		new Thread(new Runnable() {
			@Override
			public void run() {
			/*
				int verifyResult = mNvManager.startVerify(R.array.at_command_get_array, R.array.at_command_get_array_value);
				switch (verifyResult){
					case Common.EVENT_VERIFY_SUCCESS:
						Log.d(TAG, "nv config is right");
						break;
					case Common.EVENT_VERIFY_CODE_VALUE_NOT_MATCH:
						Log.d(TAG, "nv config is not right, need reconfig nv");
						startNvWrite();
						break;
					case Common.EVENT_VRIFY_FAIL_TO_SEND_AT_COMMAND:
						Log.d(TAG, "fail to send at command, retry");
						startNvWrite();
						break;
					default:
						Log.d(TAG, "unexpect return value");
				}
              */
                //add by joe for disable att voice
                disableVoiceForATTsim();

			}
		}).start();
	}

	void startNvWrite(){
		if (mNvManager.startWrite(R.array.at_command_set_array) == Common.EVENT_WRITE_SUCCESS){
			Log.d(TAG, "Success: all nv all be configed successfully");
		}
	}

	void startATTVoiceNvWrite(){
		if (mNvManager.startWrite(R.array.at_command_set_array_attvoice) == Common.EVENT_WRITE_SUCCESS){
			Log.d(TAG, "Success: att disable voice nv all be configed successfully");
		}
	}

    public void disableVoiceForATTsim(){
        //if(!isATTmccmnc()){
        //    return ;
        //}
        int verifyATTResult = mNvManager.startVerify(R.array.at_command_get_array_attvoice, R.array.at_command_get_array_value_attvoice);
        switch (verifyATTResult){
            case Common.EVENT_VERIFY_SUCCESS:
                Log.d(TAG, " ATT VOICE nv config is right");
                break;
            case Common.EVENT_VERIFY_CODE_VALUE_NOT_MATCH:
                Log.d(TAG, "ATT VOICE nv config is not right, need reconfig nv");
                startATTVoiceNvWrite();
                break;
            case Common.EVENT_VRIFY_FAIL_TO_SEND_AT_COMMAND:
                Log.d(TAG, "ATT VOICE fail to send at command, retry");
                startATTVoiceNvWrite();
                break;
            default:
                Log.d(TAG, "unexpect return value");
        }

    }

    public boolean isATTmccmnc(){
        String[] mATTmccmncVeriryValues;
        mATTmccmncVeriryValues = mContext.getResources().getStringArray(R.array.at_command_get_array_value_att_mccmnc);

        String result_values = sendAtCommand(checkATTmccmnc);
        if (result_values!=null && result_values.contains("OK")){
            
            for(int i = 0; i < mATTmccmncVeriryValues.length; i++){
                if(result_values.contains(mATTmccmncVeriryValues[i])){
                    Log.d(TAG, "ATT SIM mcc mnc match, return ture");
                        return true;
                }
                    
            }

        }
        Log.d(TAG, " ATT SIM mcc mnc mis-match, return false");
        return false;
    }

    private String sendAtCommand(int commandid) {
        if (mModemTool == null) {
            mModemTool = new ModemTool();
        }
        String atCommand = mContext.getResources().getString(commandid);
        String result = mModemTool.sendAtCommand(REQUEST_SEND_AT_COMMAND, atCommand);
        Log.d(TAG, "sendAtCommand: " + result);
        if (result == null) {
            Log.d(TAG, "sendAtCommand command fail atString: " + atCommand);
        }
        return result;
    }
}
