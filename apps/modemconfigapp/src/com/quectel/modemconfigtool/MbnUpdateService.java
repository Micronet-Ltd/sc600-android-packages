package com.quectel.modemconfigtool;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.SystemProperties;
import android.os.RemoteException;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;
import java.util.ArrayList;

import android.telephony.TelephonyManager;
import android.os.SystemProperties;

import com.quectel.modemtool.ModemTool;

import static com.quectel.modemtool.NvConstants.REQUEST_SEND_AT_COMMAND;
import com.qualcomm.qti.modemtestmode.MbnFileLoadService;
import com.qualcomm.qti.modemtestmode.MbnMetaInfo;


public class MbnUpdateService extends Service {
    private static final String TAG = "modemconfigtool-MbnUpdateService";
    private Messenger mServiceMessenger;
    private Messenger mLocalMessenger;
    private Context mContext;

    private int mMbnListId, mMbnAliasId, mMbnidsId, mAtCommandsId;
    private Map mbnMap = new HashMap<String, Integer>();
    NvManager mNvManager;

    String baseBandVersion = SystemProperties.get("gsm.version.baseband", "null");

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.mContext = this;
        Log.d(TAG, "gsm version baseband is " + baseBandVersion +
                ", version :" + mContext.getResources().getString(R.string.about_content));
        mNvManager = new NvManager(mContext);

