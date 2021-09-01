
package com.lovdream.factorykit.items;

import android.view.View;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.content.Context;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.os.SystemProperties;
import android.util.Log;
import android.telephony.TelephonyManager;
import com.swfp.utils.SimUtil;
import java.util.ArrayList;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.TestItemBase;

public class SimTest extends TestItemBase{

	private TextView mTv;


	@Override
	public String getKey(){
		return "sim_test";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.sim_test_mesg);
	}

	@Override
	public void onStartTest(){
		TelephonyManager tm = TelephonyManager.getDefault();
		int count = tm.isMultiSimEnabled() ? 2 : 1;

		if(SimUtil.getInstance(getActivity()).searchSim().isSimReady){
			postSuccess();
		}else{
			postFail();
		}
	}

	@Override
	public void onStopTest(){
	
	}
	
	@Override
	public View getTestView(LayoutInflater inflater){
		View v = inflater.inflate(R.layout.test_mesg_view,null);
		TextView tv = (TextView)v.findViewById(R.id.test_mesg_view);
		tv.setText(SimUtil.getInstance(getActivity()).searchSim().simMsg);
		enableSuccess(SimUtil.getInstance(getActivity()).searchSim().isSimReady);
		return v;
	}
}
