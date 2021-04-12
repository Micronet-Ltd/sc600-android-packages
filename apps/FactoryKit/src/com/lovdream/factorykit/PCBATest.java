package com.lovdream.factorykit;

import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.RemoteException;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.Activity;
import android.app.ListFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.widget.Toast;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.BaseAdapter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.swfp.utils.SimUtil;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.lovdream.util.SystemUtil;
import com.lovdream.factorykit.Config.TestItem;
import com.lovdream.factorykit.libs.GridFragment;
import com.swfp.model.WifiModel;
import com.swfp.model.WifiModel.WifiBack;
import com.swfp.utils.ProjectControlUtil;
import com.swfp.utils.WifiUtil;

@SuppressLint("NewApi")
public class PCBATest extends GridFragment implements
        TestItemBase.TestCallback, View.OnClickListener {

    private static final String TAG = Main.TAG;

    private TestItemFactory mFactory;
    private ArrayList<TestItem> pcbaItems;
    private View mFooter;
    private Config mConfig;// = Config.getInstance(mActivity);
    private Activity mActivity;
    private boolean isInSPCBA =false;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        isInSPCBA = Utils2.getInstance().currentTestMode==Utils2.SPCBA;
        
        mActivity =getActivity();
        FactoryKitApplication app = (FactoryKitApplication) mActivity
                .getApplication();
        Config config = app.getTestConfig();

        pcbaItems = new ArrayList<TestItem>();
        ArrayList<TestItem> allItems = config.getTestItems();
        for (TestItem item : allItems) {
        	if(isInSPCBA){
        		if (item.inPCBATest && item.inSPCBATest) {
                    pcbaItems.add(item);
                }
        	}else{
        		if (item.inPCBATest) {
                    pcbaItems.add(item);
                }
        	}
            
        }
        mConfig = Config.getInstance(mActivity);
        TestItem _4gft = new TestItem("4gft", "4G FT",true);
        pcbaItems.add(_4gft);

        updateFooter(config);

        setGridAdapter(new MyAdapter(mActivity, pcbaItems));
        Log.i(TAG, "pcba test launched");
        mHandler = new MyHandler();
        mFactory = TestItemFactory.getInstance(mActivity);

    }

    @Override
    public void onStart() {
    super.onStart();
    //add by xxf
    if(ProjectControlUtil.isPcbaHasAutoTest()){
     //add by xxf
            AutoTest();
            initGps();
            mConfig=Config.getInstance(mActivity);
            if (mConfig.getPCBAFlag(findIndex("wifi_test")) == Config.TEST_FLAG_NO_TEST) {
                startWifi(getActivity());
            } 
            if(mConfig.getPCBAFlag(findIndex("wifi_5g_test")) == Config.TEST_FLAG_NO_TEST){
                start5GWifi(getActivity());
            }
    }
}

    private void AutoTest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.w(TAG, "CompressTest");
                initCompass();
                if (mConfig.getPCBAFlag(findIndex("sim_test")) != Config.TEST_FLAG_PASS) {
                    initSIMTest();
                }
                initBt();
                //initSARsensor();
                init4GFT();
            }
        }).start();
    }

    private static final int MSG_GPS_TEST = 1;
    private static final int MSG_COMPASS_TEST = 2;
    private static final int MSG_SIM_TEST = 3;
    private static final int MSG_BT_TEST = 4;
    private static final int MSG_SAR_TEST = 5;
    private static final int MSG_4GTF_TEST = 6;
    private MyHandler mHandler;
    private static final String isTestPass = "isTestPass";

    private final class MyHandler extends Handler {

        public void handleMessage(Message msg) {
            final int what = msg.what;
            boolean isPass = msg.getData().getBoolean(isTestPass, false);
            mConfig = Config.getInstance(mActivity);
            switch (what) {
            case MSG_COMPASS_TEST:
                mConfig.savePCBAFlag(findIndex("compass"), isPass);
                break;
            case MSG_4GTF_TEST:
                mConfig.savePCBAFlag(findIndex("test_flag"), isPass);
                break;
            case MSG_SIM_TEST:
                mConfig.savePCBAFlag(findIndex("sim_test"), isPass);
                break;
            case MSG_BT_TEST:
                mConfig.savePCBAFlag(findIndex("bt_test"), isPass);
                break;
            case MSG_SAR_TEST:
                mConfig.savePCBAFlag(findIndex("sarsensor_test"), isPass);
                break;
        }
            ((MyAdapter) getGridAdapter()).notifyDataSetChanged();
    }
  }
    
    private void updateUI(){
    	 ((MyAdapter) getGridAdapter()).notifyDataSetChanged();
    }

    private int findIndex(String key) {
        for (TestItem item : pcbaItems) {
            if (item.key.equals(key)) {
                return item.fm.pcbaFlag;
            }
        }
        return -1;
    }

    private void updateFooter(Config config) {
        if ((pcbaItems == null) || (pcbaItems.size() == 0)) {
            return;
        }

        if (config.get4GftStatus() != Config.TEST_FLAG_PASS) {
            return;
        }

        for (TestItem item : pcbaItems) {
        	if(item.fm.key!=null && (!item.fm.key.equals("4gft"))){
        		 if (config.getPCBAFlag(item.fm.pcbaFlag) != Config.TEST_FLAG_PASS) {
        			 config.setPCBAFt(false);
                     mFooter.setVisibility(View.GONE);
                     return;
                 }
        	}
           
        }
        config.setPCBAFt(true);
        mFooter.setVisibility(View.VISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        v.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        mFooter = v.findViewById(R.id.footer);
        v.findViewById(R.id.footer_button).setOnClickListener(this);
        return v;
    }

    @Override
    public void onStop() {
    super.onStop();
    if(ProjectControlUtil.isC601 || ProjectControlUtil.isC802){

            
    stopWifi();
    stopWifi_5G();
            mSensorManager.unregisterListener(mListener);
            stopGPS();

        }
    }

    @Override
    public void onClick(View v) {
        PowerManager pm = (PowerManager) mActivity.getSystemService(
                Context.POWER_SERVICE);
        pm.shutdown(false, null, false);
    }
    private void stopGPS() {
        try {
            mLocationManager.removeUpdates(mLocationListener);
            mLocationManager.removeGpsStatusListener(mStatusListener);
        } catch (Exception e) {
        }
    }

    @Override
    public void onGridItemClick(GridView l, View v, int position, long id) {
        super.onGridItemClick(l, v, position, id);
        TestItem item = (TestItem) v.getTag();
        if (item == null) {
            return;
        }

        if ("4gft".equals(item.key)) {
            return;
        }

        TestItemBase fragment = mFactory.createTestItem(mActivity, item);

        if (fragment == null) {
            Toast.makeText(mActivity, R.string.no_item, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        if (fragment.isAdded()) {
            return;
        }

        setHasOptionsMenu(false);
        fragment.setTestCallback(this);
        fragment.setAutoTest(false);
        fragment.setPCBATest(true);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(android.R.id.content, fragment, item.key);
        ft.addToBackStack(item.key);
        ft.commit();
        mActivity.setTitle(item.displayName);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.pcba_test);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity.setTitle(R.string.app_name);
        ((MyAdapter) getGridAdapter()).notifyDataSetChanged();
    }

    private void savePCBAFtIfNeed() {
        if ((pcbaItems == null) || (pcbaItems.size() == 0)) {
            return;
        }
        Config config = Config.getInstance(mActivity);

        if (config.get4GftStatus() != Config.TEST_FLAG_PASS) {
            return;
        }

        for (TestItem item : pcbaItems) {
        	if(item.fm.key!=null && (!item.fm.key.equals("4gft"))){
        		  if (config.getPCBAFlag(item.fm.pcbaFlag) != Config.TEST_FLAG_PASS) {
                      config.setPCBAFt(false);
                      mFooter.setVisibility(View.GONE);
                      return;
                  }
        	}
        }

        config.setPCBAFt(true);
        mFooter.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(R.string.clear_test_result);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Config.getInstance(mActivity).clearPCBAFlag();
        ((MyAdapter) getGridAdapter()).notifyDataSetChanged();
        updateFooter(Config.getInstance(mActivity));
        return true;
    }

    @Override
    public void onTestFinish(TestItemBase item) {
        item.setPCBATest(false);
        if(mActivity!=null)
        mActivity.setTitle(R.string.pcba_test);
        setHasOptionsMenu(true);
        ((BaseAdapter) getGridAdapter()).notifyDataSetChanged();
        savePCBAFtIfNeed();
    }

    public static class MyAdapter extends BaseAdapter {

        ArrayList<TestItem> mItems;
        LayoutInflater mInflater;
        Config mConfig;

        public MyAdapter(Context context, ArrayList<TestItem> items) {
            super();
            mItems = items;
            mInflater = LayoutInflater.from(context);
            mConfig = Config.getInstance(context);
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int positon, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.test_grid_item, null);
            }

            TestItem item = mItems.get(positon);
            TextView tv = (TextView) convertView.findViewById(R.id.item_text);
            tv.setText(item.displayName);

            int flag;
            if ("4gft".equals(item.key)) {
                flag = mConfig.get4GftStatus();
            } else {
                flag = mConfig.getPCBAFlag(item.fm.pcbaFlag);
            }

            if (flag == Config.TEST_FLAG_PASS) {
                tv.setTextColor(Color.rgb(0, 100, 0));
            } else if (flag == Config.TEST_FLAG_FAIL) {
                tv.setTextColor(Color.RED);
            } else {
                tv.setTextColor(Color.BLACK);
            }

            convertView.setTag(item);

            return convertView;
        }
    }

    private void init4GFT() {
        Log.w(TAG, "init4GFT");
        byte[] flags = NvUtil.getNvFactoryData3IByte();
        Message m = mHandler.obtainMessage(MSG_4GTF_TEST, 0, 0, null);
        m.getData().putBoolean(isTestPass,
                getStatusString(flags, Utils.FLAG_INDEX_4G_FT));
        m.sendToTarget();
    }

    private boolean getStatusString(byte[] flags, int index) {
        if ((index < 0) || (flags == null) || (index >= flags.length)) {
            return false;
        }

        char[] chars = bytesToChars(flags);

        boolean result = false;
        switch (chars[index]) {
        case 'P':
            result = true;
            break;
        case 'F':
            result = false;
            break;
        }
        return result;
    }

    private char[] bytesToChars(byte[] bytes) {
        Charset cs = Charset.forName("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes);
        bb.flip();
        CharBuffer cb = cs.decode(bb);
        return cb.array();
    }

    private void initSIMTest() {
        Log.w(TAG, "initSIMTest");
        Message m = mHandler.obtainMessage(MSG_SIM_TEST, 0, 0, null);
        m.getData().putBoolean(isTestPass, SimUtil.getInstance(getActivity()).searchSim().isSimReady);
        m.sendToTarget();
    }

    private Sensor mOrieSensor;
    private SensorEventListener mOrieSensorListener = new OrieSensorListener();
    private SensorManager mSensorManager;

    private void initCompass() {
        Log.w(TAG, "initCompass");
        if (mSensorManager == null) {
            mSensorManager = (SensorManager) mActivity.getSystemService(
                    "sensor");
        }
        mOrieSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mSensorManager.registerListener(mOrieSensorListener, mOrieSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @SuppressLint("NewApi")
    private class OrieSensorListener implements SensorEventListener {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }

        @SuppressLint("NewApi")
        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
            if (Math.abs(event.values[0]) > 1) {
                Message m = mHandler
                        .obtainMessage(MSG_COMPASS_TEST, 0, 0, null);
                m.getData().putBoolean(isTestPass, true);
                m.sendToTarget();
                mSensorManager.unregisterListener(mOrieSensorListener);
            }
        }
    }

    private final SensorEventListener mListener = new SensListener();
    private float mValues[];
    private boolean bSuccess;

    private void initSARsensor() {
        Log.w(TAG, "initSARsensor");
        bSuccess = false;
        mSensorManager = (SensorManager) mActivity.getSystemService(
                Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            Sensor sensor = mSensorManager.getDefaultSensor(33171015);
            mSensorManager.registerListener(mListener, sensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private class SensListener implements SensorEventListener {

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            mValues = event.values;
            StringBuilder stringbuilder = (new StringBuilder()).append("X = ");
            float f = mValues[0];
            String s = stringbuilder.append(f).toString();
            int[] isTrue = { 0, 0 };
            if (1 == f) {
                if (1 != isTrue[1]) {
                    isTrue[1] = 1;
                }
            } else if (0 == f) {
                if (1 != isTrue[0]) {
                    isTrue[0] = 1;
                }
            }
            if ((isTrue[0] == 1 || isTrue[1] == 1) && !bSuccess) {
                bSuccess = true;
                Message m = mHandler.obtainMessage(MSG_SAR_TEST, 0, 0, null);
                m.getData().putBoolean(isTestPass, true);
                m.sendToTarget();
            }
            return;
        }
    }

    BluetoothAdapter mBluetoothAdapter = null;
    List<DeviceInfo> mDeviceList = new ArrayList<DeviceInfo>();
    Set<BluetoothDevice> bondedDevices;
    IntentFilter filter = null;
    private final static int MIN_COUNT = 1;

    private void initBt() {
        Log.w(TAG, "initBt");
        BluetoothManager mBluetoothManager = (BluetoothManager) mActivity
                .getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager == null) {
            return;
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            return;
        }
        startScanAndUpdateAdapter();
        if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
            scanDevices();
        } else {
            if (mBluetoothAdapter.getState() != BluetoothAdapter.STATE_TURNING_ON) {
                mBluetoothAdapter.enable();
            }
        }

        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mActivity.registerReceiver(mReceiver, filter);
    }

    private void startScanAndUpdateAdapter() {
        mDeviceList.clear();
        bondedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : bondedDevices) {
            DeviceInfo deviceInfo = new DeviceInfo(device.getName(),
                    device.getAddress());
            mDeviceList.add(deviceInfo);
        }
    }

    private void scanDevices() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent
                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                mDeviceList.add(new DeviceInfo(device.getName(), device
                        .getAddress()));
                if (mDeviceList.size() >= MIN_COUNT) {
                    Message m = mHandler.obtainMessage(MSG_BT_TEST, 0, 0, null);
                    m.getData().putBoolean(isTestPass, true);
                    m.sendToTarget();
                    /*if (mReceiver != null) {
                        mActivity.unregisterReceiver(mReceiver);
                    }*/
                    if (mBluetoothAdapter != null) {
                        mBluetoothAdapter.disable();
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
                if (mDeviceList.size() >= MIN_COUNT) {
                    Message m = mHandler.obtainMessage(MSG_BT_TEST, 0, 0, null);
                    m.getData().putBoolean(isTestPass, true);
                    m.sendToTarget();
                    /*if (mReceiver != null) {
                        mActivity.unregisterReceiver(mReceiver);
                    }*/
                    if (mBluetoothAdapter != null) {
                        mBluetoothAdapter.disable();
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                startScanAndUpdateAdapter();
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                if (BluetoothAdapter.STATE_ON == intent.getIntExtra(
                        BluetoothAdapter.EXTRA_STATE, 0)) {
                    scanDevices();
                    if (BluetoothAdapter.STATE_OFF == intent.getIntExtra(
                            BluetoothAdapter.EXTRA_STATE, 0)) {
                        mBluetoothAdapter.enable();
                    }
                }
            }
        }
    };

    class DeviceInfo {
        private String name = "";
        private String address = "";

        public DeviceInfo(String name, String address) {
            super();
            this.name = name;
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    private LocationManager mLocationManager;
    private GpsStatus.Listener mStatusListener = new GpsStatusListner();
    private final LocationListener mLocationListener = new LocaltionLis();

    private void initGps() {
        Log.w(TAG, "initGps");
        mLocationManager = (LocationManager) mActivity.getSystemService(
                Context.LOCATION_SERVICE);
        if (!mLocationManager
                .isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            mConfig.savePCBAFlag(findIndex("gps_test"), false);
            ((MyAdapter) getGridAdapter()).notifyDataSetChanged();
            return;
        }

        Criteria criteria;
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        String provider = mLocationManager.getBestProvider(criteria, true);
        if (provider == null) {
            mConfig.savePCBAFlag(findIndex("gps_test"), false);
            ((MyAdapter) getGridAdapter()).notifyDataSetChanged();
        }
        mLocationManager.requestLocationUpdates(provider, 500, 0,
                mLocationListener);
        mLocationManager.addGpsStatusListener(mStatusListener);
    }

    private class LocaltionLis implements LocationListener {
        public void onLocationChanged(Location location) {
            mConfig.savePCBAFlag(findIndex("gps_test"), true);
            ((MyAdapter) getGridAdapter()).notifyDataSetChanged();
        }

        public void onProviderDisabled(String s) {
        }

        public void onProviderEnabled(String s) {
        }

        public void onStatusChanged(String s, int i, Bundle bundle) {
        }
    }
    private GpsStatus gpsStatus;
    final int MIN_SAT_NUM = 3;
    final int MIN_VALID_SAT_NUM = 2;
    final float VALID_SNR = 36;
    private GpsStatus mGpsStatus;
    private Iterable<GpsSatellite> mSatellites;

    private class GpsStatusListner implements GpsStatus.Listener {
        @Override
        public void onGpsStatusChanged(int status) {
            // TODO Auto-generated method stub
            gpsStatus = mLocationManager.getGpsStatus(gpsStatus);
            mConfig = Config.getInstance(mActivity);
            switch (status) {
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                mConfig.savePCBAFlag(findIndex("gps_test"), true);
                mLocationManager.removeUpdates(mLocationListener);
                mLocationManager.removeGpsStatusListener(mStatusListener);
                break;
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                mGpsStatus = mLocationManager.getGpsStatus(null);
                mSatellites = mGpsStatus.getSatellites();
                Iterator<GpsSatellite> it = mSatellites.iterator();
                int count = 0;
                int validCount = 0;
                while (it.hasNext()) {
                    GpsSatellite gpsS = (GpsSatellite) it.next();
                    float snr = gpsS.getSnr();
                    if (snr > 0) {
                        count++;
                    }
                    if (snr > VALID_SNR) {
                        validCount++;
                    }
                }
                if ((count > MIN_SAT_NUM) && (validCount > MIN_VALID_SAT_NUM)) {
                    mConfig.savePCBAFlag(findIndex("gps_test"), true);
                }
                break;
            }
            ((MyAdapter) getGridAdapter()).notifyDataSetChanged();
        }
    }



   
	private void startWifi(Context mContext) {
		WifiModel wifiModel = new WifiModel(new WifiBack() {

			@Override
			public void showMsg(String msg) {

			}

			@Override
			public void pass() {
				mConfig.savePCBAFlag(findIndex("wifi_test"), true);
				stopWifi();
			}

			@Override
			public boolean isFrequencyNotErr(int fre) {
				return fre>2000 && fre<5000;
			}

			@Override
			public void failed(String msg) {
				mConfig.savePCBAFlag(findIndex("wifi_test"), false);
				stopWifi();
			}

			@Override
			public void toast(String msg) {
				// TODO Auto-generated method stub
				
			}
		});
		wifiModel.testSSID = "lovdream";
		wifiUtil = new WifiUtil(mContext, wifiModel).start();

	}

	
	WifiUtil wifiUtil;
	WifiUtil wifiUtil_5g;
	
	private void stopWifi(){
		try {
			updateUI();
			if (wifiUtil != null)
				wifiUtil.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void stopWifi_5G(){
		try {
			updateUI();
			if (wifiUtil_5g != null)
				wifiUtil_5g.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void start5GWifi(Context mContext) {
		WifiModel wifiModel = new WifiModel(new WifiBack() {

			@Override
			public void showMsg(String msg) {

			}

			@Override
			public void pass() {
				mConfig.savePCBAFlag(findIndex("wifi_5g_test"), true);
				stopWifi_5G();
			}

			@Override
			public boolean isFrequencyNotErr(int fre) {
				return fre>5000;
			}

			@Override
			public void failed(String msg) {
				mConfig.savePCBAFlag(findIndex("wifi_5g_test"), false);
				stopWifi_5G();
			}

			@Override
			public void toast(String msg) {
				// TODO Auto-generated method stub
				
			}
		});
		wifiModel.testSSID = "lovdream5G";
		wifiUtil_5g = new WifiUtil(mContext, wifiModel).start();
	}



}
