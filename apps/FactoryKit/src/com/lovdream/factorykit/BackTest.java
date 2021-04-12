package com.lovdream.factorykit;

import java.util.ArrayList;

import com.lovdream.factorykit.Config.TestItem;
import com.lovdream.factorykit.PCBATest.MyAdapter;
import com.lovdream.factorykit.libs.GridFragment;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class BackTest extends GridFragment implements TestItemBase.TestCallback, 
																									View.OnClickListener {
	    protected static final String TAG = "BackTest";
	    protected TestItemFactory mFactory;
	    protected ArrayList<TestItem> backItems;
	    private View mFooter;
	    protected Config mConfig;// = Config.getInstance(getActivity());
	  @Override
	    public void onActivityCreated(Bundle savedInstanceState) {
		  super.onActivityCreated(savedInstanceState);
		  FactoryKitApplication app = (FactoryKitApplication) getActivity()
	                .getApplication();
	        Config config = app.getTestConfig();
	        backItems = new ArrayList<TestItem>();
	        ArrayList<TestItem> allItems = config.getTestItems();
	        for (TestItem item : allItems) {
	            if (item.inBackTest) {
	                backItems.add(item);
	            }
	        }
	        
	        mConfig = Config.getInstance(getActivity());
	        setGridAdapter(new MyAdapter(getActivity(), backItems));
	        Log.i(TAG, "back test launched");
	        mFactory = TestItemFactory.getInstance(getActivity());
	  }
	@Override
	public void onClick(View v) {
		
	}
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.pcba_test2);
        setHasOptionsMenu(true);
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
        getActivity().setTitle(R.string.app_name);
        ((MyAdapter) getGridAdapter()).notifyDataSetChanged();
    }
	
	 @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	            Bundle savedInstanceState) {
	        View v = super.onCreateView(inflater, container, savedInstanceState);
	        v.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
	        return v;
	    }
	
	@Override
	public void onTestFinish(TestItemBase item) {
	    item.setBackTest(false);
        getActivity().setTitle(R.string.pcba_test2);
        setHasOptionsMenu(true);
        ((BaseAdapter) getGridAdapter()).notifyDataSetChanged();
	}
	

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(R.string.clear_test_result);
    }
    
    

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Config.getInstance(getActivity()).clearBackFlag(backItems);
        ((MyAdapter) getGridAdapter()).notifyDataSetChanged();
        return true;
    }
    
    
    public  class MyAdapter extends BaseAdapter {

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

            int flag = mConfig.getBackFlag(item.fm.backFlag);

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
	

	
    @Override
    public void onGridItemClick(GridView l, View v, int position, long id) {
    	  TestItem item = (TestItem) v.getTag();
          if (item == null) {
              return;
          }
          
          TestItemBase fragment = mFactory.createTestItem(getActivity(), item);
          
          if (fragment == null) {
              Toast.makeText(getActivity(), R.string.no_item, Toast.LENGTH_SHORT)
                      .show();
              return;
          }
          if (fragment.isAdded()) {
              return;
          }
          setHasOptionsMenu(false);
          
          fragment.setTestCallback(this);
          fragment.setAutoTest(false);
          fragment.setBackTest(true);
          FragmentManager fm = getFragmentManager();
          FragmentTransaction ft = fm.beginTransaction();
          ft.add(android.R.id.content, fragment, item.key);
          ft.addToBackStack(item.key);
          ft.commit();
          getActivity().setTitle(item.displayName);
    }

}
