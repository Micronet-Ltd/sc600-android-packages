package com.lovdream.factorykit.items;

import android.view.View;
import android.view.MotionEvent;
import android.view.LayoutInflater;
import android.content.Context;
import android.widget.TextView;
import android.os.storage.StorageManager;
import android.os.storage.StorageEventListener;
import android.os.storage.VolumeInfo;
import android.os.SystemProperties;
import android.util.Log;

import java.util.List;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.TestItemBase;

public class OtgTest extends TestItemBase implements View.OnHoverListener{
	
	private StorageManager mStorageManager;
	private boolean usbVolumeDetected = false;
	
	private final StorageEventListener mListener = new StorageEventListener(){
		@Override
		public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState){
			if(isUsbVolumeMounted(vol)){
				toast(R.string.otg_detected_msg);
				postSuccess();
			}
		}
	};

	private boolean isUsbVolumeMounted(VolumeInfo vol){
		if(vol.getDisk() != null && vol.getDisk().isUsb() && isVolumeMounted(vol)){
			return true;
		}
		return false;
	}

	private boolean isVolumeMounted(VolumeInfo vol){
		return vol.getState() == VolumeInfo.STATE_MOUNTED || vol.getState() == VolumeInfo.STATE_MOUNTED_READ_ONLY;
	}

	@Override
	public String getKey(){
		return "otg_test";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.otg_test_mesg);
	}

	@Override
	public void onStartTest(){
	
        if(SystemProperties.getInt("hw.board.id", 0) >= 2){
            postSuccess();
        } else {
            mStorageManager = getActivity().getSystemService(StorageManager.class);
            mStorageManager.registerListener(mListener);

            List<VolumeInfo> vols = mStorageManager.getVolumes();
            for(VolumeInfo vol : vols){
                if(isUsbVolumeMounted(vol)){
                    usbVolumeDetected = true;
                }
            }
		}
	}

	@Override
	public void onStopTest(){
        if(SystemProperties.getInt("hw.board.id", 0) < 2){
            mStorageManager.unregisterListener(mListener);
            usbVolumeDetected = false;
        }        
		
	}

	@Override
	public View getTestView(LayoutInflater inflater){
		View v = inflater.inflate(R.layout.otg_test,null);
		v.findViewById(R.id.mouse_area).setOnHoverListener(this);
		if(usbVolumeDetected){
			toast(R.string.otg_detected_msg);
			postSuccess();
		}
		return v;
	}

	@Override
	public boolean onHover(View v, MotionEvent event){
		if(MotionEvent.ACTION_HOVER_MOVE == event.getAction()){
			postSuccess();
		}
		return true;
	}
}
