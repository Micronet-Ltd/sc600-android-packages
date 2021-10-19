package com.quectel.modemconfigtool;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.ArrayMap;
import android.util.Log;

import com.quectel.modemtool.ModemTool;
import com.quectel.modemtool.NvConstants;


import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;


import static android.content.Context.MODE_PRIVATE;

/**
 * Created by klein on 18-11-14.
 */

public class NvManager {

    private static final String TAG = "modemconfigtool-NvManager";

    private Context mContext;
    private ModemTool modemtool;
    public boolean isCompeleted = false;
    public static int mInterval = 200;  //ms, interval betwwen each at command, it is neccessary for system to sync config.

    private ArrayMap<String, String> mNvWriteMap = new ArrayMap<>();
    private String[] mNvWriteArray;

    // <key,value> values of mNvVeriryMap are used to record if we send the at command successfully,
    // not matter if we get the correct value that equal with value in mNvVeriryValues.
    HashMap<String, String> mNvVeriryMap = new HashMap<>();
    String[] mNvVeriryOptions;
    String[] mNvVeriryValues;
    private StringBuilder mNvVerityError;

    //try to reset the default nv if any at command excuted failed.
    public int MAX_RETRY_TIMES = 3;

    //SharedPreferences mSharedPreferences;
    //private final static String TRI_KEY = "com.quectel.nvsettingdomestic.atservice.nvsetting";
    //public final static String NEED_TO_SET  = "compeleted";

    public NvManager(Context context) {
        mContext = context;
        modemtool = new ModemTool();
    }

    public int startWrite(int resid) {
        Log.d(TAG, "startWrite");
        mNvWriteMap.clear();

        mNvWriteArray = mContext.getResources().getStringArray(resid);
        for(int i = 0; i < mNvWriteArray.length; i++){
            // init MAP<at command, if excute sucessfully>
            mNvWriteMap.put(mNvWriteArray[i], "false");
        }
        Log.d(TAG, "start to set default nv, mNvWriteMap = " + mNvWriteMap);
        //mSharedPreferences = mContext.getSharedPreferences(TRI_KEY, MODE_PRIVATE);
        //Log.d(TAG, "NEED_TO_SET : " + mSharedPreferences.getInt(NEED_TO_SET,0));

        //if (mSharedPreferences.getInt(NEED_TO_SET, 0) == 1){
        //    Log.d(TAG, "action done");
        //}
        for (int i = 0; i < MAX_RETRY_TIMES; i++){
            //Log.d(TAG, "[" + i + "] " + "mNvWriteMap = " + mNvWriteMap);
            if (setDefaultNv()){
                return Common.EVENT_WRITE_SUCCESS;
            }
        }

        return Common.EVENT_WRITE_FAIL;
    }

    public boolean setDefaultNv() {
        Log.d(TAG,"setDefaultNv");
        String syncresult;
        isCompeleted = true;
        for (int i = 0; i < mNvWriteArray.length; i++){
            try{
                Thread.sleep(mInterval);
            }catch (Exception e){
                e.printStackTrace();
            }
            syncresult = null;
            if (mNvWriteMap.get(mNvWriteArray[i]).equals("false")){
                syncresult = modemtool.sendAtCommand(NvConstants.REQUEST_SEND_AT_COMMAND, mNvWriteArray[i]);
                if (syncresult!=null && syncresult.contains("OK")){
                    Log.d(TAG, "setDefaultNv syncresult = " + syncresult);
                    mNvWriteMap.put(mNvWriteArray[i], "true");
                } else {
                    isCompeleted = false;
                }
            }
            Log.d(TAG, "mATCommandArray[" + i + "] = " + mNvWriteArray[i] + ", " + mNvWriteMap.get(mNvWriteArray[i]));
        }
        Log.d(TAG, "isCompeleted = " + isCompeleted);
        Log.d(TAG, "mNvWriteMap = " + mNvWriteMap);

        return isCompeleted;
    }

