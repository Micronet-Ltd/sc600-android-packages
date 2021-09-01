package com.lovdream.factorykit.items;

import android.view.View;
import android.view.LayoutInflater;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.util.Log;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.TestItemBase;

public class MasterClear extends TestItemBase{

	private static final int DEFAULT_MIN_BATTERY = 30;
	@Override
	public String getKey(){
		return "master_clear";
	}

	@Override
	public String getTestMessage(){
		return "";
	}

	@Override
	public void onStartTest(){

		int minBattery = DEFAULT_MIN_BATTERY;

		String arg[] = getParameter("minBattery");
		if(arg != null){
			try{
				minBattery = Integer.valueOf(arg[0]);
			}catch(Exception e){
				e.printStackTrace();
			}
		}

		if(minBattery <= 0){
			minBattery = DEFAULT_MIN_BATTERY;
		}

		final Context context = getActivity();

		Intent intent = context.registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		int level = intent.getIntExtra("level", 0);
		int scale = intent.getIntExtra("scale", 100);
		int batteryLevel = level * 100 / scale;

		if(batteryLevel < minBattery){
			Toast.makeText(context,context.getString(R.string.master_clear_low_battery,minBattery),Toast.LENGTH_SHORT).show();
			onFailClick();
			return;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(R.string.master_clear_mesg);
		builder.setCancelable(false);
		builder.setNegativeButton(android.R.string.cancel,new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				onFailClick();
			}
		});
		builder.setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				Intent intent = new Intent(Intent.ACTION_MASTER_CLEAR);
				intent.setPackage("android");
				intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
				intent.putExtra(Intent.EXTRA_REASON, "MasterClearConfirm");
				context.sendBroadcast(intent);
			}
		});
		builder.show();
	}

	@Override
	public void onStopTest(){
	}

	@Override
	public View getTestView(LayoutInflater inflater){
		hideButtons();
		return null;
	}
}

