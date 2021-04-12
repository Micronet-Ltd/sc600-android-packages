
package com.lovdream.factorykit;

import android.os.Bundle;
import android.app.Fragment;
import android.app.Activity;
import android.app.ListFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
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
import android.os.PowerManager;
import android.util.Log;

import java.util.ArrayList;

import com.lovdream.factorykit.Config.TestItem;
import com.lovdream.factorykit.libs.GridFragment;

public class UsbTest extends GridFragment implements TestItemBase.TestCallback,View.OnClickListener{

	private static final String TAG = Main.TAG;

	private TestItemFactory mFactory;
	private ArrayList<TestItem> UsbItems;

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		FactoryKitApplication app = (FactoryKitApplication)getActivity().getApplication();
		Config config = app.getTestConfig();
		
		UsbItems = new ArrayList<TestItem>();
		ArrayList<TestItem> allItems = config.getTestItems();
		for(TestItem item : allItems){
                        Log.w("fy","item.key="+item.key+"    item.inUSBTest="+item.inUSBTest+"      item.flagIndex="+item.flagIndex);
			if(item.inUSBTest){
				UsbItems.add(item);
			}
                             if(item.key.equals("headset_test_nuno")){
                                 UsbItems.add(item);
                             }
		}


		setGridAdapter(new MyAdapter(getActivity(),UsbItems));
		Log.i(TAG,"single test launched");
		
		mFactory = TestItemFactory.getInstance(getActivity());
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View v = super.onCreateView(inflater,container,savedInstanceState);
		v.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		return v;
	}

	@Override
	public void onClick(View v){
		PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
		pm.shutdown(false,null,false);
	}

	@Override
	public void onGridItemClick(GridView l, View v, int position, long id){
		super.onGridItemClick(l,v,position,id);
		TestItem item = (TestItem)v.getTag();
		if(item == null){
			return;
		}

		TestItemBase fragment = mFactory.createTestItem(getActivity(),item);

		if(fragment == null){
			Toast.makeText(getActivity(),R.string.no_item,Toast.LENGTH_SHORT).show();
			return;
		}

		if(fragment.isAdded()){
			return;
		}

		setHasOptionsMenu(false);
		fragment.setTestCallback(this);
		fragment.setAutoTest(false);
		fragment.setUSBTest(true);
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.add(android.R.id.content,fragment,item.key);
		ft.addToBackStack(item.key);
		ft.commit();
		getActivity().setTitle(item.displayName);
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		activity.setTitle(R.string.pcba_test);
		setHasOptionsMenu(true);
	}

	@Override
	public void onDetach(){
		super.onDetach();
		getActivity().setTitle(R.string.app_name);
		((MyAdapter)getGridAdapter()).notifyDataSetChanged();
	}

	private void saveUSBtIfNeed(){
		if((UsbItems == null) || (UsbItems.size() == 0)){
			return;
		}
		Config config = Config.getInstance(getActivity());

		for(TestItem item : UsbItems){
			if(config.getUSBFlag(item.fm.usbIndex) != Config.TEST_FLAG_PASS){
				config.setUSBFt(false);
				//mFooter.setVisibility(View.GONE);
				return;
			}
		}

		config.setUSBFt(true);
		//mFooter.setVisibility(View.VISIBLE);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu,MenuInflater inflater){
		menu.add(R.string.clear_test_result);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		Config.getInstance(getActivity()).clearUSBFlag();
		((MyAdapter)getGridAdapter()).notifyDataSetChanged();
		return true;
	}

	@Override
	public void onTestFinish(TestItemBase item){
		item.setUSBTest(false);
		getActivity().setTitle(R.string.test_usb);
		setHasOptionsMenu(true);
		((BaseAdapter)getGridAdapter()).notifyDataSetChanged();
		saveUSBtIfNeed();
	}

	public static class MyAdapter extends BaseAdapter{

		ArrayList<TestItem> mItems;
		LayoutInflater mInflater;
		Config mConfig;

		public MyAdapter(Context context,ArrayList<TestItem> items){
			super();
			mItems = items;
			mInflater = LayoutInflater.from(context);
			mConfig = Config.getInstance(context);
		}

		@Override
		public int getCount(){
			return mItems.size();
		}

		@Override
		public Object getItem(int position){
			return mItems.get(position);
		}

		@Override
		public long getItemId(int position){
			return position;
		}

		@Override
		public View getView(int positon,View convertView,ViewGroup parent){

			if(convertView == null){
				convertView = mInflater.inflate(R.layout.test_grid_item,null);
			}

			TestItem item = mItems.get(positon);
			TextView tv = (TextView)convertView.findViewById(R.id.item_text);
			tv.setText(item.displayName);

			int flag;
			if("4gft".equals(item.key)){
				flag = mConfig.get4GftStatus();
			}else{
				flag = mConfig.getUSBFlag(item.fm.usbIndex);
			}
                        
			if(flag == Config.TEST_FLAG_PASS){
				tv.setTextColor(Color.rgb(0,100,0));
			}else if(flag == Config.TEST_FLAG_FAIL){
				tv.setTextColor(Color.RED);
			}else{
				tv.setTextColor(Color.BLACK);
			}

			convertView.setTag(item);

			return convertView;
		}
	}
}
