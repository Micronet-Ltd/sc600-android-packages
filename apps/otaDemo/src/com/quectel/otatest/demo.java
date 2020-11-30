package com.quectel.otatest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RecoverySystem;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.NoSuchElementException;
import java.util.Scanner;

//import javax.net.ssl.HostnameVerifier;
//import javax.net.ssl.HttpsURLConnection;
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.SSLSession;
//import javax.net.ssl.TrustManager;
//import javax.net.ssl.X509TrustManager;
//import com.quectel.library.easyFTP;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;

//import it.sauronsoftware.ftp4j.FTPClient; 
//import it.sauronsoftware.ftp4j.FTPFile;
//import it.sauronsoftware.ftp4j.FTPDataTransferListener;


public class demo extends Activity {
    //    private EditText ip,username,password,spath,dest,dir;
    TextView destEdit,info;
    private ProgressDialog prg;

    String ip="220.180.239.212"; //172.18.105.75
    int port=21;
    String username="";
    String password="";
    String ftpRootDir= "/Smart/Telstra/";
    String ftpInfo= "ota_info.txt";
    String downloadFile=ftpRootDir+"update.zip";

    String dest="/data/media/0/update.zip";
    String dest2="/data/media/0/update.info";
    String dest_s="/data/media/0/update_s.zip";
    String spath="Public/temp/update.zip";
    String dir="Public/temp/";
    WakeLock mWakelock;
    String TAG="demo";
    String TAG1="OTAdemoLog=";
    Context mContext;
    //String urldir="https://quec-fat-oss2.oss-cn-shanghai.aliyuncs.com/fota/";
    //String urlstr="https://quec-fat-oss2.oss-cn-shanghai.aliyuncs.com/fota/update.zip";
    //String urlstrinfo="https://quec-fat-oss2.oss-cn-shanghai.aliyuncs.com/fota/ota_info.txt";//220.180.239.212 192.168.21.248
    //String urlstrinfo="https://172.18.104.219:8044/ota_info.txt";//220.180.239.212 192.168.21.248
    boolean flag=false;
    String localutc;
    long total=0;
    private ProgressDialog dialog;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(!dialog.isShowing()){
                dialog.show();
            }
            dialog.setProgress(msg.what);
            Log.i(TAG1,"setProgress==="+msg.what);
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext=this;
        setContentView(R.layout.activity_demo);
        PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
        mWakelock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "OTA Wakelock");
        //Removing Auto-focus of keyboard
        View view = this.getCurrentFocus();
        if (view != null) { //Removing on screen keyboard if still active
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        destEdit=(TextView) findViewById(R.id.dest);
        info=(TextView) findViewById(R.id.info);
       String sta=SystemProperties.get("ro.crypto.state");
        Log.d(TAG,"ro.crypto.state"+sta);
        if("encrypted".equals(sta)){
            dest=dest_s;
            Log.d(TAG,"ro.crypto.state");
        }
        localutc=SystemProperties.get("ro.build.date.utc");

        destEdit.setText(dest);
        info.setText("");

        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setTitle("正在下载");
        dialog.setMax(100);
    }


    void update1(){

        if(!flag)return;
        File recoveryFile = new File(dest);
        // first verify package
 /*       try {
            mWakelock.acquire();
			Log.d(TAG,"ota_file"+dest);
            RecoverySystem.verifyPackage(recoveryFile, recoveryVerifyListener, null);

        } catch (Exception e1) {

            e1.printStackTrace();
            showinfo("verify update.zip fail");
            flag=false;
            return;
        }finally {
            mWakelock.release();
            showMessage("verify update.zip done");
            if (!flag)showinfo("verify update.zip fail");
        }
*/
        // then install package
        try {
            mWakelock.acquire();
            RecoverySystem.installPackage(mContext, recoveryFile);
        } catch (Exception e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
            showMessage("install update.zip fail");
            flag=false;
        }  finally {
            mWakelock.release();
            showMessage("install update.zip ing..");
            if (!flag)showinfo("install update.zip fail");
        }
        // cannot reach here...
    }
	

    public  void  downloadHttp(String urlStr,String savePath){
        Log.d(TAG,"download : "+urlStr+" : "+savePath);
        flag=true;
        mWakelock.acquire();
        try{

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            final Certificate ca;
            AssetManager assetManager = mContext.getAssets();
            InputStream   caInput= assetManager.open("uwca.crt");

            try {
                ca = cf.generateCertificate(caInput);
//                Log.i("Longer", "ca=" + ((X509Certificate) ca).getSubjectDN());
//                Log.i("Longer", "key=" + ((X509Certificate) ca).getPublicKey());
            } finally {
                caInput.close();
            }
            // Create an SSLContext that uses our TrustManager
            //SSLContext context = SSLContext.getInstance("TLSv1","AndroidOpenSSL");
            /*context.init(null, new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain,
                                                       String authType)
                                throws CertificateException {

                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain,
                                                       String authType)
                                throws CertificateException {
                            for (X509Certificate cert : chain) {

                                // Make sure that it hasn't expired.
                                cert.checkValidity();

                                // Verify the certificate's public key chain.
                                try {
                                    cert.verify(((X509Certificate) ca).getPublicKey());
                                }  catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            }, null);*/

            URL url=new URL(urlStr);
            //HttpsURLConnection conn=(HttpsURLConnection)url.openConnection();
            //conn.setRequestMethod("POST");
            //conn.setSSLSocketFactory(context.getSocketFactory());
            /*conn.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });*/
            // int lengthOfFile = 96038693;
            //  lengthOfFile = conn.getContentLength();

            File file=new File(savePath);
            file.delete();

            //InputStream input=conn.getInputStream();



            OutputStream output=null;
            file.createNewFile();  //新建文件
            file.setWritable(true);
            output=new FileOutputStream(file);
            //读取大文件
            byte[] buffer=new byte[4*1024];
            long total = 0, count;
            /*while((count = input.read(buffer))!=-1&&flag){
                total += count;
                output.write(buffer,0,(int)count);
                showinfo("download progress size " + total);
            }*/
            output.flush();
            output.close();
            //input.close();


        }catch (Exception e){
            e.printStackTrace();
            showinfo("FAIL download: "+urlStr );
            flag=false;
        }finally {
            //  mWakelock.release();
        }
    }


    public  void update(View v){
        findViewById(R.id.update).setEnabled(false);
        new Thread(new Runnable() {
            public void run() {
 flag=true;
                //downloadHttp2(urlstrinfo,dest2);
				//downloadHttp3(ftpRootDir+ftpInfo,dest2);
                //parser();

                checkstaus();
                //downloadHttp2(urlstr,dest);
                //downloadHttp3(downloadFile,dest);
                checkstaus();
                update1();
                checkstaus();
            }
        }).start();

    }

