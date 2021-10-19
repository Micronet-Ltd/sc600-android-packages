package com.quectel.modemtool;
import android.util.Log;

public class ModemTool{
    public static final String TAG = "ModemTool-local";

    // Used to load the 'qlmodem' library on application startup.
    static {
        System.loadLibrary("qlmodem");
    }

    private int cSelf;

    public interface ResponseListener{
        public void onResponse(String response);
    }

    private ResponseListener mListener;

    public void setResponseListener(ResponseListener listener){
        mListener = listener;
    }

    public ModemTool() {
        setAtPort("/dev/smd8");
        //cSelf = _init();
    }


    public int sendAtCommandAsync(String atCommand) {
        Log.d(TAG, "atCommand = " + atCommand);
        //_sendAtCommandAsync(atCommand);
        return 0;
    }

    public int sendData(String atCommand) {
        Log.d(TAG, "atCommand = " + atCommand);
        //_sendData(atCommand);
        return 0;
    }

    public int setAtPort(String atCommand) {
        Log.d(TAG, "atCommand = " + atCommand);
        //_setAtPort(atCommand);
        return 0;
    }

    public void onResponse(String response){

        Log.d(TAG, "response = " + response);

        mListener.onResponse(response);
    }

    public native String sendAtCommand(int commandId, String atCommand);

}
