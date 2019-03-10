package com.example.aycicek.bequick;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.os.*;
import android.widget.Button;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.Core.FONT_HERSHEY_TRIPLEX;
import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.drawContours;
import static org.opencv.imgproc.Imgproc.putText;

public class OpenCamera extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";
    private static final String TAG_DEBUG = "debug::oncamera";

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean inputFrame_first_time = true;
    private Mat current_frame;
    private Mat previous_frame;
    private Mat diff_frame;
    private boolean shakeDetected = false;
    private boolean motionDetected = false;
    private boolean lockSystem = false;
    private boolean userisReady = false;
    private ShakeListener mShaker;
    List<MatOfPoint> contours;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private int difficultydegree;
    boolean loaded = false;
    private Context context;
    private MediaPlayer mediaPlayerFail;
    private MediaPlayer mediaPlayerTimecount;
    private MediaPlayer mediaPlayerTheme;
    private int cameraData;
    private Vibrator v;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public OpenCamera() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        if (ContextCompat.checkSelfPermission(OpenCamera.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(OpenCamera.this,
                    new String[]{Manifest.permission.CAMERA},
                    MY_CAMERA_REQUEST_CODE);
        }
        context = getApplicationContext();
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.open_camera);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            setDifficultydegree(extras.getInt("chosendifficulty"));
            setCameraData(extras.getInt("cameradata"));
        }

        mediaPlayerTheme = MediaPlayer.create(context, R.raw.missionimpossibletheme);
        mediaPlayerTimecount = MediaPlayer.create(context, R.raw.countdownready);
        mediaPlayerFail = MediaPlayer.create(context, R.raw.failalarmsound);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_preview);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setCameraIndex(getCameraData());

        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mShaker = new ShakeListener(this);
        mShaker.setOnShakeListener(new ShakeListener.OnShakeListener() {
            public void onShake() {
                setShakeDetected(true);
                startTimer();
                Log.i(TAG, "On shake");
            }

            public void onNoShake() {
                setShakeDetected(false);
                Log.i(TAG, "No shake");
            }
        });
    }

    @Override
    public void onPause() {
        mShaker.pause();
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        mShaker.resume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
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

    private void playTheme(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(isUserisReady()){
                    mediaPlayerTheme.start();
                }
            }
        }).start();
    }

    private void vibrate(){
    // Start without a delay
    // Each element then alternates between vibrate, sleep, vibrate, sleep...
        long[] pattern = {0, 100, 1000};

    // The '-1' here means to vibrate once, as '-1' is out of bounds in the pattern array
        v.vibrate(pattern, -1);

    }
    public void onCameraViewStopped() {
    }

    public Mat isMotionAvailable(Mat proccessingFrame) {
        proccessingFrame.copyTo(current_frame);

        if (inputFrame_first_time) {
            proccessingFrame.copyTo(previous_frame);
            inputFrame_first_time = false;
            Log.i("FIRST FRAME GET", "FRAME LOG");
        }
        Core.absdiff(current_frame, previous_frame, diff_frame);
        proccessingFrame.copyTo(previous_frame);

        return diff_frame;
    }

    public boolean isMotionReal(Mat processingRealFrame, List<MatOfPoint> contours) {

        for (int i = 0; i < contours.size(); i++) {
            if (Imgproc.contourArea(contours.get(i)) > getDifficultydegree()) {
                org.opencv.core.Rect rect = Imgproc.boundingRect(contours.get(i));
             //   if (rect.height > 28) {
                    motionDetected = true;
          //      }
            }
        }
        return motionDetected;
    }

    public void processAlarm(boolean motionDetected) {
        if (motionDetected) {
            mediaPlayerFail.start();
            Log.d(TAG_DEBUG,"FAIL ALARIM!!!");
        }
    }

    public void startTimer() {

        if (!isLockSystem()) {
            Handler handler = new Handler();
            setLockSystem(true);
            Log.d(TAG_DEBUG, " The system is locked !");

            int secs = 5; // Delay in seconds

            Utils.delay(secs, new Utils.DelayCallback() {
                @Override
                public void afterDelay() {
                    setLockSystem(false);
                    Log.d(TAG_DEBUG, " The system is unlocked !");
                    v.cancel();
                }
            });
        } else {
            Log.d(TAG_DEBUG, " The system is already locked !");
        }
    }

    public int getDifficultydegree() {
        return difficultydegree;
    }

    public void setDifficultydegree(int difficultydegree) {
        this.difficultydegree = difficultydegree;
    }

    public boolean isShakeDetected() {
        return shakeDetected;
    }

    public void setShakeDetected(boolean shakeDetected) {
        this.shakeDetected = shakeDetected;
    }

    public boolean isMotionDetected() {
        return motionDetected;
    }

    public void setMotionDetected(boolean motionDetected) {
        this.motionDetected = motionDetected;
    }

    public boolean isLockSystem() {
        return lockSystem;
    }

    public void setLockSystem(boolean lockSystem) {
        this.lockSystem = lockSystem;
    }

    public void readyButtonClicked(View v){
        setUserisReady(true);
        playTheme();
    }

    public boolean isUserisReady() {
        return userisReady;
    }

    public void setUserisReady(boolean userisReady) {
        Button button_confirm = (Button) findViewById(R.id.button_confirm);
        this.userisReady = userisReady;
        button_confirm.setVisibility(View.GONE);
        mediaPlayerTimecount.start();
    }

    public int getCameraData() {
        return this.cameraData;
    }

    public void setCameraData(int cameraData) {
        this.cameraData = cameraData;
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        Mat mRgba = inputFrame.rgba();
        Mat mRgbaFinal = inputFrame.rgba();
        Imgproc.pyrDown(mRgba, mRgba);
        mRgba = isMotionAvailable(mRgba);
        Mat element = Imgproc.getStructuringElement(0, new org.opencv.core.Size(2 * 2 + 1, 2 * 2 + 1), new org.opencv.core.Point(2, 2));
        Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_BGRA2GRAY);
        Imgproc.dilate(mRgba, mRgba, element);
        Imgproc.threshold(mRgba, mRgba, 50, 255, Imgproc.THRESH_BINARY);
        contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.pyrUp(mRgba, mRgba);
        Imgproc.findContours(mRgba, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        motionDetected = isMotionReal(mRgba, contours);


        if(!isLockSystem() && isUserisReady()){
            processAlarm(motionDetected);
        }

        Imgproc.resize(mRgba, mRgba, inputFrame.rgba().size());
        Imgproc.resize(mRgbaFinal, mRgbaFinal, inputFrame.rgba().size());

        if (!isShakeDetected()) {
            if (!contours.isEmpty() && motionDetected) {
                for (int i = 0; i < contours.size(); i++) {
                    MatOfPoint matOfPoint = contours.get(i);
                    Rect rect = Imgproc.boundingRect(matOfPoint);
                    Scalar color = new Scalar( 255, 255, 0);
                    if(Imgproc.contourArea(contours.get(i)) > getDifficultydegree()) {
                        putText(mRgbaFinal," +!!! ALARM !!!",new Point(500,500),
                                FONT_HERSHEY_TRIPLEX, 1,new Scalar( 255, 255, 0),1);
                        drawContours(mRgbaFinal, contours, i, color, -1);
                    }
                }
            }
        } else {
            /*putText(mRgbaFinal, "Hold the phone steady !", new org.opencv.core.Point(250, 250),
                    Core.FONT_HERSHEY_SIMPLEX,
                    2.6f,
                    new Scalar(155, 155, 0), // color in BGR format, you should change this one
                    3 // thickness (can be used to achieve bold
            );*/
            vibrate();
        }
        Scalar colorfilter = new Scalar( 10, 150, 10);
        Mat overlay = new Mat(mRgbaFinal.rows(),mRgbaFinal.cols(),CV_8UC3,colorfilter);
        cvtColor(overlay,overlay,Imgproc.COLOR_BGR2RGB);
        cvtColor(mRgbaFinal,mRgbaFinal,Imgproc.COLOR_BGR2RGB);
        Core.addWeighted(overlay, 0.4, mRgbaFinal, 0.6, 1,  mRgbaFinal);
        motionDetected = false;
        Core.flip(mRgbaFinal,mRgbaFinal,1);
        return mRgbaFinal;
    }
}