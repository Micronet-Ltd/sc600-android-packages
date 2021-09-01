package com.lovdream.factorykit.items;

import android.view.View;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.os.SystemProperties;
import android.util.Log;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.TestItemBase;

public class HoareTest extends TestItemBase{

    ImageView gray;
    ImageView green;

	@Override
	public String getKey(){
		return "hoare_test";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.hoare_test_mesg);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent keyevent) {
		switch(keyCode){
			case KeyEvent.KEYCODE_WAKEUP:
				updateView(false);
				break;
			case KeyEvent.KEYCODE_SLEEP:
				updateView(true);
				postSuccess();
				break;
		}
		return true;
	}

	@Override
	public void onStartTest(){
		SystemProperties.set("sys.cit_keytest","true");
	}

	@Override
	public void onStopTest(){
		SystemProperties.set("sys.cit_keytest","false");
	}

    @Override
    public View getTestView(LayoutInflater inflater){
        View view = inflater.inflate(R.layout.hoare_test,null,false);
        gray = (ImageView) view.findViewById(R.id.test_hoare_gray);
        green = (ImageView) view.findViewById(R.id.test_hoare_green);
        return view;
    }

	private void updateView(boolean isGreen){
		if((green == null) || (gray == null)){
			return;
		}
		if(isGreen){
			gray.setVisibility(View.INVISIBLE);
			green.setVisibility(View.VISIBLE);
		}else{
			gray.setVisibility(View.VISIBLE);
			green.setVisibility(View.INVISIBLE);
		}
	}
}
