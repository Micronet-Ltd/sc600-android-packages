package com.lovdream.factorykit.items;

import android.content.Context;
import android.widget.TextView;
import android.view.View;
import android.os.Environment;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.LayoutInflater;
import android.content.BroadcastReceiver;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.TestItemBase;

public class TFHotPlug extends TestItemBase{

    private TextView mTextView;

	private BroadcastReceiver mReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context,Intent intent){
			if(Intent.ACTION_MEDIA_MOUNTED.equals(intent.getAction())
					&& Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
				if(mTextView != null){
					mTextView.setText(R.string.tf_hot_plug_success);
				}
				postSuccess();
			}
		}
	};

    @Override
    public String getKey() {
        return "tf_hot_plug";
    }

    @Override
    public String getTestMessage() {
        return getString(R.string.tf_hot_plug_mesg);
    }

    @Override
    public void onStartTest(){
		IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        filter.addDataScheme("file");  
		getActivity().registerReceiver(mReceiver,filter);
    }

    @Override
    public void onStopTest(){
		try{
			getActivity().unregisterReceiver(mReceiver);
		}catch(Exception e){
			//ignore
		}
    }

    @Override
    public View getTestView(LayoutInflater inflater) {
        mTextView = new TextView(getActivity());
        mTextView.setTextSize(18);
        return mTextView;
    }

}
