
package com.lovdream.factorykit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.swfp.utils.ProjectControlUtil;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.telephony.TelephonyManager;

public class Utils {

	public static final int FLAG_INDEX_CALIBRATION = 0;
	public static final int FLAG_INDEX_2G_3G_FT = 1;
	public static final int FLAG_INDEX_4G_FT = 2;
	public static final int FLAG_INDEX_AUTO = 3;
	public static final int FLAG_INDEX_PCBA = 4;
        public static final int FLAG_INDEX_USB = 5;

	public static final String KEY_OUTPUT = "output";
	public static final String PROP_MULTISIM = "persist.radio.multisim.config";
	public static final String PROP_CHARGE_DISABLE = "persist.usb.chgdisabled";

	public static final String PROP_DEBUG_ABLE = "persist.factorykit.debuggable";
	public static final String PROP_WITHOUT_CARD = "sys.factorykit.without_card";
	public static final String PROP_FULL_SCREEN = "sys.factorykit.full_screen";

	private static final boolean AUTO_TEST_WITHOUT_CARD = SystemProperties.getBoolean(PROP_WITHOUT_CARD,false) || ProjectControlUtil.isC551;

	public static final boolean IS_TYPE_C = ProjectControlUtil.IS_TYPE_C;
    public static final String TAG = Main.TAG;
    public static final String RESULT_PASS = "Pass";
    public static final String RESULT_FAIL = "Failed";

    public static final String CURRENT_FILE_NAME = "CurrentMessage.txt";
    public static final String TESTLOG_FILE_NAME = "Testlog.txt";

    public static void writeCurMessage(Context context, String Tag, String result) {

        String msg = "[" + Tag + "] " + result;
        FileOutputStream mFileOutputStream = null;
        try {
            mFileOutputStream = new FileOutputStream(context.getFilesDir() + "/"
                    + CURRENT_FILE_NAME, false);
            byte[] buffer = msg.getBytes();
            mFileOutputStream.write(buffer, 0, buffer.length);
            mFileOutputStream.flush();
        } catch (Exception e) {
            loge(e);
            e.printStackTrace();
        } finally {
            try {
                mFileOutputStream.close();
            } catch (IOException e) {
                loge(e);
                e.printStackTrace();
            }
        }
        logd("Writed result=" + result);

    }

