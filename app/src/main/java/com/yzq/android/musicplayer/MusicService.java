package com.yzq.android.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import java.io.IOException;

public class MusicService extends Service{
    private MediaPlayer mediaPlayer;
    private final IBinder binder = new MyBinder();
    private boolean isStop = false;

    public class MyBinder extends Binder{
        MusicService getService() {
            return MusicService.this;
        }
    }

    public MusicService() {}

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    public boolean isValid() {
        return (mediaPlayer != null && mediaPlayer.isPlaying());
    }

    public void load(String path) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.setLooping(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        isStop = false;
    }

    public void play(){
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            if (isStop) {
                try {
                    mediaPlayer.prepare();
                    isStop = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mediaPlayer.start();
        }
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            isStop = true;
        }
    }

    public void seek(int progress) {
        if (mediaPlayer != null && !isStop)
            mediaPlayer.seekTo(progress);
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }
}
