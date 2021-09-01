package com.swfp.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

import com.lovdream.factorykit.Config;
import com.lovdream.factorykit.DisplayNameUtil;
import com.lovdream.factorykit.FlagModel;
import com.lovdream.factorykit.NvUtil;
import com.lovdream.factorykit.Config.TestItem;

/**
 * add by xxf
 * 
 * add this class for save result for user can get;
 * 
 * **/
public class TestDataUtil {
	
	public static final String PASS = "PASS";
	public static final String FAILURE = "FAIL";
	public static final String NOTEST = "?";
	
	public static final String PCBA = "PCBA";
	public static final String SINGLE = "SINGLE";
	public static final String SMALLP = "V_BOARD";
	public static final String BACK = "BACK";
	private static final String TAG = "TestDataUtil";
	
	private static final String SPLITE = "       ";
	
	private final String MODE = "mode";
	private final String ITEM = "item";
	private final String RESULT = "result";
	private final String TIME = "time";
	private final String SPLIT_EXPLAIN = " : ";
	
	String boardId = null;
	
	
	private static final String PATH_ROOT = "sdcard/cit/";
	private static  final String PATH_TXT = "citResult.txt";
	private static  final String PATH_CSV="citResult.csv" ;
	
	
	private Map<String,SaveDataModel>data;
	
	private TestDataUtil(){
		data = new HashMap<String, SaveDataModel>();
	}
	
	private static TestDataUtil mTestDataUtil;
	
	public static TestDataUtil getTestDataUtil(){
		if(mTestDataUtil==null)
			mTestDataUtil =new TestDataUtil();
		return mTestDataUtil;
	}
	
