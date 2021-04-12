
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
import android.os.Handler;
import android.os.Message;

public class BarometerTest extends TestItemBase implements SensorEventListener{

    private SensorManager mSensorManager;
    private TextView mTextView;
    private MyHandler mHandler;
    private final class MyHandler extends Handler {

        public void handleMessage(Message msg) {
            switch(msg.what){
                case 0:
                    postSuccess();
            }
        }
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {
	}

	@Override
	public void onSensorChanged(SensorEvent sensorevent) {
		float value = sensorevent.values[0];
                    if(mTextView != null){
                        mTextView.setText(String.valueOf(value) + " (hPa)");
		}
		if(value > 500){
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(0), 2000);
		}
	}

    @Override
    public String getKey() {
        return "barometer_test";
    }

    @Override
    public String getTestMessage() {
        return getString(R.string.barometer_test_msg);
    }

    @Override
    public void onStartTest() {
    mHandler = new MyHandler();
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        mSensorManager.registerListener(this,sensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onStopTest() {
        mSensorManager.unregisterListener(this);
        mHandler.removeMessages(0);
    }

    @Override
    public View getTestView(LayoutInflater inflater) {
        mTextView = new TextView(getActivity());
        mTextView.setTextSize(18);
        return mTextView;
    }

}
