
package com.swfp.device;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class DeviceManager {
    class DispatchMessageHandler extends Handler {
        public DispatchMessageHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if(DeviceManager.get0(DeviceManager.this) != null) {
                Message v0 = new Message();
                v0.what = msg.what;
                v0.arg1 = msg.arg1;
                v0.arg2 = msg.arg2;
                DeviceManager.get0(DeviceManager.this).sendMessageToClient(v0);
            }
        }
    }

    public interface IFpCallBack {
        void sendMessageToClient(Message arg1);
    }

    private static final String DRIVER_VER_PATH = "/sys/devices/platform/sunwave_version_detect/version";
    private static final String DRIVER_VER_PATH_M = "/sys/devices/virtual/misc/sunwave_fp/sunwave/version";
    private static final String TAG = "sw-DeviceManager";
    private boolean isConnected;
    private FpDevice mDevice;
    private DispatchMessageHandler mDispathMessageHandler;
    private IFpCallBack mFpCallBack;
    private static DeviceManager manager;

    static IFpCallBack get0(DeviceManager arg1) {
        return arg1.mFpCallBack;
    }

    private DeviceManager() {
        super();
        this.isConnected = true;
        this.mDevice = FpDevice.open();
        HandlerThread v0 = new HandlerThread("dispatch");
        v0.start();
        this.mDispathMessageHandler = new DispatchMessageHandler(v0.getLooper());
        this.mDevice.setDispathcMessageHandler(this.mDispathMessageHandler);
    }

    public void checkConnectToServer() {
        if(!this.isConnected) {
            this.sleepInSafe(50);
            this.mDevice.connect();
        }
    }

    public void disConnect() {
        this.isConnected = false;
        this.sleepInSafe(50);
        this.mDevice.native_release();
    }

    public static DeviceManager getDeviceManager(Context context) {
        if(DeviceManager.manager == null) {
            Class v1 = DeviceManager.class;
			synchronized(v1){
				try {
					if(DeviceManager.manager == null) {
						DeviceManager.manager = new DeviceManager();
					}
				}
				catch(Throwable v0) {
					throw v0;
				}
			}
        }

        return DeviceManager.manager;
    }

	/*
    public static String getDriverVersion() {
        BufferedReader v4;
        String v2 = "null";
        File v1 = new File("/sys/devices/platform/sunwave_version_detect/version");
        if(!v1.exists()) {
            v1 = new File("/sys/devices/virtual/misc/sunwave_fp/sunwave/version");
        }

        BufferedReader v3 = null;
        try {
            v4 = new BufferedReader(new FileReader(v1));
            goto label_10;
        }
        catch(Throwable v5) {
        }
        catch(IOException v0) {
            goto label_19;
            try {
            label_10:
                v2 = v4.readLine();
                if(v4 == null) {
                    return v2;
                }

                goto label_12;
            }
            catch(Throwable v5) {
                v3 = v4;
            }
            catch(IOException v0) {
                v3 = v4;
                try {
                label_19:
                    v0.printStackTrace();
                    if(v3 == null) {
                        return v2;
                    }
                }
                catch(Throwable v5) {
                    goto label_27;
                }

                try {
                    v3.close();
                }
                catch(IOException v0) {
                    v0.printStackTrace();
                }

                return v2;
            }
        }

    label_27:
        if(v3 != null) {
            try {
                v3.close();
            }
            catch(IOException v0) {
                v0.printStackTrace();
            }
        }

        throw v5;
        try {
        label_12:
            v4.close();
        }
        catch(IOException v0) {
            v0.printStackTrace();
        }

        return v2;
    }
	*/

    public int getImgInfo(int[] rWidth, int[] rHeight) {
        return FpDevice.fpGetImageInfo_native(rWidth, rHeight);
    }

    public String getVersionInfo() {
        return FpDevice.fpGetVersionInfo_native();
    }

    public int readImage(byte[] img, int[] rLen) {
        return FpDevice.fpReadImg_native(img, rLen);
    }

    public void registerFpCallBack(IFpCallBack callBack) {
        this.mFpCallBack = callBack;
    }

    public void scanImage() {
        this.mDevice.scanImage();
        Log.i("sw-DeviceManager", "begin scanimage");
    }

    public int sendCmd(int cmd, int param, byte[] buf, int[] len) {
        return FpDevice.fpSendCmd_native(cmd, param, buf, len);
    }

    private void sleepInSafe(long ms) {
        try {
            Thread.sleep(ms);
        }
        catch(InterruptedException v0) {
            v0.printStackTrace();
        }
    }

    public void startImageMode() {
        this.mDevice.cancelRecognize();
        this.sleepInSafe(50);
        this.scanImage();
    }

    public void stopImageMode() {
        if(this.mDevice.cancelRecognize() != 0) {
            this.sleepInSafe(5);
            this.mDevice.cancelRecognize();
        }

        this.sleepInSafe(50);
    }

    public int waitLeave() {
        return FpDevice.fpWaitFingerLeave_native();
    }
}

