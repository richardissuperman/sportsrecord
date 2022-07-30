package com.app.sportsrecord;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class SoundPlayer {
    MediaPlayer player = new MediaPlayer();
    private boolean playingSound;
    private Context mContext;
    private static int PLAY_TIMEOUT_MS = 3000;
    private static String BASKET_SOUND_PATH = "haha.mp3";

    public SoundPlayer(Context context) {
        mContext = context;
    }

    public synchronized void tryPlaySound(){
        if (playingSound) {
            throw new RuntimeException("cant not start playing another soundtrack while current one is still playing");
        }
        try {
            if (player != null) {
                player.release();
                player = null;
            }
            startPlayingTimer();
            playingSound  = true;
            playSound();
        }catch (Exception e) {
            Log.e("richard", "failed to play sound! " + e.toString());
        }
    }

    public synchronized boolean isPlaying(){
        return playingSound;
    }

    private void playSound() throws Exception{
        player = new MediaPlayer();
        AssetFileDescriptor afd = mContext.getAssets().openFd(BASKET_SOUND_PATH);
        player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
        player.prepare();
        Log.e("richard", "play sound!");
        player.start();
    }

    private void startPlayingTimer(){
        new Handler(Looper.myLooper()).postDelayed(() -> {
            synchronized (SoundPlayer.this) {
                playingSound = false;
            }
        }, PLAY_TIMEOUT_MS);
    }
}