/*
  public  void  downloadHttp2(String urlStr,String path){

 try {

             URL url = new URL(urlStr);
            try {
                HttpURLConnection hcont = (HttpURLConnection) url.openConnection();
                 hcont.connect();
                 InputStream is = hcont.getInputStream();
                double totalSize = hcont.getContentLength();
                Log.i(TAG1,"totalSize==="+totalSize);
                OutputStream os = new FileOutputStream(path);
                 int length;
                 double count = 0;
                byte [] bytes = new byte[1024*1024];
                while ((length = is.read(bytes))!= -1){
                   os.write(bytes,0,length);
                   count = count+length;
                    //Log.i(TAG1,"count==="+count+";total==="+totalSize+";string="+String.valueOf((count/totalSize)*1000)+";progress="+(int)((count/totalSize)*1000));
					handler.sendEmptyMessage((int)((count/totalSize)*100));
                 }
				 dialog.dismiss();
                 is.close();
                 os.close();
                 os.flush();
             } catch (IOException e) {
                e.printStackTrace();
             }
 
         } catch (MalformedURLException e) {
             e.printStackTrace();
         }
}
*/

/*
public  void  downloadHttp3(String path,String localPath){
        mWakelock.acquire();
	FTPClient client = new FTPClient();
	try {
		client.connect(ip, port);
		client.login(username, password);
	} catch (Exception e) {
		e.printStackTrace();
 flag=false;
showMessage("connect FTP fail");
	}

	MyFtpListener mDownloadListener = new MyFtpListener();

	try {
	File localFile=new File(localPath);
	localFile.deleteOnExit();
	localFile.createNewFile();
	int remoteFileSize = (int) client.fileSize(path);
	 Log.d(TAG,"RemoteFileSize = " + remoteFileSize);
	client.download(path, localFile, mDownloadListener);
	} catch (Exception e) {
		e.printStackTrace();	
	flag=false;
	showMessage("download  fail");
	}

	closeConnection(client);
	mWakelock.release();
}
*/

 
    public  void checkstaus(){
        if(!flag){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.update).setEnabled(true);
                }
            });

            return;
        }
    }
