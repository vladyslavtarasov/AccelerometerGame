package com.example.accelerometergame;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class AccelerometerGameActivity extends AppCompatActivity implements SensorEventListener {

    private Sensor sensor;
    private Sensor proximitySensor;
    private SensorManager sensorManager;
    private SensorManager proximityManager;
    private AnimatedView animatedView = null;
    private static final int SQUARE_ADD_INTERVAL = 500;
    private Handler handler;
    private Runnable squareAddRunnable;
    private int squareCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);

        animatedView = new AnimatedView(this);
        setContentView(animatedView);

        handler = new Handler();
        squareAddRunnable = new Runnable() {
            @Override
            public void run() {
                if (animatedView.addSquare()) squareCount++;

                if (squareCount > 10) {
                    handleGameLost();
                    return;
                }

                handler.postDelayed(this, SQUARE_ADD_INTERVAL);
            }
        };
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            animatedView.onSensorEvent(event);
        } else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            float proximityValue = event.values[0];
            if (proximityValue < proximitySensor.getMaximumRange()) {
                recreate();
            }
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

    private void handleGameLost() {
        sensorManager.unregisterListener(this);
        handler.removeCallbacks(squareAddRunnable);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Game Over");
        builder.setMessage("You have lost the game. Do you want to go to the main menu or start again?");
        builder.setPositiveButton("Main Menu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("Start Again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                restartGame();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void restartGame() {
        squareCount = 0;
        animatedView.clearSquares();

        recreate();
    }
}