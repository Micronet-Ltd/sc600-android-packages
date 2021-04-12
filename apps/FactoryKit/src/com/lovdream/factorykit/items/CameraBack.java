
package com.lovdream.factorykit.items;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.Toast;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Point;

import java.util.List;
import java.util.ArrayList;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.TestItemBase;
import com.lovdream.factorykit.libs.FocusImageView;
import java.lang.reflect.Method;

public class CameraBack extends TestItemBase implements SurfaceHolder.Callback{

    private Camera mCamera = null;
    private Button takeButton;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
	private FocusImageView mFocusImageView;
    private boolean bSurfaceDestroyed = false;

	@Override
	public String getKey(){
		return "camera_test_back";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.camera_test_mesg);
	}

	@Override
	public void onStartTest(){

		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View v = inflater.inflate(R.layout.camera_test,null);
        mSurfaceView = (SurfaceView) v.findViewById(R.id.camera_surface);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        takeButton = (Button) v.findViewById(R.id.take_picture);
        takeButton.setOnClickListener(this);
		mFocusImageView = (FocusImageView)v.findViewById(R.id.focus_image);

		showFullscreenOverlay(v,false);
	}

	@Override
	public void onStopTest(){
		stopCamera();
	}

	@Override
	public void onClick(View v){
		switch(v.getId()){
			case R.id.take_picture:
				takeButton.setVisibility(View.GONE);
				try {
					if (mCamera != null) {
						if(needAutoFocus(mCamera)){
							//mCamera.autoFocus(new AutoFocusCallback());
                                                                    takePicture();
 						}else {
							takePicture();
						}
					} else {
						fail(getString(R.string.camera_fail_open));
					}
				} catch (Exception e) {
					fail(getString(R.string.autofocus_fail));
				}
				return;
		}
		super.onClick(v);
	}

    private  Rect calculateTapArea(float x, float y, float coefficient, Camera.Size previewSize){
        Display display = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int screenHeight = display.getHeight();
        int screenWidth = display.getWidth();
        float focusAreaSize = 200;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int centerY = 0;
        int  centerX = 0;
        centerY = (int) (-x / screenWidth * 2000 + 1000);
        centerX = (int) (y / screenHeight * 2000 - 1000);
        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000);
        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

	@Override
	public boolean onTouch(View v, MotionEvent event){
		switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
                if(!bSurfaceDestroyed){
                   startFocus(new Point((int)event.getX(),(int)event.getY()));
                }
				break;
			default:
				break;
		}
		return true;
	}

	public void startFocus(Point point){

		Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback(){
			public void onAutoFocus(boolean focused, Camera camera) {
				if(focused) {
					mFocusImageView.onFocusSuccess();
				}else{
					mFocusImageView.onFocusFailed();
				}
			}
		};

		if(mCamera == null){
			return;
		}

        Camera.Parameters parameters = mCamera.getParameters();
        if(parameters.getMaxNumFocusAreas() <= 0){
            mCamera.autoFocus(autoFocusCallback);
            return;
        }

        mCamera.cancelAutoFocus();

        List<Camera.Area> areas = new ArrayList<Camera.Area>();
        List<Camera.Area> areasMetrix = new ArrayList<Camera.Area>();
        Camera.Size previewSize = parameters.getPreviewSize();
        Rect focusRect = calculateTapArea(point.x, point.y, 1.0f, previewSize);
        Rect metrixRect = calculateTapArea(point.x, point.y, 1.5f, previewSize);

        areas.add(new Camera.Area(focusRect, 1000));
        areasMetrix.add(new Camera.Area(metrixRect,1000));
        parameters.setMeteringAreas(areasMetrix);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        parameters.setFocusAreas(areas);
        try {
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        mCamera.autoFocus(autoFocusCallback);
		
		mFocusImageView.startFocus(point);
	}

	private boolean needAutoFocus(Camera camera){
		Camera.Parameters para = camera.getParameters();
		List<String> supported = para.getSupportedFocusModes();
		return supported == null ? false : supported.indexOf(Camera.Parameters.FOCUS_MODE_AUTO) >= 0;
	}
    private static final int CAMERA_HAL_API_VERSION_1_0 = 0x100;

	public int getCameraId(){
		return Camera.CameraInfo.CAMERA_FACING_BACK;
	}

	public int getRotation(){
		return 90;
	}

	public boolean isFlashModeOn(){
		return true;
	}

	@Override
    public void surfaceCreated(SurfaceHolder surfaceholder) {

        try {
            bSurfaceDestroyed = false;
            //mCamera = Camera.open(getCameraId());
            Method openMethod = Class.forName("android.hardware.Camera").getMethod(
                    "openLegacy", int.class, int.class);
            mCamera = (android.hardware.Camera) openMethod.invoke(
                    null,getCameraId(), CAMERA_HAL_API_VERSION_1_0);
			if(needAutoFocus(mCamera)){
				toast(getString(R.string.touch_to_focus));
				mSurfaceView.setOnTouchListener(this);
			}
        } catch (Exception exception) {
            fail(getString(R.string.camera_fail_open));
            mCamera = null;
			return;
        }

        if (mCamera == null) {
            fail(getString(R.string.camera_fail_open));
        } else {
            try {
                if(getCameraId()==0){
                mCamera.setDisplayOrientation(270);
                }
                mCamera.setPreviewDisplay(mSurfaceHolder);
            } catch (IOException exception) {
                mCamera.release();
                mCamera = null;
            	fail(getString(R.string.camera_fail_open));
            }
        }
    }
    
	@Override
    public void surfaceChanged(SurfaceHolder surfaceholder, int format, int w, int h) {
        startCamera();
    }

    public void surfaceDestroyed(SurfaceHolder surfaceholder) {

        stopCamera();
        bSurfaceDestroyed = true;
    }

    private void takePicture() {

        if (mCamera != null) {
            try {
                mCamera.takePicture(mShutterCallback, rawPictureCallback, jpegCallback);
            } catch (Exception e) {
                fail(getString(R.string.capture_fail));
            }
        } else {
            fail(getString(R.string.camera_fail_open));
        }
    }

    private ShutterCallback mShutterCallback = new ShutterCallback() {

        public void onShutter() {

            try {
				success();
            } catch (Exception e) {
				e.printStackTrace();
            }
        }
    };

    private PictureCallback rawPictureCallback = new PictureCallback() {

        public void onPictureTaken(byte[] _data, Camera _camera) {
            try {
				success();
            } catch (Exception e) {
				e.printStackTrace();
            }
        }
    };

    private PictureCallback jpegCallback = new PictureCallback() {

        public void onPictureTaken(byte[] _data, Camera _camera) {
            try {
				success();
            } catch (Exception e) {
				e.printStackTrace();
            }
        }
    };
    
    public final class AutoFocusCallback implements android.hardware.Camera.AutoFocusCallback {

        public void onAutoFocus(boolean focused, Camera camera) {
            if (focused) {
                takePicture();
            } else
                fail(getString(R.string.autofocus_fail));
        }
    };

    private void startCamera() {

        if (mCamera != null) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setPictureFormat(PixelFormat.JPEG);
				if(isFlashModeOn()){
                	parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
				}
                if(getCameraId()==1){
                    mCamera.setDisplayOrientation(getRotation());
                }
                mCamera.setParameters(parameters);
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void stopCamera() {

        if (mCamera != null) {
            try {
                mCamera.stopPreview();
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

	void success(){
		dismissOverlay();
		postSuccess();
	}

    void fail(Object msg) {
        toast(msg);
		dismissOverlay();
		postFail();
    }

    public void toast(Object s) {

        if (s == null)
            return;
        Toast.makeText(getActivity(), s + "", Toast.LENGTH_SHORT).show();
    }
}
