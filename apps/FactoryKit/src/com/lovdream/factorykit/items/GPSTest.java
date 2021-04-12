package com.lovdream.factorykit.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.os.SystemProperties;
import android.R.drawable;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.GnssStatus;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.TestItemBase;
import com.swfp.utils.ProjectControlUtil;

public class GPSTest extends TestItemBase{

	@Override
	public String getKey(){
		return "gps_test";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.gps_test_mesg);
	}

	@Override
	public void onStartTest(){
		mContext = getActivity();
		mInflater = LayoutInflater.from(mContext);

		mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		if (mLocationManager == null) {
			fail("Fail to get LOCATION_SERVICE!");
		}

	}

	@Override
	public void onStopTest(){
		stopGPS();
		satelliteList.clear();
		suceessCount=0;
		if (mCountDownTimer != null)
			mCountDownTimer.cancel();

	}

	@Override
	public View getTestView(LayoutInflater inflater){
		View v = inflater.inflate(R.layout.gps_test,null);
		bindView(v);
		startGPS();
		return v;
	}

	String TAG = "gps_test";
	private Context mContext;
	TextView mTextView;
	Button startButton;
	Button stopButton;
	EditText mEditText;
	ListView mListView = null;
	private Location location;
	LayoutInflater mInflater = null;
	LocationManager mLocationManager = null;
	final int OUT_TIME = 150 * 1000;
	final int MIN_SAT_NUM = 3;
	final int MIN_VALID_SAT_NUM = 2;

	final float VALID_SNR = 34;

	CountDownTimer mCountDownTimer = new CountDownTimer(OUT_TIME, 3000) {

		@Override
		public void onTick(long arg0) {

		}

		@Override
		public void onFinish() {

			fail(getString(R.string.time_out));
		}
	};

	void startGPS() {
		if (!mLocationManager
				.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
			toast(getString(R.string.gps_enable_first));
			postFail();
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
			fail("Fail to get GPS Provider!");
		}
		loge("here");
		mLocationManager.requestLocationUpdates(provider, 500, 0,
				mLocationListener);
		mLocationManager.addGpsStatusListener(gpsStatusListener);
		//mLocationManager.registerGnssStatusCallback(gpsStatusCallback);

		location = mLocationManager.getLastKnownLocation(provider);
		setLocationView(location);
	}

	private void setLocationView(Location location) {

		if (location != null) {
			double latitude = location.getLatitude();
			double longitude = location.getLongitude();
			double speed = location.getSpeed();
			double altitude = location.getAltitude();
			double bearing = location.getBearing();
			//modify by wh for 0017644 String of cit GPS
			String locationTest = getString(R.string.location_test,latitude,longitude,speed,altitude,bearing);
			mTextView.setText(locationTest);
			//mTextView.setText("纬度:" + latitude + '\n' + "经度:"
			//		+ longitude + '\n' + "速率:" + speed + "m/s" + '\n'
			//		+ "海拔:" + altitude + "m" + '\n' + "方向:"
			//		+ bearing + '\n');
			//modify by wh for 0017644 String of cit GPS
		} else {
			mTextView.setText("Location unknown or locating...");
		}
	}

	LocationListener mLocationListener = new LocationListener() {

		public void onLocationChanged(Location location) {

			setLocationView(location);
			//pass();
		}

		public void onProviderDisabled(String provider) {

			setLocationView(null);
		}

		public void onProviderEnabled(String provider) {

			toast("Provider enabled");

		}

		public void onStatusChanged(String provider, int status, Bundle extras) {

		}
	};

	private GpsStatus mGpsStatus;
	private Iterable<GpsSatellite> mSatellites;
	List<String> satelliteList = new ArrayList<String>();
	private int suceessCount = 0;

	GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {

		public void onGpsStatusChanged(int arg0) {

			switch (arg0) {
			case GpsStatus.GPS_EVENT_STARTED:
				toast(getString(R.string.start_location));
				mCountDownTimer.start();
				break;
			case GpsStatus.GPS_EVENT_STOPPED:
				// toast("GPS Stop");
				break;
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				if(suceessCount<1 && ProjectControlUtil.isC802){
					creatDialogToChangeAntenna();
				}else{
					//toast("定位成功");
					pass();	
				}
				break;
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				logd("GPS_EVENT_SATELLITE_STATUS");
				mGpsStatus = mLocationManager.getGpsStatus(null);
				mSatellites = mGpsStatus.getSatellites();
				Iterator<GpsSatellite> it = mSatellites.iterator();
				
				//add by xxf ;sort list 
				ArrayList<GpsSatellite> mGpsSatelliteList = new ArrayList<GpsSatellite>();
				while (it.hasNext()) {
					GpsSatellite gpsS = (GpsSatellite) it.next();
					mGpsSatelliteList.add(gpsS);
				}
			    Comparator<GpsSatellite> comparator = new Comparator<GpsSatellite>(){
			        public int compare(GpsSatellite g1, GpsSatellite g2) {
			        	return (int)(g2.getSnr() -g1.getSnr());
			        }
			       };
			     Collections.sort(mGpsSatelliteList,comparator);
			     
				int count = 0;
				int validCount = 0;
				satelliteList.clear();
								
				for(int i=0;i<mGpsSatelliteList.size();i++){
					GpsSatellite gpsS = mGpsSatelliteList.get(i);
					float snr = gpsS.getSnr();
					if(snr > 0){
						String satellite_List = getString(R.string.satellite_List,count++,gpsS.getPrn(),snr);
						satelliteList.add(satellite_List);
					//	satelliteList.add(count++, "Prn" + gpsS.getPrn() + " 信噪比:" + snr);
					}
					if(snr > VALID_SNR){
						validCount++;
					}
				}
				//add by xxf ;sort list 
				updateAdapter();
				if((count > MIN_SAT_NUM) && (validCount > MIN_VALID_SAT_NUM)){
					if(suceessCount<1 &&  ProjectControlUtil.isC802){
						creatDialogToChangeAntenna();
					}else{
						pass();	
					}
				}
				break;
			default:
				break;
			}

		}

	};

	GnssStatus.Callback gpsStatusCallback = new GnssStatus.Callback(){
		@Override
		public void onFirstFix(int ttffMillis){
			Log.d(TAG,"onFirstFix,ttffMillis:" + ttffMillis);
		}
		@Override
		public void onSatelliteStatusChanged(GnssStatus status){
		}
		@Override
		public void onStarted(){
			Log.d(TAG,"onStarted");
		}
		@Override
		public void onStopped(){
			Log.d(TAG,"onStopped");
		}
	};

	public void updateAdapter() {
		mAdapter.notifyDataSetChanged();
	}
	
	
	//add by xxf
	private AlertDialog dialog;

	private void creatDialogToChangeAntenna() {
		if(dialog!=null && dialog.isShowing()){
			return;
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		if(getKey().equals("gps_test")){
			builder.setTitle(getString(R.string.gps_dialog_title));
			builder.setMessage(
						getString(R.string.gps_dialog_message));
		}else if(getKey().equals("nmea_test")){
			builder.setTitle(getString(R.string.nmea_dialog_title));
			builder.setMessage(
						getString(R.string.nmea_dialog_message));
		}else{}
		
				
		builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						if (dialog != null){
							suceessCount++;
							stopGPS();
							startGPS();
							dialog.dismiss();
						}
							
					}
				});
		dialog = builder.create();
		dialog.show();
		dialog.setCanceledOnTouchOutside(false);
	}
	//add by xxf
	void stopGPS() {
		try {
			satelliteList.clear();
			updateAdapter();
			mLocationManager.removeUpdates(mLocationListener);
			mLocationManager.removeGpsStatusListener(gpsStatusListener);
		} catch (Exception e) {
			loge(e);
		}
	}

	void bindView(View v) {
		mTextView = (TextView) v.findViewById(R.id.gps_hint);
		mListView = (ListView) v.findViewById(R.id.gps_list);
		mListView.setAdapter(mAdapter);
		registerForContextMenu(mListView);
	}

	BaseAdapter mAdapter = new BaseAdapter() {

		public Object getItem(int arg0) {

			return null;
		}

		public long getItemId(int arg0) {

			return 0;
		}

		public View getView(int index, View convertView, ViewGroup parent) {

			if (convertView == null)
				convertView = mInflater.inflate(R.layout.gps_item, null);
			TextView mText = (TextView) convertView.findViewById(R.id.gps_text);
			ImageView mImage = (ImageView) convertView
					.findViewById(R.id.gps_image);
			mText.setText(satelliteList.get(index));
			mImage.setImageResource(drawable.presence_online);
			return convertView;
		}

		public int getCount() {

			if (satelliteList != null)
				return satelliteList.size();
			else
				return 0;
		}

	};

	void fail(Object msg) {

		loge(msg);
		toast(msg);
		postFail();
	}

	void pass() {
		postSuccess();
	}

	public void toast(Object s) {

		if (s == null)
			return;
		Toast.makeText(mContext, s + "", Toast.LENGTH_SHORT).show();
	}

	private void loge(Object e) {

		if (e == null)
			return;
		Thread mThread = Thread.currentThread();
		StackTraceElement[] mStackTrace = mThread.getStackTrace();
		String mMethodName = mStackTrace[3].getMethodName();
		e = "[" + mMethodName + "] " + e;
		Log.e(TAG, e + "");
	}

	private void logd(Object s) {

		Thread mThread = Thread.currentThread();
		StackTraceElement[] mStackTrace = mThread.getStackTrace();
		String mMethodName = mStackTrace[3].getMethodName();

		s = "[" + mMethodName + "] " + s;
		Log.d(TAG, s + "");
	}

}