    public static void writeCurMessage(String Tag, String result) {

        String msg = "[" + Tag + "] " + result;
        logd("WritedResult: " + Tag + "=" + result);
        FileOutputStream mFileOutputStream = null;
        String filePath = "/data/data/com.qti.factory/files/" + CURRENT_FILE_NAME;
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                loge(e);
                e.printStackTrace();
            }
        }

        try {
            mFileOutputStream = new FileOutputStream(file, false);
            byte[] buffer = msg.getBytes();
            mFileOutputStream.write(buffer, 0, buffer.length);
            mFileOutputStream.flush();
        } catch (Exception e) {
            loge(e);
            e.printStackTrace();
        } finally {
            try {
                if (mFileOutputStream != null)
                    mFileOutputStream.close();
            } catch (IOException e) {
                loge(e);
                e.printStackTrace();
            }
        }

    }

    public static void writeTestLog(String Tag, String result) {

        String msg = null;
        if (result != null)
            msg = "[" + Tag + "] " + result + "\n";
        else
            msg = Tag + "\n";// only write a string
        FileOutputStream mFileOutputStream = null;
        String filePath = "/data/data/com.qti.factory/files/" + TESTLOG_FILE_NAME;
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                loge(e);
                e.printStackTrace();
            }
        }

        try {
            mFileOutputStream = new FileOutputStream(file, true);
            byte[] buffer = msg.getBytes();
            mFileOutputStream.write(buffer, 0, buffer.length);
            mFileOutputStream.flush();
        } catch (Exception e) {
            loge(e);
            e.printStackTrace();
        } finally {
            try {
                mFileOutputStream.close();
            } catch (IOException e) {
                loge(e);
                e.printStackTrace();
            }
        }
        logd("Added TestLog=" + result);

    }

    public static void CleanCurrentMessage(String fpath, String msg) {

        File file = new File(fpath + "/" + "CurrentMessage.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
            } else {
                file.delete();
                file.createNewFile();
            }
            // ==Modified==
            FileWriter fr = new FileWriter(file, true);
            fr.write(msg);

            fr.close();
        } catch (IOException e) {
            Log.e(TAG, "log():" + fpath + ", err=" + e);
        }
    }

    public String getHwPlatform() {
        String hwPlatform = SystemProperties.get("ro.hw_platform");
        if (hwPlatform != null && hwPlatform.length() > 4)
            hwPlatform = hwPlatform.substring(hwPlatform.length() - 4);
        return hwPlatform;
    }

    public static String getStringValueSaved(Context mContext, String key, String def) {

        SharedPreferences mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        return mSharedPreferences.getString(key, def);
    }

    public static void saveStringValue(Context mContext, String key, String value) {

        SharedPreferences mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static boolean getBooleanPreference(Context context, String key, boolean def) {

        SharedPreferences mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        return mSharedPreferences.getBoolean(key, def);

    }

    public static void saveBooleanPreference(Context context, String key, boolean value) {

        SharedPreferences mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean writeFile(String filePath, String content) {
        boolean res = true;
        File file = new File(filePath);
        File dir = new File(file.getParent());
        if (!dir.exists())
            dir.mkdirs();
        try {
            FileWriter mFileWriter = new FileWriter(file, false);
            mFileWriter.write(content);
            mFileWriter.close();
        } catch (IOException e) {
            res = false;
        }
        return res;
    }

    public static String readFile(String filePath) {
        String res = "";
        File file = new File(filePath);
        if (!file.exists())
            return res;

        try {
            char[] buf = new char[1024];
            int count = 0;
            FileReader fileReader = new FileReader(file);
            while ((count = fileReader.read(buf)) > 0) {
                res += new String(buf, 0, count);
            }
            fileReader.close();

        } catch (IOException e) {
            res = "";
        }
        return res;
    }

    public static boolean setSystemProperties(String key, String val) {
        logd("set " + key + " value=" + val);
        if (val == null || key == null)
            return false;
        SystemProperties.set(key, val);
        if (val.equals(SystemProperties.get(key)))
            return true;
        else {
            loge("setproper failed. Check if app has system permission.");
            return false;
        }
    }

    public static String getSystemProperties(String key, String defaultValue) {
        if (key == null)
            return null;
        String property = SystemProperties.get(key, defaultValue);
        logd(key + "=" + property);
        return property;
    }

    public static void createShortcut(Context context, Class appClass) {
        logd("");
        Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getString(R.string.app_name));
        intent.putExtra("duplicate", false);
        Intent appIntent = new Intent();
        appIntent.setAction(Intent.ACTION_MAIN);
        appIntent.setClass(context, appClass);
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, appIntent);
        ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(context,
                R.drawable.icon);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
        context.sendBroadcast(intent);
    }

    private static void log(String fpath, String msg) {

        File file = new File(fpath);

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter mFileWriter = new FileWriter(file, true);
            mFileWriter.append(msg);
            mFileWriter.close();
        } catch (IOException e) {
        }
    }

    public static void parseParameter(final String in, HashMap<String, String> out) {
        String key, value, src;
        if (in == null || out == null)
            return;
        src = in;
        while (true) {
            if (src == null || src.length() == 0)
                break;
            int index1 = src.indexOf('=');
            if (index1 > 0)
                key = src.substring(0, index1).trim();// [start,end)
            else
                break;
            int index2 = src.indexOf(';');
            if (index2 > 0) {
                value = src.substring(index1 + 1, index2).trim();
                src = src.substring(index2 + 1);
                out.put(key, value);
            } else {
                value = src.substring(index1 + 1).trim();
                out.put(key, value);
                break;
            }
        }
    }

    public static void enableBluetooth(boolean enable) {
        logd("enableBluetooth=" + enable);
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (enable)
                mBluetoothAdapter.enable();
            else
                mBluetoothAdapter.disable();
        }
    }

    public static boolean isBluetoothEnable(Context context){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    public static void enableWifi(Context context, boolean enable) {
        logd("enableWifi=" + enable);
        WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null)
            mWifiManager.setWifiEnabled(enable);
    }

    public static void enableGps(Context context, boolean enable) {
        logd("enableGps=" + enable);
        try {
            Settings.Secure.setLocationProviderEnabled(context.getContentResolver(),
                    LocationManager.GPS_PROVIDER, enable);
        } catch (Exception e) {
            loge(e);
        }
    }

    public static boolean isWifiEnable(Context context){
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    /**
     * add by Zemel for bug 15688 15689
     */
    public static boolean isLocationProviderEnabled(Context context){
        return Settings.Secure.isLocationProviderEnabled(context.getContentResolver(),LocationManager.GPS_PROVIDER);
    }


    public static void enableNfc(Context context, boolean enable) {
        logd("enableNfc=" + enable);
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        if(nfcAdapter==null){
            loge("NFC not supported on this platform");
            return;
        }
        if (enable)
            nfcAdapter.enable();
        else {
            nfcAdapter.disable();
        }
    }

    public static boolean isNfcEnable(Context context){
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        return nfcAdapter != null && nfcAdapter.isEnabled();
    }

    public static void configScreenTimeout(Context context, int value) {

        logd(System.getProperty("screenSet"));
        if (System.getProperty("screenSet") == null) {
            Settings.System.putInt(context.getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT, value);
        }
    }

    public static void enableCharging(boolean enable) {
        logd("enableCharging=" + enable);
        String value = enable ? "0" : "1";
        Utils.setSystemProperties(PROP_CHARGE_DISABLE, value);
    }

    public static void exec(final String para, final Handler handler) {

        new Thread() {

            public void run() {
                try {
                    logd(para);

                    Process mProcess;
                    String paras[] = para.split(",");
                    for (int i = 0; i < paras.length; i++)
                        logd(i + ":" + paras[i]);
                    mProcess = Runtime.getRuntime().exec(paras);
                    mProcess.waitFor();

                    InputStream inStream = mProcess.getInputStream();
                    InputStreamReader inReader = new InputStreamReader(inStream);
                    BufferedReader inBuffer = new BufferedReader(inReader);
                    String s;
                    String data = "";
                    while ((s = inBuffer.readLine()) != null) {
                        data += s + "\n";
                    }
                    logd(data);
                    int result = mProcess.exitValue();
                    logd("ExitValue=" + result);
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString(KEY_OUTPUT, data);
                    message.setData(bundle);
                    message.setTarget(handler);
                    message.sendToTarget();

                } catch (Exception e) {
                    logd(e);
                }

            }
        }.start();

    }

    public static String byteArrayToHexArray(byte[] input) {

        String result = "";
        for (int i = 0; i < input.length; i++) {
            result += Integer.toString((input[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

	public static boolean isSdMounted(Context context){
		if(AUTO_TEST_WITHOUT_CARD){
			return true;
		}
        StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        StorageVolume[] storageVolumes = sm.getVolumeList();
        return storageVolumes.length > 1 && storageVolumes[1].getState().equals(Environment.MEDIA_MOUNTED);
	}

	public static boolean isSimReady(){
		if(AUTO_TEST_WITHOUT_CARD){
			return true;
		}
		TelephonyManager tm = TelephonyManager.getDefault();
		int count = tm.isMultiSimEnabled() ? 2 : 1;
		boolean ret = true;
		for(int i = 0;i < count;i++){
			int state = tm.getSimState(i);
			ret &= (state != TelephonyManager.SIM_STATE_ABSENT) && (state != TelephonyManager.SIM_STATE_UNKNOWN);
		}
		return ret;
	}

    private static void logd(Object s) {

        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();

        s = "[" + mMethodName + "] " + s;
        // Log.d(TAG, s + "");
    }

    private static void loge(Object e) {

        if (e == null)
            return;
        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();
        e = "[" + mMethodName + "] " + e;
        // Log.e(TAG, e + "");
    }
}
