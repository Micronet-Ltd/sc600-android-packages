
/* copy from lovdream.cit*/
package com.lovdream.factorykit.libs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.lovdream.factorykit.R;
	
public class CompassView extends SurfaceView implements SurfaceHolder.Callback,
	SensorEventListener {

	SurfaceHolder mholder;
	Bitmap background1;
	SensorManager sensorManager;
	Sensor orieSensor;
	//fix bug about update button by zzj start
	SensorChangeListener mChangeListener;
	
	public interface SensorChangeListener{
		public void updateState(boolean state);
	};
	
	public void setSensorChangeListener(SensorChangeListener sensorChangeListener ){
		mChangeListener=sensorChangeListener;
	}
	//fix bug about update button by zzj end
	float degree = 0;
	private static final Object LOCK = new Object();	

	public CompassView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		background1 = BitmapFactory.decodeResource(this.getResources(),R.drawable.compass);
		background1 = small(background1);
		getHolder().addCallback(this);

		sensorManager = (SensorManager) context
		.getSystemService(Context.SENSOR_SERVICE);
		orieSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
	
		if (orieSensor != null) {
		sensorManager.registerListener(this, orieSensor,
		SensorManager.SENSOR_DELAY_NORMAL);
		}
	}
	
	private static Bitmap small(Bitmap bitmap) {
		Matrix matrix = new Matrix();
		matrix.postScale(0.6f,0.6f); 
		Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
		return resizeBmp;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		synchronized (LOCK) {
			mholder = holder;
		}
		// PaintThread thread= new PaintThread();
		// thread.start();

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		synchronized (LOCK) {
			mholder = null;
		}
	}

	public void removeSensorListener() {
		sensorManager.unregisterListener(this);

	}

	// class PaintThread extends Thread{
	// public void run() {
//	        
	// }
	// }


	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// event.timestamp The time in nanosecond at which the event
		// happened
	
		if ((Math.abs(event.values[1]) > 32 && Math.abs(event.values[1]) < 170) || // 围绕x轴的偏转(-180 to 180)
		Math.abs(event.values[2]) > 20) { // 围绕y轴的偏转 (-90 to 90),
			// event.values[0] Azimuth, angle between the magnetic north
			// direction and the Y axis, around the Z axis (0 to 359). 0=North,
			// 90=East, 180=South, 270=West
			synchronized (LOCK) {
				if (mholder != null) {
				Canvas can = mholder.lockCanvas();
				if (can == null)
				return;
				can.drawRGB(255, 255, 255);
				Paint paint = new Paint();
				paint.setFlags(Paint.ANTI_ALIAS_FLAG);
				paint.setColor(Color.RED);
				paint.setTextSize(60);
				can.drawText(getContext().getString(R.string.compass_screen_warning), 20, getHeight()/2, paint);
				//fix bug about update button by zzj start
                                if(mChangeListener !=null){
				mChangeListener.updateState(false);
                                }
				//fix bug about update button by zzj end
				mholder.unlockCanvasAndPost(can);
				}
			}
		} else {
			synchronized (LOCK) {
				if (mholder != null &&
					(Math.abs (event.values[0] - degree) > 0.1)) { //避免屏幕不动时重复绘制
					
					degree = event.values[0];
				
					Canvas can = mholder.lockCanvas();
					if (can == null)
					return;
					can.drawRGB(255, 255, 255);
					Paint paint = new Paint();
					paint.setFlags(Paint.ANTI_ALIAS_FLAG);
					paint.setColor(Color.GREEN);
					// can.drawLine(0, 0, 100, 100, paint);
					can.save();
					can.rotate(-1 * event.values[0], getWidth() / 2,
					getHeight() / 2);
					Bitmap bitmap = background1;
					can.drawBitmap(bitmap, getWidth() / 2
					- bitmap.getWidth() /2, getHeight() / 2
					- bitmap.getHeight() /2, null);
					can.restore();
					mholder.unlockCanvasAndPost(can);	
					//fix bug about update button by zzj start
                                       if(mChangeListener!=null){
					mChangeListener.updateState(true);
                                        }
					//fix bug about update button by zzj end
					
				}
			}
		
		}
	}

} 
