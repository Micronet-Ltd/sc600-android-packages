
package src.com.lovdream.factorykit.items;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.lovdream.factorykit.TestItemBase;
import com.lovdream.factorykit.R;

public class GyroSensorTest extends TestItemBase implements SensorEventListener{

    private SensorManager mSensorManager;
    private TextView mX,mY,mZ;

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {
	}

	@Override
	public void onSensorChanged(SensorEvent sensorevent) {
		if((sensorevent.values == null) || (sensorevent.values.length < 3)){
			return;
		}

		if(mX != null){
			mX.setText("X:" + sensorevent.values[0]);
		}

		if(mY != null){
			mY.setText("Y:" + sensorevent.values[1]);
		}

		if(mZ != null){
			mZ.setText("Z:" + sensorevent.values[2]);
		}

		postSuccess();
	}

    @Override
    public String getKey() {
        return "gyro_sensor";
    }

    @Override
    public String getTestMessage() {
        return getString(R.string.gyro_test_msg);
    }

    @Override
    public void onStartTest() {
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onStopTest() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public View getTestView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.gyro_sensor_test,null,false);
		mX = (TextView)view.findViewById(R.id.gyro_x);
		mY = (TextView)view.findViewById(R.id.gyro_y);
		mZ = (TextView)view.findViewById(R.id.gyro_z);
        return view;
    }
}
