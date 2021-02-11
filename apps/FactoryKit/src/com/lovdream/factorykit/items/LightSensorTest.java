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

/**
 * Created by yangzhiming on 2017/6/16.
 */

public class LightSensorTest extends TestItemBase{
    private final SensorEventListener mLightSensorListener = new SensorListener();;
    private SensorManager mSensorManager;
    private Sensor mLightSensor;
    private TextView mTextView;

    @Override
    public String getKey() {
        return "light_sensor";
    }

    @Override
    public String getTestMessage() {
        return getString(R.string.light_sensor_msg);
    }

    @Override
    public void onStartTest() {
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }

    @Override
    public void onStopTest() {

    }

    @Override
    public View getTestView(LayoutInflater inflater) {
        mTextView = new TextView(getActivity());
        mTextView.setTextSize(18);
        return mTextView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(mLightSensorListener,
                mLightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mLightSensorListener);
    }

    class SensorListener implements SensorEventListener {

        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        public void onSensorChanged(SensorEvent sensorevent) {
            float f = sensorevent.values[0];
			if(mTextView != null){
            	mTextView.setText(String.valueOf(f));
			}
            if(f  > 1000){
                postSuccess();
            }else if (f > 0){
                enableSuccess(true);
            }
        }
    }
}
