
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.BaseAdapter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;

import java.util.ArrayList;

import com.lovdream.factorykit.Config.TestItem;

public class SingleTest extends ListFragment implements TestItemBase.TestCallback{

	private static final String TAG = Main.TAG;

	private TestItemFactory mFactory;
	protected Config mConfig;

	protected boolean itemFilter(TestItem item){
		return true;
	}

	protected int getTitleResId(){
		return R.string.single_test;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		FactoryKitApplication app = (FactoryKitApplication)getActivity().getApplication();
		mConfig = app.getTestConfig();

		ArrayList<TestItem> visibleItems = new ArrayList<TestItem>();
		ArrayList<TestItem> allItems = mConfig.getTestItems();
		for(TestItem item : allItems){
			if(itemFilter(item)){
				visibleItems.add(item);
			}
		}
      String pinText=getResources().getString(R.string.test_fourteenpin_text);
		//TestItem pin14 = new TestItem("sub_pin_test",pinText);
		//visibleItems.add(pin14);

		setListAdapter(new MyAdapter(getActivity(),visibleItems));
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
	public void onListItemClick(ListView l, View v, int position, long id){
		super.onListItemClick(l,v,position,id);
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
		beforeTest(fragment);
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.add(android.R.id.content,fragment,item.key);
		ft.addToBackStack(item.key);
		ft.commit();
		getActivity().setTitle(item.displayName);
	}

	public void beforeTest(TestItemBase item){
		//do nothing,for subclass
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		activity.setTitle(getTitleResId());
		setHasOptionsMenu(true);
	}

	@Override
	public void onDetach(){
		super.onDetach();
		getActivity().setTitle(R.string.app_name);
		((MyAdapter)getListAdapter()).notifyDataSetChanged();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu,MenuInflater inflater){
		menu.add(R.string.clear_test_result);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		clearFlag();
		((MyAdapter)getListAdapter()).notifyDataSetChanged();
		return true;
	}

	public void clearFlag(){
		mConfig.clearTestFlag();
	}

	@Override
	public void onTestFinish(TestItemBase item){
		getActivity().setTitle(getTitleResId());
		setHasOptionsMenu(true);
		((BaseAdapter)getListAdapter()).notifyDataSetChanged();
	}

	protected int getFlag(FlagModel fm){
		return mConfig.getTestFlag(fm.testFlag);
	}

	public class MyAdapter extends BaseAdapter{

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
				convertView = mInflater.inflate(R.layout.test_list_item,null);
			}

			TestItem item = mItems.get(positon);
			((TextView)convertView.findViewById(R.id.item_text)).setText(item.displayName);

			int flag = getFlag(item.fm);

			if(flag == Config.TEST_FLAG_PASS){
				((ImageView)convertView.findViewById(R.id.item_icon)).setImageResource(R.drawable.test_pass);
			}else if(flag == Config.TEST_FLAG_FAIL){
				((ImageView)convertView.findViewById(R.id.item_icon)).setImageResource(R.drawable.test_fail);
			}else{
				((ImageView)convertView.findViewById(R.id.item_icon)).setImageResource(0);
			}
			convertView.setTag(item);

			return convertView;
		}
	}
}
