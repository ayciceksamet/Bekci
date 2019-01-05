package com.example.aycicek.bequick;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

/**
 * Created by aycicek on 11/4/2018.
 */

public class SoundManager {

    private static SoundManager singleton;

    SoundPool soundPool;

    int ACTION_SOUND;
    int FAIL_ALARM_SOUND;
    int TRY_AGAIN_SOUND;

    public SoundManager(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            soundPool = (new SoundPool.Builder()).setMaxStreams(1).build();
        }else{
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 5);
        }
    }

    public void loadSound(Context context){
        ACTION_SOUND        = soundPool.load(context,R.raw.actiontheme,1);
        FAIL_ALARM_SOUND    = soundPool.load(context,R.raw.failalarmsound,1);
        TRY_AGAIN_SOUND     = soundPool.load(context,R.raw.tryagainlater,1);
    }

    public void playActionSound(){
        soundPool.play(ACTION_SOUND, 1.0F, 1.0F, 0, -1, 1.0F);
    }

    public void playFailAlarmSound(){
        soundPool.play(FAIL_ALARM_SOUND, 1.0F, 1.0F, 0, 0, 1.0F);
    }

    public void playTryAgainSound(){
        soundPool.play(TRY_AGAIN_SOUND, 1.0F, 1.0F, 0, 0, 1.0F);
    }

    public static void initialize(Context context){
        SoundManager soundManager = getInstance();
        soundManager.loadSound(context);
    }

    public static synchronized SoundManager getInstance(){
        if(singleton == null){
            singleton = new SoundManager();
        }
        return singleton;
    }
}
