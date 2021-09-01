package src.com.lovdream.factorykit.items;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.lovdream.factorykit.TestItemBase;
import com.lovdream.factorykit.R;

import java.io.FileReader;
import java.util.List;

/**
 * Created by yangzhiming on 2017/6/16.
 */

public class ProximitySenorTest extends TestItemBase implements Runnable{
    private static final String ALS_ADC = "sys/devices/virtual/input/input0/als_adc";
    private static final String PS_ADC = "sys/devices/virtual/input/input1/ps_adc";
    private static final int REFRESH_TEXT = 1;
    private boolean bStop;

    ImageView imageView[];
    Sensor mProimitySensor;
    private SensorEventListener mProximityListener;
    private SensorManager mSensorManager;
    TextView madc_values;
    String strPromityinfo;
    TextView mtv_pro_info;
    private List<Sensor> mSensorList;

    private void initAllControl(View view) {
        imageView = new ImageView[2];
        imageView[0] = (ImageView) view.findViewById(R.id.test_proximitysensor_gray);
        imageView[1] = (ImageView) view.findViewById(R.id.test_proximitysensor_green);
        strPromityinfo = getString(R.string.distance_sensor_msg);
        madc_values = (TextView) view.findViewById(R.id.adc_values);
        mtv_pro_info = (TextView) view.findViewById(R.id.tv_proximitysenor_info);
        //madc_values.setVisibility(View.GONE);
        mProximityListener = new SenListener();
    }

    @Override
    public View getTestView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.proximity_senor_test,null,false);
        initAllControl(view);
        return view;
    }

    @Override
    public String getKey() {
        return "distance_sensor";
    }

    @Override
    public String getTestMessage() {
        return getString(R.string.distance_sensor_msg);
    }

    @Override
    public void onStartTest() {
        bStop = false;
    }

    @Override
    public void onStopTest() {
        bStop = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSensorManager = (SensorManager) getActivity().getSystemService("sensor");
        //fix bug about mProimitySensor by zzj start
        mSensorList= mSensorManager.getSensorList(Sensor.TYPE_ALL);
        mProimitySensor = mSensorManager.getDefaultSensor(8);
        mSensorManager.registerListener(mProximityListener,
                mProimitySensor, 3);
    }

    @Override
    public void onPause() {
        super.onPause();
        SensorManager sensormanager = mSensorManager;
        SensorEventListener sensoreventlistener = mProximityListener;
        sensormanager.unregisterListener(sensoreventlistener);
    }

    @Override
    public void run() {
        while(!bStop)
        {
            try {
                FileReader file_als = new FileReader(ALS_ADC);
                FileReader file_ps = new FileReader(PS_ADC);
                char[] buffer_als = new char[64];
                char[] buffer_ps = new char[64];
                int len_ls = file_als.read(buffer_als, 0, 64);
                int len_ps = file_ps.read(buffer_ps, 0, 1);
                String value_als = (new String(buffer_als, 0, len_ls)).trim();
                String value_ps = (new String(buffer_ps, 0, len_ps)).trim();
                mRefreshHandler.sendMessage(mRefreshHandler
                        .obtainMessage(REFRESH_TEXT, value_als+"\n"+value_ps));
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    Handler mRefreshHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case REFRESH_TEXT:
                    madc_values.setText(msg.obj.toString());
                    //madc_values.setVisibility(View.VISIBLE);
            }
        }
    };

    private class SenListener implements SensorEventListener{
        private boolean mIsSuccess;

        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        public void onSensorChanged(SensorEvent sensorevent) {
            StringBuilder stringbuilder;
            String s1;
            if (sensorevent.values[0] < 5F) {
                imageView[0].setVisibility(View.INVISIBLE);
                imageView[1].setVisibility(View.VISIBLE);
                enableSuccess(true);
                mIsSuccess = true;
            } else {
                imageView[0].setVisibility(View.VISIBLE);
                imageView[1].setVisibility(View.INVISIBLE);

            }
            stringbuilder = new StringBuilder();
            s1 = stringbuilder.append(sensorevent.values[0]).toString();
            mtv_pro_info.setText(s1);

            if(mIsSuccess && sensorevent.values[0] >=5F){
                setAutoTest(true);
                mRefreshHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        postSuccess();
                    }
                },500);
            }
        }

    }
}
