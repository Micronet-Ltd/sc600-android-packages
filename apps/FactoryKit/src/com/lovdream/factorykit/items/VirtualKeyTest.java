
package com.lovdream.factorykit.items;

import android.view.KeyEvent;

import com.lovdream.factorykit.R;

public class VirtualKeyTest extends KeyTest{

	@Override
	public String getKey(){
		return "virtual_key_test";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.virtual_key_test_mesg);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

	/*	if((event.getFlags() & KeyEvent.FLAG_VIRTUAL_HARD_KEY) == 0){
			return true;
		}*/
		
		return super.onKeyUp(keyCode,event);
	}
}
