package com.swfp.utils;

import android.content.Context;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.lovdream.factorykit.R;
public class SimUtil {

	
	static SimUtil mSimUtil;
	private Context mContext;
	private SimUtil(Context context){
		mContext = context;
	}
	
	public static SimUtil getInstance(Context context){
		if(mSimUtil==null){
			mSimUtil = new SimUtil(context);
		}
		return mSimUtil;
	}
	
	private static final String TAG = "SimUtil";

	public SimInfo searchSim() {
	        Log.w(TAG, "initSIMTest");
	        TelephonyManager tm = TelephonyManager.getDefault();
	        //int count = tm.isMultiSimEnabled() ? 2 : 1;
	        boolean result = true;
	        SimInfo mSimInfo = new SimInfo();
	        //for (int i = 0; i < count; i++) {
	            int type = tm.getNetworkType();
	            int state = tm.getSimState();
	            mSimInfo.simMsg= mContext.getString(R.string.sim_status_label,
	                    cardTypeToString(type), cardStateToString(state));
	            result &= (state != TelephonyManager.SIM_STATE_ABSENT);
	            result &= (state != TelephonyManager.SIM_STATE_UNKNOWN);
	            result &= (state != TelephonyManager.SIM_STATE_NOT_READY);
	            
	        //}
	        mSimInfo.isSimReady = result;
	        return mSimInfo;
	    }
	
	public class SimInfo {
		public boolean isSimReady;
		public String simMsg;
	}
	
    private String cardTypeToString(int type) {
        switch (type) {
        case TelephonyManager.NETWORK_TYPE_UMTS:
            return "USIM";
        case TelephonyManager.NETWORK_TYPE_CDMA:
        case TelephonyManager.NETWORK_TYPE_EVDO_0:
        case TelephonyManager.NETWORK_TYPE_EVDO_A:
        case TelephonyManager.NETWORK_TYPE_EVDO_B:
        case TelephonyManager.NETWORK_TYPE_1xRTT:
        case TelephonyManager.NETWORK_TYPE_LTE:
            return "UIM";
        }
        return "SIM";
    }

    private String cardStateToString(int state) {
        switch (state) {
        case TelephonyManager.SIM_STATE_ABSENT:
            return mContext.getString(R.string.sim_status_no_card);
        case TelephonyManager.SIM_STATE_PIN_REQUIRED:
            return mContext.getString(R.string.sim_status_pin_req);
        case TelephonyManager.SIM_STATE_PUK_REQUIRED:
            return mContext.getString(R.string.sim_status_puk_req);
        case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
            return mContext.getString(R.string.sim_status_locked);
        case TelephonyManager.SIM_STATE_READY:
            return mContext.getString(R.string.sim_status_ready);
        }
        return mContext.getString(R.string.sim_status_unknown);
    }
	
}
