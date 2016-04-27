package com.swatiag1101.sunshine_new_v1;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.util.Log;

/**
 * Created by Swati Agarwal on 15-03-2016.
 */
public class MyView extends View {

    private Paint paint;
    public MyView(Context context) {
        super(context);

        paint = new Paint();
        paint.setColor(Color.GRAY);
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    private float degreesNorth = 0;
    private float degreesSouth = 180;


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
                MeasureSpec.getSize(widthMeasureSpec),
                MeasureSpec.getSize(heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        float centerX = getWidth() / 2;
        float centerY = getHeight() / 2;
        float minOfXY = Math.min(centerX, centerY);

        int borderStrokeWidth = 4;
        Paint border = new Paint(Paint.ANTI_ALIAS_FLAG);
        border.setColor(getResources().getColor(R.color.sunshine_blue));
        border.setStrokeWidth(borderStrokeWidth);
        border.setStyle(Paint.Style.STROKE);

        Paint fillBlue = new Paint();
        fillBlue.setColor(getResources().getColor(R.color.sunshine_dark_blue));
        fillBlue.setStyle(Paint.Style.FILL);

        float borderInner = 0.8f;
        Paint fillWhite = new Paint();
        fillWhite.setColor(Color.WHITE);
        fillWhite.setStyle(Paint.Style.FILL);

        float needleLength = 0.75f;
        int needleStrokeWidth = 8;
        Paint needleNorth = new Paint();
        needleNorth.setColor(Color.RED);
        needleNorth.setStyle(Paint.Style.FILL);
        needleNorth.setStrokeWidth(needleStrokeWidth);

        Paint needleSouth = new Paint();
        needleSouth.setColor(getResources().getColor(R.color.grey));
        needleSouth.setStyle(Paint.Style.FILL);
        needleSouth.setStrokeWidth(needleStrokeWidth);

        // Draw outer border.
        canvas.drawCircle(centerX, centerY, minOfXY, fillBlue);
        canvas.drawCircle(centerX, centerY, minOfXY - (borderStrokeWidth / 2), border);

        // Draw inner border.
        canvas.drawCircle(centerX, centerY, minOfXY * borderInner, fillWhite);
        canvas.drawCircle(centerX, centerY, minOfXY * borderInner, border);

        // Draw needle.
        canvas.drawLine(centerX, centerY,
                (float)(centerX + (centerX * needleLength * Math.sin(Math.toRadians(degreesSouth)))),
                (float)(centerY - (centerY * needleLength * Math.cos(Math.toRadians(degreesSouth)))),
                needleSouth);
        canvas.drawLine(centerX, centerY,
                (float)(centerX + (centerX * needleLength * Math.sin(Math.toRadians(degreesNorth)))),
                (float)(centerY - (centerY * needleLength * Math.cos(Math.toRadians(degreesNorth)))),
                needleNorth);

        invalidate();
    }

    public void setDegrees(float degrees) {
        degreesNorth = degrees;
        degreesSouth = degrees + 180;
    }
}