	 private String getSnVersion() {
	        String strSN ="";
	        try {
	        	strSN = com.lovdream.util.SystemUtil.getSN();
	        	int length = strSN.length();
		        strSN = strSN.substring(0,15);
			} catch (Exception e) {
				// TODO: handle exception
			}
	        return strSN.equals("")?"no sn": strSN;
	    }
	    
	
	public void saveDataResult(SaveDataModel mSaveDataModel,boolean isWrite){
		try {
			if(mSaveDataModel!=null) data.put((mSaveDataModel.testModle+","+mSaveDataModel.testItem).replaceAll(" ", ""), mSaveDataModel);
			if(isWrite)backMapToFileDL();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void backDataForUser(Context context,FlagModel fm,boolean result,boolean isWrite){
		try {
			SaveDataModel mSaveDataModel=null;
			if(fm.getTestModel()!=null){
				mSaveDataModel = new SaveDataModel();
				mSaveDataModel.testItem = getEhglishName(fm.displayName,context);
				mSaveDataModel.testResult = result?PASS:FAILURE;
				mSaveDataModel.testTime = getSaveTime();
				mSaveDataModel.testModle = fm.getTestModel();
			}
			if(mSaveDataModel!=null){
				saveDataResult(mSaveDataModel,isWrite);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public String getSaveTime(){
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
		Date date = new Date(System.currentTimeMillis());
		return simpleDateFormat.format(date);
	}
	
	private String getTxtlineData(SaveDataModel mSaveDataModel){
		if(mSaveDataModel!=null){
			
			int columnWide = 24;
			String column1 = MODE+SPLIT_EXPLAIN+mSaveDataModel.testModle+SPLITE;
			column1 = appent(column1,columnWide);
			String column2 = ITEM+SPLIT_EXPLAIN+mSaveDataModel.testItem+SPLITE;
			column2 = appent(column2,2*columnWide);
			String column3 =  RESULT+SPLIT_EXPLAIN+"["+mSaveDataModel.testResult+"]"+SPLITE;
			column3 = appent(column3,columnWide);
			String column4 = TIME+SPLIT_EXPLAIN+mSaveDataModel.testTime;
			
			return column1+column2+column3+column4;
		}
		return null;
	}
	
	public  String appent(String str , int length){
        if(str == null){
            str = "";
        }
        try {
            int strLen = 0;//计算原字符串所占长度,规定中文占两个,其他占一个
            for(int i = 0 ; i<str.length(); i++){
                if(isChinese(str.charAt(i))){
                    strLen = strLen + 2;
                }else{
                    strLen = strLen + 1;
                }
            }
            if(strLen>=length){
                return str;
            }
            int remain = length - strLen;//计算所需补充空格长度
            for(int i =0 ; i< remain ;i++){
                str = str + " ";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }
	
	  private static boolean isChinese(char c) {
	        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
	        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
	                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
	                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
	                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
	                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
	                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
	                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
	            return true;
	        }
	        return false;
	    }
	

	//写入到文件中;
	public void backMapToFileDL(){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					write();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public void writeToFile(String path, String flag) throws IOException {
		//try {
			boolean res = true;
			File file = new File(path);
			File dir = new File(file.getParent());
			if (!dir.exists())
				dir.mkdirs();

			//try {
				FileWriter mFileWriter = new FileWriter(file, false);
				mFileWriter.write(flag);
				mFileWriter.close();
			//} catch (IOException e) {
				//e.printStackTrace();
				//android.util.Log.d(TAG, "e------>" + (e));
				//res = false;
			//}

		//} catch (Exception e) {
			//e.printStackTrace();
		//}
	}

	// 这个方法写,相当于append;
	private void writeTxtToFile(String strcontent, String filePath,
			String fileName) {
		// 生成文件夹之后，再生成文件，不然会出错
		makeFilePath(filePath, fileName);

		String strFilePath = filePath + fileName;
		// 每次写入时，都换行写
		String strContent = strcontent + "\r\n";
		try {
			File file = new File(strFilePath);
			if (!file.exists()) {
				Log.d("TestFile", "Create the file:" + strFilePath);
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			RandomAccessFile raf = new RandomAccessFile(file, "rwd");
			raf.seek(file.length());
			raf.write(strContent.getBytes());
			raf.close();
		} catch (Exception e) {
			Log.e("TestFile", "Error on write File:" + e);
		}
	}
	  private String getBoardId() {
		  if(boardId!=null) return boardId;
	        return SystemProperties.get("persist.sys.broad.config", "UNKNOWN");
	    }
	private void writeCsvToFile(String filePath,
			String fileName) {
				makeFilePath(filePath, fileName);
				String strFilePath = filePath + fileName;
				try {
					File file = new File(strFilePath);
					if (!file.exists()) {
						Log.d("TestFile", "Create the file:" + strFilePath);
						file.getParentFile().mkdirs();
						file.createNewFile();
					}
		            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
		            //我们在第1行将sn号和boardid 写进去;
		            bw.write("SN : ");bw.write(",");
		            bw.write(getSnVersion());bw.write(",");
		            bw.write("BoardId : ");bw.write(",");
		            bw.write(getBoardId());bw.write(",");
		            bw.newLine();
		            //我们在第2行写标题:
		            bw.write(MODE);bw.write(",");
		            bw.write(ITEM);bw.write(",");
		            bw.write(RESULT);bw.write(",");
		            bw.write(TIME);bw.write(",");
		            bw.newLine();
		            
		            
		            //第3行开始循环写数据;我们需要将集合中的数据写入到表格中
		            for(Entry<String, SaveDataModel> entry:data.entrySet()){
		  		      if(entry.getKey()!=null && entry.getKey().contains(PCBA)){
		  		    	bw.write(entry.getValue().testModle);bw.write(",");
		  		    	bw.write(entry.getValue().testItem);bw.write(",");
		  		    	bw.write(entry.getValue().testResult);bw.write(",");
		  		    	bw.write(entry.getValue().testTime);bw.write(",");
		  		    	bw.newLine();
		  		      }
		  		    }
		  		    
		  		    for(Entry<String, SaveDataModel> entry:data.entrySet()){
		  			      if(entry.getKey()!=null && entry.getKey().contains(SINGLE)){
		  			    	bw.write(entry.getValue().testModle);bw.write(",");
			  		    	bw.write(entry.getValue().testItem);bw.write(",");
			  		    	bw.write(entry.getValue().testResult);bw.write(",");
			  		    	bw.write(entry.getValue().testTime);bw.write(",");
			  		    	bw.newLine();
		  			      }
		  			    }
		  		    
		  		    
		  		    for(Entry<String, SaveDataModel> entry:data.entrySet()){
		  			      if(entry.getKey()!=null && entry.getKey().contains(SMALLP)){
		  			    	bw.write(entry.getValue().testModle);bw.write(",");
			  		    	bw.write(entry.getValue().testItem);bw.write(",");
			  		    	bw.write(entry.getValue().testResult);bw.write(",");
			  		    	bw.write(entry.getValue().testTime);bw.write(",");
			  		    	bw.newLine();
		  			      }
		  			    }
		  		    
		  		    
		  		    for(Entry<String, SaveDataModel> entry:data.entrySet()){
		  			      if(entry.getKey()!=null && entry.getKey().contains(BACK)){
		  			    	bw.write(entry.getValue().testModle);bw.write(",");
			  		    	bw.write(entry.getValue().testItem);bw.write(",");
			  		    	bw.write(entry.getValue().testResult);bw.write(",");
			  		    	bw.write(entry.getValue().testTime);bw.write(",");
			  		    	bw.newLine();
		  			      }
		  			    }
		            bw.close();
				} catch (Exception e) {
					Log.e("TestFile", "Error on write File:" + e);
				}

	}

	// 生成文件

	private File makeFilePath(String filePath, String fileName) {
		File file = null;
		makeRootDirectory(filePath);
		try {
			file = new File(filePath + fileName);
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return file;
	}

	// 生成文件夹

	private static void makeRootDirectory(String filePath) {
		        File file = null;
		        try {
		            file = new File(filePath);
		            if (!file.exists()) {
		                file.mkdir();
		            }
		        } catch (Exception e) {
		            Log.i("error:", e + "");
		        }
		    }
	
	/**
	 * 逐行读取文件
	 * 
	 * @param strFilePath
	 */
	public List<String> readLine(String strFilePath) {
		List<String> txtList = new ArrayList<String>();
		File file = new File(strFilePath);
		if (file.isDirectory()) {
			Log.d(TAG, "The File doesn't not exist.");
		} else {
			try {
				InputStream instream = new FileInputStream(file);
				if (instream != null) {
					InputStreamReader inputreader = new InputStreamReader(
							instream);
					BufferedReader buffreader = new BufferedReader(inputreader);
					String line;
					// 逐行读取
					while ((line = buffreader.readLine()) != null) {
						txtList.add(line);
					}
					instream.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return txtList;
	}
	public void write() {
		synchronized (data) {
		
			StringBuilder sb = new StringBuilder();

		    for(Entry<String, SaveDataModel> entry:data.entrySet()){
		      if(entry.getKey()!=null && entry.getKey().contains(PCBA)){
		    	  sb.append(getTxtlineData(entry.getValue())+"\n");
		      }
		    }
		    
		    for(Entry<String, SaveDataModel> entry:data.entrySet()){
			      if(entry.getKey()!=null && entry.getKey().contains(SINGLE)){
			    	  sb.append(getTxtlineData(entry.getValue())+"\n");
			      }
			    }
		    
		    
		    for(Entry<String, SaveDataModel> entry:data.entrySet()){
			      if(entry.getKey()!=null && entry.getKey().contains(SMALLP)){
			    	  sb.append(getTxtlineData(entry.getValue())+"\n");
			      }
			    }
		    
		    
		    for(Entry<String, SaveDataModel> entry:data.entrySet()){
			      if(entry.getKey()!=null && entry.getKey().contains(BACK)){
			    	  sb.append(getTxtlineData(entry.getValue())+"\n");
			      }
			    }
		    
		    writeTxtToFile(sb.toString(),PATH_ROOT,PATH_TXT);
		    writeCsvToFile(PATH_ROOT,PATH_CSV);
		}
	}
	
			public boolean isFirstBoot() {
				return !new File(PATH_ROOT + PATH_CSV).exists();
			}
		
			//如果没有记录文件,我们需要从nv中将结果读出来;
			public void readDataFromNv(Context context) {
				byte[] mTestFlag = NvUtil.getNvFactoryData3IByte();
				  ArrayList<TestItem> allItems = Config.getInstance(context).getTestItems();
				  readSmallFromNv(allItems,context,false);
				  readSingleFromNv(allItems,context,false);
				  readBacklFromNv(allItems,context,false);
				  readPcbaFromNv(allItems,context,true);
			}
		
	public void readSmallFromNv(ArrayList<TestItem> allItems,Context context,boolean isWrite) {
		for (int i = 0; i < allItems.size(); i++) {
			if (allItems.get(i) != null) {
				FlagModel fm = allItems.get(i).fm;
					  if(allItems.get(i).inSmallPCB){
						  SaveDataModel mSaveDataModel = new SaveDataModel();
						  mSaveDataModel.testModle = SMALLP;
						  mSaveDataModel.testItem = getEhglishName(fm.displayName,context);
						  //没办法,如果是从nv中获取数据,我们不可能知道时间,因为已经格式化掉了;
						  mSaveDataModel.testTime = "?";
						  mSaveDataModel.testResult = fm.getResult( Config.getInstance(context), Config.DATA_SMALL);
						  saveDataResult(mSaveDataModel, false);
					  }
			}
		}
		if(isWrite) saveDataResult(null, true);
	}
	
	public void readBacklFromNv(ArrayList<TestItem> allItems,Context context,boolean isWrite) {
		for (int i = 0; i < allItems.size(); i++) {
			if (allItems.get(i) != null) {
				FlagModel fm = allItems.get(i).fm;
					  if(allItems.get(i).inBackTest){
						  SaveDataModel mSaveDataModel = new SaveDataModel();
						  mSaveDataModel.testModle = BACK;
						  mSaveDataModel.testItem = getEhglishName(fm.displayName,context);
						  //没办法,如果是从nv中获取数据,我们不可能知道时间,因为已经格式化掉了;
						  mSaveDataModel.testTime = "?";
						  mSaveDataModel.testResult = fm.getResult( Config.getInstance(context), Config.DATA_BACK);
						  saveDataResult(mSaveDataModel, false);
					  }
			}
		}
		if(isWrite) saveDataResult(null, true);
	}
	
	
	public void readSingleFromNv(ArrayList<TestItem> allItems,Context context,boolean isWrite) {
		for (int i = 0; i < allItems.size(); i++) {
			if (allItems.get(i) != null) {
						FlagModel fm = allItems.get(i).fm;
						  SaveDataModel mSaveDataModel = new SaveDataModel();
						  mSaveDataModel.testModle = SINGLE;
						  mSaveDataModel.testItem = getEhglishName(fm.displayName,context);
						  //没办法,如果是从nv中获取数据,我们不可能知道时间,因为已经格式化掉了;
						  mSaveDataModel.testTime = "?";
						  mSaveDataModel.testResult = fm.getResult(Config.getInstance(context), Config.DATA_SIGNLE);
						  saveDataResult(mSaveDataModel, false);
			}
		}
		if(isWrite) saveDataResult(null, true);
	}
	
	public void readPcbaFromNv(ArrayList<TestItem> allItems,Context context,boolean isWrite) {
		for (int i = 0; i < allItems.size(); i++) {
			if (allItems.get(i) != null) {
				FlagModel fm = allItems.get(i).fm;
				if (allItems.get(i).inPCBATest) {
					SaveDataModel mSaveDataModel = new SaveDataModel();
					mSaveDataModel.testModle = PCBA;
					mSaveDataModel.testItem = getEhglishName(fm.displayName,context);
					// 没办法,如果是从nv中获取数据,我们不可能知道时间,因为已经格式化掉了;
					mSaveDataModel.testTime = "?";
					mSaveDataModel.testResult = fm.getResult(
							Config.getInstance(context), Config.DATA_PCBA);
					saveDataResult(mSaveDataModel, false);
				}
			}
		}
		if(isWrite) saveDataResult(null, true);
	}
			
			//如果文件存在,我们从文件中读取数据;
			public void readDataFromFile() {
				List<String> lineData = readLine(PATH_ROOT+PATH_TXT);
				for (int i = 0; i < lineData.size(); i++) {
					try {
						String line = lineData.get(i);
						SaveDataModel mSaveDataModel = new SaveDataModel();
						mSaveDataModel.testModle = ((String) line.subSequence(line.indexOf(MODE+SPLIT_EXPLAIN)+7,line.indexOf(MODE+SPLIT_EXPLAIN)+17));
						mSaveDataModel.testItem =((String) line.subSequence(line.indexOf(ITEM+SPLIT_EXPLAIN)+7,line.indexOf(ITEM+SPLIT_EXPLAIN)+30));
						mSaveDataModel.testResult =((String) line.subSequence(line.indexOf(RESULT+SPLIT_EXPLAIN)+10,line.indexOf(TIME+SPLIT_EXPLAIN))).replaceAll("]", "").replaceAll(" ", "");
						mSaveDataModel.testTime = ((String) line.subSequence(line.indexOf(TIME+SPLIT_EXPLAIN)+7,line.length()));
						saveDataResult(mSaveDataModel, false);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}	
				 saveDataResult(null, true);
			}
			
			
			private String getEhglishName(String key,Context context){
				try {
						int resId = context.getResources().getIdentifier(key+"2","string",context.getPackageName());
						if (resId != 0){
							String name = context.getResources().getString(resId);
							if (!TextUtils.isEmpty(name)){
								return  name;
							}
						}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return key;
			}
			
			
	}
