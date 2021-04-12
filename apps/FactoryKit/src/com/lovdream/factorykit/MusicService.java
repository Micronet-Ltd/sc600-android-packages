package com.lovdream.factorykit;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import com.lovdream.factorykit.R;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
	
	private MediaPlayer mediaPlayer;

    public void onCreate(){
        super.onCreate();
        mediaPlayer = MediaPlayer.create(this, R.raw.test);
        mediaPlayer.setOnCompletionListener(this);
    }
    
    
    @Override
    public int onStartCommand (Intent intent,int flag, int startId) {
        super.onStartCommand(intent,flag,startId);
            if(!mediaPlayer.isPlaying()){
                mediaPlayer.start();
                mediaPlayer.setLooping(true);
            }
        return START_STICKY;
    }

    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        mediaPlayer.release();

    }
    
    public class MyMusicBinder extends Binder{
        public MusicService getService(){
            return MusicService.this;
        }
    }


    private final IBinder binder = new MyMusicBinder();  

	@Override
	public IBinder onBind(Intent intent) {
	
		 if(!mediaPlayer.isPlaying()){
	            mediaPlayer.start();
	            mediaPlayer.setLooping(true);
	        }
	        return binder;
	}
	
	@Override
    public boolean onUnbind(Intent intent) {
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        return super.onUnbind(intent);
    }
	


	@Override
	public void onCompletion(MediaPlayer arg0) {
		 stopSelf();
	}

}
