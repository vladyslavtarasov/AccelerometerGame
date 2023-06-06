package com.example.accelerometergame;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

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

    public class AnimatedView extends View {

        private static final int CIRCLE_RADIUS = 25;
        private static final int BORDER_WIDTH = 10;
        private final Paint ballPaint;
        private final Paint borderPaint;

        private int x;
        private int y;

        private int width;
        private int height;

        private final List<Square> squares;

        public AnimatedView (Context context) {
            super(context);

            ballPaint = new Paint();
            ballPaint.setColor(Color.GREEN);

            borderPaint = new Paint();
            borderPaint.setColor(Color.RED);

            squares = new ArrayList<>();
        }

        public void addSquare() {
            int size = 100;
            int x = (int) (Math.random() * (width - size));
            int y = (int) (Math.random() * (height - size));
            Square square = new Square(x, y, size);
            squares.add(square);
        }

        public void removeSquare(Square square) {
            squares.remove(square);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            width = w;
            height = h;
        }

        public void onSensorEvent(SensorEvent event) {
            int scalingFactor = 3;

            int newX = x - (int) (event.values[0] * scalingFactor);
            int newY = y + (int) (event.values[1] * scalingFactor);

            if (newX <= CIRCLE_RADIUS + BORDER_WIDTH / 2) {
                newX = CIRCLE_RADIUS + BORDER_WIDTH / 2;
            } else if (newX >= width - CIRCLE_RADIUS - BORDER_WIDTH / 2) {
                newX = width - CIRCLE_RADIUS - BORDER_WIDTH / 2;
            }

            if (newY <= CIRCLE_RADIUS + BORDER_WIDTH / 2) {
                newY = CIRCLE_RADIUS + BORDER_WIDTH / 2;
            } else if (newY >= height - CIRCLE_RADIUS - BORDER_WIDTH / 2) {
                newY = height - CIRCLE_RADIUS - BORDER_WIDTH / 2;
            }

            x = newX;
            y = newY;

            for (int i = 0; i < squares.size(); i++) {
                Square square = squares.get(i);

                if (x + CIRCLE_RADIUS > square.getX() &&
                        x - CIRCLE_RADIUS < square.getX() + square.getSize() &&
                        y + CIRCLE_RADIUS > square.getY() &&
                        y - CIRCLE_RADIUS < square.getY() + square.getSize()) {
                    squares.remove(i);
                    i--;
                }
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(BORDER_WIDTH);

            canvas.drawLine(0, 0, width, 0, borderPaint);
            canvas.drawLine(0, 0, 0, height, borderPaint);
            canvas.drawLine(width, 0, width, height, borderPaint);
            canvas.drawLine(0, height, width, height, borderPaint);

            canvas.drawCircle(x, y, CIRCLE_RADIUS, ballPaint);

            for (Square square : squares) {
                canvas.drawRect(square.getX(), square.getY(),
                        square.getX() + square.getSize(), square.getY() + square.getSize(), borderPaint);
            }

            invalidate();
        }
    }
}