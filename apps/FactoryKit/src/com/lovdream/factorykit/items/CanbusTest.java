
package com.lovdream.factorykit.items;

import android.view.View;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.os.SystemProperties;
import android.util.Log;
import android.telephony.TelephonyManager;
import com.swfp.utils.SimUtil;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;
import android.os.SystemClock;

import com.lovdream.factorykit.R;
import com.lovdream.factorykit.TestItemBase;
import com.lovdream.factorykit.Utils;

public class CanbusTest extends TestItemBase{

	private TextView mTv;
	private boolean isDataTransferred = false;
	private boolean mIsInTest = false;
	TextView tv;
	private WifiManager mWifiManager;
	private int counter = 0;
	private final byte[] expected = new byte[]{0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48};
	private Class<?> canServiceClass;
	private Method bitrate;
	private Method mode;
	private Method link;
	private Method open;
	private Method bind;
	private Method close;
	private Method config;
	private Method send;
	private Method recvmsg;
	private Object j;
	private int socket1;
	//private int socket2;
	private int idx1;
	//private int idx2;
	private boolean receive;
	private int totalDropped;
	private boolean abort = false;


	@Override
	public String getKey(){
		return "canbus_test";
	}

	@Override
	public String getTestMessage(){
		return getActivity().getString(R.string.canbus_test_mesg);
	}

	@Override
	public void onStartTest(){
	
	try {
            canServiceClass = Class.forName("com.android.server.net.CanbusService");
            bitrate = canServiceClass.getDeclaredMethod("bitrate", int.class);
            mode = canServiceClass.getDeclaredMethod("mode", String.class);
            link = canServiceClass.getDeclaredMethod("link", String.class);
            open = canServiceClass.getDeclaredMethod("open", String.class);
            bind = canServiceClass.getDeclaredMethod("bind", String.class, int.class);
            close = canServiceClass.getDeclaredMethod("close", int.class);
            config = canServiceClass.getDeclaredMethod("config", String.class, IntBuffer.class, IntBuffer.class, int.class, int.class, int.class, int.class, int.class);
            send = canServiceClass.getDeclaredMethod("send", int.class,int.class,int.class,byte[].class);
            recvmsg=canServiceClass.getDeclaredMethod("recvmsg", int.class, int.class, IntBuffer.class,
                    IntBuffer.class, LongBuffer.class, IntBuffer.class, ByteBuffer.class);
            j=canServiceClass.getConstructor().newInstance();
        }catch (Exception e){
            e.printStackTrace();
            postFail();
            return;
        }
        
        Thread t = new Thread(mRunnable, "t1");
		t.start();
	}

	@Override
	public void onStopTest(){
		try{
			abort=true;
			close.invoke(j, socket1);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	@Override
	public View getTestView(LayoutInflater inflater){
		View v = inflater.inflate(R.layout.test_mesg_view,null);
		tv = (TextView)v.findViewById(R.id.test_mesg_view);
		tv.setText("Waiting for Can packet 0xFE40 ... ");
		return v;
	}
	
	private Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
		try {
                link.invoke(j, "down");
                SystemClock.sleep(200);

                bitrate.invoke(j, 500000);
                SystemClock.sleep(200);

                mode.invoke(j, "normal");
                SystemClock.sleep(200);

                link.invoke(j, "up");
                SystemClock.sleep(200);

                socket1 = (int) open.invoke(j, "can0");
//                 socket2 = (int) open.invoke(j, "can0");

                IntBuffer ids = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
                IntBuffer flt = ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
                Log.e("config",""+config.invoke(j, "can0", ids, flt, 0, 0, socket1, 0, 0));
                //config.invoke(j, "can0", null, null, 0, 0, socket2, 1, 0);
                idx1 =(int)bind.invoke(j, "can0", socket1);
                //idx2 =(int)bind.invoke(j, "can0", socket2);
            } catch (Exception e){
                e.printStackTrace();
                if (!abort){
		  getActivity().runOnUiThread(new Runnable() {
		  @Override
		  public void run() {
		  postFail();
		  }});
		}
                return;
            }
            SystemClock.sleep(1000);
            Log.e("Ready","Ready!");
            try{
                IntBuffer id =ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
                IntBuffer dlc =ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
                IntBuffer dropped =ByteBuffer.allocateDirect(4).order(ByteOrder.nativeOrder()).asIntBuffer();
                LongBuffer ts=ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder()).asLongBuffer();
                ByteBuffer pl=ByteBuffer.allocateDirect(8).order(ByteOrder.nativeOrder());

                id.position(0);
                dlc.position(0);
                dropped.position(0);
                ts.position(0);
                pl.position(0);
                receive=true;
                while (receive) {
                    recvmsg.invoke(j, socket1, idx1, id, dlc, ts, dropped, pl);
                    int intId = id.get();
                    int dlcInt = dlc.get();
                    byte[] data = new byte[dlcInt];

                    for (int i = 0; i < dlcInt; i++) {
                        data[i]=pl.get();
                    }
                    
                    if (((intId>>8)&0xFFFF) == 0xFE40){ //Arrays.equals(data,expected)){
		      receive=false;
		      getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
			postSuccess();
		      }});
                    } 

                    id.position(0);
                    dlc.position(0);
                    dropped.position(0);
                    ts.position(0);
                    pl.position(0);
                }
            }catch (Exception e){
                e.printStackTrace();
                if (!abort){
		  getActivity().runOnUiThread(new Runnable() {
		  @Override
		  public void run() {
		  postFail();
		  }});
		}
            }
	}
	};
}
