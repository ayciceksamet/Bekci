package com.example.aycicek.bequick;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.content.Context;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static Button open_camera_button;
    private Mat blur_background = null;
    private int counter;


    public int difficultydegree;
    public int cameraData;
    private Context context;

    static {
        System.loadLibrary("opencv_java3");
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        } else {
            Log.i("TAG", "OpenCV loaded successfully");

        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("LOG", "Trying to load OpenCV library");

        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        context = getApplicationContext();

        final MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.actiontheme);

        mediaPlayer.start();

        setDifficultydegree(40000);

        setCameraData(1);

        open_camera_button = (Button)findViewById(R.id.camera_open_button);

        open_camera_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                new CountDownTimer(5000, 1000){
                    public void onTick(long millisUntilFinished){
                        Toast.makeText(MainActivity.this, "Hold your phone steady in " + String.valueOf(TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)) + " seconds", Toast.LENGTH_SHORT).show();
                    }
                    public  void onFinish(){

                        Intent open_camera = new Intent(MainActivity.this, OpenCamera.class);
                        open_camera.putExtra("chosendifficulty",getDifficultydegree());
                        open_camera.putExtra("cameradata",getCameraData());
                        mediaPlayer.stop();
                        startActivity(open_camera);
                    }
                }.start();

            }
        });
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("SUCCESS", "OpenCV loaded successfully");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radioEasy:
                if (checked){
                    setDifficultydegree(40000);
                }
                    break;
            case R.id.radioNormal:
                if (checked){
                    setDifficultydegree(25000);
                }
                    break;
            case R.id.radioHard:
                if (checked){
                    setDifficultydegree(10000);
                }
                    break;
        }
    }

    public void onRadioButtonClickedCamera(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.front:
                if (checked){
                    setCameraData(1);
                }
                break;
            case R.id.back:
                if (checked){
                    setCameraData(0);
                }
                break;
        }
    }

    public int getDifficultydegree() {
        return difficultydegree;
    }

    public void setDifficultydegree(int difficultydegree) {
        this.difficultydegree = difficultydegree;
    }

    public int getCameraData() {
        return cameraData;
    }

    public void setCameraData(int cameraData) {
        this.cameraData = cameraData;
    }

    @Override
    public void onResume()
    {
        super.onResume();

    }

}
