package com.example.aycicek.bekci;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.widget.ImageView;
import jp.wasabeef.blurry.Blurry;
import android.graphics.Color;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.nio.channels.DatagramChannel;

public class MainActivity extends AppCompatActivity {

    private static Button open_camera_button;
    private Mat blur_background = null;

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

        open_camera_button = (Button)findViewById(R.id.camera_open_button);

        open_camera_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent open_camera = new Intent(MainActivity.this, open_camera.class);
                startActivity(open_camera);
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



    @Override
    public void onResume()
    {
        super.onResume();

    }






}
