
package com.lovdream.factorykit.items;

import android.view.View;
import android.content.Context;
import android.widget.TextView;
import android.view.LayoutInflater;

import com.lovdream.util.SystemUtil;
import com.lovdream.factorykit.NvUtil;
import com.lovdream.factorykit.R;
import com.lovdream.factorykit.Utils;
import com.lovdream.factorykit.TestItemBase;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class TestFlag extends TestItemBase{

	@Override
	public String getKey(){
		return "test_flag";
	}

	@Override
	public String getTestMessage(){
		return "";
	}

	@Override
	public void onStartTest(){
	}

	@Override
	public void onStopTest(){
	}

	@Override
	public View getTestView(LayoutInflater inflater){
		byte[] flags = NvUtil.getNvFactoryData3IByte();

		if(flags == null){
			TextView tv = new TextView(getActivity());
			tv.setText(R.string.get_flag_error_mesg);
			return tv;
		}

		View v = inflater.inflate(R.layout.test_flag,null);
		updateViews(v,flags);
		return v;
	}

	private void updateViews(View v,byte[] flags){
		TextView calibration = (TextView)v.findViewById(R.id.calibration);
		calibration.setText(getActivity().getString(R.string.test_flag_calibration,getStatusString(flags,Utils.FLAG_INDEX_CALIBRATION)));

		TextView _2g3gft = (TextView)v.findViewById(R.id._2g_3g_ft);
		_2g3gft.setText(getActivity().getString(R.string.test_flag_2g_3g_ft,getStatusString(flags,Utils.FLAG_INDEX_2G_3G_FT)));

		TextView _4gft = (TextView)v.findViewById(R.id._4g_ft);
		_4gft.setText(getActivity().getString(R.string.test_flag_4g_ft,getStatusString(flags,Utils.FLAG_INDEX_4G_FT)));

		TextView pcba = (TextView)v.findViewById(R.id.flag_pcba);
		pcba.setText(getActivity().getString(R.string.test_flag_pcba,getStatusString(flags,Utils.FLAG_INDEX_PCBA)));

		TextView auto = (TextView)v.findViewById(R.id.flag_auto);
		auto.setText(getActivity().getString(R.string.test_flag_auto,getStatusString(flags,Utils.FLAG_INDEX_AUTO)));
	}

	private String getStatusString(byte[] flags,int index){
		if((index < 0) || (flags == null) || (index >= flags.length)){
			return getActivity().getString(R.string.test_flag_unknown_error);
		}

		char[] chars = bytesToChars(flags);

		int statId = R.string.test_flag_no_init;
		switch(chars[index]){
			case 'P':
				statId = R.string.test_flag_pass;
				break;
			case 'F':
				statId = R.string.test_flag_fail;
				break;
		}
		return getActivity().getString(statId);
	}
	
	private char[] bytesToChars(byte[] bytes){
		Charset cs = Charset.forName("UTF-8");
		ByteBuffer bb = ByteBuffer.allocate(bytes.length);
		bb.put(bytes);
		bb.flip();
		CharBuffer cb = cs.decode(bb);
		return cb.array();
	}
}
