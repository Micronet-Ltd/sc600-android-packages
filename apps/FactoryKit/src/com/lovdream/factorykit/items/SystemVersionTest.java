package com.lovdream.factorykit.items;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.lovdream.factorykit.TestItemBase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.lovdream.factorykit.R;
import android.os.SystemProperties;

/**
 * Created by yangzhiming on 2017/7/5.
 */

public class SystemVersionTest extends TestItemBase{
    private static final String TITLE = "title";
    private static final String HINT_MSG = "info";
    public static final String TP_INFO = "/sys/devices/virtual/assist/ctp/ic";
    public static final String TP_VER = "/sys/devices/virtual/assist/ctp/fw_ver";
    private static final String UNKNOWN = "unknown";

    private ListView mListView;
    private SimpleAdapter mSimpleAdapter;
    private List<Map<String,String>> mItemInfos = new ArrayList<>();
    @Override
    public String getKey() {
        return "system_version";
    }

    @Override
    public String getTestMessage() {
        return null;
    }

    @Override
    public void onStartTest() {

    }

    @Override
    public void onStopTest() {

    }

    @Override
    public View getTestView(LayoutInflater inflater) {
        mSimpleAdapter =
                new SimpleAdapter(getActivity(),mItemInfos,R.layout.device_info_item,
                        new String[]{TITLE,HINT_MSG},new int[]{R.id.title,R.id.info});
        mListView = new ListView(getActivity());
        mListView.setAdapter(mSimpleAdapter);
        return mListView;
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
        hideButtons();
    }

    private void init(){
        mItemInfos.clear();
        int[] titleIds = {R.string.model, R.string.soft_version,
                R.string.internal_version,R.string.baseband_version,
                R.string.hardware_version,
                R.string.sn,
                R.string.tp_info,R.string.tp_version,R.string.mcu_version};
        int length = titleIds.length;
        HashMap hashMap;
        String title,info;
        for (int i = 0; i < length;i ++){
            hashMap = new HashMap();
            title = getString(titleIds[i]);
            switch (titleIds[i]){
                case R.string.model:
                    info = getSystemproString("ro.product.ui_model");
                    if (UNKNOWN.equals(info)){
                        info = getSystemproString("ro.product.ui_model_s");
                    }
                    break;
                case R.string.soft_version:
                    info = getSystemproString("ro.build.display.id");
                    break;
                case R.string.internal_version:
                    info = getSystemproString("ro.build.inter_version");
                    break;
                case R.string.baseband_version:
                    info = SystemProperties.get("persist.sys.bandversion", "MPSS.TR.2.0-00633");
                    break;
                case R.string.tp_info:
                    info = readFileSdcardFile(TP_INFO);
                    break;
                case R.string.tp_version:
                    info = readFileSdcardFile(TP_VER);
                    break;
                case R.string.sn:
                    info = getSnVersion();
                    break;
                case R.string.hardware_version:
                    info = getHardwareVersion();
                    break;
                case R.string.mcu_version:
                    info = getSystemproString("hw.build.version.mcu");
                    break;
                default:
                    info =UNKNOWN;
            }
            hashMap.put(TITLE,title);
            hashMap.put(HINT_MSG,info);
            mItemInfos.add(hashMap);
        }
        mSimpleAdapter.notifyDataSetChanged();
    }
    private String getSystemproString(String property) {
        return SystemProperties.get(property, UNKNOWN);
    }
    public String readFileSdcardFile(String filePath){
        String res="";
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath))));
            String str=null;
            while((str=br.readLine())!=null){
                res+=str;
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return res;
    }
    private String getSnVersion() {
        String strSN = com.lovdream.util.SystemUtil.getSN();
        int length = strSN.length();
        if (length >= 20){
            strSN = strSN.substring(0,20);
        } else {
            strSN = strSN.substring(0,15);
        }
        return strSN;
    }
    
    
    private String getHardwareVersion() {
    	return getSystemproString("persist.sys.broad.config");
    }
}
