package com.lovdream.factorykit.items;

import android.os.SystemProperties;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.TestItemBase;
import com.swfp.utils.Utils;

public class MemoryTest extends TestItemBase{

	
	TextView tv_ram;
	TextView tv_rom;

	public String getKey(){
		return "memory_test";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.memory_test_mesg);
	}

	@Override
	public void onStartTest() {
		
	}

	@Override
	public void onStopTest() {
		
	}
	
	@Override
	public View getTestView(LayoutInflater inflater){
		View v = inflater.inflate(R.layout.memory_test,null);
		
		String ramStr = Utils.getRamTotalSize(getActivity());
		String romStr = Utils.getRomTotalSize(getActivity());
		
		tv_ram = (TextView)v.findViewById(R.id.ram_info);
		tv_rom = (TextView)v.findViewById(R.id.rom_info);
		
		int ram = Integer.parseInt(ramStr.substring(0,ramStr.indexOf("GB"))); 
		int rom = Integer.parseInt(romStr.substring(0,romStr.indexOf(".")));

		if((SystemProperties.getInt("hw.board.id", 0) < 2 && ram == 3 && rom == 32) || (SystemProperties.getInt("hw.board.id", 0) >=2  && ram == 2 && rom == 16)){
                enableSuccess(true);
                postSuccess();
		
		} else {
            enableSuccess(false);
            postFail();
		}


		String ramInfo = getResources().getString(R.string.memory_test_ram)+ramStr;
		String romInfo = getResources().getString(R.string.memory_test_rom)+romStr;
		tv_ram.setText(ramInfo);
		tv_rom.setText(romInfo);
		
		return v;
	}

}
