package com.example.aycicek.bekci;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class open_camera extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean              mIsJavaCamera = true;
    private boolean  inputFrame_first_time = true;
    private MenuItem mItemSwitchCamera = null;
    private Mat mYuvFrameData;
    private Mat mRotated;
    private Mat current_frame;
    private Mat previous_frame;
    private Mat diff_frame;
    private boolean motionDetected = false;
    List<MatOfPoint> contours;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public open_camera() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.open_camera);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
    }




    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        previous_frame = new Mat(width, height, CvType.CV_64FC4);
        current_frame = new Mat(width, height, CvType.CV_64FC4);
        diff_frame = new Mat(width, height, CvType.CV_64FC4);

    }


    public void onCameraViewStopped() {
    }

    public Mat isMotionAvailable(Mat proccessingFrame){
        proccessingFrame.copyTo(current_frame);

        if(inputFrame_first_time){
            proccessingFrame.copyTo(previous_frame);
            inputFrame_first_time = false;
            Log.i("FIRST FRAME GET","FRAME LOG");
        }
        Core.absdiff(current_frame,previous_frame,diff_frame);
        proccessingFrame.copyTo(previous_frame);

        return diff_frame;
    }

    public boolean isMotionReal(Mat processingRealFrame, List<MatOfPoint> contours ){

        for(int i=0; i< contours.size();i++){
            if (Imgproc.contourArea(contours.get(i)) > 500 ){
                org.opencv.core.Rect rect = Imgproc.boundingRect(contours.get(i));
                if (rect.height > 28){
                    motionDetected=true;
                }
            }
        }
        return motionDetected;
    }

    public void processAlarm(boolean motionDetected) {
        if(motionDetected) {
            playAlarm();

        }
        else{
            stopAlarm();
        }

    }

    private void stopAlarm() {
        MediaPlayer mp = MediaPlayer.create(this,
                R.raw.alarm4);
        mp.stop();
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }

        });

    }
    private void playAlarm() {
        MediaPlayer mp = MediaPlayer.create(this,
                R.raw.alarm4);
        if(!mp.isPlaying()) {
            mp.start();
        }
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }

        });

    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat mRgba = inputFrame.rgba();
        Imgproc.pyrDown(mRgba, mRgba);
        mRgba = isMotionAvailable(mRgba);
        Mat element = Imgproc.getStructuringElement(0, new org.opencv.core.Size(2*2 + 1, 2*2+1),new org.opencv.core.Point(2,2));
        Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_BGRA2GRAY);
        Imgproc.dilate(mRgba, mRgba, element);
        Imgproc.threshold(mRgba, mRgba, 50, 255,Imgproc.THRESH_BINARY);
        contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(mRgba, contours, new Mat(), Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
        motionDetected = isMotionReal(mRgba,contours);
        processAlarm(motionDetected);
        motionDetected=false;
        Imgproc.resize(mRgba,mRgba,inputFrame.rgba().size());
        return mRgba;
    }



}