
package com.lovdream.factorykit;

import android.os.SystemProperties;

import java.util.Date;
import java.io.FileWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import com.swfp.utils.ProjectControlUtil;

public class CrashHandler implements UncaughtExceptionHandler{

	public static final String TRACE_FILE = "/mnt/sdcard/cit.log";
	public static final String CRASH_PROP = "sys.factory.crash";

	private static CrashHandler mInstance;

	public static CrashHandler getInstance(){
		if(mInstance == null){
			mInstance = new CrashHandler();
		}
		return mInstance;
	}

	private static void saveStackTrace(Throwable e){
		try {
			FileWriter fw = new FileWriter(TRACE_FILE, true);
			fw.write(new Date() + "\n");
			StackTraceElement[] stackTrace = e.getStackTrace();
			fw.write(e.getMessage() + "\n");
			for(int i = 0; i < stackTrace.length; i++){
				fw.write("file:" + stackTrace[i].getFileName() + " class:" 
						+ stackTrace[i].getClassName() + " method:"
						+ stackTrace[i].getMethodName() + " line:"
						+ stackTrace[i].getLineNumber() + "\n");
			}
			fw.write("\n");
			fw.close();
		}catch (Exception e2){
			e2.printStackTrace();
		}
	}

	@Override
	public void uncaughtException(Thread t, Throwable e){
		saveStackTrace(e);
		SystemProperties.set(CRASH_PROP,String.valueOf(true));
		ProjectControlUtil.comeCit(false);
		e.printStackTrace();
		android.os.Process.killProcess(android.os.Process.myPid());
	}
}
