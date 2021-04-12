
package com.lovdream.factorykit.items;

import android.view.View;
import android.view.LayoutInflater;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.hardware.fingerprint.FingerprintManager;
import android.widget.TextView;
import android.widget.ImageView;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.ViewGroup.LayoutParams;
import android.util.DisplayMetrics;

import com.swfp.device.DeviceManager;
import com.swfp.device.DeviceManager.IFpCallBack;
import com.swfp.utils.Utils;
import com.swfp.utils.MessageType;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.TestItemBase;

public class FingerprintTest extends TestItemBase{

	DeviceManager mManager;
	ImageView fingerImage;
	TextView fingerState;

	private int image_col = 112;
	private int image_row = 112;

	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){

			Log.d(TAG,"finger test handler,msg what:" + msg.what + " arg1:" + msg.arg1);

			switch(msg.what){
				case MessageType.FP_MSG_FINGER:
					if(msg.arg1 == MessageType.FP_MSG_FINGER_WAIT_TOUCH){
						setState(R.string.fingerdown);

					}else if(msg.arg1 == MessageType.FP_MSG_FINGER_LEAVE){
						setState(R.string.fingerdown);
						startScanImg();
					}
					break;
				case MessageType.FP_MSG_TEST:
					if(msg.arg1 == MessageType.FP_MSG_TEST_IMG_QUALITY){
						setState(R.string.fingerup);

						byte[] tmp = new byte[image_col * image_row];

						if(getFingerPrintImage(tmp,image_col,image_row) == 0){
							byte[] img = Utils.translateImageCode(tmp,image_col,image_row);
							setImage(Utils.rotateBitmap(BitmapFactory.decodeByteArray(img, 0, img.length), 270f));
							postSuccess();
						}
						waitFingerLeave();
					}
					break;
			}
		}
	};

 	private void startScanImg() {
        byte[] buf = new byte[8];
        mManager.sendCmd(MessageType.FP_MSG_TEST_IMG_QUALITY, 0, buf, new int[]{buf.length});
    }

	@Override
	public String getKey(){
		return "fingerprint_test";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.fingerprint_test_mesg);
	}

	@Override
	public void onStartTest(){
		FingerprintManager fm = (FingerprintManager)getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
		if(!fm.isHardwareDetected()){
			toast(R.string.fingerprint_no_sensor);
			postFail();
			return;
		}
		
		mManager = DeviceManager.getDeviceManager(getActivity().getApplicationContext());
		mManager.registerFpCallBack(new IFpCallBack() {
			@Override
			public void sendMessageToClient(Message msg) {
				mHandler.sendMessage(msg);
			}
		});
		
		mManager.checkConnectToServer();
		initImageConfig();
		startScanImg();
	}

	@Override
	public void onStopTest(){
		if(mManager != null){
			mManager.disConnect();
		}
	}

	@Override
	public View getTestView(LayoutInflater inflater){
		View v = inflater.inflate(R.layout.fingerprint_test,null);
		fingerState = (TextView)v.findViewById(R.id.finger_state);
		fingerImage = (ImageView)v.findViewById(R.id.finger_img);
		initImageView(fingerImage);
		return v;
	}

	private void initImageConfig(){
		int[] col = new int[1];
		int[] row = new int[1];
		if(mManager.getImgInfo(col,row) == 0){
			image_col = col[0];
			image_row = row[0];
		}
	}

	private void initImageView(ImageView view){
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		int scale = (dm.widthPixels / this.image_row) < (dm.heightPixels / this.image_col) ? (dm.widthPixels / this.image_row) : (dm.heightPixels / this.image_col);
		LayoutParams param = view.getLayoutParams();
		param.width = image_row * scale * 2 / 3;
		param.height = image_col * scale * 2 / 3;
		view.setLayoutParams(param);
	}

	private void setState(int resId){
		if(fingerState != null){
			fingerState.setText(resId);
		}
	}

	private void setImage(Bitmap img){
		if(fingerImage != null){
			fingerImage.setImageBitmap(img);
		}
	}

    public int getFingerPrintImage(byte[] fingerImage, int col, int row) {
		return mManager.sendCmd(MessageType.FP_MSG_TEST_READ_IMG, 0, fingerImage, new int[]{col * row});
    }

	private void waitFingerLeave(){
		try{
			if(mManager != null){
				mManager.waitLeave();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
