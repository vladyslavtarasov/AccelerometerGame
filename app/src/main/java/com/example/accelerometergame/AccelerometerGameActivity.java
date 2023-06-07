package com.example.accelerometergame;

import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class AccelerometerGameActivity extends AppCompatActivity implements SensorEventListener {


    private Sensor sensor;
    private SensorManager sensorManager;
    private AnimatedView animatedView = null;
    private static final int SQUARE_ADD_INTERVAL = 500;
    private Handler handler;
    private Runnable squareAddRunnable;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        animatedView = new AnimatedView(this);
        setContentView(animatedView);

        handler = new Handler();
        squareAddRunnable = new Runnable() {
            @Override
            public void run() {
                animatedView.addSquare();
                handler.postDelayed(this, SQUARE_ADD_INTERVAL);
            }
        };


    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            animatedView.onSensorEvent(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);

        handler.removeCallbacks(squareAddRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        handler.postDelayed(squareAddRunnable, SQUARE_ADD_INTERVAL);
    }

}