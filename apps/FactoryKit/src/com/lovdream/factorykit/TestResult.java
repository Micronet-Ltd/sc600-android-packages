
package com.lovdream.factorykit;

import android.view.View;
import android.app.Activity;
import android.view.LayoutInflater;

import com.lovdream.factorykit.items.TestFlag;

public class TestResult extends TestFlag{

	@Override
	public View getTestView(LayoutInflater inflater){
		hideButtons();
		return super.getTestView(inflater);
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		activity.setTitle(R.string.test_result);
	}

	@Override
	public void onDetach(){
		super.onDetach();
		getActivity().setTitle(R.string.app_name);
	}
}