    public String getNVWriteResultMessage(){
        StringBuilder errorMessage = new StringBuilder();
        for (Map.Entry<String,String> entry:mNvWriteMap.entrySet()) {
            if (entry.getValue().equals("false")) {
                errorMessage.append(entry.getKey()+" ;");
            }
        }
        return errorMessage.toString();
    }
    public int startVerify(int resid, int value_resid) {
        Log.d(TAG,"startVerify");
        String syncresult = null;
        String parseresult = null;
        mNvVerityError =new StringBuilder();
        boolean isVerifySuccess;
        int event_code = Common.EVENT_VERIFY_SUCCESS;
        mNvVeriryOptions = mContext.getResources().getStringArray(resid);
        mNvVeriryValues = mContext.getResources().getStringArray(value_resid);
        for(int i = 0; i < mNvVeriryOptions.length; i++){
            mNvVeriryMap.put(mNvVeriryOptions[i], "false");

            Log.d(TAG, "mNvVeriryOptions[" + i + "] = " + mNvVeriryOptions[i] + "; "
                    + "mNvVeriryValues[" + i + "] = " + mNvVeriryValues[i] + "; "
                    + mNvVeriryMap.get(mNvVeriryOptions[i]));
        }

        for (int retryCount = 0; retryCount < MAX_RETRY_TIMES; retryCount++){
            Log.d(TAG, "####retryCount/MAX_RETRY_TIMES = " + retryCount + "/" + MAX_RETRY_TIMES + ", start####");
            Log.d(TAG, "[" + retryCount + " start] " + "mNvVeriryMap = " + mNvVeriryMap);
            mNvVerityError.setLength(0);
            isVerifySuccess = true;
            event_code = Common.EVENT_VERIFY_SUCCESS;

            for (int i = 0; i < mNvVeriryOptions.length; i++){
                if (mNvVeriryMap.get(mNvVeriryOptions[i]).equals("true")){
                    continue;
                }

                syncresult = null;
                syncresult = modemtool.sendAtCommand(NvConstants.REQUEST_SEND_AT_COMMAND, mNvVeriryOptions[i]);
                Log.d(TAG, "syncresult = " + syncresult);

                if (syncresult!=null && syncresult.contains("OK")){
                    parseresult = parseAtResult(syncresult);

                    if(parseresult.equals(mNvVeriryValues[i])){
                        Log.d(TAG, "parseresult = " + parseresult + ", mNvVeriryValues[" + i + "] = " + mNvVeriryValues[i]);
                    } else {
                        isVerifySuccess = false;
                        mNvVerityError.append(mNvVeriryOptions[i]+" ;");
                        event_code = Common.EVENT_VERIFY_CODE_VALUE_NOT_MATCH;
                        //Log.d(TAG, "syncresult = " + syncresult);
                        Log.d(TAG, "result is not right, expect " + "mNvVeriryValues[" + i + "] " + mNvVeriryValues[i]);
                        return event_code;
                    }
                    //if send at command and the result sucessfully, set verify result to true
                    mNvVeriryMap.put(mNvVeriryOptions[i], "true");
                } else {
                    //retry when fail to send at command, becase we can't get the value of corressponding nv.
                    isVerifySuccess = false;
                    event_code = Common.EVENT_VRIFY_FAIL_TO_SEND_AT_COMMAND;
                }
            }
            Log.d(TAG, "[" + retryCount + " end] " + "mNvVeriryMap = " + mNvVeriryMap);
            Log.d(TAG, "####retryCount/MAX_RETRY_TIMES = " + retryCount + "/" + MAX_RETRY_TIMES + ", end####");
            if (isVerifySuccess){
                return Common.EVENT_VERIFY_SUCCESS;
            }
        }

        return event_code;
    }

    /* # AT+QNVFR="/nv/item_files/modem/lte/ML1/update_band_range"
       AT+QNVFR="/nv/item_files/modem/lte/ML1/update_band_range"
       +QNVFR: 01002900309D18A1000000000000000000000000


       # at+qnvr=1920,0
       +QNVR: "07030000"

       OK

       # AT+QCFG="hotswap"
       +QCFG: "HOTSWAP",1,1

       OK
    */

    public String parseAtResult(String atresult){
        String[] s1;
        String parseresult = null;
        if (atresult.contains("OK")){
            s1 = atresult.split("\t|\r|\n");
            for (String subs1:s1){
                //Log.d(TAG, "subs1 = " + subs1);
                if (subs1.startsWith("+QNVR: ")){
                    Pattern p=Pattern.compile("\"(.*?)\"");
                    Matcher m=p.matcher(subs1);
                    while(m.find()){
                        parseresult = m.group().substring(1, m.group().length()-1);
                        Log.d(TAG, "parseresult = " + parseresult);
                    }
                } else if (subs1.startsWith("+QNVFR: ")){
                    parseresult = subs1.replace("+QNVFR: ", "");
                } else if (subs1.startsWith("+QCFG: ")){
                    parseresult = subs1.replace("+QCFG: ", "");
                }
            }
        }
        return parseresult;
    }

    public String getVerifyErrormessage(){
        return mNvVerityError.toString();
    }
    public void setFlag(){
        //SharedPreferences.Editor editor = mSharedPreferences.edit();
        //editor.putInt(NEED_TO_SET,1);
        //editor.commit();
    }
}
