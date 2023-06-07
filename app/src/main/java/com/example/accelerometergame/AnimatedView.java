package com.example.accelerometergame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.SensorEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class AnimatedView extends View {

    private static final int CIRCLE_RADIUS = 30;
    private static final int BORDER_WIDTH = 10;
    private static final int SQUARE_SIZE = 30;
    private final Paint ballPaint, borderPaint, squarePaint;

    private int x;
    private int y;

    private int width;
    private int height;

    private final List<Square> squares;

    public AnimatedView (Context context) {
        super(context);

        ballPaint = new Paint();
        ballPaint.setColor(Color.BLUE);

        borderPaint = new Paint();
        borderPaint.setColor(Color.GREEN);

        squarePaint = new Paint();
        squarePaint.setColor(Color.RED);

        squares = new ArrayList<>();
    }

    public boolean addSquare() {
        int x = (int) (Math.random() * (width - SQUARE_SIZE));
        int y = (int) (Math.random() * (height - SQUARE_SIZE));
        Square newSquare = new Square(x, y, SQUARE_SIZE);

        for (Square square : squares) {
            if (checkCollision(newSquare, square)) {
                return false;
            }
        }

        if (x < BORDER_WIDTH || y < BORDER_WIDTH || x + SQUARE_SIZE > width - BORDER_WIDTH ||
                y + SQUARE_SIZE > height - BORDER_WIDTH) {
            return false;
        }

        squares.add(newSquare);
        return true;
    }

    private boolean checkCollision(Square square1, Square square2) {
        int distanceBetweenSquares = SQUARE_SIZE / 2;
        return square1.getX() < square2.getX() + square2.getSize() + distanceBetweenSquares &&
                square1.getX() + square1.getSize() + distanceBetweenSquares > square2.getX() &&
                square1.getY() < square2.getY() + square2.getSize() + distanceBetweenSquares &&
                square1.getY() + square1.getSize() + distanceBetweenSquares > square2.getY();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;

        x = w / 2;
        y = h / 2;
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
                    square.getX() + square.getSize(), square.getY() + square.getSize(), squarePaint);
        }

        invalidate();
    }

    public void clearSquares() {
        squares.clear();
    }
}
