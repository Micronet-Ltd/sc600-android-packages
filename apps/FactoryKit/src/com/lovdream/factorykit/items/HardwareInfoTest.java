package com.lovdream.factorykit.items;

import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.TestItemBase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HardwareInfoTest extends TestItemBase {

    public static final String TP_INFO = "/sys/devices/virtual/assist/ctp/ic";
    public static final String TP_VER = "/sys/devices/virtual/assist/ctp/fw_ver";

    public static final String MCP_NAME = "/sys/class/mmc_host/mmc0/mmc0:0001/name";
    public static final String MCP_MANFID = "/sys/class/mmc_host/mmc0/mmc0:0001/manfid";
    public static final String MCP_OEMID = "/sys/class/mmc_host/mmc0/mmc0:0001/oemid";

    public static final String LCD_INFO = "/proc/cmdline";
    public static final String STORAGE_INFO = "/sys/block/mmcblk0/size";

    private static final String TITLE = "title";
    private static final String HINT_MSG = "info";

    private String[] mTitles;
    private ListView mListView;
    private SimpleAdapter mSimpleAdapter;
    private List<Map<String,String>> mItemInfos;

    @Override
    public String getKey() {
        return "hardware_info";
    }

    @Override
    public String getTestMessage() {
        return "";
    }

    @Override
    public void onStartTest() {

    }

    @Override
    public void onStopTest() {
        mTitles = null;
        mItemInfos = null;
    }

    @Override
    public View getTestView(LayoutInflater inflater) {
        initData();
        return mListView;
    }

    private void initData(){
        mTitles = new String[]{
                getString(R.string.mcp_info),getString(R.string.lcd_info),
                getString(R.string.tp),getString(R.string.camera_info),
                getString(R.string.storage_info)
        };
        mItemInfos = new ArrayList<>();
        HashMap map;
        int length = mTitles.length;
        for (int i = 0 ;i < length ;i++){
            map = new HashMap();
            map.put(TITLE,mTitles[i]);
            String info = ""    ;
            switch (i){
                case 0:
                    info = "MCP_NAME:" + readFile(MCP_NAME) + "\n" +
                            "MCP_MANFID:" + readFile(MCP_MANFID) + "\n" +
                            "MCP_OEMID:" + readFile(MCP_OEMID);
                    break;

                case 1:
                    String lcd_info = "";
                    try {
                        lcd_info = readFile(LCD_INFO);
                        lcd_info = lcd_info.substring(lcd_info.indexOf("mdss_dsi_")+9);
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    info = "LCD_INFO:" + lcd_info;
                    break;
                case 2:
                    String tp_info = "";
                    String tp_ic = "";
                    String tp_module = "";
                    tp_info = readFile(TP_INFO);
                    try {
                        tp_ic = tp_info.substring(tp_info.indexOf(".")+1);
                        tp_module = tp_info.substring(0, tp_info.indexOf("."));
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    info = "TP_IC:" + tp_ic + "\n" +
                            "TP_MODULE:" + tp_module + "\n" +
                            "TP_VERSION:" + readFile(TP_VER);
                    break;
                case 3:
                    info = "CAMERA_IC:" + "gc2235" + "\n" +
                            "CAMERA_MODULE:" + "bolixin";
                    break;
                // add by hudayu for emmc storage
                case 4:
                    String storage_info= "";
                    long storageValue = 0;
                    try {
                        storage_info = readFile(STORAGE_INFO);
                       String storage_info1 = storage_info.substring(0);
                        storageValue = Long.valueOf(storage_info1);
                        storageValue = roundStorageSize(storageValue * 512);
                    } catch (Exception e) {
                        // TODO: handle exception
                    }

                    info = "EMMC STORAGE:"+Formatter.formatFileSize(getActivity(), storageValue);
                    break;
            }
            map.put(HINT_MSG,info);
            mItemInfos.add(map);
        }
        mSimpleAdapter =
                new SimpleAdapter(getActivity(),mItemInfos,R.layout.device_info_item,
                        new String[]{TITLE,HINT_MSG},new int[]{R.id.title,R.id.info});
        mListView = new ListView(getActivity());
        mListView.setAdapter(mSimpleAdapter);
    }

    private long roundStorageSize(long size) {
        long val = 1;
        long pow = 1;
        while ((val * pow) < size) {
            val <<= 1;
            if (val > 512) {
                val = 1;
                pow *= 1000;
            }
        }
        return val * pow;
    }

    private String readFile(String filePath) {
        String res = "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath))))) {
            String str = null;
            while ((str = br.readLine()) != null) {
                res += str;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
}