        mMbnListId = R.array.mbns;
        mMbnAliasId = R.array.mbn_alias;
        mMbnidsId = R.array.mbn_ids;
        mAtCommandsId = R.array.at_commands_for_mbns_config;
        startMbnFileLoadService(this);
    }

    private void startMbnFileLoadService(Context context) {
        Log.d(TAG, "startMbnFileLoadService");

        Intent intent = new Intent(context, MbnFileLoadService.class);
        if (mLocalMessenger == null) {
            mLocalMessenger = new Messenger(new MyHandler());
        }
        ServiceConnection conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                Log.d(TAG, "onServiceConnected");
                mServiceMessenger = new Messenger(iBinder);

                Message message = Message.obtain();
                message.what = MbnFileLoadService.CMD_TRY_TO_EASTABLISH_CONNECTION;

                message.replyTo = mLocalMessenger;

                try {
                    mServiceMessenger.send(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };
        context.bindService(intent, conn, Service.BIND_AUTO_CREATE);
    }


    private class MyHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MbnFileLoadService.TYPE_DEFAULT:
                    //TODO
                    break;

                case MbnFileLoadService.TYPE_CONNECTION_ESTABLISHED:

                    //sendCommandMessage(MbnFileLoadService.CMD_LOAD_MBN);
                    //try{
                    //    Thread.sleep(2000);
                    //}catch (Exception e ){
                    //   e.printStackTrace();
                    //}
                    //getTheMbnConfigure();
                    getTheMbnConfigure();

                    break;
                case MbnFileLoadService.TYPE_MBN_CONFIGURATION:
                    ArrayList<MbnMetaInfo> mbnlist = (ArrayList<MbnMetaInfo>) msg.obj;
                    if (mbnlist != null) {
                        for (int i = 0; i < mbnlist.size(); i++) {
                            MbnMetaInfo mbninfo = mbnlist.get(i);
                            Log.d(TAG, "mbninfo.getMetaInfo() = " + mbninfo.getMetaInfo()
                                    + ", mbninfo.getOemVersion = " + bytesToHex(mbninfo.getOemVersion()));
                        }
                    }
                    if (mbnlist == null || !isCurrentMbnsConfigRight(mbnlist)) {
                        Log.d(TAG, "start to update Mbn");
                        getMbnFiles();
                        try {
                            Set<Map.Entry<String, Integer>> set = mbnMap.entrySet();
                            for (Map.Entry<String, Integer> me : set) {
                                String key = me.getKey();
                                Integer value = me.getValue();
                                copyMbnToSD(value, "sdcard/" + key + ".mbn");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        startLoadMbn();
                    } else {
                        Log.d(TAG, "isCurrentMbnsConfigRight true Mbn had been update");
                    }
                    break;

                case MbnFileLoadService.TYPE_MBN_LOAD_SUCCESS:
                    Log.d(TAG, "Success: mbns load successfully");
                    try {
                        clearTempMbnFiles();
                        if (mNvManager.startWrite(mAtCommandsId) == Common.EVENT_WRITE_SUCCESS){
                            Log.d(TAG, "Success: at for mbn config set successfully");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                default:
                    Log.d(TAG, "Unexpected event:" + msg.what);
                    break;
            }
        }
    }

    public boolean isCurrentMbnsConfigRight(ArrayList<MbnMetaInfo> mbnlist) {
        boolean result = true;
        if (mbnlist.size() == 0) {
            Log.d(TAG, "All meta info are not correct");
            return false;
        }
        HashMap<String, String> mbnMap = new HashMap<>();
        HashMap<String, String> currentDeviceMbnMap = new HashMap<>();
        String[] mMbnAlias;
        String[] mMbnIds;
        mMbnAlias = mContext.getResources().getStringArray(mMbnAliasId);
        mMbnIds = mContext.getResources().getStringArray(mMbnidsId);
        if (mbnlist.size() != mMbnAlias.length) {
            Log.d(TAG, "Mbn count not match");
            return false;
        }
        for (int i = 0; i < mMbnAlias.length; i++) {
            mbnMap.put(mMbnAlias[i], mMbnIds[i]);
            Log.d(TAG, "targetMbnMap : " + mMbnAlias[i] + "," + mMbnIds[i]);
        }
        for (int i = 0; i < mbnlist.size(); i++) {
            currentDeviceMbnMap.put(mbnlist.get(i).getMetaInfo(), bytesToHex(mbnlist.get(i).getOemVersion()));
            Log.d(TAG, "currentMbnMap : " + mbnlist.get(i).getMetaInfo() + "," + bytesToHex(mbnlist.get(i).getOemVersion()));
        }
        Iterator<Map.Entry<String, String>> entries = mbnMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, String> entryNext = entries.next();
            if (currentDeviceMbnMap.containsKey(entryNext.getKey())) {
                if (Long.parseLong(currentDeviceMbnMap.get(entryNext.getKey()), 16) >= Long.parseLong(entryNext.getValue(), 16)) {
                    Log.d(TAG, "currentDeviceMbnMap contains " + entryNext.getKey()
                            + "," + entryNext.getValue() + " matched");
                    continue;
                } else {
                    Log.d(TAG, "currentDeviceMbnMap  contains " + entryNext.getKey()
                            + ", but not contains " + entryNext.getValue());
                    return false;
                }
            } else {
                Log.d(TAG, "currentDeviceMbnMap not contains " + entryNext.getKey());
                return false;
            }
        }
        return true;
    }

    private void getTheMbnConfigure() {
        Log.d(TAG, "getTheMbnConfigure");
        sendCommandMessage(MbnFileLoadService.CMD_GET_MBN_CONFIGURATION);
    }

    private void copyMbnToSD(int originalFileId, String strOutFileName) throws IOException {
        InputStream myInput = mContext.getResources().openRawResource(originalFileId);
        ;
        OutputStream myOutput = new FileOutputStream(strOutFileName);
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }

        myOutput.flush();
        myInput.close();
        myOutput.close();
    }

    private void startLoadMbn() {
        Log.d(TAG, "startLoadMbn");
        sendCommandMessage(MbnFileLoadService.CMD_LOAD_MBN);
    }

    public void sendCommandMessage(int command) {
        Message message = new Message();
        message.what = command;
        try {
            mServiceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    void getMbnFiles() {
        Log.d(TAG, "getMbnFiles");
        String[] stringArray = mContext.getResources().getStringArray(mMbnListId);
        for (int i = 0; i < stringArray.length; i++) {
            mbnMap.put(stringArray[i], mContext.getResources().getIdentifier(stringArray[i], "raw", mContext.getPackageName()));
        }

        Set<Map.Entry<String, Integer>> set = mbnMap.entrySet();
        for (Map.Entry<String, Integer> me : set) {
            String key = me.getKey();
            Integer value = me.getValue();
            Log.d(TAG, "key = " + key + ", " + "value = " + value);
        }
    }

    private void clearTempMbnFiles() throws IOException {
        Set<Map.Entry<String, Integer>> set = mbnMap.entrySet();
        for (Map.Entry<String, Integer> me : set) {
            String key = me.getKey();
            Integer value = me.getValue();
            File file = new File("sdcard/" + key + ".mbn");
            if (file.exists()) {
                file.delete();
            }
        }
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder buf = new StringBuilder(bytes.length * 2);
        for (int i = bytes.length-1; i >= 0; i--){
            buf.append(String.format("%02x", new Integer(bytes[i] & 0xff)));
        }

        return buf.toString().toUpperCase();
    }
}
