
package com.swfp.device;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.swfp.utils.MessageType;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class FpDevice {
    class EventHandler extends Handler {
        public EventHandler(FpDevice fp, Looper looper) {
            super(looper);
            FpDevice.set0(fp);
        }

        public void handleMessage(Message msg) {
            Log.i("sw-FpDevice", "FpDevice: message type: " + msg.what);
        }
    }

    private static final String TAG = "sw-FpDevice";
    private WeakReference dispatchHandlerRef;
    private EventHandler mEventHandler;
    private static FpDevice mFpDevice;
    private int mNativeContext;
    private WeakReference ref;

    static FpDevice set0(FpDevice arg0) {
        FpDevice.mFpDevice = arg0;
        return arg0;
    }

    static {
        FpDevice.mFpDevice = null;
        System.loadLibrary("fpjni_sw");
        Log.e("sw-FpDevice", "FpDevice jni load ok");
    }

    private FpDevice() throws RuntimeException {
        super();
        EventHandler v1 = null;
        this.mEventHandler = v1;
        this.dispatchHandlerRef = null;
        Looper v0 = Looper.myLooper();
        if(v0 != null) {
            this.mEventHandler = new EventHandler(this, v0);
        }
        else {
            v0 = Looper.getMainLooper();
            this.mEventHandler = v0 != null ? new EventHandler(this, v0) : v1;
        }

        this.ref = new WeakReference(this);
        this.connect();
		mNativeContext = 0;
    }

    public int cancelRecognize() {
        return FpDevice.fpIdentifyStop_native();
    }

    public int cancelRegister() {
        return FpDevice.fpRegisterStop_native();
    }

    public void connect() {
        try {
            this.native_setup(this.ref, 1);
            return;
        }
        catch(RuntimeException v0) {
            throw v0;
        }
    }

    public int delete(int index) {
        return FpDevice.fpDeleteTemp_native(index);
    }

    public static native int fpDeleteTemp_native(int arg1);

    public int fpDownloadFw(byte[] fwBuf, int fwlen, int bintype) {
        int v0 = FpDevice.fpDownloadFw_native(fwBuf, fwlen, bintype);
        if(v0 != 0) {
            Log.w("sw-FpDevice", "fpDownloadFw err:" + v0);
        }

        return v0;
    }

    public static native int fpDownloadFw_native(byte[] arg1, int arg2, int arg3);

    public static native int fpGetImageInfo_native(int[] arg1, int[] arg2);

    public int fpGetLibVersion() {
        return FpDevice.fpGetLibVersion_native();
    }

    public static native int fpGetLibVersion_native();

    public static native int fpGetTempInfo_native(int[] arg1, byte[] arg2, byte[] arg3);

    public static native String fpGetVersionInfo_native();

    public static native int fpIdentifyStart_native();

    public static native int fpIdentifyStop_native();

    public static native int fpReadBin_native(byte[] arg1, int[] arg2);

    public static native int fpReadImg_native(byte[] arg1, int[] arg2);

    public static native int fpRegisterStart_native(int arg1);

    public static native int fpRegisterStop_native();

    public static native int fpRenameTemp_native(int arg1, byte[] arg2);

    public static native int fpScanImg_native();

    public static native int fpSendCmd_native(int arg1, int arg2, byte[] arg3, int[] arg4);

    public static native int fpWaitFingerLeave_native();

    public static native int getNumberOfFps();

    public final native void lock();

    public final native void native_release();

    private final native void native_setup(Object arg1, int arg2);

    public static FpDevice open() throws RuntimeException {
        if(FpDevice.mFpDevice == null) {
            try {
                FpDevice.mFpDevice = new FpDevice();
                Log.i("sw-FpDevice", "FpDevice open ok");
            }
            catch(RuntimeException v0) {
                Log.e("sw-FpDevice", "FpDevice Init failed");
                throw v0;
            }
        }

        return FpDevice.mFpDevice;
    }

    private static void postEventFromNative(Object fpdevice_ref, int what, int arg1, int arg2, Object 
            obj) {
        Object v0 = ((WeakReference)fpdevice_ref).get();
        Log.i("sw-FpDevice", MessageType.getMegInfo(what, arg1, arg2));
        if(v0 == null) {
            return;
        }

        if(((FpDevice)v0).mEventHandler != null) {
            Message v1 = ((FpDevice)v0).mEventHandler.obtainMessage(what, arg1, arg2, obj);
            if(((FpDevice)v0).dispatchHandlerRef != null && ((FpDevice)v0).dispatchHandlerRef.get() != 
                    null) {
                ((Handler)((FpDevice)v0).dispatchHandlerRef.get()).sendMessage(v1);
                return;
            }

            Log.d("sw-FpDevice", "FpDevice: mEventHandler");
            ((FpDevice)v0).mEventHandler.sendMessage(v1);
        }
    }

    public int query() {
        int[] v1 = new int[1];
        FpDevice.fpGetTempInfo_native(v1, new byte[5], new byte[100]);
        return v1[0];
    }

    public int recognize() {
        return FpDevice.fpIdentifyStart_native();
    }

    public final native void reconnect() throws IOException;

    public int register(int idx) {
        return FpDevice.fpRegisterStart_native(idx);
    }

    public int resetRegister(int idx) {
        FpDevice.fpRegisterStop_native();
        return FpDevice.fpRegisterStart_native(idx);
    }

    public int saveRegister(int index) {
        return 0;
    }

    public int scanImage() {
        int v0 = FpDevice.fpScanImg_native();
        if(v0 != 0) {
            Log.w("sw-FpDevice", "scanImage err:" + v0);
        }

        return v0;
    }

    public void setDispathcMessageHandler(Handler handler) {
        if(handler != null) {
            Log.i("sw-FpDevice", "FpDevice:setDispathcMessageHandler");
            this.dispatchHandlerRef = new WeakReference(handler);
        }
    }

    public final native void unlock();
}