/*
    public void parser(){
        File fileinfo=new File(dest2);
        try {
            FileReader reader = new FileReader(fileinfo);
            BufferedReader in = new BufferedReader(reader);
            String str;
            while ((str = in.readLine()) != null) {
                Log.d(TAG,str);
                String[] strs= str.split("\\|");
                Log.d(TAG,":"+strs[0]+":::"+strs[1]+":");
                if(localutc.equals(strs[0])){
                    downloadFile=ftpRootDir+ strs[1];
					urlstr=urldir+ strs[1];
                    Log.d(TAG,"set new url "+urlstr);
                }

            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/


    RecoverySystem.ProgressListener recoveryVerifyListener = new RecoverySystem.ProgressListener() {
        public void onProgress(int progress) {
            Log.d(TAG, "verify progress" + progress);
            final int progress1=progress;
            showinfo("verify progress " + progress1+" %");
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    prg = new ProgressDialog(demo.this);
//                    prg.setMessage("verify progress" + progress1);
//                    prg.show();
//                }
//            });

        }
    };



    class downloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            prg = new ProgressDialog(demo.this);
            prg.setMessage("downloading...");
            prg.show();
            mWakelock.acquire();
            showinfo("downloading...");
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                File file=new File(dest);
                file.deleteOnExit();
                easyFTP ftp = new easyFTP();
                InputStream is=getResources().openRawResource(+R.drawable.easyftptest);
                ftp.connect(params[0],params[1],params[2]);
                ftp.downloadFile(params[3],params[4]);
                return new String("Download Successful");
            }catch (Exception e){
                String t="Failure : " + e.getLocalizedMessage();
                return t;
            }
        }

        @Override
        protected void onPostExecute(String str) {
            prg.dismiss();
            mWakelock.release();
            Toast.makeText(demo.this,str,Toast.LENGTH_LONG).show();
            findViewById(R.id.down).setEnabled(true);
            showinfo(str);
        }
    }



    public  void down(View v){
        findViewById(R.id.down).setEnabled(false);
        String address=ip,u=username,p=password,serverPath=spath,destination=dest;
        downloadTask async=new downloadTask();
        async.execute(address,u,p,serverPath,destination);//Passing arguments to AsyncThread
    }

    void showMessage(final String str){

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mContext,str,Toast.LENGTH_SHORT).show();
                    info.setText(str);
                }
            });



    }

    void showinfo(final String str){

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    info.setText(str);
                }
            });

        }

/*
	public void closeConnection(FTPClient client){
		try {
			client.disconnect(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
*/
/*
	public class MyFtpListener implements FTPDataTransferListener {

		@Override
		public void aborted() {
			// TODO Auto-generated method stub
			Log.d(TAG,"aborted");
		}

		@Override
		public void completed() {
			// TODO Auto-generated method stub
			Log.d(TAG,"completed");
		}

		@Override
		public void failed() {
			// TODO Auto-generated method stub
			Log.d(TAG, "failed");
		}

		@Override
		public void started() {
			// TODO Auto-generated method stub
			Log.d(TAG,"started");
			total=0;
		}

		@Override
		public void transferred(int arg0) {
			// TODO Auto-generated method stub
			total += arg0;
			if(total>(1024*1024)){
				showinfo("download progress size " + total/(1024*1024) +" M");
				}else {
				showinfo("download progress size " + total/1024 +" kb");
				}
//			Log.d(TAG,"transferred  length = " + arg0);
		}
	}
*/
}
