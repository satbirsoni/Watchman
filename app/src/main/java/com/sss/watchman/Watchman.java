package com.sss.watchman;

import android.app.Activity;
import android.graphics.Bitmap;

import Interfaces.BaseAlarmManager;
import Interfaces.BaseImage;
import Interfaces.BaseImageCompare;
import Interfaces.BaseImageSource;
import Interfaces.ImageChangedCallback;
import Interfaces.UserInterface;
import factory.Factory;

/**
 * Created by satbir on 12/02/17.
 */

public class Watchman implements ImageChangedCallback{
    /**
     *  Will decide image capture frequency
     */
     private int mImageIntervalSec;

    /**
     * On what difference alarm will be raised
     */
    private int mAlarmThresholdPercentage = 5000;

    private BaseImageSource mImageSource = null;
    private BaseImageCompare mBaseImageCompare =null;
    private BaseImage lastImage = null;
    private BaseAlarmManager mAlarmManager= null;
    private UserInterface mImageChangedCallback = null;


    public Watchman(Activity activity, UserInterface cb)
    {
        mImageChangedCallback = cb;
        Factory f = new Factory(activity, this);
        mImageSource = f.getImageSource();
        mBaseImageCompare = f.getImageCompare();
        mAlarmManager = f.getAlertManager();
        mAlarmThresholdPercentage = 10;
        mImageIntervalSec = 1;
    }

    public void setmAlarmThresholdPercentage(final int val){
        mAlarmThresholdPercentage = val;
    }

    public int getmAlarmThresholdPercentage(){
       return mAlarmThresholdPercentage;
    }


    void start() throws InterruptedException {
        mImageSource.start(mImageIntervalSec);
    }

    @Override
    public void onImageChanged(BaseImage image) {
           if(lastImage ==null){
               lastImage = image;
               return;
           }
        BaseImageCompare.Result result = mBaseImageCompare.getDifference(lastImage, image);

        if( result.diff > mAlarmThresholdPercentage){
            mAlarmManager.raiseAlarm(BaseAlarmManager.FailureType.Breach, result.diff);
        }
        mImageChangedCallback.onDifference(result.diff);


        lastImage = image;
        try {
            mImageChangedCallback.onImageChanged(result.bmp);
        }catch (Exception e) { e.printStackTrace();}

    }

    @Override
    public void onFailure() {
        mImageChangedCallback.onFailure();
        mImageSource.stop();

    }
    @Override public void onMessage(String msg){

    }

    void stop(){
        mImageSource.stop();
    }
}
