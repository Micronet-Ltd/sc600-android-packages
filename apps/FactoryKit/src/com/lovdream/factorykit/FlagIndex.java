
package com.lovdream.factorykit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.lovdream.factorykit.Config.TestItem;

public final class FlagIndex{


	public static final int  DEFAULT_INDEX = -1;	
	private static final String[] ALL_KEYS = {
		/*if some item is in small pcb test,index do not big than 10*/
        "charging_test",
        "headset_test_nuno",
        "handset_loopback",
		"key_test",
		"system_version",
        "speaker_test",
		"button_light",
		"camera_test_back",
		"camera_test_front",
		"sim_test",
		"speaker_storage_test",
		"vibrator_test",
		"mic_loop",
		"gsensor_test",
		"sarsensor_test",
		"wifi_test",
		"bt_test",
		"light_sensor",
		"distance_sensor",
		"side_charging_test",
		"flash_light",
		"front_flash_light",
		"gps_test",
		"lcd_test",
		"tp_test",
		"temperature_test",
		"nfc_test",
		"gyro_sensor",
		"compass",
		"otg_test",
		"noise_mic",
		"led_test",
		"wifi_5g_test",
		"back_clip_otg",
		"fingerprint_test",
		"hoare_test",
		"back_charge",
		"back_led",
		"memory_test",
		"noise_mic_front",
		"barometer_test",
		"wake_up_test",
		"nmea_test",
		"fm_test",
		"tp_grid_test",
		"tf_hot_plug",
		"virtual_key_test",
		"headset_key_test",
		"laser_test",
		"ircut_test",
		"infrared_test",
		"media_mic",
		"sub_mic",
		"ignition_test",
		"hardware_info",
		"test_flag",
		"master_clear",
		"sub_pin_test"
	};

	public static List<FlagModel> flagmodels;
	
	private static void load(){
		flagmodels = new ArrayList<FlagModel>();
		for(int i = 0;i < ALL_KEYS.length;i++){
			FlagModel fm =  new FlagModel(ALL_KEYS[i],i,DEFAULT_INDEX,DEFAULT_INDEX,DEFAULT_INDEX);
			flagmodels.add(fm);
		}
	}

	public static int getIndex(String key){
		if (flagmodels == null) {
			load();
		}
		boolean isFind = false;
		String searchKey = "";
		int index = 0;
		for (int i = 0; i < flagmodels.size(); i++) {
			searchKey = flagmodels.get(i).key;
			if (searchKey != null && searchKey.equals(key)) {
				isFind = true;
				index = i;
			}
		}
		if (!isFind) {
			throw new RuntimeException(
					"Invalid key,you should add the key in FlagIndex.ALL_KEYS if you implement any test items");
		}
		return index;
	}
	
	public static FlagModel getFlagModel(String key){
		if (flagmodels == null) {
			load();
		}
		boolean isFind = false;
		String searchKey = "";
		int index = 0;
		for (int i = 0; i < flagmodels.size(); i++) {
			searchKey = flagmodels.get(i).key;
			if (searchKey != null && searchKey.equals(key)) {
				isFind = true;
				index = i;
			}
		}
		if (!isFind) {
			throw new RuntimeException(
					"Invalid key,you should add the key in FlagIndex.ALL_KEYS if you implement any test items");
		}
		return flagmodels.get(index);
	}
	
}
