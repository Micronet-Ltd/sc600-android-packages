
package com.lovdream.factorykit.items;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.TestItemBase;

public class FrontFlashLedTest extends FlashLedTest{

	@Override
	protected String getCameraId(){
		return "1";
	}

    @Override
    public String getKey() {
        return "front_flash_light";
    }

    @Override
    public String getTestMessage() {
        return getString(R.string.front_flash_led_msg);
    }
}
